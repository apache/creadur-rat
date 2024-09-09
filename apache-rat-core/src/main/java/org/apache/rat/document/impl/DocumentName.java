/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.document.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;


/**
 * A collection of names for a document.  All names in the set are
 * either Rat (Linux like) or Native (OS specific).
 */
public final class DocumentName implements Comparable<DocumentName> {
    public static final boolean fsIsCaseSensitive;
    /** The full name for the document */
    private final String name;
    /** The name of the base directory for the document */
    private final String baseName;
    /** The directory separator for this document. */
    final String dirSeparator;
    /** The case-sensitive flag */
    final boolean isCaseSensitive;

    static {
        boolean fsSensitive = true;
        try {
            Path p = Files.createTempDirectory("NameSet");
            File f = p.toFile();
            fsSensitive = !new File(f, "a").equals(new File(f, "A"));
            FileUtils.deleteDirectory(f);
        } catch (IOException e) {
            fsSensitive = true;
        }
        fsIsCaseSensitive = fsSensitive;
    }

    /**
     * Creates a Dcoument name.
     * @param name the name of the document
     * @param baseName the base name of the document.
     * @param dirSeparator the directory separator used in the name.
     * @param isCaseSensitive {@code true} if the name is case-sensitive.
     */
    public DocumentName(String name, String baseName, String dirSeparator, boolean isCaseSensitive) {
        this.name = Objects.requireNonNull(name);
        this.baseName = Objects.requireNonNull(StringUtils.defaultIfEmpty(baseName, null));
        this.dirSeparator = Objects.requireNonNull(dirSeparator);
        this.isCaseSensitive = isCaseSensitive;
    }

    /**
     * Creates a document name with the name of the file and the same basename as the baseName document.
     * @param file the file to name the document from.
     * @param baseName the DocumentName to provide the baseName.
     */
    public DocumentName(File file, DocumentName baseName) {
        this(file.getAbsolutePath(), baseName.baseName, File.separator, fsIsCaseSensitive);
    }

    /**
     * Creates a document name with the name and basename equal to the file name
     * @param file the file name to use.
     */
    public DocumentName(File file) {
        this(file.getAbsolutePath(), file.getAbsolutePath(), File.separator, fsIsCaseSensitive);
    }

    /**
     * Creates a new document name by adding the child to the current name.
     * @param child the child to add (must use directory separator from this document name.
     * @return the new document name with the same base name, directory separator and case sensitivity as this one.
     */
    public DocumentName resolve(String child) {
        List<String> parts = new ArrayList<>();
        parts.addAll(Arrays.asList(tokenize(name)));
        parts.addAll(Arrays.asList(tokenize(child)));
        String newName = String.join(dirSeparator, parts);
        return new DocumentName(newName, baseName, dirSeparator, isCaseSensitive);
    }

    /**
     * Gets the fully qualified name of the document.
     * @return the fully qualified name of the document.
     */
    public String name() {
        return name;
    }

    /**
     * Gets the fully qualified basename of the document.
     * @return the fully qualified basename of the document.
     */
    public String baseName() {
        return baseName;
    }

    /**
     * Gets the DocumentName for the basename of this document name.
     * @return the DocumentName for the basename of this document name.
     */
    public DocumentName baseDocumentName() {
        return name.equals(baseName) ? this : new DocumentName(baseName, baseName, dirSeparator, isCaseSensitive);
    }

    /**
     * Returns the directory separator.
     * @return the directory separator.
     */
    public String dirSeparator() {
        return dirSeparator;
    }

    /**
     * Gets the portion of the name that is not part of the base name.
     * The resulting name will always start wit the directory separator.
     * @return the portion of the name that is not part of the base name.
     */
    public String localized() {
        String result = name;
        if (result.startsWith(baseName)) {
            result = result.substring(baseName.length());
        }
        if (!result.startsWith(dirSeparator)) {
            result = dirSeparator + result;
        }
        return result;
    }

    /**
     * Gets the portion of the name that is not part of the base name.
     * The resulting name will always start wit the directory separator.
     * @param dirSeparator The character to use to separate directories in the result.
     * @return the portion of the name that is not part of the base name.
     */
    public String localized(String dirSeparator) {
        return String.join(dirSeparator, tokenize(localized()));
    }


    /**
     * Tokenizes the string based on the dirSeparator
     * @param source  the source to tokenize
     * @return the array of tokenized strings.
     */
    private String[] tokenize(String source) {
        String[] result = source.split("\\Q"+dirSeparator+"\\E");
        return result;
        /*List<String> ret = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(source, dirSeparator);
        while (st.hasMoreTokens()) {
            ret.add(st.nextToken());
        }
        return ret.toArray(new String[0]);
        */

    }

    /**
     * Gets the last segment of the name.  This is the part after the last directory separator.
     * @return the last segment of the name.
     */
    public String shortName() {
        int pos = name.lastIndexOf(dirSeparator);
        return pos == -1 ? name : name.substring(pos + 1);
    }

    /**
     * Gets the case sensitivity flag.
     * @return {@code true} if the name is case-sensitive.
     */
    public boolean isCaseSensitive() {
        return isCaseSensitive;
    }

    @Override
    public String toString() {
        return localized();
    }

    @Override
    public int compareTo(DocumentName o) {
        return fsIsCaseSensitive ? name.compareTo(o.name) : name.compareToIgnoreCase(o.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentName that = (DocumentName) o;
        if (isCaseSensitive() == that.isCaseSensitive() &&  Objects.equals(dirSeparator, that.dirSeparator))
        {
            return isCaseSensitive ? name.equalsIgnoreCase(that.name) : name.equals(that.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, dirSeparator, isCaseSensitive());
    }
}
