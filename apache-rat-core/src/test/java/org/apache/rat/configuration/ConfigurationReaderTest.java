/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rat.configuration;

import static org.junit.Assert.assertArrayEquals;

import java.net.URL;
import java.util.Collection;
import java.util.stream.Collectors;

import org.junit.Test;

public class ConfigurationReaderTest {

    private static String[] EXPECTED_IDS = { "AL", "ASL", "BSD-3", "CDDL1", "DOJO", "GEN", "GPL1", "GPL2", "GPL3",
            "MIT", "OASIS", "W3C", "W3CD" };
    private static String[] EXPECTED_LICENSES = { "AL   ", "ASL  ", "BSD-3", "CDDL1", "DOJO ", "GEN  ", "GPL1 ",
            "GPL2 ", "GPL3 ", "ILLUM", "MIT  ", "OASIS", "TMF  ", "W3C  ", "W3CD " };

    @Test
    public void approvedLicenseIdTest() {
        XMLConfigurationReader reader = new XMLConfigurationReader();
        URL url = ConfigurationReaderTest.class.getResource("/org/apache/rat/default.xml");
        reader.read(url);

        Collection<String> readCategories = reader.approvedLicenseId();
        assertArrayEquals(EXPECTED_IDS, readCategories.toArray(new String[readCategories.size()]));
    }

    @Test
    public void LicensesTest() {
        XMLConfigurationReader reader = new XMLConfigurationReader();
        URL url = ConfigurationReaderTest.class.getResource("/org/apache/rat/default.xml");
        reader.read(url);

        Collection<String> readCategories = reader.readLicenses().stream()
                .map(x -> x.getLicenseFamily().getFamilyCategory()).collect(Collectors.toList());
        assertArrayEquals(EXPECTED_LICENSES, readCategories.toArray(new String[readCategories.size()]));
    }

}
