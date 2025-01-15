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
import org.apache.rat.api.RatException;
import org.apache.rat.commandline.Arg;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.document.FileDocument;
import org.apache.rat.report.IReportable;
import org.apache.rat.report.RatReport;
import org.apache.rat.utils.DefaultLog;

/**
 * Implementation of IReportable that traverses over a resource collection
 * internally.
 */
public class FileListWalker implements IReportable {
    /** The source document name */
    private final FileDocument source;
    /** The root document name */
    private final FileDocument rootDoc;
    /** the base directory for the source document */
    private final FileDocument baseDoc;

    /**
     * Constructor.
     * @param source The file document that is the source from which this walker will read.
     */
    public FileListWalker(final FileDocument source) {
        DefaultLog.getInstance().info("Created file list document on " + source.getName().getName());
        DefaultLog.getInstance().info("... on root " + source.getName().getRoot());
        this.source = source;
        File baseDir = source.getFile().getParentFile().getAbsoluteFile();
        this.baseDoc = new FileDocument(baseDir, DocumentNameMatcher.MATCHES_ALL);
        File p = baseDir;
        while (p.getParentFile() != null) {
            p = p.getParentFile();
        }
        File rootDir = p;
        rootDoc = new FileDocument(rootDir, DocumentNameMatcher.MATCHES_ALL);
    }

    private FileDocument createDocument(final String unixFileName) {
        DocumentName sourceName = source.getName();
        String finalName = "/".equals(sourceName.getDirectorySeparator()) ? unixFileName :
            unixFileName.replace("/", sourceName.getDirectorySeparator());
        FileDocument documentBase = unixFileName.startsWith("/") ? rootDoc : baseDoc;
        File documentFile = new File(documentBase.getFile(), finalName).getAbsoluteFile();
        DefaultLog.getInstance().info("Creating document from " + unixFileName);
        return new FileDocument(rootDoc.getName(), documentFile, DocumentNameMatcher.MATCHES_ALL);
    }

    @Override
    public void run(final RatReport report) throws RatException {
        DefaultLog.getInstance().debug(String.format("Reading file name: %s due to option %s", source, Arg.SOURCE.option()));
        DocumentName sourceName = getName();
        try (Reader reader = source.reader()) {
            for (String docName : IOUtils.readLines(reader)) {
                try {
                    DefaultLog.getInstance().debug("Reading file name: " + docName);
                    FileDocument document = createDocument(docName);
                    if (document.isDirectory()) {
                        new DirectoryWalker(document).run(report);
                    } else {
                        report.report(document);
                    }
                } catch (RatException e) {
                    throw new RatException(String.format("Error reading file `%s` read from `%s`", docName, sourceName), e);
                }
            }
        }  catch (IOException e) {
            throw new RatException("can not read " + sourceName.getName(), e);
        }
    }

    @Override
    public DocumentName getName() {
        return source.getName();
    }
}
