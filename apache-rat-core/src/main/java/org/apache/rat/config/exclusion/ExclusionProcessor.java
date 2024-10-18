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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import org.apache.rat.config.exclusion.plexus.MatchPattern;
import org.apache.rat.config.exclusion.plexus.MatchPatterns;
import org.apache.rat.document.impl.DocumentName;
import org.apache.rat.document.impl.DocumentNameMatcher;
import org.apache.rat.document.impl.DocumentNameMatcherSupplier;
import org.apache.rat.document.impl.TraceableDocumentNameMatcher;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.ExtendedIterator;

import static java.lang.String.format;

/**
 * Processes the include and exclude patterns and applies the result against a base directory
 * to return an IReportable that contains all the reportable objects.
 */
public class ExclusionProcessor {
    /** Strings that identify the files/directories to exclude */
    private final Set<String> excludedPatterns = new HashSet<>();
    /** Path matchers that exclude files/directories */
    private final List<DocumentNameMatcherSupplier> excludedPaths = new ArrayList<>();
    /** Strings that identify the files/directories to include (overrides exclude) */
    private final Set<String> includedPatterns = new HashSet<>();
    /** Path matchers that identify the files/directories to include (overrides exclude) */
    private final List<DocumentNameMatcherSupplier> includedPaths = new ArrayList<>();
    /**
     * Collections of StandardCollections that have file processors that should be
     * used to add additional exclude files to the process
     */
    private final Set<StandardCollection> fileProcessors = new HashSet<>();
    /** Standard collections that contribute to the inclusion processing */
    private final Set<StandardCollection> includedCollections = new HashSet<>();
    /** Standard collections that contribute to the exclusion procession */
    private final Set<StandardCollection> excludedCollections = new HashSet<>();
    /** The last generated PathMatcher */
    private TraceableDocumentNameMatcher lastMatcher;
    /** The base dir for the last PathMatcher */
    private DocumentName lastMatcherBaseDir;

