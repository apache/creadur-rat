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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.rat.utils.DefaultLog;

/**
 * The name for a document.  The {@code DocumentName} is an immutable structure that handles all the intricacies of file
 * naming on various operating systems.  DocumentNames have several components:
 * <ul>
 *     <li>{@code root} - Where in the file system the name starts (e.g C: on windows).  May be empty bu not null.</li>
 *     <li>{@code dirSeparator} - the separator between name segments (e.g. "\\" on windows, "/" on linux). My not be
 *     empty or null</li>
 *     <li>{@code name} - The name of the file relative to the {@code root}. May not be null. Does NOT begin with a {@code dirSeparator}</li>
 *     <li>{@code baseName} - The name of a directory or file from which this file is reported.  A DocumentName with a
 *     {@code name} of "foo/bar/baz.txt" and a {@code baseName} of "foo" will be reported as "bar/baz.txt". My not be null.</li>
 *     <li>{@code isCaseSensitive} - identifies if the underlying file system is cases sensitive.</li>
 * </ul>
 * <p>
 *     {@code DocumentName}s are generally used to represent files on the files system.  However they are also used to represent files
 *     within an archive.  When representing a file in an archive the baseName is the name of the archive document.
 * </p>
 */
public final class DocumentName implements Comparable<DocumentName> {
    /** The list of all roots on the file system. */
    static final Set<String> ROOTS = new HashSet<>();
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
    /** The root for the DocumentName.  May be empty but not null */
    private final String root;

    // determine the cases sensitivity of the File system we are operating on.
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

