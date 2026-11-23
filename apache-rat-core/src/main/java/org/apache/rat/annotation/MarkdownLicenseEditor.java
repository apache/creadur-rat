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
        String line = FrontMatterReader.copy(document, reader, writer);
        writer.write(header);
        writer.write(System.lineSeparator());
        if (line != null) {
            PlainLicenseEditor.writeLine(writer, line);
        }
        PlainLicenseEditor.copyLines(reader, writer);
        return true;
    }
}
