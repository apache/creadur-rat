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
package org.apache.rat.policy;

import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.api.MetaData.Datum;
import org.apache.rat.document.MockLocation;
import org.apache.rat.report.claim.impl.xml.MockClaimReporter;
import org.junit.Before;
import org.junit.Test;

import static org.apache.rat.api.domain.RatLicenseFamily.W3C;
import static org.apache.rat.api.domain.RatLicenseFamily.W3C_DOCUMENTATION;
import static org.junit.Assert.assertEquals;


public class DefaultPolicyTest {

    MockClaimReporter reporter;
    DefaultPolicy policy;
    private Document subject;

    @Before
    public void setUp() throws Exception {
        reporter = new MockClaimReporter();
        policy = new DefaultPolicy();
        subject = new MockLocation("subject");
    }

    @Test
    public void testALFamily() throws Exception {
        subject.getMetaData().set(MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_APACHE_LICENSE_VERSION_2_0);
        policy.analyse(subject);
        assertApproval(true);
    }

    @SuppressWarnings("boxing") // OK in test code
    private void assertApproval(boolean pApproved) {
        assertEquals(pApproved, MetaData.RAT_APPROVED_LICENSE_VALUE_TRUE.equals(subject.getMetaData().value(MetaData.RAT_URL_APPROVED_LICENSE)));
    }

    @Test
    public void testOASISFamily() throws Exception {
        subject.getMetaData().set(MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_OASIS_OPEN_LICENSE);
        policy.analyse(subject);
        assertApproval(true);
    }
    
    @Test
    public void testW3CFamily() throws Exception {
        subject.getMetaData().set(new MetaData.Datum(MetaData.RAT_URL_LICENSE_FAMILY_NAME, W3C.getName()));
        policy.analyse(subject);
        assertApproval(true);
    }
    
    @Test
    public void testW3CDocFamily() throws Exception {
        subject.getMetaData().set(new MetaData.Datum(MetaData.RAT_URL_LICENSE_FAMILY_NAME, W3C_DOCUMENTATION.getName()));
        policy.analyse(subject);
        assertApproval(true);
    }
    
    @Test
    public void testUnknownFamily() throws Exception {
        subject.getMetaData().set(MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_UNKNOWN);
        policy.analyse(subject);
        assertApproval(false);
    }
}
