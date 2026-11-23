/* SPDX-License-Identifier: Apache-2.0 */
package org.apache.rat.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

/** Preserves initial YAML, TOML or JSON metadata shared by Markdown and AsciiDoc. */
final class FrontMatterReader {
    private FrontMatterReader() {
    }

    /** Copies initial front matter, if present, and returns the first unconsumed line. */
    static String copy(final File document, final BufferedReader reader, final Writer writer) throws IOException {
        String line = reader.readLine();
        if ("---".equals(line) || "+++".equals(line)) {
            copyDelimitedFrontMatter(document, reader, writer, line);
            return reader.readLine();
        }
        if (line != null && line.trim().startsWith("{")) {
            new JsonFrontMatterReader().copy(document, reader, writer, line);
            return reader.readLine();
        }
        return line;
    }

    private static void copyDelimitedFrontMatter(final File document, final BufferedReader reader, final Writer writer,
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
