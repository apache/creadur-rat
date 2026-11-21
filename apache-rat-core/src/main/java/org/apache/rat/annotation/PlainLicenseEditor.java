/* SPDX-License-Identifier: Apache-2.0 */
package org.apache.rat.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

/** Inserts a license before the document content. */
final class PlainLicenseEditor implements LicenseEditor {
    @Override
    public boolean edit(final File document, final BufferedReader reader, final Writer writer, final String header) throws IOException {
        writer.write(header);
        writer.write(System.lineSeparator());
        copyLines(reader, writer);
        return true;
    }

    static void writeLine(final Writer writer, final String line) throws IOException {
        writer.write(line);
        writer.write(System.lineSeparator());
    }

    static void copyLines(final BufferedReader reader, final Writer writer) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            writeLine(writer, line);
        }
    }
}
