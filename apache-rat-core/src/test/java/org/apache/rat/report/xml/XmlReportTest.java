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
package org.apache.rat.report.xml;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.rat.analysis.DefaultAnalyserFactory;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.matchers.CopyrightMatcher;
import org.apache.rat.analysis.matchers.OrMatcher;
import org.apache.rat.analysis.matchers.SimpleTextMatcher;
import org.apache.rat.document.IDocumentAnalyser;
import org.apache.rat.license.ILicense;
import org.apache.rat.report.AbstractReport;
import org.apache.rat.report.RatReport;
import org.apache.rat.report.claim.impl.xml.SimpleXmlClaimReporter;
import org.apache.rat.report.claim.util.ClaimReporterMultiplexer;
import org.apache.rat.report.xml.writer.IXmlWriter;
import org.apache.rat.report.xml.writer.impl.base.XmlWriter;
import org.apache.rat.test.utils.Resources;
import org.apache.rat.testhelpers.TestingLicense;
import org.apache.rat.testhelpers.XmlUtils;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.walker.DirectoryWalker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

public class XmlReportTest {

    private static final Pattern IGNORE = Pattern.compile(".svn");
    private ByteArrayOutputStream out;
    private IXmlWriter writer;
    private RatReport report;

    @BeforeEach
    public void setUp() throws Exception {
        out = new ByteArrayOutputStream();
        writer = new XmlWriter(new BufferedWriter(new OutputStreamWriter(out)));
        writer.startDocument();
        final SimpleXmlClaimReporter reporter = new SimpleXmlClaimReporter(writer);
        final IHeaderMatcher asf1Matcher = new SimpleTextMatcher("http://www.apache.org/licenses/LICENSE-2.0");
        final IHeaderMatcher asf2Matcher = new SimpleTextMatcher("https://www.apache.org/licenses/LICENSE-2.0.txt");
        final IHeaderMatcher asfMatcher = new OrMatcher(Arrays.asList(asf1Matcher, asf2Matcher));
        final ILicense asfLic = new TestingLicense("ASF", asfMatcher);

        final IHeaderMatcher qosMatcher = new CopyrightMatcher("2004", "2011", "QOS.ch");
        final ILicense qosLic = new TestingLicense("QOS", qosMatcher);

        IDocumentAnalyser analyser = DefaultAnalyserFactory.createDefaultAnalyser(DefaultLog.INSTANCE,Arrays.asList(asfLic, qosLic));
        final List<AbstractReport> reporters = new ArrayList<>();
        reporters.add(reporter);
        report = new ClaimReporterMultiplexer(analyser, reporters);
    }

    private void report(DirectoryWalker directory) throws Exception {
        directory.run(report);
    }

    @Test
    public void baseReport() throws Exception {
        final String elementsPath = Resources.getResourceDirectory("elements/Source.java");
        DirectoryWalker directory = new DirectoryWalker(new File(elementsPath), IGNORE, HiddenFileFilter.HIDDEN);
        report.startReport();
        report(directory);
        report.endReport();
        writer.closeDocument();
        final String output = out.toString();
        assertTrue(output.startsWith("<?xml version='1.0'?>" + "<rat-report timestamp="), "Preamble and document element are OK");
        assertTrue(XmlUtils.isWellFormedXml(output),"Is well formed");

        XPath xPath = XPathFactory.newInstance().newXPath();
        Document doc = XmlUtils.toDom(new ByteArrayInputStream(out.toByteArray()));

        XmlUtils.printDocument(System.out, doc);
        XmlUtils.checkNode(doc, xPath, "src/test/resources/elements/ILoggerFactory.java", "QOS", null, "standard");
        XmlUtils.checkNode(doc, xPath, "src/test/resources/elements/Image.png", null, null, "binary");
        XmlUtils.checkNode(doc, xPath, "src/test/resources/elements/LICENSE", null, null, "notice");
        XmlUtils.checkNode(doc, xPath, "src/test/resources/elements/NOTICE", null, null, "notice");
        XmlUtils.checkNode(doc, xPath, "src/test/resources/elements/Source.java", "?????", null, "standard");
        XmlUtils.checkNode(doc, xPath, "src/test/resources/elements/Text.txt", "ASF", null, "standard");
        XmlUtils.checkNode(doc, xPath, "src/test/resources/elements/TextHttps.txt", "ASF", null, "standard");
        XmlUtils.checkNode(doc, xPath, "src/test/resources/elements/Xml.xml", "ASF", null, "standard");
        XmlUtils.checkNode(doc, xPath, "src/test/resources/elements/buildr.rb", "ASF", null, "standard");
        XmlUtils.checkNode(doc, xPath, "src/test/resources/elements/dummy.jar", null, null, "archive");
        XmlUtils.checkNode(doc, xPath, "src/test/resources/elements/plain.json", null, null, "binary");
        XmlUtils.checkNode(doc, xPath, "src/test/resources/elements/sub/Empty.txt", "?????", null, "standard");
    }

}
