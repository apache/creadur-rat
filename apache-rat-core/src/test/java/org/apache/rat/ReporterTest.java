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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.rat.api.Document.Type;
import org.apache.rat.commandline.OutputArgs;
import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.document.impl.FileDocument;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.test.utils.Resources;
import org.apache.rat.testhelpers.TextUtils;
import org.apache.rat.testhelpers.XmlUtils;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.walker.DirectoryWalker;
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

    @Test
    public void testOutputOption() throws Exception {
        File output = new File(tempDirectory, "test");
        CommandLine cl = new DefaultParser().parse(OptionCollection.buildOptions(), new String[] { "-o", output.getCanonicalPath()});
        ReportConfiguration config = OptionCollection.createConfiguration(DefaultLog.getInstance(), "target/test-classes/elements", cl);
        new Reporter(config).output();
        assertTrue(output.exists());
        String content = FileUtils.readFileToString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("2 Unknown Licenses"));
        assertTrue(content.contains("target/test-classes/elements/Source.java"));
        assertTrue(content.contains("target/test-classes/elements/sub/Empty.txt"));
    }

    @Test
    public void testDefaultOutput() throws Exception {
        File output = new File(tempDirectory,"sysout");
        output.delete();
        PrintStream origin = System.out;
        try (PrintStream out = new PrintStream(output)){
            System.setOut(out);
            CommandLine cl = new DefaultParser().parse(OptionCollection.buildOptions(), new String[] {});
            ReportConfiguration config = OptionCollection.createConfiguration(DefaultLog.getInstance(), "target/test-classes/elements", cl);
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
        assertTrue(content.contains(" S target/test-classes/elements/ILoggerFactory.java"));
        assertTrue(content.contains(" B target/test-classes/elements/Image.png"));
        assertTrue(content.contains(" N target/test-classes/elements/LICENSE"));
        assertTrue(content.contains(" N target/test-classes/elements/NOTICE"));
        assertTrue(content.contains("!S target/test-classes/elements/Source.java"));
        assertTrue(content.contains(" S target/test-classes/elements/Text.txt"));
        assertTrue(content.contains(" S target/test-classes/elements/TextHttps.txt"));
        assertTrue(content.contains(" S target/test-classes/elements/Xml.xml"));
        assertTrue(content.contains(" S target/test-classes/elements/buildr.rb"));
        assertTrue(content.contains(" A target/test-classes/elements/dummy.jar"));
        assertTrue(content.contains("!S target/test-classes/elements/sub/Empty.txt"));
        assertTrue(content.contains(" S target/test-classes/elements/tri.txt"));
        assertTrue(content.contains(" G target/test-classes/elements/generated.txt"));
    }

    @Test
    public void testXMLOutput() throws Exception {
        File output = new File(tempDirectory,"sysout");
        output.delete();
        PrintStream origin = System.out;

        CommandLine cl = new DefaultParser().parse(OptionCollection.buildOptions(), new String[] { "--output-style", "xml", "--output-file", output.getPath() });
        ReportConfiguration config = OptionCollection.createConfiguration(DefaultLog.getInstance(), "target/test-classes/elements", cl);
        new Reporter(config).output();

        assertTrue(output.exists());
        Document doc = XmlUtils.toDom(new FileInputStream(output));
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
                "/rat-report/resource[@name='target/test-classes/elements/Source.java']/sample");
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

    @Test
    public void xmlReportTest() throws Exception {
        Defaults defaults = Defaults.builder().build(DefaultLog.getInstance());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        final String elementsPath = Resources.getResourceDirectory("elements/Source.java");
        final ReportConfiguration configuration = new ReportConfiguration(DefaultLog.getInstance());
        configuration.setStyleSheet(OutputArgs.getStyleSheet(StyleSheets.XML));
        configuration.setFrom(defaults);
        configuration.setDirectoriesToIgnore(HiddenFileFilter.HIDDEN);
        configuration.setReportable(new DirectoryWalker(configuration, new FileDocument(new File(elementsPath))));
        configuration.setOut(() -> out);
        new Reporter(configuration).output();
        Document doc = XmlUtils.toDom(new ByteArrayInputStream(out.toByteArray()));

        XPath xPath = XPathFactory.newInstance().newXPath();

        XmlUtils.getNode(doc, xPath, "/rat-report[@timestamp]");

        LicenseInfo apacheLic = new LicenseInfo("AL", true, false);
        checkNode(doc, xPath, "src/test/resources/elements/ILoggerFactory.java", new LicenseInfo("MIT", true, false),
                "STANDARD", false);
        checkNode(doc, xPath, "src/test/resources/elements/Image.png", null, "BINARY", false);
        checkNode(doc, xPath, "src/test/resources/elements/LICENSE", null, "NOTICE", false);
        checkNode(doc, xPath, "src/test/resources/elements/NOTICE", null, "NOTICE", false);
        checkNode(doc, xPath, "src/test/resources/elements/Source.java", new LicenseInfo("?????", false, false),
                "STANDARD", true);
        checkNode(doc, xPath, "src/test/resources/elements/Text.txt", apacheLic, "STANDARD", false);
        checkNode(doc, xPath, "src/test/resources/elements/TextHttps.txt", apacheLic, "STANDARD", false);
        checkNode(doc, xPath, "src/test/resources/elements/Xml.xml", apacheLic, "STANDARD", false);
        checkNode(doc, xPath, "src/test/resources/elements/buildr.rb", apacheLic, "STANDARD", false);
        checkNode(doc, xPath, "src/test/resources/elements/dummy.jar", null, "ARCHIVE", false);
        checkNode(doc, xPath, "src/test/resources/elements/sub/Empty.txt", new LicenseInfo("?????", false, false),
                "STANDARD", false);
        checkNode(doc, xPath, "src/test/resources/elements/tri.txt", apacheLic, "STANDARD", false);
        checkNode(doc, xPath, "src/test/resources/elements/tri.txt", new LicenseInfo("BSD-3", true, false), "STANDARD",
                false);
        checkNode(doc, xPath, "src/test/resources/elements/tri.txt", new LicenseInfo("TMF", "BSD-3", true, false),
                "STANDARD", false);
        checkNode(doc, xPath, "src/test/resources/elements/generated.txt", new LicenseInfo("GEN", true, true),
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

    private String documentOut(boolean approved, Type type, String name) {
        return String.format("^\\Q%s%s %s\\E$", approved ? " " : "!", type.name().charAt(0), name);
    }

    private String licenseOut(String family, String name) {
        return licenseOut(family, family, name);
    }

    private String licenseOut(String family, String id, String name) {
        return String.format("\\s+\\Q%s\\E\\s+\\Q%s\\E\\s+\\Q%s\\E$", family, id, name);
    }

    @Test
    public void plainReportTest() throws Exception {
        Defaults defaults = Defaults.builder().build(DefaultLog.getInstance());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        final String elementsPath = Resources.getResourceDirectory("elements/Source.java");
        final ReportConfiguration configuration = new ReportConfiguration(DefaultLog.getInstance());
        configuration.setFrom(defaults);
        configuration.setDirectoriesToIgnore(HiddenFileFilter.HIDDEN);
        configuration.setReportable(new DirectoryWalker(configuration, new FileDocument(new File(elementsPath))));
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
                        + "\\Qsrc/test/resources/elements/Source.java\\E\\s+" //
                        + "\\Qsrc/test/resources/elements/sub/Empty.txt\\E\\s",
                document);
        TextUtils.assertPatternInTarget(documentOut(true, Type.ARCHIVE, "src/test/resources/elements/dummy.jar"),
                document);
        TextUtils.assertPatternInTarget(
                documentOut(true, Type.STANDARD, "src/test/resources/elements/ILoggerFactory.java")
                        + licenseOut("MIT", "The MIT License"),
                document);
        TextUtils.assertPatternInTarget(documentOut(true, Type.BINARY, "src/test/resources/elements/Image.png"),
                document);
        TextUtils.assertPatternInTarget(documentOut(true, Type.NOTICE, "src/test/resources/elements/LICENSE"),
                document);
        TextUtils.assertPatternInTarget(documentOut(true, Type.NOTICE, "src/test/resources/elements/NOTICE"), document);
        TextUtils.assertPatternInTarget(documentOut(false, Type.STANDARD, "src/test/resources/elements/Source.java")
                + licenseOut("?????", "Unknown license (Unapproved)"), document);
        TextUtils.assertPatternInTarget(documentOut(true, Type.STANDARD, "src/test/resources/elements/Text.txt")
                + licenseOut("AL", "Apache License Version 2.0"), document);
        TextUtils.assertPatternInTarget(documentOut(true, Type.STANDARD, "src/test/resources/elements/Xml.xml")
                + licenseOut("AL", "Apache License Version 2.0"), document);
        TextUtils.assertPatternInTarget(documentOut(true, Type.STANDARD, "src/test/resources/elements/buildr.rb")
                + licenseOut("AL", "Apache License Version 2.0"), document);
        TextUtils.assertPatternInTarget(documentOut(true, Type.STANDARD, "src/test/resources/elements/TextHttps.txt")
                + licenseOut("AL", "Apache License Version 2.0"), document);
        TextUtils.assertPatternInTarget(documentOut(true, Type.STANDARD, "src/test/resources/elements/tri.txt")
                + licenseOut("AL", "Apache License Version 2.0") + licenseOut("BSD-3", "BSD 3 clause")
                + licenseOut("BSD-3", "TMF", "The Telemanagement Forum License"), document);
        TextUtils.assertPatternInTarget(documentOut(false, Type.STANDARD, "src/test/resources/elements/sub/Empty.txt")
                + licenseOut("?????", "Unknown license (Unapproved)"), document);
    }

    @Test
    public void UnapprovedLicensesReportTest() throws Exception {
        Defaults defaults = Defaults.builder().build(DefaultLog.getInstance());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        final String elementsPath = Resources.getResourceDirectory("elements/Source.java");
        final ReportConfiguration configuration = new ReportConfiguration(DefaultLog.getInstance());
        configuration.setFrom(defaults);
        configuration.setDirectoriesToIgnore(HiddenFileFilter.HIDDEN);
        configuration.setReportable(new DirectoryWalker(configuration, new FileDocument(new File(elementsPath))));
        configuration.setOut(() -> out);
        configuration.setStyleSheet(this.getClass().getResource("/org/apache/rat/unapproved-licenses.xsl"));
        new Reporter(configuration).output();

        out.flush();
        String document = out.toString();

        assertTrue(document.startsWith("Generated at: "), "'Generated at' is not present in " + document);

        TextUtils.assertPatternInTarget("\\Qsrc/test/resources/elements/Source.java\\E$", document);
        TextUtils.assertPatternInTarget("\\Qsrc/test/resources/elements/sub/Empty.txt\\E", document);
    }

    private static class LicenseInfo {
        String id;
        String family;
        boolean approval;
        boolean hasNotes;

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
