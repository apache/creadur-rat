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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.SortedSet;

import org.apache.commons.io.function.IOSupplier;
import org.apache.rat.api.Document;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.FSInfoTest;

public class TestingDocument extends Document {

    private final Reader reader;
    private final IOSupplier<InputStream> input;

    public TestingDocument() {
        this("name");
    }

    public TestingDocument(String name) {
        this(name, null);
    }

    public TestingDocument(DocumentName documentName) {
        super(documentName, DocumentNameMatcher.MATCHES_ALL);
        this.reader = null;
        this.input = null;
    }

    public TestingDocument(String name, DocumentNameMatcher matcher) {
        super(DocumentName.builder().setName(name).setBaseName("").build(), matcher);
        this.reader = null;
        this.input = null;
    }

    public TestingDocument(Reader reader, String name) {
        super(DocumentName.builder().setName(name).setBaseName("").build(), DocumentNameMatcher.MATCHES_ALL);
        this.reader = reader;
        this.input = null;
    }

    public TestingDocument(IOSupplier<InputStream> inputStream, String name) {
        super(DocumentName.builder(FSInfoTest.UNIX).setName(name).setBaseName("").build(), DocumentNameMatcher.MATCHES_ALL);
        this.input = inputStream;
        this.reader = null;
    }

    @Override
    public Reader reader() throws IOException {
            return reader == null ? new InputStreamReader(input.get()) : reader;
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
    public InputStream inputStream() throws IOException {
        if (input != null) {
            return input.get();
        }
        throw new UnsupportedOperationException();
    }
}
