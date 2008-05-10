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

import org.apache.rat.analysis.Claims;
import org.apache.rat.license.Apache20LicenseFamily;
import org.apache.rat.license.OASISLicenseFamily;
import org.apache.rat.license.W3CDocumentLicenseFamily;
import org.apache.rat.license.W3CSoftwareLicenseFamily;
import org.apache.rat.report.claim.impl.xml.MockClaimReporter;
import junit.framework.TestCase;

public class DefaultPolicyTest extends TestCase {

    MockClaimReporter reporter;
    DefaultPolicy policy;
    
    protected void setUp() throws Exception {
        super.setUp();
        reporter = new MockClaimReporter();
        policy = new DefaultPolicy(reporter);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testOtherPredicate() throws Exception {
        policy.claim("subject", "predicate", "object", true);
        assertEquals("No claim", 0, reporter.claims.size());
    }

    public void testASLFamily() throws Exception {
        policy.claim("subject", Claims.LICENSE_FAMILY_PREDICATE, Apache20LicenseFamily.APACHE_SOFTWARE_LICENSE_NAME, true);
        assertEquals("Approved claim", 1, reporter.claims.size());
        assertEquals("Approved claim", new MockClaimReporter.Claim("subject", Claims.LICENSE_APPROVAL_PREDICATE, "true", false), reporter.claims.get(0));
    }
    
    public void testOASISFamily() throws Exception {
        policy.claim("subject", Claims.LICENSE_FAMILY_PREDICATE, OASISLicenseFamily.OASIS_OPEN_LICENSE_NAME, true);
        assertEquals("Approved claim", 1, reporter.claims.size());
        assertEquals("Approved claim", new MockClaimReporter.Claim("subject", Claims.LICENSE_APPROVAL_PREDICATE, "true", false), reporter.claims.get(0));
    }
    
    public void testW3CFamily() throws Exception {
        policy.claim("subject", Claims.LICENSE_FAMILY_PREDICATE, W3CSoftwareLicenseFamily.W3C_SOFTWARE_COPYRIGHT_NAME, true);
        assertEquals("Approved claim", 1, reporter.claims.size());
        assertEquals("Approved claim", new MockClaimReporter.Claim("subject", Claims.LICENSE_APPROVAL_PREDICATE, "true", false), reporter.claims.get(0));
    }
    
    public void testW3CDocFamily() throws Exception {
        policy.claim("subject", Claims.LICENSE_FAMILY_PREDICATE, W3CDocumentLicenseFamily.W3C_DOCUMENT_COPYRIGHT_NAME, true);
        assertEquals("Approved claim", 1, reporter.claims.size());
        assertEquals("Approved claim", new MockClaimReporter.Claim("subject", Claims.LICENSE_APPROVAL_PREDICATE, "true", false), reporter.claims.get(0));
    }
    
    public void testUnknownFamily() throws Exception {
        policy.claim("subject", Claims.LICENSE_FAMILY_PREDICATE, "?????", true);
        assertEquals("Approved claim", 1, reporter.claims.size());
        assertEquals("Approved claim", new MockClaimReporter.Claim("subject", Claims.LICENSE_APPROVAL_PREDICATE, "false", false), reporter.claims.get(0));
    }
    
    public void testCustomNames() throws Exception {
        reporter = new MockClaimReporter();
        String[] custom = {"Example"};
        policy = new DefaultPolicy(reporter, custom);
        policy.claim("subject", Claims.LICENSE_FAMILY_PREDICATE, "?????", true);
        policy.claim("subject", Claims.LICENSE_FAMILY_PREDICATE, W3CDocumentLicenseFamily.W3C_DOCUMENT_COPYRIGHT_NAME, true);
        policy.claim("subject", Claims.LICENSE_FAMILY_PREDICATE, W3CSoftwareLicenseFamily.W3C_SOFTWARE_COPYRIGHT_NAME, true);
        policy.claim("subject", Claims.LICENSE_FAMILY_PREDICATE, Apache20LicenseFamily.APACHE_SOFTWARE_LICENSE_NAME, true);
        assertEquals("Four unapproved claims", 4, reporter.claims.size());
        assertEquals("Four unapproved claim", new MockClaimReporter.Claim("subject", Claims.LICENSE_APPROVAL_PREDICATE, "false", false), reporter.claims.get(0));
        assertEquals("Four unapproved claim", new MockClaimReporter.Claim("subject", Claims.LICENSE_APPROVAL_PREDICATE, "false", false), reporter.claims.get(1));
        assertEquals("Four unapproved claim", new MockClaimReporter.Claim("subject", Claims.LICENSE_APPROVAL_PREDICATE, "false", false), reporter.claims.get(2));
        assertEquals("Four unapproved claim", new MockClaimReporter.Claim("subject", Claims.LICENSE_APPROVAL_PREDICATE, "false", false), reporter.claims.get(3));
        policy.claim("subject", Claims.LICENSE_FAMILY_PREDICATE, "Example", true);
        assertEquals("Approved claim", 5, reporter.claims.size());
        assertEquals("Approved claim", new MockClaimReporter.Claim("subject", 
                Claims.LICENSE_APPROVAL_PREDICATE, "true", false), reporter.claims.get(4));
    }
}
