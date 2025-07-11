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
package org.apache.rat.analysis.matchers;

import java.util.Locale;

import org.apache.rat.analysis.IHeaders;

/**
 * Accumulates all letters and numbers contained inside the header and compares
 * it to the full text of a given license (after reducing it to letters and
 * numbers as well).
 * <p>
 * The text comparison is case-insensitive but assumes only characters in the
 * US-ASCII charset are being matched.
 * </p>
 */
public class FullTextMatcher extends SimpleTextMatcher {
    /**
     * The text that we are searching for. This text has been pruned and converted to lower case.
     */
    private final String prunedText;

    /**
     * Constructs the full text matcher with a unique random id and the specified
     * text to match.
     *
     * @param simpleText the text to match
     */
    public FullTextMatcher(final String simpleText) {
        this(null, simpleText);
    }

    /**
     * Constructs the full text matcher for the specified text.
     *
     * @param id the id for the matcher
     * @param simpleText the text to match
     */
    public FullTextMatcher(final String id, final String simpleText) {
        super(id, simpleText);
        this.prunedText = prune(simpleText).toLowerCase(Locale.ENGLISH);
    }

    /**
     * Removes everything except letter or digit from text.
     *
     * @param text The text to remove extra chars from.
     * @return the pruned text.
     */
    public static String prune(final String text) {
        final int length = text.length();
        final StringBuilder buffer = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char at = text.charAt(i);
            if (Character.isLetterOrDigit(at)) {
                buffer.append(at);
            }
        }
        return buffer.toString();
    }

    @Override
    public boolean matches(final IHeaders headers) {
        if (headers.pruned().length() >= prunedText.length()) { // we have enough data to match
            return headers.pruned().contains(prunedText);
        }
        return false;
    }
}
