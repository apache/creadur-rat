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
package org.apache.rat.document.impl;

import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;

import static java.lang.String.format;

/**
 * A PatternMatcher that will emit trace messages during execution if the environment variable is set.
 * Also provides a printable name for general logging.
 */
public class TraceableDocumentNameMatcher implements DocumentNameMatcher {
    /** The envrionment variable to set to enable tracing for the PatternMatcher execution */
    public static final String ENV_VAR = TraceableDocumentNameMatcher.class.getName();
    /** The delegate that will do all the work */
    private final DocumentNameMatcher delegate;
    /** Indicator is set to {@code true} if the environment variable has been set to "true" */
    private static final boolean TRACE_ENABLED = Boolean.parseBoolean(System.getenv(ENV_VAR));
    /** The supplier for the name */
    protected final Supplier<String> name;
    /** A TraceableDocumentNameMatcher that always returns {@code true} */
    public static final TraceableDocumentNameMatcher TRUE = TraceableDocumentNameMatcher.make(() -> "True", pth -> true);
    /** A TraceableDocumentNameMatcher that always returns {@code false} */
    public static final TraceableDocumentNameMatcher FALSE = TraceableDocumentNameMatcher.make(() -> "False", pth -> false);

    /**
     * Creates a TraceableDocumentNameMatcher from a PathMatcher.
     * If the {@code delegate} is already a TraceableDocumentNameMatcher it will be returned unmodified.
     * @param name the name of the traceable path matcher.
     * @param delegate the PathMatcher that will actually do the work.
     * @return the new TraceableDocumentNameMatcher
     */
    public static TraceableDocumentNameMatcher make(final Supplier<String> name, final DocumentNameMatcher delegate) {
        if (delegate instanceof TraceableDocumentNameMatcher) {
            return (TraceableDocumentNameMatcher) delegate;
        }
        return new TraceableDocumentNameMatcher(name, delegate);
    }

    public static TraceableDocumentNameMatcher from(final FileFilter fileFilter) {
        return new TraceableDocumentNameMatcher(fileFilter::toString,
                DocumentNameMatcher.from(fileFilter));
    }

    /**
     * Creates a traceable pattern matcher with the specified name.
     * @param name the name of the pattern matcher.
     * @param delegate the delegate that actually does the work.
     */
    protected TraceableDocumentNameMatcher(final Supplier<String> name, final DocumentNameMatcher delegate) {
        this.name = name;
        this.delegate = delegate;
    }

    /**
     * Performs a logical "not" on a nameMatcher.
     * @param nameMatcher the matcher to negate.
     * @return a PathMatcher that is the negation of the argument.
     */
    public static TraceableDocumentNameMatcher not(final TraceableDocumentNameMatcher nameMatcher) {
        if (nameMatcher == TRUE) {
            return FALSE;
        }
        if (nameMatcher == FALSE) {
            return TRUE;
        }
        return new TraceableDocumentNameMatcher(() -> "not", documentName -> !nameMatcher.matches(documentName)) {
            @Override
            public String toString() {
                return format("not(%s)", nameMatcher);
            }
        };
    }

    /**
     * Performs a logical "OR" across the collection of matchers.
     * @param matchers the matchers to check.
     * @return a matcher that returns TRUE if any of the enclosed matchers returns true.
     */
    public static TraceableDocumentNameMatcher or(final Collection<TraceableDocumentNameMatcher> matchers) {
        if (matchers.isEmpty()) {
            return FALSE;
        }
        if (matchers.size() == 1) {
            return matchers.iterator().next();
        }
        if (matchers.contains(TRUE)) {
            return TRUE;
        }
        return new TraceableDocumentNameMatcher(() -> "or", documentName -> {
            for (DocumentNameMatcher matcher : matchers) {
                if (matcher.matches(documentName)) {
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
     * Performs a logical "OR" across the collection of matchers.
     * @param matchers the matchers to check.
     * @return a matcher that returns TRUE if any of the enclosed matchers returns true.
     */
    public static TraceableDocumentNameMatcher or(final TraceableDocumentNameMatcher... matchers) {
        return or(Arrays.asList(matchers));
    }

    /**
     * Performs a logical "AND" across the collection of matchers.
     * @param matchers the matchers to check.
     * @return a matcher that returns TRUE if all of the enclosed matchers returns true.
     */
    public static TraceableDocumentNameMatcher and(final TraceableDocumentNameMatcher... matchers) {
        if (matchers.length == 0) {
            return FALSE;
        }
        if (matchers.length == 1) {
            return matchers[0];
        }
        if (Arrays.asList(matchers).contains(FALSE)) {
            return FALSE;
        }
        return new TraceableDocumentNameMatcher(() -> "and",
                documentName -> {
                    for (DocumentNameMatcher matcher : matchers) {
                        if (!matcher.matches(documentName)) {
                            return false;
                        }
                    }
                    return true;
                }) {
            @Override
            public String toString() {
                List<String> children = new ArrayList<>();
                Arrays.asList(matchers).forEach(s -> children.add(s.toString()));
                return format("%s(%s)", name.get(), String.join(", ", children));
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name.get());
        if (delegate instanceof TraceableDocumentNameMatcher) {
            sb.append(" ").append(delegate.toString());
        }
        return sb.toString();
    }

    @Override
    public boolean matches(final DocumentName documentName) {
        boolean result = delegate.matches(documentName);
        if (TRACE_ENABLED && DefaultLog.getInstance().isEnabled(Log.Level.DEBUG)) {
            DefaultLog.getInstance().debug(format("{%s} %s %s", documentName, result, this));
        }
        return result;
    }
}
