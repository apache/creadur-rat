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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.license.ILicense;
import org.junit.Test;

public class DefaultsTest {
    private static String[] FAMILIES = { "GEN  ", "?????", "AL   ", "OASIS", "W3CD ", "W3C  ", "GPL1 ", "GPL2 ",
            "GPL3 ", "MIT  ", "CDDL1", "BSD_m" };

    @Test
    public void defaultConfigTest() {
        Defaults.builder().build();

        Collection<ILicense> licenses = Defaults.getLicenses();

        Set<String> names = new HashSet<>();
        licenses.forEach(x -> x.reportFamily(y -> names.add(y.getFamilyCategory())));
        assertEquals(FAMILIES.length - 1, names.size());
        names.removeAll(Arrays.asList(FAMILIES));
        assertTrue(names.isEmpty());


        assertEquals(15, licenses.size());
        Map<String, Integer> result = new TreeMap<>();
        licenses.forEach(x -> {
            x.reportFamily(y -> {
                String cat = y.getFamilyCategory();
                Integer i = result.get(cat);
                if (i == null) {
                    result.put(cat, 1);
                } else {
                    result.put(cat, 1 + i.intValue());
                }
            });
        });
        assertEquals(4, result.get("AL   ").intValue());
        assertEquals(2, result.get("BSD_m").intValue());
        assertEquals(3, result.get("CDDL1").intValue());
        assertEquals(2, result.get("GEN  ").intValue());
        assertEquals(2, result.get("GPL1 ").intValue());
        assertEquals(2, result.get("GPL2 ").intValue());
        assertEquals(2, result.get("GPL3 ").intValue());
        assertEquals(2, result.get("MIT  ").intValue());
        assertEquals(2, result.get("OASIS").intValue());
        assertEquals(2, result.get("W3C  ").intValue());
        assertEquals(1, result.get("W3CD ").intValue());
    }
}
