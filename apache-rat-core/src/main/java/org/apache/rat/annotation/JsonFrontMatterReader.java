/* SPDX-License-Identifier: Apache-2.0 */
package org.apache.rat.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Deque;

/** Finds the end of an initial JSON object without parsing its metadata values. */
final class JsonFrontMatterReader {
    /** Closing delimiters expected for nested objects and arrays. */
    private final Deque<Character> closingDelimiters = new ArrayDeque<>();
    /** Whether the current character belongs to a quoted string. */
    private boolean inString;
    /** Whether the next string character is escaped. */
    private boolean escaped;

    /** Copies the object, including its first line, stopping before the document body. */
    void copy(final File document, final BufferedReader reader, final Writer writer, final String firstLine) throws IOException {
        String line = firstLine;
        while (line != null) {
            boolean complete = scanLine(document, line);
            PlainLicenseEditor.writeLine(writer, line);
            if (complete) {
                writer.write(System.lineSeparator());
                return;
            }
            line = reader.readLine();
        }
        throw new IOException("Unterminated JSON front matter in " + document);
    }

    private boolean scanLine(final File document, final String line) throws IOException {
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (inString) {
                scanStringCharacter(character);
            } else if (character == '"') {
                inString = true;
            } else if (character == '{') {
                closingDelimiters.push('}');
            } else if (character == '[') {
                closingDelimiters.push(']');
            } else if ((character == '}' || character == ']')
                    && closeDelimiter(document, character, line, index)) {
                return true;
            }
        }
        // JSON strings cannot span physical lines, even with a trailing backslash.
        if (inString) {
            throw new IOException("Unterminated JSON string in front matter in " + document);
        }
        return false;
    }

    private boolean closeDelimiter(final File document, final char character, final String line, final int index) throws IOException {
        if (closingDelimiters.isEmpty() || closingDelimiters.pop() != character) {
            throw new IOException("Mismatched delimiter in JSON front matter in " + document);
        }
        if (!closingDelimiters.isEmpty()) {
            return false;
        }
        if (!line.substring(index + 1).trim().isEmpty()) {
            throw new IOException("Unexpected content after JSON front matter in " + document);
        }
        return true;
    }

    private void scanStringCharacter(final char character) {
        if (escaped) {
            escaped = false;
        } else if (character == '\\') {
            escaped = true;
        } else if (character == '"') {
            inString = false;
        }
    }
}
