/* SPDX-License-Identifier: Apache-2.0 */
package org.apache.rat.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

/** Preserves initial YAML, TOML or JSON front matter before inserting the header. */
final class MarkdownLicenseEditor implements LicenseEditor {
    @Override
    public boolean edit(final File document, final BufferedReader reader, final Writer writer, final String header) throws IOException {
        String line = reader.readLine();
        if ("---".equals(line) || "+++".equals(line)) {
            copyDelimitedFrontMatter(document, reader, writer, line);
            line = reader.readLine();
        } else if (line != null && line.trim().startsWith("{")) {
            new JsonFrontMatterReader().copy(document, reader, writer, line);
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

    private void copyDelimitedFrontMatter(final File document, final BufferedReader reader, final Writer writer,
                                          final String openingDelimiter) throws IOException {
        PlainLicenseEditor.writeLine(writer, openingDelimiter);
        String line;
        while ((line = reader.readLine()) != null) {
            PlainLicenseEditor.writeLine(writer, line);
            // Only unindented delimiters close the block. YAML also allows "...".
            if (openingDelimiter.equals(line) || "---".equals(openingDelimiter) && "...".equals(line)) {
                writer.write(System.lineSeparator());
                return;
            }
        }
        String format = "---".equals(openingDelimiter) ? "YAML" : "TOML";
        throw new IOException("Unterminated " + format + " front matter in " + document);
    }
}
