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
package org.apache.rat.document;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.apache.rat.utils.DefaultLog;


/**
 * A collection of names for a document. All names in the set are
 * either Rat (Linux like) or native (OS specific).
 */
public final class DocumentName implements Comparable<DocumentName> {
    /** True if the file system on which we are operating is case-sensitive */
    public static final boolean FS_IS_CASE_SENSITIVE;
    /** The full name for the document */
    private final String name;
    /** The name of the base directory for the document */
    private final String baseName;
    /** The directory separator for this document. */
    private final String dirSeparator;
    /** The case-sensitive flag */
    private final boolean isCaseSensitive;

    static {
        boolean fsSensitive = true;
        File f = null;
        try {
            Path p = Files.createTempDirectory("NameSet");
            f = p.toFile();
            fsSensitive = !new File(f, "a").equals(new File(f, "A"));
        } catch (IOException e) {
            fsSensitive = true;
        } finally {
            if (f != null) {
                try {
                    FileUtils.deleteDirectory(f);
                } catch (IOException e) {
                    DefaultLog.getInstance().warn("Unable to delete temporary directory: " + f, e);
                }
            }
        }
        FS_IS_CASE_SENSITIVE = fsSensitive;
    }

    /**
     * Creates a builder with directory separator and case sensitivity based on the local file system.
     * @return the Builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder from a File.  The base name is set to the file name if it is a directory otherwise
     * it is set to the directory containing the file. Directory separator is set from the file and
     * case sensitivity based on the local file system.
     * @param file The file to set defaults from.
     * @return the Builder.
     */
    public static Builder builder(final File file) {
        return new Builder(file);
    }

    /**
     * Creates a builder from a document name. The builder will be configured to create a clone of the document name.
     * @param documentName the document name to set the defaults from.
     * @return the builder.
     */
    public static Builder builder(final DocumentName documentName) {
        return new Builder(documentName);
    }

    /**
     * Builds the document name.
     * @param builder the builder to provide the values.
     */
    private DocumentName(final Builder builder) {
        this.name = builder.name;
        this.baseName = builder.baseName;
        this.dirSeparator = builder.dirSeparator;
        this.isCaseSensitive = builder.isCaseSensitive;
    }

    /**
     * Creates a new document name by adding the child to the current name.
     * @param child the child to add (must use directory separator from this document name).
     * @return the new document name with the same base name, directory separator and case sensitivity as this one.
     */
    public DocumentName resolve(final String child) {
        List<String> parts = new ArrayList<>();
        parts.addAll(Arrays.asList(tokenize(name)));
        parts.addAll(Arrays.asList(tokenize(child)));
        String newName = String.join(dirSeparator, parts);
        return new Builder(this).setName(newName).build();
    }

    /**
     * Gets the fully qualified name of the document.
     * @return the fully qualified name of the document.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the fully qualified basename of the document.
     * @return the fully qualified basename of the document.
     */
    public String getBaseName() {
        return baseName;
    }

    /**
     * Gets the DocumentName for the basename of this document name.
     * @return the DocumentName for the basename of this document name.
     */
    public DocumentName getBaseDocumentName() {
        return name.equals(baseName) ? this : builder(this).setName(baseName).build();
    }

    /**
     * Returns the directory separator.
     * @return the directory separator.
     */
    public String getDirectorySeparator() {
        return dirSeparator;
    }

    /**
     * Gets the portion of the name that is not part of the base name.
     * The resulting name will always start with the directory separator.
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
     * The resulting name will always start with the directory separator.
     * @param dirSeparator The character to use to separate directories in the result.
     * @return the portion of the name that is not part of the base name.
     */
    public String localized(final String dirSeparator) {
        return String.join(dirSeparator, tokenize(localized()));
    }

    /**
     * Tokenizes the string based on the dirSeparator
     * @param source the source to tokenize
     * @return the array of tokenized strings.
     */
    public String[] tokenize(final String source) {
        return source.split("\\Q" + dirSeparator + "\\E");
    }

