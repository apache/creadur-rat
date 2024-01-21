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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;
import java.util.SortedSet;

import org.apache.rat.Defaults;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.api.MetaData.Datum;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseFamilySetFactory;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.testhelpers.TestingLicense;
import org.apache.rat.testhelpers.TestingLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the Default policy imlementatin.
 */
public class DefaultPolicyTest {
    /**
     * This is the number of accepted licenses in the default license file : /org/apache/rat/default.xml
     */
    private static final int NUMBER_OF_DEFAULT_ACCEPTED_LICENSES = 11;
    
    private static final ILicenseFamily[] APPROVED_FAMILIES = {
            makeFamily("AL", "Apache License Version 2.0"),
            makeFamily("BSD-3", "BSD 3 clause"),
            makeFamily("CDDL1", "COMMON DEVELOPMENT AND DISTRIBUTION LICENSE Version 1.0"),
            makeFamily("GEN", "Generated Files"),
            makeFamily("GPL1", "GNU General Public License, version 1"),
            makeFamily("GPL2", "GNU General Public License, version 2"),
            makeFamily("GPL3", "GNU General Public License, version 3"),
            makeFamily("MIT", "The MIT License"),
            makeFamily("OASIS", "OASIS Open License"),
            makeFamily("W3CD", "W3C Document Copyright"),
            makeFamily("W3C", "W3C Software Copyright"),
    };

    private Document document;
    private DefaultPolicy policy;
    private Defaults defaults;


    @BeforeEach
    public void setUp() throws Exception {
        defaults = Defaults.builder().build();
        policy = new DefaultPolicy(defaults.getLicenseFamilies(LicenseFilter.approved));
        document = new TestingLocation("subject");
    }

    private void assertApproval(boolean pApproved) {
        assertEquals(pApproved, MetaData.RAT_APPROVED_LICENSE_VALUE_TRUE
                .equals(document.getMetaData().value(MetaData.RAT_URL_APPROVED_LICENSE)));
    }

    private void setMetadata(ILicenseFamily family) {
        document.getMetaData().reportOnLicense(new TestingLicense(family));
    }

    private static ILicenseFamily makeFamily(String category, String name) {
        return ILicenseFamily.builder().setLicenseFamilyCategory(category)
                .setLicenseFamilyName(name).build();
    }
    
    @Test
    public void testCount() {
        assertEquals(NUMBER_OF_DEFAULT_ACCEPTED_LICENSES, policy.getApprovedLicenseNames().size());
    }

    @Test
    public void testApprovedLicenses() {
        
        assertEquals(NUMBER_OF_DEFAULT_ACCEPTED_LICENSES, APPROVED_FAMILIES.length, "Approved license count mismatch");
        for (ILicenseFamily family : APPROVED_FAMILIES) {
            setMetadata(family);
            policy.analyse(document);
            assertApproval(true);
        }
    }
    
    @Test
    public void testUnApprovedLicenses() {
        SortedSet<ILicenseFamily> all = defaults.getLicenseFamilies(LicenseFilter.all);
        SortedSet<ILicenseFamily> unapproved = LicenseFamilySetFactory.emptyLicenseFamilySet();
        unapproved.addAll(all);
        unapproved.removeAll(defaults.getLicenseFamilies(LicenseFilter.approved));

        assertEquals(all.size()-NUMBER_OF_DEFAULT_ACCEPTED_LICENSES, unapproved.size(), "Unapproved license count mismatch");
        for (ILicenseFamily family : unapproved) {
            setMetadata(family);
            policy.analyse(document);
            assertApproval(false);
        }
    }
    

    @Test
    public void testUnknownFamily() throws Exception {
        setMetadata(makeFamily("?????", "Unknown document"));
        policy.analyse(document);
        assertApproval(false);
    }

    @Test
    public void testAddNewApprovedLicenseAndDefaults() {
        ILicenseFamily testingFamily = makeFamily("test", "Testing License Family");
        setMetadata(testingFamily);
        policy.analyse(document);
        assertApproval(false);

        policy.add(testingFamily);
        assertNotNull(LicenseFamilySetFactory.search(testingFamily, policy.getApprovedLicenseNames()), "Did not properly add ILicenseFamily");
        policy.analyse(document);
        assertApproval(true);
    }

    @Test
    public void testAddNewApprovedLicenseNoDefaults() {
        policy = new DefaultPolicy(Collections.emptySet());
        assertEquals(0, policy.getApprovedLicenseNames().size());
        ILicenseFamily testingFamily = makeFamily("test", "Testing License Family");
        setMetadata(testingFamily);
        policy.analyse(document);
        assertApproval(false);

        policy.add(testingFamily);
        assertEquals(1, policy.getApprovedLicenseNames().size());
        assertNotNull(LicenseFamilySetFactory.search(testingFamily, policy.getApprovedLicenseNames()), "Did not properly add ILicenseFamily");
        policy.analyse(document);
        assertApproval(true);
    }
    
    @Test
    public void testNonStandardDocumentsDoNotFailLicenseTests() {
        Datum[] nonStandardDocuments = {
                MetaData.RAT_DOCUMENT_CATEGORY_DATUM_NOTICE,
                MetaData.RAT_DOCUMENT_CATEGORY_DATUM_ARCHIVE,
                MetaData.RAT_DOCUMENT_CATEGORY_DATUM_BINARY
        };
        
        for (Datum d : nonStandardDocuments) {
            document = new TestingLocation("subject");
            document.getMetaData().set(d);
            policy.analyse(document);
            assertNull( document.getMetaData().get(MetaData.RAT_URL_APPROVED_LICENSE), "failed on "+d.getValue());
        }
    }

    @Test
    public void testUnclassifiedDocumentsDoNotFailLicenseTests() {
        document.getMetaData().set(MetaData.RAT_DOCUMENT_CATEGORY_DATUM_STANDARD);
        policy.analyse(document);
        assertApproval( false );
    }
    
    @Test
    public void testReportLicenseApprovalClaim() {
        assertNull( document.getMetaData().get(MetaData.RAT_URL_APPROVED_LICENSE));
        
        
        policy.reportLicenseApprovalClaim(document, false);
        assertEquals( MetaData.RAT_APPROVED_LICENSE_DATUM_FALSE, document.getMetaData().get(MetaData.RAT_URL_APPROVED_LICENSE));
        
        policy.reportLicenseApprovalClaim(document, true);
        assertEquals( MetaData.RAT_APPROVED_LICENSE_DATUM_TRUE, document.getMetaData().get(MetaData.RAT_URL_APPROVED_LICENSE));
        
    }
}
