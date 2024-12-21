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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.rat.config.exclusion.ExclusionUtils;
import org.apache.rat.config.exclusion.MatcherSet;
import org.apache.rat.config.exclusion.plexus.MatchPatterns;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;

import static org.apache.rat.config.exclusion.ExclusionUtils.NEGATION_PREFIX;

/**
 * Processes the .gitignore file.
 * @see <a href='https://git-scm.com/docs/gitignore'>.gitignore documentation</a>
 */
public class GitIgnoreBuilder extends MatcherSet.Builder {
    // create a list of levels that a list of processors at that level.
    // will return a custom matcher that from an overridden MatcherSet.customDocumentNameMatchers method
    // build LevelMatcher as a system that returns Include, Exclude or no status for each check.
    // put the level matcher in an array with other level matchers at the specific level below the root
    // When searching start at the lowest level and work up the tree.

    private static final String IGNORE_FILE = ".gitignore";
    private static final String COMMENT_PREFIX = "#";
    private static final String ESCAPED_COMMENT = "\\#";
    private static final String ESCAPED_NEGATION = "\\!";
    private static final String SLASH = "/";

    private SortedMap<Integer, LevelBuilder> levelBuilders = new TreeMap<>();


    /**
     * Constructs a file processor that processes a .gitignore file and ignores any lines starting with "#".
     */
    public GitIgnoreBuilder() {
        super(IGNORE_FILE, COMMENT_PREFIX);
    }

    /**
     * Process the directory tree looking for files that match the filter. Process any matching file and return
     * a list of fully qualified patterns.
     * @param directory The name of the directory to process.
     * @param fileFilter the filter to detect processable files with.
     * @return the list of fully qualified file patterns.
     */
    protected void checkDirectory(final DocumentName directory, final FileFilter fileFilter) {
        checkDirectory(0, directory, fileFilter);
        List<Integer> keys = new ArrayList<>(levelBuilders.keySet());
        keys.sort( (a, b) -> -1 * a.compareTo(b));
        for (int level : keys) {
            LevelBuilder levelBuilder  = levelBuilders.get(level);
            MatcherSet fileProcessor = levelBuilder.build(directory);
            fileProcessor.excludes().ifPresent(this::addExcluded);
            fileProcessor.includes().ifPresent(this::addIncluded);
        }
    }

    private void checkDirectory(final int level, final DocumentName directory, final FileFilter fileFilter) {
        File dirFile = new File(directory.getName());
        for (File f : listFiles(dirFile, fileFilter)) {
            LevelBuilder levelBuilder = levelBuilders.computeIfAbsent(level, LevelBuilder::new);
            DocumentName dirBasedName = DocumentName.builder(f).setBaseName(directory.getBaseName()).build();
            levelBuilder.process(dirBasedName, DocumentName.builder(f).setBaseName(directory.getName()).build());
        }
        for (File dir : listFiles(dirFile, DirectoryFileFilter.DIRECTORY)) {
            checkDirectory(level + 1, DocumentName.builder(dir).setBaseName(directory.getBaseName()).build(), fileFilter);
        }
    }
    /**
     * package private for testing.
     * @return the included DocumentNameMatcher.
     */
    DocumentNameMatcher getIncluded() {
        return included;
    }

    /**
     * package private for testing.
     * @return the excluded DocumentNameMatcher.
     */
    DocumentNameMatcher getExcluded() {
        return excluded;
    }

    @Override
    public String modifyEntry(final DocumentName documentName, final String entry) {
        return modifyEntry(documentName, entry, this::addIncluded, this::addExcluded);
    }

    private static String modifyEntry(final DocumentName documentName, final String entry, Consumer<DocumentNameMatcher> include,
                                      Consumer<DocumentNameMatcher> exclude) {
        // An optional prefix "!" which negates the pattern;
        boolean prefix = entry.startsWith(NEGATION_PREFIX);
        String pattern = prefix || entry.startsWith(ESCAPED_COMMENT) || entry.startsWith(ESCAPED_NEGATION) ?
                entry.substring(1) : entry;

        // If there is a separator at the beginning or middle (or both) of the pattern, then
        // the pattern is relative to the directory level of the particular .gitignore file itself.
        // Otherwise, the pattern may also match at any level below the .gitignore level.
        int slashPos = pattern.indexOf(SLASH);
        // no slash or at end already
        if (slashPos == -1 || slashPos == pattern.length() - 1) {
            pattern = "**/" + pattern;
        }
        if (slashPos == 0) {
            pattern = pattern.substring(1);
        }
        // If there is a separator at the end of the pattern then the pattern will only match directories,
        // otherwise the pattern can match both files and directories.
        if (pattern.endsWith(SLASH)) {
            pattern = pattern.substring(0, pattern.length() - 1);
            String name = prefix ? NEGATION_PREFIX + pattern : pattern;
            DocumentName matcherPattern = DocumentName.builder(documentName).setName(name.replace(SLASH, documentName.getDirectorySeparator()))
                    .build();
            DocumentNameMatcher matcher = DocumentNameMatcher.and(new DocumentNameMatcher("isDirectory", File::isDirectory),
                    new DocumentNameMatcher(name, MatchPatterns.from(matcherPattern.localized(SLASH))));
            if (prefix) {
                exclude.accept(matcher);
            } else {
                include.accept(matcher);
            }
            return null;
        }
        return prefix ? NEGATION_PREFIX + pattern : pattern;
    }

    private class LevelBuilder extends MatcherSet.Builder {
        LevelBuilder(int level) {
            super(IGNORE_FILE+"-"+level, COMMENT_PREFIX);
        }

        public String modifyEntry(final DocumentName documentName, final String entry) {
            return GitIgnoreBuilder.modifyEntry(documentName, entry, this::addIncluded, this::addExcluded);
        }

        public void process(DocumentName directory, DocumentName documentName) {
            Set<String> included = new HashSet<>();
            Set<String> excluded = new HashSet<>();
            List<String> iterable = new ArrayList<>();
            ExclusionUtils.asIterator(new File(documentName.getName()), commentFilter)
                    .map(entry -> modifyEntry(documentName, entry))
                    .filter(Objects::nonNull)
                    .map(entry -> localizePattern(documentName, entry))
                    .forEachRemaining(iterable::add);
            segregateList(included, excluded, iterable);
            addExcluded(directory, excluded);
            addIncluded(directory, included);

        }
    }
}
