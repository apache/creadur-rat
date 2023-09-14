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

import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.rat.license.ILicenseFamily;
import org.junit.Test;

public class ConfigurationReaderTest {

    private static String[] FAMILIES = { "GEN  ", "AL   ", "ASL  ", "TMF  ", "DOJO ", "OASIS", "W3CD ", "W3C  ",
            "GPL1 ", "GPL2 ", "GPL3 ", "MIT  ", "CDDL1", "ILLUM", "BSD-3" };

    @Test
    public void readDefault() {
        XMLConfigurationReader reader = new XMLConfigurationReader();
        URL url = XMLConfigurationReader.class.getResource("/org/apache/rat/default.xml");
        reader.read(url);
        List<String> familyCategories = Arrays.asList(FAMILIES);
        List<String> readCategories = reader.readFamilies().stream().map(ILicenseFamily::getFamilyCategory)
                .collect(Collectors.toList());
        assertTrue(readCategories.containsAll(familyCategories));
        readCategories.removeAll(familyCategories);
        assertTrue(readCategories.isEmpty());
    }

}
