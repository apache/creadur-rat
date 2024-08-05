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

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.rat.Defaults;
import org.apache.rat.api.Document;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.testhelpers.TestingLicense;
import org.apache.rat.testhelpers.TestingDocument;
import org.apache.rat.testhelpers.TestingMatcher;
import org.apache.rat.utils.DefaultLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the Default policy imlementatin.
 */
public class DefaultPolicyTest {
    /**
     * This is the number of accepted licenses in the default license file :
     * /org/apache/rat/default.xml
     */
    private static final int NUMBER_OF_DEFAULT_ACCEPTED_LICENSES = 11;

    private static final ILicenseFamily[] APPROVED_FAMILIES = { makeFamily("AL", "Apache License Version 2.0"),
            makeFamily("BSD-3", "BSD 3 clause"),
            makeFamily("CDDL1", "COMMON DEVELOPMENT AND DISTRIBUTION LICENSE Version 1.0"),
            makeFamily("GEN", "Generated Files"), makeFamily("GPL1", "GNU General Public License, version 1"),
            makeFamily("GPL2", "GNU General Public License, version 2"),
            makeFamily("GPL3", "GNU General Public License, version 3"), makeFamily("MIT", "The MIT License"),
            makeFamily("OASIS", "OASIS Open License"), makeFamily("W3CD", "W3C Document Copyright"),
            makeFamily("W3C", "W3C Software Copyright"), };

    private Document document;
    private DefaultPolicy policy;
    private Defaults defaults;

    @BeforeEach
    public void setUp() throws Exception {
        defaults = Defaults.builder().build();
        policy = new DefaultPolicy(defaults.getLicenseSetFactory().getLicenseFamilies(LicenseFilter.APPROVED));
        document = new TestingDocument("subject");
    }

    private void assertApproval(boolean pApproved) {
        boolean state = document.getMetaData().approvedLicenses().count() > 0;
        assertEquals(pApproved, state);
    }

    private void setMetadata(ILicenseFamily family) {
        document.getMetaData().reportOnLicense(new TestingLicense(family.getFamilyCategory().trim(), new TestingMatcher(), family));
    }

    private static ILicenseFamily makeFamily(String category, String name) {
        return ILicenseFamily.builder().setLicenseFamilyCategory(category).setLicenseFamilyName(name).build();
    }

    @Test
    public void testCount() {
        assertEquals(NUMBER_OF_DEFAULT_ACCEPTED_LICENSES, policy.getApprovedLicenseFamilies().size());
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
        SortedSet<ILicenseFamily> all = defaults.getLicenseSetFactory().getLicenseFamilies(LicenseFilter.ALL);
        SortedSet<ILicenseFamily> unapproved = new TreeSet<>();
        unapproved.addAll(all);
        unapproved.removeAll(defaults.getLicenseSetFactory().getLicenseFamilies(LicenseFilter.APPROVED));

        assertEquals(all.size() - NUMBER_OF_DEFAULT_ACCEPTED_LICENSES, unapproved.size(),
                "Unapproved license count mismatch");
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
        assertNotNull(LicenseSetFactory.familySearch(testingFamily, policy.getApprovedLicenseFamilies()),
                "Did not properly add ILicenseFamily");
        policy.analyse(document);
        assertApproval(true);
    }

    @Test
    public void testAddNewApprovedLicenseNoDefaults() {
        policy = new DefaultPolicy(Collections.emptySet());
        assertEquals(0, policy.getApprovedLicenseFamilies().size());
        ILicenseFamily testingFamily = makeFamily("test", "Testing License Family");
        setMetadata(testingFamily);
        policy.analyse(document);
        assertApproval(false);

        policy.add(testingFamily);
        assertEquals(1, policy.getApprovedLicenseFamilies().size());
        assertNotNull(LicenseSetFactory.familySearch(testingFamily, policy.getApprovedLicenseFamilies()),
                "Did not properly add ILicenseFamily");
        policy.analyse(document);
        assertApproval(true);
    }

    @Test
    public void testNonStandardDocumentsDoNotFailLicenseTests() {
        Document.Type[] nonStandardDocuments = { Document.Type.NOTICE, Document.Type.ARCHIVE, Document.Type.BINARY };

        for (Document.Type d : nonStandardDocuments) {
            document = new TestingDocument("subject");
            document.getMetaData().setDocumentType(d);
            policy.analyse(document);
            assertEquals(0, document.getMetaData().licenses().count(), "failed on " + d);
        }
    }

    @Test
    public void testUnclassifiedDocumentsDoNotFailLicenseTests() {
        document.getMetaData().setDocumentType(Document.Type.STANDARD);
        policy.analyse(document);
        assertApproval(false);
    }
}
