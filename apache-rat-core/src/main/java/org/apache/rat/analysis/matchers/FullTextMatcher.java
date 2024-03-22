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
import java.util.Objects;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.config.parameters.Description;

/**
 * Accumulates all letters and numbers contained inside the header and compares
 * it to the full text of a given license (after reducing it to letters and
 * numbers as well).
 *
 * <p> The text comparison is case insensitive but assumes only characters in
 * the US-ASCII charset are being matched. </p>
 */
public class FullTextMatcher extends SimpleTextMatcher {

    // Number of match characters assumed to be present on first line
    private static final int DEFAULT_INITIAL_LINE_LENGTH = 20;

    private final String fullText;

    private final String firstLine;

    private boolean seenFirstLine;

    private final StringBuilder buffer = new StringBuilder();

    /**
     * Constructs the full text matcher with a unique random id and the specified
     * text to match.
     *
     * @param simpleText the text to match
     */
    public FullTextMatcher(String simpleText) {
        this(null, simpleText);
    }

    /**
     * Constructs the full text matcher for the specified text.
     *
     * @param id the id for the matcher
     * @param fullText the text to match
     */
    public FullTextMatcher(String id, String fullText) {
        super(id, fullText);
        int offset = fullText.indexOf('\n');
        if (offset == -1) {
            offset = Math.min(DEFAULT_INITIAL_LINE_LENGTH, fullText.length());
        }
        firstLine = prune(fullText.substring(0, offset)).toLowerCase(Locale.ENGLISH);
        this.fullText = prune(fullText).toLowerCase(Locale.ENGLISH);
        buffer.setLength(0);
        seenFirstLine = false;
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
    public boolean doMatch(String line) {
        final String inputToMatch = prune(line).toLowerCase(Locale.ENGLISH);
        if (seenFirstLine) { // Accumulate more input
            buffer.append(inputToMatch);
        } else {
            int offset = inputToMatch.indexOf(firstLine);
            if (offset >= 0) {
                // we have a match, save the text starting with the match
                buffer.append(inputToMatch.substring(offset));
                seenFirstLine = true;
                // Drop out to check whether full text is matched
            } else {
                // we assume that the first line must appear in a single line
                return false; // no more to do here
            }
        }

        if (buffer.length() >= fullText.length()) { // we have enough data to match
            if (buffer.toString().contains(fullText)) {
                return true;
            }
            // buffer contains first line but does not contain full text
            // It's possible that the buffer contains the first line again
            int offset = buffer.substring(1).indexOf(firstLine);
            if (offset >= 0) { // first line found again
                buffer.delete(0, offset); // reset buffer to the new start
            } else { // buffer does not even contain first line, so cannot be used to match full text
                reset();
            }
        }
        return false;
    }

    @Override
    public void reset() {
        super.reset();
        buffer.setLength(0);
        seenFirstLine = false;
    }
}