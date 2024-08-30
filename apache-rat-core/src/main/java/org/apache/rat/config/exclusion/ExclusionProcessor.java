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

import org.apache.rat.config.exclusion.plexus.MatchPatterns;
import org.apache.rat.utils.iterator.WrappedIterator;

import java.io.File;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Processes the include and exclude patterns and applies the result against a base directory
 * to return an IReportable that contains all the reportable objects.
 */
public class ExclusionProcessor {
    /** Strings that identify the files/directories to exclude */
    private final Set<String> excludedPatterns = new HashSet<>();
    /** Path matchers that exclude files/directories */
    private final List<PathMatcherSupplier> excludedPaths = new ArrayList<>();
    /** Strings that identify the files/directories to include (overrides exclude) */
    private final Set<String> includedPatterns = new HashSet<>();
    /** Path matchers that identify the  files/directories to include (overrides exclude) */
    private final List<PathMatcherSupplier> includedPaths = new ArrayList<>();
    /** Collections of StandardCollections that have file processors that should be
     * used to add additional exclude files to the process
     */
    private final Set<StandardCollection> fileProcessors = new HashSet<>();
    /** Standard collections that contribute to the inclusion processing */
    private final Set<StandardCollection> includedCollections = new HashSet<>();
    /** Standard collections that contribute to the exclusion procession */
    private final Set<StandardCollection> excludedCollections = new HashSet<>();
    /** the cases sensitive flag for matching -- should be set based on platform */
    private final boolean isCaseSensitive = true;

    /**
     * Add the Iterable of strings to the collection of file/directory patters to ignore.
     * @param patterns the patterns to add
     */
    public void addIncludedPatterns(Iterable<String> patterns) {
        patterns.forEach(includedPatterns::add);
    }

    /**
     * Add a PathMatcherSupplier to the collection of file/directory patters to ignore.
     * @param pathMatcherSupplier the supplier of the pathMatcher to add (may be null).
     */
    public void addIncludedFilter(PathMatcherSupplier pathMatcherSupplier) {
        if (pathMatcherSupplier != null) {
            includedPaths.add(pathMatcherSupplier);
        }
    }

    /**
     * add All StandardCollections to the consumer
     * @param consumer the consumer of StandardCollections.
     */
    private void addAllCollection(Consumer<StandardCollection> consumer) {
        for (StandardCollection standardCollection : StandardCollection.values()) {
            if (standardCollection != StandardCollection.ALL) {
                consumer.accept(standardCollection);
            }
        }
    }

    /**
     * Add the file processor from a StandardCollection.
     * @param collection the collection to add the processor from.
     */
    public void addFileProcessor(StandardCollection collection) {
        if (collection == StandardCollection.ALL) {
            addAllCollection(this::addFileProcessor);
        }
        if (collection.fileProcessor != null) {
            fileProcessors.add(collection);
        }
    }

    /**
     * Add the included files from a StandardCollection.
     * @param collection the standard collection to add the includes from.
     * @return this
     */
    public ExclusionProcessor addIncludedCollection(StandardCollection collection) {
        if (collection == StandardCollection.ALL) {
            addAllCollection(this::addIncludedCollection);
        }
        if (collection != null) {
            includedCollections.add(collection);
            includedPatterns.addAll(collection.patterns);
        }
        return this;
    }

    /**
     * Add the excluded file/directory patterns from an iterable of strings.
     * @param patterns the strings to add to the excluded patterns.
     * @return this
     */
    public ExclusionProcessor addExcludedPatterns(Iterable<String> patterns) {
        patterns.forEach(excludedPatterns::add);
        return this;
    }

    /**
     * Add the excluded file/directory paths from a PathMatcherSupplier of strings.
     * @param pathMatcherSupplier the supplier of the path matcher to add.
     * @return this
     */
    public ExclusionProcessor addExcludedFilter(PathMatcherSupplier pathMatcherSupplier) {
        if (pathMatcherSupplier != null) {
            excludedPaths.add(pathMatcherSupplier);
        }
        return this;
    }

