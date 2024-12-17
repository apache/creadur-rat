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
package org.apache.rat.license;

import java.util.SortedSet;
import org.apache.rat.Defaults;
import org.apache.rat.analysis.UnknownLicense;
import org.apache.rat.testhelpers.TestingLicense;
import org.apache.rat.testhelpers.TestingMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LicenseSetFactoryTest {

    /**
     * This is the number of accepted licenses in the default license file :
     * /org/apache/rat/default.xml
     */
    private static final int NUMBER_OF_DEFAULT_ACCEPTED_LICENSES = 7;

    private static final ILicenseFamily[] APPROVED_FAMILIES = { //
            makeFamily("AL", "Apache License Version 2.0"),
            makeFamily("BSD-3", "BSD 3 clause"),
            makeFamily("CDDL1", "COMMON DEVELOPMENT AND DISTRIBUTION LICENSE Version 1.0"),
            makeFamily("MIT", "The MIT License"),
            makeFamily("OASIS", "OASIS Open License"),
            makeFamily("W3CD", "W3C Document Copyright"),
            makeFamily("W3C", "W3C Software Copyright"),};

    private Defaults defaults;
    private LicenseSetFactory licenseSetFactory;

    @BeforeEach
    public void setUp() {
        defaults = Defaults.builder().build();
        licenseSetFactory = defaults.getLicenseSetFactory();
        assertThat(licenseSetFactory.getLicenseFamilies(LicenseSetFactory.LicenseFilter.APPROVED).size()).isEqualTo(NUMBER_OF_DEFAULT_ACCEPTED_LICENSES);
    }

    private static ILicenseFamily makeFamily(String category, String name) {
        return ILicenseFamily.builder().setLicenseFamilyCategory(category).setLicenseFamilyName(name).build();
    }

    private SortedSet<ILicenseFamily> getUnapprovedLicenseFamilies() {
        SortedSet<ILicenseFamily> unapproved = defaults.getLicenseSetFactory().getLicenseFamilies(LicenseSetFactory.LicenseFilter.ALL);
        for (ILicenseFamily family : APPROVED_FAMILIES) {
            unapproved.remove(family);
        }
        return unapproved;
    }

    @Test
    void testDefaultApprovedLicenses() {
        assertThat(APPROVED_FAMILIES.length).as("Approved license count mismatch").isEqualTo(NUMBER_OF_DEFAULT_ACCEPTED_LICENSES);
        for (ILicenseFamily family : APPROVED_FAMILIES) {
            TestingLicense license = new TestingLicense(family.getFamilyCategory().trim(), new TestingMatcher(), family);
            assertThat(licenseSetFactory.getApprovedLicensePredicate().test(license))
                    .as("Did not approve family " + family)
                    .isTrue();
        }
    }

    @Test
    void testDefaultUnApprovedLicenses() {
        SortedSet<ILicenseFamily> unapproved = getUnapprovedLicenseFamilies();

        for (ILicenseFamily family : unapproved) {
            TestingLicense license = new TestingLicense(family.getFamilyCategory().trim(), new TestingMatcher(), family);
            assertThat(licenseSetFactory.getApprovedLicensePredicate().test(license))
                    .as("Did not find unapproved family " + family)
                    .isFalse();
        }
    }

    @Test
    public void testUnknownFamily() {
        ILicenseFamily family = makeFamily("?????", "Unknown document");
        TestingLicense license = new TestingLicense(family.getFamilyCategory().trim(), new TestingMatcher(), family);

        assertThat(licenseSetFactory.getApprovedLicensePredicate().test(license))
                .as("Did not find unapproved family " + family)
                .isFalse();


        assertThat(licenseSetFactory.getApprovedLicensePredicate().test(UnknownLicense.INSTANCE))
                .as("Did not find UnknownLicense.INSTANCE")
                .isFalse();
    }

    @Test
    void testLicenseCategoryManipulation() {
        ILicenseFamily family = makeFamily("test", "Testing License Family");
        TestingLicense license = new TestingLicense(family.getFamilyCategory().trim(), new TestingMatcher(), family);

        assertThat(licenseSetFactory.getApprovedLicensePredicate().test(license))
                .as("Found unapproved family " + family)
                .isFalse();

        licenseSetFactory.approveLicenseCategory(family.getFamilyCategory());
        assertThat(licenseSetFactory.getApprovedLicensePredicate().test(license))
                .as("Did not find approved family " + family)
                .isTrue();

        licenseSetFactory.removeLicenseCategory(family.getFamilyCategory());
        assertThat(licenseSetFactory.getApprovedLicensePredicate().test(license))
                .as("Found unapproved family " + family)
                .isFalse();
    }

    @Test
    void testAddNewApprovedLicenseNoDefaults() {
        licenseSetFactory = new LicenseSetFactory();
        assertThat(licenseSetFactory.getLicenseFamilies(LicenseSetFactory.LicenseFilter.APPROVED).size())
                .isEqualTo(0);

        ILicenseFamily family = makeFamily("test", "Testing License Family");
        TestingLicense license = new TestingLicense(family.getFamilyCategory().trim(), new TestingMatcher(), family);
        assertThat(licenseSetFactory.getApprovedLicensePredicate().test(license))
                .as("Did find unapproved family " + family)
                .isFalse();

        licenseSetFactory.addLicense(license);
        licenseSetFactory.approveLicenseCategory(family.getFamilyCategory());
        assertThat(licenseSetFactory.getLicenseFamilies(LicenseSetFactory.LicenseFilter.APPROVED).size())
                .isEqualTo(1);
        assertThat(licenseSetFactory.getApprovedLicensePredicate().test(license))
                .as("Did not find approved family " + family)
                .isTrue();
    }

    @Test
    void testLicenseIDManipulation() {
        String licenseId = "customId";
        ILicenseFamily family = getUnapprovedLicenseFamilies().first();
        TestingLicense license = new TestingLicense(licenseId, new TestingMatcher(), family);

        assertThat(licenseSetFactory.getApprovedLicensePredicate().test(license))
                .as("Found unapproved id " + licenseId)
                .isFalse();

        licenseSetFactory.approveLicenseId(licenseId);
        assertThat(licenseSetFactory.getApprovedLicensePredicate().test(license))
                .as("Did not find approved id " + licenseId)
                .isTrue();

        licenseSetFactory.removeLicenseId(licenseId);
        assertThat(licenseSetFactory.getApprovedLicensePredicate().test(license))
                .as("Found unapproved id " + licenseId)
                .isFalse();

    }

//        @Test
//        public void testNonStandardDocumentsDoNotFailLicenseTests() {
//            Document.Type[] nonStandardDocuments = { Document.Type.NOTICE, Document.Type.ARCHIVE, Document.Type.BINARY };
//            for (Document.Type d : nonStandardDocuments) {
//                document = new TestingDocument("subject");
//                document.getMetaData().setDocumentType(d);
//                document.getMetaData().setApprovalPredicate(licenseSetFactory.getApprovedLicensePredicate());
//                for (ILicenseFamily family : APPROVED_FAMILIES) {
//                    TestingLicense license = new TestingLicense(family.getFamilyCategory().trim(), new TestingMatcher(), family);
//                    assertThat(document.getMetaData().isApproved(license)).isFalse();
//                }
//            }
//        }
//
//        @Test
//        public void testUnclassifiedDocumentsDoNotFailLicenseTests() {
//            document.getMetaData().setDocumentType(Document.Type.STANDARD);
//            policy.analyse(document);
//            assertApproval(false);
//        }

}
