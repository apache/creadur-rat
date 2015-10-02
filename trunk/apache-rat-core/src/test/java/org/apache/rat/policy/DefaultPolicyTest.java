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

import org.apache.rat.Defaults;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.document.MockLocation;
import org.apache.rat.license.ILicenseFamily;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;


public class DefaultPolicyTest {
    private static final int NUMBER_OF_DEFAULT_ACCEPTED_LICENSES = Defaults.DEFAULT_LICENSE_FAMILIES.size();

    private static final ILicenseFamily JUST_A_TEST_LIC_FAMILY = new ILicenseFamily() {
        public String getFamilyName() {
            return "justATest";
        }
    };

    private Document subject;
    private DefaultPolicy policy;

    @Before
    public void setUp() throws Exception {
        policy = new DefaultPolicy();
        subject = new MockLocation("subject");
    }

    @SuppressWarnings("boxing") // OK in test code
    private void assertApproval(boolean pApproved) {
        assertEquals(pApproved, MetaData.RAT_APPROVED_LICENSE_VALUE_TRUE.equals(subject.getMetaData().value(MetaData.RAT_URL_APPROVED_LICENSE)));
    }

    @Test
    public void testALFamily() throws Exception {
        assertEquals(NUMBER_OF_DEFAULT_ACCEPTED_LICENSES, policy.getApprovedLicenseNames().size());
        subject.getMetaData().set(MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_APACHE_LICENSE_VERSION_2_0);
        policy.analyse(subject);
        assertApproval(true);
    }

    @Test
    public void testOASISFamily() throws Exception {
        assertEquals(NUMBER_OF_DEFAULT_ACCEPTED_LICENSES, policy.getApprovedLicenseNames().size());
        subject.getMetaData().set(MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_OASIS_OPEN_LICENSE);
        policy.analyse(subject);
        assertApproval(true);
    }

    @Test
    public void testW3CFamily() throws Exception {
        assertEquals(NUMBER_OF_DEFAULT_ACCEPTED_LICENSES, policy.getApprovedLicenseNames().size());
        subject.getMetaData().set(MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_W3C_SOFTWARE_COPYRIGHT);
        policy.analyse(subject);
        assertApproval(true);
    }

    @Test
    public void testW3CDocFamily() throws Exception {
        assertEquals(NUMBER_OF_DEFAULT_ACCEPTED_LICENSES, policy.getApprovedLicenseNames().size());
        subject.getMetaData().set(MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_W3C_DOCUMENT_COPYRIGHT);
        policy.analyse(subject);
        assertApproval(true);
    }

    @Test
    public void testModifiedBSDFamily() throws Exception {
        assertEquals(NUMBER_OF_DEFAULT_ACCEPTED_LICENSES, policy.getApprovedLicenseNames().size());
        subject.getMetaData().set(MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_MODIFIED_BSD_LICENSE);
        policy.analyse(subject);
        assertApproval(true);
    }

    @Test
    public void testMITFamily() throws Exception {
        assertEquals(NUMBER_OF_DEFAULT_ACCEPTED_LICENSES, policy.getApprovedLicenseNames().size());
        subject.getMetaData().set(MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_MIT);
        policy.analyse(subject);
        assertApproval(true);
    }

    @Test
    public void testCDDL1Family() throws Exception {
        assertEquals(NUMBER_OF_DEFAULT_ACCEPTED_LICENSES, policy.getApprovedLicenseNames().size());
        subject.getMetaData().set(MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_CDDL1);
        policy.analyse(subject);
        assertApproval(true);
    }

    @Test
    public void testUnknownFamily() throws Exception {
        assertEquals(NUMBER_OF_DEFAULT_ACCEPTED_LICENSES, policy.getApprovedLicenseNames().size());
        subject.getMetaData().set(MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_UNKNOWN);
        policy.analyse(subject);
        assertApproval(false);
    }

    @Test
    public void testNullAsMarkerOfDefaults() {
        // with defaults
        for (DefaultPolicy policy : new DefaultPolicy[]{//
                new DefaultPolicy(), //
                new DefaultPolicy(new ArrayList<String>(0), true),//
                new DefaultPolicy(new ILicenseFamily[]{}, true),
        }) {
            assertEquals("Did you add new license defaults?", NUMBER_OF_DEFAULT_ACCEPTED_LICENSES, policy.getApprovedLicenseNames().size());
        }

        // without defaults and no additions == 0
        for (DefaultPolicy policy : new DefaultPolicy[]{//
                new DefaultPolicy(new ArrayList<String>(0), false),//
                new DefaultPolicy(new ILicenseFamily[]{}, false),
        }) {
            assertEquals(0, policy.getApprovedLicenseNames().size());
        }
    }

    @Test
    public void testAddNewApprovedLicenseAndDefaults() {
        assertEquals("justATest", new DefaultPolicy(new ILicenseFamily[]{JUST_A_TEST_LIC_FAMILY}, false).getApprovedLicenseNames().get(0));
        assertEquals("Did not properly merge approved licenses with default", 1, new DefaultPolicy(new ILicenseFamily[]{JUST_A_TEST_LIC_FAMILY}, false).getApprovedLicenseNames().size());
    }

    @Test
    public void testAddNewApprovedLicenseNoDefaults() {
        assertEquals("justATest", new DefaultPolicy(new ILicenseFamily[]{JUST_A_TEST_LIC_FAMILY}, false).getApprovedLicenseNames().get(0));
        assertEquals("Did not properly merge approved licenses with default", NUMBER_OF_DEFAULT_ACCEPTED_LICENSES + 1, new DefaultPolicy(new ILicenseFamily[]{JUST_A_TEST_LIC_FAMILY}, true).getApprovedLicenseNames().size());
    }
}
