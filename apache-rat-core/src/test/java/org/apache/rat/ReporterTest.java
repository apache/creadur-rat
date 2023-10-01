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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.report.xml.XmlUtils;
import org.apache.rat.test.utils.Resources;
import org.apache.rat.walker.DirectoryWalker;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ReporterTest {

    private Node checkNode(Document doc, XPath xpath, String resource, String family, String approval, String type)
            throws Exception {

        Node root = getNode(doc, xpath, String.format("/rat-report/resource[@name='%s']", resource));
        if (family != null) {
            getNode(root, xpath, String.format("header-type[@name='%s']", ILicenseFamily.makeCategory(family)));
            getNode(root, xpath, "license-family[@name]");
            if (family.equals("?????")) {
                getNode(root, xpath, "header-sample");
            }
        }
        getNode(root, xpath, String.format("license-approval[@name='%s']", approval));
        getNode(root, xpath, String.format("type[@name='%s']", type));
        return root;
    }

    private Node getNode(Object source, XPath xPath, String xpath) throws XPathExpressionException {
        NodeList nodeList = (NodeList) xPath.compile(xpath).evaluate(source, XPathConstants.NODESET);
        assertEquals("Could not find " + xpath, 1, nodeList.getLength());
        return nodeList.item(0);
    }

    @Test
    public void xmlReportTest() throws Exception {
        Defaults defaults = Defaults.builder().build();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        final String elementsPath = Resources.getResourceDirectory("elements/Source.java");
        final ReportConfiguration configuration = new ReportConfiguration();
        configuration.setStyleReport(false);
        configuration.setFrom(defaults);
        configuration.setReportable(new DirectoryWalker(new File(elementsPath)));
        configuration.setOut(()->out);
        Reporter.report(configuration);
        Document doc = XmlUtils.toDom(new ByteArrayInputStream(out.toByteArray()));
        XPath xPath = XPathFactory.newInstance().newXPath();

        getNode(doc, xPath, "/rat-report[@timestamp]");

        checkNode(doc, xPath, "src/test/resources/elements/ILoggerFactory.java", "MIT", "true", "standard");
        checkNode(doc, xPath, "src/test/resources/elements/Image.png", null, "false", "binary");
        checkNode(doc, xPath, "src/test/resources/elements/LICENSE", null, "false", "notice");
        checkNode(doc, xPath, "src/test/resources/elements/NOTICE", null, "false", "notice");
        checkNode(doc, xPath, "src/test/resources/elements/Source.java", "?????", "false", "standard");
        checkNode(doc, xPath, "src/test/resources/elements/Text.txt", "AL", "true", "standard");
        checkNode(doc, xPath, "src/test/resources/elements/TextHttps.txt", "AL", "true", "standard");
        checkNode(doc, xPath, "src/test/resources/elements/Xml.xml", "AL", "true", "standard");
        checkNode(doc, xPath, "src/test/resources/elements/buildr.rb", "AL", "true", "standard");
        checkNode(doc, xPath, "src/test/resources/elements/dummy.jar", null, "false", "archive");
        checkNode(doc, xPath, "src/test/resources/elements/plain.json", null, "false", "binary");
        checkNode(doc, xPath, "src/test/resources/elements/sub/Empty.txt", "?????", "false", "standard");

        NodeList nodeList = (NodeList) xPath.compile("/rat-report/resource").evaluate(doc, XPathConstants.NODESET);
        assertEquals(12, nodeList.getLength());
    }

    private static final String NL = System.getProperty("line.separator");
    private static final String PARAGRAPH = "*****************************************************";
    private static final String HEADER = NL + PARAGRAPH + NL + //
            "Summary" + NL + //
            "-------" + NL + //
            "Generated at: ";

    @Test
    public void plainReportWithArchivesAndUnapprovedLicenses() throws Exception {
        Defaults defaults = Defaults.builder().build();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        final String elementsPath = Resources.getResourceDirectory("elements/Source.java");
        final ReportConfiguration configuration = new ReportConfiguration();
        configuration.setFrom(defaults);
        configuration.setReportable(new DirectoryWalker(new File(elementsPath)));
        configuration.setOut(()->out);
        Reporter.report(configuration);

        String document = out.toString();
        //System.out.println(document);
        assertTrue("'Generated at' is present in " + document, document.startsWith(HEADER));

        // final int generatedAtLineEnd = document.indexOf(NL, HEADER.length());
        find("^Notes: 2$", document);
        find("^Binaries: 2$", document);
        find("^Archives: 1$", document);
        find("^Standards: 7$", document);
        find("^Apache Licensed: 4$", document);
        find("^Generated Documents: 0$", document);
        find("^2 Unknown Licenses$", document);
        find("^Files with unapproved licenses:\\s+" + "src/test/resources/elements/Image.png\\s+"
                + "src/test/resources/elements/LICENSE\\s+" + "src/test/resources/elements/NOTICE\\s+"
                + "src/test/resources/elements/Source.java\\s+"
                + "src/test/resources/elements/dummy.jar\\s+" + "src/test/resources/elements/plain.json\\s+"
                + "src/test/resources/elements/sub/Empty.txt\\s", document);
        find("^Archives:\\s+" + "\\+ src/test/resources/elements/dummy.jar\\s*", document);
        find("MIT\\s+src/test/resources/elements/ILoggerFactory.java", document);
        find("!B\\s+src/test/resources/elements/Image.png", document);
        find("!N\\s+src/test/resources/elements/LICENSE", document);
        find("!N\\s+src/test/resources/elements/NOTICE", document);
        find("!\\Q?????\\E\\s+src/test/resources/elements/Source.java", document);
        find("AL\\s+src/test/resources/elements/Text.txt", document);
        find("AL\\s+src/test/resources/elements/TextHttps.txt", document);
        find("AL\\s+src/test/resources/elements/Xml.xml", document);
        find("AL\\s+src/test/resources/elements/buildr.rb", document);
        find("!A\\s+src/test/resources/elements/dummy.jar", document);
        find("!B\\s+src/test/resources/elements/plain.json", document);
        find("!\\Q?????\\E\\s+src/test/resources/elements/sub/Empty.txt", document);
        find("== File: src/test/resources/elements/sub/Empty.txt", document);
    }

    private void find(String pattern, String document) {
        assertTrue(String.format("Could not find '%s'", pattern),
                Pattern.compile(pattern, Pattern.MULTILINE).matcher(document).find());
    }
}
