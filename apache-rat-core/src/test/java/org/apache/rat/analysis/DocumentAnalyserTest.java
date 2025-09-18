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
package org.apache.rat.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.rat.Defaults;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.api.Document;
import org.apache.rat.document.DocumentAnalyser;
import org.apache.rat.document.RatDocumentAnalysisException;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.testhelpers.TestingLicense;
import org.apache.rat.testhelpers.TestingDocument;
import org.apache.rat.testhelpers.TestingMatcher;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests the Default policy implementation.
 */
public class DocumentAnalyserTest {

    private Document document;

    private void assertApproval(boolean state) {
        if (state) {
            assertThat(document.getMetaData().approvedLicenses().findAny()).isPresent();
        } else {
            assertThat(document.getMetaData().approvedLicenses().findAny()).isNotPresent();
        }
    }

    private void setMetadata(Document document, ILicenseFamily family) {
        document.getMetaData().reportOnLicense(new TestingLicense(family.getFamilyCategory().trim(), new TestingMatcher(), family));
    }

    private static ILicenseFamily makeFamily(String category, String name) {
        return ILicenseFamily.builder().setLicenseFamilyCategory(category).setLicenseFamilyName(name).build();
    }

    private Collection<String> asCategories(Collection<ILicenseFamily> families) {
        return families.stream().map(ILicenseFamily::getFamilyCategory).collect(Collectors.toList());
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("defaultAnalyserTestData")
    void licenseSetFactoryTest(String name, ReportConfiguration configuration,
                             Map<LicenseFilter, Collection<ILicenseFamily>> licenseFamilies,
                             Map<LicenseFilter, Collection<ILicense>> licenses
    ) {
        DefaultLog.getInstance().log(Log.Level.DEBUG, "Running " + name);
        LicenseSetFactory factory = configuration.getLicenseSetFactory();

        Collection<String> categories = asCategories(licenseFamilies.get(LicenseFilter.APPROVED));
        assertThat(factory.getLicenseCategories(LicenseFilter.APPROVED)).containsExactlyInAnyOrderElementsOf(categories);
        categories = asCategories(licenseFamilies.get(LicenseFilter.ALL));
        assertThat(factory.getLicenseCategories(LicenseFilter.ALL)).containsExactlyInAnyOrderElementsOf(categories);
        assertThat(factory.getLicenseCategories(LicenseFilter.NONE)).isEmpty();

        assertThat(factory.getLicenseFamilies(LicenseFilter.APPROVED)).containsExactlyInAnyOrderElementsOf(licenseFamilies.get(LicenseFilter.APPROVED));
        assertThat(factory.getLicenseFamilies(LicenseFilter.ALL)).containsExactlyInAnyOrderElementsOf(licenseFamilies.get(LicenseFilter.ALL));
        assertThat(factory.getLicenseFamilies(LicenseFilter.NONE)).isEmpty();

        assertThat(factory.getLicenses(LicenseFilter.APPROVED)).containsExactlyInAnyOrderElementsOf(licenses.get(LicenseFilter.APPROVED));
        assertThat(factory.getLicenses(LicenseFilter.ALL)).containsExactlyInAnyOrderElementsOf(licenses.get(LicenseFilter.ALL));
        assertThat(factory.getLicenses(LicenseFilter.NONE)).isEmpty();
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("defaultAnalyserTestData")
    void analyserTest(String name, ReportConfiguration configuration,
                             Map<LicenseFilter, Collection<ILicenseFamily>> licenseFamilies,
                             Map<LicenseFilter, Collection<ILicense>> licenses
    ) throws RatDocumentAnalysisException {
        DefaultLog.getInstance().log(Log.Level.DEBUG, "Running " + name);
        DocumentAnalyser analyser = AnalyserFactory.createConfiguredAnalyser(configuration);

        // verify approved license families report approved.
        for (ILicenseFamily family : licenseFamilies.get(LicenseFilter.APPROVED)) {
            document = new TestingDocument(() -> new ByteArrayInputStream("Hello World".getBytes(StandardCharsets.UTF_8)), "subject");
            setMetadata(document, family);
            analyser.analyse(document);
            assertApproval(true);
        }

        // verify licenses report approved as per the licenses argument
        for (ILicense license : licenses.get(LicenseFilter.ALL)) {
            document = new TestingDocument(() -> new ByteArrayInputStream("Hello World".getBytes(StandardCharsets.UTF_8)), "subject");
            document.getMetaData().reportOnLicense(license);
            analyser.analyse(document);
            assertApproval(licenses.get(LicenseFilter.APPROVED).contains(license));
        }

        // verify that the unknown license is not approved.
        document = new TestingDocument(() -> new ByteArrayInputStream("Hello World".getBytes(StandardCharsets.UTF_8)), "subject");
        setMetadata(document, makeFamily("?????", "Unknown document"));
        analyser.analyse(document);
        assertApproval(false);

        // verify that the standard document without a license detected is not approved.
        document = new TestingDocument(() -> new ByteArrayInputStream("Hello World".getBytes(StandardCharsets.UTF_8)), "subject");
        document.getMetaData().setDocumentType(Document.Type.STANDARD);
        analyser.analyse(document);
        assertApproval(false);
    }

    private static List<ILicenseFamily> defaultApprovedLicenseFamilies() {
        return Arrays.asList(
                ILicenseFamily.builder().setLicenseFamilyCategory("AL").setLicenseFamilyName("Apache License").build(),
                ILicenseFamily.builder().setLicenseFamilyCategory("CDDL1").setLicenseFamilyName("COMMON DEVELOPMENT AND DISTRIBUTION LICENSE Version 1.0").build(),
                ILicenseFamily.builder().setLicenseFamilyCategory("MIT").setLicenseFamilyName("The MIT License").build(),
                ILicenseFamily.builder().setLicenseFamilyCategory("OASIS").setLicenseFamilyName("OASIS Open License").build(),
                ILicenseFamily.builder().setLicenseFamilyCategory("W3C").setLicenseFamilyName("W3C Software Copyright").build(),
                ILicenseFamily.builder().setLicenseFamilyCategory("W3CD").setLicenseFamilyName("W3C Document Copyright").build(),
                ILicenseFamily.builder().setLicenseFamilyCategory("BSD-3").setLicenseFamilyName("BSD 3 clause").build()
        );
    }

    private static List<ILicenseFamily> defaultAllLicenseFamilies() {
        List<ILicenseFamily> result = new ArrayList<>(defaultApprovedLicenseFamilies());
        result.add(ILicenseFamily.builder().setLicenseFamilyCategory("GPL").setLicenseFamilyName("GNU General Public License family").build());
        return result;
    }

    private static List<ILicense> defaultApprovedLicenses() {
        return Arrays.asList(
                new TestingLicense("AL", "AL1.0"),
                new TestingLicense("AL", "AL1.1"),
                new TestingLicense("AL", "AL2.0"),
                new TestingLicense("BSD-3", "BSD-3"),
                new TestingLicense("BSD-3", "DOJO"),
                new TestingLicense("BSD-3", "TMF"),
                new TestingLicense("CDDL1", "CDDL1"),
                new TestingLicense("CDDL1", "ILLUMOS"),
                new TestingLicense("MIT", "MIT"),
                new TestingLicense("OASIS", "OASIS"),
                new TestingLicense("W3C", "W3C"),
                new TestingLicense("W3CD", "W3CD")
        );
    }

    private static List<ILicense> defaultAllLicenses() {
        List<ILicense> result = new ArrayList<>(defaultApprovedLicenses());
        result.addAll(Arrays.asList(new TestingLicense("GPL", "GPL1"),
                new TestingLicense("GPL", "GPL2"),
                new TestingLicense("GPL", "GPL3")));
        return result;
    }

    private static Stream<Arguments> defaultAnalyserTestData() {
        List<Arguments> lst = new ArrayList<>();
        Defaults defaults = Defaults.builder().build();

        ReportConfiguration config = new ReportConfiguration();
        config.setFrom(defaults);

        // default setup
        Map<LicenseFilter, Collection<ILicenseFamily>> licenseFamilies = new HashMap<>();
        licenseFamilies.put(LicenseFilter.APPROVED, defaultApprovedLicenseFamilies());
        licenseFamilies.put(LicenseFilter.ALL, defaultAllLicenseFamilies());

        Map<LicenseFilter, Collection<ILicense>> licenses = new HashMap<>();
        licenses.put(LicenseFilter.APPROVED, defaultApprovedLicenses());
        licenses.put(LicenseFilter.ALL, defaultAllLicenses());

        lst.add(Arguments.of("default", config, licenseFamilies, licenses));

        // GPL approved license id
        config = new ReportConfiguration();
        config.setFrom(defaults);
        config.addApprovedLicenseId("GPL1");

        licenseFamilies = new HashMap<>();
        licenseFamilies.put(LicenseFilter.APPROVED, defaultApprovedLicenseFamilies());
        licenseFamilies.put(LicenseFilter.ALL, defaultAllLicenseFamilies());

        licenses = new HashMap<>();
        List<ILicense> approvedLicenses = new ArrayList<>(defaultApprovedLicenses());
        approvedLicenses.add(new TestingLicense("GPL", "GPL1"));
        licenses.put(LicenseFilter.APPROVED, approvedLicenses);
        licenses.put(LicenseFilter.ALL, defaultAllLicenses());

        lst.add(Arguments.of("GPL1 id added ", config, licenseFamilies, licenses));

        // GPL family added
        config = new ReportConfiguration();
        config.setFrom(defaults);
        config.addApprovedLicenseCategory(ILicenseFamily.makeCategory("GPL"));

        licenseFamilies = new HashMap<>();
        licenseFamilies.put(LicenseFilter.APPROVED, defaultAllLicenseFamilies());
        licenseFamilies.put(LicenseFilter.ALL, defaultAllLicenseFamilies());

        licenses = new HashMap<>();
        approvedLicenses = new ArrayList<>(defaultApprovedLicenses());
        approvedLicenses.addAll(Arrays.asList(new TestingLicense("GPL", "GPL1"),
                new TestingLicense("GPL", "GPL2"),
                new TestingLicense("GPL", "GPL3")));
        licenses.put(LicenseFilter.APPROVED, approvedLicenses);
        licenses.put(LicenseFilter.ALL, defaultAllLicenses());

        lst.add(Arguments.of("GPL family added", config, licenseFamilies, licenses));

        // Add new license
        config = new ReportConfiguration();
        config.setFrom(defaults);
        ILicense newLicense = new TestingLicense("FAM", "Testing");
        config.addLicense(newLicense);

        licenseFamilies = new HashMap<>();
        licenseFamilies.put(LicenseFilter.APPROVED, defaultApprovedLicenseFamilies());
        ArrayList<ILicenseFamily> allFamilies = new ArrayList<>(defaultAllLicenseFamilies());
        allFamilies.add(newLicense.getLicenseFamily());
        licenseFamilies.put(LicenseFilter.ALL, allFamilies);

        licenses = new HashMap<>();
        licenses.put(LicenseFilter.APPROVED, defaultApprovedLicenses());
        ArrayList<ILicense> allLicenses = new ArrayList<>(defaultAllLicenses());
        allLicenses.add(newLicense);
        licenses.put(LicenseFilter.ALL, allLicenses);

        lst.add(Arguments.of("Testing license added", config, licenseFamilies, licenses));

        // Add new license approved by id
        config = new ReportConfiguration();
        config.setFrom(defaults);
        newLicense = new TestingLicense("FAM", "Testing");
        config.addLicense(newLicense);
        config.addApprovedLicenseId(newLicense.getId());

        licenseFamilies = new HashMap<>();
        licenseFamilies.put(LicenseFilter.APPROVED, defaultApprovedLicenseFamilies());
        allFamilies = new ArrayList<>(defaultAllLicenseFamilies());
        allFamilies.add(newLicense.getLicenseFamily());
        licenseFamilies.put(LicenseFilter.ALL, allFamilies);

        licenses = new HashMap<>();

        approvedLicenses = new ArrayList<>(defaultApprovedLicenses());
        approvedLicenses.add(newLicense);
        licenses.put(LicenseFilter.APPROVED, approvedLicenses);
        allLicenses = new ArrayList<>(defaultAllLicenses());
        allLicenses.add(newLicense);
        licenses.put(LicenseFilter.ALL, allLicenses);

        lst.add(Arguments.of("Testing license id approved", config, licenseFamilies, licenses));

        // Add new license approved by family
        config = new ReportConfiguration();
        config.setFrom(defaults);
        newLicense = new TestingLicense("FAM", "Testing");
        config.addLicense(newLicense);
        config.addApprovedLicenseCategory(newLicense.getLicenseFamily());

        licenseFamilies = new HashMap<>();
        ArrayList<ILicenseFamily> approvedFamilies = new ArrayList<>(defaultApprovedLicenseFamilies());
        approvedFamilies.add(newLicense.getLicenseFamily());
        licenseFamilies.put(LicenseFilter.APPROVED, approvedFamilies);
        allFamilies = new ArrayList<>(defaultAllLicenseFamilies());
        allFamilies.add(newLicense.getLicenseFamily());
        licenseFamilies.put(LicenseFilter.ALL, allFamilies);

        licenses = new HashMap<>();
        approvedLicenses = new ArrayList<>(defaultApprovedLicenses());
        approvedLicenses.add(newLicense);
        licenses.put(LicenseFilter.APPROVED, approvedLicenses);
        allLicenses = new ArrayList<>(defaultAllLicenses());
        allLicenses.add(newLicense);
        licenses.put(LicenseFilter.ALL, allLicenses);

        lst.add(Arguments.of("Testing license family approved", config, licenseFamilies, licenses));

        return lst.stream();
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("nonStandardDocumentData")
    void testNonStandardDocumentsDoNotFailLicenseTests(Document.Type expected, Document document) throws RatDocumentAnalysisException {
        Defaults defaults = Defaults.builder().build();
        ReportConfiguration config = new ReportConfiguration();
        config.setFrom(defaults);

        DocumentAnalyser analyser = AnalyserFactory.createConfiguredAnalyser(config);
        analyser.analyse(document);
        assertThat(document.getMetaData().getDocumentType()).isEqualTo(expected);
        assertThat(document.getMetaData().licenses()).hasSize(0);
    }
    
    private static Stream<Arguments> nonStandardDocumentData() {
        List<Arguments> lst = new ArrayList<>();

        lst.add(Arguments.of(Document.Type.NOTICE, new TestingDocument(() -> new ByteArrayInputStream("Hello World".getBytes(StandardCharsets.UTF_8)), "NOTICE")));
        byte[] zipMagic = new byte[]{0x50, 0x4B, 0x03, 0x06};
        lst.add(Arguments.of(Document.Type.ARCHIVE, new TestingDocument(() -> new ByteArrayInputStream(zipMagic), "example.zip")));
        byte[] gifMagic = new byte[]{0x47, 0x49, 0x46, 0x38, 0x37, 0x61};
        lst.add(Arguments.of(Document.Type.BINARY, new TestingDocument(() -> new ByteArrayInputStream(gifMagic), "example.gif")));
        lst.add(Arguments.of(Document.Type.IGNORED, new TestingDocument(() -> new ByteArrayInputStream("THIS FILE IS AUTOMATICALLY GENERATED".getBytes(StandardCharsets.UTF_8)), "example.ignored")));
        return lst.stream();
    }
}
