/* SPDX-License-Identifier: Apache-2.0 */
package org.apache.rat.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

/** Preserves an initial YAML front matter block before inserting the header. */
final class MarkdownLicenseEditor implements LicenseEditor {
    @Override
    public boolean edit(final File document, final BufferedReader reader, final Writer writer, final String header) throws IOException {
        String line = reader.readLine();
        if ("---".equals(line)) {
            copyFrontMatter(document, reader, writer);
            line = reader.readLine();
        }
        writer.write(header);
        writer.write(System.lineSeparator());
        if (line != null) {
            PlainLicenseEditor.writeLine(writer, line);
        }
        PlainLicenseEditor.copyLines(reader, writer);
        return true;
    }

    private void copyFrontMatter(final File document, final BufferedReader reader, final Writer writer) throws IOException {
        PlainLicenseEditor.writeLine(writer, "---");
        String line;
        while ((line = reader.readLine()) != null) {
            PlainLicenseEditor.writeLine(writer, line);
            // Indented delimiters belong to YAML content, not the block boundary.
            if ("---".equals(line) || "...".equals(line)) {
                writer.write(System.lineSeparator());
                return;
            }
        }
        throw new IOException("Unterminated YAML front matter in " + document);
    }
}
