/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rat.mp;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.rat.mp.RatTestHelpers.ensureRatReportIsCorrect;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import java.io.File;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.FileUtils;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.ReportConfigurationTest;
import org.apache.rat.ReporterTestUtils;
import org.apache.rat.api.Document;
import org.apache.rat.commandline.Arg;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.test.AbstractOptionsProvider;
import org.apache.rat.test.utils.Resources;
import org.apache.rat.testhelpers.TextUtils;
import org.apache.rat.testhelpers.XmlUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * Test case for the {@link RatCheckMojo} and {@link RatReportMojo}.
 */
public class RatCheckMojoTest { //extends BetterAbstractMojoTestCase {

    @TempDir
    static Path tempDir;

    private static XPath xPath = XPathFactory.newInstance().newXPath();

    @AfterAll
    static void preserveData() {
        AbstractOptionsProvider.preserveData(tempDir.toFile(), "unit");
    }


    private RatCheckMojo getMojo(File pomFile) throws IOException {
        try {
            final RatCheckMojo mojo = new OptionMojoTest.SimpleMojoTestcase() {
            }.getMojo(pomFile);
            Assertions.assertNotNull(mojo);
            return mojo;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(format("Unable to generate mojo for %s", pomFile), e);
        }
    }

    /**
     * Creates a new instance of {@link AbstractRatMojo}.
     *
     * @param testDir The directory, where to look for a pom.xml file.
     * copy location.
     * @return The configured Mojo.
     * @throws Exception An error occurred while creating the Mojo.
     */
    private RatCheckMojo newRatMojo(String testDir) throws Exception {
        Arg.reset();
        final File pomFile = Resources.getResourceFile(format("unit/%s/pom.xml", testDir));
        final File sourceDir = pomFile.getParentFile();
        final File baseDir = tempDir.resolve(testDir).toFile();
        FileUtils.copyDirectory(sourceDir, baseDir);

        RatCheckMojo mojo = getMojo(pomFile);
        assertThat(mojo).isNotNull();
        assertThat(mojo.getProject())
                .as("The mojo is missing its MavenProject, which will result in an NPE during RAT runs.")
                .isNotNull();

        File buildDirectory = new File(baseDir, "target");
        buildDirectory.mkdirs();
        final File ratTxtFile = new File(buildDirectory, "rat.txt");
        FileUtils.write(ratTxtFile, "", UTF_8); // Ensure the output file exists and is empty (rerunning the test will append)
        mojo.setOutputFile(ratTxtFile.getAbsolutePath());

        return mojo;
    }

    private String getDir(RatCheckMojo mojo) {
        return mojo.getProject().getBasedir().getAbsolutePath().replace("\\", "/") + "/";
    }

    /**
     * Runs a check, which should expose no problems.
     *
     * @throws Exception The test failed.
     */
    @Test
    void it1() throws Exception {
        final RatCheckMojo mojo = newRatMojo("it1");
        final File ratTxtFile = mojo.getRatTxtFile();

        ReportConfiguration config = mojo.getConfiguration();
        ReportConfigurationTest.validateDefault(config);

        mojo.execute();
        Map<ClaimStatistic.Counter, String> data = new HashMap<>();
        data.put(ClaimStatistic.Counter.ARCHIVES, "0");
        data.put(ClaimStatistic.Counter.APPROVED, "1");
        data.put(ClaimStatistic.Counter.BINARIES, "0");
        data.put(ClaimStatistic.Counter.DOCUMENT_TYPES, "2");
        data.put(ClaimStatistic.Counter.IGNORED, "1");
        data.put(ClaimStatistic.Counter.LICENSE_CATEGORIES, "1");
        data.put(ClaimStatistic.Counter.LICENSE_NAMES, "1");
        data.put(ClaimStatistic.Counter.NOTICES, "0");
        data.put(ClaimStatistic.Counter.STANDARDS, "1");
        data.put(ClaimStatistic.Counter.UNAPPROVED, "0");
        data.put(ClaimStatistic.Counter.UNKNOWN, "0");

        org.w3c.dom.Document document = XmlUtils.toDom(Files.newInputStream(ratTxtFile.toPath()));
        XPath xPath = XPathFactory.newInstance().newXPath();

        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            String xpath = String.format("/rat-report/statistics/statistic[@name='%s']", counter.displayName());
            Map<String, String> map = mapOf("approval", "true", "count", data.get(counter),
                    "description", counter.getDescription());
            XmlUtils.assertAttributes(document, xPath, xpath, map);
        }

