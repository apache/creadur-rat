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
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.ConfigurationException;
import org.apache.rat.analysis.IHeaders;
import org.apache.rat.config.parameters.ComponentType;
import org.apache.rat.config.parameters.ConfigComponent;

/**
 * Defines a factory to produce matchers for an SPDX tag. SPDX tag is of the
 * format {@code SPDX-License-Identifier: short-name} where {@code short-name}
 * matches the regex pattern [A-Za-z0-9\.\-]+
 * <p>
 * SPDX identifiers are specified by the Software Package Data Exchange(R) also
 * known as SPDX(R) project from the Linux foundation.
 * </p>
 *
 * @see <a href="https://spdx.dev/ids/">List of Ids at spdx.dev</a>
 */
public final class SPDXMatcherFactory {

    /**
     * The collection of all matchers produced by this factory.
     */
    private static final Map<String, SPDXMatcherFactory.Match> MATCHER_MAP = new HashMap<>();

    /**
     * The instance of this factory.
     */
    public static final SPDXMatcherFactory INSTANCE = new SPDXMatcherFactory();

    /**
     * The text for the group selector.
     */
    static final String LICENSE_IDENTIFIER = "SPDX-License-Identifier:";

    /**
     * The regular expression to locate the SPDX license identifier in the text
     * stream.
     */
    private static final Pattern GROUP_SELECTOR = Pattern.compile(".*" + LICENSE_IDENTIFIER + "\\s([A-Za-z0-9\\.\\-]+)");

    /**
     * The set of SPDX Ids that matched the last text.
     */
    private final Set<String> lastMatch;

    /**
     * Flag to indicate this document has been checked for SPDX tags.
     */
    private boolean checked;

    /**
     * Constructor.
     */
    private SPDXMatcherFactory() {
        lastMatch = new HashSet<>();
    }

    /**
     * Reset the matching for the next document.
     */
    private void reset() {
        lastMatch.clear();
        checked = false;
    }

    /**
     * Creates the SPDX matcher.
     *
     * @param spdxId the SPDX name to match.
     * @return a SPDX matcher.
     */
    public Match create(final String spdxId) {
        if (StringUtils.isBlank(spdxId)) {
            throw new ConfigurationException("'SPDX' type matcher requires a name");
        }
        Match matcher = MATCHER_MAP.get(spdxId);
        if (matcher == null) {
            matcher = new Match(spdxId);
            MATCHER_MAP.put(spdxId, matcher);
        }
        return matcher;
    }

    /**
     * Each matcher calls this method to present the documentText it is working on.
     *
     * @param documentText The documentText the caller is looking at.
     * @param caller the Match that is calling this method.
     * @return true if the caller matches the text.
     */
    private boolean check(final String documentText, final Match caller) {
        /*
        If the documentText has not been seen yet see if we can extract the SPDX id from the documentText.
        If so then see for each match extract and add the name to lastMatch.
        */
        if (!checked) {
            checked = true;
            if (documentText.contains(LICENSE_IDENTIFIER)) {
                Matcher matcher = GROUP_SELECTOR.matcher(documentText);
                while (matcher.find()) {
                    lastMatch.add(matcher.group(1));
                }
            }
        }
        // see if the caller is in the lastMatch.
        return lastMatch.contains(caller.spdxId);
    }

    /**
     * Matches an SPDX identifier.
     */
    @ConfigComponent(type = ComponentType.MATCHER, name = "spdx",
            desc = "A matcher that matches SPDX tags. SPDX tags have the form: \"SPDX-License-Identifier: short-name\", " +
                    "where short-name matches the regex pattern \"[A-Za-z0-9\\.-]+\". " +
                    "The spdx matcher takes the short name as an argument.")
    public class Match extends AbstractHeaderMatcher {
        /**
         * The SPDX identifier.
         */
        @ConfigComponent(type = ComponentType.PARAMETER, name = "name", desc = "The SPDX identifier string")
        private final String spdxId;

        /**
         * Gets the name of this matcher. Same as the SPDX identifier.
         * @return name of this matcher, that equals the SPDX identifier.
         */
        public String getName() {
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
        public boolean matches(final IHeaders headers) {
            return SPDXMatcherFactory.this.check(headers.raw(), this);
        }

        @Override
        public void reset() {
            super.reset();
            SPDXMatcherFactory.this.reset();
        }
    }
}
