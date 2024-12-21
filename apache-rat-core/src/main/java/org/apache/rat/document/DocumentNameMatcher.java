/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */
package org.apache.rat.document;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.rat.ConfigurationException;
import org.apache.rat.config.exclusion.plexus.MatchPattern;
import org.apache.rat.config.exclusion.plexus.MatchPatterns;

import static java.lang.String.format;

/**
 * Matches document names.
 */
public final class DocumentNameMatcher {

    /** The predicate that does the actual matching. */
    private final Predicate<DocumentName> predicate;
    /** The name of this matcher. */
    private final String name;

    private final boolean isCollection;

    /**
     * A matcher that matches all documents.
     */
    public static final DocumentNameMatcher MATCHES_ALL = new DocumentNameMatcher("TRUE", (Predicate<DocumentName>) x -> true);

    /**
     * A matcher that matches no documents.
     */
    public static final DocumentNameMatcher MATCHES_NONE = new DocumentNameMatcher("FALSE", (Predicate<DocumentName>) x -> false);

    /**
     * Constructs a DocumentNameMatcher from a name and a DocumentName predicate.
     * @param name the name for this matcher.
     * @param predicate the predicate to determine matches.
     */
    public DocumentNameMatcher(final String name, final Predicate<DocumentName> predicate) {
        this.name = name;
        this.predicate = predicate;
        this.isCollection = predicate instanceof MatcherPredicate;
    }

    /**
     * Constructs a DocumentNameMatcher from a name and a delegate DocumentNameMatcher.
     * @param name the name for this matcher.
     * @param delegate the delegate to defer to.
     */
    public DocumentNameMatcher(final String name, final DocumentNameMatcher delegate) {
        this(name, delegate::matches);
    }

    /**
     * Constructs a DocumentNameMatcher from a name a MatchPatterns instance and the DocumentName for the base name.
     * @param name the name of this matcher.
     * @param patterns the patterns in the matcher.
     * @param basedir the base directory for the scanning.
     */
    public DocumentNameMatcher(final String name, final MatchPatterns patterns, final DocumentName basedir) {
        this(name, (Predicate<DocumentName>) documentName -> patterns.matches(documentName.getName(),
                MatchPattern.tokenizePathToString(documentName.getName(), basedir.getDirectorySeparator()),
                basedir.isCaseSensitive()));
    }

    /**
     * Constructs a DocumentNameMatcher from a name and a DocumentName predicate.
     * @param name the name of the matcher.
     * @param matchers fully specified matchers.
     */
    public DocumentNameMatcher(final String name, final MatchPatterns matchers) {
        this(name, (Predicate<DocumentName>) documentName -> matchers.matches(documentName.getName(), documentName.isCaseSensitive()));
    }

    /**
     * Creates a DocumentNameMatcher from a File filter.
     * @param name The name of this matcher.
     * @param fileFilter the file filter to execute.
     */
    public DocumentNameMatcher(final String name, final FileFilter fileFilter) {
        this(name, (Predicate<DocumentName>) documentName -> fileFilter.accept(new File(documentName.getName())));
    }

    /**
     * Creates a DocumentNameMatcher from a File filter.
     * @param fileFilter the file filter to execute.
     */
    public DocumentNameMatcher(final FileFilter fileFilter) {
        this(fileFilter.toString(), fileFilter);
    }

