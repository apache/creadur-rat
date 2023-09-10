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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.license.BaseLicense;
import org.apache.rat.api.MetaData;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.junit.Test;
import org.xml.sax.SAXException;

public class ConfigurationReaderTest {
    
    private static String[] FAMILIES = {"GEN  ", "?????", "AL   ", "OASIS", "W3CD ", "W3C  ", "GPL1 ", "GPL2 ", "GPL3 ", "MIT  ", "CDDL1", "BSD_m"};
    
    @Test
    public void readDefault() throws ConfigurationException, SAXException, IOException, ParserConfigurationException {
        XMLConfigurationReader reader = new XMLConfigurationReader();
        URL url = XMLConfigurationReader.class.getResource("/org/apache/rat/default.config");
        reader.read(url);
        List<String> familyCategories = Arrays.asList(FAMILIES);
        Set<String> readCategories = reader.readFamilies().stream().map(ILicenseFamily::getFamilyCategory).collect(Collectors.toSet());
        assertTrue(readCategories.containsAll(familyCategories));
        readCategories.removeAll(familyCategories);
        assertTrue(readCategories.isEmpty());
        
        Collection<ILicense> licenses = reader.readLicenses();
        assertEquals(15, licenses.size());
        Map<String,Integer> result = new TreeMap<>();
        for (IHeaderMatcher license : licenses) {
            license.reportFamily( c -> { 
                Integer i = result.get(c.getFamilyCategory());
                if (i == null) {
                    result.put(c.getFamilyCategory(), 1);
                } else {
                    result.put(c.getFamilyCategory(), 1+i.intValue());
                }
            });
        }
 
        assertEquals(3,result.get("AL   ").intValue());
        assertEquals(2,result.get("BSD_m").intValue());
        assertEquals(3,result.get("CDDL1").intValue());
        assertEquals(2,result.get("GEN  ").intValue());
        assertEquals(2,result.get("GPL1 ").intValue());
        assertEquals(2,result.get("GPL2 ").intValue());
        assertEquals(2,result.get("GPL3 ").intValue());
        assertEquals(2,result.get("MIT  ").intValue());
        assertEquals(2,result.get("OASIS").intValue());
        assertEquals(2,result.get("W3C  ").intValue());
        assertEquals(1,result.get("W3CD ").intValue());
    }

}
