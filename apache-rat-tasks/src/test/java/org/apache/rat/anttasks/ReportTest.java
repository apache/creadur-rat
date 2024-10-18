/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rat.anttasks;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.ReportConfigurationTest;
import org.apache.rat.document.impl.DocumentName;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;

public class ReportTest extends AbstractRatAntTaskTest {
    private final String baseNameStr = String.join(File.separator, new String[]{"src","test","resources","antunit"});
    private final File antFile = new File(new File(baseNameStr), "report-junit.xml").getAbsoluteFile();
    private DocumentName documentName;

    @Before
    public void setUp()  {
        String antFileName = antFile.getAbsolutePath();
        File baseFile = antFile.getParentFile();
        for (int i = 0; i < 4; i++) {
            baseFile = baseFile.getParentFile();
        }
        documentName = new DocumentName(antFile, new DocumentName(baseFile));
        System.setProperty(MagicNames.PROJECT_BASEDIR, documentName.getBaseName());
        super.setUp();
    }
    @Override
    protected File getAntFile() {
        return antFile;
    }

    private String logLine(String id) {
        return logLine(true, documentName.localized("/"), id);
    }
    
    private String logLine(String fileText, String id) {
        return logLine(true, fileText, id);
    }
    
    private String logLine(boolean approved, String fileText, String id) {
        return String.format( "%sS \\Q%s\\E\\s+\\Q%s\\E ", approved?" ":"!", fileText, id);
    }
    
    @Test
    public void testWithReportSentToAnt() {
        buildRule.executeTarget("testWithReportSentToAnt");
        assertLogMatches(logLine("AL"));
    }

    @Test
    public void testWithReportSentToFile() throws Exception {
        final File reportFile = new File(getTempDir(), "selftest.report");
        final String alLine = " S \\Q" + documentName.localized("/") + "\\E";

        if (!getTempDir().mkdirs() && !getTempDir().isDirectory()) {
            throw new IOException("Could not create temporary directory " + getTempDir());
        }
        if (reportFile.isFile() && !reportFile.delete()) {
            throw new IOException("Unable to remove report file " + reportFile);
        }
        buildRule.executeTarget("testWithReportSentToFile");
        assertLogDoesNotMatch(alLine);
        assertTrue("Expected report file " + reportFile, reportFile.isFile());
        assertFileMatches(reportFile, alLine);
    }

    @Test
    public void testWithALUnknown() {
        buildRule.executeTarget("testWithALUnknown");
        assertLogDoesNotMatch(logLine("AL"));
        assertLogMatches(logLine(false, documentName.localized("/"), "?????"));
    }

    @Test
    public void testCustomLicense() {
        buildRule.executeTarget("testCustomLicense");
        assertLogDoesNotMatch(logLine("AL"));
        assertLogMatches(logLine("newFa"));
    }

    @Test
    public void testCustomMatcher() {
        buildRule.executeTarget("testCustomMatcher");
        assertLogDoesNotMatch(logLine("AL"));
        assertLogMatches(logLine("YASL1"));
    }

    @Test
    public void testInlineCustomMatcher() {
        buildRule.executeTarget("testInlineCustomMatcher");
        assertLogDoesNotMatch(logLine("AL"));
        assertLogMatches(logLine("YASL1"));
    }

    @Test
    public void testCustomMatcherBuilder() {
        buildRule.executeTarget("testCustomMatcherBuilder");
        assertLogDoesNotMatch(logLine("AL"));
        assertLogMatches(logLine("YASL1"));
    }

    @Test
    public void testNoResources() {
        try {
            buildRule.executeTarget("testNoResources");
            fail("Expected Exceptoin");
        } catch (BuildException e) {
            final String expect = "You must specify at least one file";
            assertTrue("Expected " + expect + ", got " + e.getMessage(), e.getMessage().contains(expect));
        }
    }

    @Test
    public void testCopyrightBuild() {
        try {
            buildRule.executeTarget("testCopyrightBuild");
            assertLogMatches(logLine("/src/test/resources/antunit/index.apt","YASL1"));
            assertLogDoesNotMatch(logLine("/index.apt","AL"));
        } catch (BuildException e) {
            final String expect = "You must specify at least one file";
            assertTrue("Expected " + expect + ", got " + e.getMessage(), e.getMessage().contains(expect));
        }
    }

