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
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * The name for a document.  The {@code DocumentName} is an immutable structure that handles all the intricacies of file
 * naming on various operating systems. DocumentNames have several components:
 * <ul>
 *     <li>{@code root} - where in the file system the name starts (e.g C: on windows). May be empty but not null.</li>
 *     <li>{@code dirSeparator} - the separator between name segments (e.g. "\\" on windows, "/" on linux). May not be
 *     empty or null.</li>
 *     <li>{@code name} - the name of the file relative to the {@code root}. May not be null. Does NOT begin with a {@code dirSeparator}</li>
 *     <li>{@code baseName} - the name of a directory or file from which this file is reported. A DocumentName with a
 *     {@code name} of "foo/bar/baz.txt" and a {@code baseName} of "foo" will be reported as "bar/baz.txt". May not be null.</li>
 *     <li>{@code isCaseSensitive} - identifies if the underlying file system is case-sensitive.</li>
 * </ul>
 * <p>
 *     {@code DocumentName}s are generally used to represent files on the files system. However, they are also used to represent files
 *     within an archive. When representing a file in an archive the baseName is the name of the enclosing archive document.
 * </p>
 */
public class DocumentName implements Comparable<DocumentName> {
    /** The full name for the document. */
    private final String name;
    /** The name of the base directory for the document. */
    private final DocumentName baseName;
    /** The file system info for this document. */
    private final FSInfo fsInfo;
    /** The root for the DocumentName. May be empty but not null. */
    private final String root;

    /**
     * Creates a Builder with the default file system info.
     * @return the builder.
     * @see FSInfo
     */
    public static Builder builder() {
        return new Builder(FSInfo.getDefault());
    }

    /**
     * Creates a builder with the specified FSInfo instance.
     * @param fsInfo the FSInfo to use for the builder.
     * @return a new builder.
     */
    public static Builder builder(final FSInfo fsInfo) {
        return new Builder(fsInfo);
    }

    /**
     * Creates a builder for the specified file system.
     * @param fileSystem the file system to create the builder on.
     * @return a new builder.
     */
    public static Builder builder(final FileSystem fileSystem) {
        return new Builder(fileSystem);
    }

    /**
     * Creates a builder from a File. The {@link #baseName} is set to the file name if it is a directory otherwise
     * it is set to the directory containing the file.
     * @param file The file to set defaults from.
     * @return the builder.
     */
    public static Builder builder(final File file) {
        return new Builder(file);
    }

    /**
     * Creates a builder from a document name. The builder will be configured to create a clone of the DocumentName.
     * @param documentName the document name to set the defaults from.
     * @return the builder.
     */
    public static Builder builder(final DocumentName documentName) {
        return new Builder(documentName);
    }

    /**
     * Builds the DocumentName from the builder.
     * @param builder the builder to provide the values.
     */
    DocumentName(final Builder builder) {
        this.name = builder.name;
        this.fsInfo = builder.fsInfo;
        this.root = builder.root;
        this.baseName = builder.sameNameFlag ? this : builder.baseName;
    }

    /**
     * Creates a file from the document name.
     * @return a new File object.
     */
    public File asFile() {
        return new File(getName());
    }

    /**
     * Creates a path from the document name.
     * @return a new Path object.
     */
    public Path asPath() {
        return Paths.get(name);
    }

    /**
     * Creates a new DocumentName by adding the child to the current name.
     * Resulting documentName will have the same base name.
     * @param child the child to add (must use directory separator from this document name).
     * @return the new document name with the same {@link #baseName}, directory sensitivity and case sensitivity as
     * this one.
     */
    public DocumentName resolve(final String child) {
        if (StringUtils.isBlank(child)) {
            return this;
        }
        String separator = getDirectorySeparator();
        String pattern = separator.equals("/") ? child.replace('\\', '/') :
                child.replace('/', '\\');

        if (!pattern.startsWith(separator)) {
             pattern = name + separator + pattern;
        }

        return new Builder(this).setName(fsInfo.normalize(pattern)).build();
    }

    /**
     * Gets the fully qualified name of the document.
     * @return the fully qualified name of the document.
     */
    public String getName() {
        return root + fsInfo.dirSeparator() + name;
    }

    /**
     * Gets the fully qualified basename of the document.
     * @return the fully qualified basename of the document.
     */
    public String getBaseName() {
        return baseName.getName();
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
        return baseName;
    }

    /**
     * Returns the directory separator.
     * @return the directory separator.
     */
    public String getDirectorySeparator() {
        return fsInfo.dirSeparator();
    }