    /** Reset the pathmatcher to start again */
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
        List<String> lst = new ArrayList<>();
        patterns.forEach(lst::add);
        DefaultLog.getInstance().info(format("Including patterns: %s", String.join(", ", lst)));
        includedPatterns.addAll(lst);
        resetLastMatcher();
        return this;
    }

    /**
     * Add a PathMatcherSupplier to the collection of file/directory patters to ignore.
     * @param matcherSupplier the supplier of the pathMatcher to add (may be null).
     * @return this
     */
    public ExclusionProcessor addIncludedFilter(final DocumentNameMatcherSupplier matcherSupplier) {
        if (matcherSupplier != null) {
            includedPaths.add(matcherSupplier);
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
     * Add the included files from a StandardCollection.
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
     * Add the excluded file/directory patterns from an iterable of strings.
     * @param patterns the strings to add to the excluded patterns.
     * @return this
     */
    public ExclusionProcessor addExcludedPatterns(final Iterable<String> patterns) {
        List<String> lst = new ArrayList<>();
        patterns.forEach(lst::add);
        DefaultLog.getInstance().info(format("Excluding patterns: %s", String.join(", ", lst)));
        excludedPatterns.addAll(lst);
        resetLastMatcher();
        return this;
    }

    /**
     * Add the excluded file/directory paths from a PathMatcherSupplier of strings.
     * @param matcherSupplier the supplier of the path matcher to add.
     * @return this
     */
    public ExclusionProcessor addExcludedFilter(final DocumentNameMatcherSupplier matcherSupplier) {
        if (matcherSupplier != null) {
            excludedPaths.add(matcherSupplier);
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
     * Adds to lists of qualified file names.
     * @param matching the list to put matching file names into.
     * @param notMatching the list to put non-matching files names into.
     * @param patterns the patterns to match.
     */
    private void segregateList(final Set<String> matching, final Set<String> notMatching,
                               final Collection<String> patterns) {
        if (!patterns.isEmpty()) {
            ExtendedIterator.create(patterns.iterator()).filter(ExclusionUtils.MATCH_FILTER).forEach(matching::add);
            ExtendedIterator.create(patterns.iterator()).filter(ExclusionUtils.NOT_MATCH_FILTER)
                    .map(s -> s.substring(1))
                    .forEach(notMatching::add);
        }
    }

    /**
     * Creates a Document name matcher that will return {@code false} on any
     * document that is excluded.
     * @param basedir the base directory to make everything relative to.
     * @return A container of files, which are being checked.
     */
    public DocumentNameMatcher getNameMatcher(final DocumentName basedir) {
        if (lastMatcher == null || !basedir.equals(lastMatcherBaseDir)) {
            lastMatcherBaseDir = basedir;

            final Set<String> incl = new TreeSet<>();
            final Set<String> excl = new TreeSet<>();

            // add the file processors
            for (StandardCollection sc : fileProcessors) {
                sc.fileProcessor().forEach(fp -> segregateList(excl, incl, fp.apply(basedir)));
            }

            // add the standard patterns
            segregateList(incl, excl, FileProcessor.from(includedPatterns).apply(basedir));
            segregateList(excl, incl, FileProcessor.from(excludedPatterns).apply(basedir));

            // add the collection patterns
            for (StandardCollection sc : includedCollections) {
                segregateList(incl, excl, FileProcessor.from(sc.patterns()).apply(basedir));
            }
            for (StandardCollection sc : excludedCollections) {
                segregateList(excl, incl, FileProcessor.from(sc.patterns()).apply(basedir));
            }

            // add the matchers
            List<TraceableDocumentNameMatcher> inclMatchers = ExtendedIterator.create(includedCollections.iterator())
                    .filter(StandardCollection::hasDocumentNameMatchSupplier)
                    .map(s -> TraceableDocumentNameMatcher.make(() -> "Path match " + s.name(), s.documentNameMatcherSupplier().get(basedir)))
                    .toList();

            List<TraceableDocumentNameMatcher> exclMatchers = ExtendedIterator.create(excludedCollections.iterator())
                    .filter(StandardCollection::hasDocumentNameMatchSupplier)
                    .map(s -> TraceableDocumentNameMatcher.make(() -> "Path match " + s.name(), s.documentNameMatcherSupplier().get(basedir)))
                    .toList();

            if (!incl.isEmpty()) {
                inclMatchers.add(makeMatcher(() -> "included patterns", MatchPatterns.from(incl), basedir));
            }
            if (!excl.isEmpty()) {
                exclMatchers.add(makeMatcher(() -> "excluded patterns", MatchPatterns.from(excl), basedir));
            }

            if (!includedPaths.isEmpty()) {
                for (DocumentNameMatcherSupplier supplier : includedPaths) {
                    TraceableDocumentNameMatcher nameMatcher = TraceableDocumentNameMatcher.make(supplier::toString, supplier.get(basedir));
                    DefaultLog.getInstance().info(format("Including path matcher %s", nameMatcher));
                    inclMatchers.add(nameMatcher);
                }
            }
            if (!excludedPaths.isEmpty()) {
                for (DocumentNameMatcherSupplier supplier : excludedPaths) {
                    TraceableDocumentNameMatcher pathMatcher = TraceableDocumentNameMatcher.make(supplier::toString, supplier.get(basedir));
                    DefaultLog.getInstance().info(format("Excluding path matcher %s", pathMatcher));
                    exclMatchers.add(pathMatcher);
                }
            }

            lastMatcher = TraceableDocumentNameMatcher.TRUE;
            if (!exclMatchers.isEmpty()) {
                lastMatcher = TraceableDocumentNameMatcher.not(TraceableDocumentNameMatcher.or(exclMatchers));
                if (!inclMatchers.isEmpty()) {
                    lastMatcher = TraceableDocumentNameMatcher.or(TraceableDocumentNameMatcher.or(inclMatchers), lastMatcher);
                }
            }
        }
        return lastMatcher;
    }

    /**
     * Creates a TraceablePathMatcher
     * @param name the name of the matcher.
     * @param patterns the patterns in the matcher.
     * @param basedir the base directory for the scanning.
     * @return The matcher
     */
    TraceableDocumentNameMatcher makeMatcher(final Supplier<String> name, final MatchPatterns patterns, final DocumentName basedir) {
        return TraceableDocumentNameMatcher.make(name,
                p -> patterns.matches(p.getName(), MatchPattern.tokenizePathToString(p.getName(), basedir.getDirectorySeparator()), basedir.isCaseSensitive()));
    }
}
