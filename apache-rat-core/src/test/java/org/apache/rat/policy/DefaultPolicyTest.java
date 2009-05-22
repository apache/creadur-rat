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

import junit.framework.TestCase;

import org.apache.rat.api.MetaData;
import org.apache.rat.document.IDocument;
import org.apache.rat.document.MockLocation;
import org.apache.rat.report.claim.impl.xml.CustomClaim;
import org.apache.rat.report.claim.impl.xml.MockClaimReporter;


public class DefaultPolicyTest extends TestCase {

    MockClaimReporter reporter;
    DefaultPolicy policy;
    private IDocument subject;
    
    protected void setUp() throws Exception {
        super.setUp();
        reporter = new MockClaimReporter();
        policy = new DefaultPolicy();
        subject = new MockLocation("subject");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testOtherPredicate() throws Exception {
        final String predicate = "predicate";
        final String object = "object";
        policy.claim(new CustomClaim(subject, predicate, object, true));
        assertEquals("No claim", 0, reporter.claims.size());
    }

    public void testASLFamily() throws Exception {
        subject.getMetaData().set(MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_APACHE_LICENSE_VERSION_2_0);
        policy.report(subject);
        policy.claim(null);
        assertApproval(true);
    }

    private void assertApproval(boolean pApproved) {
        assertEquals(pApproved, MetaData.RAT_APPROVED_LICENSE_VALUE_TRUE.equals(subject.getMetaData().value(MetaData.RAT_URL_APPROVED_LICENSE)));
    }

    public void testOASISFamily() throws Exception {
        subject.getMetaData().set(MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_OASIS_OPEN_LICENSE);
        policy.report(subject);
        policy.claim(null);
        assertApproval(true);
    }
    
    public void testW3CFamily() throws Exception {
        subject.getMetaData().set(MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_W3C_SOFTWARE_COPYRIGHT);
        policy.report(subject);
        policy.claim(null);
        assertApproval(true);
    }
    
    public void testW3CDocFamily() throws Exception {
        subject.getMetaData().set(MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_W3C_DOCUMENT_COPYRIGHT);
        policy.report(subject);
        policy.claim(null);
        assertApproval(true);
    }
    
    public void testUnknownFamily() throws Exception {
        subject.getMetaData().set(MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_UNKNOWN);
        policy.report(subject);
        policy.claim(null);
        assertApproval(false);
    }
}
