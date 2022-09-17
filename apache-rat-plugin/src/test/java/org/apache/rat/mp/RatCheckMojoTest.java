package org.apache.rat.mp;

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

import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.apache.rat.config.AddLicenseHeaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Assume;

import static org.apache.rat.mp.RatTestHelpers.ensureRatReportIsCorrect;
import static org.apache.rat.mp.RatTestHelpers.getSourceDirectory;
import static org.apache.rat.mp.RatTestHelpers.newArtifactFactory;
import static org.apache.rat.mp.RatTestHelpers.newArtifactRepository;
import static org.apache.rat.mp.RatTestHelpers.newArtifactResolver;
import static org.apache.rat.mp.RatTestHelpers.newSiteRenderer;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Test case for the {@link RatCheckMojo} and {@link RatReportMojo}.
 */
public class RatCheckMojoTest extends AbstractMojoTestCase {

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
     * @param pDir  The directory, where to look for a pom.xml file.
     * @param pGoal The goal, which the Mojo must implement.
     * @return The configured Mojo.
     * @throws Exception An error occurred while creating the Mojo.
     */
    private AbstractRatMojo newRatMojo(String pDir, String pGoal,
                                       boolean pCreateCopy) throws Exception {
        final File baseDir = new File(getBasedir());
        final File testBaseDir = getSourceDirectory(getBasedir(), pDir,
                pCreateCopy, baseDir);
        File testPom = new File(testBaseDir, "pom.xml");
        AbstractRatMojo mojo = (AbstractRatMojo) lookupMojo(pGoal, testPom);
        assertNotNull(mojo);
        final File buildDirectory = new File(new File(baseDir, "target/test"),
                pDir);
        setVariableValueToObject(mojo, "basedir", testBaseDir);
        setVariableValueToObject(mojo, "addDefaultLicenseMatchers",
                Boolean.TRUE);
        setVariableValueToObject(mojo, "useDefaultExcludes", Boolean.TRUE);
        setVariableValueToObject(mojo, "useMavenDefaultExcludes", Boolean.TRUE);
        setVariableValueToObject(mojo, "useEclipseDefaultExcludes",
                Boolean.TRUE);
        setVariableValueToObject(mojo, "addLicenseHeaders", AddLicenseHeaders.FALSE.name());
        final Build build = new Build();
        build.setDirectory(buildDirectory.getPath());
        final MavenProjectStub project = new MavenProjectStub() {
            @Override
            public Build getBuild() {
                return build;
            }
        };
        setVariableValueToObject(mojo, "project", project);
        assertNotNull(
                "Problem in test setup - you are missing a project in your mojo.",
                project);
        assertNotNull(
                "The mojo is missing its MavenProject, which will result in an NPE during rat runs.",
                mojo.getProject());
        assertNotNull(
                "No artifactRepos found, which will result in an NPE during rat runs.",
                project.getRemoteArtifactRepositories());

        if (mojo instanceof RatReportMojo) {
            setVariableValueToObject(mojo, "localRepository",
                    newArtifactRepository(container));
            setVariableValueToObject(mojo, "resolver", newArtifactResolver());
            setVariableValueToObject(mojo, "factory", newArtifactFactory());
            setVariableValueToObject(mojo, "siteRenderer",
                    newSiteRenderer(container));
        } else if (mojo instanceof RatCheckMojo) {
            final File ratTxtFile = new File(buildDirectory, "rat.txt");
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

    /**
     * Runs a check, which should expose no problems.
     *
     * @throws Exception The test failed.
     */
    public void testIt1() throws Exception {
        final RatCheckMojo mojo = newRatCheckMojo("it1");
        final File ratTxtFile = getRatTxtFile(mojo);
        mojo.execute();
        ensureRatReportIsCorrect(ratTxtFile, 1, 0);
    }

    /**
     * Runs a check, which should detect a problem.
     *
     * @throws Exception The test failed.
     */
    public void testIt2() throws Exception {
        final RatCheckMojo mojo = newRatCheckMojo("it2");
        final File ratTxtFile = getRatTxtFile(mojo);
        try {
            mojo.execute();
            fail("Expected RatCheckException");
        } catch (RatCheckException e) {
            final String msg = e.getMessage();
            // default value is "${project.build.directory}/rat.txt"
            final String REPORTFILE = "rat.txt";

            assertTrue("report filename was not contained in '" + msg + "'",
                    msg.contains(REPORTFILE));
            assertFalse("no null allowed in '" + msg + "'", (msg.toUpperCase()
                    .contains("NULL")));
        }
        ensureRatReportIsCorrect(ratTxtFile, 1, 1);
    }

    private String getFirstLine(File pFile) throws IOException {
        FileInputStream fis = null;
        InputStreamReader reader = null;
        BufferedReader breader = null;
        try {
            fis = new FileInputStream(pFile);
            reader = new InputStreamReader(fis, StandardCharsets.UTF_8);
            breader = new BufferedReader(reader);
            final String result = breader.readLine();
            breader.close();
            return result;
        } finally {
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(breader);
        }
    }

    /**
     * Tests adding license headers.
     */
    public void testIt3() throws Exception {
        final RatCheckMojo mojo = (RatCheckMojo) newRatMojo("it3", "check",
                true);
        setVariableValueToObject(mojo, "addLicenseHeaders", AddLicenseHeaders.TRUE.name());
        setVariableValueToObject(mojo, "numUnapprovedLicenses",
                1);
        mojo.execute();
        final File ratTxtFile = getRatTxtFile(mojo);
        ensureRatReportIsCorrect(ratTxtFile, 1, 1);

        final File baseDir = new File(getBasedir());
        final File sourcesDir = new File(new File(baseDir, "target/it-source"),
                "it3");
        final String firstLineOrig = getFirstLine(new File(sourcesDir,
                "src.apt"));
        assertTrue(firstLineOrig.contains("--"));
        assertFalse(firstLineOrig.contains("~~"));
        final String firstLineModified = getFirstLine(new File(sourcesDir,
                "src.apt.new"));
        assertTrue(firstLineModified.contains("~~"));
        assertFalse(firstLineModified.contains("--"));
    }

    /**
     * Test correct generation of XML file if non-UTF8 file.encoding is set.
     *
     * @throws Exception The test failed.
     */
    public void testIt4() throws Exception {
    	// In previous versions of the JDK, it used to be possible to
    	// change the value of file.encoding at runtime. As of Java 16,
    	// this is no longer possible. Instead, at this point, we check,
    	// that file.encoding is actually ISO-8859-1. (Within Maven, this
    	// is enforced by the configuration of the surefire plugin.) If not,
    	// we skip this test.
    	Assume.assumeTrue("Expected file.encoding=ISO-8859-1", "ISO-8859-1".equals(System.getProperty("file.encoding")));
        final RatCheckMojo mojo = newRatCheckMojo("it4");
        final File ratTxtFile = getRatTxtFile(mojo);
        try {
            setVariableValueToObject(mojo, "reportStyle", "xml");
            mojo.execute();
            fail("Expected RatCheckException");
        } catch (RatCheckException e) {
            final String msg = e.getMessage();
            // default value is "${project.build.directory}/rat.txt"
            final String REPORTFILE = "rat.txt";

            assertTrue("report filename was not contained in '" + msg + "'",
                    msg.contains(REPORTFILE));
            assertFalse("no null allowed in '" + msg + "'", (msg.toUpperCase()
                    .contains("NULL")));
        }
        assertTrue(ratTxtFile.exists());
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        try (FileInputStream fis = new FileInputStream(ratTxtFile)) {
            Document doc = db.parse(fis);
            NodeList headerSample = doc.getElementsByTagName("header-sample");
            String textContent = headerSample.item(0).getTextContent();
            if (textContent.length() == 0) { // can be the pom since this test will parse 2 files but the pom is "ok"
                textContent = headerSample.item(1).getTextContent();
            }
            boolean byteSequencePresent = textContent.contains("\u00E4\u00F6\u00FC\u00C4\u00D6\u00DC\u00DF");
            assertTrue("Report should contain test umlauts, got '" + textContent + "'", byteSequencePresent);
        } catch (Exception ex) {
            fail("Report file could not be parsed as XML: " + ex.getMessage());
        }
    }
}