    /**
     * Gets the last segment of the name. This is the part after the last directory separator.
     * @return the last segment of the name.
     */
    public String getShortName() {
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
    public int compareTo(final DocumentName o) {
        return FS_IS_CASE_SENSITIVE ? name.compareTo(o.name) : name.compareToIgnoreCase(o.name);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DocumentName that = (DocumentName) o;
        if (isCaseSensitive() == that.isCaseSensitive() &&  Objects.equals(dirSeparator, that.dirSeparator)) {
            return isCaseSensitive ? name.equalsIgnoreCase(that.name) : name.equals(that.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, dirSeparator, isCaseSensitive());
    }

    public static final class Builder {
        /** The name for the document */
        private String name;
        /** The base name for the document */
        private String baseName;
        /** The directory separator */
        private String dirSeparator;
        /** The case sensitivity flag */
        private boolean isCaseSensitive;

        private Builder() {
            isCaseSensitive = FS_IS_CASE_SENSITIVE;
            dirSeparator = File.separator;
        }

        private Builder(final File file) {
            this();
            setName(file);
            baseName = name;
            if (!file.isDirectory()) {
                File p = file.getParentFile();
                if (p != null) {
                    baseName = p.getAbsolutePath();
                }
            }
            isCaseSensitive = FS_IS_CASE_SENSITIVE;
            dirSeparator = File.separator;
        }

        private Builder(final DocumentName documentName) {
            this.name = documentName.name;
            this.baseName = documentName.baseName;
            this.isCaseSensitive = documentName.isCaseSensitive;
            this.dirSeparator = documentName.dirSeparator;
        }

        private void verify() {
            Objects.requireNonNull(name, "Name cannot be null");
            Objects.requireNonNull(baseName, "Base name cannot be null");
        }

        public Builder setName(final String name) {
            this.name = name;
            return this;
        }

        public Builder setName(final File file) {
            this.name = file.getAbsolutePath();
            return this;
        }

        public Builder setBaseName(final String baseName) {
            this.baseName = baseName;
            return this;
        }

        public Builder setBaseName(final DocumentName baseName) {
            this.baseName = baseName.getName();
            return this;
        }

        public Builder setBaseName(final File file) {
            this.baseName = file.getAbsolutePath();
            return this;
        }

        public Builder setDirSeparator(final String dirSeparator) {
            Objects.requireNonNull(dirSeparator, "Directory separator cannot be null");
            this.dirSeparator = dirSeparator;
            return this;
        }

        public Builder setCaseSensitive(final boolean isCaseSensitive) {
            this.isCaseSensitive = isCaseSensitive;
            return this;
        }

        public DocumentName build() {
            verify();
            return new DocumentName(this);
        }
    }

//         public DocumentName(final String name, final String baseName, final String dirSeparator, final boolean isCaseSensitive) {
//            this.name = Objects.requireNonNull(name);
//            this.baseName = Objects.requireNonNull(StringUtils.defaultIfEmpty(baseName, null));
//            this.dirSeparator = Objects.requireNonNull(dirSeparator);
//            this.isCaseSensitive = isCaseSensitive;
//        }
//
//        /**
//         * Creates a document name with the name of the file and the same basename as the baseName document.
//         * @param file the file to name the document from.
//         * @param baseName the DocumentName to provide the baseName.
//         */
//    public DocumentName(final File file, final DocumentName baseName) {
//            this(file.getAbsolutePath(), baseName.baseName, File.separator, FS_IS_CASE_SENSITIVE);
//        }
//
//        /**
//         * Creates a document name with the name and basename equal to the file name
//         * @param file the file name to use.
//         */
//    public DocumentName(final File file) {
//            this(file.getAbsolutePath(), file.getAbsolutePath(), File.separator, FS_IS_CASE_SENSITIVE);
//        }
//    }
}
