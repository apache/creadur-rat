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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.ConfigurationException;
import org.apache.rat.config.exclusion.plexus.MatchPattern;
import org.apache.rat.config.exclusion.plexus.SelectorUtils;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.ExtendedIterator;
import org.apache.rat.utils.Log;

import static java.lang.String.format;

/**
 * Utilities for Exclusion processing.
 */
public final class ExclusionUtils {

    /** The list of comment prefixes that are used to filter comment lines.  */
    public static final List<String> COMMENT_PREFIXES = Arrays.asList("#", "##", "//", "/**", "/*");

    /** Prefix used to negate a given pattern. */
    public static final String NEGATION_PREFIX = "!";

    /** A predicate that filters out lines that do NOT start with {@link #NEGATION_PREFIX}. */
    public static final Predicate<String> NOT_MATCH_FILTER = s -> s.startsWith(NEGATION_PREFIX);

    /** A predicate that filters out lines that start with {@link #NEGATION_PREFIX}. */
    public static final Predicate<String> MATCH_FILTER = NOT_MATCH_FILTER.negate();

    private ExclusionUtils() {
        // do not instantiate
    }

    /**
     * Creates predicate that filters out comment and blank lines. Leading spaces are removed and
     * if the line then starts with a commentPrefix string it is considered a comment and will be removed
     *
     * @param commentPrefixes the list of comment prefixes.
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
     * Creates predicate that filters out comment and blank lines. Leading spaces are removed and
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
     * Create a FileFilter from a PathMatcher.
     * @param parent the document name for the parent of the file to be filtered.
     * @param nameMatcher the path matcher to convert.
     * @return a FileFilter.
     */
    public static FileFilter asFileFilter(final DocumentName parent, final DocumentNameMatcher nameMatcher) {
        return file -> {
            DocumentName candidate = DocumentName.builder(file).setBaseName(parent.getBaseName()).build();
            boolean result = nameMatcher.matches(candidate);
            Log log = DefaultLog.getInstance();
            if (log.isEnabled(Log.Level.DEBUG)) {
                log.debug(format("FILTER TEST for %s -> %s", file, result));
                if (!result) {
                    List< DocumentNameMatcher.DecomposeData> data = nameMatcher.decompose(candidate);
                    log.debug("Decomposition for " + candidate);
                    data.forEach(log::debug);
                }
            }
            return result;
        };
    }

    /**
     * Creates an iterator of Strings from a file of patterns.
     * Removes comment lines.
     * @param patternFile the file to read.
     * @param commentFilters A predicate returning {@code true} for non-comment lines.
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
     * @param commentFilters A predicate returning {@code true} for non-comment lines.
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
     * Returns {@code true} if the filename represents a hidden file
     * @param fileName the file to check.
     * @return true if it is the name of a hidden file.
     */
    public static boolean isHidden(final String fileName) {
        return fileName.startsWith(".") && !(fileName.equals(".") || fileName.equals(".."));
    }

    private static void verifyFile(final File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new ConfigurationException(format("%s is not a valid file.", file));
        }
    }

    /**
     * Modifies the {@link MatchPattern} formatted {@code pattern} argument by expanding the pattern and
     * by adjusting the pattern to include the basename from the {@code documentName} argument.
     * @param documentName the name of the file being read.
     * @param pattern the pattern to format.
     * @return the completely formatted pattern
     */
    public static String qualifyPattern(final DocumentName documentName, final String pattern) {
        boolean prefix = pattern.startsWith(NEGATION_PREFIX);
        String workingPattern = prefix ? pattern.substring(1) : pattern;
        String normalizedPattern = SelectorUtils.extractPattern(workingPattern, documentName.getDirectorySeparator());

        StringBuilder sb = new StringBuilder(prefix ? NEGATION_PREFIX : "");
        if (SelectorUtils.isRegexPrefixedPattern(workingPattern)) {
            sb.append(SelectorUtils.REGEX_HANDLER_PREFIX)
                    .append("\\Q").append(documentName.getBaseName())
                    .append(documentName.getDirectorySeparator())
                    .append("\\E").append(normalizedPattern)
                    .append(SelectorUtils.PATTERN_HANDLER_SUFFIX);
        } else {
            sb.append(documentName.getBaseDocumentName().resolve(normalizedPattern).getName());
        }
        return sb.toString();
    }

    /**
     * Tokenizes the string based on the directory separator.
     * @param source the source to tokenize.
     * @param from the directory separator for the source.
     * @param to the directory separator for the result.
     * @return the source string with the separators converted.
     */
    public static String convertSeparator(final String source, final String from, final String to) {
        if (StringUtils.isEmpty(source) || from.equals(to)) {
            return source;
        }
        return String.join(to, source.split("\\Q" + from + "\\E"));
    }
}
