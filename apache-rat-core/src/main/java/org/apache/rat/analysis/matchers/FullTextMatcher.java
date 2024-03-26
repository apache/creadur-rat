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
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.config.parameters.DescriptionImpl;

/**
 * Accumulates all letters and numbers contained inside the header and compares
 * it to the full text of a given license (after reducing it to letters and
 * numbers as well).
 *
 * <p> The text comparison is case insensitive but assumes only characters in
 * the US-ASCII charset are being matched. </p>
 */
public class FullTextMatcher extends AbstractSimpleMatcher {

    private final String fullText;

    private Description[] children = { new DescriptionImpl(Type.Text, "", "The text to match", this::getFullText) };

    /**
     * Constructs the full text matcher with a unique random id and the specified
     * text to match.
     *
     * @param fullText the text to match
     */
    public FullTextMatcher(String fullText) {
        this(null, fullText);
    }

    /**
     * Constructs the full text matcher for the specified text.
     *
     * @param id the id for the matcher
     * @param fullText the text to match
     */
    public FullTextMatcher(String id, String fullText) {
        super(id);
        this.fullText = prune(fullText).toLowerCase(Locale.ENGLISH);
    }

    private String getFullText() {
        return fullText;
    }

    /**
     * Removes everything except letter or digit from text.
     *
     * @param text The text to remove extra chars from.
     * @return the pruned text.
     */
    public static String prune(String text) {
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
    public boolean matches(IHeaders headers) {
        if (headers.pruned().length() >= fullText.length()) { // we have enough data to match
            return headers.pruned().contains(fullText);
        }
        return false;
    }
}