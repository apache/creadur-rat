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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.rat.mp.RatTestHelpers.ensureRatReportIsCorrect;
import static org.apache.rat.mp.RatTestHelpers.getSourceDirectory;
import static org.apache.rat.mp.RatTestHelpers.newArtifactFactory;
import static org.apache.rat.mp.RatTestHelpers.newArtifactRepository;
import static org.apache.rat.mp.RatTestHelpers.newSiteRenderer;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import java.io.FileInputStream;
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
import org.apache.rat.testhelpers.TextUtils;
import org.apache.rat.testhelpers.XmlUtils;
import org.junit.Ignore;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * Test case for the {@link RatCheckMojo} and {@link RatReportMojo}.
 */
public class RatCheckMojoTest extends BetterAbstractMojoTestCase {
    /**
     * Creates a new instance of {@link RatCheckMojo}.
     *
     * @param pDir The directory, where to look for a pom.xml file.
     * @return The configured Mojo.
     * @throws Exception An error occurred while creating the Mojo.
     */
    private RatCheckMojo newRatCheckMojo(String pDir) throws Exception {
        Arg.reset();
        return (RatCheckMojo) newRatMojo(pDir, "check", false);
    }

    /**
     * Creates a new instance of {@link AbstractRatMojo}.
     *
     * @param pDir The directory, where to look for a pom.xml file.
     * @param pGoal The goal, which the Mojo must implement.
     * @param pCreateCopy if {@code true} copy the directory contents and return the
     * copy location.
     * @return The configured Mojo.
     * @throws Exception An error occurred while creating the Mojo.
     */
    private AbstractRatMojo newRatMojo(String pDir, String pGoal, boolean pCreateCopy) throws Exception {
        final File baseDir = new File(getBasedir());
        final File testBaseDir = getSourceDirectory(getBasedir(), pDir, pCreateCopy, baseDir);
        final File testPom = new File(testBaseDir, "pom.xml");
        final File buildDirectory = new File(new File(baseDir, "target/test"), pDir);
        AbstractRatMojo mojo = (AbstractRatMojo) lookupConfiguredMojo(testPom, pGoal);
        assertNotNull(mojo);

        assertNotNull("The mojo is missing its MavenProject, which will result in an NPE during RAT runs.",
                mojo.getProject());

        if (mojo instanceof RatReportMojo) {
            setVariableValueToObject(mojo, "localRepository", newArtifactRepository(getContainer()));
            setVariableValueToObject(mojo, "factory", newArtifactFactory());
            setVariableValueToObject(mojo, "siteRenderer", newSiteRenderer(getContainer()));
        } else if (mojo instanceof RatCheckMojo) {
            final File ratTxtFile = new File(buildDirectory, "rat.txt");
            FileUtils.write(ratTxtFile, "", UTF_8); // Ensure the output file exists and is empty (rerunning the test will append)
            mojo.setOutputFile(ratTxtFile.getAbsolutePath());
        }
        return mojo;
    }

    private String getDir(RatCheckMojo mojo) {
        return mojo.getProject().getBasedir().getAbsolutePath().replace("\\","/") + "/";
    }
    /**
     * Runs a check, which should expose no problems.
     *
     * @throws Exception The test failed.
     */
    public void testIt1() throws Exception {

        final RatCheckMojo mojo = newRatCheckMojo("it1");
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


        org.w3c.dom.Document document = XmlUtils.toDom(new FileInputStream(ratTxtFile));
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
        for (int i=0; i<parts.length; i+=2) {
            map.put(parts[i], parts[i+1]);
        }
        return map;
    }

