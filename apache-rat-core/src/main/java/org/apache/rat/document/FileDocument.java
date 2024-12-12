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
package org.apache.rat.document;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.rat.api.Document;
import org.apache.rat.config.exclusion.ExclusionUtils;

/**
 * Document wrapping a File object.
 */
public class FileDocument extends Document {

    /** The wrapped file */
    private final File file;

    /**
     * Creates a File document.
     * @param basedir the base directory for this document.
     * @param file the file to wrap.
     * @param nameMatcher the path matcher to filter files/directories with.
     */
    public FileDocument(final DocumentName basedir, final File file, final DocumentNameMatcher nameMatcher) {
        super(DocumentName.builder(file).setBaseName(basedir.getBaseName()).build(), nameMatcher);
        this.file = file;
    }

    /**
     * Creates a File document where the baseDir is the root directory.
     * @param file the file to wrap.
     * @param nameMatcher the path matcher to filter files/directories with.
     */
    public FileDocument(final File file, final DocumentNameMatcher nameMatcher) {
        super(DocumentName.builder(file).setBaseName(File.separator).build(), nameMatcher);
        this.file = file;
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }

    @Override
    public SortedSet<Document> listChildren() {
        if (isDirectory()) {
            SortedSet<Document> result = new TreeSet<>();
            File[] files = file.listFiles();
            if (files != null) {
                FileFilter fileFilter = ExclusionUtils.asFileFilter(name, nameMatcher);
                for (File child : files) {
                    if (fileFilter.accept(child)) {
                        result.add(new FileDocument(name, child, nameMatcher));
                    } else {
                        result.add(new IgnoredDocument(name, child, nameMatcher));
                    }
                }
            }
            return result;
        }
        return Collections.emptySortedSet();
    }

    @Override
    public InputStream inputStream() throws IOException {
        return Files.newInputStream(file.toPath());
    }

    /**
     * Gets the file underlying this document.
     * @return the file underlying this document.
     */
    public File getFile() {
        return file;
    }
}
