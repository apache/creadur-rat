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
package org.apache.rat.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedSet;

import org.apache.rat.document.CompositeDocumentException;

/**
 * The representation of a document being scanned.
 */
public abstract class Document implements Comparable<Document> {
    /**
     * An enumeraton of document types.
     */
    public enum Type {
        /** A generated document. */
        GENERATED,
        /** An unknown document type. */
        UNKNOWN,
        /** An archive type document. */
        ARCHIVE,
        /** A notice document (e.g. LICENSE file) */
        NOTICE,
        /** A binary file */
        BINARY,
        /** A standard document */
        STANDARD;;
    }

    private final MetaData metaData;
    private final String name;

    protected Document(String name) {
        this.name = name;
        this.metaData = new MetaData();
    }

    /**
     * @return the name of the current document.
     */
    public final String getName() {
        return name;
    }

    public int compareTo(Document doc) {
        return getPath().compareTo(doc.getPath());
    }

    @Override
    public int hashCode() {
        return getPath().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || ! (obj instanceof Document)) {
            return false;
        }
        return getPath().equals(((Document)obj).getPath());
    }

    public Path getPath() {
        return Paths.get(getName());
    }
    /**
     * Reads the contents of this document.
     * 
     * @return <code>Reader</code> not null
     * @throws IOException if this document cannot be read
     * @throws CompositeDocumentException if this document can only be read as a
     * composite archive
     */
    public abstract Reader reader() throws IOException;

    /**
     * Streams the document's contents.
     * 
     * @return a non null input stream of the document.
     * @throws IOException when stream could not be opened
     */
    public abstract InputStream inputStream() throws IOException;

    /**
     * Gets data describing this resource.
     * 
     * @return a non null MetaData object.
     */
    public final MetaData getMetaData() {
        return metaData;
    }

    /**
     * Tests if this a composite document.
     * 
     * @return true if composite, false otherwise
     */
    public abstract boolean isComposite();

    /**
     * Representations suitable for logging.
     * @return a <code>String</code> representation
     * of this object.
     */
    @Override
    public String toString()
    {
        return String.format("%s( name = %s metaData = %s )", this.getClass().getSimpleName(), getName(), getMetaData());
    }

    public abstract boolean isDirectory();

    public abstract SortedSet<Document> listChildren();

}
