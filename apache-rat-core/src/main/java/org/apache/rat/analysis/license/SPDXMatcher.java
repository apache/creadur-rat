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
package org.apache.rat.analysis.license;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.api.Document;
import org.apache.rat.license.ILicenseFamily;

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
public class SPDXMatcher implements IHeaderMatcher {

    /**
     * Creates IHeaderMatcher instances that are a collection of SPDXMatcher
     * instances. Used in the default matcher construction.
     */
    private static final Map<String, SPDXMatcher.Match> matchers = new HashMap<>();

    public static final SPDXMatcher INSTANCE = new SPDXMatcher();

    private static Pattern groupSelector = Pattern.compile(".*SPDX-License-Identifier:\\s([A-Za-z0-9\\.\\-]+)");

    /**
     * Constructor.
     * 
     * @param spdxId A regular expression that matches the @{short-name} of the SPDX
     * Identifier.
     * @param licenseFamilyCategory the RAT license family category for the license.
     * @param licenseFamilyName the RAT license family name for the license.
     */
    private SPDXMatcher() {
    };

    public BaseLicense register(String idPrefix, final ILicenseFamily licenseFamily, String notes, final String spdxId) {
        Match matcher = new Match(idPrefix, licenseFamily, notes, spdxId);
        matchers.put(spdxId, matcher);
        return matcher;
    }

    @Override
    public String toString() {
        return getId();
    }
    
    @Override
    public String getId() {
        return "SPDXMatcher";
    }

    public boolean isActive() {
        return !matchers.isEmpty();
    }

    @Override
    public void reset() {
    }

    @Override
    public boolean match(Document subject, String line) throws RatHeaderAnalysisException {
        Matcher matcher = groupSelector.matcher(line);
        if (matcher.find()) {
            Match match = matchers.get(matcher.group(1));
            if (match != null) {
                match.reportOnLicense(subject);
                return true;
            }
        }
        return false;
    }

    @Override
    public void reportFamily(Consumer<ILicenseFamily> consumer) {
        matchers.values().forEach(x -> x.reportFamily(consumer));
    }

    @Override
    public void extractMatcher(Consumer<IHeaderMatcher> matchers, Predicate<ILicenseFamily> comparator) {
        SPDXMatcher.matchers.values().forEach(x -> x.extractMatcher(matchers, comparator));
    }

    public class Match extends BaseLicense {
        String spdx;

        /**
         * Constructor.
         * 
         * @param spdxId A regular expression that matches the @{short-name} of the SPDX
         * Identifier.
         * @param licenseFamilyCategory the RAT license family category for the license.
         * @param licenseFamilyName the RAT license family name for the license.
         * @param notes The notes for this matcher.
         */
        Match(String idPrefix, final ILicenseFamily licenseFamily, String notes, final String spdxId ) {
            super(licenseFamily, notes, idPrefix);
            spdx = spdxId;
        }

        @Override
        public void reset() {
        }

        /*
         * This method only gets called during testing and othe cases where individual matchers are executed.
         * In this case we need to call the SPDXMatcher match method.
         */
        @Override
        public boolean match(Document subject, String line) throws RatHeaderAnalysisException {
            return SPDXMatcher.this.match(subject, line);
        }

        @Override
        public String getId() {
            return String.format("%s: %s", SPDXMatcher.this.getId(), spdx);
        }
        
        @Override
        public String toString() {
            return getId();
        }
    }
}
