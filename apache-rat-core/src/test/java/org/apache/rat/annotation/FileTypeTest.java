/* SPDX-License-Identifier: Apache-2.0 */
package org.apache.rat.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.apache.rat.api.MetaData;
import org.apache.tika.mime.MediaType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class FileTypeTest {
    @ParameterizedTest
    @CsvSource(textBlock = """
            UNKNOWN, 0, NONE, FRONT, unrecognized
            JAVA, 1, C, PACKAGE, java
            XML, 2, SGML, XML, config|csdproj|csproj|fxcop|ndoc|nunit|rdf|resx|shfbproj|stylecop|svg|vbdproj|vbproj|vcproj|vsdisco|webinfo|xml|xproj|xsl
            HTML, 3, SGML, FRONT, asax|ascx|aspx|htm|html
            CSS, 4, C, FRONT, css
            JAVASCRIPT, 5, C, FRONT, js
            APT, 6, APT, FRONT, apt
            PROPERTIES, 7, SHELL, FRONT, properties
            PYTHON, 8, SHELL, SHEBANG, py
            C, 9, C, FRONT, c
            H, 10, C, FRONT, h|hh|hpp
            SH, 11, SHELL, SHEBANG, sh
            BAT, 12, BATCH, BATCH, bat|cmd
            VM, 13, VELOCITY, FRONT, vm
            SCALA, 14, C, FRONT, scala
            RUBY, 15, SHELL, SHEBANG, rb
            PERL, 16, SHELL, SHEBANG, pl
            TCL, 17, SHELL, SHEBANG, tcl
            CPP, 18, C, FRONT, cc|cpp
            CSHARP, 19, C, FRONT, cs
            PHP, 20, C, PHP, php
            GROOVY, 21, C, FRONT, groovy
            VISUAL_STUDIO_SOLUTION, 22, SHELL, VISUAL_STUDIO, sln
            BEANSHELL, 23, C, FRONT, bsh
            JSP, 24, SGML, FRONT, jsp
            FML, 25, SGML, FRONT, fml
            GO, 26, C, PACKAGE, go
            PM, 27, SHELL, PACKAGE, pm
            MD, 28, SGML, MARKDOWN, md
            YAML, 29, SHELL, FRONT, yaml|yml
            ASCIIDOC, 30, ASCIIDOC, ASCIIDOC, adoc|asc|asciidoc
            """)
    void retainLegacyMappings(final FileType type, final int id, final CommentStyle style,
                              final HeaderPlacement placement, final String extensions) {
        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender();
        assertThat(type.legacyId()).isEqualTo(id);
        assertThat(FileType.fromLegacyId(id)).isSameAs(type);
        assertThat(type.commentStyle()).isSameAs(style);
        assertThat(type.placement()).isSameAs(placement);
        for (String extension : extensions.split("\\|")) {
            assertThat(FileType.fromExtension(extension)).as("Extension %s", extension).isSameAs(type);
            assertThat(appender.getType(new File("example." + extension))).isEqualTo(id);
        }
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            text/asciidoc, ASCIIDOC
            text/x-asciidoc, ASCIIDOC
            text/markdown, MD
            text/x-web-markdown, MD
            text/markdown; charset=UTF-8, MD
            application/xml, XML
            text/xml, XML
            text/html, HTML
            text/plain, UNKNOWN
            application/json, UNKNOWN
            application/octet-stream, UNKNOWN
            """)
    void retainMediaTypeMappings(final String mediaType, final FileType type) {
        MetaData metadata = new MetaData();
        metadata.setMediaType(MediaType.parse(mediaType));
        assertThat(FileType.fromMediaType(metadata.getMediaType())).isSameAs(type);
        assertThat(new ApacheV2LicenseAppender().getType(new File("README"), metadata)).isEqualTo(type.legacyId());
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            NONE, '', '', ''
            C, /*, ' */', ' * '
            SGML, <!--, -->, ''
            ASCIIDOC, ////, ////, ''
            APT, '', '', '~~ '
            SHELL, '', '', '# '
            BATCH, '', '', 'rem '
            VELOCITY, '', '', '## '
            """)
    void retainCommentFormatting(final CommentStyle style, final String opening, final String closing, final String prefix) {
        String separator = System.lineSeparator();
        assertThat(style.firstLine()).isEqualTo(opening.isEmpty() ? "" : opening + separator);
        assertThat(style.lastLine()).isEqualTo(closing.isEmpty() ? "" : closing + separator);
        assertThat(style.line("License")).isEqualTo(style == CommentStyle.NONE ? "" : prefix + "License" + separator);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 31, 1000})
    void unknownIdsRetainEmptyFormatting(final int id) {
        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender();
        assertThat(FileType.fromLegacyId(id)).isSameAs(FileType.UNKNOWN);
        assertThat(appender.getFirstLine(id)).isEmpty();
        assertThat(appender.getLastLine(id)).isEmpty();
        assertThat(appender.getLine(id, "License")).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"README", "example.", "example.JAVA", "example.unknown"})
    void retainUnknownFilenameBehavior(final String filename) {
        assertThat(new ApacheV2LicenseAppender().getType(new File(filename))).isZero();
    }

    @Test
    void extensionTakesPrecedenceAndMissingMetadataIsSafe() {
        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender();
        File file = new File("example.md");
        MetaData metadata = new MetaData();
        metadata.setMediaType(MediaType.parse("text/html"));
        assertThat(appender.getType(file, metadata)).isEqualTo(28);
        assertThat(appender.getType(file, null)).isEqualTo(28);
        assertThat(appender.getType(new File("README"), new MetaData())).isZero();
        assertThat(FileType.fromMediaType(null)).isSameAs(FileType.UNKNOWN);
    }
}
