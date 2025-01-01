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

package org.apache.rat.walker;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.io.IOUtils;
import org.apache.rat.api.Document;
import org.apache.rat.api.RatException;
import org.apache.rat.document.ArchiveEntryDocument;
import org.apache.rat.document.DocumentName;
import org.apache.rat.report.RatReport;
import org.apache.rat.utils.DefaultLog;

import static java.lang.String.format;

/**
 * Walks various kinds of archives files
 */
public class ArchiveWalker extends Walker {

    /**
     * Constructs a walker.
     * @param document the document to process.
     */
    public ArchiveWalker(final Document document) {
        super(document);
    }

    /**
     * Run a report over all files and directories in this GZIPWalker,
     * ignoring any files/directories set to be ignored.
     *
     * @param report the defined RatReport to run on this GZIP walker.
     *
     */
    public void run(final RatReport report) throws RatException {
        for (Document document : getDocuments()) {
            report.report(document);
        }
    }

    /**
     * Creates an input stream from the Directory being walked.
     * @return A buffered input stream reading the archive data.
     * @throws IOException on error
     */
    private InputStream createInputStream() throws IOException {
        return new BufferedInputStream(getDocument().inputStream());
    }
    /**
     * Retrieves the documents from the archive.
     * @return A collection of documents that pass the file filter.
     * @throws RatException on error.
     */
    public Collection<Document> getDocuments() throws RatException {
        List<Document> result = new ArrayList<>();
        //DocumentName.FSInfo archiveInfo = new DocumentName.FSInfo(true, Arrays.asList("/"), "/");
        try (ArchiveInputStream<? extends ArchiveEntry> input = new ArchiveStreamFactory().createArchiveInputStream(createInputStream())) {
            ArchiveEntry entry = null;
            while ((entry = input.getNextEntry()) != null) {
                if (!entry.isDirectory() && input.canReadEntryData(entry)) {
                    DocumentName innerName = DocumentName.builder().setName(entry.getName())
                            .setBaseName(".").build();
                    if (this.getDocument().getNameExcluder().matches(innerName)) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        IOUtils.copy(input, baos);
                        DocumentName archiveName = getDocument().getName();
                        String outerNameStr = format("%s#%s", archiveName.getName(), entry.getName());
                        DocumentName outerName = DocumentName.builder(archiveName).setName(outerNameStr).build();
                        result.add(new ArchiveEntryDocument(outerName, baos.toByteArray(), getDocument().getNameExcluder()));
                    }
                }
            }
        } catch (ArchiveException e) {
            DefaultLog.getInstance().warn(format("Unable to process %s: %s", getDocument().getName(), e.getMessage()));
        } catch (IOException e) {
            throw RatException.makeRatException(e);
        }
        return result;
    }
}
