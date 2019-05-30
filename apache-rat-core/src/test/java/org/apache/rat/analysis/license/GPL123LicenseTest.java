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

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.api.Document;
import org.apache.rat.document.MockLocation;
import org.apache.rat.report.claim.impl.xml.MockClaimReporter;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests GPL license occurrences within comments and other characters.
 * Works for GPL1 to GPL3.
 */
public class GPL123LicenseTest {
    MockClaimReporter reporter;
    Document subject;

    /**
     * To ease testing provide a map with a given license version and the string to test for.
     */
    private static Map<IHeaderMatcher, String> licenseStringMap;

    /**
     * If you replace this with BeforeClass and make this method static the build fails at line 67.
     */
    @Before
    public void initLicensesUnderTest() {
        licenseStringMap = new HashMap<>();
        licenseStringMap.put(new GPL1License(), GPL1License.FIRST_LICENSE_LINE);
        licenseStringMap.put(new GPL2License(), GPL2License.FIRST_LICENSE_LINE);
        licenseStringMap.put(new GPL3License(), GPL3License.FIRST_LICENSE_LINE);
    }

    @Before
    public final void initReporter() {
        this.reporter = new MockClaimReporter();
        this.subject = new MockLocation("subject");
    }

    @Test
    public void testNegativeMatches() throws Exception {
        for (Map.Entry<IHeaderMatcher, String> licenseUnderTest : licenseStringMap.entrySet()) {
            assertFalse(licenseUnderTest.getKey().match(subject, "'Behold, Telemachus! (nor fear the sight,)"));
        }
    }

    @Test
    public void testPositiveMatchInDocument() throws Exception {
        for (Map.Entry<IHeaderMatcher, String> licenseUnderTest : licenseStringMap.entrySet()) {
            assertTrue(licenseUnderTest.getKey().match(subject, "\t" + licenseUnderTest.getValue()));
            assertTrue(licenseUnderTest.getKey().match(subject, "     " + licenseUnderTest.getValue()));
            assertTrue(licenseUnderTest.getKey().match(subject, licenseUnderTest.getValue()));
            assertTrue(licenseUnderTest.getKey().match(subject, " * " + licenseUnderTest.getValue()));
            assertTrue(licenseUnderTest.getKey().match(subject, " // " + licenseUnderTest.getValue()));
            assertTrue(licenseUnderTest.getKey().match(subject, " /* " + licenseUnderTest.getValue()));
            assertTrue(licenseUnderTest.getKey().match(subject, " /** " + licenseUnderTest.getValue()));
            assertTrue(licenseUnderTest.getKey().match(subject, "    " + licenseUnderTest.getValue()));
            assertTrue(licenseUnderTest.getKey().match(subject, " ## " + licenseUnderTest.getValue()));
            assertTrue(licenseUnderTest.getKey().match(subject, " ## " + licenseUnderTest.getValue() + " ##"));
        }
    }

}
