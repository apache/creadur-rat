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

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.StringWriter;

import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.rat.ConfigurationException;
import org.apache.rat.Defaults;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.api.Document;
import org.apache.rat.document.impl.FileDocument;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.report.RatReport;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.report.xml.writer.IXmlWriter;
import org.apache.rat.report.xml.writer.impl.base.XmlWriter;
import org.apache.rat.test.utils.Resources;
import org.apache.rat.testhelpers.TestingLicense;
import org.apache.rat.testhelpers.TestingMatcher;
import org.apache.rat.testhelpers.XmlUtils;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.walker.DirectoryWalker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class XmlReportFactoryTest {

    private final ILicenseFamily family = ILicenseFamily.builder().setLicenseFamilyCategory("TEST")
            .setLicenseFamilyName("Testing family").build();

    private StringWriter out;
    private IXmlWriter writer;

    @BeforeEach
    public void setUp() throws Exception {
        out = new StringWriter();
        writer = new XmlWriter(out);
        writer.startDocument();
    }

    private void report(DirectoryWalker directory, RatReport report) throws Exception {
        directory.run(report);
    }

    @Test
    public void standardReport() throws Exception {
        final String elementsPath = Resources.getResourceDirectory("elements/Source.java");
        final ReportConfiguration configuration = new ReportConfiguration();
        final TestingLicense testingLicense = new TestingLicense("TEST", new TestingMatcher(true), family);
        configuration.setFrom(Defaults.builder().build());
        configuration.setDirectoriesToIgnore(HiddenFileFilter.HIDDEN);
        DirectoryWalker directory = new DirectoryWalker(configuration, new FileDocument(new File(elementsPath)));
        final ClaimStatistic statistic = new ClaimStatistic();

        configuration.addLicense(testingLicense);
        RatReport report = XmlReportFactory.createStandardReport(writer, statistic, configuration);
        report.startReport();
        report(directory, report);
        report.endReport();
        writer.closeDocument();
        final String output = out.toString();
        assertTrue(output.startsWith("<?xml version='1.0'?>" + "<rat-report timestamp="),
                "Preamble and document element are OK");

        assertTrue(XmlUtils.isWellFormedXml(output), "Is well formed");
        assertEquals(2, statistic.getCounter(Document.Type.BINARY), "Binary files");
        assertEquals(2, statistic.getCounter(Document.Type.NOTICE), "Notice files");
        assertEquals(8, statistic.getCounter(Document.Type.STANDARD), "Standard files");
        assertEquals(1, statistic.getCounter(Document.Type.ARCHIVE), "Archives");
    }

    @Test
    public void testNoLicense()  {

        final ILicense mockLicense = mock(ILicense.class);
        when(mockLicense.matches(any())).thenReturn(true);
        when(mockLicense.getLicenseFamily()).thenReturn(family);

        final ClaimStatistic statistic = new ClaimStatistic();
        final ReportConfiguration configuration = new ReportConfiguration();
        // configuration.addLicense(mockLicense);
        try {
            XmlReportFactory.createStandardReport(writer, statistic, configuration);
            fail("Should have thrown exception");
        } catch (ConfigurationException e) {
            // expected;
        }
    }
}
