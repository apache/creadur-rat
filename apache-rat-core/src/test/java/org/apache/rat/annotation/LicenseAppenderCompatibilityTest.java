/* SPDX-License-Identifier: Apache-2.0 */
package org.apache.rat.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.rat.api.MetaData;
import org.apache.tika.mime.MediaType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LicenseAppenderCompatibilityTest {
    @TempDir
    private Path directory;

    @Test
    void overriddenLegacyTypeStillControlsPlacementAndFormatting() throws IOException {
        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender() {
            @Override
            protected int getType(final File document) {
                return 1; // Stable Java ID, not an enum ordinal.
            }
        };
        File file = directory.resolve("example.custom").toFile();
        Files.writeString(file.toPath(), "package demo;\nclass Example {}\n");
        MetaData metadata = new MetaData();
        metadata.setMediaType(MediaType.parse("text/html"));

        appender.append(file, metadata);

        String separator = System.lineSeparator();
        assertThat(appender.getType(file, metadata)).isEqualTo(1);
        assertThat(appender.getLicenseHeader(file)).startsWith("/*" + separator);
        assertThat(Files.readString(Path.of(file + ".new")))
                .isEqualTo("package demo;" + separator + separator + appender.getLicenseHeader(file)
                        + separator + "class Example {}" + separator);
    }

    @Test
    void overriddenMetadataTypeStillControlsPlacement() throws IOException {
        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender() {
            @Override
            protected int getType(final File document, final MetaData metadata) {
                return 28; // Stable Markdown ID.
            }
        };
        File file = directory.resolve("README").toFile();
        Files.writeString(file.toPath(), "---\ntitle: Example\n---\n# Heading\n");

        appender.append(file);

        String separator = System.lineSeparator();
        String header = appender.getLicenseHeader(file, null);
        assertThat(header).startsWith("<!--" + separator);
        assertThat(Files.readString(Path.of(file + ".new")))
                .isEqualTo("---" + separator + "title: Example" + separator + "---" + separator + separator
                        + header + separator + "# Heading" + separator);
    }

    @Test
    void overriddenFormattingHooksRemainInUse() throws IOException {
        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender() {
            @Override
            protected String getFirstLine(final int type) {
                return "BEGIN " + type + System.lineSeparator();
            }

            @Override
            protected String getLastLine(final int type) {
                return "END " + type + System.lineSeparator();
            }

            @Override
            protected String getLine(final int type, final String content) {
                return type + ": " + content + System.lineSeparator();
            }
        };
        File file = directory.resolve("example.py").toFile();
        Files.writeString(file.toPath(), "#!/usr/bin/python\nprint('hello')\n");

        appender.append(file);

        String separator = System.lineSeparator();
        String header = appender.getLicenseHeader(file);
        assertThat(header)
                .startsWith("BEGIN 8" + separator + "8: Licensed to the Apache Software Foundation")
                .endsWith("END 8" + separator);
        assertThat(Files.readString(Path.of(file + ".new")))
                .isEqualTo("#!/usr/bin/python" + separator + header + "print('hello')" + separator);
    }

    @Test
    void publicHeaderOverrideRemainsInUse() throws IOException {
        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender() {
            @Override
            public String getLicenseHeader(final File document) {
                return "Custom license" + System.lineSeparator();
            }
        };
        File file = directory.resolve("example.md").toFile();
        Files.writeString(file.toPath(), "# Heading\n");
        MetaData metadata = new MetaData();
        metadata.setMediaType(MediaType.parse("text/plain"));

        appender.append(file, metadata);

        assertThat(Files.readString(Path.of(file + ".new")))
                .isEqualTo(appender.getLicenseHeader(file) + System.lineSeparator() + "# Heading" + System.lineSeparator());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 1000})
    void customNonzeroTypeRetainsPlainInsertion(final int id) throws IOException {
        AbstractLicenseAppender appender = new AbstractLicenseAppender() {
            @Override
            protected int getType(final File document) {
                return id;
            }

            @Override
            public String getLicenseHeader(final File document) {
                return "Custom license" + System.lineSeparator();
            }
        };
        File file = directory.resolve("example.custom").toFile();
        Files.writeString(file.toPath(), "Body\n");

        appender.append(file);

        assertThat(Files.readString(Path.of(file + ".new")))
                .isEqualTo(appender.getLicenseHeader(file) + System.lineSeparator() + "Body" + System.lineSeparator());
    }

    @ParameterizedTest
    @ValueSource(strings = {"java", "go", "pm", "xml"})
    void missingDeclarationRetainsFallback(final String extension) throws IOException {
        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender();
        File file = directory.resolve("example." + extension).toFile();
        Files.writeString(file.toPath(), "Body\n");

        appender.append(file);

        String separator = System.lineSeparator();
        String prefix = "xml".equals(extension) ? "<?xml version='1.0'?>" + separator : "";
        assertThat(Files.readString(Path.of(file + ".new")))
                .isEqualTo(prefix + appender.getLicenseHeader(file) + separator + "Body" + separator);
    }

    @Test
    void missingPhpInstructionStillDoesNotInsertHeader() throws IOException {
        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender();
        File file = directory.resolve("example.php").toFile();
        Files.writeString(file.toPath(), "Body\n");

        appender.append(file);

        assertThat(Files.readString(Path.of(file + ".new"))).isEqualTo("Body" + System.lineSeparator());
    }
}
