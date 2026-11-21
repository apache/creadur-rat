/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */
package org.apache.rat.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.input.BOMInputStream;
import org.apache.rat.api.MetaData;
import org.apache.rat.utils.DefaultLog;

/**
 * Add a license header to a document. This appender does not check for the
 * existence of an existing license header, it is assumed that either a second
 * license header is intentional or that there is no license header present
 * already.
 */
public abstract class AbstractLicenseAppender {
    /** Whether to overwrite existing files. */
    private boolean isOverwrite;

    /**
     * Constructor.
     */
    public AbstractLicenseAppender() {
        super();
    }

    /**
     * Append the default license header to the supplied document.
     *
     * @param document document to append to.
     * @throws IOException if there is a problem while reading or writing the file.
     */
    public void append(final File document) throws IOException {
        append(document, null);
    }

    /**
     * Appends a header using already detected content metadata when available.
     * @param document the file to edit.
     * @param metaData metadata from document analysis, or {@code null}.
     * @throws IOException if reading or writing fails.
     */
    public void append(final File document, final MetaData metaData) throws IOException {
        // Resolve through the protected hook so subclasses retain control of typing.
        int typeId = getType(document, metaData);
        if (typeId == FileType.UNKNOWN.legacyId()) {
            return;
        }
        // Custom nonzero IDs retain the legacy plain-placement behavior.
        HeaderPlacement placement = FileType.fromLegacyId(typeId).placement();
        LicenseEditor editor = placement.editor();
        String header = getLicenseHeader(document, metaData);

        File newDocument = new File(document.getAbsolutePath() + ".new");
        try {
            boolean written;
            try (FileWriter writer = new FileWriter(newDocument, StandardCharsets.UTF_8)) {
                written = attachLicense(writer, document, editor, header);
            }
            if (!written && placement.needsFallback()) {
                try (FileWriter writer2 = new FileWriter(newDocument, StandardCharsets.UTF_8)) {
                    writer2.write(placement.fallbackPrefix());
                    attachLicense(writer2, document, HeaderPlacement.FRONT.editor(), header);
                }
            }
        } catch (IOException e) {
            try {
                Files.deleteIfExists(newDocument.toPath());
            } catch (IOException cleanupException) {
                e.addSuppressed(cleanupException);
            }
            throw e;
        }

        if (isOverwrite) {
            replaceOriginalDocument(document, newDocument);
        }
    }

    /**
     * Replace the original document while preserving its executable flag.
     */
    private void replaceOriginalDocument(final File document, final File newDocument) {
        try {
            Path docPath = document.toPath();
            boolean isExecutable = Files.isExecutable(docPath);
            Files.move(newDocument.toPath(), docPath, StandardCopyOption.REPLACE_EXISTING);
            if (isExecutable && !document.setExecutable(true)) {
                DefaultLog.getInstance().warn(String.format("Could not set %s as executable.", document));
            }
        } catch (InvalidPathException | IOException e) {
            DefaultLog.getInstance().error(String.format("Failed to rename new file to %s, original file is unchanged.", document), e);
        }
    }

    private boolean attachLicense(final Writer writer, final File document, final LicenseEditor editor,
                                  final String header) throws IOException {
        try (FileInputStream stream = new FileInputStream(document);
             BOMInputStream bom = BOMInputStream.builder().setInputStream(stream).get();
             BufferedReader reader = new BufferedReader(new InputStreamReader(bom, StandardCharsets.UTF_8))) {
            return editor.edit(document, reader, writer, header);
        }
    }

    /**
     * Resolves supported types using extensions first, then specific detected MIME types.
     * Generic text/plain cannot determine the required comment syntax.
     * @param document the source file.
     * @param metaData detected metadata, or {@code null}.
     * @return the stable legacy type ID, or zero for an unknown type.
     */
    protected int getType(final File document, final MetaData metaData) {
        int type = getType(document);
        if (type != FileType.UNKNOWN.legacyId() || metaData == null) {
            return type;
        }
        return FileType.fromMediaType(metaData.getMediaType()).legacyId();
    }

    /**
     * Detect the type of document. IDs remain stable independently of enum ordering.
     *
     * @param document to retrieve type from.
     * @return the stable legacy type ID, or zero for an unknown type.
     */
    protected int getType(final File document) {
        String path = document.getPath();
        int lastDot = path.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < path.length() - 1) {
            return FileType.fromExtension(path.substring(lastDot + 1)).legacyId();
        }
        return FileType.UNKNOWN.legacyId();
    }

    /**
     * Set the force flag on this appender. If this flag is set
     * to true then files will be modified directly, otherwise
     * new files will be created alongside the existing files.
     *
     * @param overwrite force flag.
     */
    public void setOverwrite(final boolean overwrite) {
        isOverwrite = overwrite;
    }

    /**
     * Gets the header text to insert into the file.
     * @param document document to extract from.
     * @return Get the license header of a document.
     */
    public abstract String getLicenseHeader(File document);

    /**
     * Gets a header using detected metadata. Defaults to the legacy formatting hook.
     * @param document the source file.
     * @param metaData detected metadata, or {@code null}.
     * @return the formatted header.
     */
    protected String getLicenseHeader(final File document, final MetaData metaData) {
        return getLicenseHeader(document);
    }

    /**
     * Get the first line of the license header formatted for the given type.
     * @param type the stable ID returned by {@link #getType(File)}.
     * @return the opening comment line, or an empty string.
     */
    protected String getFirstLine(final int type) {
        return FileType.fromLegacyId(type).commentStyle().firstLine();
    }

    /**
     * Get the last line of the license header formatted for the given type.
     * @param type the stable ID returned by {@link #getType(File)}.
     * @return the closing comment line, or an empty string.
     */
    protected String getLastLine(final int type) {
        return FileType.fromLegacyId(type).commentStyle().lastLine();
    }

    /**
     * Get a line of the license header formatted for the given type.
     * @param type the stable ID returned by {@link #getType(File)}.
     * @param content the content for this line.
     * @return the formatted line, or an empty string for an unknown type.
     */
    protected String getLine(final int type, final String content) {
        return FileType.fromLegacyId(type).commentStyle().line(content);
    }
}
