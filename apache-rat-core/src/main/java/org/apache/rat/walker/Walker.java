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

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;

/**
 * Abstract walker.
 */
public abstract class Walker implements IReportable {

    /** The file that this walker started at */


    /** The file name filter that the walker is applying */
    private final FilenameFilter filesToIgnore;
    private  final Document document;

    public Walker(final Document document, final FilenameFilter filesToIgnore) {
        this.document = document;
        this.filesToIgnore = filesToIgnore == null ? FalseFileFilter.FALSE : filesToIgnore;
    }

    /**
     * Retrieve the file from the constructor.
     * @return the file from the constructor.
     */
    protected Document getDocument() {
        return document;
    }

    /**
     * Test if the specified file should be ignored.
     * @param document the document to test.
     * @return {@code true} if the file should be ignored.
     */
    protected final boolean isNotIgnored() {
        return isNotIgnored(document.getPath());
    }

    /**
     * Test if the specified file should be ignored.
     * @param document the document to test.
     * @return {@code true} if the file should be ignored.
     */
    protected final boolean isNotIgnored(Path pth) {
        return !filesToIgnore.accept(pth.getParent().toFile(), pth.toString());
    }
}
