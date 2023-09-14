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

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.matchers.OrMatcher;
import org.apache.rat.analysis.matchers.SimpleTextMatcher;
import org.apache.rat.license.ILicenseFamily;

/**
 * @since Rat 0.8
 */
public class SimplePatternBasedLicense extends BaseLicense {

    private static IHeaderMatcher getMatcher(String[] patterns) {
        if (patterns.length == 1) {
            return new SimpleTextMatcher(patterns[0]);
        }
        Collection<IHeaderMatcher> collection = Arrays.stream(patterns).map(SimpleTextMatcher::new)
                .collect(Collectors.toList());
        return new OrMatcher(collection);
    }

    /**
     * Creates a pattern based license with full documentation.
     * 
     * @param pLicenseFamilyCategory
     * @param pLicenseFamilyName
     * @param pNotes
     * @param pPatterns
     */
    public SimplePatternBasedLicense(ILicenseFamily licenseFamily, String notes, String[] patterns) {
        this(null, licenseFamily, notes, patterns);
    }

    public SimplePatternBasedLicense(String id, ILicenseFamily licenseFamily, String notes, String[] patterns) {
        super(licenseFamily, notes, getMatcher(patterns));
    }
}
