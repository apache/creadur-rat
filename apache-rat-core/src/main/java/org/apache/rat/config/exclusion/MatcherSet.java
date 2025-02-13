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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.rat.config.exclusion.plexus.MatchPattern;
import org.apache.rat.config.exclusion.plexus.MatchPatterns;
import org.apache.rat.config.exclusion.plexus.SelectorUtils;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;

import static org.apache.rat.document.DocumentNameMatcher.MATCHES_NONE;

/**
 * The file processor reads the file specified in the DocumentName.
 * It must return a list of fully qualified strings for the {@link MatchPattern} to process.
 * It may return either Ant or Regex style strings, or a mixture of both.
 * See {@link SelectorUtils} for a description of the formats.
 * It may also generate custom DocumentNameMatchers which are added to the customMatchers instance variable.
 */
public interface MatcherSet {
    Optional<DocumentNameMatcher> includes();
    Optional<DocumentNameMatcher> excludes();

    default String getDescription() {
        return String.format("MatcherSet: include [%s] exclude [%s]", includes().orElse(MATCHES_NONE), excludes().orElse(MATCHES_NONE));
    }
    /**
     * Creates a DocumentNameMatcher from an iterable of matcher sets.
     * @return A DocumentNameMatcher that processes the matcher sets.
     */
    default DocumentNameMatcher createMatcher() {
        return DocumentNameMatcher.matcherSet(includes().orElse(MATCHES_NONE), excludes().orElse(MATCHES_NONE));
    }

    static MatcherSet merge(List<MatcherSet> matcherSets) {
        Builder builder = new Builder();
        for (MatcherSet matcherSet : matcherSets) {
            matcherSet.includes().ifPresent(builder::addIncluded);
            matcherSet.excludes().ifPresent(builder::addExcluded);
        }
        return builder.build();
    }

    /**
     * A MatcherSet that assumes the files contain the already formatted strings and just need to be
     * localised for the fileName. When {@link #build()} is called the builder is reset to the initial state.
     */
    class Builder {

        /**
         * Adds to lists of qualified file patterns. Non-matching patterns start with a {@link ExclusionUtils#NEGATION_PREFIX}.
         * @param matching the list to put matching file patterns into.
         * @param notMatching the list to put non-matching file patterns into.
         * @param patterns the patterns to match.
         */
        public static void segregateList(final Set<String> matching, final Set<String> notMatching,
                                         final Iterable<String> patterns) {
            patterns.forEach(s -> {
                if (ExclusionUtils.MATCH_FILTER.test(s)) {
                    matching.add(s);
                } else {
                    notMatching.add(s.substring(1));
                }
            });
        }

        /** The DocumentNameMatcher that specifies included files */
        protected DocumentNameMatcher included;
        /** The DocumentNameMatcher that specifies excluded files */
        protected DocumentNameMatcher excluded;

        public Builder() {
        }

        /**
         * Converts a collection names into DocumentNameMatchers that use the {@code fromDocument} directory separator.
         * @param dest the consumer to accept the DocumentNameMatcher.
         * @param nameFormat the format for the matcher names. Requires '%s' for the {@code fromDocument} localised name.
         * @param fromDocument the document that the patterns are associated with.
         * @param names the list of patterns. If empty no action is taken.
         */
        private void processNames(final Consumer<DocumentNameMatcher> dest, final String nameFormat, final DocumentName fromDocument, final Set<String> names) {
            if (!names.isEmpty()) {
                String name = String.format(nameFormat, fromDocument.localized("/").substring(1));
                dest.accept(new DocumentNameMatcher(name, MatchPatterns.from(fromDocument.getDirectorySeparator(), names), fromDocument.getBaseDocumentName()));
            }
        }

        /**
         * Adds included file names from the specified document. File names are resolved relative to the directory
         * of the {@code fromDocument}.
         * @param fromDocument the document the names were read from.
         * @param names the names that were read from the document. Must use the separator specified by {@code fromDocument}.
         * @return this
         */
        public Builder addIncluded(final DocumentName fromDocument, final Set<String> names) {
            processNames(this::addIncluded, "'included %s'", fromDocument, names);
            return this;
        }

        /**
         * Adds excluded file names from the specified document. File names are resolved relative to the directory
         * of the {@code fromDocument}.
         * @param fromDocument the document the names were read from.
         * @param names the names that were read from the document. Must use the separator specified by {@code fromDocument}.
         * @return this
         */
        public Builder addExcluded(final DocumentName fromDocument, final Set<String> names) {
            processNames(this::addExcluded, "'excluded %s'", fromDocument, names);
            return this;
        }

        /**
         * Adds specified DocumentNameMatcher to the included matchers.
         * @param matcher A document name matcher to add to the included set.
         * @return this
         */
        public Builder addIncluded(final DocumentNameMatcher matcher) {
            this.included = this.included == null ? matcher : DocumentNameMatcher.or(this.included, matcher);
            return this;
        }

        /**
         * Adds specified DocumentNameMatcher to the excluded matchers.
         * @param matcher A document name matcher to add to the excluded set.
         * @return this
         */
        public Builder addExcluded(final DocumentNameMatcher matcher) {
            this.excluded = this.excluded == null ? matcher : DocumentNameMatcher.or(this.excluded, matcher);
            return this;
        }

        /**
         * Builds a MatcherSet. When {@code build()} is called the builder is reset to the initial state.
         * @return the MatcherSet based upon the included and excluded matchers.
         */
        public MatcherSet build() {
            MatcherSet result = new MatcherSet() {
                private final DocumentNameMatcher myIncluded = included;
                private final DocumentNameMatcher myExcluded = excluded;

                @Override
                public Optional<DocumentNameMatcher> includes() {
                    return Optional.ofNullable(myIncluded);
                }

                @Override
                public Optional<DocumentNameMatcher> excludes() {
                    return Optional.ofNullable(myExcluded);
                }
            };
            included = null;
            excluded = null;
            return result;
        }
    }
}
