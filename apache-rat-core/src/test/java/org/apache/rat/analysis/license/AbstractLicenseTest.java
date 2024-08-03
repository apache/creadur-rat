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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

import org.apache.rat.Defaults;
import org.apache.rat.analysis.HeaderCheckWorker;
import org.apache.rat.analysis.IHeaders;
import org.apache.rat.api.MetaData;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.testhelpers.TestingLicense;
import org.apache.rat.utils.DefaultLog;
import org.junit.jupiter.api.BeforeEach;
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

    protected ILicense extractCategory(String famId, String id) {
        Optional<ILicense> result = LicenseSetFactory.search(famId, id, defaults.getLicenseSetFactory().getLicenses(LicenseFilter.ALL));
        if (!result.isPresent()) {
            fail(String.format("No licenses for id: f:%s l:%s", famId, id));
        }
        return result.get();
    }

    @ParameterizedTest
    @MethodSource("parameterProvider")
    public void testMatchProcessing(String id, String familyPattern, String name, String notes, String[][] targets)
            throws IOException {
        ILicense license = extractCategory(familyPattern, id);
        try {
            for (String[] target : targets) {
                if (processText(license, target[TEXT])) {
                    data.reportOnLicense(license);
                    assertEquals(1, data.licenses().count());
                    assertEquals(license, data.licenses().findFirst().get());
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
            IHeaders headers = HeaderCheckWorker.readHeader(in,
                    HeaderCheckWorker.DEFAULT_NUMBER_OF_RETAINED_HEADER_LINES);
            return license.matches(headers);
        }
    }

    @ParameterizedTest
    @MethodSource("parameterProvider")
    public void testEmbeddedStrings(String id, String family, String name, String notes, String[][] targets)
            throws IOException {
        String formats[] = { "%s", "now is not the time %s for copyright", "#%s", "##%s", "## %s", "##%s##", "## %s ##",
                "/*%s*/", "/* %s */" };

        ILicense license = extractCategory(family, id);
        try {
            for (String[] target : targets) {
                for (String fmt : formats) {
                    boolean found = processText(license, String.format(fmt, target[TEXT]));
                    license.reset();
                    assertTrue(found, () -> String.format("%s %s did not match pattern '%s' for target string %s", id,
                            name, fmt, target[NAME]));
                }
            }
        } finally {
            license.reset();
        }
    }

}