    /**
     * Runs a check, which should detect a problem.
     *
     * @throws Exception The test failed.
     */
    public void testIt2() throws Exception {

        final RatCheckMojo mojo = newRatCheckMojo("it2");
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
            assertTrue("report filename was not contained in '" + msg + "'", msg.contains(ratTxtFile.getName()));
            assertFalse("no null allowed in '" + msg + "'", (msg.toUpperCase().contains("NULL")));
            ensureRatReportIsCorrect(ratTxtFile, expected, TextUtils.EMPTY);
        }
    }

    /**
     * Tests adding license headers.
     */
    public void testIt3() throws Exception {
        final RatCheckMojo mojo = (RatCheckMojo) newRatMojo("it3", "check", true);
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
        assertTrue("should be adding licenses", config.isAddingLicenses());

        mojo.execute();

        ensureRatReportIsCorrect(ratTxtFile, expected, TextUtils.EMPTY);
    }

    /**
     * Tests defining licenses in configuration
     */
    public void testIt5() throws Exception {
        final RatCheckMojo mojo = (RatCheckMojo) newRatMojo("it5", "check", true);
        final File ratTxtFile = mojo.getRatTxtFile();

        ReportConfiguration config = mojo.getConfiguration();
        assertFalse("Should not be adding licenses", config.isAddingLicenses());
        assertFalse("Should not be forcing licenses", config.isAddingLicensesForced());

        ReportConfigurationTest.validateDefaultApprovedLicenses(config);
        assertFalse(config.getLicenseCategories(LicenseFilter.APPROVED).contains(ILicenseFamily.makeCategory("YAL")));
        ReportConfigurationTest.validateDefaultLicenseFamilies(config, "YAL");
        assertNotNull(LicenseSetFactory.familySearch("YAL", config.getLicenseFamilies(LicenseFilter.ALL)));
        ReportConfigurationTest.validateDefaultLicenses(config, "MyLicense", "CpyrT", "RegxT", "SpdxT", "TextT",
                "Not", "All", "Any");
        assertTrue(LicenseSetFactory.search("YAL", "MyLicense", config.getLicenses(LicenseFilter.ALL)).isPresent());
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


        org.w3c.dom.Document document = XmlUtils.toDom(new FileInputStream(ratTxtFile));
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
    public void testRAT_343() throws Exception {
        final RatCheckMojo mojo = newRatCheckMojo("RAT-343");
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
    public void /*test*/RAT_335GitIgnoreParsing() throws Exception {
        final RatCheckMojo mojo = newRatCheckMojo("RAT-335-GitIgnore");
        final File ratTxtFile = mojo.getRatTxtFile();
//        final String[] expected = {
//                ReporterTestUtils.counterText(ClaimStatistic.Counter.NOTICES, 1, false),
//                ReporterTestUtils.counterText(ClaimStatistic.Counter.BINARIES, 0, false),
//                ReporterTestUtils.counterText(ClaimStatistic.Counter.ARCHIVES, 0, false),
//                ReporterTestUtils.counterText(ClaimStatistic.Counter.STANDARDS, 6, false),
//                ReporterTestUtils.counterText(ClaimStatistic.Counter.IGNORED, 0, false),
//                ReporterTestUtils.counterText(ClaimStatistic.Counter.UNKNOWN, 4, false),
//                ReporterTestUtils.apacheLicenseVersion2(2),
//                ReporterTestUtils.unknownLicense(4),
//                ReporterTestUtils.documentOut(true, Document.Type.STANDARD, "/pom.xml") +
//                        ReporterTestUtils.APACHE_LICENSE,
//                ReporterTestUtils.documentOut(false, Document.Type.STANDARD, "/dir1/dir1.md") +
//                        ReporterTestUtils.UNKNOWN_LICENSE,
//                ReporterTestUtils.documentOut(false, Document.Type.STANDARD, "/dir2/dir2.txt") +
//                        ReporterTestUtils.UNKNOWN_LICENSE,
//                ReporterTestUtils.documentOut(false, Document.Type.STANDARD, "/dir3/file3.log") +
//                        ReporterTestUtils.UNKNOWN_LICENSE,
//        };
        try {
            mojo.execute();
            fail("Expected RatCheckException");
        } catch (RatCheckException e) {
            final String msg = e.getMessage();
            assertTrue("report filename was not contained in '" + msg + "'", msg.contains(ratTxtFile.getName()));
            assertFalse("no null allowed in '" + msg + "'", (msg.toUpperCase().contains("NULL")));

            Map<ClaimStatistic.Counter, String> data = new HashMap<>();
            data.put(ClaimStatistic.Counter.APPROVED, "2");
            data.put(ClaimStatistic.Counter.ARCHIVES, "0");
            data.put(ClaimStatistic.Counter.BINARIES, "0");
            data.put(ClaimStatistic.Counter.DOCUMENT_TYPES, "3");
            data.put(ClaimStatistic.Counter.IGNORED, "6");
            data.put(ClaimStatistic.Counter.LICENSE_CATEGORIES, "2");
            data.put(ClaimStatistic.Counter.LICENSE_NAMES, "2");
            data.put(ClaimStatistic.Counter.NOTICES, "1");
            data.put(ClaimStatistic.Counter.STANDARDS, "6");
            data.put(ClaimStatistic.Counter.UNAPPROVED, "4");
            data.put(ClaimStatistic.Counter.UNKNOWN, "4");


            org.w3c.dom.Document document = XmlUtils.toDom(new FileInputStream(ratTxtFile));
            XPath xPath = XPathFactory.newInstance().newXPath();

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
                        mapOf("count", "4" ));

            XmlUtils.assertAttributes(document, xPath, "/rat-report/statistics/licenseCategory[@name='AL   ']",
                    mapOf("count", "2" ));

            // license names
            XmlUtils.assertAttributes(document, xPath, "/rat-report/statistics/licenseName[@name='Apache License Version 2.0']",
                    mapOf("count", "2" ));

            XmlUtils.assertAttributes(document, xPath, "/rat-report/statistics/licenseName[@name='Unknown license']",
                    mapOf("count", "4" ));


            // Document types
            XmlUtils.assertAttributes(document, xPath, "/rat-report/statistics/documentType[@name='IGNORED']",
                    mapOf("count", "6" ));

            XmlUtils.assertAttributes(document, xPath, "/rat-report/statistics/documentType[@name='NOTICE']",
                    mapOf("count", "1" ));

            XmlUtils.assertAttributes(document, xPath, "/rat-report/statistics/documentType[@name='STANDARD']",
                    mapOf("count", "6" ));

            List<String> ignoredFiles = new ArrayList<>(Arrays.asList(
                    "/dir1/dir1.txt",
                    "/dir1/file1.log",
                    "/dir1/.gitignore",
                    "/dir2/dir2.md",
                    "/dir3/dir3.log",
                    "/.gitignore",
                    "/root.md"));

            NodeList nodeList = XmlUtils.getNodeList(document, xPath, "/rat-report/resource[@type='IGNORED']");
            for (int i=0;i< nodeList.getLength(); i++) {
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
    public void /*test*/RAT362GitIgnore() throws Exception {
        final RatCheckMojo mojo = newRatCheckMojo("RAT-362-GitIgnore");
        final File ratTxtFile = mojo.getRatTxtFile();
        final String dirStr = getDir(mojo);

        if (dirStr.contains(":")) {
            // The problem this is testing for cannot happen if there is
            // a Windows drive letter in the name of the directory.
            // Any duplication of a ':' will make it all fail always.
            // So there is no point in continuing this test.
            return;
        }
        File dir = new File(dirStr);

        // Make the target directory for the test file
        assertTrue(dir.isDirectory());

        // Create the test file with a content on which it must fail
        File barFile = new File(dir, "bar.md");
        assertThat(barFile).exists();
        final String[] expected = {
                ReporterTestUtils.counterText(ClaimStatistic.Counter.NOTICES, 0, false),
                ReporterTestUtils.counterText(ClaimStatistic.Counter.BINARIES, 0, false),
                ReporterTestUtils.counterText(ClaimStatistic.Counter.ARCHIVES, 0, false),
                ReporterTestUtils.counterText(ClaimStatistic.Counter.STANDARDS, 3, false),
                ReporterTestUtils.counterText(ClaimStatistic.Counter.IGNORED, 0, false),
                ReporterTestUtils.apacheLicenseVersion2(2),
                ReporterTestUtils.unknownLicense(1),
                ReporterTestUtils.documentOut(false, Document.Type.STANDARD, "/bar.md") +
                        ReporterTestUtils.UNKNOWN_LICENSE
        };
        try {
            mojo.execute();
            fail("Expected RatCheckException: This check should have failed on the invalid test file");
        } catch (RatCheckException e) {
            final String msg = e.getMessage();
            assertTrue("report filename was not contained in '" + msg + "'", msg.contains(ratTxtFile.getName()));
            assertFalse("no null allowed in '" + msg + "'", (msg.toUpperCase().contains("NULL")));
            ensureRatReportIsCorrect(ratTxtFile, expected, TextUtils.EMPTY);
        }
    }

    /**
    * Tests implicit excludes apply to submodules too
    */
   public void testRAT_107() throws Exception {
       final RatCheckMojo mojo = (RatCheckMojo) newRatMojo("RAT-107", "check", true);
       final File ratTxtFile = mojo.getRatTxtFile();
       final String[] expected = {};
       final String[] notExpected = {};
       setVariableValueToObject(mojo, "excludeSubProjects", Boolean.FALSE);
       mojo.setInputExcludeParsedScm("MAVEN");
       mojo.setInputExcludeParsedScm("idea");
       mojo.setInputExcludeParsedScm("eclipse");
       mojo.execute();

       ensureRatReportIsCorrect(ratTxtFile, expected, notExpected);
   }
}
