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

import org.apache.rat.api.Document;
import org.apache.rat.document.MockLocation;
import org.apache.rat.report.claim.impl.xml.MockClaimReporter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class W3CLicenseTest {

    public static final String COPYRIGHT_URL 
    = "http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231";
    
    public static final String COPYRIGHT_URL_COMMENTED
    = "# http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231 #";
    
    public static final String COPYRIGHT_URL_XML
    = "<!-- http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231 -->";
    
    private W3CLicense license;
    private MockClaimReporter reporter;

    @Before
    public void setUp() throws Exception {
        license = new W3CLicense();
        reporter = new MockClaimReporter();
    }

    @Test
    public void match() throws Exception {
        final Document subject = new MockLocation("subject");
        assertTrue("Expected matcher to return license", license.match(subject, COPYRIGHT_URL));
        assertTrue("Expected matcher to return license", license.match(subject, COPYRIGHT_URL_COMMENTED));
        assertTrue("Expected matcher to return license", license.match(subject, COPYRIGHT_URL_XML));
        assertFalse("Return null if the license isn't matched", license.match(subject, "Bogus"));
    }

}
