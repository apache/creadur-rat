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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.Defaults;
import org.apache.rat.analysis.IHeaderMatcher.State;
import org.apache.rat.analysis.matchers.FullTextMatcher;
import org.apache.rat.api.MetaData;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.testhelpers.TestingLicense;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;


/**
 * Test to see if short form license information will be recognized correctly.
 *
 */
abstract public class AbstractLicenseTest {
    private static int NAME = 0;
    private static int TEXT = 1;

    private Defaults defaults;
    protected MetaData data;

    
    protected AbstractLicenseTest() {
    }
    

    @BeforeEach
    public void setup() {
        data = new MetaData();
        defaults = Defaults.builder().build();
    }

    protected ILicense extractCategory(String id) {
        TestingLicense testingLicense = new TestingLicense();
        testingLicense.setId(id);
        ILicense result = LicenseSetFactory.search(testingLicense, defaults.getLicenses(LicenseFilter.all));
        if (result == null) {
            fail("No licenses for id: " + id);
        }
        return result;
    }

    @ParameterizedTest
    @MethodSource("parameterProvider")
    public void testMatchProcessing(String id, String familyPattern, String name, String notes, String[][] targets) throws IOException {
        ILicense license = extractCategory(id);
        String family = ILicenseFamily.makeCategory(familyPattern);
            try {
                for (String[] target : targets) {
                    if (processText(license, target[TEXT])) {
                        data.reportOnLicense(license);
                        assertNotNull(data.get(MetaData.RAT_URL_HEADER_CATEGORY),"No URL HEADER CATEGORY");
                        assertEquals(family,
                                data.get(MetaData.RAT_URL_HEADER_CATEGORY).getValue(), license.toString());
                        assertNotNull(data.get(MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY), "No URL LICENSE FAMILY CATEGORY");
                        assertEquals(family,
                                data.get(MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY).getValue(), license.toString());
                        if (StringUtils.isNotBlank(notes)) {
                            assertNotNull(data.get(MetaData.RAT_URL_HEADER_SAMPLE), "No URL HEADER SAMPLE");
                            assertEquals(FullTextMatcher.prune(notes),
                                    FullTextMatcher.prune(data.get(MetaData.RAT_URL_HEADER_SAMPLE).getValue()), license.toString());
                        } else {
                            assertNull(data.get(MetaData.RAT_URL_HEADER_SAMPLE), "URL HEADER SAMPLE was not null");
                        }
                        assertNotNull(data.get(MetaData.RAT_URL_LICENSE_FAMILY_NAME), "No URL LICENSE FAMILY NAME");
                        assertEquals(name,
                                data.get(MetaData.RAT_URL_LICENSE_FAMILY_NAME).getValue(), license.toString());
                        data.clear();
                    } else {
                        fail(license + " was not matched by " + target[NAME]);
                    }
                    license.reset();
                }
            } finally {
                license.reset();
            }

    }

    private boolean processText(ILicense license, String text) throws IOException {
        try (BufferedReader in = new BufferedReader(new StringReader(text))) {
            String line;
            while (null != (line = in.readLine())) {
                if (license.matches(line) == State.t) {
                    return true;
                }
            }
            return license.finalizeState().asBoolean();
        }
    }

    @ParameterizedTest
    @MethodSource("parameterProvider")
    public void testEmbeddedStrings(String id, String family, String name, String notes, String[][] targets) throws IOException {
        String formats[] = { "%s", "now is not the time %s for copyright", "#%s", "##%s", "## %s", "##%s##", "## %s ##",
                "/*%s*/", "/* %s */" };

        ILicense license = extractCategory(id);
            try {
                for (String[] target : targets) {
                    for (String fmt : formats) {
                        boolean found = processText(license, String.format(fmt, target[TEXT]));
                        license.reset();
                        assertTrue(found, ()->String.format("%s %s did not match pattern '%s' for target string %s", id,
                                name, fmt, target[NAME]));
                    }
                }
            } finally {
                license.reset();
            }
    }
    
}
