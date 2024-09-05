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
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.SortedSet;

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
        /** A notice document (e.g. LICENSE file) */
        NOTICE,
        /** A binary file */
        BINARY,
        /** A standard document */
        STANDARD;
    }

    /** The path matcher used by this document */
    protected final PathMatcher pathMatcher;
    /** THe metadata for this document */
    private final MetaData metaData;
    /** The fully qualified name of this document */
    private final String name;
    /** The base directory for this document */
    private final String basedirStr;

    /**
     * Creates an instance.
     * @param name the name of the resource.
     * @param pathMatcher the path matcher to filter directories/files.
     */
    protected Document(final String basedirStr, final String name, final PathMatcher pathMatcher) {
        this.basedirStr = basedirStr;
        this.name = name;
        this.pathMatcher = pathMatcher;
        this.metaData = new MetaData();
    }

    /**
     * Returns the name of the current document.
     * @return the name of the current document.
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the base directory of the current document.
     * @return the base directory of the current document.
     */
    public final String getBasedir() {
        return basedirStr;
    }

    /**
     * Returns the name of the document relatvie to the basedir.
     * @return the name of the document
     */
    public final String localizedName() {
        String result = name;
        if (result.startsWith(basedirStr)) {
            result = result.substring(basedirStr.length());
        }
        if (! result.startsWith("/")) {
            result = "/" + result;
        }
        return result;
    }

    /**
     * Returns the file filter this document was created with.
     * @return the file filter this document was created with.
     */
    public final PathMatcher getPathMatcher() {
        return pathMatcher;
    }

    @Override
    public int compareTo(final Document doc) {
        return getPath().compareTo(doc.getPath());
    }

    @Override
    public int hashCode() {
        return getPath().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Document)) {
            return false;
        }
        return getPath().equals(((Document) obj).getPath());
    }

    /**
     * Get the path that identifies the document.
     * @return the path for the document.
     */
    public Path getPath() {
        return Paths.get(getName());
    }

    /**
     * Reads the contents of this document.
     * @return <code>Reader</code> not null
     * @throws IOException if this document cannot be read
     * composite archive
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
        return String.format("%s( name = %s metaData = %s )", this.getClass().getSimpleName(), getName(), getMetaData());
    }

    /**
     * Determines if this Document is a directory type.
     * @return {@code true} if this is a directory.
     */
    public abstract boolean isDirectory();

    /**
     * Gets a sorted set of Documents that are children of this document.
     * @return A sorted set of child Documents.  May  be empty
     */
    public abstract SortedSet<Document> listChildren();
}