        XmlUtils.assertAttributes(document, xPath, "/rat-report/resource[@name='/.bzrignore']",
                mapOf("mediaType", "application/octet-stream", "type", "IGNORED"));

        XmlUtils.assertAttributes(document, xPath, "/rat-report/resource[@name='/pom.xml']",
                mapOf("mediaType", "application/xml", "type", "STANDARD", "encoding", "ISO-8859-1"));
    }

    private static Map<String, String> mapOf(String... parts) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < parts.length; i += 2) {
            map.put(parts[i], parts[i + 1]);
        }
        return map;
    }

    /**
     * Runs a check, which should detect a problem.
     *
     * @throws Exception The test failed.
     */
    @Test
    void it2() throws Exception {
        final RatCheckMojo mojo = newRatMojo("it2");
        final File ratTxtFile = mojo.getRatTxtFile();
        final String[] expected = {
                "^Files with unapproved licenses\\s+\\*+\\s+\\Q/src.txt\\E\\s+",
                ReporterTestUtils.counterText(ClaimStatistic.Counter.NOTICES, 0, false),
                ReporterTestUtils.counterText(ClaimStatistic.Counter.BINARIES, 0, false),
                ReporterTestUtils.counterText(ClaimStatistic.Counter.ARCHIVES, 0, false),
                ReporterTestUtils.counterText(ClaimStatistic.Counter.STANDARDS, 2, false),
                ReporterTestUtils.counterText(ClaimStatistic.Counter.IGNORED, 0, false),
                ReporterTestUtils.apacheLicenseVersion2(1),
                ReporterTestUtils.unknownLicense(1),
                ReporterTestUtils.documentOut(false, Document.Type.STANDARD, "/src.txt") +
                        ReporterTestUtils.UNKNOWN_LICENSE,
                ReporterTestUtils.documentOut(true, Document.Type.STANDARD, "/pom.xml") +
                        ReporterTestUtils.APACHE_LICENSE
        };
        try {
            mojo.execute();
            fail("Expected RatCheckException");
        } catch (RatCheckException e) {
            final String msg = e.getMessage();
            assertThat(msg.contains(ratTxtFile.getName())).as(() -> format("report filename was not contained in '%s'", msg))
                    .isTrue();
            assertThat(msg.toUpperCase()).contains("UNAPPROVED EXCEEDED MINIMUM");

            ensureRatReportIsCorrect(ratTxtFile, expected, TextUtils.EMPTY);
        }
    }

    /**
     * Tests adding license headers.
     */
    @Test
    void it3() throws Exception {
        final RatCheckMojo mojo = newRatMojo("it3");
        final File ratTxtFile = mojo.getRatTxtFile();
        final String[] expected = {
                "^Files with unapproved licenses\\s+\\*+\\s+\\Q/src.apt\\E\\s+",
                ReporterTestUtils.counterText(ClaimStatistic.Counter.NOTICES, 0, false),
                ReporterTestUtils.counterText(ClaimStatistic.Counter.BINARIES, 0, false),
                ReporterTestUtils.counterText(ClaimStatistic.Counter.ARCHIVES, 0, false),
                ReporterTestUtils.counterText(ClaimStatistic.Counter.STANDARDS, 2, false),
                ReporterTestUtils.counterText(ClaimStatistic.Counter.IGNORED, 0, false),
                ReporterTestUtils.apacheLicenseVersion2(1),
                ReporterTestUtils.unknownLicense(1),
                ReporterTestUtils.documentOut(false, Document.Type.STANDARD, "/src.apt") +
                        ReporterTestUtils.UNKNOWN_LICENSE,
                ReporterTestUtils.documentOut(true, Document.Type.STANDARD, "/pom.xml") +
                        ReporterTestUtils.APACHE_LICENSE
        };

        ReportConfiguration config = mojo.getConfiguration();
        assertThat(config.isAddingLicenses()).as("should be adding licenses").isTrue();
        mojo.execute();
        org.w3c.dom.Document document = XmlUtils.toDom(Files.newInputStream(ratTxtFile.toPath()));

        XmlUtils.assertAttributes(document, xPath, "/rat-report/resource[@name='/pom.xml']", "type",
                "STANDARD");
        XmlUtils.assertAttributes(document, xPath, "/rat-report/resource[@name='/src.apt']", "type",
                "STANDARD");
        XmlUtils.assertIsPresent(document, xPath, "/rat-report/resource[@name='/src.apt']/license[@approval='false']");

        XmlUtils.assertAttributes(document, xPath, "/rat-report/resource[@name='/src.apt']", "type",
                "STANDARD");
        XmlUtils.assertIsPresent(document, xPath, "/rat-report/resource[@name='/src.apt.new']/license[@approval='true']");

        XmlUtils.assertAttributes(document, xPath, "/rat-report/statistics/documentType[@name='STANDARD']", "count",
                "3");

        for (Document.Type type : Document.Type.values()) {
            if (type == Document.Type.STANDARD) {
                XmlUtils.assertAttributes(document, xPath, "/rat-report/statistics/documentType[@name='STANDARD']", "count",
                        "3");
            } else {
                XmlUtils.assertIsNotPresent(document, xPath, format("/rat-report/statistics/documentType[@name='%s']", type));
            }
        }
    }

    /**
     * Tests defining licenses in configuration
     */
    @Test
    void it5() throws Exception {
        final RatCheckMojo mojo = newRatMojo("it5");
        final File ratTxtFile = mojo.getRatTxtFile();

        ReportConfiguration config = mojo.getConfiguration();
        assertThat(config.isAddingLicenses()).as("Should not be adding licenses").isFalse();
        assertThat(config.isAddingLicensesForced()).as("Should not be forcing licenses").isFalse();

        ReportConfigurationTest.validateDefaultApprovedLicenses(config);
        assertThat(config.getLicenseCategories(LicenseFilter.APPROVED)).doesNotContain(ILicenseFamily.makeCategory("YAL"));
        ReportConfigurationTest.validateDefaultLicenseFamilies(config, "YAL");
        assertThat(LicenseSetFactory.familySearch("YAL", config.getLicenseFamilies(LicenseFilter.ALL))).isNotNull();
        ReportConfigurationTest.validateDefaultLicenses(config, "MyLicense", "CpyrT", "RegxT", "SpdxT", "TextT",
                "Not", "All", "Any");
        assertThat(LicenseSetFactory.search("YAL", "MyLicense", config.getLicenses(LicenseFilter.ALL))).isPresent();
        try {
            mojo.execute();
            fail("Should have thrown exception");
        } catch (RatCheckException e) {
            assertThat(e.getMessage()).contains("UNAPPROVED");
        }

        Map<ClaimStatistic.Counter, String> data = new HashMap<>();
        data.put(ClaimStatistic.Counter.APPROVED, "0");
        data.put(ClaimStatistic.Counter.ARCHIVES, "0");
        data.put(ClaimStatistic.Counter.BINARIES, "0");
        data.put(ClaimStatistic.Counter.DOCUMENT_TYPES, "2");
        data.put(ClaimStatistic.Counter.IGNORED, "1");
        data.put(ClaimStatistic.Counter.LICENSE_CATEGORIES, "1");
        data.put(ClaimStatistic.Counter.LICENSE_NAMES, "4");
        data.put(ClaimStatistic.Counter.NOTICES, "0");
        data.put(ClaimStatistic.Counter.STANDARDS, "1");
        data.put(ClaimStatistic.Counter.UNAPPROVED, "4");
        data.put(ClaimStatistic.Counter.UNKNOWN, "0");

        org.w3c.dom.Document document = XmlUtils.toDom(Files.newInputStream(ratTxtFile.toPath()));
        XPath xPath = XPathFactory.newInstance().newXPath();

        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            String xpath = String.format("/rat-report/statistics/statistic[@name='%s']", counter.displayName());
            Map<String, String> map = mapOf("approval",
                    counter == ClaimStatistic.Counter.UNAPPROVED ? "false" : "true",
                    "count", data.get(counter),
                    "description", counter.getDescription());
            XmlUtils.assertAttributes(document, xPath, xpath, map);
        }

        XmlUtils.assertAttributes(document, xPath, "/rat-report/resource[@name='/.bzrignore']",
                mapOf("mediaType", "application/octet-stream", "type", "IGNORED"));

        XmlUtils.assertAttributes(document, xPath, "/rat-report/resource[@name='/pom.xml']",
                mapOf("mediaType", "application/xml", "type", "STANDARD", "encoding", "ISO-8859-1"));

        XmlUtils.assertAttributes(document, xPath, "/rat-report/resource[@name='/pom.xml']/license[@id='Any']",
                mapOf("approval", "false", "family", "YAL  ", "name", "Any testing"));
        XmlUtils.assertAttributes(document, xPath, "/rat-report/resource[@name='/pom.xml']/license[@id='MyLicense']",
                mapOf("approval", "false", "family", "YAL  ", "name", "Yet another license"));
        XmlUtils.assertAttributes(document, xPath, "/rat-report/resource[@name='/pom.xml']/license[@id='RegxT']",
                mapOf("approval", "false", "family", "YAL  ", "name", "Regex with tag"));
        XmlUtils.assertAttributes(document, xPath, "/rat-report/resource[@name='/pom.xml']/license[@id='TextT']",
                mapOf("approval", "false", "family", "YAL  ", "name", "Text with tag"));
    }

    /**
     * Runs a check, which should expose no problems.
     *
     * @throws Exception The test failed.
     */
    @Test
    void rat343() throws Exception {
        final RatCheckMojo mojo = newRatMojo("RAT-343");
        final File ratTxtFile = mojo.getRatTxtFile();
        // POM reports AL, BSD and CC BYas BSD because it contains the BSD and CC BY strings
        final String[] expected = {
                ReporterTestUtils.documentOut(false, Document.Type.STANDARD, "/pom.xml") +
                        ReporterTestUtils.APACHE_LICENSE +
                        ReporterTestUtils.licenseOut("BSD", "BSD") +
                        ReporterTestUtils.licenseOut("CC BY", "Creative Commons Attribution (Unapproved)"),
                ReporterTestUtils.counterText(ClaimStatistic.Counter.NOTICES, 0, false),
                ReporterTestUtils.counterText(ClaimStatistic.Counter.BINARIES, 0, false),
                ReporterTestUtils.counterText(ClaimStatistic.Counter.ARCHIVES, 0, false),
                ReporterTestUtils.counterText(ClaimStatistic.Counter.STANDARDS, 1, false),
                ReporterTestUtils.counterText(ClaimStatistic.Counter.IGNORED, 0, false),
                ReporterTestUtils.apacheLicenseVersion2(1),
                "^BSD: 1 ",
                "^Creative Commons Attribution: 1 ",
        };
        final String[] notExpected = {
                "^Unknown License:"
        };

        ReportConfiguration config = mojo.getConfiguration();
        // validate configuration
        assertThat(config.isAddingLicenses()).isFalse();
        assertThat(config.isAddingLicensesForced()).isFalse();
        assertThat(config.getCopyrightMessage()).isNull();
        assertThat(config.getStyleSheet()).withFailMessage("Stylesheet should not be null").isNotNull();

        ReportConfigurationTest.validateDefaultApprovedLicenses(config, 1);
        ReportConfigurationTest.validateDefaultLicenseFamilies(config, "BSD", "CC BY");
        ReportConfigurationTest.validateDefaultLicenses(config, "BSD", "CC BY");

        mojo.execute();
        ensureRatReportIsCorrect(ratTxtFile, expected, notExpected);
    }

    /**
     * Tests verifying gitignore parsing
     */
    @Test
    void rat335() throws Exception {
        final RatCheckMojo mojo = newRatMojo("RAT-335");
        final File ratTxtFile = mojo.getRatTxtFile();
        try {
            mojo.execute();
            fail("Expected RatCheckException");
        } catch (RatCheckException e) {
            final String msg = e.getMessage();
            assertThat(msg).contains(ratTxtFile.getName());
            assertThat(msg).contains("UNAPPROVED exceeded minimum");

            Map<ClaimStatistic.Counter, String> data = new HashMap<>();
            data.put(ClaimStatistic.Counter.APPROVED, "1");
            data.put(ClaimStatistic.Counter.ARCHIVES, "0");
            data.put(ClaimStatistic.Counter.BINARIES, "0");
            data.put(ClaimStatistic.Counter.DOCUMENT_TYPES, "3");
            data.put(ClaimStatistic.Counter.IGNORED, "6");
            data.put(ClaimStatistic.Counter.LICENSE_CATEGORIES, "2");
            data.put(ClaimStatistic.Counter.LICENSE_NAMES, "2");
            data.put(ClaimStatistic.Counter.NOTICES, "1");
            data.put(ClaimStatistic.Counter.STANDARDS, "5");
            data.put(ClaimStatistic.Counter.UNAPPROVED, "4");
            data.put(ClaimStatistic.Counter.UNKNOWN, "4");

            org.w3c.dom.Document document = XmlUtils.toDom(Files.newInputStream(ratTxtFile.toPath()));

            for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
                String xpath = String.format("/rat-report/statistics/statistic[@name='%s']", counter.displayName());
                Map<String, String> map = mapOf("approval",
                        counter == ClaimStatistic.Counter.UNAPPROVED ? "false" : "true",
                        "count", data.get(counter),
                        "description", counter.getDescription());
                XmlUtils.assertAttributes(document, xPath, xpath, map);
            }

            // license categories
            XmlUtils.assertAttributes(document, xPath, "/rat-report/statistics/licenseCategory[@name='?????']",
                    mapOf("count", "4"));

            XmlUtils.assertAttributes(document, xPath, "/rat-report/statistics/licenseCategory[@name='AL   ']",
                    mapOf("count", "1"));

            // license names
            XmlUtils.assertAttributes(document, xPath, "/rat-report/statistics/licenseName[@name='Apache License Version 2.0']",
                    mapOf("count", "1"));

            XmlUtils.assertAttributes(document, xPath, "/rat-report/statistics/licenseName[@name='Unknown license']",
                    mapOf("count", "4"));

            // Document types
            XmlUtils.assertAttributes(document, xPath, "/rat-report/statistics/documentType[@name='IGNORED']",
                    mapOf("count", "6"));

            XmlUtils.assertAttributes(document, xPath, "/rat-report/statistics/documentType[@name='NOTICE']",
                    mapOf("count", "1"));

            XmlUtils.assertAttributes(document, xPath, "/rat-report/statistics/documentType[@name='STANDARD']",
                    mapOf("count", "5"));

            List<String> ignoredFiles = new ArrayList<>(Arrays.asList(
                    "/dir1/dir1.txt",
                    "/dir1/.gitignore",
                    "/dir2/dir2.md",
                    "/dir3/dir3.log",
                    "/.gitignore",
                    "/root.md"));

            NodeList nodeList = XmlUtils.getNodeList(document, xPath, "/rat-report/resource[@type='IGNORED']");
            for (int i = 0; i < nodeList.getLength(); i++) {
                NamedNodeMap attr = nodeList.item(i).getAttributes();
                String s = attr.getNamedItem("name").getNodeValue();
                assertThat(ignoredFiles).contains(s);
                ignoredFiles.remove(s);
            }
            assertThat(ignoredFiles).isEmpty();
        }
    }

    /**
     * Tests verifying gitignore parsing under a special edge case condition
     * The problem occurs when '/foo.md' is to be ignored and a file with that name exists
     * in a directory which is the project base directory twice concatenated.
     * So for this test we must create such a file which is specific for the current
     * working directory.
     */
    @Test
    void rat362() throws Exception {
        final RatCheckMojo mojo = newRatMojo("RAT-362");
        final File ratTxtFile = mojo.getRatTxtFile();
        try {
            mojo.execute();
            fail("Expected RatCheckException");
        } catch (RatCheckException e) {
            final String msg = e.getMessage();
            assertThat(msg).contains(ratTxtFile.getName());
            assertThat(msg).contains("UNAPPROVED exceeded minimum");

            org.w3c.dom.Document document = XmlUtils.toDom(Files.newInputStream(ratTxtFile.toPath()));
            // Document types
            XmlUtils.assertAttributes(document, xPath, "/rat-report/statistics/documentType[@name='IGNORED']",
                    "count", "2");

            XmlUtils.assertAttributes(document, xPath, "/rat-report/resource[@name='/bar.md']",
                    "type", "STANDARD");
            XmlUtils.assertAttributes(document, xPath, "/rat-report/resource[@name='/foo.md']",
                    "type", "IGNORED");
        }
    }

    /**
     * Tests implicit excludes apply to submodules too
     */
    @Test
    void rat107() throws Exception {
        final RatCheckMojo mojo = newRatMojo("RAT-107");
        final File ratTxtFile = mojo.getRatTxtFile();
        final String[] expected = {};
        final String[] notExpected = {};
        //setVariableValueToObject(mojo, "excludeSubProjects", Boolean.FALSE);
        mojo.setInputExcludeParsedScm("MAVEN");
        mojo.setInputExcludeParsedScm("idea");
        mojo.setInputExcludeParsedScm("eclipse");
        mojo.execute();

        ensureRatReportIsCorrect(ratTxtFile, expected, notExpected);
    }
}
