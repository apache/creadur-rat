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

import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.BuildException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.rat.document.impl.guesser.BinaryGuesser;
import org.junit.Assume;
import org.junit.Test;
import org.w3c.dom.Document;

import static org.junit.Assert.*;

public class ReportTest extends AbstractRatAntTaskTest {
    private static final File antFile = new File("src/test/resources/antunit/report-junit.xml").getAbsoluteFile();

    @Override
    protected File getAntFile() {
        return antFile;
    }

    @Test
    public void testWithReportSentToAnt() throws Exception {
        buildRule.executeTarget("testWithReportSentToAnt");
        assertLogMatches("AL +\\Q" + getAntFileName() + "\\E");
    }

    @Test
    public void testWithReportSentToFile() throws Exception {
        final File reportFile = new File(getTempDir(), "selftest.report");
        if (!getTempDir().mkdirs() && !getTempDir().isDirectory()) {
            throw new IOException("Could not create temporary directory " + getTempDir());
        }
        final String alLine = "AL +\\Q" + getAntFileName() + "\\E";
        if (reportFile.isFile() && !reportFile.delete()) {
            throw new IOException("Unable to remove report file " + reportFile);
        }
        buildRule.executeTarget("testWithReportSentToFile");
        assertLogDoesNotMatch(alLine);
        assertTrue("Expected report file " + reportFile, reportFile.isFile());
        assertFileMatches(reportFile, alLine);
    }

    @Test
    public void testWithALUnknown() throws Exception {
        buildRule.executeTarget("testWithALUnknown");
        assertLogDoesNotMatch("AL +\\Q" + getAntFileName() + "\\E");
        assertLogMatches("\\!\\?\\?\\?\\?\\? +\\Q" + getAntFileName() + "\\E");
    }

    @Test
    public void testCustomMatcher() throws Exception {
        buildRule.executeTarget("testCustomMatcher");
        assertLogDoesNotMatch("AL +\\Q" + getAntFileName() + "\\E");
        assertLogMatches("EXMPL +\\Q" + getAntFileName() + "\\E");
    }

    @Test
    public void testNoResources() throws Exception {
        try {
            buildRule.executeTarget("testNoResources");
            fail("Expected Exception");
        } catch (BuildException e) {
            final String expect = "You must specify at least one file";
            assertTrue("Expected " + expect + ", got " + e.getMessage(),
                    e.getMessage().contains(expect));
        }
    }

    @Test
    public void testNoLicenseMatchers() throws Exception {
        try {
            buildRule.executeTarget("testNoLicenseMatchers");
            fail("Expected Exception");
        } catch (BuildException e) {
            final String expect = "at least one license";
            assertTrue("Expected " + expect + ", got " + e.getMessage(),
                    e.getMessage().contains(expect));
        }
    }

    private String getAntFileName() {
        return getAntFile().getPath().replace('\\', '/');
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
    public void testISO88591() throws Exception {
    	// In previous versions of the JDK, it used to be possible to
    	// change the value of file.encoding at runtime. As of Java 16,
    	// this is no longer possible. Instead, at this point, we check,
    	// that file.encoding is actually ISO-8859-1. (Within Maven, this
    	// is enforced by the configuration of the surefire plugin.) If not,
    	// we skip this test.
    	Assume.assumeTrue("Expected file.encoding=ISO-8859-1", "ISO-8859-1".equals(System.getProperty("file.encoding")));
        buildRule.executeTarget("testISO88591");
        assertTrue("Log should contain the test umlauts", buildRule.getLog().contains("\u00E4\u00F6\u00FC\u00C4\u00D6\u00DC\u00DF"));
    }

    /**
     * Test correct generation of XML file if non-UTF8 file.encoding is set.
     */
    @Test
    public void testISO88591WithFile() throws Exception {
    	// In previous versions of the JDK, it used to be possible to
    	// change the value of file.encoding at runtime. As of Java 16,
    	// this is no longer possible. Instead, at this point, we check,
    	// that file.encoding is actually ISO-8859-1. (Within Maven, this
    	// is enforced by the configuration of the surefire plugin.) If not,
    	// we skip this test.
    	Assume.assumeTrue("Expected file.encoding=ISO-8859-1", "ISO-8859-1".equals(System.getProperty("file.encoding")));
        Charset.defaultCharset();
        String outputDir = System.getProperty("output.dir", "target/anttasks");
        String selftestOutput = System.getProperty("report.file", outputDir + "/selftest.report");
        buildRule.executeTarget("testISO88591WithReportFile");
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        boolean documentParsed = false;
        try (FileInputStream fis = new FileInputStream(selftestOutput)) {
            Document doc = db.parse(fis);
            boolean byteSequencePresent = doc.getElementsByTagName("header-sample")
                    .item(0)
                    .getTextContent()
                    .contains("\u00E4\u00F6\u00FC\u00C4\u00D6\u00DC\u00DF");
            assertTrue("Report should contain test umlauts", byteSequencePresent);
            documentParsed = true;
        } catch (Exception ex) {
            documentParsed = false;
        }
        assertTrue("Report file could not be parsed as XML", documentParsed);
    }
}
