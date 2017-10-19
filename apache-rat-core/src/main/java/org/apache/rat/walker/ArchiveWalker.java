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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.rat.api.Document;
import org.apache.rat.api.RatException;
import org.apache.rat.document.impl.ArchiveEntryDocument;
import org.apache.rat.report.IReportable;
import org.apache.rat.report.RatReport;

/**
 * Walks various kinds of archives files
 */
public class ArchiveWalker extends Walker implements IReportable {

    /**
     * Constructs a walker.
     * @param file not null
     * @param filter filters input files (optional), 
     * or null when no filtering should be performed
     * @throws FileNotFoundException in case of I/O errors. 
     */
    public ArchiveWalker(File file, final FilenameFilter filter) throws FileNotFoundException {
        super(file, filter);
    }
    
    /**
     * Run a report over all files and directories in this GZIPWalker,
     * ignoring any files/directories set to be ignored.
     * 
     * @param report the defined RatReport to run on this GZIP walker.
     * 
     */
    public void run(final RatReport report) throws RatException {

        try {
            ArchiveInputStream input;

            /* I am really sad that classes aren't first-class objects in
               Java :'( */
            try {
                input = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(file)));
            } catch (IOException e) {
                try {
                    input = new TarArchiveInputStream(new BZip2CompressorInputStream(new FileInputStream(file)));
                } catch (IOException e2) {
                    input = new ZipArchiveInputStream(new FileInputStream(file));
                }
            }

            ArchiveEntry entry = input.getNextEntry();
            while (entry != null) {
                File f = new File(entry.getName());
                byte[] contents = new byte[(int) entry.getSize()];
                int offset = 0;
                int length = contents.length;

                while (offset < entry.getSize()) {
                    int actualRead = input.read(contents, offset, length);
                    length -= actualRead;
                    offset += actualRead;
                }

                if (!entry.isDirectory() && isNotIgnored(f)) {
                    report(report, contents, f);
                }

                entry = input.getNextEntry();
            }

            input.close();
        } catch (IOException e) {
            throw new RatException(e);
        }
    }

    /**
     * Report on the given file.
     * 
     * @param report the report to process the file with
     * @param file the file to be reported on
     * @throws RatException
     */
    private void report(final RatReport report, byte[] contents, File file) throws RatException {

        Document document = new ArchiveEntryDocument(file, contents);
        report.report(document);

    }
}
