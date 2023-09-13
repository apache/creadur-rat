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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.rat.analysis.IHeaderMatcher;

/**
 * Defines a matcher for an SPDX tag. SPDX tag is of the format
 * {@code SPDX-License-Identifier: short-name} where {@code short-name} matches
 * the regex pattern [A-Za-z0-9\.\-]+
 * <p>
 * SPDX identifiers are specified by the Software Package Data Exchange(R) also
 * known as SPDX(R) project from the Linux foundation.
 * </p>
 * 
 * @see https://spdx.dev/ids/
 */
public class SPDXMatcherFactory {

    /**
     * Creates IHeaderMatcher instances that are a collection of SPDXMatcher
     * instances. Used in the default matcher construction.
     */
    private static final Map<String, SPDXMatcherFactory.Match> matchers = new HashMap<>();

    public static final SPDXMatcherFactory INSTANCE = new SPDXMatcherFactory();

    private static Pattern groupSelector = Pattern.compile(".*SPDX-License-Identifier:\\s([A-Za-z0-9\\.\\-]+)");

    private String lastLine;
    private Match lastMatch;

    /**
     * Constructor.
     * 
     * @param spdxId A regular expression that matches the @{short-name} of the SPDX
     * Identifier.
     * @param licenseFamilyCategory the RAT license family category for the license.
     * @param licenseFamilyName the RAT license family name for the license.
     */
    private SPDXMatcherFactory() {
        lastLine = null;
    };

    public IHeaderMatcher create(String spdxId) {
        Match matcher = matchers.get(spdxId);
        if (matcher == null) {
            matcher = new Match(spdxId);
            matchers.put(spdxId, matcher);
        }
        return matcher;
    }

    private boolean check(String line, Match caller) {
        if (lastLine == null || !lastLine.equals(line)) {
            Matcher matcher = groupSelector.matcher(line);
            if (matcher.find()) {
                lastMatch = matchers.get(matcher.group(1));
            } else {
                lastMatch = null;
            }
        }
        return caller == lastMatch;
    }

    public class Match extends AbstractHeaderMatcher {
        
        /**
         * Constructor.
         * 
         * @param spdxId A regular expression that matches the @{short-name} of the SPDX
         * Identifier.
         * @param licenseFamilyCategory the RAT license family category for the license.
         * @param licenseFamilyName the RAT license family name for the license.
         * @param notes The notes for this matcher.
         */
        Match(final String spdxId) {
            super("SPDX:" + spdxId);
        }

        @Override
        public void reset() {
        }

        @Override
        public boolean matches(String line) {
            return SPDXMatcherFactory.this.check(line, this);
        }

        @Override
        public String toString() {
            return getId();
        }
    }
}
