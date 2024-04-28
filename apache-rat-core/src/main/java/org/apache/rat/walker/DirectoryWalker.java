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
import java.io.FilenameFilter;
import java.util.Arrays;

import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.rat.api.RatException;
import org.apache.rat.document.impl.FileDocument;
import org.apache.rat.report.IReportable;
import org.apache.rat.report.RatReport;

/**
 * Walks directories.
 */
public class DirectoryWalker extends Walker implements IReportable {

    private static final FileNameComparator COMPARATOR = new FileNameComparator();

    private final IOFileFilter directoriesToIgnore;

    /**
     * Constructs a walker.
     *
     * @param file the directory to walk (not null).
     * @param filesToIgnore filters input files (optional),
     *               or null when no filtering should be performed
     * @param directoriesToIgnore filters directories (optional), or null when no filtering should be performed.
     */
    public DirectoryWalker(final File file, final FilenameFilter filesToIgnore, final IOFileFilter directoriesToIgnore) {
        super(file, filesToIgnore);
        this.directoriesToIgnore = directoriesToIgnore == null ? FalseFileFilter.FALSE : directoriesToIgnore;
    }

    /**
     * Process a directory, restricted directories will be ignored.
     *
     * @param report The report to process the directory with
     * @param file   the directory to process
     * @throws RatException on error.
     */
    private void processDirectory(final RatReport report, final File file) throws RatException {
        if (!directoriesToIgnore.accept(file)) {
            process(report, file);
        }
    }

    /**
     * Run a report over all files and directories in this DirectoryWalker,
     * ignoring any files/directories set to be ignored.
     *
     * @param report the defined RatReport to run on this Directory walker.
     * @throws RatException on error
     */
    public void run(final RatReport report) throws RatException {
        process(report, getBaseFile());
    }

    /**
     * Process a directory, ignoring any files/directories set to be ignored.
     *
     * @param report the report to use in processing
     * @param file   the run the report against
     * @throws RatException on error
     */
    protected void process(final RatReport report, final File file) throws RatException {
        final File[] files = file.listFiles();
        if (files != null) {
            Arrays.sort(files, COMPARATOR);
            // breadth first traversal
            processNonDirectories(report, files);
            processDirectories(report, files);
        }
    }

    private boolean isNotIgnoredDirectory(final File file) {
        return !directoriesToIgnore.accept(file.getParentFile(), file.getName());
    }

    /**
     * Process all directories in a set of file objects, ignoring any directories set to be ignored.
     *
     * @param report the report to use in processing
     * @param files  the files to process (only directories will be processed)
     * @throws RatException on error
     */
    private void processDirectories(final RatReport report, final File[] files) throws RatException {
        for (final File file : files) {
            if (file.isDirectory() && isNotIgnoredDirectory(file)) {
                processDirectory(report, file);
            }
        }
    }

    /**
     * Process all files in a set of file objects, ignoring any files set to be ignored.
     *
     * @param report the report to use in processing
     * @param files  the files to process (only files will be processed)
     * @throws RatException on error
     */
    private void processNonDirectories(final RatReport report, final File[] files) throws RatException {
        for (final File file : files) {
            if (!file.isDirectory() && isNotIgnored(file)) {
                report.report(new FileDocument(file));
            }
        }
    }
}
