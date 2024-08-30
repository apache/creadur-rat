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

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.config.exclusion.ExclusionUtils;
import org.apache.rat.config.exclusion.FileProcessor;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.List;

public class DescendingFileProcessor implements FileProcessor {
    private final String fileName;
    protected final Predicate<String> commentFilter;

    public DescendingFileProcessor(String fileName, String commentPrefix) {
        this(fileName, commentPrefix == null ? null : Collections.singletonList(commentPrefix));
    }

    public DescendingFileProcessor(String fileName, Iterable<String> commentPrefixes) {
        this.fileName = fileName;
        this.commentFilter = commentPrefixes == null ? StringUtils::isNotBlank : ExclusionUtils.commentFilter(commentPrefixes);
    }

    protected List<String> process(File f) {
        final File dir = f.getParentFile();
        return ExclusionUtils.asIterator(f, commentFilter).map(s -> new File(dir, s).getPath())
                .toList();
    }

    private File[] listFiles(File dir, FileFilter filter) {
        File[] result = dir.listFiles(filter);
        return result == null ? new File[0] : result;
    }

    private List<String> checkdirectory(File directory, FileFilter fileFilter) {
        List<String> fileNames = new ArrayList<>();
        for (File f : listFiles(directory, fileFilter))
        {
            fileNames.addAll(process(f));
        }
        for (File dir : listFiles(directory, DirectoryFileFilter.DIRECTORY)) {
            fileNames.addAll(checkdirectory(dir, fileFilter));
        }
        return fileNames;
    }

    @Override
    public List<String> apply(String dir) {
        return checkdirectory(new File(dir), new NameFileFilter(Collections.singletonList(fileName)));
    }
}
