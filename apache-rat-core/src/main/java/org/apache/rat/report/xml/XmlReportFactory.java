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

import java.util.ArrayList;
import java.util.List;

import org.apache.rat.ReportConfiguration;
import org.apache.rat.analysis.DefaultAnalyserFactory;
import org.apache.rat.document.DocumentAnalyserMultiplexer;
import org.apache.rat.document.IDocumentAnalyser;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.policy.DefaultPolicy;
import org.apache.rat.report.ConfigurationReport;
import org.apache.rat.report.RatReport;
import org.apache.rat.report.claim.ClaimAggregator;
import org.apache.rat.report.claim.ClaimReporterMultiplexer;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.report.claim.ClaimValidatorReport;
import org.apache.rat.report.claim.LicenseAddingReport;
import org.apache.rat.report.claim.SimpleXmlClaimReporter;
import org.apache.rat.report.xml.writer.IXmlWriter;

/**
 * A factory to create reports from a writer and a configuration.
 */
public final class XmlReportFactory {

    private XmlReportFactory() {
        // Do not instantiate
    }

    /**
     * Creates a RatReport from the arguments.
     * The {@code statistic} is used to create a ClaimAggregator.
     * If the {@code configuration} indicates that licenses should be added a LicenseAddingReport is added.
     *
     * @param writer        The XML writer to send output to.
     * @param statistic     the ClaimStatistics for the report. may be null.
     * @param configuration The report configuration.
     * @return a RatReport instance.
     */
    public static RatReport createStandardReport(final IXmlWriter writer, final ClaimStatistic statistic, final ReportConfiguration configuration) {
        final List<RatReport> reporters = new ArrayList<>();
        if (statistic != null) {
            reporters.add(new ClaimAggregator(statistic));
        }
        if (configuration.isAddingLicenses() && !configuration.isDryRun()) {
            reporters.add(new LicenseAddingReport(configuration.getCopyrightMessage(), configuration.isAddingLicensesForced()));
        }

        if (configuration.listFamilies() != LicenseFilter.NONE || configuration.listLicenses() != LicenseFilter.NONE) {

            reporters.add(new ConfigurationReport(writer, configuration));
        }

        reporters.add(new SimpleXmlClaimReporter(writer));
        reporters.add(new ClaimValidatorReport(writer, statistic, configuration));

        final IDocumentAnalyser analyser = DefaultAnalyserFactory.createDefaultAnalyser(configuration);
        final DefaultPolicy policy = new DefaultPolicy(configuration.getLicenseFamilies(LicenseFilter.APPROVED));

        final IDocumentAnalyser[] analysers = {analyser, policy};
        DocumentAnalyserMultiplexer analysisMultiplexer = new DocumentAnalyserMultiplexer(analysers);
        return new ClaimReporterMultiplexer(writer, configuration.isDryRun(), analysisMultiplexer, reporters);
    }
}
