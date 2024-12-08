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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.SortedSet;

import org.apache.rat.api.Document;

/**
 * Document wrapping a File object that is ignored by rule.
 */
public class IgnoredDocument extends Document {

    /**
     * Creates a File document.
     * @param basedir the base directory for this document.
     * @param file the file to wrap.
     * @param nameMatcher the path matcher to filter files/directories with.
     */
    public IgnoredDocument(final DocumentName basedir, final File file, final DocumentNameMatcher nameMatcher) {
        super(DocumentName.builder(file).setBaseName(basedir.getBaseName()).build(), nameMatcher);
        this.getMetaData().setDocumentType(Type.IGNORED);
        this.getMetaData().setIsDirectory(file.isDirectory());
    }

    @Override
    public boolean isDirectory() {
        return getMetaData().isDirectory();
    }

    @Override
    public SortedSet<Document> listChildren() {
        return Collections.emptySortedSet();
    }

    @Override
    public InputStream inputStream() throws IOException {
        return new ByteArrayInputStream(new byte[0]);
    }
}
