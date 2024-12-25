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
package org.apache.rat.config.exclusion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import java.util.stream.Collectors;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.ExtendedIterator;

import static java.lang.String.format;

/**
 * Processes the include and exclude patterns and applies the result against a base directory
 * to return an IReportable that contains all the reportable objects.
 */
public class ExclusionProcessor {
    /** Strings that identify the files/directories to exclude */
    private final Set<String> excludedPatterns;
    /** Path matchers that exclude files/directories */
    private final List<DocumentNameMatcher> excludedPaths;
    /** Strings that identify the files/directories to include (overrides exclude) */
    private final Set<String> includedPatterns;
    /** Path matchers that identify the files/directories to include (overrides exclude) */
    private final List<DocumentNameMatcher> includedPaths;
    /**
     * Collections of StandardCollections that have file processors that should be
     * used to add additional exclude files to the process
     */
    private final Set<StandardCollection> fileProcessors;
    /** Standard collections that contribute to the inclusion processing */
    private final Set<StandardCollection> includedCollections;
    /** Standard collections that contribute to the exclusion procession */
    private final Set<StandardCollection> excludedCollections;
    /** The last generated PathMatcher */
    private DocumentNameMatcher lastMatcher;
    /** The base dir for the last PathMatcher */
    private DocumentName lastMatcherBaseDir;

    /**
     * Constructs the processor.
     */
    public ExclusionProcessor() {
        excludedPatterns = new HashSet<>();
        excludedPaths = new ArrayList<>();
        includedPatterns = new HashSet<>();
        includedPaths = new ArrayList<>();
        fileProcessors = new HashSet<>();
        includedCollections = new HashSet<>();
        excludedCollections = new HashSet<>();
    }

    /** Reset the {@link #lastMatcher} and {@link #lastMatcherBaseDir} to start again */
    private void resetLastMatcher() {
        lastMatcher = null;
        lastMatcherBaseDir = null;
    }

    /**
     * Add the Iterable of strings to the collection of file/directory patters to ignore.
     * @param patterns the patterns to add
     * @return this
     */
    public ExclusionProcessor addIncludedPatterns(final Iterable<String> patterns) {
        DefaultLog.getInstance().info(format("Including patterns: %s", String.join(", ", patterns)));
        patterns.forEach(includedPatterns::add);
        resetLastMatcher();
        return this;
    }

    /**
     * Add a DocumentNameMatcher to the collection of file/directory patterns to ignore.
     * @param matcher the DocumentNameMatcher to add. Will be ignored if {@code null}.
     * @return this
     */
    public ExclusionProcessor addIncludedMatcher(final DocumentNameMatcher matcher) {
        if (matcher != null) {
            includedPaths.add(matcher);
            resetLastMatcher();
        }
        return this;
    }

    /**
     * Add the file processor from a StandardCollection.
     * @param collection the collection to add the processor from.
     * @return this
     */
    public ExclusionProcessor addFileProcessor(final StandardCollection collection) {
        if (collection != null) {
            DefaultLog.getInstance().info(format("Processing exclude file from %s.", collection));
            fileProcessors.add(collection);
            resetLastMatcher();
        }
        return this;
    }

    /**
     * Add the patterns from the StandardCollection as included patterns.
     * @param collection the standard collection to add the includes from.
     * @return this
     */
    public ExclusionProcessor addIncludedCollection(final StandardCollection collection) {
        if (collection != null) {
            DefaultLog.getInstance().info(format("Including %s collection.", collection));
            includedCollections.add(collection);
            resetLastMatcher();
        }
        return this;
    }

    /**
     * Add the patterns from collections of patterns as excluded patterns.
     * @param patterns the strings to that define patterns to be excluded from processing.
     * @return this
     */
    public ExclusionProcessor addExcludedPatterns(final Iterable<String> patterns) {
        DefaultLog.getInstance().info(format("Excluding patterns: %s", String.join(", ", patterns)));
        patterns.forEach(excludedPatterns::add);
        resetLastMatcher();
        return this;
    }

    /**
     * Add the DocumentNameMatcher as an excluded pattern.
     * @param matcher the DocumentNameMatcher to exclude.
     * @return this
     */
    public ExclusionProcessor addExcludedMatcher(final DocumentNameMatcher matcher) {
        if (matcher != null) {
            excludedPaths.add(matcher);
            resetLastMatcher();
        }
        return this;
    }

    /**
     * Excludes the files/directories specified by a StandardCollection.
     * @param collection the StandardCollection that identifies the files to exclude.
     * @return this
     */
    public ExclusionProcessor addExcludedCollection(final StandardCollection collection) {
        if (collection != null) {
            DefaultLog.getInstance().info(format("Excluding %s collection.", collection));
            excludedCollections.add(collection);
            resetLastMatcher();
        }
        return this;
    }

