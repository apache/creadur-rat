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
import static org.junit.Assert.assertTrue;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.document.MockDocument;
import org.junit.Before;
import org.junit.Test;

public class SPDXMatcherTest {

    private IHeaderMatcher defaults = new SPDXMatcher();
    private Document testDocument;

    @Before
    public void setup() {
        testDocument = new MockDocument("SPDXMatcher test");
    }
    
    private String getTag(String shortName) {
        return String.format("Some text\n  SPDX-License-Identifier:\t%s \nSome more text", shortName);
    }
    
    private void validate( String category, String name) {
        assertEquals(category, testDocument.getMetaData().value(MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY));
        assertEquals(name, testDocument.getMetaData().value(MetaData.RAT_URL_LICENSE_FAMILY_NAME));
  
    }
    @Test
    public void testCDDL1() throws RatHeaderAnalysisException {
        assertTrue( defaults.match( testDocument, getTag("CDDL-1.0")));
        validate(MetaData.RAT_LICENSE_FAMILY_CATEGORY_VALUE_CDDL1, MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_CDDL1);
    }
    
    @Test
    public void testGPL1() throws RatHeaderAnalysisException {
        assertTrue( defaults.match( testDocument, getTag("GPL-1.0-only")));
        validate(MetaData.RAT_LICENSE_FAMILY_CATEGORY_VALUE_GPL1, MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_GPL_VERSION_1);
    }

    @Test
    public void testGPL2() throws RatHeaderAnalysisException {
        assertTrue( defaults.match( testDocument, getTag("GPL-2.0-only")));
        validate(MetaData.RAT_LICENSE_FAMILY_CATEGORY_VALUE_GPL2, MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_GPL_VERSION_2);
    }

    @Test
    public void testGPL3() throws RatHeaderAnalysisException {
        assertTrue( defaults.match( testDocument, getTag("GPL-3.0-only")));
        validate(MetaData.RAT_LICENSE_FAMILY_CATEGORY_VALUE_GPL3, MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_GPL_VERSION_3);
    }

    @Test
    public void testMIT() throws RatHeaderAnalysisException {
        assertTrue( defaults.match( testDocument, getTag("MIT")));
        validate(MetaData.RAT_LICENSE_FAMILY_CATEGORY_VALUE_MIT, MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_MIT);
    }

    @Test
    public void testApache2() throws RatHeaderAnalysisException {
        assertTrue( defaults.match( testDocument, getTag("Apache-2.0")));
        validate(MetaData.RAT_LICENSE_FAMILY_CATEGORY_VALUE_ASL, MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_APACHE_LICENSE_VERSION_2_0);
    }

    @Test
    public void testW3C() throws RatHeaderAnalysisException {
        assertTrue( defaults.match( testDocument, getTag("W3C")));
        validate(MetaData.RAT_LICENSE_FAMILY_CATEGORY_VALUE_W3CD, MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_W3C_DOCUMENT_COPYRIGHT);
    }
}