    /**
     * Excludes the files/directories specified by a StandardCollection.
     * @param collection the StandardCollection to that identifies the files to exclude.
     * @return this.
     */
    public ExclusionProcessor addExcludedCollection(StandardCollection collection) {
        if (collection == StandardCollection.ALL) {
            addAllCollection(this::addExcludedCollection);
        }
        if (collection != null) {
            excludedCollections.add(collection);
            excludedPatterns.addAll(collection.patterns);
        }
        return this;
    }

    /**
     * Adds to lists of qualified file names.
     * @param matching the list to put matching file names into.
     * @param notMatching the list to put non-matching files names into.
     * @param basedir the base directory to match against.
     * @param patterns the patterns to match.
     */
    private void appendList(final Set<String> matching, final Set<String> notMatching,
                            final File basedir, final Collection<String> patterns) {
        if (!patterns.isEmpty()) {
            WrappedIterator.create(patterns.iterator()).filter(ExclusionUtils.MATCH_FILTER)
                    .map(s -> ExclusionUtils.localizeFileName(basedir.getPath(), s))
                    .map(ExclusionUtils::normalizePattern).forEach(matching::add);
            WrappedIterator.create(patterns.iterator()).filter(ExclusionUtils.NOT_MATCH_FILTER)
                    .map(s -> ExclusionUtils.localizeFileName(basedir.getPath(), s.substring(1)))
                    .map(ExclusionUtils::normalizePattern).forEach(notMatching::add);
        }
    }

    /**
     * Adds to lists of qualified file names.
     * @param matching the list to put matching file names into.
     * @param notMatching the list to put non-matching files names into.
     * @param basedir the base directory to match against.
     * @param standardCollections the StandardSets providing the strings to match.
     */
    private void appendList(final Set<String> matching, final Set<String> notMatching,
                            final File basedir, final Set<StandardCollection> standardCollections) {
        if (!standardCollections.isEmpty()) {
            for (StandardCollection collection : standardCollections) {
                appendList(matching, notMatching, basedir, collection.patterns);
            }
        }
    }

    /**
     * Creates an iterator over the files to check.
     * @param basedir the base directory to make everything relative to.
     * @return A container of files, which are being checked.
     */
    public PathMatcher getPathMatcher(String basedir) {
        final String dirStr = basedir.replace('/', File.separatorChar).replace('\\', File.separatorChar);
        final String basedirStr = dirStr.endsWith(File.separator) ? dirStr.substring(0, dirStr.length() - 1) : dirStr;
        final File basedirFile = new File(basedirStr);

        final Set<String> incl = new TreeSet<>();
        final Set<String> excl = new TreeSet<>();

        // add the file processors to the excluded paths.
        for (StandardCollection sc : fileProcessors) {
            if (sc.hasFileProcessor()) {
                Collection<String> patterns = sc.fileProcessor.apply(basedirStr);
                if (!patterns.isEmpty()) {
                    WrappedIterator.create(patterns.iterator()).filter(ExclusionUtils.MATCH_FILTER)
                            .map(s -> ExclusionUtils.localizeFileName(basedirStr, s))
                            .map(ExclusionUtils::normalizePattern).forEach(excl::add);
                    WrappedIterator.create(patterns.iterator()).filter(ExclusionUtils.NOT_MATCH_FILTER)
                            .map(s -> ExclusionUtils.localizeFileName(basedirStr, s.substring(1)))
                            .map(ExclusionUtils::normalizePattern).forEach(incl::add);
                }
            }
        }

        appendList(incl, excl, basedirFile, includedPatterns);
        appendList(incl, excl, basedirFile, includedCollections);

        appendList(excl, incl, basedirFile, excludedPatterns);
        appendList(excl, incl, basedirFile, excludedCollections);

        List<TracablePathMatcher> inclMatchers = WrappedIterator.create(includedCollections.iterator())
                .filter(StandardCollection::hasPathMatchSupplier)
                .map(s -> TracablePathMatcher.make(() -> "Path match "+s.name(), s.pathMatcherSupplier.get(basedirStr)))
                .toList();

        List<TracablePathMatcher> exclMatchers = WrappedIterator.create(excludedCollections.iterator())
                .filter(StandardCollection::hasPathMatchSupplier)
                .map(s -> TracablePathMatcher.make(() -> "Path match "+s.name(), s.pathMatcherSupplier.get(basedirStr)))
                .toList();

        if (!incl.isEmpty()) {
            inclMatchers.add(makeMatcher(() -> "included patterns", MatchPatterns.from(incl),
                    isCaseSensitive, basedirStr));
        }
        if (!excl.isEmpty()) {
            exclMatchers.add(makeMatcher(() -> "excluded patterns", MatchPatterns.from(excl),
                    isCaseSensitive, basedirStr));
        }

        if (!includedPaths.isEmpty()) {
            for (PathMatcherSupplier supplier : includedPaths) {
                inclMatchers.add(TracablePathMatcher.make(supplier::toString, supplier.get(basedirStr)));
            }
        }
        if (!excludedPaths.isEmpty()) {
            for (PathMatcherSupplier supplier : excludedPaths) {
                exclMatchers.add(TracablePathMatcher.make(supplier::toString, supplier.get(basedirStr)));
            }
        }

        return exclMatchers.isEmpty() ? TracablePathMatcher.TRUE :
                inclMatchers.isEmpty() ? not(or(exclMatchers)) : not(and(or(exclMatchers), not(or(inclMatchers))));
    }


