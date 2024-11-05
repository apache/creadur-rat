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
package org.apache.rat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.io.FileUtils;
import org.apache.rat.api.Document.Type;
import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.document.FileDocument;
import org.apache.rat.document.DocumentName;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.test.utils.Resources;
import org.apache.rat.testhelpers.TextUtils;
import org.apache.rat.testhelpers.XmlUtils;
import org.apache.rat.walker.DirectoryWalker;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Tests the output of the Reporter.
 */
public class ReporterTest {
    @TempDir
    File tempDirectory;
    final String basedir;

    ReporterTest() {
        basedir = new File(Files.currentFolder(), "target/test-classes/elements").getPath();
    }

    @Test
    public void testOutputOption() throws Exception {
        File output = new File(tempDirectory, "test");
        CommandLine cl = new DefaultParser().parse(OptionCollection.buildOptions(), new String[]{"-o", output.getCanonicalPath()});
        ReportConfiguration config = OptionCollection.createConfiguration(basedir, cl);
        new Reporter(config).output();
        assertTrue(output.exists());
        String content = FileUtils.readFileToString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("2 Unknown Licenses"));
        assertTrue(content.contains("/Source.java"));
        assertTrue(content.contains("/sub/Empty.txt"));
    }

    @Test
    public void testDefaultOutput() throws Exception {
        File output = new File(tempDirectory, "sysout");
        output.delete();
        PrintStream origin = System.out;
        try (PrintStream out = new PrintStream(output)) {
            System.setOut(out);
            CommandLine cl = new DefaultParser().parse(OptionCollection.buildOptions(), new String[]{});
            ReportConfiguration config = OptionCollection.createConfiguration(basedir, cl);
            new Reporter(config).output();
        } finally {
            System.setOut(origin);
        }
        assertTrue(output.exists());
        String content = FileUtils.readFileToString(output, StandardCharsets.UTF_8);
        TextUtils.assertPatternInTarget("Notes: 2$", content);
        TextUtils.assertPatternInTarget("Binaries: 2$", content);
        TextUtils.assertPatternInTarget("Archives: 1$", content);
        TextUtils.assertPatternInTarget("Standards: 8$", content);
        TextUtils.assertPatternInTarget("Apache Licensed: 5$", content);
        TextUtils.assertPatternInTarget("Generated Documents: 1$", content);
        TextUtils.assertPatternInTarget("^2 Unknown Licenses", content);
        assertTrue(content.contains(" S /ILoggerFactory.java"), () -> " S /ILoggerFactory.java");
        assertTrue(content.contains(" B /Image.png"), () -> " B /Image.png");
        assertTrue(content.contains(" N /LICENSE"), () -> " N /LICENSE");
        assertTrue(content.contains(" N /NOTICE"), () -> " N /NOTICE");
        assertTrue(content.contains("!S /Source.java"), () -> "!S /Source.java");
        assertTrue(content.contains(" S /Text.txt"), () -> " S /Text.txt");
        assertTrue(content.contains(" S /TextHttps.txt"), () -> " S /TextHttps.txt");
        assertTrue(content.contains(" S /Xml.xml"), () -> " S /Xml.xml");
        assertTrue(content.contains(" S /buildr.rb"), () -> " S /buildr.rb");
        assertTrue(content.contains(" A /dummy.jar"), () -> " A /dummy.jar");
        assertTrue(content.contains("!S /sub/Empty.txt"), () -> "!S /sub/Empty.txt");
        assertTrue(content.contains(" S /tri.txt"), () -> " S /tri.txt");
        assertTrue(content.contains(" G /generated.txt"), () -> " G /generated.txt");
    }

    @Test
    public void testXMLOutput() throws Exception {
        File output = new File(tempDirectory, "sysout");
        output.delete();
        PrintStream origin = System.out;

        CommandLine cl = new DefaultParser().parse(OptionCollection.buildOptions(), new String[]{"--output-style", "xml", "--output-file", output.getPath()});
        ReportConfiguration config = OptionCollection.createConfiguration(basedir, cl);
        new Reporter(config).output();

        assertTrue(output.exists());
        Document doc = XmlUtils.toDom(java.nio.file.Files.newInputStream(output.toPath()));
        XPath xPath = XPathFactory.newInstance().newXPath();

        NodeList nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource/license[@approval='false']");
        assertEquals(2, nodeList.getLength());

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource/license[@id='AL']");
        assertEquals(5, nodeList.getLength());

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource/license[@id='MIT']");
        assertEquals(1, nodeList.getLength());

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource/license[@id='BSD-3']");
        assertEquals(1, nodeList.getLength());

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource/license[@id='TMF']");
        assertEquals(1, nodeList.getLength());

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource/license[@id='?????']");
        assertEquals(2, nodeList.getLength());

        // GENERATED, UNKNOWN, ARCHIVE, NOTICE, BINARY, STANDARD
        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource[@type='STANDARD']");
        assertEquals(8, nodeList.getLength());

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource[@type='ARCHIVE']");
        assertEquals(1, nodeList.getLength());

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource[@type='BINARY']");
        assertEquals(2, nodeList.getLength());

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource[@type='GENERATED']");
        assertEquals(1, nodeList.getLength());

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource[@type='UNKNOWN']");
        assertEquals(0, nodeList.getLength());

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource[@type='NOTICE']");
        assertEquals(2, nodeList.getLength());

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource/sample");
        assertEquals(1, nodeList.getLength());

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource[@type='GENERATED']/license/notes");
        assertEquals(1, nodeList.getLength());

        nodeList = XmlUtils.getNodeList(doc, xPath,
                "/rat-report/resource[@name='/Source.java']/sample");
        assertEquals(1, nodeList.getLength());
    }

    /**
     * Finds a node via xpath on the document. And then checks family, approval and
     * type of elements of the node.
     *
     * @param doc The document to check/
     * @param xpath the XPath instance to use.
     * @param resource the xpath statement to locate the node.
     * @param licenseInfo the license info for the node. (may = null)
     * @param type the type of resource located.
     * @param hasSample true if a sample from the document should be present.
     * @throws Exception on XPath error.
     */
    private static void checkNode(Document doc, XPath xpath, String resource, LicenseInfo licenseInfo, String type,
                                  boolean hasSample) throws Exception {
        XmlUtils.getNode(doc, xpath, String.format("/rat-report/resource[@name='%s'][@type='%s']", resource, type));
        if (licenseInfo != null) {
            XmlUtils.getNode(doc, xpath,
                    String.format("/rat-report/resource[@name='%s'][@type='%s']/license[@id='%s'][@family='%s']",
                            resource, type, licenseInfo.id, licenseInfo.family));
            XmlUtils.getNode(doc, xpath,
                    String.format("/rat-report/resource[@name='%s'][@type='%s']/license[@id='%s'][@approval='%s']",
                            resource, type, licenseInfo.id, licenseInfo.approval));
            if (licenseInfo.hasNotes) {
                XmlUtils.getNode(doc, xpath,
                        String.format("/rat-report/resource[@name='%s'][@type='%s']/license[@id='%s']/notes", resource,
                                type, licenseInfo.id));
            }
        }
        if (hasSample) {
            XmlUtils.getNode(doc, xpath,
                    String.format("/rat-report/resource[@name='%s'][@type='%s']/sample", resource, type));
        }
    }

    private ReportConfiguration initializeConfiguration() throws IOException {
        Defaults defaults = Defaults.builder().build();
        final File elementsFile = new File(Resources.getResourceDirectory("elements/Source.java"));
        final ReportConfiguration configuration = new ReportConfiguration();
        configuration.setFrom(defaults);
        DocumentName documentName = new DocumentName(elementsFile);
        configuration.setReportable(new DirectoryWalker(new FileDocument(documentName, elementsFile,
                configuration.getNameMatcher(documentName))));
        return configuration;
    }

    @Test
    public void xmlReportTest() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();


        ReportConfiguration configuration = initializeConfiguration();
        configuration.setStyleSheet(StyleSheets.XML.getStyleSheet());
        configuration.setOut(() -> out);
        new Reporter(configuration).output();
        Document doc = XmlUtils.toDom(new ByteArrayInputStream(out.toByteArray()));

        XPath xPath = XPathFactory.newInstance().newXPath();

        XmlUtils.getNode(doc, xPath, "/rat-report[@timestamp]");

        LicenseInfo apacheLic = new LicenseInfo("AL", true, false);
        checkNode(doc, xPath, "/ILoggerFactory.java", new LicenseInfo("MIT", true, false),
                "STANDARD", false);
        checkNode(doc, xPath, "/Image.png", null, "BINARY", false);
        checkNode(doc, xPath, "/LICENSE", null, "NOTICE", false);
        checkNode(doc, xPath, "/NOTICE", null, "NOTICE", false);
        checkNode(doc, xPath, "/Source.java", new LicenseInfo("?????", false, false),
                "STANDARD", true);
        checkNode(doc, xPath, "/Text.txt", apacheLic, "STANDARD", false);
        checkNode(doc, xPath, "/TextHttps.txt", apacheLic, "STANDARD", false);
        checkNode(doc, xPath, "/Xml.xml", apacheLic, "STANDARD", false);
        checkNode(doc, xPath, "/buildr.rb", apacheLic, "STANDARD", false);
        checkNode(doc, xPath, "/dummy.jar", null, "ARCHIVE", false);
        checkNode(doc, xPath, "/sub/Empty.txt", new LicenseInfo("?????", false, false),
                "STANDARD", false);
        checkNode(doc, xPath, "/tri.txt", apacheLic, "STANDARD", false);
        checkNode(doc, xPath, "/tri.txt", new LicenseInfo("BSD-3", true, false), "STANDARD",
                false);
        checkNode(doc, xPath, "/tri.txt", new LicenseInfo("TMF", "BSD-3", true, false),
                "STANDARD", false);
        checkNode(doc, xPath, "/generated.txt", new LicenseInfo("GEN", true, true),
                "GENERATED", false);
        NodeList nodeList = (NodeList) xPath.compile("/rat-report/resource").evaluate(doc, XPathConstants.NODESET);
        assertEquals(14, nodeList.getLength());
    }

    private static final String NL = System.lineSeparator();
    private static final String PARAGRAPH = "*****************************************************";
    private static final String HEADER = NL + PARAGRAPH + NL + //
            "Summary" + NL + //
            "-------" + NL + //
            "Generated at: ";

    @Test
    public void plainReportTest() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ReportConfiguration configuration = initializeConfiguration();
        configuration.setOut(() -> out);
        new Reporter(configuration).output();

        out.flush();
        String document = out.toString();

        TextUtils.assertNotContains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", document);
        assertTrue(document.startsWith(HEADER), "'Generated at' is not present in " + document);

        TextUtils.assertPatternInTarget("^Notes: 2$", document);
        TextUtils.assertPatternInTarget("^Binaries: 2$", document);
        TextUtils.assertPatternInTarget("^Archives: 1$", document);
        TextUtils.assertPatternInTarget("^Standards: 8$", document);
        TextUtils.assertPatternInTarget("^Apache Licensed: 5$", document);
        TextUtils.assertPatternInTarget("^Generated Documents: 1$", document);
        TextUtils.assertPatternInTarget("^2 Unknown Licenses$", document);
        TextUtils.assertPatternInTarget(
                "^Files with unapproved licenses:\\s+" //
                        + "\\Q/Source.java\\E\\s+" //
                        + "\\Q/sub/Empty.txt\\E\\s",
                document);
        TextUtils.assertPatternInTarget(ReporterTestUtils.documentOut(true, Type.ARCHIVE, "/dummy.jar"),
                document);
        TextUtils.assertPatternInTarget(
                ReporterTestUtils.documentOut(true, Type.STANDARD, "/ILoggerFactory.java")
                        + ReporterTestUtils.licenseOut("MIT", "The MIT License"),
                document);
        TextUtils.assertPatternInTarget(ReporterTestUtils.documentOut(true, Type.BINARY, "/Image.png"),
                document);
        TextUtils.assertPatternInTarget(ReporterTestUtils.documentOut(true, Type.NOTICE, "/LICENSE"),
                document);
        TextUtils.assertPatternInTarget(ReporterTestUtils.documentOut(true, Type.NOTICE, "/NOTICE"), document);
        TextUtils.assertPatternInTarget(ReporterTestUtils.documentOut(false, Type.STANDARD, "/Source.java")
                + ReporterTestUtils.UNKNOWN_LICENSE, document);
        TextUtils.assertPatternInTarget(ReporterTestUtils.documentOut(true, Type.STANDARD, "/Text.txt")
                + ReporterTestUtils.APACHE_LICENSE, document);
        TextUtils.assertPatternInTarget(ReporterTestUtils.documentOut(true, Type.STANDARD, "/Xml.xml")
                + ReporterTestUtils.APACHE_LICENSE, document);
        TextUtils.assertPatternInTarget(ReporterTestUtils.documentOut(true, Type.STANDARD, "/buildr.rb")
                + ReporterTestUtils.APACHE_LICENSE, document);
        TextUtils.assertPatternInTarget(ReporterTestUtils.documentOut(true, Type.STANDARD, "/TextHttps.txt")
                + ReporterTestUtils.APACHE_LICENSE, document);
        TextUtils.assertPatternInTarget(ReporterTestUtils.documentOut(true, Type.STANDARD, "/tri.txt")
                + ReporterTestUtils.APACHE_LICENSE + ReporterTestUtils.licenseOut("BSD-3", "BSD 3 clause")
                + ReporterTestUtils.licenseOut("BSD-3", "TMF", "The Telemanagement Forum License"), document);
        TextUtils.assertPatternInTarget(ReporterTestUtils.documentOut(false, Type.STANDARD, "/sub/Empty.txt")
                + ReporterTestUtils.UNKNOWN_LICENSE, document);
    }

    @Test
    public void unapprovedLicensesReportTest() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ReportConfiguration configuration = initializeConfiguration();
        configuration.setOut(() -> out);
        configuration.setStyleSheet(this.getClass().getResource("/org/apache/rat/unapproved-licenses.xsl"));
        new Reporter(configuration).output();

        out.flush();
        String document = out.toString();

        assertTrue(document.startsWith("Generated at: "), "'Generated at' is not present in " + document);

        TextUtils.assertPatternInTarget("\\Q/Source.java\\E$", document);
        TextUtils.assertPatternInTarget("\\Q/sub/Empty.txt\\E", document);
    }

    @Test
    public void maxUnapprovedTest() throws Exception {
        ReportConfiguration config = initializeConfiguration();
        Reporter reporter = new Reporter(config);
        reporter.output();
        assertTrue(config.getClaimValidator().hasErrors());
        assertFalse(config.getClaimValidator().isValid(ClaimStatistic.Counter.UNAPPROVED, reporter.getClaimsStatistic().getCounter(ClaimStatistic.Counter.UNAPPROVED)));

        config = initializeConfiguration();
        config.setMaximumUnapprovedLicenses(2);
        reporter = new Reporter(config);
        reporter.output();
        assertFalse(config.getClaimValidator().hasErrors());
        assertTrue(config.getClaimValidator().isValid(ClaimStatistic.Counter.UNAPPROVED, reporter.getClaimsStatistic().getCounter(ClaimStatistic.Counter.UNAPPROVED)));
    }

    private static class LicenseInfo {
        final String id;
        final String family;
        final boolean approval;
        final boolean hasNotes;

        LicenseInfo(String id, boolean approval, boolean hasNotes) {
            this(id, id, approval, hasNotes);
        }

        LicenseInfo(String id, String family, boolean approval, boolean hasNotes) {
            this.id = id;
            this.family = ILicenseFamily.makeCategory(family);
            this.approval = approval;
            this.hasNotes = hasNotes;
        }
    }
}
