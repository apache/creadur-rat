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
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.analysis.util.HeaderMatcherMultiplexer;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;

/**
 * Defines a matcher for an SPDX tag.  SPDX tag is of the format
 * {@code SPDX-License-Identifier: short-name} where {@code short-name} matches the regex pattern
 * [A-Za-z0-9\.\-]+
 * <p>SPDX identifiers are specified by the Software Package Data Exchange(R) also known as SPDX(R) project
 * from the Linux foundation.</p>
 * 
 * @see https://spdx.dev/ids/
 */
public class SPDXMatcher extends BaseLicense implements IHeaderMatcher {

    /**
     * Creates IHeaderMatcher instances that are a collection of SPDXMatcher instances.  Used in the
     * default matcher construction.
     */
    public static class Factory {
        static Map<String, IHeaderMatcher> getDefaultMatchers() {
            Map<String, IHeaderMatcher> matchers = new HashMap<>();
            matchers.put("CDDL-1.0", new SPDXMatcher("CDDL-1.0", MetaData.RAT_LICENSE_FAMILY_CATEGORY_DATUM_CDLL1,
                    MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_CDDL1));
            matchers.put("GPL-1.0-only",
                    new SPDXMatcher("GPL-1.0-only", MetaData.RAT_LICENSE_FAMILY_CATEGORY_DATUM_GPL1,
                            MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_GPL_VERSION_1));
            matchers.put("GPL-2.0-only",
                    new SPDXMatcher("GPL-2.0-only", MetaData.RAT_LICENSE_FAMILY_CATEGORY_DATUM_GPL2,
                            MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_GPL_VERSION_2));
            matchers.put("GPL-3.0-only",
                    new SPDXMatcher("GPL-3.0-only", MetaData.RAT_LICENSE_FAMILY_CATEGORY_DATUM_GPL3,
                            MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_GPL_VERSION_3));
            matchers.put("MIT", new SPDXMatcher("MIT", MetaData.RAT_LICENSE_FAMILY_CATEGORY_DATUM_MIT,
                    MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_MIT));
            matchers.put("Apache-2.0", new SPDXMatcher("Apache-2.0", MetaData.RAT_LICENSE_FAMILY_CATEGORY_DATUM_ASL,
                    MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_APACHE_LICENSE_VERSION_2_0));
            matchers.put("W3C", new SPDXMatcher("W3C", MetaData.RAT_LICENSE_FAMILY_CATEGORY_DATUM_W3CD,
                    MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_W3C_DOCUMENT_COPYRIGHT));
            return matchers;
        }

        public static final IHeaderMatcher getDefault() {
            return new HeaderMatcherMultiplexer(getDefaultMatchers().values());
        }
    }

    private static Pattern groupSelector = Pattern.compile(".*SPDX-License-Identifier:\\s([A-Za-z0-9\\.\\-]+)");
    private Predicate<String> regex;

    /**
     * Constructor.
     * @param spdxId A regular expression that matches the @{short-name} of the SPDX Identifier.
     * @param licenseFamilyCategory the RAT license family category for the license.
     * @param licenseFamilyName the RAT license family name for the license.
     */
    public SPDXMatcher(final String spdxId, final MetaData.Datum licenseFamilyCategory,
            final MetaData.Datum licenseFamilyName) {
        this(spdxId,licenseFamilyCategory, licenseFamilyName, "");
    }

    /**
     * Constructor.
     * @param spdxId A regular expression that matches the @{short-name} of the SPDX Identifier.
     * @param licenseFamilyCategory the RAT license family category for the license.
     * @param licenseFamilyName the RAT license family name for the license.
     * @param notes The notes for this matcher.
     */
    public SPDXMatcher(final String spdxId, final MetaData.Datum licenseFamilyCategory,
            final MetaData.Datum licenseFamilyName, String notes) {
        super(licenseFamilyCategory, licenseFamilyName, notes);
        this.regex = Pattern.compile(spdxId).asPredicate();
    }
    
    @Override
    public void reset() {
    }

    @Override
    public boolean match(Document subject, String line) throws RatHeaderAnalysisException {
        Matcher matcher = groupSelector.matcher(line);
        if (matcher.find()) {
            String name = matcher.group(1);
            if (regex.test(name)) {
                reportOnLicense(subject);
                return true;
            }
        }
        return false;
    }
}
