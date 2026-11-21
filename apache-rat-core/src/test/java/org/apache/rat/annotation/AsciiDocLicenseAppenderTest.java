/* SPDX-License-Identifier: Apache-2.0 */
package org.apache.rat.annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.rat.Defaults;
import org.apache.rat.analysis.HeaderCheckWorker;
import org.apache.rat.analysis.TikaProcessor;
import org.apache.rat.analysis.UnknownLicense;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.document.FileDocument;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.report.claim.LicenseAddingReport;
import org.apache.rat.test.utils.Resources;
import org.apache.tika.mime.MediaType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class AsciiDocLicenseAppenderTest {
    @TempDir
    private Path directory;

    private String ticketExample() throws Exception {
        return Files.readString(Resources.getResourceFile("annotation/RAT-517.adoc").toPath(), StandardCharsets.UTF_8);
    }

    private String normalize(final String content) {
        return content.replace("\n", System.lineSeparator());
    }

    @ParameterizedTest
    @CsvSource({"adoc,false", "adoc,true", "asciidoc,false", "asciidoc,true", "asc,false", "asc,true"})
    void preserveTicketExample(final String extension, final boolean overwrite) throws Exception {
        File file = directory.resolve("example." + extension).toFile();
        String example = ticketExample();
        // Also exercise BOM removal and CRLF handling with the ticket metadata.
        String input = "\uFEFF" + example.replace("\n", "\r\n");
        Files.writeString(file.toPath(), input, StandardCharsets.UTF_8);
        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender();
        appender.setOverwrite(overwrite);

        appender.append(file);

        Path output = overwrite ? file.toPath() : Path.of(file + ".new");
        int endOfHeader = example.indexOf("\n\n") + 1;
        String header = appender.getLicenseHeader(file);
        String expected = normalize(example.substring(0, endOfHeader)) + header + System.lineSeparator()
                + normalize(example.substring(endOfHeader));
        assertEquals(expected, Files.readString(output, StandardCharsets.UTF_8));
        assertTrue(header.startsWith("////" + System.lineSeparator() + "Licensed to the Apache Software Foundation"));
        assertTrue(header.endsWith("////" + System.lineSeparator()));
        assertFalse(header.contains("<!--"));
        assertFalse(header.contains(" * "));
        if (overwrite) {
            assertFalse(Path.of(file + ".new").toFile().exists());
        } else {
            assertEquals(input, Files.readString(file.toPath(), StandardCharsets.UTF_8));
        }
        ILicense license = LicenseSetFactory.search("AL", "AL2.0",
                Defaults.builder().build().getLicenseSetFactory().getLicenses(LicenseFilter.ALL)).orElseThrow();
        try (BufferedReader reader = Files.newBufferedReader(output, StandardCharsets.UTF_8)) {
            assertTrue(license.matches(HeaderCheckWorker.readHeader(reader,
                    HeaderCheckWorker.DEFAULT_NUMBER_OF_RETAINED_HEADER_LINES)));
        }
    }

    static Stream<Arguments> documentHeaders() {
        return Stream.of(
                Arguments.of("", ""),
                Arguments.of("", "A paragraph without a header\n\n:toc: left\n"),
                Arguments.of("", "== A section, not a document title\n\nText\n"),
                Arguments.of("", "\n= Not an initial title\n"),
                Arguments.of("= Title\n", "\nText\n"),
                Arguments.of("= Title\n", "== Section without a separating blank line\n"),
                Arguments.of("= Title\n", ""),
                Arguments.of(":toc: left\n:sectnums:\n:icons: font\n", "\nText\n"),
                Arguments.of(":toc:\n", "Text without a separating blank line\n"),
                Arguments.of(":toc:\n", ""),
                Arguments.of(":toc!:\n:!sectnums:\n", "\nText\n"),
                Arguments.of(":description: First line \\\n  second line\n:toc:\n", "\nText\n"),
                Arguments.of("= Title\nMax Mustermann <max@example.com>\nv1.0, 2026-11-16\n:toc:\n", "\nText\n"),
                Arguments.of("= Title\n:toc:\n", "\nText\n\n:icons: font\n"));
    }

    @ParameterizedTest
    @MethodSource("documentHeaders")
    void insertOnlyAfterInitialHeader(final String preamble, final String body) throws Exception {
        File file = directory.resolve("example.adoc").toFile();
        String input = preamble + body;
        // A header at EOF does not require a final newline in the input.
        if (body.isEmpty() && !preamble.isEmpty()) {
            input = input.substring(0, input.length() - 1);
        }
        Files.writeString(file.toPath(), input, StandardCharsets.UTF_8);
        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender();

        appender.append(file);

        assertEquals(normalize(preamble) + appender.getLicenseHeader(file) + System.lineSeparator() + normalize(body),
                Files.readString(Path.of(file + ".new"), StandardCharsets.UTF_8));
        assertEquals(input, Files.readString(file.toPath(), StandardCharsets.UTF_8));
    }

    @ParameterizedTest
    @ValueSource(strings = {"text/asciidoc", "text/x-asciidoc", "text/asciidoc; charset=UTF-8"})
    void useDetectedMetadataWithoutExtension(final String mediaType) throws Exception {
        File file = directory.resolve("README").toFile();
        String example = ticketExample();
        Files.writeString(file.toPath(), example, StandardCharsets.UTF_8);
        MetaData metadata = new MetaData();
        metadata.setMediaType(MediaType.parse(mediaType));
        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender();

        appender.append(file, metadata);

        int endOfHeader = example.indexOf("\n\n") + 1;
        String header = appender.getLicenseHeader(directory.resolve("reference.adoc").toFile());
        assertEquals(normalize(example.substring(0, endOfHeader)) + header + System.lineSeparator()
                + normalize(example.substring(endOfHeader)), Files.readString(Path.of(file + ".new")));
    }

    @Test
    void insertCopyrightAsBlockComment() throws Exception {
        File file = directory.resolve("example.adoc").toFile();
        Files.writeString(file.toPath(), "= Title\n\nText\n");
        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender("Copyright 2026 Max Mustermann");

        appender.append(file);

        String header = appender.getLicenseHeader(file);
        assertTrue(header.startsWith(normalize("////\nCopyright 2026 Max Mustermann\n\n")));
        assertTrue(header.endsWith(normalize("////\n")));
        assertEquals(normalize("= Title\n") + header + System.lineSeparator() + normalize("\nText\n"),
                Files.readString(Path.of(file + ".new")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"adoc", "asciidoc", "asc"})
    void insertViaReportWithTikaMetadata(final String extension) throws Exception {
        File file = directory.resolve("example." + extension).toFile();
        String example = ticketExample();
        Files.writeString(file.toPath(), example, StandardCharsets.UTF_8);
        FileDocument document = new FileDocument(file, DocumentNameMatcher.MATCHES_ALL);
        TikaProcessor.process(document);
        assertEquals(Document.Type.STANDARD, document.getMetaData().getDocumentType());
        document.getMetaData().reportOnLicense(UnknownLicense.INSTANCE);

        new LicenseAddingReport(null, false).report(document);

        int endOfHeader = example.indexOf("\n\n") + 1;
        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender();
        assertEquals(normalize(example.substring(0, endOfHeader)) + appender.getLicenseHeader(file)
                + System.lineSeparator() + normalize(example.substring(endOfHeader)),
                Files.readString(Path.of(file + ".new")));
    }
}
