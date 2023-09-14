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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

import org.apache.rat.Defaults;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.api.MetaData.Datum;
import org.apache.rat.document.MockLocation;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.SimpleLicenseFamily;
import org.junit.Before;
import org.junit.Test;

public class DefaultPolicyTest {
    private static final int NUMBER_OF_DEFAULT_ACCEPTED_LICENSES = 15;

    private Document subject;
    private DefaultPolicy policy;

    @Before
    public void setUp() throws Exception {
        Defaults.builder().build();
        policy = new DefaultPolicy(Defaults.getLicenseFamilies());
        subject = new MockLocation("subject");
    }

    private void assertApproval(boolean pApproved) {
        assertEquals(pApproved, MetaData.RAT_APPROVED_LICENSE_VALUE_TRUE
                .equals(subject.getMetaData().value(MetaData.RAT_URL_APPROVED_LICENSE)));
    }

    private void setMetadata(ILicenseFamily family) {
        subject.getMetaData().add(new Datum(MetaData.RAT_URL_LICENSE_FAMILY_NAME, family.getFamilyName()));
        subject.getMetaData().add(new Datum(MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY, family.getFamilyCategory()));
    }

    @Test
    public void testCount() {
        assertEquals(NUMBER_OF_DEFAULT_ACCEPTED_LICENSES, policy.getApprovedLicenseNames().size());
    }

    @Test
    public void testALFamily() throws Exception {
        setMetadata(new SimpleLicenseFamily("AL", "Apache License Version 2.0"));
        policy.analyse(subject);
        assertApproval(true);
    }

    @Test
    public void testOASISFamily() throws Exception {
        setMetadata(new SimpleLicenseFamily("OASIS", "OASIS Open License"));
        policy.analyse(subject);
        assertApproval(true);
    }

    @Test
    public void testW3CFamily() throws Exception {
        setMetadata(new SimpleLicenseFamily("W3C", "W3C Software Copyright"));
        policy.analyse(subject);
        assertApproval(true);
    }

    @Test
    public void testW3CDocFamily() throws Exception {
        setMetadata(new SimpleLicenseFamily("W3CD", "W3C Document Copyright"));
        policy.analyse(subject);
        assertApproval(true);
    }

    @Test
    public void testModifiedBSDFamily() throws Exception {
        setMetadata(new SimpleLicenseFamily("TMF", "The Telemanagement Forum License"));
        policy.analyse(subject);
        assertApproval(true);
    }

    @Test
    public void testMITFamily() throws Exception {
        setMetadata(new SimpleLicenseFamily("MIT", "The MIT License"));
        policy.analyse(subject);
        assertApproval(true);
    }

    @Test
    public void testCDDL1Family() throws Exception {
        setMetadata(new SimpleLicenseFamily("CDDL1", "COMMON DEVELOPMENT AND DISTRIBUTION LICENSE Version 1.0"));
        policy.analyse(subject);
        assertApproval(true);
    }

    @Test
    public void testUnknownFamily() throws Exception {
        setMetadata(new SimpleLicenseFamily("?????", "Unknown document"));
        policy.analyse(subject);
        assertApproval(false);
    }

    @Test
    public void testNullAsMarkerOfDefaults() {
        // without defaults and no additions == 0
        for (DefaultPolicy policy : new DefaultPolicy[] { //
                new DefaultPolicy(new ArrayList<ILicenseFamily>(0)), //
                new DefaultPolicy(new ILicenseFamily[] {}), }) {
            assertEquals(0, policy.getApprovedLicenseNames().size());
        }
    }

    @Test
    public void testAddNewApprovedLicenseAndDefaults() {
        ILicenseFamily testingFamily = new SimpleLicenseFamily("test", "Testing License Family");
        setMetadata(testingFamily);
        policy.analyse(subject);
        assertApproval(false);

        policy.add(testingFamily);
        assertNotNull("Did not properly add ILicenseFamily",
                ILicenseFamily.search(testingFamily, policy.getApprovedLicenseNames()));
        policy.analyse(subject);
        assertApproval(true);
    }

    @Test
    public void testAddNewApprovedLicenseNoDefaults() {
        policy = new DefaultPolicy();
        assertEquals(0, policy.getApprovedLicenseNames().size());
        ILicenseFamily testingFamily = new SimpleLicenseFamily("test", "Testing License Family");
        setMetadata(testingFamily);
        policy.analyse(subject);
        assertApproval(false);

        policy.add(testingFamily);
        assertEquals(1, policy.getApprovedLicenseNames().size());
        assertNotNull("Did not properly add ILicenseFamily",
                ILicenseFamily.search(testingFamily, policy.getApprovedLicenseNames()));
        policy.analyse(subject);
        assertApproval(true);
    }
}
