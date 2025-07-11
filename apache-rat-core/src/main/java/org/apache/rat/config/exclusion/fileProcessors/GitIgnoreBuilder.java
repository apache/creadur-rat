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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.rat.config.exclusion.ExclusionUtils;
import org.apache.rat.config.exclusion.MatcherSet;
import org.apache.rat.config.exclusion.plexus.MatchPatterns;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;

import static org.apache.rat.config.exclusion.ExclusionUtils.NEGATION_PREFIX;

/**
 * Processes the {@code .gitignore} file.
 * @see <a href='https://git-scm.com/docs/gitignore'>.gitignore documentation</a>
 */
public class GitIgnoreBuilder extends AbstractFileProcessorBuilder {
    /** The name of the file we read from */
    private static final String IGNORE_FILE = ".gitignore";
    /** The comment prefix */
    private static final String COMMENT_PREFIX = "#";
    /** An escaped comment in the .gitignore file.  (Not a comment) */
    private static final String ESCAPED_COMMENT = "\\#";
    /** An escaped negation in the .gitignore file. (Not a negation) */
    private static final String ESCAPED_NEGATION = "\\!";
    /** The slash string */
    private static final String SLASH = "/";

    /**
     * Constructs a file processor that processes a {@code .gitignore} file and ignores any lines starting with {@value #COMMENT_PREFIX}.
     */
    public GitIgnoreBuilder() {
        super(IGNORE_FILE, COMMENT_PREFIX, true);
    }

    private MatcherSet processGlobalIgnore(final Consumer<MatcherSet> matcherSetConsumer, final DocumentName root, final DocumentName globalGitIgnore) {
        final MatcherSet.Builder matcherSetBuilder = new MatcherSet.Builder();
        final List<String> iterable = new ArrayList<>();
        ExclusionUtils.asIterator(globalGitIgnore.asFile(), commentFilter)
                .map(entry -> modifyEntry(matcherSetConsumer, globalGitIgnore, entry).orElse(null))
                .filter(Objects::nonNull)
                .map(entry -> ExclusionUtils.qualifyPattern(root, entry))
                .forEachRemaining(iterable::add);

        Set<String> included = new HashSet<>();
        Set<String> excluded = new HashSet<>();
        MatcherSet.Builder.segregateList(excluded, included, iterable);
        DocumentName displayName = DocumentName.builder(root).setName("global gitignore").build();
        matcherSetBuilder.addExcluded(displayName, excluded);
        matcherSetBuilder.addIncluded(displayName, included);
        return matcherSetBuilder.build();
    }

    @Override
    protected MatcherSet process(final Consumer<MatcherSet> matcherSetConsumer, final DocumentName root, final DocumentName documentName) {
      if (root.equals(documentName.getBaseDocumentName())) {
          Optional<File> globalGitIgnore = globalGitIgnore();
          List<MatcherSet> matcherSets = new ArrayList<MatcherSet>();
          matcherSets.add(super.process(matcherSetConsumer, root, documentName));
          if (globalGitIgnore.isPresent()) {
              LevelBuilder levelBuilder = getLevelBuilder(Integer.MAX_VALUE);
              DocumentName ignore = DocumentName.builder(globalGitIgnore.get()).build();
              matcherSets.add(processGlobalIgnore(levelBuilder::add, root, ignore));
          }
          return MatcherSet.merge(matcherSets);
      } else {
          return super.process(matcherSetConsumer, root, documentName);
      }
    }

    /**
     * Convert the string entry.
     * If the string ends with a slash an {@link DocumentNameMatcher#and} is constructed from a directory check and the file
     * name matcher.  In this case an empty Optional is returned.
     * If the string starts with {@value ExclusionUtils#NEGATION_PREFIX} then the entry is placed in the include list, otherwise
     * the entry is placed in the exclude list and the name of the check returned.
     * @param documentName The name of the document being processed.
     * @param entry The entry from the document
     * @return and Optional containing the name of the matcher.
     */
    @Override
    protected Optional<String> modifyEntry(final Consumer<MatcherSet> matcherSetConsumer, final DocumentName documentName, final String entry) {
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
                    new DocumentNameMatcher(name, MatchPatterns.from(matcherPattern.localized(documentName.getDirectorySeparator()))));

            MatcherSet.Builder builder = new MatcherSet.Builder();
            if (prefix) {
                builder.addIncluded(matcher);
            } else {
                builder.addExcluded(matcher);
            }
            matcherSetConsumer.accept(builder.build());
            return Optional.empty();
        }
        return Optional.of(prefix ? NEGATION_PREFIX + pattern : pattern);
    }

    /**
     * The global gitignore file to process, based on the
     * RAT_NO_GIT_GLOBAL_IGNORE, XDG_CONFIG_HOME, and HOME environment
     * variables.
     */
    protected Optional<File> globalGitIgnore() {
        if (System.getenv("RAT_NO_GIT_GLOBAL_IGNORE") != null) {
            return Optional.empty();
        }

        String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");
        String filename;
        if (xdgConfigHome != null && !xdgConfigHome.isEmpty()) {
            filename = xdgConfigHome + File.separator + "git" + File.separator + "ignore";
        } else {
            String home = System.getenv("HOME");
            if (home == null) {
                home = "";
            }
            filename = home + File.separator + ".config" + File.separator + "git" + File.separator + "ignore";
        }
        File file = new File(filename);
        if (file.exists()) {
            return Optional.of(file);
        } else {
            return Optional.empty();
        }
    }

}
