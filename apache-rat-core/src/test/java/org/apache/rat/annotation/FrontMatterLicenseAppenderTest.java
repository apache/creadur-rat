/* SPDX-License-Identifier: Apache-2.0 */
package org.apache.rat.annotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.apache.rat.analysis.UnknownLicense;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.document.FileDocument;
import org.apache.rat.report.claim.LicenseAddingReport;
import org.apache.tika.mime.MediaType;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Exercises the same Hugo front matter with both document comment syntaxes. */
class FrontMatterLicenseAppenderTest {
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final List<String> EXTENSIONS = List.of("md", "adoc", "asciidoc", "asc");
    private static final List<String> FRONT_MATTER = List.of(
            "---\ntitle: Example\ndescription: |\n  ---\n  ...\n---\n",
            "---\ntitle: Example\n...\n",
            "+++\ntitle = \"Example\"\n+++\n",
            "{\"title\": \"Example\", \"params\": {\"tags\": [\"docs\"]}}\n");
    private static final String BODY = "A paragraph\n\n---\n+++\n{\"body\": true}\n:toc:\n";

    @TempDir
    private Path directory;

    static Stream<Arguments> documents() {
        return EXTENSIONS.stream().flatMap(extension -> FRONT_MATTER.stream().flatMap(frontMatter ->
                Stream.of("\n", "\r\n", "\r").flatMap(separator -> Stream.of(false, true).flatMap(overwrite ->
                        Stream.of(false, true).map(bom -> Arguments.of(extension, frontMatter, separator, overwrite, bom))))));
    }

    @ParameterizedTest
    @MethodSource("documents")
    void preserveFrontMatterAndCommentSyntax(final String extension, final String frontMatter, final String separator,
                                             final boolean overwrite, final boolean bom) throws IOException {
        File file = directory.resolve("example." + extension).toFile();
        String input = (bom ? "\uFEFF" : "") + (frontMatter + BODY).replace("\n", separator);
        Files.writeString(file.toPath(), input, StandardCharsets.UTF_8);
        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender();
        appender.setOverwrite(overwrite);

        appender.append(file);

        String header = appender.getLicenseHeader(file);
        if ("md".equals(extension)) {
            assertThat(header).startsWith("<!--" + LINE_SEPARATOR).endsWith("-->" + LINE_SEPARATOR);
        } else {
            assertThat(header).startsWith("////" + LINE_SEPARATOR).endsWith("////" + LINE_SEPARATOR)
                    .doesNotContain("<!--", "-->");
        }
        Path output = overwrite ? file.toPath() : Path.of(file + ".new");
        assertThat(Files.readString(output, StandardCharsets.UTF_8))
                .isEqualTo(normalize(frontMatter) + LINE_SEPARATOR + header + LINE_SEPARATOR + normalize(BODY));
        if (overwrite) {
            assertThat(Path.of(file + ".new")).doesNotExist();
        } else {
            assertThat(Files.readString(file.toPath(), StandardCharsets.UTF_8)).isEqualTo(input);
        }
    }

    static Stream<Arguments> documentsWithoutFrontMatter() {
        return EXTENSIONS.stream().flatMap(extension -> Stream.of("", "\n", "\n---\n", BODY)
                .map(input -> Arguments.of(extension, input)));
    }

    @ParameterizedTest
    @MethodSource("documentsWithoutFrontMatter")
    void insertBeforeContentWithoutInitialFrontMatter(final String extension, final String input) throws IOException {
        File file = directory.resolve("example." + extension).toFile();
        Files.writeString(file.toPath(), input);
        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender();

        appender.append(file);

        assertThat(Files.readString(Path.of(file + ".new")))
                .isEqualTo(appender.getLicenseHeader(file) + LINE_SEPARATOR + normalize(input));
        assertThat(Files.readString(file.toPath())).isEqualTo(input);
    }

    static Stream<Arguments> endOfFileDocuments() {
        return EXTENSIONS.stream().flatMap(extension -> FRONT_MATTER.stream()
                .map(frontMatter -> Arguments.of(extension, frontMatter.stripTrailing())));
    }

