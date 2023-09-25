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

import org.apache.rat.ReportConfiguration;
import org.apache.rat.ReportConfiguration.LicenseFilter;
import org.apache.rat.analysis.DefaultAnalyserFactory;
import org.apache.rat.document.IDocumentAnalyser;
import org.apache.rat.document.impl.util.DocumentAnalyserMultiplexer;
import org.apache.rat.policy.DefaultPolicy;
import org.apache.rat.report.RatReport;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.report.claim.impl.ClaimAggregator;
import org.apache.rat.report.claim.impl.xml.SimpleXmlClaimReporter;
import org.apache.rat.report.claim.util.ClaimReporterMultiplexer;
import org.apache.rat.report.claim.util.LicenseAddingReport;
import org.apache.rat.report.xml.writer.IXmlWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates reports.
 *
 */
public class XmlReportFactory {
    public static final RatReport createStandardReport(IXmlWriter writer,
            final ClaimStatistic statistic, ReportConfiguration configuration) {
        final List<RatReport> reporters = new ArrayList<>();
        if (statistic != null) {
            reporters.add(new ClaimAggregator(statistic));
        }
        if (configuration.isAddingLicenses()) {
            reporters.add(new LicenseAddingReport(configuration.getCopyrightMessage(), configuration.isAddingLicensesForced()));
        }
        reporters.add(new SimpleXmlClaimReporter(writer));

        final IDocumentAnalyser analyser =
            DefaultAnalyserFactory.createDefaultAnalyser(configuration.getLicenses(LicenseFilter.all));
        final DefaultPolicy policy = new DefaultPolicy(configuration.getLicenseFamilies(LicenseFilter.approved));

        final IDocumentAnalyser[] analysers = {analyser, policy};
        DocumentAnalyserMultiplexer analysisMultiplexer = new DocumentAnalyserMultiplexer(analysers);
        return new ClaimReporterMultiplexer(analysisMultiplexer, reporters);
    }
}
