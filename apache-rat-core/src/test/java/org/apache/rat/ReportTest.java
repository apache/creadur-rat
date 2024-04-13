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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.rat.testhelpers.TextUtils;
import org.apache.rat.testhelpers.XmlUtils;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class ReportTest {
    @Test
    public void parseExclusionsForCLIUsage() {
        final FilenameFilter filter = Report
                .parseExclusions(Arrays.asList("", " # foo/bar", "foo", "##", " ./foo/bar"));
        assertNotNull(filter);
    }

    @Test
    public void testDefaultConfiguration() throws ParseException, IOException {
        String[] empty = {};
        CommandLine cl = new DefaultParser().parse(Report.buildOptions(), empty);
        ReportConfiguration config = Report.createConfiguration("", cl);
        ReportConfigurationTest.validateDefault(config);
    }

    @Test
    public void testOutputOption() throws Exception {
        CommandLine cl = new DefaultParser().parse(Report.buildOptions(), new String[] { "-o", "target/test" });
        ReportConfiguration config = Report.createConfiguration("target/test-classes/elements", cl);
        new Reporter(config).output();
        File output = new File("target/test");
        assertTrue(output.exists());
        String content = FileUtils.readFileToString(output, StandardCharsets.UTF_8);
        assertTrue(content.contains("2 Unknown Licenses"));
        assertTrue(content.contains("target/test-classes/elements/Source.java"));
        assertTrue(content.contains("target/test-classes/elements/sub/Empty.txt"));
    }

    @Test
    public void testDefaultOutput() throws Exception {

        File output = new File("target/sysout");
        output.delete();
        PrintStream origin = System.out;
        try {
            System.setOut(new PrintStream(output));
            CommandLine cl = new DefaultParser().parse(Report.buildOptions(), new String[] {});
            ReportConfiguration config = Report.createConfiguration("target/test-classes/elements", cl);
            new Reporter(config).output();
        } finally {
            System.setOut(origin);
        }
        assertTrue(output.exists());
        String content = FileUtils.readFileToString(output, StandardCharsets.UTF_8);
        TextUtils.isMatching("Notes: 2$", content);
        TextUtils.isMatching("Binaries: 2$", content);
        TextUtils.isMatching("Archives: 1$", content);
        TextUtils.isMatching("Standards: 8$", content);
        TextUtils.isMatching("Apache Licensed: 5$", content);
        TextUtils.isMatching("Generated Documents 1$", content);
        TextUtils.isMatching("^2 Unknown licenses", content);
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
        assertTrue(content.contains(" B target/test-classes/elements/plain.json"));
        assertTrue(content.contains("!S target/test-classes/elements/sub/Empty.txt"));
        assertTrue(content.contains(" S target/test-classes/elements/tri.txt"));
        assertTrue(content.contains(" G target/test-classes/elements/generated.txt"));
    }

    @Test
    public void testXMLOutput() throws Exception {

        File output = new File("target/sysout");
        output.delete();
        PrintStream origin = System.out;
        try {
            System.setOut(new PrintStream(output));
            CommandLine cl = new DefaultParser().parse(Report.buildOptions(), new String[] { "-x" });
            ReportConfiguration config = Report.createConfiguration("target/test-classes/elements", cl);
            new Reporter(config).output();
        } finally {
            System.setOut(origin);
        }
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
}
