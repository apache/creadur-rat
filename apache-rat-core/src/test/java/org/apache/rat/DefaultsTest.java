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
package org.apache.rat;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.apache.rat.license.ILicense;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.utils.DefaultLog;
import org.junit.jupiter.api.Test;


public class DefaultsTest {
    private static final String[] FAMILIES = { "BSD-3", "GEN  ", "AL   ", "OASIS", "W3CD ", "W3C  ", "GPL1 ",
            "GPL2 ", "GPL3 ", "MIT  ", "CDDL1" };

    @Test
    public void defaultConfigTest() {
        Defaults defaults = Defaults.builder().build(DefaultLog.getInstance());

        Set<ILicense> licenses = defaults.getLicenseSetFactory().getLicenses(LicenseFilter.ALL);

        Set<String> names = new TreeSet<>();
        licenses.forEach(x -> names.add(x.getLicenseFamily().getFamilyCategory()));
        assertEquals(FAMILIES.length, names.size());
        names.removeAll(Arrays.asList(FAMILIES));
        assertTrue(names.isEmpty());
    }
}
