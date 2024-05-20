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

import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.rat.api.Document;
import org.apache.rat.report.IReportable;

import java.io.FilenameFilter;
import java.nio.file.Path;

/**
 * Abstract walker.
 */
public abstract class Walker implements IReportable {

    /** The file name filter that the walker is applying */
    private final FilenameFilter filesToIgnore;
    /** The document this walker is walking */
    private  final Document document;

    /**
     * Creates  the walker
     * @param document The document the walker is walking.
     * @param filesToIgnore the Files to ignore.  If {@code null} no files are ignored.
     */
    protected Walker(final Document document, final FilenameFilter filesToIgnore) {
        this.document = document;
        this.filesToIgnore = filesToIgnore == null ? FalseFileFilter.FALSE : filesToIgnore;
    }

    /**
     * Retrieves the document from the constructor.
     * @return the document from the constructor.
     */
    protected Document getDocument() {
        return document;
    }

    /**
     * Tests if the specified path should be ignored.
     * @param path the Path to test
     * @return {@code true} if the file should not be ignored.
     */
    protected final boolean isNotIgnored(final Path path) {
        return !filesToIgnore.accept(path.getParent().toFile(), path.toString());
    }
}