    /**
     * Determines if the candidate starts with the root or separator strings.
     * @param candidate the candidate to check. If blank method will return {@code false}.
     * @param root the root to check. If blank the root check is skipped.
     * @param separator the separator to check. If blank the check is skipped.
     * @return true if either the root or separator check returned {@code true}.
     */
    boolean startsWithRootOrSeparator(final String candidate, final String root, final String separator) {
        if (StringUtils.isBlank(candidate)) {
            return false;
        }
        boolean result = !StringUtils.isBlank(root) && candidate.startsWith(root);
        if (!result) {
            result = !StringUtils.isBlank(separator) && candidate.startsWith(separator);
        }
        return result;
    }

    /**
     * Gets the portion of the name that is not part of the base name.
     * The resulting name will always start with the directory separator.
     * @return the portion of the name that is not part of the base name.
     */
    public String localized() {
        String result = getName();
        String baseNameStr = baseName.getName();
        if (result.startsWith(baseNameStr)) {
            result = result.substring(baseNameStr.length());
        }
        if (!startsWithRootOrSeparator(result, getRoot(), fsInfo.dirSeparator())) {
            result = fsInfo.dirSeparator() + result;
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
        String[] tokens = fsInfo.tokenize(localized());
        if (tokens.length == 0) {
            return dirSeparator;
        }
        if (tokens.length == 1) {
            return dirSeparator + tokens[0];
        }

        String modifiedRoot =  dirSeparator.equals("/") ? root.replace('\\', '/') :
                root.replace('/', '\\');
        String result = String.join(dirSeparator, tokens);
        return startsWithRootOrSeparator(result, modifiedRoot, dirSeparator) ? result : dirSeparator + result;
    }

    /**
     * Gets the last segment of the name. This is the part after the last directory separator.
     * @return the last segment of the name.
     */
    public String getShortName() {
        int pos = name.lastIndexOf(fsInfo.dirSeparator());
        return pos == -1 ? name : name.substring(pos + 1);
    }

    /**
     * Gets the case sensitivity flag.
     * @return {@code true} if the name is case-sensitive.
     */
    public boolean isCaseSensitive() {
        return fsInfo.isCaseSensitive();
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
    public int compareTo(final DocumentName other) {
        return CompareToBuilder.reflectionCompare(this, other);
    }

    @Override
    public boolean equals(final Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    /**
     * The file system information needed to process document names.
     */
    public static class FSInfo implements Comparable<FSInfo> {
        /** The common name for the file system this Info represents. */
        private final String name;
        /** The separator between directory names. */
        private final String separator;
        /** The case-sensitivity flag. */
        private final boolean isCaseSensitive;
        /** The list of roots for the file system. */
        private final List<String> roots;

        public static FSInfo getDefault() {
            FSInfo result = (FSInfo) System.getProperties().get("FSInfo");
            return result == null ?
                    new FSInfo("default", FileSystems.getDefault())
                    : result;
        }
        /**
         * Constructor. Extracts the necessary data from the file system.
         * @param fileSystem the file system to extract data from.
         */
        public FSInfo(final FileSystem fileSystem) {
            this("anon", fileSystem);
        }

        /**
         * Constructor. Extracts the necessary data from the file system.
         * @param fileSystem the file system to extract data from.
         */
        public FSInfo(final String name, final FileSystem fileSystem) {
            this.name = name;
            this.separator = fileSystem.getSeparator();
            this.isCaseSensitive = isCaseSensitive(fileSystem);
            roots = new ArrayList<>();
            fileSystem.getRootDirectories().forEach(r -> roots.add(r.toString()));
        }

        /**
         * Determines if the file system is case-sensitive.
         * @param fileSystem the file system to check.
         * @return {@code true} if the file system is case-sensitive.
         */
        private static boolean isCaseSensitive(final FileSystem fileSystem) {
            boolean isCaseSensitive = false;
            Path nameSet = null;
            Path filea = null;
            Path fileA = null;
            try {
                try {
                    Path root = fileSystem.getPath("");
                    nameSet = Files.createTempDirectory(root, "NameSet");
                    filea = nameSet.resolve("a");
                    fileA = nameSet.resolve("A");
                    Files.createFile(filea);
                    Files.createFile(fileA);
                    isCaseSensitive = true;
                } catch (IOException e) {
                    // do nothing
                } finally {
                    if (filea != null) {
                        Files.deleteIfExists(filea);
                    }
                    if (fileA != null) {
                        Files.deleteIfExists(fileA);
                    }
                    if (nameSet != null) {
                        Files.deleteIfExists(nameSet);
                    }
                }
            } catch (IOException e) {
                // do nothing.
            }
            return isCaseSensitive;
        }

        /**
         * Gets the common name for the underlying file system.
         * @return the common file system name.
         */
        @Override
        public String toString() {
            return name;
        }

        /**
         * Constructor for virtual/abstract file systems for example the entry names within an archive.
         * @param separator the separator string to use.
         * @param isCaseSensitive the case-sensitivity flag.
         * @param roots the roots for the file system.
         */
        FSInfo(final String name, final String separator, final boolean isCaseSensitive, final List<String> roots) {
            this.name = name;
            this.separator = separator;
            this.isCaseSensitive = isCaseSensitive;
            this.roots = new ArrayList<>(roots);
        }

        /**
         * Gets the directory separator.
         * @return The directory separator.
         */
        public String dirSeparator() {
            return separator;
        }

        /**
         * Gets the case-sensitivity flag.
         * @return the case-sensitivity flag.
         */
        public boolean isCaseSensitive() {
            return isCaseSensitive;
        }

        /**
         * Retrieves the root extracted from the name.
         * @param name the name to extract the root from
         * @return an optional containing the root or empty.
         */
        public Optional<String> rootFor(final String name) {
            for (String sysRoot : roots) {
                if (name.startsWith(sysRoot)) {
                    return Optional.of(sysRoot);
                }
            }
            return Optional.empty();
        }

        /**
         * Tokenizes the string based on the directory separator of this DocumentName.
         * @param source the source to tokenize.
         * @return the array of tokenized strings.
         */
        public String[] tokenize(final String source) {
            return source.split("\\Q" + dirSeparator() + "\\E");
        }

        /**
         * Removes {@code .} and {@code ..} from filenames.
         * @param pattern the file name pattern
         * @return the normalized file name.
         */
        public String normalize(final String pattern) {
            if (StringUtils.isBlank(pattern)) {
                return "";
            }
            List<String> parts = new ArrayList<>(Arrays.asList(tokenize(pattern)));
            for (int i = 0; i < parts.size(); i++) {
                String part = parts.get(i);
                if (part.equals("..")) {
                    if (i == 0) {
                        throw new IllegalStateException("Unable to create path before root");
                    }
                    parts.set(i - 1, null);
                    parts.set(i, null);
                } else if (part.equals(".")) {
                    parts.set(i, null);
                }
            }
            return parts.stream().filter(Objects::nonNull).collect(Collectors.joining(dirSeparator()));
        }

        @Override
        public int compareTo(final FSInfo other) {
            return CompareToBuilder.reflectionCompare(this, other);
        }

        @Override
        public boolean equals(final Object other) {
            return EqualsBuilder.reflectionEquals(this, other);
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }
    }

    /**
     * The Builder for a DocumentName.
     */
    public static final class Builder {
        /** The name for the document. */
        private String name;
        /** The base name for the document. */
        private DocumentName baseName;
        /** The file system info. */
        private final FSInfo fsInfo;
        /** The file system root. */
        private String root;
        /** A flag for baseName same as this. */
        private boolean sameNameFlag;

        /**
         * Create with default settings.
         */
        private Builder(final FSInfo fsInfo) {
            this.fsInfo = fsInfo;
            root = "";
        }

        /**
         * Create with default settings.
         */
        private Builder(final FileSystem fileSystem) {
            this(new FSInfo(fileSystem));
        }

        /**
         * Create based on the file provided.
         * @param file the file to base the builder on.
         */
        private Builder(final File file) {
            this(FSInfo.getDefault());
            setName(file);
        }

        /**
         * Used in testing.
         * @param fsInfo the FSInfo for the file.
         * @param file the file to process.
         */
        Builder(final FSInfo fsInfo, final File file) {
            this(fsInfo);
            setName(file);
        }

        /**
         * Create a Builder that clones the specified DocumentName.
         * @param documentName the DocumentName to clone.
         */
        Builder(final DocumentName documentName) {
            this.root = documentName.root;
            this.name = documentName.name;
            this.baseName = documentName.baseName;
            this.fsInfo = documentName.fsInfo;
        }

        /**
         * Get the directory separator for this builder.
         * @return the directory separator fo this builder.
         */
        public String directorySeparator() {
            return fsInfo.dirSeparator();
        }

        /**
         * Verify that the builder will build a proper DocumentName.
         */
        private void verify() {
            Objects.requireNonNull(name, "Name must not be null");
            if (name.startsWith(fsInfo.dirSeparator())) {
                name = name.substring(fsInfo.dirSeparator().length());
            }
            if (!sameNameFlag) {
                Objects.requireNonNull(baseName, "Basename must not be null");
            }
        }

        /**
         * Sets the root for the DocumentName.
         * @param root the root for the DocumentName.
         * @return this.
         */
        public Builder setRoot(final String root) {
            this.root = StringUtils.defaultIfBlank(root, "");
            return this;
        }

        /**
         * Sets the name for this DocumentName relative to the baseName.
         * If the {@code name} is {@code null} an empty string is used.
         * <p>
         *     To correctly parse the string it must use the directory separator specified by
         *     this Document.
         * </p>
         * @param name the name for this Document name. Will be made relative to the baseName.
         * @return this
         */
        public Builder setName(final String name) {
            Pair<String, String> pair = splitRoot(StringUtils.defaultIfBlank(name, ""));
            if (this.root.isEmpty()) {
                this.root = pair.getLeft();
            }
            this.name = fsInfo.normalize(pair.getRight());
            if (this.baseName != null && !baseName.name.isEmpty()) {
                if (!this.name.startsWith(baseName.name)) {
                    this.name = this.name.isEmpty() ? baseName.name :
                            baseName.name + fsInfo.dirSeparator() + this.name;
                }
            }
            return this;
        }

        /**
         * Extracts the root/name pair from a name string.
         * <p>
         *     Package private for testing.
         * </p>
         * @param name the name to extract the root/name pair from.
         * @return the root/name pair.
         */
        Pair<String, String> splitRoot(final String name) {
            String workingName = name;
            Optional<String> maybeRoot = fsInfo.rootFor(name);
            String root = maybeRoot.orElse("");
            if (!root.isEmpty()) {
                if (workingName.startsWith(root)) {
                    workingName = workingName.substring(root.length());
                    if (!workingName.startsWith(fsInfo.dirSeparator())) {
                        if (root.endsWith(fsInfo.dirSeparator())) {
                            root = root.substring(0, root.length() - fsInfo.dirSeparator().length());
                        }
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
         * Sets the properties from the file. Will reset the baseName appropriately.
         * @param file the file to set the properties from.
         * @return this.
         */
        public Builder setName(final File file) {
            Pair<String, String> pair = splitRoot(file.getAbsolutePath());
            setEmptyRoot(pair.getLeft());
            this.name = fsInfo.normalize(pair.getRight());
            if (file.isDirectory()) {
                sameNameFlag = true;
            } else {
                File p = file.getParentFile();
                if (p != null) {
                    setBaseName(p);
                } else {
                    Builder baseBuilder = new Builder(this.fsInfo).setName(this.directorySeparator());
                    baseBuilder.sameNameFlag = true;
                    setBaseName(baseBuilder.build());
                }
            }
            return this;
        }

        /**
         * Sets the baseName.
         * Will set the root if it is not set.
         * <p>
         *     To correctly parse the string it must use the directory separator specified by this builder.
         * </p>
         * @param baseName the basename to use.
         * @return this.
         */
        public Builder setBaseName(final String baseName) {
            DocumentName.Builder builder = DocumentName.builder(fsInfo).setName(baseName);
            builder.sameNameFlag = true;
            setBaseName(builder);
            return this;
        }

        /**
         * Sets the basename from the {@link #name} of the specified DocumentName.
         * Will set the root the baseName has the root set.
         * @param baseName the DocumentName to set the basename from.
         * @return this.
         */
        public Builder setBaseName(final DocumentName baseName) {
            this.baseName = baseName;
            if (!baseName.getRoot().isEmpty()) {
                this.root = baseName.getRoot();
            }
            return this;
        }

        /**
         * Executes the builder, sets the base name and clears the sameName flag.
         * @param builder the builder for the base name.
         */
        private void setBaseName(final DocumentName.Builder builder) {
            this.baseName = builder.build();
            this.sameNameFlag = false;
        }

        /**
         * Sets the basename from a File. Sets {@link #root} and the {@link #baseName}
         * Will set the root.
         * @param file the file to set the base name from.
         * @return this.
         */
        public Builder setBaseName(final File file) {
            DocumentName.Builder builder = DocumentName.builder(fsInfo).setName(file);
            builder.sameNameFlag = true;
            setBaseName(builder);
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
