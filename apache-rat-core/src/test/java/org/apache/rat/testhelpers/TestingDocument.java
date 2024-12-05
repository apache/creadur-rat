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
package org.apache.rat.testhelpers;

import java.io.InputStream;
import java.io.Reader;
import java.util.Collections;
import java.util.SortedSet;

import org.apache.rat.api.Document;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.document.DocumentName;

public class TestingDocument extends Document {

    private final Reader reader;

    public TestingDocument() {
        this(null, "name");
    }

    public TestingDocument(String name) {
        this(null, name);
    }

    public TestingDocument(DocumentName documentName) {
        super(documentName, DocumentNameMatcher.MATCHES_ALL);
        this.reader = null;
    }

    public TestingDocument(String name, DocumentNameMatcher matcher) {
        super(DocumentName.builder().setName(name).setBaseName("").setDirSeparator("/").setCaseSensitive(true).build(), matcher);
        this.reader = null;
    }

    public TestingDocument(Reader reader, String name) {
        super(DocumentName.builder().setName(name).setBaseName("").setDirSeparator("/").setCaseSensitive(true).build(), DocumentNameMatcher.MATCHES_ALL);
        this.reader = reader;
    }

    @Override
    public Reader reader() {
        return reader;
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
    public InputStream inputStream() {
        throw new UnsupportedOperationException();
    }
}
