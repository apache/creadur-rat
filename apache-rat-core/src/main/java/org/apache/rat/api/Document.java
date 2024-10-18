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
import java.util.SortedSet;

import org.apache.rat.document.impl.DocumentName;
import org.apache.rat.document.impl.DocumentNameMatcher;

/**
 * The representation of a document being scanned.
 */
public abstract class Document implements Comparable<Document> {

    /**
     * An enumeration of document types.
     */
    public enum Type {
        /** A generated document. */
        GENERATED,
        /** An unknown document type. */
        UNKNOWN,
        /** An archive type document. */
        ARCHIVE,
        /** A notice document (e.g. LICENSE file). */
        NOTICE,
        /** A binary file. */
        BINARY,
        /** A standard document. */
        STANDARD;
    }

    /** The path matcher used by this document */
    protected final DocumentNameMatcher nameMatcher;
    /** The metadata for this document */
    private final MetaData metaData;
    /** The fully qualified name of this document */
    protected final DocumentName name;

    /**
     * Creates an instance.
     * @param name the native NameSet of the resource.
     * @param nameMatcher the document name matcher to filter directories/files.
     */
    protected Document(final DocumentName name, final DocumentNameMatcher nameMatcher) {
        this.name = name;
        this.nameMatcher = nameMatcher;
        this.metaData = new MetaData();
    }

    /**
     * Gets the name of the current document.
     * @return the name of the current document.
     */
    public final DocumentName getName() {
        return name;
    }

    /**
     * Gets the file filter this document was created with.
     * @return the file filter this document was created with.
     */
    public final DocumentNameMatcher getNameMatcher() {
        return nameMatcher;
    }

    @Override
    public int compareTo(final Document doc) {
        return name.compareTo(doc.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Document)) {
            return false;
        }
        return name.equals(((Document) obj).name);
    }

    /**
     * Reads the contents of this document.
     * @return <code>Reader</code> not null
     * @throws IOException if this document cannot be read
     */
    public abstract Reader reader() throws IOException;

    /**
     * Streams the document's contents.
     * @return a non null input stream of the document.
     * @throws IOException when stream could not be opened
     */
    public abstract InputStream inputStream() throws IOException;

    /**
     * Gets data describing this resource.
     * @return a non null MetaData object.
     */
    public final MetaData getMetaData() {
        return metaData;
    }

    /**
     * Representations suitable for logging.
     * @return a <code>String</code> representation
     * of this object.
     */
    @Override
    public String toString() {
        return String.format("%s( name = %s metaData = %s )", this.getClass().getSimpleName(), getName().localized(), getMetaData());
    }

    /**
     * Determines if this Document is a directory type.
     * @return {@code true} if this is a directory.
     */
    public abstract boolean isDirectory();

    /**
     * Gets a sorted set of Documents that are children of this document.
     * @return A sorted set of child Documents. May be empty.
     */
    public abstract SortedSet<Document> listChildren();
}
