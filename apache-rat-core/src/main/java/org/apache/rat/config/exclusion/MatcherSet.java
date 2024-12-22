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
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import java.util.Set;
import java.util.function.Predicate;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.config.exclusion.plexus.MatchPattern;
import org.apache.rat.config.exclusion.plexus.MatchPatterns;
import org.apache.rat.config.exclusion.plexus.SelectorUtils;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;

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

    /**
     * A MatcherSet that assumes the files contain the already formatted strings and just need to be
     * localized for the fileName.
     */
    class Builder {

        /** A String format pattern to print a regex string */
        protected static final String REGEX_FMT = "%%regex[%s]";

        /**
         * Adds to lists of qualified file patterns. Non-matching patterns start with a {@code !}.
         * @param matching the list to put matching file patterns into.
         * @param notMatching the list to put non-matching files patterns into.
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

        /** The name of the file being processed */
        protected final String fileName;

        /** The predicate that will return {@code false} for any comment line in the file. */
        protected final Predicate<String> commentFilter;

        protected DocumentNameMatcher included;

        protected DocumentNameMatcher excluded;

        Builder() {
            fileName = StringUtils.EMPTY;
            commentFilter = StringUtils::isNotBlank;
        }

        /**
         * Constructor.
         * @param fileName name of the file to process
         * @param commentPrefixes a collection of comment prefixes.
         */
        protected Builder(final String fileName, final Iterable<String> commentPrefixes) {
            super();
            this.fileName = fileName;
            // null prefixes = check prefix may not be blank.
            this.commentFilter = commentPrefixes == null ? StringUtils::isNotBlank : ExclusionUtils.commentFilter(commentPrefixes);
        }

        /**
         * Allows modification of the file entry to match the {@link MatchPattern} format.
         * Default implementation returns the @{code entry} argument.
         * @param documentName the name of the document that the file was read from.
         * @param entry the entry from that document.
         * @return the modified string or an empty Optional to skip the string.
         */
        protected Optional<String> modifyEntry(final DocumentName documentName, final String entry) {
            return Optional.of(entry);
        }

        public Builder addIncluded(DocumentName fromDocument, Set<String> names) {
            if (!names.isEmpty()) {
                String name = String.format("'included %s'", fromDocument.localized("/").substring(1));
                addIncluded(new DocumentNameMatcher(name, MatchPatterns.from(names), fromDocument.getBaseDocumentName()));
            }
            return this;
        }

        public Builder addExcluded(DocumentName fromDocument, Set<String> names) {
            if (!names.isEmpty()) {
                String name = String.format("'excluded %s'", fromDocument.localized("/").substring(1));
                addExcluded(new DocumentNameMatcher(name, MatchPatterns.from(names), fromDocument.getBaseDocumentName()));
            }
            return this;
        }

        public Builder addIncluded(DocumentNameMatcher matcher) {
            this.included = this.included == null ? matcher : DocumentNameMatcher.or(this.included, matcher);
            return this;
        }

        public Builder addExcluded(DocumentNameMatcher matcher) {
            this.excluded = this.excluded == null ? matcher : DocumentNameMatcher.or(this.excluded, matcher);
            return this;
        }

        /**
         * Process by reading the file setting the {@link #included} and {@link #excluded} DocumentMatchers.
         * @param documentName the file to read.
         */
         protected void process(final DocumentName documentName) {
             List<String> iterable = new ArrayList<>();
             ExclusionUtils.asIterator(new File(documentName.getName()), commentFilter)
                    .map(entry -> modifyEntry(documentName, entry).orElse(null))
                    .filter(Objects::nonNull)
                    .map(entry -> ExclusionUtils.localizePattern(documentName, entry))
                            .forEachRemaining(iterable::add);
             segregateProcessResult(documentName, iterable);
         }

        /**
         * Moves properly formatted file names includes, excludes into the proper
         * {@link #included} and {@link #excluded} DocumentMatchers.
         * @param documentName the nome of the document being processed.
         * @param iterable the list of properly formatted include and excludes from the input.
         */
        protected void segregateProcessResult(final DocumentName documentName, List<String> iterable) {
             Set<String> included = new HashSet<>();
             Set<String> excluded = new HashSet<>();
             segregateList(included, excluded, iterable);
             addExcluded(documentName, excluded);
             addIncluded(documentName, included);
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
         * Process the directory tree looking for files that match the filter. Call {@link #process} on any matching file.
         * @param directory The name of the directory to process.
         * @param fileFilter the filter to detect processable files with.
         */
        protected void checkDirectory(final DocumentName directory, final FileFilter fileFilter) {
            File dirFile = new File(directory.getName());
            for (File f : listFiles(dirFile, fileFilter)) {
                process(DocumentName.builder(f).setBaseName(directory.getBaseName()).build());
            }
            for (File dir : listFiles(dirFile, DirectoryFileFilter.DIRECTORY)) {
                checkDirectory(DocumentName.builder(dir).build(), fileFilter);
            }
        }

        public MatcherSet build(final DocumentName dir) {
            checkDirectory(dir, new NameFileFilter(fileName));

            MatcherSet result = new MatcherSet() {
                final DocumentNameMatcher myIncluded = included;
                final DocumentNameMatcher myExcluded = excluded;

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