    private Report getReport(String target) {
        Target testDefault = buildRule.getProject().getTargets().get(target);
        Optional<Task> optT = Arrays.stream(testDefault.getTasks()).filter(t -> t.getTaskName().equals("rat:report"))
                .findFirst();
        assertTrue(optT.isPresent());
        optT.get().maybeConfigure();
        return (Report) ((UnknownElement) optT.get()).getRealThing();
    }

    @Test
    public void testDefault() {
        Report report = getReport("testDefault");
        ReportConfiguration config = report.getConfiguration();
        ReportConfigurationTest.validateDefault(config);
    }

    @Test
    public void testNoLicenseMatchers() {
        try {
            buildRule.executeTarget("testNoLicenseMatchers");
            fail("Expected Exception");
        } catch (BuildException e) {
            final String expect = "at least one license";
            assertTrue("Expected " + expect + ", got " + e.getMessage(), e.getMessage().contains(expect));
        }
    }

    private String getAntFileName() {
        return "/src/test/resources/antunit/"+getAntFile().getName();
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

    @Test
    public void testAddLicenseHeaders() throws Exception {
        buildRule.executeTarget("testAddLicenseHeaders");

        final File origFile = new File("target/anttasks/it-sources/index.apt");
        final String origFirstLine = getFirstLine(origFile);
        assertTrue(origFirstLine.contains("--"));
        assertFalse(origFirstLine.contains("~~"));
        final File modifiedFile = new File("target/anttasks/it-sources/index.apt.new");
        final String modifiedFirstLine = getFirstLine(modifiedFile);
        assertFalse(modifiedFirstLine.contains("--"));
        assertTrue(modifiedFirstLine.contains("~~"));
    }

    /**
     * Test correct generation of string result if non-UTF8 file.encoding is set.
     */
    @Test
    @Ignore
    public void testISO88591() {
        // In previous versions of the JDK, it used to be possible to
        // change the value of file.encoding at runtime. As of Java 16,
        // this is no longer possible. Instead, at this point, we check,
        // that file.encoding is actually ISO-8859-1. (Within Maven, this
        // is enforced by the configuration of the surefire plugin.) If not,
        // we skip this test.
        Assume.assumeTrue("Expected file.encoding=ISO-8859-1",
                "ISO-8859-1".equals(System.getProperty("file.encoding")));
        buildRule.executeTarget("testISO88591");
        assertTrue("Log should contain the test umlauts",
                buildRule.getLog().contains("\u00E4\u00F6\u00FC\u00C4\u00D6\u00DC\u00DF"));
    }

    /**
     * Test correct generation of XML file if non-UTF8 file.encoding is set.
     */
    @Test
    @Ignore
    public void testISO88591WithFile() throws Exception {
        // In previous versions of the JDK, it used to be possible to
        // change the value of file.encoding at runtime. As of Java 16,
        // this is no longer possible. Instead, at this point, we check,
        // that file.encoding is actually ISO-8859-1. (Within Maven, this
        // is enforced by the configuration of the surefire plugin.) If not,
        // we skip this test.
        Assume.assumeTrue("Expected file.encoding=ISO-8859-1",
                "ISO-8859-1".equals(System.getProperty("file.encoding")));
        Charset.defaultCharset();
        String outputDir = System.getProperty("output.dir", "target/anttasks");
        String selftestOutput = System.getProperty("report.file", outputDir + "/selftest.report");
        buildRule.executeTarget("testISO88591WithReportFile");
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        boolean documentParsed = false;
        try (FileInputStream fis = new FileInputStream(selftestOutput)) {
            Document doc = db.parse(fis);
            boolean byteSequencePresent = doc.getElementsByTagName("header-sample").item(0).getTextContent()
                    .contains("\u00E4\u00F6\u00FC\u00C4\u00D6\u00DC\u00DF");
            assertTrue("Report should contain test umlauts", byteSequencePresent);
            documentParsed = true;
        } catch (Exception ex) {
            documentParsed = false;
        }
        assertTrue("Report file could not be parsed as XML", documentParsed);
    }
}
