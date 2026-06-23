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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.rat.ReportConfiguration;
import org.apache.rat.analysis.AnalyserFactory;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.api.RatException;
import org.apache.rat.configuration.XMLConfigurationWriter;
import org.apache.rat.document.DocumentAnalyser;
import org.apache.rat.document.RatDocumentAnalysisException;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.report.RatReport;
import org.apache.rat.report.claim.ClaimAggregator;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.report.claim.LicenseAddingReport;
import org.apache.rat.report.xml.writer.XmlWriter;

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
    public static RatReport createStandardReport(final XmlWriter writer, final ClaimStatistic statistic, final ReportConfiguration configuration) {
        final List<RatReport> reporters = new ArrayList<>();
        if (statistic != null) {
            reporters.add(new ClaimAggregator(statistic));
        }
        if (configuration.isAddingLicenses() && !configuration.isDryRun()) {
            reporters.add(new LicenseAddingReport(configuration.getCopyrightMessage(), configuration.isAddingLicensesForced()));
        }

        if (configuration.listFamilies() != LicenseFilter.NONE || configuration.listLicenses() != LicenseFilter.NONE) {
            reporters.add(configuration(writer, configuration));
        }

        reporters.add(simple(writer));
        reporters.add(validator(writer, statistic, configuration));

        return multiplexer(writer, configuration.isDryRun(), AnalyserFactory.createConfiguredAnalyser(configuration), reporters);
    }

    /**
     * Creates a simple claim reporter.
     * @param writer The XmlWriter to use
     * @return the RatReport that will write to the writer.
     */
    public static RatReport simple(final XmlWriter writer) {
        return new RatReport() {
            @Override
            public void report(final Document document) throws RatException {
                final MetaData metaData = document.getMetaData();
                XmlElements.document(writer, document);
                for (Iterator<ILicense> iter = metaData.licenses().iterator(); iter.hasNext(); ) {
                    final ILicense license = iter.next();
                    XmlElements.license(writer, license, metaData.isApproved(license));
                }
                try {
                    writer.closeElement();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    /**
     * Creates a simple claim validator reporter
     * @param writer The XmlWriter to use
     * @return the RatReport that will write to the writer.
     */
    public static RatReport validator(final XmlWriter writer, final ClaimStatistic statistic, final ReportConfiguration configuration) {
        return new RatReport() {
            @Override
            public void endReport() throws RatException {
                XmlElements.statistics(writer, statistic, configuration.getClaimValidator());
            }
        };
    }

    /**
     * Creates a configuration report
     *
     * @param writer The writer to write the XML data to.
     * @param configuration the configuration to write
     */
    public static RatReport configuration(final XmlWriter writer, final ReportConfiguration configuration) {
        return new RatReport() {
            @Override
            public void startReport() throws RatException {
                if (configuration.listFamilies() != LicenseFilter.NONE || configuration.listLicenses() != LicenseFilter.NONE) {
                    new XMLConfigurationWriter(configuration).write(writer);
                }
            }
        };
    }


    /**
     * Creates a RatReport that multiplexes the running of multiple RatReports
     */
    public static RatReport multiplexer(final XmlWriter writer, final boolean dryRun, final DocumentAnalyser analyser,
                                        final List<? extends RatReport> reporters) {
        return new RatReport() {
            @Override
            public void report(final Document document) throws RatException {
                if (!dryRun) {
                    if (analyser != null) {
                        try {
                            analyser.analyse(document);
                        } catch (RatDocumentAnalysisException e) {
                            throw new RatException(e.getMessage(), e);
                        }
                    }
                    for (RatReport report : reporters) {
                        report.report(document);
                    }
                }
            }

            @Override
            public void startReport() throws RatException {
                XmlElements.ratReport(writer);
                for (RatReport report : reporters) {
                    report.startReport();
                }
            }

            @Override
            public void endReport() throws RatException {
                for (RatReport report : reporters) {
                    report.endReport();
                }
                try {
                    writer.closeElement();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
