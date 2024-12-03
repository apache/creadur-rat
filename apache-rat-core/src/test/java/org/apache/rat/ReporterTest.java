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

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.rat.api.Document.Type;
import org.apache.rat.api.RatException;
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
import org.xml.sax.SAXException;

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
    public void testExecute() throws RatException, ParseException {
        File output = new File(tempDirectory, "testExecute");

        CommandLine cl = new DefaultParser().parse(OptionCollection.buildOptions(), new String[]{"--output-style", "xml", "--output-file", output.getPath(), basedir});
        ReportConfiguration config = OptionCollection.createConfiguration(cl);
        ClaimStatistic statistic = new Reporter(config).execute();

        assertEquals(1, statistic.getCounter(Type.ARCHIVE));
        assertEquals(2, statistic.getCounter(Type.BINARY));
        assertEquals(1, statistic.getCounter(Type.IGNORED));
        assertEquals(2, statistic.getCounter(Type.NOTICE));
        assertEquals(8, statistic.getCounter(Type.STANDARD));
        assertEquals(0, statistic.getCounter(Type.UNKNOWN));
        assertEquals(9, statistic.getCounter(ClaimStatistic.Counter.APPROVED));
        assertEquals(1, statistic.getCounter(ClaimStatistic.Counter.ARCHIVES));
        assertEquals(2, statistic.getCounter(ClaimStatistic.Counter.BINARIES));
        assertEquals(5, statistic.getCounter(ClaimStatistic.Counter.DOCUMENT_TYPES));
        assertEquals(1, statistic.getCounter(ClaimStatistic.Counter.IGNORED));
        assertEquals(5, statistic.getCounter(ClaimStatistic.Counter.LICENSE_CATEGORIES));
        assertEquals(6, statistic.getCounter(ClaimStatistic.Counter.LICENSE_NAMES));
        assertEquals(2, statistic.getCounter(ClaimStatistic.Counter.NOTICES));
        assertEquals(8, statistic.getCounter(ClaimStatistic.Counter.STANDARDS));
        assertEquals(2, statistic.getCounter(ClaimStatistic.Counter.UNAPPROVED));
        assertEquals(2, statistic.getCounter(ClaimStatistic.Counter.UNKNOWN));

        List<Type> typeList = statistic.getDocumentTypes();
        assertEquals(Arrays.asList(Type.ARCHIVE, Type.BINARY, Type.IGNORED, Type.NOTICE, Type.STANDARD), typeList);

        TreeMap<String, Integer> expected = new TreeMap<>();
        expected.put("Unknown license", 2);
        expected.put("Apache License Version 2.0", 5);
        expected.put("The MIT License", 1);
        expected.put("BSD 3 clause", 1);
        expected.put("Generated Files", 1);
        expected.put("The Telemanagement Forum License", 1);
        TreeMap<String, Integer> actual = new TreeMap<>();

        for (String licenseName : statistic.getLicenseNames()) {
            actual.put(licenseName, statistic.getLicenseNameCount(licenseName));
        }
        assertEquals(expected, actual);

        expected.clear();
        expected.put("?????", 2);
        expected.put("AL   ", 5);
        expected.put("BSD-3", 2);
        expected.put("GEN  ", 1);
        expected.put("MIT  ", 1);
        actual.clear();
        for (String licenseCategory : statistic.getLicenseFamilyCategories()) {
            actual.put(licenseCategory, statistic.getLicenseCategoryCount(licenseCategory));
        }
        assertEquals(expected, actual);
    }

    @Test
    public void testOutputOption() throws Exception {
        File output = new File(tempDirectory, "test");
        CommandLine commandLine = new DefaultParser().parse(OptionCollection.buildOptions(), new String[]{"-o", output.getCanonicalPath(), basedir});
        ReportConfiguration config = OptionCollection.createConfiguration(commandLine);
        new Reporter(config).output();
        assertTrue(output.exists());
        String content = FileUtils.readFileToString(output, StandardCharsets.UTF_8);
        TextUtils.assertPatternInTarget("^Unapproved:\\s*2 ", content);
        assertTrue(content.contains("/Source.java"));
        assertTrue(content.contains("/sub/Empty.txt"));
    }

    @Test
    public void testDefaultOutput() throws Exception {
        File output = new File(tempDirectory, "testDefaultOutput");

        PrintStream origin = System.out;
        try (PrintStream out = new PrintStream(output)) {
            System.setOut(out);
            CommandLine commandLine = new DefaultParser().parse(OptionCollection.buildOptions(), new String[]{basedir});
            ReportConfiguration config = OptionCollection.createConfiguration(commandLine);
            new Reporter(config).output();
        } finally {
            System.setOut(origin);
        }
        assertTrue(output.exists());
        String content = FileUtils.readFileToString(output, StandardCharsets.UTF_8);
        verifyStandardContent(content);
    }

    @Test
    public void testXMLOutput() throws Exception {
        File output = new File(tempDirectory, "testXMLOutput");

        CommandLine commandLine = new DefaultParser().parse(OptionCollection.buildOptions(), new String[]{"--output-style", "xml", "--output-file", output.getPath(), basedir});
        ReportConfiguration config = OptionCollection.createConfiguration(commandLine);
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

        // IGNORED, UNKNOWN, ARCHIVE, NOTICE, BINARY, STANDARD
        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource[@type='STANDARD']");
        assertEquals(8, nodeList.getLength());

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource[@type='ARCHIVE']");
        assertEquals(1, nodeList.getLength());

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource[@type='BINARY']");
        assertEquals(2, nodeList.getLength());

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource[@type='IGNORED']");
        assertEquals(1, nodeList.getLength());

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource[@type='UNKNOWN']");
        assertEquals(0, nodeList.getLength());

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource[@type='NOTICE']");
        assertEquals(2, nodeList.getLength());

        nodeList = XmlUtils.getNodeList(doc, xPath, "/rat-report/resource[@type='IGNORED']/license/notes");
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
     * @throws Exception on XPath error.
     */
    private static void checkNode(final Document doc, final XPath xpath, final String resource, final LicenseInfo licenseInfo,
                                  final String type) throws Exception {
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
    }

    private ReportConfiguration initializeConfiguration() throws IOException {
        Defaults defaults = Defaults.builder().build();
        final File elementsFile = new File(Resources.getResourceDirectory("elements/Source.java"));
        final ReportConfiguration configuration = new ReportConfiguration();
        configuration.setFrom(defaults);
        DocumentName documentName = DocumentName.builder(elementsFile).build();
        configuration.addSource(new DirectoryWalker(new FileDocument(documentName, elementsFile,
                configuration.getNameMatcher(documentName))));
        return configuration;
    }

    private void verifyStandardContent(final String document) {
        TextUtils.assertPatternInTarget("^Notices:\\s*2 ", document);
        TextUtils.assertPatternInTarget("^Binaries:\\s*2 ", document);
        TextUtils.assertPatternInTarget("^Archives:\\s*1 ", document);
        TextUtils.assertPatternInTarget("^Standards:\\s*8 ", document);
        TextUtils.assertPatternInTarget("^Ignored:\\s*1 ", document);
        TextUtils.assertPatternInTarget("^Unapproved:\\s*2 ", document);
        TextUtils.assertPatternInTarget("^Unknown:\\s*2 ", document);

        TextUtils.assertPatternInTarget("^Apache License Version 2.0: 5 ", document);
        TextUtils.assertPatternInTarget("^BSD 3 clause: 1 ", document);
        TextUtils.assertPatternInTarget("^Generated Files: 1 ", document);
        TextUtils.assertPatternInTarget("^The MIT License: 1 ", document);
        TextUtils.assertPatternInTarget("^The Telemanagement Forum License: 1 ", document);
        TextUtils.assertPatternInTarget("^Unknown license: 2 ", document);

        TextUtils.assertPatternInTarget("^\\Q?????\\E: 2 ", document);
        TextUtils.assertPatternInTarget("^AL   : 5 ", document);
        TextUtils.assertPatternInTarget("^BSD-3: 2 ", document);
        TextUtils.assertPatternInTarget("^GEN  : 1 ", document);
        TextUtils.assertPatternInTarget("^MIT  : 1 ", document);

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

    private Validator initValidator() throws SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source schemaFile = new StreamSource(Reporter.class.getResourceAsStream("/org/apache/rat/rat-report.xsd"));
        Schema schema = factory.newSchema(schemaFile);
        return schema.newValidator();
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
                "STANDARD");
        checkNode(doc, xPath, "/Image.png", null, "BINARY");
        checkNode(doc, xPath, "/LICENSE", null, "NOTICE");
        checkNode(doc, xPath, "/NOTICE", null, "NOTICE");
        checkNode(doc, xPath, "/Source.java", new LicenseInfo("?????", false, false),
                "STANDARD");
        checkNode(doc, xPath, "/Text.txt", apacheLic, "STANDARD");
        checkNode(doc, xPath, "/TextHttps.txt", apacheLic, "STANDARD");
        checkNode(doc, xPath, "/Xml.xml", apacheLic, "STANDARD");
        checkNode(doc, xPath, "/buildr.rb", apacheLic, "STANDARD");
        checkNode(doc, xPath, "/dummy.jar", null, "ARCHIVE");
        checkNode(doc, xPath, "/sub/Empty.txt", new LicenseInfo("?????", false, false),
                "STANDARD");
        checkNode(doc, xPath, "/tri.txt", apacheLic, "STANDARD");
        checkNode(doc, xPath, "/tri.txt", new LicenseInfo("BSD-3", true, false), "STANDARD");
        checkNode(doc, xPath, "/tri.txt", new LicenseInfo("TMF", "BSD-3", true, false),
                "STANDARD");
        checkNode(doc, xPath, "/generated.txt", new LicenseInfo("GEN", true, true),
                "IGNORED");
        NodeList nodeList = (NodeList) xPath.compile("/rat-report/resource").evaluate(doc, XPathConstants.NODESET);
        assertEquals(14, nodeList.getLength());
        Validator validator = initValidator();
        try {
            validator.validate(new DOMSource(doc));
        } catch (SAXException e) {
            fail("Missing properties?", e);
        }
    }

    private static final String NL = System.lineSeparator();
    private static final String PARAGRAPH = "*****************************************************";
    private static final String HEADER = NL + PARAGRAPH + NL + //
            "Summary" + NL + //
            PARAGRAPH + NL + //
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

        verifyStandardContent(document);
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

        TextUtils.assertContains("Generated at: ", document );
        TextUtils.assertPatternInTarget("\\Q/Source.java\\E$", document);
        TextUtils.assertPatternInTarget("\\Q/sub/Empty.txt\\E", document);
    }

    @Test
    public void counterMaxTest() throws Exception {
        ReportConfiguration config = initializeConfiguration();
        Reporter reporter = new Reporter(config);
        reporter.output();
        assertTrue(config.getClaimValidator().hasErrors());
        assertFalse(config.getClaimValidator().isValid(ClaimStatistic.Counter.UNAPPROVED, reporter.getClaimsStatistic().getCounter(ClaimStatistic.Counter.UNAPPROVED)));

        config = initializeConfiguration();
        config.getClaimValidator().setMax(ClaimStatistic.Counter.UNAPPROVED, 2);
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