        // determine all the roots on the file system(s).
        File[] roots = File.listRoots();
        if (roots != null) {
            for (File root : roots) {
                String name = root.getPath();
                ROOTS.add(name);
            }
        }

    }

    /**
     * Creates a Builder with directory separator and case sensitivity based on the local file system.
     * @return the Builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder from a File.  The {@link #baseName} is set to the file name if it is a directory otherwise
     * it is set to the directory containing the file. The {@link #dirSeparator} is set from the file and
     * case sensitivity based on the local file system.
     * @param file The file to set defaults from.
     * @return the Builder.
     */
    public static Builder builder(final File file) {
        return new Builder(file);
    }

    /**
     * Creates a Builder from a document name. The Builder will be configured to create a clone of the DocumentName.
     * @param documentName the document name to set the defaults from.
     * @return the Builder.
     */
    public static Builder builder(final DocumentName documentName) {
        return new Builder(documentName);
    }

    /**
     * Builds the DocumentName from the builder.
     * @param builder the builder to provide the values.
     */
    private DocumentName(final Builder builder) {
        this.name = builder.name;
        this.baseName = builder.baseName;
        this.dirSeparator = builder.dirSeparator;
        this.isCaseSensitive = builder.isCaseSensitive;
        this.root = builder.root;
    }

    /**
     * Creates a new DocumentName by adding the child to the current name.
     * @param child the child to add (must use directory separator from this document name).
     * @return the new document name with the same {@link #baseName}, {@link #dirSeparator} and case sensitivity as
     * this one.
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
        return root + dirSeparator + name;
    }

    /**
     * Gets the fully qualified basename of the document.
     * @return the fully qualified basename of the document.
     */
    public String getBaseName() {
        return root + dirSeparator + baseName;
    }

    /**
     * Gets the root for this document.
     * @return the root for this document.
     */
    public String getRoot() {
        return root;
    }

    /**
     * Gets the DocumentName for the basename of this DocumentName.
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
     * @param dirSeparator The character(s) to use to separate directories in the result.
     * @return the portion of the name that is not part of the base name.
     */
    public String localized(final String dirSeparator) {
        return String.join(dirSeparator, tokenize(localized()));
    }

    /**
     * Tokenizes the string based on the {@link #dirSeparator} of this DocumentName.
     * @param source the source to tokenize
     * @return the array of tokenized strings.
     */
    public String[] tokenize(final String source) {
        return source.split("\\Q" + dirSeparator + "\\E");
    }

    /**
     * Gets the last segment of the name. This is the part after the last {@link #dirSeparator}..
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

    /**
     * Returns the localized file name.
     * @return the localized file name.
     */
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

    /**
     * The Builder for a DocumentName.
     */
    public static final class Builder {
        /** The name for the document */
        private String name;
        /** The base name for the document */
        private String baseName;
        /** The directory separator */
        private String dirSeparator;
        /** The case sensitivity flag */
        private boolean isCaseSensitive;
        /** The file system root */
        private String root;

        /**
         * Create with default settings.
         */
        private Builder() {
            isCaseSensitive = FS_IS_CASE_SENSITIVE;
            dirSeparator = File.separator;
            root = "";
        }

        /**
         * Create based on the file provided.
         * @param file the file to base the builder on.
         */
        private Builder(final File file) {
            this();
            setName(file);
            isCaseSensitive = FS_IS_CASE_SENSITIVE;
            dirSeparator = File.separator;
        }

        /**
         * Create a Builder that clones the specified DocumentName.
         * @param documentName the DocumentName to clone.
         */
        private Builder(final DocumentName documentName) {
            this.root = documentName.root;
            this.name = documentName.name;
            this.baseName = documentName.baseName;
            this.isCaseSensitive = documentName.isCaseSensitive;
            this.dirSeparator = documentName.dirSeparator;
        }

        /**
         * Verify that the builder will build a proper DocumentName.
         */
        private void verify() {
            Objects.requireNonNull(name, "Name cannot be null");
            Objects.requireNonNull(baseName, "Basename cannot be null");
            if (name.startsWith(dirSeparator)) {
                name = name.substring(dirSeparator.length());
            }
            if (baseName.startsWith(dirSeparator)) {
                baseName = baseName.substring(dirSeparator.length());
            }
        }

        /**
         * Sets the root for the DocumentName.
         * @param root the root for the DocumentName.
         * @return this.
         */
        public Builder setRoot(final String root) {
            this.root = root;
            return this;
        }

        /**
         * Sets the name for this DocumentName. Will reset the root to the empty string.
         * <p>
         *     To correctly parse the string it must either be the directory separator specified by
         *     {@link File#separator} or must have been explicitly set by calling {@link #setDirSeparator(String)}
         *     before making this call.
         * </p>
         * @param name the name for this Document name.
         * @return this
         */
        public Builder setName(final String name) {
            Pair<String, String> pair = splitRoot(name, dirSeparator);
            if (this.root.isEmpty()) {
                this.root = pair.getLeft();
            }
            this.name = pair.getRight();
            return this;
        }

        /**
         * Extracts the root/name pair from a file.
         * @param file the file to extract the root/naim pair from.
         * @return the root/name pair.
         */
        static Pair<String, String> splitRoot(final File file) {
            return splitRoot(file.getAbsolutePath(), File.separator);
        }

        /**
         * Extracts the root/name pair from a name string.
         * <p>
         *     Package private for testing.
         * </p>
         * @param name the name to extract the root/naim pair from.
         * @param dirSeparator the directory separator.
         * @return the root/name pair.
         */
        static Pair<String, String> splitRoot(final String name, final String dirSeparator) {
            String workingName = name;
            String root = "";
            for (String sysRoot : ROOTS) {
                if (workingName.startsWith(sysRoot)) {
                    workingName = workingName.substring(sysRoot.length());
                    if (!workingName.startsWith(dirSeparator)) {
                        if (sysRoot.endsWith(dirSeparator)) {
                            root = sysRoot.substring(0, sysRoot.length() - dirSeparator.length());
                        }
                        return ImmutablePair.of(root, workingName);
                    }
                }
            }
            return ImmutablePair.of(root, workingName);
        }

        /**
         * Sets the builder root if it is empty.
         * @param root the root to set the builder root to if it is empty.
         */
        private void setEmptyRoot(final String root) {
            if (this.root.isEmpty()) {
                this.root = root;
            }
        }

        /**
         * Sets the properties from the file.  This method sets the {@link #root} if it is empty, and resets {@link #name},
         * {@link #dirSeparator}, and {@link #baseName}.
         * @param file the file to set the properties from.
         * @return this.
         */
        public Builder setName(final File file) {
            Pair<String, String> pair = splitRoot(file);
            setEmptyRoot(pair.getLeft());
            this.name = pair.getRight();
            this.dirSeparator = File.separator;
            this.baseName = name;
            if (!file.isDirectory()) {
                File p = file.getParentFile();
                if (p != null) {
                    setBaseName(p);
                }
            }
            return this;
        }

        /**
         * Sets the baseName.
         * Will set the root if it is not set.
         * <p>
         *     To correctly parse the string it must either be the directory separator specified by
         *     {@link File#separator} or must have been explicitly set by calling {@link #setDirSeparator(String)}
         *     before making this call.
         * </p>
         * @param baseName the basename to use.
         * @return this.
         */
        public Builder setBaseName(final String baseName) {
            Pair<String, String> pair = splitRoot(baseName, dirSeparator);
            setEmptyRoot(pair.getLeft());
            this.baseName = pair.getRight();
            return this;
        }

        /**
         * Sets the basename from the {@link #name} of the specified DocumentName.
         * Will set the root the baseName has the root set.
         * @param baseName the DocumentName to set the basename from.
         * @return this.
         */
        public Builder setBaseName(final DocumentName baseName) {
            this.baseName = baseName.getName();
            if (!baseName.getRoot().isEmpty()) {
                this.root = baseName.getRoot();
            }
            return this;
        }

        /**
         * Sets the basename from a File.  Sets {@link #root} and the {@link #baseName}
         * Will set the root.
         * @param file the file to set the base name from.
         * @return this.
         */
        public Builder setBaseName(final File file) {
            Pair<String, String> pair = splitRoot(file);
            this.root = pair.getLeft();
            this.baseName = pair.getRight();
            return this;
        }

        /**
         * Sets the directory separator.
         * @param dirSeparator the directory separator to use.
         * @return this.
         */
        public Builder setDirSeparator(final String dirSeparator) {
            Objects.requireNonNull(dirSeparator, "Directory separator cannot be null");
            this.dirSeparator = dirSeparator;
            return this;
        }

        /**
         * Sets the {@link #isCaseSensitive} flag.
         * @param isCaseSensitive the expected state of the flag.
         * @return this.
         */
        public Builder setCaseSensitive(final boolean isCaseSensitive) {
            this.isCaseSensitive = isCaseSensitive;
            return this;
        }

        /**
         * Build a DocumentName from this builder.
         * @return A new DocumentName.
         */
        public DocumentName build() {
            verify();
            return new DocumentName(this);
        }
    }
}
