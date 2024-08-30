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
package org.apache.rat.document.impl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.rat.api.Document;
import org.apache.rat.config.exclusion.ExclusionUtils;

/**
 * Document wrapping a File object
 */
public class FileDocument extends Document {

    /** the wrapped file */
    private final File file;

    /**
     * Creates a File document.
     * @param file the file to wrap.
     * @param pathMatcher the path matcher to filter files/directories with.
     */
    public FileDocument(final File file, final PathMatcher pathMatcher) {
        super(normalizeFileName(file), pathMatcher);
        this.file = file;
    }

    /**
     * Normalizes a file name to linux style.  Accounts for Windows to Unix conversion.
     * @param file The file to normalize
     * @return the String for the file name.
     */
    public static String normalizeFileName(final File file) {
        String path = file.getPath();
        return path.replace('\\', '/');
    }

    @Override
    public Path getPath() {
        return file.toPath();
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }

    @Override
    public SortedSet<Document> listChildren() {
        if (isDirectory()) {
            SortedSet<Document> result = new TreeSet<>();
            File[] lst = file.listFiles(ExclusionUtils.asFileFilter(pathMatcher));
            if (lst != null && lst.length > 0) {
                Arrays.stream(lst).map(f -> new FileDocument(f, pathMatcher)).forEach(result::add);
            }
            return result;
        }
        return Collections.emptySortedSet();
    }

    public Reader reader() throws IOException {
        return new FileReader(file);
    }

    public InputStream inputStream() throws IOException {
        return Files.newInputStream(file.toPath());
    }
}
