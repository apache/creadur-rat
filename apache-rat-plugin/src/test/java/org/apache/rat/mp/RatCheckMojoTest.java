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
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.ReportConfigurationTest;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseFamilySetFactory;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.testhelpers.TextUtils;
import org.apache.rat.walker.NameBasedHiddenFileFilter;

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

        assertNotNull("The mojo is missing its MavenProject, which will result in an NPE during rat runs.",
                mojo.getProject());

        if (mojo instanceof RatReportMojo) {
            setVariableValueToObject(mojo, "localRepository", newArtifactRepository(getContainer()));
            setVariableValueToObject(mojo, "factory", newArtifactFactory());
            setVariableValueToObject(mojo, "siteRenderer", newSiteRenderer(getContainer()));
        } else if (mojo instanceof RatCheckMojo) {
            final File ratTxtFile = new File(buildDirectory, "rat.txt");
            FileUtils.write(ratTxtFile, "", UTF_8); // Ensure the output file exists and is empty (rerunning the test will append)
            setVariableValueToObject(mojo, "reportFile", ratTxtFile);
        }
        return mojo;
    }

    /**
     * Reads the location of the rat text file from the Mojo.
     *
     * @param pMojo The configured Mojo.
     * @return Value of the "reportFile" property.
     * @throws Exception An error occurred while reading the property.
     */
    private File getRatTxtFile(RatCheckMojo pMojo) throws Exception {
        return (File) getVariableValueFromObject(pMojo, "reportFile");
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
        final File ratTxtFile = getRatTxtFile(mojo);
        final String[] expected = { " AL +\\Q" + getDir(mojo) + "pom.xml\\E", "Notes: 0", "Binaries: 0", "Archives: 0",
                "Standards: 1$", "Apache Licensed: 1$", "Generated Documents: 0", "^0 Unknown Licenses" };

        ReportConfiguration config = mojo.getConfiguration();
        ReportConfigurationTest.validateDefault(config);

        mojo.execute();
        ensureRatReportIsCorrect(ratTxtFile, expected, TextUtils.EMPTY);
    }

    /**
     * Runs a check, which should detect a problem.
     *
     * @throws Exception The test failed.
     */
    public void testIt2() throws Exception {

        final RatCheckMojo mojo = newRatCheckMojo("it2");
        final File ratTxtFile = getRatTxtFile(mojo);
        final String dir = getDir(mojo);
        final String[] expected = { "^Files with unapproved licenses:\\s+\\Q" + dir + "src.txt\\E\\s+", "Notes: 0",
                "Binaries: 0", "Archives: 0", "Standards: 2$", "Apache Licensed: 1$", "Generated Documents: 0",
                "^1 Unknown Licenses", " AL +\\Q" + dir + "pom.xml\\E$", "\\Q!????? " + dir + "src.txt\\E$",
                "^== File: \\Q" + dir + "src.txt\\E$" };
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
        final File ratTxtFile = getRatTxtFile(mojo);
        final String dir = getDir(mojo);
        final String[] expected = { "^Files with unapproved licenses:\\s+\\Q" + dir + "src.apt\\E\\s+", "Notes: 0",
                "Binaries: 0", "Archives: 0", "Standards: 2$", "Apache Licensed: 1$", "Generated Documents: 0",
                "^1 Unknown Licenses", " AL +\\Q" + dir + "pom.xml\\E$", "\\Q!????? " + dir + "src.apt\\E$",
                "^== File: \\Q" + dir + "src.apt\\E$" };

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
        final File ratTxtFile = getRatTxtFile(mojo);
        final String[] expected = { "Notes: 0", "Binaries: 0", "Archives: 0", "Standards: 0$", "Apache Licensed: 0$",
                "Generated Documents: 0", "^0 Unknown Licenses" };

        ReportConfiguration config = mojo.getConfiguration();
        assertFalse("Should not be adding licenses", config.isAddingLicenses());
        assertFalse("Should not be forcing licenses", config.isAddingLicensesForced());
        assertTrue("Should be styling report", config.isStyleReport());

        ReportConfigurationTest.validateDefaultApprovedLicenses(config, 1);
        assertTrue(config.getApprovedLicenseCategories().contains(ILicenseFamily.makeCategory("YAL")));
        ReportConfigurationTest.validateDefaultLicenseFamilies(config, "YAL");
        assertNotNull(LicenseFamilySetFactory.search("YAL", config.getLicenseFamilies(LicenseFilter.all)));
        ReportConfigurationTest.validateDefaultLicenses(config, "MyLicense", "CpyrT", "RegxT", "SpdxT", "TextT", 
                "Not", "All", "Any");
        assertNotNull(LicenseSetFactory.search("MyLicense", config.getLicenses(LicenseFilter.all)));
        assertNull("Should not have inputFileFilter", config.getInputFileFilter());
        mojo.execute();

        ensureRatReportIsCorrect(ratTxtFile, expected, TextUtils.EMPTY);
    }
    
    /**
     * Runs a check, which should expose no problems.
     *
     * @throws Exception The test failed.
     */
    public void testRAT_343() throws Exception {
        final RatCheckMojo mojo = newRatCheckMojo("RAT-343");
        final File ratTxtFile = getRatTxtFile(mojo);
        // POM reports as BSD because it has the BSD string in and that gets found before AL match
        final String[] expected = { " BSD +\\Q" + getDir(mojo) + "pom.xml\\E", "Notes: 0", "Binaries: 0", "Archives: 0",
                "Standards: 1$", "Apache Licensed: 0$", "Generated Documents: 0", "^0 Unknown Licenses" };

        ReportConfiguration config = mojo.getConfiguration();
        // validate configuration
        assertThat(config.isAddingLicenses()).isFalse();
        assertThat(config.isAddingLicensesForced()).isFalse();
        assertThat(config.getCopyrightMessage()).isNull();
        assertThat(config.getInputFileFilter()).isNull();
        assertThat(config.isStyleReport()).isTrue();
        assertThat(config.getStyleSheet()).isNotNull().withFailMessage("Stylesheet should not be null");
        assertThat(config.getDirectoryFilter()).isNotNull().withFailMessage("Directory filter should not be null");
        assertThat(config.getDirectoryFilter()).isExactlyInstanceOf(NameBasedHiddenFileFilter.class);
        
        ReportConfigurationTest.validateDefaultApprovedLicenses(config, 1);
        ReportConfigurationTest.validateDefaultLicenseFamilies(config, "BSD", "CC BY");
        ReportConfigurationTest.validateDefaultLicenses(config, "BSD", "CC BY");

        mojo.execute();
        ensureRatReportIsCorrect(ratTxtFile, expected, TextUtils.EMPTY);
    }

    /**
     * Tests verifying gitignore parsing
     */
    public void testRAT335GitIgnore() throws Exception {
        final RatCheckMojo mojo = newRatCheckMojo("RAT-335-GitIgnore");
        final File ratTxtFile = getRatTxtFile(mojo);
        final String dir = getDir(mojo);
        final String[] expected = {
            "Notes: 1",
            "Binaries: 0",
            "Archives: 0",
            "Standards: 5$",
            "Apache Licensed: 2$",
            "Generated Documents: 0",
            "^3 Unknown Licenses",
            " AL +\\Q" + dir + "pom.xml\\E$",
            "\\Q!????? " + dir + "dir1/dir1.md\\E$",
            "\\Q!????? " + dir + "dir2/dir2.txt\\E$",
            "\\Q!????? " + dir + "dir3/file3.log\\E$",
            "^== File: \\Q" + dir + "dir1/dir1.md\\E$",
            "^== File: \\Q" + dir + "dir2/dir2.txt\\E$",
            "^== File: \\Q" + dir + "dir3/file3.log\\E$"
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
}
