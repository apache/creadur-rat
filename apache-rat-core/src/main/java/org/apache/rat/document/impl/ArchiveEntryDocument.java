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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.SortedSet;

import org.apache.rat.api.Document;

/**
 * A Document that wraps an Archive entry.
 */
public class ArchiveEntryDocument extends Document {

    /** The contents of the entry */
    private final byte[] contents;

    /**
     * Creates an Archive entry.
     * @param outerName the name of this entry from outside the archive
     * @param contents the contents of the entry.
     * @param nameMatcher the name matcher to filter contents with.
     */
    public ArchiveEntryDocument(final DocumentName outerName, final byte[] contents, final DocumentNameMatcher nameMatcher) {
        super(outerName, nameMatcher);
        this.contents = contents;
    }

    @Override
    public InputStream inputStream() {
        return new ByteArrayInputStream(contents);
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public SortedSet<Document> listChildren() {
        return Collections.emptySortedSet();
    }

    @Override
    public Reader reader() {
        return new InputStreamReader(new ByteArrayInputStream(contents), StandardCharsets.UTF_8);
    }
}
