/* SPDX-License-Identifier: Apache-2.0 */
package org.apache.rat.annotation;

/** Comment syntax used to format a license, independently of its placement. */
enum CommentStyle {
    /** No supported comment syntax. */
    NONE("", "", ""),
    /** C-family block comments. */
    C("/*", " */", " * "),
    /** XML and HTML comments. */
    SGML("<!--", "-->", ""),
    /** AsciiDoc block comments. */
    ASCIIDOC("////", "////", ""),
    /** Almost Plain Text comments. */
    APT("", "", "~~ "),
    /** Shell-style line comments. */
    SHELL("", "", "# "),
    /** Batch line comments. */
    BATCH("", "", "rem "),
    /** Velocity line comments. */
    VELOCITY("", "", "## ");

    /** Native output line separator. */
    private static final String LINE_SEPARATOR = System.lineSeparator();
    /** Opening block delimiter, or empty for line comments. */
    private final String opening;
    /** Closing block delimiter, or empty for line comments. */
    private final String closing;
    /** Prefix for each license line. */
    private final String prefix;

    CommentStyle(final String opening, final String closing, final String prefix) {
        this.opening = opening;
        this.closing = closing;
        this.prefix = prefix;
    }

    String firstLine() {
        return opening.isEmpty() ? "" : opening + LINE_SEPARATOR;
    }

    String lastLine() {
        return closing.isEmpty() ? "" : closing + LINE_SEPARATOR;
    }

    String line(final String content) {
        return this == NONE ? "" : prefix + content + LINE_SEPARATOR;
    }
}