    /**
     * Creates a Document name matcher that will return {@code false} on any
     * document that is excluded.
     * @param basedir the base directory to make everything relative to.
     * @return A DocumentNameMatcher that will return {@code false} for any document that is to be excluded.
     */
    public DocumentNameMatcher getNameMatcher(final DocumentName basedir) {
        // if lastMatcher is not set or the basedir is not the same as the last one then
        // we have to regenerate the matching document. Otherwise we can just return the
        // lastMatcher since there is no change.
        if (lastMatcher == null || !basedir.equals(lastMatcherBaseDir)) {
            lastMatcherBaseDir = basedir;

            // add the file processors
            final List<MatcherSet> matchers = extractFileProcessors(basedir);
            final MatcherSet.Builder fromCommandLine = new MatcherSet.Builder();
            DocumentName.Builder nameBuilder = DocumentName.builder().setBaseName(basedir);
            extractPatterns(nameBuilder, fromCommandLine);
            extractCollectionPatterns(nameBuilder, fromCommandLine);
            extractCollectionMatchers(fromCommandLine);
            extractPaths(fromCommandLine);
            matchers.add(fromCommandLine.build());

            lastMatcher = MatcherSet.merge(matchers).createMatcher();
        }
        return lastMatcher;
    }

    /**
     * Extracts the file processors from {@link #fileProcessors}.
     * @param basedir The directory to base the file processors on.
     * @return a list of MatcherSets that are created for each {@link #fileProcessors} entry.
     */
    private List<MatcherSet> extractFileProcessors(final DocumentName basedir) {
        final List<MatcherSet> fileProcessorList = new ArrayList<>();
        for (StandardCollection sc : fileProcessors) {
            ExtendedIterator<List<MatcherSet>> iter =  sc.fileProcessorBuilder().map(builder -> builder.build(basedir));
            if (iter.hasNext()) {
                iter.forEachRemaining(fileProcessorList::addAll);
            } else {
                DefaultLog.getInstance().debug(String.format("%s does not have a fileProcessor.", sc));
            }
        }
        return fileProcessorList;
    }

    /**
     * Extracts {@link #includedPatterns} and {@link #excludedPatterns} into the specified matcherBuilder.
     * @param nameBuilder The name builder for the pattern.  File names are resolved against the generated name.
     * @param matcherBuilder the MatcherSet.Builder to add the patterns to.
     */
    private void extractPatterns(DocumentName.Builder nameBuilder, MatcherSet.Builder matcherBuilder) {
        DocumentName name = nameBuilder.setName("Patterns").build();
        if (!excludedPatterns.isEmpty()) {
            matcherBuilder.addExcluded(name, excludedPatterns.stream().map(s -> ExclusionUtils.localizePattern(name.getBaseDocumentName(), s)).collect(Collectors.toSet()));
        }
        if (!includedPatterns.isEmpty()) {

            matcherBuilder.addIncluded(name, includedPatterns.stream().map(s -> ExclusionUtils.localizePattern(name.getBaseDocumentName(), s)).collect(Collectors.toSet()));
        }
    }

    /**
     * Extracts {@link #includedCollections} and {@link #excludedCollections} patterns into the specified matcherBuilder.
     * @param nameBuilder the name builder for the pattern names.
     * @param matcherBuilder the MatcherSet.Builder to add the collections to.
     */
    private void extractCollectionPatterns(final DocumentName.Builder nameBuilder, final MatcherSet.Builder matcherBuilder) {
        final Set<String> incl = new TreeSet<>();
        final Set<String> excl = new TreeSet<>();
        for (StandardCollection sc : includedCollections) {
            Set<String> patterns = sc.patterns();
            if (patterns.isEmpty()) {
                DefaultLog.getInstance().debug(String.format("%s does not have a defined collection for inclusion.", sc));
            } else {
                MatcherSet.Builder.segregateList(incl, excl, sc.patterns());
            }
        }
        for (StandardCollection sc : excludedCollections) {
            Set<String> patterns = sc.patterns();
            if (patterns.isEmpty()) {
                DefaultLog.getInstance().info(String.format("%s does not have a defined collection for exclusion.", sc));
            } else {
                MatcherSet.Builder.segregateList(excl, incl, sc.patterns());
            }
        }
        DocumentName name = nameBuilder.setName("Collections").build();
        matcherBuilder
                .addExcluded(name, excl)
                .addIncluded(name, incl);

    }

    /**
     * Extracts {@link #includedCollections} and {@link #excludedCollections} matchers into the specified matcherBuilder.
     * @param matcherBuilder the MatcherSet.Builder to add the collections to.
     */
    private void extractCollectionMatchers(final MatcherSet.Builder matcherBuilder) {
        ExtendedIterator.create(includedCollections.iterator())
                .map(StandardCollection::staticDocumentNameMatcher)
                .filter(Objects::nonNull)
                .forEachRemaining(matcherBuilder::addIncluded);

        ExtendedIterator.create(excludedCollections.iterator())
                .map(StandardCollection::staticDocumentNameMatcher)
                .filter(Objects::nonNull)
                .forEachRemaining(matcherBuilder::addExcluded);

    }

    /**
     * Extracts {@link #includedPaths} and {@link #excludedPaths} patterns into the specified matcherBuilder.
     * @param matcherBuilder the MatcherSet.Builder to add the collections to.
     */
    private void extractPaths(final MatcherSet.Builder matcherBuilder) {
        if (!includedPaths.isEmpty()) {
            for (DocumentNameMatcher matcher : includedPaths) {
                DefaultLog.getInstance().info(format("Including path matcher %s", matcher));
                matcherBuilder.addIncluded(matcher);
            }
        }
        if (!excludedPaths.isEmpty()) {
            for (DocumentNameMatcher matcher : excludedPaths) {
                DefaultLog.getInstance().info(format("Excluding path matcher %s", matcher));
                matcherBuilder.addExcluded(matcher);
            }
        }
    }


}
