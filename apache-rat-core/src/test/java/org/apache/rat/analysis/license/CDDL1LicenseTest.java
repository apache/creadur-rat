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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CDDL1LicenseTest {

    private static final String LICENSE_LINE =
            " DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.\n\n"
                    + "Copyright 2011-2013 Tirasa. All rights reserved.\n\n"
                    + "The contents of this file are subject to the terms of the Common Development\n"
                    + "and Distribution License(\"CDDL\") (the \"License\"). You may not use this file\n"
                    + "except in compliance with the License.\n\n"
                    + "You can obtain a copy of the License at https://oss.oracle.com/licenses/CDDL\n"
                    + "See the License for the specific language governing permissions and limitations\n"
                    + "under the License.";

    /**
     * To ease testing provide a map with a given license version and the string to test for.
     */
    private static Map<IHeaderMatcher, String> licenseStringMap;

    private Document subject;

    @BeforeClass
    public static void initLicensesUnderTest() {
        licenseStringMap = new HashMap<>();
        licenseStringMap.put(new CDDL1License(), LICENSE_LINE);
        assertEquals(1, licenseStringMap.entrySet().size());
    }

    @Before
    public final void initSubject() {
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