    @ParameterizedTest
    @MethodSource("endOfFileDocuments")
    void insertAfterFrontMatterAtEndOfFile(final String extension, final String input) throws IOException {
        File file = directory.resolve("example." + extension).toFile();
        Files.writeString(file.toPath(), input);
        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender();

        appender.append(file);

        assertThat(Files.readString(Path.of(file + ".new")))
                .isEqualTo(normalize(input) + LINE_SEPARATOR + LINE_SEPARATOR
                        + appender.getLicenseHeader(file) + LINE_SEPARATOR);
    }

    static Stream<Arguments> invalidDocuments() {
        return EXTENSIONS.stream().flatMap(extension -> Stream.of(
                "---\ntitle: Example\n  ---\n", "+++\ntitle = \"Example\"\n  +++\n",
                "{\"title\": \"Example\"\n", "{\"tags\": [1, 2}\n", "{} trailing content\n")
                .flatMap(input -> Stream.of(false, true).map(overwrite -> Arguments.of(extension, input, overwrite))));
    }

    @ParameterizedTest
    @MethodSource("invalidDocuments")
    void rejectInvalidFrontMatterWithoutChangingOriginal(final String extension, final String input,
                                                         final boolean overwrite) throws IOException {
        File file = directory.resolve("example." + extension).toFile();
        Files.writeString(file.toPath(), input);
        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender();
        appender.setOverwrite(overwrite);

        assertThatThrownBy(() -> appender.append(file)).isInstanceOf(IOException.class).hasMessageContaining(file.toString());

        assertThat(Files.readString(file.toPath())).isEqualTo(input);
        assertThat(Path.of(file + ".new")).doesNotExist();
    }

    static Stream<Arguments> metadataDocuments() {
        return Stream.of("text/markdown", "text/asciidoc", "text/x-asciidoc")
                .flatMap(mediaType -> FRONT_MATTER.stream().flatMap(frontMatter -> Stream.of(false, true)
                        .map(overwrite -> Arguments.of(mediaType, frontMatter, overwrite))));
    }

    @ParameterizedTest
    @MethodSource("metadataDocuments")
    void insertViaReportWithDetectedSyntax(final String mediaType, final String frontMatter, final boolean overwrite) throws Exception {
        File file = directory.resolve("README").toFile();
        Files.writeString(file.toPath(), frontMatter + BODY);
        FileDocument document = new FileDocument(file, DocumentNameMatcher.MATCHES_ALL);
        document.getMetaData().setMediaType(MediaType.parse(mediaType));
        document.getMetaData().reportOnLicense(UnknownLicense.INSTANCE);

        new LicenseAddingReport(null, overwrite).report(document);

        String extension = "text/markdown".equals(mediaType) ? "md" : "adoc";
        String header = new ApacheV2LicenseAppender().getLicenseHeader(directory.resolve("reference." + extension).toFile());
        Path output = overwrite ? file.toPath() : Path.of(file + ".new");
        assertThat(Files.readString(output))
                .isEqualTo(normalize(frontMatter) + LINE_SEPARATOR + header + LINE_SEPARATOR + normalize(BODY));
    }

    static Stream<Arguments> nativeHeaders() {
        return FRONT_MATTER.stream().flatMap(frontMatter -> Stream.of(
                "= Title\nAuthor\nv1.0\n:toc:\n", ":toc:\n:sectnums:\n")
                .map(preamble -> Arguments.of(frontMatter, preamble)));
    }

    @ParameterizedTest
    @MethodSource("nativeHeaders")
    void preserveAsciiDocHeaderAfterFrontMatter(final String frontMatter, final String preamble) throws IOException {
        File file = directory.resolve("example.adoc").toFile();
        Files.writeString(file.toPath(), frontMatter + preamble + "\n" + BODY);
        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender();

        appender.append(file);

        assertThat(Files.readString(Path.of(file + ".new")))
                .isEqualTo(normalize(frontMatter) + LINE_SEPARATOR + normalize(preamble)
                        + appender.getLicenseHeader(file) + LINE_SEPARATOR + LINE_SEPARATOR + normalize(BODY));
    }

    private static String normalize(final String content) {
        return content.replace("\n", LINE_SEPARATOR);
    }
}
