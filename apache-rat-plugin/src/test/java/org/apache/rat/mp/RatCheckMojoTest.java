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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.apache.commons.cli.Option;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.rat.ConfigurationException;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.ReportConfigurationTest;
import org.apache.rat.api.Document;
import org.apache.rat.commandline.Arg;
import org.apache.rat.license.ILicenseFamily;
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

        assertNotNull("The mojo is missing its MavenProject, which will result in an NPE during rat runs.",
                mojo.getProject());

        if (mojo instanceof RatReportMojo) {
            setVariableValueToObject(mojo, "localRepository", newArtifactRepository(getContainer()));
            setVariableValueToObject(mojo, "factory", newArtifactFactory());
            setVariableValueToObject(mojo, "siteRenderer", newSiteRenderer(getContainer()));
        } else if (mojo instanceof RatCheckMojo) {
            final File ratTxtFile = new File(buildDirectory, "rat.txt");
            FileUtils.write(ratTxtFile, "", UTF_8); // Ensure the output file exists and is empty (rerunning the test will append)
            mojo.setOut(ratTxtFile.getAbsolutePath());
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
        final String[] expected = { 
                RatTestHelpers.documentOut(true, Document.Type.STANDARD, getDir(mojo) + "pom.xml") +
                RatTestHelpers.APACHE_LICENSE,
                "Notes: 0", "Binaries: 0", "Archives: 0",
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
        final File ratTxtFile = mojo.getRatTxtFile();
        final String dir = getDir(mojo);
        final String[] expected = { 
                "^Files with unapproved licenses:\\s+\\Q" + dir + "src.txt\\E\\s+", 
                "Notes: 0", 
                "Binaries: 0", "Archives: 0", "Standards: 2$", "Apache Licensed: 1$", "Generated Documents: 0",
                "^1 Unknown Licenses", 
                RatTestHelpers.documentOut(false, Document.Type.STANDARD, dir + "src.txt") +
                RatTestHelpers.UNKNOWN_LICENSE,
                RatTestHelpers.documentOut(true, Document.Type.STANDARD, dir + "pom.xml") +
                RatTestHelpers.APACHE_LICENSE
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
        final String dir = getDir(mojo);
        final String[] expected = { "^Files with unapproved licenses:\\s+\\Q" + dir + "src.apt\\E\\s+", "Notes: 0",
                "Binaries: 0", "Archives: 0", "Standards: 2$", "Apache Licensed: 1$", "Generated Documents: 0",
                "^1 Unknown Licenses", 
                RatTestHelpers.documentOut(false, Document.Type.STANDARD, dir + "src.apt") +
                RatTestHelpers.UNKNOWN_LICENSE,
                RatTestHelpers.documentOut(true, Document.Type.STANDARD, dir + "pom.xml") +
                RatTestHelpers.APACHE_LICENSE
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
        final String[] expected = { "Notes: 0", "Binaries: 0", "Archives: 0", "Standards: 0$", "Apache Licensed: 0$",
                "Generated Documents: 0", "^0 Unknown Licenses" };

        ReportConfiguration config = mojo.getConfiguration();
        assertFalse("Should not be adding licenses", config.isAddingLicenses());
        assertFalse("Should not be forcing licenses", config.isAddingLicensesForced());

        ReportConfigurationTest.validateDefaultApprovedLicenses(config, 1);
        assertTrue(config.getLicenseCategories(LicenseFilter.APPROVED).contains(ILicenseFamily.makeCategory("YAL")));
        ReportConfigurationTest.validateDefaultLicenseFamilies(config, "YAL");
        assertNotNull(LicenseSetFactory.familySearch("YAL", config.getLicenseFamilies(LicenseFilter.ALL)));
        ReportConfigurationTest.validateDefaultLicenses(config, "MyLicense", "CpyrT", "RegxT", "SpdxT", "TextT", 
                "Not", "All", "Any");
        assertTrue(LicenseSetFactory.search("YAL", "MyLicense", config.getLicenses(LicenseFilter.ALL)).isPresent());
        assertNotNull("Should have filesToIgnore", config.getFilesToIgnore());
        assertThat(config.getFilesToIgnore()).isExactlyInstanceOf(FalseFileFilter.class);
        assertNotNull("Should have directoriesToIgnore", config.getDirectoriesToIgnore());
        assertThat(config.getDirectoriesToIgnore()).isExactlyInstanceOf(NameBasedHiddenFileFilter.class);
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
        final File ratTxtFile = mojo.getRatTxtFile();
        // POM reports AL, BSD and CC BYas BSD because it contains the BSD and CC BY strings
        final String[] expected = { 
                RatTestHelpers.documentOut(false, Document.Type.STANDARD, getDir(mojo) + "pom.xml") +
                RatTestHelpers.APACHE_LICENSE +
                RatTestHelpers.licenseOut("BSD", "BSD") +
                RatTestHelpers.licenseOut("CC BY", "Creative Commons Attribution (Unapproved)"),
                "Notes: 0", "Binaries: 0", "Archives: 0",
                "Standards: 1$", "Apache Licensed: 1$", "Generated Documents: 0", "^0 Unknown Licenses" };

        ReportConfiguration config = mojo.getConfiguration();
        // validate configuration
        assertThat(config.isAddingLicenses()).isFalse();
        assertThat(config.isAddingLicensesForced()).isFalse();
        assertThat(config.getCopyrightMessage()).isNull();
        assertThat(config.getStyleSheet()).withFailMessage("Stylesheet should not be null").isNotNull();
        assertThat(config.getDirectoriesToIgnore()).withFailMessage("directoriesToIgnore filter should not be null").isNotNull();
        assertThat(config.getDirectoriesToIgnore()).isExactlyInstanceOf(NameBasedHiddenFileFilter.class);
        assertThat(config.getFilesToIgnore()).withFailMessage("filesToIgnore filter should not be null").isNotNull();
        assertThat(config.getFilesToIgnore()).isExactlyInstanceOf(FalseFileFilter.class);
        
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
        final File ratTxtFile = mojo.getRatTxtFile();
        final String dir = getDir(mojo);
        final String[] expected = {
            "Notes: 1",
            "Binaries: 0",
            "Archives: 0",
            "Standards: 5$",
            "Apache Licensed: 2$",
            "Generated Documents: 0",
            "^3 Unknown Licenses",
            RatTestHelpers.documentOut(true, Document.Type.STANDARD, dir + "pom.xml")+
            RatTestHelpers.APACHE_LICENSE,
            RatTestHelpers.documentOut(false, Document.Type.STANDARD, dir + "dir1/dir1.md")+
                RatTestHelpers.UNKNOWN_LICENSE,
            RatTestHelpers.documentOut(false, Document.Type.STANDARD, dir + "dir2/dir2.txt")+
                RatTestHelpers.UNKNOWN_LICENSE,
            RatTestHelpers.documentOut(false, Document.Type.STANDARD, dir + "dir3/file3.log")+
                RatTestHelpers.UNKNOWN_LICENSE,  
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
     * Tests verifying gitignore parsing under a special edge case condition
     * The problem occurs when '/foo.md' is to be ignored and a file with that name exists
     * in a directory which is the project base directory twice concatenated.
     * So for this test we must create such a file which is specific for the current
     * working directory.
     */
    public void testRAT362GitIgnore() throws Exception {
        final RatCheckMojo mojo = newRatCheckMojo("RAT-362-GitIgnore");
        final File ratTxtFile = mojo.getRatTxtFile();
        final String dir = getDir(mojo);

        if (dir.contains(":")) {
            // The problem this is testing for cannot happen if there is
            // a Windows drive letter in the name of the directory.
            // Any duplication of a ':' will make it all fail always.
            // So there is no point in continuing this test.
            return;
        }

        // Make the target directory for the test file
        File targetDirectory = new File(dir + dir);
        if (!targetDirectory.exists()) {
            assertTrue(targetDirectory.mkdirs());
        }
        assertTrue(targetDirectory.isDirectory());

        // Create the test file with a content on which it must fail
        File generatedFile = new File(targetDirectory + "/foo.md");
        final String[] expected = {
                "Notes: 0",
                "Binaries: 0",
                "Archives: 0",
                "Standards: 3$",
                "Apache Licensed: 2$",
                "Generated Documents: 0",
                "^1 Unknown Licenses",
                RatTestHelpers.documentOut(false, Document.Type.STANDARD, generatedFile.getCanonicalPath()) +
                    RatTestHelpers.UNKNOWN_LICENSE,
        };
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(generatedFile));
            writer.write("File without a valid license\n");
            writer.close();

            mojo.execute();
            fail("Expected RatCheckException: This check should have failed on the invalid test file");
        } catch (RatCheckException e) {
            final String msg = e.getMessage();
            assertTrue("report filename was not contained in '" + msg + "'", msg.contains(ratTxtFile.getName()));
            assertFalse("no null allowed in '" + msg + "'", (msg.toUpperCase().contains("NULL")));
            ensureRatReportIsCorrect(ratTxtFile, expected, TextUtils.EMPTY);
        } finally {
            // Cleanup
            assertTrue(generatedFile.delete());
        }
    }

}
