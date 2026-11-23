/* SPDX-License-Identifier: Apache-2.0 */
package org.apache.rat.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.regex.Pattern;

/** Preserves Hugo front matter and the native AsciiDoc header before inserting a block comment. */
final class AsciiDocLicenseEditor implements LicenseEditor {
    /** Attribute entries, including empty values and attribute unsetting. */
    private static final Pattern ATTRIBUTE = Pattern.compile(":!?[\\w-]+!?:.*");

    @Override
    public boolean edit(final File document, final BufferedReader reader, final Writer writer, final String header) throws IOException {
        String line = FrontMatterReader.copy(document, reader, writer);
        boolean hasTitle = line != null && line.startsWith("= ");
        if (hasTitle) {
            PlainLicenseEditor.writeLine(writer, line);
            line = reader.readLine();
        }
        boolean continued = false;
        // A titled header may also contain author and revision lines. It ends at
        // the first blank line; never search the body for further attributes.
        while (line != null && !line.trim().isEmpty() && !line.startsWith("== ")
                && (hasTitle || continued || ATTRIBUTE.matcher(line).matches())) {
            PlainLicenseEditor.writeLine(writer, line);
            continued = line.endsWith(" \\");
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
}
