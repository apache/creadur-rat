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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.ConfigurationException;
import org.apache.rat.config.exclusion.plexus.MatchPatterns;
import org.apache.rat.config.exclusion.plexus.SelectorUtils;
import org.apache.rat.document.impl.DocumentName;
import org.apache.rat.document.impl.DocumentNameMatcher;
import org.apache.rat.utils.ExtendedIterator;

import static java.lang.String.format;

/**
 * Utilities for Exclusion processing.
 */
public final class ExclusionUtils {

    /** The list of comment prefixes that are used to filter comment lines.  */
    public static final List<String> COMMENT_PREFIXES = Arrays.asList("#", "##", "//", "/**", "/*");

    /** A predicate that filters out lines that do NOT start with "!" */
    public static final Predicate<String> NOT_MATCH_FILTER = s -> s.startsWith("!");

    /** A predicate that filters out lines that  start with "!" */
    public static final Predicate<String> MATCH_FILTER = s -> !s.startsWith("!");

    private ExclusionUtils() {
        // do not instantiate
    }

    /**
     * Creates predicate that filters out comment and blank lines.  Leading spaces are removed and
     * if the line then starts with a commentPrefix string it is considered a comment and will be removed
     *
     * @param commentPrefixes the list of comment prefixes
     * @return the Predicate that returns false for lines that start with commentPrefixes or are empty.
     */
    public static Predicate<String> commentFilter(final Iterable<String> commentPrefixes) {
        return s -> {
            if (StringUtils.isNotBlank(s)) {
                int i = 1;
                while (StringUtils.isBlank(s.substring(0, i))) {
                    i++;
                }
                String trimmed = i > 0 ? s.substring(i - 1) : s;
                for (String prefix : commentPrefixes) {
                    if (trimmed.startsWith(prefix)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        };
    }

    /**
     * Creates predicate that filters out comment and blank lines.  Leading spaces are removed and
     * if the line then starts with a commentPrefix string it is considered a comment and will be removed
     *
     * @param commentPrefix the prefix string for comments.
     * @return the Predicate that returns false for lines that start with commentPrefixes or are empty.
     */
    public static Predicate<String> commentFilter(final String commentPrefix) {
        return s -> {
            if (StringUtils.isNotBlank(s)) {
                int i = 1;
                while (StringUtils.isBlank(s.substring(0, i))) {
                    i++;
                }
                String trimmed = i > 0 ? s.substring(i - 1) : s;
                return !trimmed.startsWith(commentPrefix);
            }
            return false;
        };
    }

    /**
     * Create a path matcher from a FileFilter
     * @param fileFilter the file filter to convert.
     * @return a Path matcher.
     */
    public static PathMatcher fromFileFilter(final FileFilter fileFilter) {
        return path -> fileFilter.accept(path.toFile());
    }

    /**
     * Create a FileFilter from a PathMatcher.
     * @param parent the document name for the parent of the file to be filtered.
     * @param nameMatcher the path matcher to convert
     * @return a FileFilter.
     */
    public static FileFilter asFileFilter(final DocumentName parent, final DocumentNameMatcher nameMatcher) {
        return file -> nameMatcher.matches(new DocumentName(file, parent));
    }

    /**
     * Creates an iterator of Strings from a file of patterns.
     * Removes comment lines.
     * @param patternFile the file to read.
     * @param commentPrefix the prefix string for comments.
     * @return the iterable of Strings from the file.
     */
    public static ExtendedIterator<String> asIterator(final File patternFile, final String commentPrefix) {
        return asIterator(patternFile, ExclusionUtils.commentFilter(commentPrefix));
    }

    /**
     * Creates an iterator of Strings from a file of patterns.
     * Removes comment lines.
     * @param patternFile the file to read.
     * @param commentFilters A predicate return true for non-comment lines
     * @return the iterable of Strings from the file.
     */
    public static ExtendedIterator<String> asIterator(final File patternFile, final Predicate<String> commentFilters) {
        verifyFile(patternFile);
        Objects.requireNonNull(commentFilters, "commentFilters");
        try {
            return ExtendedIterator.create(IOUtils.lineIterator(new FileReader(patternFile))).filter(commentFilters);
        } catch (FileNotFoundException e) {
            throw new ConfigurationException(format("%s is not a valid file.", patternFile));
        }
    }

    /**
     * Creates an iterable of Strings from a file of patterns.
     * Removes comment lines.
     * @param patternFile the file to read.
     * @param commentPrefix the prefix string for comments.
     * @return the iterable of Strings from the file.
     */
    public static Iterable<String> asIterable(final File patternFile, final String commentPrefix)  {
        return asIterable(patternFile, commentFilter(commentPrefix));
    }

    /**
     * Creates an iterable of Strings from a file of patterns.
     * Removes comment lines.
     * @param patternFile the file to read.
     * @param commentFilters A predicate returning true for non-comment lines
     * @return the iterable of Strings from the file.
     */
    public static Iterable<String> asIterable(final File patternFile, final Predicate<String> commentFilters)  {
        verifyFile(patternFile);
        Objects.requireNonNull(commentFilters, "commentFilters");
        // can not return LineIterator directly as the patternFile will not be closed leading
        // to a resource leak in some cases.
        try (FileReader reader = new FileReader(patternFile)) {
            List<String> result = new ArrayList<>();
            Iterator<String> iter = new LineIterator(reader) {
                @Override
                protected boolean isValidLine(final String line) {
                    return commentFilters.test(line);
                }
            };
            iter.forEachRemaining(result::add);
            return result;
        } catch (IOException e) {
            throw new ConfigurationException("Unable to read file " + patternFile, e);
        }
    }

    /**
     * Returns {@code true} if the file name represents a hidden file
     * @param f the file to check.
     * @return true if it is the name of a hidden file.
     */
    public static boolean isHidden(final File f) {
        String s = f.getName();
        return s.startsWith(".") && !(s.equals(".") || s.equals(".."));
    }

    private static void verifyFile(final File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new ConfigurationException(format("%s is not a valid file.", file));
        }
    }

    /**
     * Normalize a match pattern to the standard that MatchPattern expects
     * @param pattern the raw pattern text.
     * @return the pattern text for MatchPattern.
     */
    public static String normalizePattern(final String pattern) {
        String cleanPattern = pattern.trim();

        if (cleanPattern.startsWith(SelectorUtils.REGEX_HANDLER_PREFIX)) {
            if (File.separatorChar == '\\') {
                cleanPattern = StringUtils.replace(cleanPattern, "/", "\\\\");
            } else {
                cleanPattern = StringUtils.replace(cleanPattern, "\\\\", "/");
            }
        } else {
            cleanPattern = cleanPattern.replace(File.separatorChar == '/' ? '\\' : '/', File.separatorChar);

            if (cleanPattern.endsWith(File.separator)) {
                cleanPattern += "**";
            }
        }
        return cleanPattern;
    }

    /**
     * Create matching patterns from the collection of strings
     * @param iterable the collection of strings.
     * @return The match patterns from the strings.
     */
    public static Optional<MatchPatterns> matchPattern(final Iterable<String> iterable) {
        return iterable == null ? Optional.empty() : matchPattern(iterable.iterator());
    }

    /**
     * Create matching patterns from the iterator of strings
     * @param iter the iterator of strings.
     * @return The match patterns from the strings.
     */
    public static Optional<MatchPatterns> matchPattern(final Iterator<String> iter) {
        ExtendedIterator<String> eIter = ExtendedIterator.create(iter).filter(MATCH_FILTER)
                .map(ExclusionUtils::normalizePattern);
        return iter.hasNext() ?  Optional.of(MatchPatterns.from(() -> eIter))
        : Optional.empty();
    }

    /**
     * Create not matching patterns from the collection of strings
     * These are strings that start with "!".
     * @param iterable the collection of strings.
     * @return The match patterns from the strings.
     */
    public static Optional<MatchPatterns> notMatchPattern(final Iterable<String> iterable) {
        return iterable == null ? Optional.empty() : notMatchPattern(iterable.iterator());
    }

    /**
     * Create not matching patterns from the iterator of strings
     * These are strings that start with "!".
     * @param iter the collection of strings.
     * @return The match patterns from the strings.
     */
    public static Optional<MatchPatterns> notMatchPattern(final Iterator<String> iter) {
        ExtendedIterator<String> eIter =  ExtendedIterator.create(iter).filter(NOT_MATCH_FILTER)
                .map(s -> normalizePattern(s.substring(1)));
        return eIter.hasNext() ? Optional.of(MatchPatterns.from(() -> eIter))
                : Optional.empty();
    }

    /**
     * @param filename file name to cleanup.
     * @return
     */
    public static String localizeFileName(final String filename) {
        String part = filename;
        boolean regexHandlerPrefix = part.startsWith(SelectorUtils.REGEX_HANDLER_PREFIX);
        boolean antHandlerPrefix = part.startsWith(SelectorUtils.REGEX_HANDLER_PREFIX);

        if (regexHandlerPrefix) {
            part = part.substring(SelectorUtils.REGEX_HANDLER_PREFIX.length(),
                    part.lastIndexOf(SelectorUtils.PATTERN_HANDLER_SUFFIX));
        } else if (antHandlerPrefix) {
            part = part.substring(SelectorUtils.ANT_HANDLER_PREFIX.length(),
                    part.lastIndexOf(SelectorUtils.PATTERN_HANDLER_SUFFIX));
        }

        if (part.startsWith(File.separator)) {
            if (File.separator.equals("\\")) {
                // remove an escaped backslash. Otherwise leave it alone.
                if (part.length() > 1 && part.charAt(1) == File.separatorChar) {
                        // an escaped backslash so remove both.
                        part = part.substring(2);
                }
            } else {
                // normal file separator -- remove it.
                part = part.substring(1);
            }
        }

        if (regexHandlerPrefix || antHandlerPrefix) {
            part = format("%s%s%s",
                    regexHandlerPrefix ? SelectorUtils.REGEX_HANDLER_PREFIX : SelectorUtils.ANT_HANDLER_PREFIX,
                    part, SelectorUtils.PATTERN_HANDLER_SUFFIX);
        }
        return part;
    }
}
