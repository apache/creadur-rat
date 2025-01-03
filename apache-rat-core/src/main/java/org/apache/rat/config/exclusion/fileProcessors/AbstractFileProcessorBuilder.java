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
package org.apache.rat.config.exclusion.fileProcessors;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.config.exclusion.ExclusionUtils;
import org.apache.rat.config.exclusion.MatcherSet;
import org.apache.rat.config.exclusion.plexus.MatchPattern;
import org.apache.rat.config.exclusion.plexus.MatchPatterns;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;

/**
 * Creates a List of {@link MatcherSet}s that represent the inclusions and exclusions of this file processor.
 * <p>
 *     By default this processor:
 * </p>
 * <ul>
 *     <li>Creates a list of levels that correspond the depth of the directories where the specific include/exclude file is located.
 *     Directory depth is relative to the initially discovered include/exclude file.</li>
 *     <li>A MatcherSet is created for each include/exclude file located, and the MatcherSet is added to the proper level.</li>
 *     <li>During the build:
 *     <ul>
 *         <li>Each level creates a MatcherSet for the level.</li>
 *         <li>The MatcherSet for each level is returned in reverse order (deepest first).  This ensures that most include/exclude
 *         files will be properly handled.</li>
 *     </ul></li>
 *  </ul>
 */
public abstract class AbstractFileProcessorBuilder {
    /** A String format pattern to print a regex string */
    protected static final String REGEX_FMT = "%%regex[%s]";
    /** The name of the file being processed */
    protected final String fileName;
    /** The predicate that will return {@code false} for any comment line in the file. */
    protected final Predicate<String> commentFilter;
    /** the collection of level builders */
    private final SortedMap<Integer, LevelBuilder> levelBuilders;
    /** if {@code true} then the processor file name will be included in the list of files to ignore */
    private final boolean includeProcessorFile;

    /**
     * Constructor for multiple comment prefixes.
     * @param fileName The name of the file being read.
     * @param commentPrefixes the collection of comment prefixes.
     */
    protected AbstractFileProcessorBuilder(final String fileName, final Iterable<String> commentPrefixes, final boolean includeProcessorFile) {
        this(fileName, commentPrefixes == null ? StringUtils::isNotBlank : ExclusionUtils.commentFilter(commentPrefixes), includeProcessorFile);
    }

    /**
     * Constructor for single comment prefix
     * @param fileName The name of the file to process.
     * @param commentPrefix the comment prefix
     */
    protected AbstractFileProcessorBuilder(final String fileName, final String commentPrefix, final boolean includeProcessorFile) {
        this(fileName, commentPrefix == null ? null : Collections.singletonList(commentPrefix), includeProcessorFile);
    }

    /**
     * Constructor for single comment prefix
     * @param fileName The name of the file to process.
     * @param commentFilter the comment prefix filter.
     */
    protected AbstractFileProcessorBuilder(final String fileName, final Predicate<String> commentFilter, final boolean includeProcessorFile) {
        this.levelBuilders = new TreeMap<>();
        this.fileName = fileName;
        this.commentFilter = commentFilter;
        this.includeProcessorFile = includeProcessorFile;
    }

    /**
     * Creates the MatcherSet from each level and returns them in a list in reverse order.
     * @return a list of MatcherSet
     */

    private List<MatcherSet> createMatcherSetList() {
        List<Integer> keys = new ArrayList<>(levelBuilders.keySet());
        keys.sort((a, b) -> -1 * Integer.compare(a, b));
        return keys.stream().map(key -> levelBuilders.get(key).asMatcherSet()).collect(Collectors.toList());
    }

    /**
     * Builder the list of MatcherSet that define the inclusions/exclusions for the file processor.
     * @param root the directory against which name resolution should be made.
     * @return the List of MatcherSet that represent this file processor.
     */
    public final List<MatcherSet> build(final DocumentName root) {
        if (includeProcessorFile) {
            String name = String.format("**/%s", fileName);
            String pattern = ExclusionUtils.localizePattern(root, name);
            MatcherSet matcherSet = new MatcherSet.Builder()
                    .addExcluded(new DocumentNameMatcher(name, MatchPatterns.from("/", Collections.singletonList(pattern)), root))
            .build();
            LevelBuilder levelBuilder = levelBuilders.computeIfAbsent(0, k -> new LevelBuilder());
            levelBuilder.add(matcherSet);
        }

        checkDirectory(0, root, root, new NameFileFilter(fileName));

        List<MatcherSet> result = levelBuilders.size() == 1 ? Collections.singletonList(levelBuilders.get(0).asMatcherSet())
            : createMatcherSetList();
        levelBuilders.clear();
        return result;
    }

