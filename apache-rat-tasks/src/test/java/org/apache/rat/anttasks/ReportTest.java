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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.rat.ConfigurationException;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.ReportConfigurationTest;
import org.apache.rat.document.DocumentName;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ReportTest extends AbstractRatAntTaskTest {
    private static final String baseNameStr = String.join(File.separator, new String[]{"src","test","resources","antunit"});
    private static final File baseNameFile = new File(baseNameStr);
    private static final File antFile = new File(baseNameFile, "report-junit.xml").getAbsoluteFile();
    private DocumentName documentName;
    private DocumentName indexName;

    @BeforeEach
    public void setUp() {
        File baseFile = antFile.getParentFile();
        for (int i = 0; i < 4; i++) {
            baseFile = baseFile.getParentFile();
        }
        documentName = DocumentName.builder(antFile).setBaseName(baseFile).build();
        indexName = DocumentName.builder(new File(baseNameFile, "index.apt")).setBaseName(baseFile).build();

        File f = new File(documentName.getBaseName());

        StringBuilder sb = new StringBuilder(documentName.getBaseName());
                    sb.append("antfile: ").append(antFile).append("\n")
                            .append("baseFile: ").append(baseFile).append("\n");
        if (!f.exists()) {
            sb.append(" does not exist (RAT CHECK)\n");
            System.err.println(sb);
            Assertions.fail(sb.toString());
        }

        sb.append(" DOES exist (RAT CHECK)\n");
        System.err.println(sb);
        System.setProperty(MagicNames.PROJECT_BASEDIR, documentName.getBaseName());
        super.setUp();
    }

    @ParameterizedTest
    @MethodSource("allTestData")
    @Disabled("Need to build build/verify tests for all options")
    void testAllRules(String targetName) {
        buildRule.executeTarget(targetName);
    }

    private static List<Arguments> allTestData() {
        List<Arguments> args = new ArrayList<>();
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(new InputSource(new FileReader(antFile)));
            NodeList nodeList = document.getElementsByTagName("target");
            for (int i = 0; i < nodeList.getLength(); i++) {
                String targetName = nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
                System.out.println(targetName);
                args.add(Arguments.of(targetName));
            }
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new ConfigurationException("Unable to read inputSource", e);
        }
        return args;
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
        return String.format( "%s \\Q%s\\E\\s+S .*\\s+\\Q%s\\E ", approved?" ":"!", fileText, id);
    }
    
    @Test
    public void testWithReportSentToAnt() {
        buildRule.executeTarget("testWithReportSentToAnt");
        assertLogMatches(logLine("AL"));
    }

    @Test
    public void testWithReportSentToFile() throws Exception {
        final File reportFile = new File(getTempDir(), "selftest.report");
        final String alLine = String.format("\\Q%s\\E\\s+S ", documentName.localized("/"));

        if (!getTempDir().mkdirs() && !getTempDir().isDirectory()) {
            throw new IOException("Could not create temporary directory " + getTempDir());
        }
        if (reportFile.isFile() && !reportFile.delete()) {
            throw new IOException("Unable to remove report file " + reportFile);
        }
        buildRule.executeTarget("testWithReportSentToFile");
        assertLogDoesNotMatch(alLine);
        assertThat(reportFile).describedAs("Expected report file " + reportFile).isFile();
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
        assertLogDoesNotMatch(logLine(indexName.localized("/"), "AL"));
        assertLogMatches(logLine(true, indexName.localized("/"), "YASL"));
    }

    @Test
    public void testCustomMatcher() {
        buildRule.executeTarget("testCustomMatcher");
        assertLogDoesNotMatch(logLine("AL"));
        assertLogMatches(logLine("YASL2"));
    }

    @Test
    public void testNoResources() {
        try {
            buildRule.executeTarget("testNoResources");
            fail("Expected Exception");
        } catch (BuildException e) {
            final String expect = "You must specify at least one file";
            assertThat(e.getMessage()).describedAs("Expected " + expect).contains(expect);
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
            assertThat(e.getMessage()).describedAs("Expected " + expect).contains(expect);
        }
    }

    private Report getReport(String target) {
        Target testDefault = buildRule.getProject().getTargets().get(target);
        Optional<Task> optT = Arrays.stream(testDefault.getTasks()).filter(t -> t.getTaskName().equals("rat:report"))
                .findFirst();
        assertThat(optT).isPresent();
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
            assertThat(e.getMessage()).describedAs("Expected " + expect).contains(expect);
        }
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
        assertThat(origFirstLine).contains("--");
        assertThat(origFirstLine).doesNotContain("~~");
        final File modifiedFile = new File("target/anttasks/it-sources/index.apt.new");
        final String modifiedFirstLine = getFirstLine(modifiedFile);
        assertThat(modifiedFirstLine).doesNotContain("--");
        assertThat(modifiedFirstLine).contains("~~");
    }

    /**
     * Test correct generation of string result if non-UTF8 {@code file.encoding} is set.
     */
    @Test
    @Disabled
    public void testISO88591() {
        // In previous versions of the JDK, it used to be possible to
        // change the value of file.encoding at runtime. As of Java 16,
        // this is no longer possible. Instead, at this point, we check,
        // that file.encoding is actually ISO-8859-1. (Within Maven, this
        // is enforced by the configuration of the surefire plugin.) If not,
        // we skip this test.
        assumeTrue("ISO-8859-1".equals(System.getProperty("file.encoding")), "Expected file.encoding=ISO-8859-1");
        buildRule.executeTarget("testISO88591");
        assertThat(buildRule.getLog()).describedAs("Log should contain the test umlauts").contains("\u00E4\u00F6\u00FC\u00C4\u00D6\u00DC\u00DF");
    }

    /**
     * Test correct generation of XML file if non-UTF8 {@code file.encoding} is set.
     */
    @Test
    @Disabled
    public void testISO88591WithFile() throws Exception {
        // In previous versions of the JDK, it used to be possible to
        // change the value of file.encoding at runtime. As of Java 16,
        // this is no longer possible. Instead, at this point, we check,
        // that file.encoding is actually ISO-8859-1. (Within Maven, this
        // is enforced by the configuration of the surefire plugin.) If not,
        // we skip this test.
        assumeTrue("ISO-8859-1".equals(System.getProperty("file.encoding")), "Expected file.encoding=ISO-8859-1");
        Charset.defaultCharset();
        String outputDir = System.getProperty("output.dir", "target/anttasks");
        String selftestOutput = System.getProperty("report.file", outputDir + "/selftest.report");
        buildRule.executeTarget("testISO88591WithReportFile");
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        boolean documentParsed;
        try (FileInputStream fis = new FileInputStream(selftestOutput)) {
            Document doc = db.parse(fis);
            assertThat(doc.getElementsByTagName("header-sample").item(0).getTextContent())
                    .describedAs("Report should contain test umlauts")
                    .contains("\u00E4\u00F6\u00FC\u00C4\u00D6\u00DC\u00DF");
            documentParsed = true;
        } catch (Exception ex) {
            documentParsed = false;
        }
        assertThat(documentParsed).describedAs("Report file could not be parsed as XML").isTrue();
    }

}
