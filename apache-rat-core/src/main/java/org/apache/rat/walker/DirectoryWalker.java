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

import java.util.SortedSet;

import org.apache.rat.api.Document;
import org.apache.rat.api.RatException;
import org.apache.rat.report.RatReport;

/**
 * Walks directories.
 */
public class DirectoryWalker extends Walker {

    /**
     * Constructs a directory walker.
     *
     * @param document the document to process.
     */
    public DirectoryWalker(final Document document) {
        super(document);
    }

    /**
     * Run a report over all files and directories in this DirectoryWalker,
     * ignoring any files/directories set to be ignored.
     *
     * @param report the defined RatReport to run on this Directory walker.
     * @throws RatException on error
     */
    public void run(final RatReport report) throws RatException {
        process(report, getDocument());
    }

    /**
     * Process a directory, ignoring any files/directories set to be ignored.
     *
     * @param report the report to use in processing
     * @param document the document to run the report against
     * @throws RatException on error
     */
    protected void process(final RatReport report, final Document document) throws RatException {
        final SortedSet<Document> documents = document.listChildren();
        if (documents != null) {
            for (final Document doc : documents) {
                if (Document.Type.IGNORED == doc.getMetaData().getDocumentType() || !doc.isDirectory()) {
                    report.report(doc);
                } else {
                    process(report, doc);
                }
            }
        }
    }
}
