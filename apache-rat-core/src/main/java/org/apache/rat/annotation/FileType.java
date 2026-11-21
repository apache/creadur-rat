/* SPDX-License-Identifier: Apache-2.0 */
package org.apache.rat.annotation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tika.mime.MediaType;

/** Internal file classification with explicit, stable IDs for the legacy protected API. */
enum FileType {
    /** Unsupported or unrecognized type. */
    UNKNOWN(0, CommentStyle.NONE, HeaderPlacement.FRONT),
    /** Java source. */
    JAVA(1, CommentStyle.C, HeaderPlacement.PACKAGE, "java"),
    /** XML and related project/configuration files. */
    XML(2, CommentStyle.SGML, HeaderPlacement.XML, "config", "csdproj", "csproj", "fxcop", "ndoc", "nunit",
            "rdf", "resx", "shfbproj", "stylecop", "svg", "vbdproj", "vbproj", "vcproj", "vsdisco", "webinfo", "xml", "xproj", "xsl"),
    /** HTML and ASP files. */
    HTML(3, CommentStyle.SGML, HeaderPlacement.FRONT, "asax", "ascx", "aspx", "htm", "html"),
    /** Cascading Style Sheets. */
    CSS(4, CommentStyle.C, HeaderPlacement.FRONT, "css"),
    /** JavaScript source. */
    JAVASCRIPT(5, CommentStyle.C, HeaderPlacement.FRONT, "js"),
    /** Almost Plain Text documents. */
    APT(6, CommentStyle.APT, HeaderPlacement.FRONT, "apt"),
    /** Properties files. */
    PROPERTIES(7, CommentStyle.SHELL, HeaderPlacement.FRONT, "properties"),
    /** Python scripts. */
    PYTHON(8, CommentStyle.SHELL, HeaderPlacement.SHEBANG, "py"),
    /** C source. */
    C(9, CommentStyle.C, HeaderPlacement.FRONT, "c"),
    /** C and C++ headers. */
    H(10, CommentStyle.C, HeaderPlacement.FRONT, "h", "hh", "hpp"),
    /** Shell scripts. */
    SH(11, CommentStyle.SHELL, HeaderPlacement.SHEBANG, "sh"),
    /** Batch scripts. */
    BAT(12, CommentStyle.BATCH, HeaderPlacement.BATCH, "bat", "cmd"),
    /** Velocity templates. */
    VM(13, CommentStyle.VELOCITY, HeaderPlacement.FRONT, "vm"),
    /** Scala source. */
    SCALA(14, CommentStyle.C, HeaderPlacement.FRONT, "scala"),
    /** Ruby scripts. */
    RUBY(15, CommentStyle.SHELL, HeaderPlacement.SHEBANG, "rb"),
    /** Perl scripts. */
    PERL(16, CommentStyle.SHELL, HeaderPlacement.SHEBANG, "pl"),
    /** Tcl scripts. */
    TCL(17, CommentStyle.SHELL, HeaderPlacement.SHEBANG, "tcl"),
    /** C++ source. */
    CPP(18, CommentStyle.C, HeaderPlacement.FRONT, "cc", "cpp"),
    /** C# source. */
    CSHARP(19, CommentStyle.C, HeaderPlacement.FRONT, "cs"),
    /** PHP source. */
    PHP(20, CommentStyle.C, HeaderPlacement.PHP, "php"),
    /** Groovy source. */
    GROOVY(21, CommentStyle.C, HeaderPlacement.FRONT, "groovy"),
    /** Visual Studio solution files. */
    VISUAL_STUDIO_SOLUTION(22, CommentStyle.SHELL, HeaderPlacement.VISUAL_STUDIO, "sln"),
    /** BeanShell scripts. */
    BEANSHELL(23, CommentStyle.C, HeaderPlacement.FRONT, "bsh"),
    /** JavaServer Pages. */
    JSP(24, CommentStyle.SGML, HeaderPlacement.FRONT, "jsp"),
    /** FML documents. */
    FML(25, CommentStyle.SGML, HeaderPlacement.FRONT, "fml"),
    /** Go source. */
    GO(26, CommentStyle.C, HeaderPlacement.PACKAGE, "go"),
    /** Perl modules with package declarations. */
    PM(27, CommentStyle.SHELL, HeaderPlacement.PACKAGE, "pm"),
    /** Markdown documents. */
    MD(28, CommentStyle.SGML, HeaderPlacement.MARKDOWN, "md"),
    /** YAML documents. */
    YAML(29, CommentStyle.SHELL, HeaderPlacement.FRONT, "yaml", "yml"),
    /** AsciiDoc documents. */
    ASCIIDOC(30, CommentStyle.ASCIIDOC, HeaderPlacement.ASCIIDOC, "adoc", "asc", "asciidoc");

    /** Lookup by legacy ID; enum ordinals are never used as IDs. */
    private static final Map<Integer, FileType> BY_ID;
    /** Explicit, case-sensitive extension lookup. */
    private static final Map<String, FileType> BY_EXTENSION;
    /** Stable ID returned by the protected integer-based methods. */
    private final int legacyId;
    /** Comment syntax for this file type. */
    private final CommentStyle commentStyle;
    /** Header placement for this file type. */
    private final HeaderPlacement placement;
    /** Recognized extensions for this file type. */
    private final List<String> extensions;

    static {
        Map<Integer, FileType> ids = new HashMap<>();
        Map<String, FileType> extensions = new HashMap<>();
        for (FileType type : values()) {
            if (ids.put(type.legacyId, type) != null) {
                throw new IllegalStateException("Duplicate license file type ID: " + type.legacyId);
            }
            for (String extension : type.extensions) {
                if (extensions.put(extension, type) != null) {
                    throw new IllegalStateException("Duplicate license file extension: " + extension);
                }
            }
        }
        BY_ID = Map.copyOf(ids);
        BY_EXTENSION = Map.copyOf(extensions);
    }

    FileType(final int legacyId, final CommentStyle commentStyle, final HeaderPlacement placement, final String... extensions) {
        this.legacyId = legacyId;
        this.commentStyle = commentStyle;
        this.placement = placement;
        this.extensions = List.of(extensions);
    }

    int legacyId() {
        return legacyId;
    }

    CommentStyle commentStyle() {
        return commentStyle;
    }

    HeaderPlacement placement() {
        return placement;
    }

    static FileType fromLegacyId(final int legacyId) {
        return BY_ID.getOrDefault(legacyId, UNKNOWN);
    }

    static FileType fromExtension(final String extension) {
        return BY_EXTENSION.getOrDefault(extension, UNKNOWN);
    }

    static FileType fromMediaType(final MediaType mediaType) {
        if (mediaType == null) {
            return UNKNOWN;
        }
        return switch (mediaType.getBaseType().toString()) {
        case "text/asciidoc", "text/x-asciidoc" -> ASCIIDOC;
        case "text/markdown", "text/x-web-markdown" -> MD;
        case "application/xml", "text/xml" -> XML;
        case "text/html" -> HTML;
        default -> UNKNOWN;
        };
    }
}
