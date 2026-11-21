/* SPDX-License-Identifier: Apache-2.0 */
package org.apache.rat.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

/** Inserts the header after a package declaration or processing instruction. */
final class DeclarationLicenseEditor implements LicenseEditor {
    /** The declaration marker after which the license is inserted. */
    private final String prefix;

    DeclarationLicenseEditor(final String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean edit(final File document, final BufferedReader reader, final Writer writer, final String header) throws IOException {
        boolean written = false;
        String line;
        while ((line = reader.readLine()) != null) {
            PlainLicenseEditor.writeLine(writer, line);
            if (line.startsWith(prefix)) {
                writer.write(System.lineSeparator());
                writer.write(header);
                writer.write(System.lineSeparator());
                written = true;
            }
        }
        return written;
    }
}
