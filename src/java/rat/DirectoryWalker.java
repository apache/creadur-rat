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

package rat;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Pattern;

import rat.document.IDocument;
import rat.document.impl.FileDocument;
import rat.document.impl.zip.ZipFileUnarchiver;
import rat.report.IReportable;
import rat.report.RatReport;
import rat.report.RatReportFailedException;

/**
 * Walks directories.
 */
public class DirectoryWalker implements IReportable {

    private static final ZipFileUnarchiver UNARCHIVER = new ZipFileUnarchiver();
    private static final FileNameComparator COMPARATOR = new FileNameComparator();
    
    protected final File file;
    protected final String name;

    private final Pattern ignoreNameRegex;;
    
	public DirectoryWalker(File file) {
            this(file, null);
	}
	
    public DirectoryWalker(File file, final Pattern ignoreNameRegex) {
        this(file.getPath(), file, ignoreNameRegex);
    }

    private DirectoryWalker(final String name, final File file, final Pattern ignoreNameRegex) {
        this.name = name;
        this.file = file;
        this.ignoreNameRegex = ignoreNameRegex;
    }
	
    public boolean isRestricted() {
        return false;
    }

    protected final boolean ignored(final String name) {
        boolean result = false;
        if (ignoreNameRegex != null) {
            result = ignoreNameRegex.matcher(name).matches();
        }
        return result;
    }

	
	boolean isRestricted(File file) {
		String name = file.getName();
		boolean result = name.startsWith(".");
		return result;
	}
    
    private void processDirectory(RatReport  report, final File file) throws RatReportFailedException {
        if (!isRestricted(file)) {
            process(report, file);
        }
    }
    
    public void run(final RatReport report) throws RatReportFailedException {
        process(report, file);
    }

    private void process(final RatReport report, final File file) throws RatReportFailedException {
        final File[] files = file.listFiles();
        Arrays.sort(files, COMPARATOR);
        if (files != null) {
            // breadth first traversal
            processNonDirectories(report, files);
            processDirectories(report, files);
        }
    }

    private void processDirectories(final RatReport report, final File[] files) throws RatReportFailedException {
        for (int i = 0; i < files.length; i++) {
            final File file = files[i];
            final String name = file.getName();
            if (!ignored(name)) {
                if (file.isDirectory()) {
                    processDirectory(report, file);
                }
            }
        }
    }

    private void processNonDirectories(final RatReport report, final File[] files) throws RatReportFailedException {
        for (int i = 0; i < files.length; i++) {
            final File file = files[i];
            final String name = file.getName();
            if (!ignored(name)) {
                if (!file.isDirectory()) {
                    report(report, file);
                }
            }
        }
    }

    private void report(final RatReport report, File file) throws RatReportFailedException {

        IDocument document = new FileDocument(file, UNARCHIVER);
        report.report(document);

    }
}
