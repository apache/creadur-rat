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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.StringWriter;
import java.util.regex.Pattern;

import org.apache.rat.ConfigurationException;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.analysis.IHeaderMatcher.State;
import org.apache.rat.api.MetaData;
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
import org.apache.rat.walker.DirectoryWalker;
import org.junit.Before;
import org.junit.Test;

public class XmlReportFactoryTest {

    private static final Pattern IGNORE_EMPTY = Pattern.compile(".svn|Empty.txt");
    private ILicenseFamily family = ILicenseFamily.builder().setLicenseFamilyCategory("TEST")
            .setLicenseFamilyName("Testing family").build();

    private StringWriter out;
    private IXmlWriter writer;

    @Before
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

        ILicenseFamily family = ILicenseFamily.builder().setLicenseFamilyCategory("TEST")
                .setLicenseFamilyName("Testing family").build();
        
        final TestingLicense testingLicense = new TestingLicense(new TestingMatcher(true), family);

        DirectoryWalker directory = new DirectoryWalker(new File(elementsPath), IGNORE_EMPTY);
        final ClaimStatistic statistic = new ClaimStatistic();
        final ReportConfiguration configuration = new ReportConfiguration();
        configuration.addLicense(testingLicense);
        RatReport report = XmlReportFactory.createStandardReport(writer, statistic, configuration);
        report.startReport();
        report(directory, report);
        report.endReport();
        writer.closeDocument();
        final String output = out.toString();
        assertTrue("Preamble and document element are OK",
                output.startsWith("<?xml version='1.0'?>" + "<rat-report timestamp="));

        assertTrue("Is well formed", XmlUtils.isWellFormedXml(output));
        assertEquals("Binary files", Integer.valueOf(2),
                statistic.getDocumentCategoryMap().get(MetaData.RAT_DOCUMENT_CATEGORY_VALUE_BINARY));
        assertEquals("Notice files", Integer.valueOf(2),
                statistic.getDocumentCategoryMap().get(MetaData.RAT_DOCUMENT_CATEGORY_VALUE_NOTICE));
        assertEquals("Standard files", Integer.valueOf(6),
                statistic.getDocumentCategoryMap().get(MetaData.RAT_DOCUMENT_CATEGORY_VALUE_STANDARD));
        assertEquals("Archives", Integer.valueOf(1),
                statistic.getDocumentCategoryMap().get(MetaData.RAT_DOCUMENT_CATEGORY_VALUE_ARCHIVE));
    }

    @Test
    public void testNoLicense() throws Exception {

        final ILicense mockLicense = mock(ILicense.class);
        when(mockLicense.matches(any())).thenReturn(State.t);
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