    /**
     * Creates a TracablePathMatcher
     * @param name the name of the matcher.
     * @param patterns the patterns in the matcher
     * @param isCaseSensitive true if match is case sensitive
     * @param basedirStr the base directory for the scanning.
     * @return The matcher
     */
    TracablePathMatcher makeMatcher(Supplier<String> name, MatchPatterns patterns, boolean isCaseSensitive, String basedirStr) {
        return TracablePathMatcher.make(name, p -> {
            String part = p.toString();
            if (part.startsWith(basedirStr)) {
                part = part.substring(basedirStr.length());
                if (part.startsWith(File.separator)) {
                    part = part.substring(1);
                }
            }
            return patterns.matches(part, isCaseSensitive);
        });
    }

    TracablePathMatcher not(TracablePathMatcher pathMatcher) {
        if (pathMatcher == TracablePathMatcher.TRUE) {
            return TracablePathMatcher.FALSE;
        }
        if (pathMatcher == TracablePathMatcher.FALSE) {
            return TracablePathMatcher.TRUE;
        }
        return new TracablePathMatcher(() -> "not", p -> !pathMatcher.matches(p)) {
            @Override
            public String toString() {
                return "not " + pathMatcher.toString();
            }
        };
    }

    TracablePathMatcher or(Collection<TracablePathMatcher> matchers) {
        if (matchers.isEmpty()) {
            return TracablePathMatcher.FALSE;
        }
        if (matchers.size() == 1) {
            return matchers.iterator().next();
        }
        if (matchers.contains(TracablePathMatcher.TRUE)) {
            return TracablePathMatcher.TRUE;
        }
        return new TracablePathMatcher(() -> "or", pth -> {
            for (PathMatcher matcher : matchers) {
                if (matcher.matches(pth)) {
                    return true;
                }
            }
            return false;
        }) {
            @Override
            public String toString() {
                List<String> children = new ArrayList<>();
                matchers.forEach(s -> children.add(s.toString()));
                return String.format("%s(%s)", name.get(), String.join(", ", children));
            }
        };
    }

    TracablePathMatcher and(TracablePathMatcher... matchers) {
        if (matchers.length == 0) {
            return TracablePathMatcher.FALSE;
        }
        if (matchers.length == 1) {
            return matchers[0];
        }
        if (Arrays.asList(matchers).contains(TracablePathMatcher.FALSE)) {
            return TracablePathMatcher.FALSE;
        }
        return new TracablePathMatcher(() -> "and",
                pth -> {
                    for (PathMatcher matcher : matchers) {
                        if (!matcher.matches(pth)) {
                            return false;
                        }
                    }
                    return true;
                }) {
            @Override
            public String toString() {
                List<String> children = new ArrayList<>();
                Arrays.asList(matchers).forEach(s -> children.add(s.toString()));
                return String.format("%s(%s)", name, String.join(", ", children));
            }
        };
    }
}
