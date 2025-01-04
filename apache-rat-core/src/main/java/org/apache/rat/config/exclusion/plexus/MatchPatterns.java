package org.apache.rat.config.exclusion.plexus;

/*
 *
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.rat.utils.DefaultLog;

@SuppressWarnings({"checkstyle:RegexpSingleLine", "checkstyle:JavadocVariable"})
/**
 * A list of patterns to be matched
 * <p>Based on code from plexus-utils.</p>
 *
 * @author Kristian Rosenvold
 */
public final class MatchPatterns {
    private final MatchPattern[] patterns;

    private MatchPatterns(final MatchPattern[] patterns) {
        this.patterns = patterns;
    }

    @Override
    public String toString() {
        return source();
    }

    public String source() {
        List<String> sources = new ArrayList<>();
        for (MatchPattern pattern : patterns) {
            sources.add(pattern.source());
        }
        return "[" + String.join(", ", sources) + "]";
    }

    public Iterable<MatchPattern> patterns() {
        return Arrays.asList(patterns);
    }

    /**
     * <p>Checks these MatchPatterns against a specified string.</p>
     *
     * <p>Uses far less string tokenization than any of the alternatives.</p>
     *
     * @param name The name to look for
     * @param isCaseSensitive If the comparison is case sensitive
     * @return true if any of the supplied patterns match
     */
    public boolean matches(final String name, final boolean isCaseSensitive) {
        String[] tokenized = MatchPattern.tokenizePathToString(name, File.separator);
        return matches(name, tokenized, isCaseSensitive);
    }

    public boolean matches(final String name, final String[] tokenizedName, final boolean isCaseSensitive) {
        char[][] tokenizedNameChar = new char[tokenizedName.length][];
        for (int i = 0; i < tokenizedName.length; i++) {
            tokenizedNameChar[i] = tokenizedName[i].toCharArray();
        }
        return matches(name, tokenizedNameChar, isCaseSensitive);
    }

    public boolean matches(final String name, final char[][] tokenizedNameChar, final boolean isCaseSensitive) {
        DefaultLog.getInstance().warn(String.format("Matching %s against %s case-sensitivity: %s", name, tokenizedNameChar, isCaseSensitive));
        for (MatchPattern pattern : patterns) {
            if (pattern.matchPath(name, tokenizedNameChar, isCaseSensitive)) {
                return true;
            }
        }
        return false;
    }

    public static MatchPatterns from(final String separator, final String... sources) {
        final int length = sources.length;
        MatchPattern[] result = new MatchPattern[length];
        for (int i = 0; i < length; i++) {
            result[i] = new MatchPattern(sources[i], separator);
        }
        return new MatchPatterns(result);
    }

    public static MatchPatterns from(final String separator, final Iterable<String> strings) {
        return new MatchPatterns(getMatchPatterns(separator, strings));
    }

    private static MatchPattern[] getMatchPatterns(final String separator, final Iterable<String> items) {
        List<MatchPattern> result = new ArrayList<>();
        for (String string : items) {
            result.add(new MatchPattern(string, separator));
        }
        return result.toArray(new MatchPattern[0]);
    }
}
