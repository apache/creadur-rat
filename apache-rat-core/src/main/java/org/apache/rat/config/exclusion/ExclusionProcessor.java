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

import java.io.File;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import org.apache.rat.config.exclusion.plexus.MatchPatterns;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.iterator.WrappedIterator;

import static java.lang.String.format;

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

    /** the last generated PathMatcher */
    private PathMatcher lastMatcher;
    /** The base dir for the last PathMatcher */
    private String lastMatcherBaseDir;

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
     * @param pathMatcherSupplier the supplier of the pathMatcher to add (may be null).
     * @return this
     */
    public ExclusionProcessor addIncludedFilter(final PathMatcherSupplier pathMatcherSupplier) {
        if (pathMatcherSupplier != null) {
            includedPaths.add(pathMatcherSupplier);
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
        if (collection != null && collection.hasFileProcessor()) {
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
     * @param pathMatcherSupplier the supplier of the path matcher to add.
     * @return this
     */
    public ExclusionProcessor addExcludedFilter(final PathMatcherSupplier pathMatcherSupplier) {
        if (pathMatcherSupplier != null) {
            excludedPaths.add(pathMatcherSupplier);
            resetLastMatcher();
        }
        return this;
    }

    /**
     * Excludes the files/directories specified by a StandardCollection.
     * @param collection the StandardCollection to that identifies the files to exclude.
     * @return this.
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
                appendList(matching, notMatching, basedir, collection.patterns());
            }
        }
    }

    /**
     * Creates an iterator over the files to check.
     * @param basedir the base directory to make everything relative to.
     * @return A container of files, which are being checked.
     */
    public PathMatcher getPathMatcher(final String basedir) {
        final String dirStr = basedir.replace('/', File.separatorChar).replace('\\', File.separatorChar);
        final String basedirStr = dirStr.endsWith(File.separator) ? dirStr.substring(0, dirStr.length() - 1) : dirStr;

        if (!basedirStr.equals(lastMatcherBaseDir) || lastMatcher == null) {
            lastMatcherBaseDir = basedirStr;

            final File basedirFile = new File(basedirStr);

            final Set<String> incl = new TreeSet<>();
            final Set<String> excl = new TreeSet<>();

            // add the file processors to the excluded paths.
            for (StandardCollection sc : fileProcessors) {
                if (sc.hasFileProcessor()) {
                    Collection<String> patterns = sc.fileProcessor().apply(basedirStr);
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
                    .map(s -> TracablePathMatcher.make(() -> "Path match " + s.name(), s.pathMatcherSupplier().get(basedirStr)))
                    .toList();

            List<TracablePathMatcher> exclMatchers = WrappedIterator.create(excludedCollections.iterator())
                    .filter(StandardCollection::hasPathMatchSupplier)
                    .map(s -> TracablePathMatcher.make(() -> "Path match " + s.name(), s.pathMatcherSupplier().get(basedirStr)))
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
                    TracablePathMatcher pathMatcher = TracablePathMatcher.make(supplier::toString, supplier.get(basedirStr));
                    DefaultLog.getInstance().info(format("Including path matcher %s", pathMatcher.name.get()));
                    inclMatchers.add(pathMatcher);
                }
            }
            if (!excludedPaths.isEmpty()) {
                for (PathMatcherSupplier supplier : excludedPaths) {
                    TracablePathMatcher pathMatcher = TracablePathMatcher.make(supplier::toString, supplier.get(basedirStr));
                    DefaultLog.getInstance().info(format("Excluding path matcher %s", pathMatcher.name.get()));
                    exclMatchers.add(pathMatcher);
                }
            }

            lastMatcher = exclMatchers.isEmpty() ? TracablePathMatcher.TRUE :
                    inclMatchers.isEmpty() ? not(or(exclMatchers)) : not(and(or(exclMatchers), not(or(inclMatchers))));
        }
        return lastMatcher;
    }


    /**
     * Creates a TracablePathMatcher
     * @param name the name of the matcher.
     * @param patterns the patterns in the matcher
     * @param isCaseSensitive true if match is case sensitive
     * @param basedirStr the base directory for the scanning.
     * @return The matcher
     */
    TracablePathMatcher makeMatcher(final Supplier<String> name, final MatchPatterns patterns,
                                    final boolean isCaseSensitive, final String basedirStr) {
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

    /**
     * Performs a logical "not" on a pathMatcher.
     * @param pathMatcher the matcher to negate.
     * @return a PathMatcher that is the negation of the argument.
     */
    TracablePathMatcher not(final TracablePathMatcher pathMatcher) {
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

    /**
     * Performs a logical "OR" across the collection of matchers.
     * @param matchers the matchers to check.
     * @return a matcher that returns TRUE if any of the enclosed matchers returns true.
     */
    TracablePathMatcher or(final Collection<TracablePathMatcher> matchers) {
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
                return format("%s(%s)", name.get(), String.join(", ", children));
            }
        };
    }

    /**
     * Performs a logical "AND" across the collection of matchers.
     * @param matchers the matchers to check.
     * @return a matcher that returns TRUE if all of the enclosed matchers returns true.
     */
    TracablePathMatcher and(final TracablePathMatcher... matchers) {
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
                return format("%s(%s)", name, String.join(", ", children));
            }
        };
    }
}