    public boolean isCollection() {
        return isCollection;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Performs the match against the DocumentName.
     * @param documentName the document name to check.
     * @return true if the documentName matchers this DocumentNameMatcher.
     */
    public boolean matches(final DocumentName documentName) {
        return predicate.test(documentName);
    }

    /**
     * Performs a logical {@code NOT} on a DocumentNameMatcher.
     * @param nameMatcher the matcher to negate.
     * @return a PathMatcher that is the negation of the argument.
     */
    public static DocumentNameMatcher not(final DocumentNameMatcher nameMatcher) {
        if (nameMatcher == MATCHES_ALL) {
            return MATCHES_NONE;
        }
        if (nameMatcher == MATCHES_NONE) {
            return MATCHES_ALL;
        }

        return new DocumentNameMatcher(format("not(%s)", nameMatcher),
                (Predicate<DocumentName>) documentName -> !nameMatcher.matches(documentName));
    }

    /**
     * Joins a collection of DocumentNameMatchers together to create a list of the names.
     * @param matchers the matchers to extract the names from.
     * @return the String of the concatenation of the names.
     */
    private static String join(final Collection<DocumentNameMatcher> matchers) {
        List<String> children = new ArrayList<>();
        matchers.forEach(s -> children.add(s.toString()));
        return String.join(", ", children);
    }

    private static Optional<DocumentNameMatcher> standardCollectionCheck(Collection<DocumentNameMatcher> matchers, DocumentNameMatcher override) {
        if (matchers.isEmpty()) {
            throw new ConfigurationException("Empty matcher collection");
        }
        if (matchers.size() == 1) {
            return Optional.of(matchers.iterator().next());
        }
        if (matchers.contains(override)) {
            return Optional.of(override);
        }
        return Optional.empty();
    }

    /**
     * Performs a logical {@code OR} across the collection of matchers.
     * @param matchers the matchers to check.
     * @return a matcher that returns {@code true} if any of the enclosed matchers returns {@code true}.
     */
    public static DocumentNameMatcher or(final Collection<DocumentNameMatcher> matchers) {
        Optional<DocumentNameMatcher> opt = standardCollectionCheck(matchers, MATCHES_ALL);
        if (opt.isPresent()) {
            return opt.get();
        }

        Set<DocumentNameMatcher> myList = new HashSet<>();
        for (DocumentNameMatcher matcher : matchers) {
            if (matcher.predicate instanceof MatcherPredicate && ((MatcherPredicate)matcher.predicate).matchValue) {
                // nested "or"
                ((MatcherPredicate)matcher.predicate).matchers.forEach(myList::add);
            } else {
                myList.add(matcher);
            }
        }
        opt = standardCollectionCheck(matchers, MATCHES_ALL);
        return opt.orElseGet(() -> new DocumentNameMatcher(format("or(%s)", join(myList)), new MatcherPredicate(true, myList)));
    }

    /**
     * Performs a logical {@code OR} across the collection of matchers.
     * @param matchers the matchers to check.
     * @return a matcher that returns {@code true} if any of the enclosed matchers returns {@code true}.
     */
    public static DocumentNameMatcher or(final DocumentNameMatcher... matchers) {
        return or(Arrays.asList(matchers));
    }

    /**
     * Performs a logical {@code AND} across the collection of matchers.
     * @param matchers the matchers to check.
     * @return a matcher that returns {@code true} if all the enclosed matchers return {@code true}.
     */
    public static DocumentNameMatcher and(final Collection<DocumentNameMatcher> matchers) {
        Optional<DocumentNameMatcher> opt = standardCollectionCheck(matchers, MATCHES_NONE);
        if (opt.isPresent()) {
            return opt.get();
        }

        Set<DocumentNameMatcher> myList = new HashSet<>();
        for (DocumentNameMatcher matcher : matchers) {
            if (matcher.predicate instanceof MatcherPredicate && !((MatcherPredicate)matcher.predicate).matchValue) {
                // nested "and"
                ((MatcherPredicate)matcher.predicate).matchers.forEach(myList::add);
            } else {
                myList.add(matcher);
            }
        }
        opt = standardCollectionCheck(matchers, MATCHES_NONE);
        return opt.orElseGet(() -> new DocumentNameMatcher(format("and(%s)", join(myList)), new MatcherPredicate(false, myList)));
    }

    /**
     * Performs a logical {@code AND} across the collection of matchers.
     * @param matchers the matchers to check.
     * @return a matcher that returns {@code true} if all the enclosed matchers return {@code true}.
     */
    public static DocumentNameMatcher and(final DocumentNameMatcher... matchers) {
        return and(Arrays.asList(matchers));
    }

    private static class MatcherPredicate implements Predicate<DocumentName> {
        final Iterable<DocumentNameMatcher> matchers;
        final boolean matchValue;

        MatcherPredicate(boolean matchValue, Iterable<DocumentNameMatcher> matchers) {
            this.matchers = matchers;
            this.matchValue = matchValue;
        }

        @Override
        public boolean test(DocumentName documentName) {
            for (DocumentNameMatcher matcher : matchers) {
                if (matcher.matches(documentName)) {
                    return true;
                }
            }
            return !matchValue;
        }
    }
}
