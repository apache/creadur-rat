/* SPDX-License-Identifier: Apache-2.0 */
package org.apache.rat.annotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.rat.Defaults;
import org.apache.rat.analysis.HeaderCheckWorker;
import org.apache.rat.analysis.TikaProcessor;
import org.apache.rat.analysis.UnknownLicense;
import org.apache.rat.api.Document;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.document.FileDocument;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.report.claim.LicenseAddingReport;
import org.apache.tika.mime.MediaType;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class MarkdownFrontMatterTest {
    /** Native line endings used by the appender. */
    private static final String LINE_SEPARATOR = System.lineSeparator();
    /** TOML metadata with arrays, a table, comments and an indented delimiter. */
    private static final String TOML = """
            +++
            title = "Community Guidelines"
            # Metadata comment
            tags = ["community", "docs"]
            [params]
            description = \"""
              +++
            \"""
            +++
            """;
    /** Nested JSON metadata with delimiters, escaped quotes and backslashes inside strings. */
    private static final String JSON = """
            {
              "title": "Community Guidelines",
              "params": {"tags": ["docs", {"enabled": true}], "weight": 500},
              "description": "Braces } { and brackets ] [ and quote \\\"",
              "path": "C:\\\\docs\\\\"
            }
            """;
    /** Body markers must not be mistaken for another front matter block. */
    private static final String BODY = "\n# Community Guidelines\n\n+++\n{\"body\": true}\nText\n";

    @TempDir
    private Path directory;

    static Stream<Arguments> frontMatterInputs() {
        List<Arguments> inputs = new ArrayList<>();
        for (String frontMatter : List.of(TOML, JSON)) {
            for (String separator : List.of("\n", "\r\n", "\r")) {
                for (boolean overwrite : List.of(false, true)) {
                    for (boolean bom : List.of(false, true)) {
                        inputs.add(Arguments.of(frontMatter, separator, overwrite, bom));
                    }
                }
            }
        }
        return inputs.stream();
    }

    @ParameterizedTest
    @MethodSource("frontMatterInputs")
    void preserveFrontMatter(final String frontMatter, final String separator, final boolean overwrite, final boolean bom) throws Exception {
        File file = directory.resolve("example.md").toFile();
        String input = (bom ? "\uFEFF" : "") + (frontMatter + BODY).replace("\n", separator);
        Files.writeString(file.toPath(), input, StandardCharsets.UTF_8);
        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender();
        appender.setOverwrite(overwrite);

        appender.append(file);

        Path output = overwrite ? file.toPath() : Path.of(file + ".new");
        assertThat(Files.readString(output, StandardCharsets.UTF_8))
                .isEqualTo(frontMatter.replace("\n", LINE_SEPARATOR) + LINE_SEPARATOR
                        + appender.getLicenseHeader(file) + LINE_SEPARATOR + BODY.replace("\n", LINE_SEPARATOR));
        if (overwrite) {
            assertThat(Path.of(file + ".new")).doesNotExist();
        } else {
            assertThat(Files.readString(file.toPath(), StandardCharsets.UTF_8)).isEqualTo(input);
        }
        ILicense license = LicenseSetFactory.search("AL", "AL2.0",
                Defaults.builder().build().getLicenseSetFactory().getLicenses(LicenseFilter.ALL)).orElseThrow();
        try (BufferedReader reader = Files.newBufferedReader(output, StandardCharsets.UTF_8)) {
            assertThat(license.matches(HeaderCheckWorker.readHeader(reader,
                    HeaderCheckWorker.DEFAULT_NUMBER_OF_RETAINED_HEADER_LINES))).isTrue();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"+++\n+++", "+++\ntitle = \"Example\"\n+++\n", "{}", "{}\n",
            "{\"nested\": [{\"key\": \"value\"}]}", "  {\"title\": \"Example\"}  \n", "{\n}\n"})
    void frontMatterAtEndOfFile(final String input) throws IOException {
        File file = directory.resolve("example.md").toFile();
        Files.writeString(file.toPath(), input, StandardCharsets.UTF_8);
        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender();

        appender.append(file);

        String frontMatter = input.endsWith("\n") ? input : input + "\n";
        assertThat(Files.readString(Path.of(file + ".new")))
                .isEqualTo(frontMatter.replace("\n", LINE_SEPARATOR) + LINE_SEPARATOR
                        + appender.getLicenseHeader(file) + LINE_SEPARATOR);
        assertThat(Files.readString(file.toPath())).isEqualTo(input);
    }

    static Stream<Arguments> invalidFrontMatter() {
        List<Arguments> inputs = new ArrayList<>();
        for (boolean overwrite : List.of(false, true)) {
            inputs.add(Arguments.of("+++\ntitle = \"Example\"\n...\n---\n  +++\n", overwrite, "Unterminated TOML front matter"));
            inputs.add(Arguments.of("{\n  \"title\": \"Example\"\n", overwrite, "Unterminated JSON front matter"));
            inputs.add(Arguments.of("{\"params\": {\"key\": 1}\n", overwrite, "Unterminated JSON front matter"));
            inputs.add(Arguments.of("{\"params\": [1, 2}\n", overwrite, "Mismatched delimiter"));
            inputs.add(Arguments.of("{\"title\": \"Example }\n}\n", overwrite, "Unterminated JSON string"));
            inputs.add(Arguments.of("{\"title\": \"Example\\\n}\n", overwrite, "Unterminated JSON string"));
            inputs.add(Arguments.of("{} trailing content\n", overwrite, "Unexpected content after JSON front matter"));
        }
        return inputs.stream();
    }

    @ParameterizedTest
    @MethodSource("invalidFrontMatter")
    void rejectIncompleteOrAmbiguousFrontMatter(final String input, final boolean overwrite, final String message) throws IOException {
        File file = directory.resolve("example.md").toFile();
        Files.writeString(file.toPath(), input, StandardCharsets.UTF_8);
        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender();
        appender.setOverwrite(overwrite);

        assertThatThrownBy(() -> appender.append(file))
                .isInstanceOf(IOException.class)
                .hasMessageContaining(message)
                .hasMessageContaining(file.toString());

        assertThat(Files.readString(file.toPath(), StandardCharsets.UTF_8)).isEqualTo(input);
        assertThat(Path.of(file + ".new")).doesNotExist();
    }

    @ParameterizedTest
    @ValueSource(strings = {"# Heading\n+++\ntitle = \"Example\"\n+++\n", "# Heading\n{\"title\": \"Example\"}\n",
            "\n+++\n+++\n", "\n{}\n", "[\"not an object\"]\n", "```json\n{\"title\": \"Example\"}\n```\n"})
    void doNotTreatBodyContentAsFrontMatter(final String input) throws IOException {
        File file = directory.resolve("example.md").toFile();
        Files.writeString(file.toPath(), input, StandardCharsets.UTF_8);
        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender();

        appender.append(file);

        assertThat(Files.readString(Path.of(file + ".new")))
                .isEqualTo(appender.getLicenseHeader(file) + LINE_SEPARATOR + input.replace("\n", LINE_SEPARATOR));
    }

    @ParameterizedTest
    @ValueSource(strings = {"TOML", "JSON"})
    void insertViaReportWithTikaMetadata(final String format) throws Exception {
        File file = directory.resolve("example.md").toFile();
        String frontMatter = "TOML".equals(format) ? TOML : JSON;
        Files.writeString(file.toPath(), frontMatter + BODY, StandardCharsets.UTF_8);
        FileDocument document = new FileDocument(file, DocumentNameMatcher.MATCHES_ALL);
        TikaProcessor.process(document);
        assertThat(document.getMetaData().getDocumentType()).isEqualTo(Document.Type.STANDARD);
        document.getMetaData().reportOnLicense(UnknownLicense.INSTANCE);

        new LicenseAddingReport(null, false).report(document);

        String header = new ApacheV2LicenseAppender().getLicenseHeader(file);
        assertThat(Files.readString(Path.of(file + ".new")))
                .isEqualTo(frontMatter.replace("\n", LINE_SEPARATOR) + LINE_SEPARATOR + header
                        + LINE_SEPARATOR + BODY.replace("\n", LINE_SEPARATOR));
    }

    @ParameterizedTest
    @CsvSource({"TOML,false", "TOML,true", "JSON,false", "JSON,true"})
    void insertViaReportUsingMarkdownMetadata(final String format, final boolean overwrite) throws Exception {
        File file = directory.resolve("README").toFile();
        String frontMatter = "TOML".equals(format) ? TOML : JSON;
        Files.writeString(file.toPath(), frontMatter + BODY, StandardCharsets.UTF_8);
        FileDocument document = new FileDocument(file, DocumentNameMatcher.MATCHES_ALL);
        document.getMetaData().setMediaType(MediaType.parse("text/markdown"));
        document.getMetaData().reportOnLicense(UnknownLicense.INSTANCE);

        new LicenseAddingReport(null, overwrite).report(document);

        Path output = overwrite ? file.toPath() : Path.of(file + ".new");
        String header = new ApacheV2LicenseAppender().getLicenseHeader(directory.resolve("reference.md").toFile());
        assertThat(Files.readString(output))
                .isEqualTo(frontMatter.replace("\n", LINE_SEPARATOR) + LINE_SEPARATOR + header
                        + LINE_SEPARATOR + BODY.replace("\n", LINE_SEPARATOR));
    }
}
