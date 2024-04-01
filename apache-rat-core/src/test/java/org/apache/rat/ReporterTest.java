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

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.rat.api.Document.Type;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.test.utils.Resources;
import org.apache.rat.testhelpers.TextUtils;
import org.apache.rat.testhelpers.XmlUtils;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.walker.DirectoryWalker;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Tests the output of the Reporter.
 */
public class ReporterTest {

    /**
     * Finds a node via xpath on the document. And then checks family, approval and
     * type of elements of the node.
     * 
     * @param doc The document to check/
     * @param xpath the XPath instance to use.
     * @param resource the xpath statement to locate the node.
     * @param id the expected family for the node (may be null)
     * @param approval the expected approval value (may be null)
     * @param type the type of resource located.
     * @throws Exception on XPath error.
     */
    public static void checkNode(Document doc, XPath xpath, String resource, LicenseInfo licenseInfo, String type,
            boolean hasSample) throws Exception {
        XmlUtils.getNode(doc, xpath, String.format("/rat-report/resource[@name='%s'][@type='%s']", resource, type));
        if (licenseInfo != null) {
            XmlUtils.getNode(doc, xpath,
                    String.format("/rat-report/resource[@name='%s'][@type='%s']/license[@id='%s'][@family='%s']",
                            resource, type, licenseInfo.id, licenseInfo.family));
            XmlUtils.getNode(doc, xpath,
                    String.format("/rat-report/resource[@name='%s'][@type='%s']/license[@id='%s'][@approval='%s']",
                            resource, type, licenseInfo.id, Boolean.toString(licenseInfo.approval)));
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
        Defaults defaults = Defaults.builder().build(DefaultLog.INSTANCE);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        final String elementsPath = Resources.getResourceDirectory("elements/Source.java");
        final ReportConfiguration configuration = new ReportConfiguration(DefaultLog.INSTANCE);
        configuration.setStyleReport(false);
        configuration.setFrom(defaults);
        configuration.setReportable(new DirectoryWalker(new File(elementsPath), HiddenFileFilter.HIDDEN));
        configuration.setOut(() -> out);
        new Reporter(configuration).output();
        Document doc = XmlUtils.toDom(new ByteArrayInputStream(out.toByteArray()));

        XPath xPath = XPathFactory.newInstance().newXPath();

        XmlUtils.getNode(doc, xPath, "/rat-report[@timestamp]");

        LicenseInfo apacheLic = new LicenseInfo("AL", true, false);
        checkNode(doc, xPath, "src/test/resources/elements/ILoggerFactory.java", new LicenseInfo("MIT", true, false),
                "standard", false);
        checkNode(doc, xPath, "src/test/resources/elements/Image.png", null, "binary", false);
        checkNode(doc, xPath, "src/test/resources/elements/LICENSE", null, "notice", false);
        checkNode(doc, xPath, "src/test/resources/elements/NOTICE", null, "notice", false);
        checkNode(doc, xPath, "src/test/resources/elements/Source.java", new LicenseInfo("?????", false, false),
                "standard", true);
        checkNode(doc, xPath, "src/test/resources/elements/Text.txt", apacheLic, "standard", false);
        checkNode(doc, xPath, "src/test/resources/elements/TextHttps.txt", apacheLic, "standard", false);
        checkNode(doc, xPath, "src/test/resources/elements/Xml.xml", apacheLic, "standard", false);
        checkNode(doc, xPath, "src/test/resources/elements/buildr.rb", apacheLic, "standard", false);
        checkNode(doc, xPath, "src/test/resources/elements/dummy.jar", null, "archive", false);
        checkNode(doc, xPath, "src/test/resources/elements/plain.json", null, "binary", false);
        checkNode(doc, xPath, "src/test/resources/elements/sub/Empty.txt", new LicenseInfo("?????", false, false),
                "standard", false);
        checkNode(doc, xPath, "src/test/resources/elements/tri.txt", apacheLic, "standard", false);
        checkNode(doc, xPath, "src/test/resources/elements/tri.txt", new LicenseInfo("BSD-3", true, false), "standard",
                false);
        checkNode(doc, xPath, "src/test/resources/elements/tri.txt", new LicenseInfo("TMF", "BSD-3", true, false),
                "standard", false);
        checkNode(doc, xPath, "src/test/resources/elements/generated.txt", new LicenseInfo("GEN", true, true),
                "generated", false);
        NodeList nodeList = (NodeList) xPath.compile("/rat-report/resource").evaluate(doc, XPathConstants.NODESET);
        assertEquals(14, nodeList.getLength());
    }

    private static final String NL = System.getProperty("line.separator");
    private static final String PARAGRAPH = "*****************************************************";
    private static final String HEADER = NL + PARAGRAPH + NL + //
            "Summary" + NL + //
            "-------" + NL + //
            "Generated at: ";

    private String documentOut(boolean approved, Type type, String name) {
        return String.format("^\\Q%s%s %s\\E$", approved ? " " : "!", type.name().substring(0, 1), name);
    }

    private String licenseOut(String family, String name) {
        return licenseOut(family, family, name);
    }

    private String licenseOut(String family, String id, String name) {
        return String.format("\\s+\\Q%s\\E\\s+\\Q%s\\E\\s+\\Q%s\\E$", family, id, name);
    }

    @Test
    public void plainReportTest() throws Exception {
        Defaults defaults = Defaults.builder().build(DefaultLog.INSTANCE);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        final String elementsPath = Resources.getResourceDirectory("elements/Source.java");
        final ReportConfiguration configuration = new ReportConfiguration(DefaultLog.INSTANCE);
        configuration.setFrom(defaults);
        configuration.setReportable(new DirectoryWalker(new File(elementsPath), HiddenFileFilter.HIDDEN));
        configuration.setOut(() -> out);
        new Reporter(configuration).output();

        out.flush();
        String document = out.toString();

        assertTrue(document.startsWith(HEADER), "'Generated at' is not present in " + document);

        TextUtils.assertPatternInOutput("^Notes: 2$", document);
        TextUtils.assertPatternInOutput("^Binaries: 2$", document);
        TextUtils.assertPatternInOutput("^Archives: 1$", document);
        TextUtils.assertPatternInOutput("^Standards: 8$", document);
        TextUtils.assertPatternInOutput("^Apache Licensed: 5$", document);
        TextUtils.assertPatternInOutput("^Generated Documents: 1$", document);
        TextUtils.assertPatternInOutput("^2 Unknown Licenses$", document);
        TextUtils.assertPatternInOutput(
                "^Files with unapproved licenses:\\s+" + "\\Qsrc/test/resources/elements/Source.java\\E\\s+"
                        + "\\Qsrc/test/resources/elements/sub/Empty.txt\\E\\s",
                document);
        TextUtils.assertPatternInOutput(documentOut(true, Type.archive, "src/test/resources/elements/dummy.jar"),
                document);
        TextUtils.assertPatternInOutput(
                documentOut(true, Type.standard, "src/test/resources/elements/ILoggerFactory.java")
                        + licenseOut("MIT", "The MIT License"),
                document);
        TextUtils.assertPatternInOutput(documentOut(true, Type.binary, "src/test/resources/elements/Image.png"),
                document);
        TextUtils.assertPatternInOutput(documentOut(true, Type.notice, "src/test/resources/elements/LICENSE"),
                document);
        TextUtils.assertPatternInOutput(documentOut(true, Type.notice, "src/test/resources/elements/NOTICE"), document);
        TextUtils.assertPatternInOutput(documentOut(false, Type.standard, "src/test/resources/elements/Source.java")
                + licenseOut("?????", "Unknown license (Unapproved)"), document);
        TextUtils.assertPatternInOutput(documentOut(true, Type.standard, "src/test/resources/elements/Text.txt")
                + licenseOut("AL", "Apache License Version 2.0"), document);
        TextUtils.assertPatternInOutput(documentOut(true, Type.standard, "src/test/resources/elements/Xml.xml")
                + licenseOut("AL", "Apache License Version 2.0"), document);
        TextUtils.assertPatternInOutput(documentOut(true, Type.standard, "src/test/resources/elements/buildr.rb")
                + licenseOut("AL", "Apache License Version 2.0"), document);
        TextUtils.assertPatternInOutput(documentOut(true, Type.standard, "src/test/resources/elements/TextHttps.txt")
                + licenseOut("AL", "Apache License Version 2.0"), document);
        TextUtils.assertPatternInOutput(documentOut(true, Type.binary, "src/test/resources/elements/plain.json"),
                document);
        TextUtils.assertPatternInOutput(documentOut(true, Type.standard, "src/test/resources/elements/tri.txt")
                + licenseOut("AL", "Apache License Version 2.0") + licenseOut("BSD-3", "BSD 3 clause")
                + licenseOut("BSD-3", "TMF", "The Telemanagement Forum License"), document);
        TextUtils.assertPatternInOutput(documentOut(false, Type.standard, "src/test/resources/elements/sub/Empty.txt")
                + licenseOut("?????", "Unknown license (Unapproved)"), document);
    }

    @Test
    public void UnapprovedLicensesReportTest() throws Exception {
        Defaults defaults = Defaults.builder().build(DefaultLog.INSTANCE);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        final String elementsPath = Resources.getResourceDirectory("elements/Source.java");
        final ReportConfiguration configuration = new ReportConfiguration(DefaultLog.INSTANCE);
        configuration.setFrom(defaults);
        configuration.setReportable(new DirectoryWalker(new File(elementsPath), HiddenFileFilter.HIDDEN));
        configuration.setOut(() -> out);
        configuration.setStyleSheet(this.getClass().getResource("/org/apache/rat/unapproved-licenses.xsl"));
        new Reporter(configuration).output();

        out.flush();
        String document = out.toString();

        assertTrue(document.startsWith("Generated at: "), "'Generated at' is not present in " + document);

        TextUtils.assertPatternInOutput("\\Qsrc/test/resources/elements/Source.java\\E$", document);
        TextUtils.assertPatternInOutput("\\Qsrc/test/resources/elements/sub/Empty.txt\\E", document);
    }

    private class LicenseInfo {
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
