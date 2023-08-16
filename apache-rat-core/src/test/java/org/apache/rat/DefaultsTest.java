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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.rat.analysis.license.BaseLicense;
import org.junit.Test;

public class DefaultsTest {
    private static String[] FAMILIES = {"GEN", "?????", "AL", "OASIS", "W3CD", "W3C", "GPL1", "GPL2", "GPL3", "MIT", "CDDL1", "BSD_m"};

    @Test
    public void defaultConfigTest() {
        Defaults.builder().build();

        Collection<BaseLicense> families = Defaults.getLicenses();
        Set<String> names = families.stream().map(x -> x.getLicenseFamilyCategory().trim()).collect(Collectors.toSet());
        assertEquals(FAMILIES.length-1, names.size());
        names.removeAll(Arrays.asList(FAMILIES));
        assertTrue(names.isEmpty());

        Collection<BaseLicense> licenses = Defaults.getLicenses();
        assertEquals(15, licenses.size());
        Map<String,Integer> result = new TreeMap<>();
        licenses.stream().map(BaseLicense::getLicenseFamilyCategory).forEach( x -> {
            Integer i = result.get(x);
            if (i == null) {
                result.put(x, 1);
            } else {
                result.put(x, 1+i.intValue());
            }
        });
        assertEquals(2,result.get("AL   ").intValue());
        assertEquals(2,result.get("BSD_m").intValue());
        assertEquals(2,result.get("CDDL1").intValue());
        assertEquals(2,result.get("GEN  ").intValue());
        assertEquals(1,result.get("GPL1 ").intValue());
        assertEquals(1,result.get("GPL2 ").intValue());
        assertEquals(1,result.get("GPL3 ").intValue());
        assertEquals(1,result.get("MIT  ").intValue());
        assertEquals(1,result.get("OASIS").intValue());
        assertEquals(1,result.get("W3C  ").intValue());
        assertEquals(1,result.get("W3CD ").intValue());
    }
}
