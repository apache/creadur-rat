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
import java.util.Collection;
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
 *     // create a list of levels that a list of processors at that level.
 *     // will return a custom matcher that from an overridden MatcherSet.customDocumentNameMatchers method
 *     // build LevelMatcher as a system that returns Include, Exclude or no status for each check.
 *     // put the level matcher in an array with other level matchers at the specific level below the root
 *     // When searching start at the lowest level and work up the tree.
 */
public abstract class AbstractFileProcessorBuilder {
    /** A String format pattern to print a regex string */
    protected static final String REGEX_FMT = "%%regex[%s]";

    /** The name of the file being processed */
    protected final String fileName;

    /** The predicate that will return {@code false} for any comment line in the file. */
    protected final Predicate<String> commentFilter;

    private final SortedMap<Integer, LevelBuilder> levelBuilders;

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

    private List<MatcherSet> createMatcherSetList(DocumentName dir) {
        List<Integer> keys = new ArrayList<>(levelBuilders.keySet());
        keys.sort((a, b) -> -1 * Integer.compare(a, b));
        return keys.stream().map( key -> levelBuilders.get(key).asMatcherSet(dir)).collect(Collectors.toList());
    }

    public final List<MatcherSet> build(final DocumentName dir) {
        if (includeProcessorFile) {
            String name = String.format("**/%s", fileName);
            String pattern = ExclusionUtils.localizePattern(dir, name);
            MatcherSet matcherSet = new MatcherSet.Builder()
                    .addExcluded(new DocumentNameMatcher(name, MatchPatterns.from(Collections.singletonList(pattern)), dir))
            .build();
            LevelBuilder levelBuilder = levelBuilders.computeIfAbsent(0, LevelBuilder::new);
            levelBuilder.add(matcherSet);
        }

        checkDirectory(0, dir, new NameFileFilter(fileName));

        List<MatcherSet> result = null;
        if (levelBuilders.size() == 1) {
            result = Collections.singletonList(levelBuilders.get(0).asMatcherSet(dir));
        } else {
            result = createMatcherSetList(dir);
        }
        levelBuilders.clear();
        return result;
    }

    /**
     * Process by reading the file, creating a MatcherSet, and adding it to the
     * matcherSets.
     * @param documentName the file to read.
     */
    protected MatcherSet process(final Consumer<MatcherSet> matcherSetConsumer, final DocumentName dirBasedName, final DocumentName documentName) {
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
        matcherSetBuilder.addExcluded(dirBasedName, excluded);
        matcherSetBuilder.addIncluded(dirBasedName, included);
        return matcherSetBuilder.build();
    }

    /**
     * Process the directory tree looking for files that match the filter. Call {@link #process} on any matching file.
     * @param directory The name of the directory to process.
     * @param fileFilter the filter to detect processable files with.
     */
    private void checkDirectory(final int level, final DocumentName directory, final FileFilter fileFilter) {
        File dirFile = new File(directory.getName());
        for (File f : listFiles(dirFile, fileFilter)) {
            LevelBuilder levelBuilder = levelBuilders.computeIfAbsent(level, LevelBuilder::new);
            DocumentName dirBasedName = DocumentName.builder(f).setBaseName(directory.getBaseName()).build();
            levelBuilder.add(process(levelBuilder::add, dirBasedName, DocumentName.builder(f).setBaseName(directory.getName()).build()));
        }
        for (File dir : listFiles(dirFile, DirectoryFileFilter.DIRECTORY)) {
            checkDirectory(level + 1, DocumentName.builder(dir).setBaseName(directory.getBaseName()).build(), fileFilter);
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

    protected static class FileProcessorPredicate implements Predicate<DocumentName> {
        private final Collection<MatcherSet> matcherSets;
        FileProcessorPredicate(Collection<MatcherSet> matcherSets) {
            this.matcherSets = matcherSets;
        }

        public Collection<MatcherSet> getMatcherSets() {
            return matcherSets;
        }

        @Override
        public boolean test(DocumentName documentName) {
            for (MatcherSet matcherSet : matcherSets) {
                if (matcherSet.includes().orElse(DocumentNameMatcher.MATCHES_NONE).matches(documentName)) {
                    return true;
                }
                if (matcherSet.excludes().orElse(DocumentNameMatcher.MATCHES_NONE).matches(documentName)) {
                    return false;
                }
            }
            return true;
        }
    }

    static class LevelBuilder {
        /**
         * The list of MatcherSets that this builder produced.
         */
        private final MatcherSet.Builder builder;

        public LevelBuilder(Integer integer) {
            int level = integer;
            builder = new MatcherSet.Builder();
        }

        public void add(MatcherSet matcherSet) {
            if (matcherSet.includes().isPresent()) {
                builder.addIncluded(matcherSet.includes().get());
            }
            if (matcherSet.excludes().isPresent()) {
                builder.addExcluded(matcherSet.excludes().get());
            }
        }

        public MatcherSet asMatcherSet(DocumentName dir) {
            return builder.build();
        }
    }
}
