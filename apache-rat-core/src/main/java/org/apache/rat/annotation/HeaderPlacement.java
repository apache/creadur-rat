/* SPDX-License-Identifier: Apache-2.0 */
package org.apache.rat.annotation;

/** Selects an editor and the fallback for a missing document declaration. */
enum HeaderPlacement {
    /** Before all content. */
    FRONT,
    /** After a shebang when present. */
    SHEBANG,
    /** After an initial batch echo command when present. */
    BATCH,
    /** After Visual Studio solution version information. */
    VISUAL_STUDIO,
    /** After a package declaration, otherwise before all content. */
    PACKAGE,
    /** After an XML declaration, creating one if absent. */
    XML,
    /** After a PHP processing instruction. */
    PHP,
    /** After Markdown front matter when present. */
    MARKDOWN,
    /** After the AsciiDoc document header when present. */
    ASCIIDOC;

    LicenseEditor editor() {
        return switch (this) {
        case FRONT -> new PlainLicenseEditor();
        case SHEBANG -> new FirstLineLicenseEditor("#!");
        case BATCH -> new FirstLineLicenseEditor("@echo");
        case VISUAL_STUDIO -> new VisualStudioLicenseEditor();
        case PACKAGE -> new DeclarationLicenseEditor("package ");
        case XML -> new DeclarationLicenseEditor("<?xml ");
        case PHP -> new DeclarationLicenseEditor("<?php");
        case MARKDOWN -> new MarkdownLicenseEditor();
        case ASCIIDOC -> new AsciiDocLicenseEditor();
        };
    }

    boolean needsFallback() {
        return this == PACKAGE || this == XML;
    }

    String fallbackPrefix() {
        return this == XML ? "<?xml version='1.0'?>" + System.lineSeparator() : "";
    }
}
