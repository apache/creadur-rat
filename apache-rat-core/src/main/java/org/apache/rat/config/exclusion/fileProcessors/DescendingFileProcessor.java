/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.config.exclusion.fileProcessors;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.List;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.config.exclusion.ExclusionUtils;
import org.apache.rat.config.exclusion.FileProcessor;
import org.apache.rat.document.impl.DocumentName;

/**
 * A FileProcessor that assumes the files contains the already formatted strings and just need to be
 * localized for the fileName.
 */
public class DescendingFileProcessor implements FileProcessor {
    private final String fileName;
    protected final Predicate<String> commentFilter;
    protected final String separator;

    public DescendingFileProcessor(String fileName, String commentPrefix) {
        this(fileName, commentPrefix == null ? null : Collections.singletonList(commentPrefix), null);
    }

    public DescendingFileProcessor(String fileName, Iterable<String> commentPrefixes) {
        this(fileName, commentPrefixes, null);
    }

    /**
     * Package private for testing.
     * @param fileName the file name,
     * @param commentPrefixes the list of commong prefixes
     * @param separator the file separator string. (e.g. "/")
     */
    DescendingFileProcessor(String fileName, Iterable<String> commentPrefixes, String separator) {
        this.fileName = fileName;
        this.commentFilter = commentPrefixes == null ? StringUtils::isNotBlank : ExclusionUtils.commentFilter(commentPrefixes);
        this.separator = StringUtils.isEmpty(separator) ? File.separator : separator;
    }

    /**
     * Process the read the file and return a list of properly formatted patterns.
     * The default implementation does the following:
     * <ul>
     *     <li>reads lines from the file specified by documentName</li>
     *     <li>modifies those entries by calling {@link FileProcessor#modifyEntry(DocumentName, String)}</li>
     *     <li>further modifies the entry my calling {@link FileProcessor#localizePattern(DocumentName, String)}</li>
     *     <li>retrieving the name from the reulting DocumentName</li>
     * </ul>
     * @param documentName the file to read.
     * @return the list of properly formatted patterns
     */
    protected List<String> process(DocumentName documentName) {
        return ExclusionUtils.asIterator(new File(documentName.name()), commentFilter)
                .map(entry -> modifyEntry(documentName, entry))
                .filter(Objects::nonNull)
                .map(entry -> FileProcessor.localizePattern(documentName, entry))
                .map(DocumentName::name)
                .toList();
    }

    /**
     * Create a list of files by applying the filter to the specified directory.
     * @param dir the directory.
     * @param filter the filter.
     * @return a list of files.  May be empty but will not be null.
     */
    private File[] listFiles(File dir, FileFilter filter) {
        File[] result = dir.listFiles(filter);
        return result == null ? new File[0] : result;
    }

    /**
     * Process the directory tree looking for files that match the filter.  Process any matching file and return
     * a list of fully qualified patterns.
     * @param directory The name of the directory to process.
     * @param fileFilter the filter to detect processable files with.
     * @return the list of fully qualified file patterns.
     */
    private List<String> checkdirectory(DocumentName directory, FileFilter fileFilter) {
        List<String> fileNames = new ArrayList<>();
        File dirFile = new File(directory.name());
        for (File f : listFiles(dirFile, fileFilter))
        {
            fileNames.addAll(process(new DocumentName(f, directory)));
        }
        for (File dir : listFiles(dirFile, DirectoryFileFilter.DIRECTORY)) {
            fileNames.addAll(checkdirectory(new DocumentName(dir), fileFilter));
        }
        return fileNames;
    }

    @Override
    public List<String> apply(DocumentName dir) {
       return checkdirectory(dir, new NameFileFilter(fileName));
    }
}
