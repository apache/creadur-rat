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

/**
 * A Document for testing.  The document is guaranteed to have a name and may have content is specified in the constructor.
 */
public class TestingDocument extends Document {

    private final Reader reader;
    private final IOSupplier<InputStream> input;

    /**
     * Constructs a TestingDocument with the name "name" and no content.
     */
    public TestingDocument() {
        this("name");
    }

    /**
     * Constructs a TestingDocument with the specified "name", no content and will not have an associated document name matcher.
     * @param name The name of the document.
     */
    public TestingDocument(String name) {
        this(name, null);
    }

    /**
     * Constructs a TestingDocument with the specified DocumentName,no content and the {@link DocumentNameMatcher#MATCHES_ALL}
     * name matcher associated with it.
     * @documentName the document name.
     */
    public TestingDocument(DocumentName documentName) {
        super(documentName, DocumentNameMatcher.MATCHES_ALL);
        this.reader = null;
        this.input = null;
    }

    /**
     * Constructs a TestingDocument with the specified "name", no contentand the specified DocumentNameMatcher
     * associated with it.
     * @param name the document name.
     * @param matcher the associated document name matcher.
     */
    public TestingDocument(String name, DocumentNameMatcher matcher) {
        super(DocumentName.builder().setName(name).setBaseName("").build(), matcher);
        this.reader = null;
        this.input = null;
    }

    /**
     * Constructs a TestingDocument with the name "name" and the content provided by the reader.
     * @param reader the Reader that provides content for the document.
     * @param name the name of the document
     */
    public TestingDocument(Reader reader, String name) {
        super(DocumentName.builder().setName(name).setBaseName("").build(), DocumentNameMatcher.MATCHES_ALL);
        this.reader = reader;
        this.input = null;
    }

    /**
     * Constructs a TestingDocument with the name "name" and the content provided by the input stream.
     * @param inputSupplier the input supplier that provides content for the document as an input stream.
     * @param name the name of the document
     */
    public TestingDocument(IOSupplier<InputStream> inputSupplier, String name) {
        super(DocumentName.builder(FSInfoTest.UNIX).setName(name).setBaseName("").build(), DocumentNameMatcher.MATCHES_ALL);
        this.input = inputSupplier;
        this.reader = null;
    }

    /**
     * Gets the reader for the document content.
     * @return the Reader for the contents.
     * @throws IOException on IO error when reading from input stream.
     * @throws NullPointerException if neither the reader nor the input stream were provided.
     */
    @Override
    public Reader reader() throws IOException {
            return reader == null ? new InputStreamReader(input.get()) : reader;
    }

    /**
     * @return always returns false.
     */
    @Override
    public boolean isDirectory() {
        return false;
    }

    /**
     * @return Always returns an empty set.
     */
    @Override
    public SortedSet<Document> listChildren() {
        return Collections.emptySortedSet();
    }

    /**
     * Returns the input stream if it was provided in the constructor.
     * @return the input stream if it was provided in the constructor.
     * @throws IOException if the input stream can not be retrieved.
     * @throws UnsupportedOperationException if the input stream was not provided.
     */
    @Override
    public InputStream inputStream() throws IOException {
        if (input != null) {
            return input.get();
        }
        throw new UnsupportedOperationException();
    }
}