    /**
     * Process by reading the file, creating a MatcherSet, and adding it to the
     * matcherSets.
     * @param matcherSetConsumer the consumer to add the custom matcher sets to.
     * @param root The root against which to resolve names.
     * @param documentName the file to read.
     * @return A matcher set based on the strings in the file.
     */
    protected MatcherSet process(final Consumer<MatcherSet> matcherSetConsumer, final DocumentName root, final DocumentName documentName) {
        final MatcherSet.Builder matcherSetBuilder = new MatcherSet.Builder();
        final List<String> iterable = new ArrayList<>();
        ExclusionUtils.asIterator(new File(documentName.getName()), commentFilter)
                .map(entry -> modifyEntry(matcherSetConsumer, documentName, entry).orElse(null))
                .filter(Objects::nonNull)
                .map(entry -> ExclusionUtils.localizePattern(documentName, entry))
                .forEachRemaining(iterable::add);

        Set<String> included = new HashSet<>();
        Set<String> excluded = new HashSet<>();
        MatcherSet.Builder.segregateList(excluded, included, iterable);
        DocumentName displayName = DocumentName.builder(root).setName(documentName.getName()).build();
        matcherSetBuilder.addExcluded(displayName, excluded);
        matcherSetBuilder.addIncluded(displayName, included);
        return matcherSetBuilder.build();
    }

    /**
     * Process the directory tree looking for files that match the filter. Call {@link #process} on any matching file.
     * @param level the level being precessed
     * @param root the directory against which names should be resolved.
     * @param directory The name of the directory to process.
     * @param fileFilter the filter to detect processable files with.
     */
    private void checkDirectory(final int level, final DocumentName root, final DocumentName directory, final FileFilter fileFilter) {
        File dirFile = directory.asFile();
        for (File file : listFiles(dirFile, fileFilter)) {
            LevelBuilder levelBuilder = levelBuilders.computeIfAbsent(level, k -> new LevelBuilder());
            levelBuilder.add(process(levelBuilder::add, root, DocumentName.builder(file).build()));
        }
        for (File dir : listFiles(dirFile, DirectoryFileFilter.DIRECTORY)) {
            checkDirectory(level + 1, root, DocumentName.builder(dir).setBaseName(directory.getBaseName()).build(), fileFilter);
        }
    }

    /**
     * Allows modification of the file entry to match the {@link MatchPattern} format.
     * Default implementation returns the @{code entry} argument.
     * @param documentName the name of the document that the file was read from.
     * @param entry the entry from that document.
     * @return the modified string or an empty Optional to skip the string.
     */
    protected Optional<String> modifyEntry(final Consumer<MatcherSet> matcherSetConsumer, final DocumentName documentName, final String entry) {
        return Optional.of(entry);
    }

    /**
     * Create a list of files by applying the filter to the specified directory.
     * @param dir the directory.
     * @param filter the filter.
     * @return an array of files. May be empty but will not be null.
     */
    protected File[] listFiles(final File dir, final FileFilter filter) {
        File[] result = dir.listFiles(filter);
        return result == null ? new File[0] : result;
    }

    /**
     * Manages the merging of {@link MatcherSet}s for the specified level.
     */
    private static class LevelBuilder {
        /**
         * The list of MatcherSets that this builder produced.
         */
        private final MatcherSet.Builder builder = new MatcherSet.Builder();

        /**
         * Adds a MatcherSet to this level.
         * @param matcherSet the matcher set to add.
         */
        public void add(final MatcherSet matcherSet) {
            matcherSet.includes().ifPresent(builder::addIncluded);
            matcherSet.excludes().ifPresent(builder::addExcluded);
        }

        /**
         * Constructs the MatcherSet for this level.
         * @return the MatcherSet.
         */
        public MatcherSet asMatcherSet() {
            return builder.build();
        }
    }
}
