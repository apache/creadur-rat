/* SPDX-License-Identifier: Apache-2.0 */
package org.apache.rat.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

/** Preserves the leading Visual Studio solution version information. */
final class VisualStudioLicenseEditor implements LicenseEditor {
    @Override
    public boolean edit(final File document, final BufferedReader reader, final Writer writer, final String header) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return false;
        }
        if (line.isEmpty()) {
            line = passThroughReadNext(writer, line, reader);
        }
        if (line.startsWith("Microsoft Visual Studio Solution File")) {
            line = passThroughReadNext(writer, line, reader);
        }
        FirstLineLicenseEditor.writeFirstLine(writer, line, header, "# Visual ");
        PlainLicenseEditor.copyLines(reader, writer);
        return true;
    }

    private String passThroughReadNext(final Writer writer, final String line, final BufferedReader reader) throws IOException {
        PlainLicenseEditor.writeLine(writer, line);
        String next = reader.readLine();
        return next == null ? "" : next;
    }
}
