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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * The name for a document. The {@code DocumentName} is an immutable structure that handles all the intricacies of file
 * naming on various operating systems. DocumentNames have several components:
 * <ul>
 *     <li>{@code root} - where in the file system the name starts (e.g C:\ on Microsoft Windows). May be empty but not null.</li>
 *     <li>{@code dirSeparator} - the separator between name segments (e.g. "\" on Microsoft Windows, "/" on linux). May not be
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
    /** The root for the DocumentName. May be empty but not null. Must be one of the roots in fsInfo. */
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
     * Creates a file from the fully qualified document name.
     * @return a new File object.
     */
    public File asFile() {
        return new File(getName());
    }

    /**
     * Creates a path from the document name. This method uses the fully qualified name without the root.
     * this results in a relative file name from the root.
     * @return a new Path object.
     */
    public Path asPath() {
        return Paths.get(name);
    }

    /**
     * Creates a new DocumentName by adding the child to the current name.
     * Resulting documentName will have the same base name.
     * Directory separator is normalized to the directory separator for this file system.
     * If the child string:
     * <dl>
     *     <dt>Is blank</dt>
     *     <dd>This DocumentName is returned.</dd>
     *     <dt>Starts with the file system root</dt>
     *     <dd>The root must match the root of this DocumentName and the directory structure
     *     must start with the directory structure of the basename for this DocumentName.</dd>
     *     <dt>Starts with the directory separator character<dt>
     *     <dd>Result will be a tree starting at the directory specified by the basename.</dd>
     *     <dt>Does not start with a directory separator character</dt>
     *     <dd>Result will be a tree starting at the directory specified by this DocumentName</dd>
     * </dl>
     * @param child the child to add (must use directory separator from this document name).
     * @return the new document name with the same {@link #baseName}, directory sensitivity and case sensitivity as
     * this one.
     * @throws IllegalArgumentException if the child specifies a different root from this document name.
     */
    public DocumentName resolve(final String child) {
        if (StringUtils.isBlank(child)) {
            return this;
        }
        String separator = getDirectorySeparator();
        String pattern = separator.equals("/") ? child.replace('\\', '/') :
                child.replace('/', '\\');

        Optional<String> expectedRoot = fsInfo.rootFor(child);
        if (expectedRoot.isPresent()) {
            if (!expectedRoot.get().equals(getRoot())) {
                throw new IllegalArgumentException(String.format("%s does not start with %s", pattern, getName()));
            }
            if (!getRoot().equals(separator)) {
                // we have something like C:\ as the root so convert the pattern to start with the separator.
                pattern = separator + pattern.substring(getRoot().length());
                if (pattern.startsWith(baseName.name)) {
                    pattern = pattern.substring(baseName.name.length());
                }
            }
        }

        // Patterns with separators either start with the name of this document plus a relative
        // name, or are just directory off the baseName. In either case the name is correct.
        // So just handle the relative case.
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
        return root + name;
    }

    /**
     * Gets the path of the document. This is the fully qualified name without the root but starting with a path separator.
     * @return the path of the document.
     */
    public String getPath() {
        return getDirectorySeparator() + name;
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
     * Returns the FSInfo for this document name.
     * @return the FSInfo for this document name.
     */
    public FSInfo fsInfo() {
        return fsInfo;
    }

    /**
     * Determines if the candidate starts with the root or separator strings.
     * @param candidate the candidate to check. If blank method will return {@code false}.
     * @param root the root to check. If blank the root check is skipped.
     * @param separator the separator to check. If blank the check is skipped.
     * @return true if either the root or separator check returned {@code true}.
     */
    static boolean startsWithRootOrSeparator(final String candidate, final String root, final String separator) {
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
        return new CompareToBuilder()
                .append(this.root, other.root)
                .append(this.getBaseName(), other.getBaseName())
                .append(this.getName(), other.getName()).build();
    }

    @Override
    public final boolean equals(final Object other) {
        if (other instanceof DocumentName otherDocumentName) {
            return compareTo(otherDocumentName) == 0;
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return getName().hashCode();
    }

    /**
     * The File System Info Data for a DocumentName.
     * Use to preserve data across DocumentNames without having to
     * reconstruct the data for each DocumentName.
     */
    private static final class FSInfoData {
        /** The case sensitivity flag */
        private final boolean isCaseSensitive;
        /** The list of roots for the file system. */
        private final List<String> roots;
        /** The separator between directory names. */
        private final String separator;

        /**
         * Constructor for known properties.
         * @param separator the directory separator character(s).
         * @param isCaseSensitive {@code true} if the file system is cases sensitive.
         * @param roots THe list of roots for the file system.
         */
        FSInfoData(final String separator, final boolean isCaseSensitive, final List<String> roots) {
            this.isCaseSensitive = isCaseSensitive;
            this.roots = roots;
            this.separator = separator;
        }

        /**
         * Constructor for an arbitrary file system.
         * This constructor can be processor intensive as it has to check the file system for case sensitivity.
         * @param fileSystem the file system.
         */
        FSInfoData(final FileSystem fileSystem) {
            isCaseSensitive = isCaseSensitive(fileSystem);
            roots = new ArrayList<>();
            fileSystem.getRootDirectories().forEach(r -> roots.add(r.toString()));
            separator = fileSystem.getSeparator();
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

    }
    /**
     * The file system information needed to process document names.
     */
    public static final class FSInfo implements Comparable<FSInfo> {
        /**
         * The map of FileSystem to FSInfoData used to avoid expensive FileSystem processing.
         */
        private static final Map<FileSystem, FSInfoData> REGISTRY = new ConcurrentHashMap<>();

        /** The case-sensitivity flag. */
        private final FSInfoData data;

        /** The common name for the file system */
        private final String name;

        /**
         * Gets the FSInfo for the default file system.
         * If the System property {@code FSInfo} is set, the {@code FSInfo} stored there is used, otherwise
         * the {@link FileSystem} returned from {@link FileSystems#getDefault()} is used.
         * @return the FSInfo for the default file system.
         */
        public static FSInfo getDefault() {
            FSInfo result = (FSInfo) System.getProperties().get("FSInfo");
            return result == null ?
                    new FSInfo(FileSystems.getDefault())
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
         * @param name the common name for the file system.
         * @param fileSystem the file system to extract data from.
         */
        FSInfo(final String name, final FileSystem fileSystem) {
            this.data = REGISTRY.computeIfAbsent(fileSystem, k -> new FSInfoData(fileSystem));
            this.name = name;
        }

        /**
         * Constructor for virtual/abstract file systems for example the entry names within an archive.
         * @param name the common name for the file system.
         * @param separator the separator string to use.
         * @param isCaseSensitive the case-sensitivity flag.
         * @param roots the roots for the file system.
         */
        FSInfo(final String name, final String separator, final boolean isCaseSensitive, final List<String> roots) {
            data = new FSInfoData(separator, isCaseSensitive, roots);
            this.name = name;
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
         * Gets the directory separator.
         * @return The directory separator.
         */
        public String dirSeparator() {
            return data.separator;
        }

        /**
         * Gets the case-sensitivity flag.
         * @return the case-sensitivity flag.
         */
        public boolean isCaseSensitive() {
            return data.isCaseSensitive;
        }

        /**
         * Retrieves the root extracted from the name.
         * @param name the name to extract the root from
         * @return an optional containing the root or empty.
         */
        public Optional<String> rootFor(final String name) {
            for (String sysRoot : data.roots) {
                if (name.startsWith(sysRoot)) {
                    return Optional.of(sysRoot);
                }
            }
            return Optional.empty();
        }

        /**
         * Gets the array of roots for this file system.
         * @return an array of roots for this file system.
         */
        public String[] roots() {
            return data.roots.toArray(new String[0]);
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
            if (StringUtils.isBlank(pattern) || pattern.trim().equals(".")) {
                return "";
            }
            String adjustedPattern = dirSeparator().equals("/") ? pattern.replace("\\", "/") : pattern.replace("/", "\\");
            if (adjustedPattern.trim().equals(dirSeparator())) {
                return adjustedPattern;
            }
            List<String> parts = new ArrayList<>(Arrays.asList(tokenize(adjustedPattern)));
            int i = 0;
            while (i < parts.size()) {
                String part = parts.get(i);
                if (part.equals("..")) {
                    if (i == 0) {
                        throw new IllegalStateException("Unable to create path before root");
                    }
                    parts.remove(i);
                    parts.remove(i - 1);
                    i--;
                } else if (part.equals(".")) {
                    parts.remove(i);
                } else {
                    i++;
                }
            }
            if (parts.isEmpty()) {
                throw new IllegalStateException("Unable to create path before root");
            }
            return String.join(dirSeparator(), parts);
        }

        /**
         * Creates a path separated by the directory separator.
         * Starting with an empty string will cause the directory separator to appear at the beginning.
         * @param segments the segments that make up the path.
         * @return the path string.
         */
        public String mkPath(final String... segments) {
            return String.join(dirSeparator(), segments);
        }

        /**
         * Determines if the candidate string starts with a root or directory separator as defined in this
         * FSInfo.
         * @param candidate the candidate string to test.
         * @return {@code true} if the candidate starts with a root or a directory separator.
         */
        public boolean startsWithRootOrSeparator(final String candidate) {
            if (candidate == null) {
                return false;
            }
            String target = candidate.trim();
            if (StringUtils.isBlank(target)) {
                return false;
            }
            for (String root : roots()) {
                if (target.startsWith(root)) {
                    return true;
                }
            }
            return target.startsWith(dirSeparator());
        }

        private int compareData(final DocumentName.FSInfoData otherData) {
            int result = Boolean.compare(this.data.isCaseSensitive, otherData.isCaseSensitive);
            if (result == 0) {
                result = this.data.separator.compareTo(otherData.separator);
                if (result == 0) {
                    if (new HashSet<>(this.data.roots).containsAll(otherData.roots)) {
                        result = new HashSet<>(otherData.roots).containsAll(this.data.roots) ? 0 : 1;
                    } else {
                        result = -1;
                    }
                }
            }
            return result;
        }

        @Override
        public int compareTo(final FSInfo other) {
            int result = this.name.compareToIgnoreCase(other.name);
            return result == 0 ? compareData(other.data) : result;
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof FSInfo oth && this.compareTo(oth) == 0;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
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
            this.root = "";
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
                if (this.root.isBlank()) {
                    this.root = this.baseName.getRoot();
                }
            }
            if (this.root.isBlank()) {
                this.root = this.fsInfo.roots()[0];
            } else {
                if (!List.of(this.fsInfo.roots()).contains(this.root)) {
                    throw new IllegalArgumentException(String.format("'%s' is not a valid root for %s", this.root, this.fsInfo));
                }
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
                setRoot(pair.getLeft());
            }
            this.name = fsInfo.normalize(pair.getRight());
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
            String workingRoot = fsInfo.rootFor(name).orElse("");
            if (!workingRoot.isEmpty() && workingName.startsWith(workingRoot)) {
                workingName = workingName.substring(workingRoot.length());
            }
            return ImmutablePair.of(workingRoot, workingName);
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

        // only called if basName is not null
        private void verifyBaseName() {
            if (!this.name.startsWith(baseName.name)) {
                this.name = this.name.isEmpty() ? baseName.name :
                        baseName.name + fsInfo.dirSeparator() + this.name;
            }
            if (!this.baseName.getRoot().equals(root)) {
                Builder builder = new Builder(baseName).setRoot(root);
                if (baseName.baseName != null && baseName.baseName != baseName) {
                    builder.setBaseName(baseName.baseName);
                } else {
                    builder.baseName = null;
                    builder.sameNameFlag = true;
                }
                this.baseName = builder.build();
            }

        }

        /**
         * Build a DocumentName from this builder.
         * @return A new DocumentName.
         */
        public DocumentName build() {
            verify();
            if (this.baseName != null) {
                verifyBaseName();
            } else {
                if (this.name.startsWith(root)) {
                    this.name = this.name.substring(root.length());
                }
            }
            return new DocumentName(this);
        }
    }
}
