/* SPDX-License-Identifier: Apache-2.0 */
package org.apache.rat.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

/** Places a formatted license header in a particular class of documents. */
interface LicenseEditor {
    /**
     * Copies the document, inserting the header at the appropriate location.
     * The caller owns the reader and writer and handles replacement and cleanup.
     * @param document the source file, used in diagnostics.
     * @param reader the source content, with any BOM already removed.
     * @param writer the destination.
     * @param header the formatted license header.
     * @return whether a header was inserted.
     * @throws IOException if reading, writing or validating the document fails.
     */
    boolean edit(File document, BufferedReader reader, Writer writer, String header) throws IOException;
}
