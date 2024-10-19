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
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.config.exclusion.ExclusionUtils;
import org.apache.rat.config.exclusion.FileProcessor;
import org.apache.rat.document.impl.DocumentName;

/**
 * A FileProcessor that assumes the files contain the already formatted strings and just need to be
 * localized for the fileName.
 */
public class DescendingFileProcessor implements FileProcessor {
    /** The name of the file being processed */
    private final String fileName;
    /** The predicate that will return {@code false} for any comment line in the file. */
    protected final Predicate<String> commentFilter;

    /**
     * Constructor.
     * @param fileName The name of the file to process.
     * @param commentPrefix the comment prefix
     */
    public DescendingFileProcessor(final String fileName, final String commentPrefix) {
        this(fileName, commentPrefix == null ? null : Collections.singletonList(commentPrefix));
    }

    /**
     * Constructor.
     * @param fileName name of the file to process
     * @param commentPrefixes a collection of comment prefixes.
     */
    public DescendingFileProcessor(final String fileName, final Iterable<String> commentPrefixes) {
        this.fileName = fileName;
        this.commentFilter = commentPrefixes == null ? StringUtils::isNotBlank : ExclusionUtils.commentFilter(commentPrefixes);
    }

    /**
     * Process by reading the file and return a list of properly formatted patterns.
     * The default implementation does the following:
     * <ul>
     *     <li>reads lines from the file specified by documentName</li>
     *     <li>modifies those entries by calling {@link FileProcessor#modifyEntry(DocumentName, String)}</li>
     *     <li>further modifies the entry by calling {@link FileProcessor#localizePattern(DocumentName, String)}</li>
     *     <li>retrieving the name from the resulting DocumentName</li>
     * </ul>
     * @param documentName the file to read.
     * @return the list of properly formatted patterns
     */
    protected List<String> process(final DocumentName documentName) {
        return ExclusionUtils.asIterator(new File(documentName.getName()), commentFilter)
                .map(entry -> modifyEntry(documentName, entry))
                .filter(Objects::nonNull)
                .map(entry -> FileProcessor.localizePattern(documentName, entry))
                .map(DocumentName::getName)
                .populateCollection(new ArrayList<>());
    }

    /**
     * Create a list of files by applying the filter to the specified directory.
     * @param dir the directory.
     * @param filter the filter.
     * @return an array of files. May be empty but will not be null.
     */
    private File[] listFiles(final File dir, final FileFilter filter) {
        File[] result = dir.listFiles(filter);
        return result == null ? new File[0] : result;
    }

    /**
     * Process the directory tree looking for files that match the filter. Process any matching file and return
     * a list of fully qualified patterns.
     * @param directory The name of the directory to process.
     * @param fileFilter the filter to detect processable files with.
     * @return the list of fully qualified file patterns.
     */
    private List<String> checkDirectory(final DocumentName directory, final FileFilter fileFilter) {
        List<String> fileNames = new ArrayList<>();
        File dirFile = new File(directory.getName());
        for (File f : listFiles(dirFile, fileFilter)) {
            fileNames.addAll(process(new DocumentName(f, directory)));
        }
        for (File dir : listFiles(dirFile, DirectoryFileFilter.DIRECTORY)) {
            fileNames.addAll(checkDirectory(new DocumentName(dir), fileFilter));
        }
        return fileNames;
    }

    @Override
    public List<String> apply(final DocumentName dir) {
       return checkDirectory(dir, new NameFileFilter(fileName));
    }
}
