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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.junit.Before;
import org.junit.Test;

/**
 * Test to see if short form license information will be recognized correctly.
 *
 */
abstract public class AbstractLicenseTest {
    private static int NAME = 0;
    private static int TEXT = 1;

    private Defaults defaults;
    protected MetaData data;

    private final String id;
    private final String family;
    private final String name;
    private final String notes;
    private final String[][] targets;

    protected AbstractLicenseTest(String id, String family, String name, String notes, String[][] targets) {
        this.id = id;
        this.family = ILicenseFamily.makeCategory(family);
        this.name = name;
        this.notes = notes;
        this.targets = targets;
    }

    @Before
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

    @Test
    public void testMatchProcessing() throws IOException {
        ILicense license = extractCategory(id);
            try {
                for (String[] target : targets) {
                    if (processText(license, target[TEXT])) {
                        data.reportOnLicense(license);
                        assertNotNull("No URL HEADER CATEGORY", data.get(MetaData.RAT_URL_HEADER_CATEGORY));
                        assertEquals(license.toString(), family,
                                data.get(MetaData.RAT_URL_HEADER_CATEGORY).getValue());
                        assertNotNull("No URL LICENSE FAMILY CATEGORY",
                                data.get(MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY));
                        assertEquals(license.toString(), family,
                                data.get(MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY).getValue());
                        if (StringUtils.isNotBlank(notes)) {
                            assertNotNull("No URL HEADER SAMPLE", data.get(MetaData.RAT_URL_HEADER_SAMPLE));
                            assertEquals(license.toString(), FullTextMatcher.prune(notes),
                                    FullTextMatcher.prune(data.get(MetaData.RAT_URL_HEADER_SAMPLE).getValue()));
                        } else {
                            assertNull("URL HEADER SAMPLE was not null", data.get(MetaData.RAT_URL_HEADER_SAMPLE));
                        }
                        assertNotNull("No URL LICENSE FAMILY NAME", data.get(MetaData.RAT_URL_LICENSE_FAMILY_NAME));
                        assertEquals(license.toString(), name,
                                data.get(MetaData.RAT_URL_LICENSE_FAMILY_NAME).getValue());
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

    @Test
    public void testEmbeddedStrings() throws IOException {
        String formats[] = { "%s", "now is not the time %s for copyright", "#%s", "##%s", "## %s", "##%s##", "## %s ##",
                "/*%s*/", "/* %s */" };

        ILicense license = extractCategory(id);
            try {
                for (String[] target : targets) {
                    for (String fmt : formats) {
                        boolean found = processText(license, String.format(fmt, target[TEXT]));
                        license.reset();
                        assertTrue(String.format("%s %s did not match pattern '%s' for target string %s", id,
                                name, fmt, target[NAME]), found);
                    }
                }
            } finally {
                license.reset();
            }
    }
}
