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
import org.apache.rat.report.IReportable;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Abstract walker.
 */
public abstract class Walker implements IReportable {

    /** The file that this walker started at */
    private final File baseFile;

    /** The file name filter that the walker is applying */
    private final FilenameFilter filesToIgnore;

    public Walker(final File file, final FilenameFilter filesToIgnore) {
        this.baseFile = file;
        this.filesToIgnore = filesToIgnore == null ? FalseFileFilter.FALSE : filesToIgnore;
    }

    /**
     * Retrieve the file from the constructor.
     * @return the file from the constructor.
     */
    protected File getBaseFile() {
        return baseFile;
    }

    /**
     * Test if the specified file should be ignored.
     * @param file the file to test.
     * @return {@code true} if the file should be ignored.
     */
    protected final boolean isNotIgnored(final File file) {
        return !filesToIgnore.accept(file.getParentFile(), file.getName());
    }
}
