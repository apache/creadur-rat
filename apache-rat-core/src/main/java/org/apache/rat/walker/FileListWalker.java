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

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.apache.rat.api.Document;
import org.apache.rat.api.RatException;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.FileDocument;
import org.apache.rat.report.IReportable;
import org.apache.rat.report.RatReport;

/**
 * Implementation of IReportable that traverses over a resource collection
 * internally.
 */
public class FileListWalker implements IReportable {
    /** The document name */
    private final Document source;

    public FileListWalker(final Document source) {
        this.source = source;
    }

    private DocumentName createDocumentName(final String unixFileName) {
        DocumentName sourceName = getName();
        DocumentName working = new DocumentName(unixFileName, "/", "/", sourceName.isCaseSensitive());
        if (sourceName.getDirectorySeparator().equals("/")) {
            return working;
        } else {
            return new DocumentName(working.localized(sourceName.getDirectorySeparator()), sourceName.getDirectorySeparator(),
                    sourceName.getDirectorySeparator(), sourceName.isCaseSensitive());
        }
    }

    @Override
    public void run(final RatReport report) throws RatException {
        DocumentName sourceName = getName();
        DocumentName baseDocumentName = new DocumentName(sourceName.getDirectorySeparator(), sourceName.getDirectorySeparator(),
                sourceName.getDirectorySeparator(), sourceName.isCaseSensitive());
        try (Reader reader = source.reader()) {
            for (String docName : IOUtils.readLines(reader)) {
                DocumentName documentName = createDocumentName(docName);
                if (!docName.startsWith("/")) {
                    documentName = sourceName.getBaseDocumentName().resolve(documentName.getName());
                }
                File file = new File(documentName.getName());
                FileDocument fd =
                        new FileDocument(baseDocumentName, file, d -> true);
                if (file.isDirectory()) {
                    new DirectoryWalker(fd).run(report);
                } else {
                    report.report(fd);
                }
            }
        } catch (IOException e) {
            throw new RatException("can not read " + sourceName.getName(), e);
        }
    }

    @Override
    public DocumentName getName() {
        return source.getName();
    }
}
