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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.ConfigurationException;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.config.parameters.Component;
import org.apache.rat.config.parameters.ConfigComponent;
import org.apache.rat.config.parameters.Description;

/**
 * Defines a factory to produce matchers for an SPDX tag. SPDX tag is of the
 * format {@code SPDX-License-Identifier: short-name} where {@code short-name}
 * matches the regex pattern [A-Za-z0-9\.\-]+ <p> SPDX identifiers are specified
 * by the Software Package Data Exchange(R) also known as SPDX(R) project from
 * the Linux foundation. </p>
 *
 * @see <a href="https://spdx.dev/ids/">List of Ids at spdx.dev</a>
 */
public class SPDXMatcherFactory {

    /**
     * The collection of all matchers produced by this factory.
     */
    private static final Map<String, SPDXMatcherFactory.Match> matchers = new HashMap<>();

    /**
     * The instance of this factory.
     */
    public static final SPDXMatcherFactory INSTANCE = new SPDXMatcherFactory();

    /**
     * The regular expression to locate the SPDX license identifier in the text
     * stream
     */
    private static Pattern groupSelector = Pattern.compile(".*SPDX-License-Identifier:\\s([A-Za-z0-9\\.\\-]+)");

    /**
     * the last line that this factory scanned.
     */
    private String lastLine;
    /**
     * The last matcer to match the line.
     */
    private Match lastMatch;

    private SPDXMatcherFactory() {
        lastLine = null;
    }

    /**
     * Creates the spdx matcher.
     *
     * @param spdxId the spdx name to match.
     * @return a spdx matcher.
     */
    public Match create(String spdxId) {
        if (StringUtils.isBlank(spdxId)) {
            throw new ConfigurationException("'spdx' type matcher requires a name");
        }
        Match matcher = matchers.get(spdxId);
        if (matcher == null) {
            matcher = new Match(spdxId);
            matchers.put(spdxId, matcher);
        }
        return matcher;
    }

    /**
     * Each matcher calls this method to present the line it is working on.
     *
     * @param line The line the caller is looking at.
     * @param caller the Match that is calling this method.
     * @return true if the caller matches the text.
     */
    private boolean check(String line, Match caller) {
        // if the line has not been seen yet see if we can extract the SPDX id from the
        // line.
        // if so then see if that name has been registered. If so then we have a match
        // and set
        // lastMatch.
        if ((lastLine == null || !lastLine.equals(line)) && line.contains("SPDX-License-Identifier")) {
            Matcher matcher = groupSelector.matcher(line);
            if (matcher.find()) {
                lastMatch = matchers.get(matcher.group(1));
            } else {
                lastMatch = null;
            }
        }
        // see if the caller matches lastMatch.
        return (lastMatch != null) && caller.spdxId.equals(lastMatch.spdxId);
    }

    @ConfigComponent(type=Component.Type.Matcher, name="spdx", desc="Matches SPDX enclosed license identifier.")
    public class Match extends AbstractSimpleMatcher {
        @ConfigComponent(type=Component.Type.Text, name="spdx", desc="The spdx ID string")
        String spdxId;

        public String getSpdx() {
            return spdxId;
        }
        
        /**
         * Constructor.
         *
         * @param spdxId A regular expression that matches the @{short-name} of the SPDX
         * Identifier.
         */
        Match(final String spdxId) {
            super("SPDX:" + spdxId);
            Objects.requireNonNull(spdxId, "SpdxId is required");
            this.spdxId = spdxId;
        }

        @Override
        protected boolean doMatch(String line) {
            return SPDXMatcherFactory.this.check(line, this);
        }

        @Override
        public void reset() {
            super.reset();
            SPDXMatcherFactory.this.lastMatch = null;
        }
    }
}
