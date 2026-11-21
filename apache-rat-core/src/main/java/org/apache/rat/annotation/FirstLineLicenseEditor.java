/* SPDX-License-Identifier: Apache-2.0 */
package org.apache.rat.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

/** Preserves a required first line, such as a shebang or batch echo command. */
final class FirstLineLicenseEditor implements LicenseEditor {
    /** The first-line marker that must precede the license. */
    private final String prefix;

    FirstLineLicenseEditor(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean edit(final File document, final BufferedReader reader, final Writer writer, final String header) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return false;
        }
        writeFirstLine(writer, line, header, prefix);
        PlainLicenseEditor.copyLines(reader, writer);
        return true;
    }

    static void writeFirstLine(final Writer writer, final String line, final String header, final String prefix) throws IOException {
        if (line.startsWith(prefix)) {
            PlainLicenseEditor.writeLine(writer, line);
            writer.write(header);
        } else {
            writer.write(header);
            PlainLicenseEditor.writeLine(writer, line);
        }
    }
}
