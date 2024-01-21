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

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.io.Writer;

import org.apache.rat.api.RatException;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.report.RatReport;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.report.xml.XmlReportFactory;
import org.apache.rat.report.xml.writer.IXmlWriter;
import org.apache.rat.report.xml.writer.impl.base.XmlWriter;

/**
 * Class that executes the report as defined in a ReportConfiguration.
 */
public class Reporter {

    /*
     * Format used for listing license families
     */
    private static final String LICENSE_FAMILY_FORMAT = "\t%s: %s\n";

    /**
     * Format used for listing licenses.
     */
    private static final String LICENSE_FORMAT = "%s:\t%s\n\t\t%s\n";
    
    private Reporter() {
        // Do not instantiate
    }

    /**
     * Execute the report.
     * 
     * @param configuration The report configuration.
     * @return the currently collected numerical statistics.
     * @throws Exception in case of errors.
     */
    public static ClaimStatistic report(ReportConfiguration configuration) throws Exception {
        if (configuration.getReportable() != null) {
            if (configuration.isStyleReport()) {
                try (PipedReader reader = new PipedReader();
                        PipedWriter writer = new PipedWriter(reader);
                        InputStream style = configuration.getStyleSheet().get();
                        PrintWriter reportWriter = configuration.getWriter().get();) {
                    ReportTransformer transformer = new ReportTransformer(reportWriter, style, reader);
                    Thread transformerThread = new Thread(transformer);
                    transformerThread.start();
                    final ClaimStatistic statistic = report(writer, configuration);
                    writer.flush();
                    writer.close();
                    transformerThread.join();
                    return statistic;
                }
            }
            try (Writer writer = configuration.getWriter().get()) {
                return report(writer, configuration);
            }
        }
        return null;
    }

    /**
     * Execute the report.
     * @param outputWriter the writer to send output to.
     * @param configuration The report configuration.
     * @return the currently collected numerical statistics.
     * @throws IOException in case of I/O errors.
     * @throws RatException in case of internal errors.
     */
    private static ClaimStatistic report(Writer outputWriter, ReportConfiguration configuration)
            throws IOException, RatException {
        try (IXmlWriter writer = new XmlWriter(outputWriter)) {
            final ClaimStatistic statistic = new ClaimStatistic();
            RatReport report = XmlReportFactory.createStandardReport(writer, statistic, configuration);
            report.startReport();
            configuration.getReportable().run(report);
            report.endReport();

            return statistic;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
   
//    /**
//     * lists the license families information on the configured output stream.
//     * @param configuration The configuration for the system
//     * @throws IOException if PrintWriter can not be retrieved from configuration.
//     */
//    public static void listLicenseFamilies(ReportConfiguration configuration, LicenseFilter filter) throws IOException {
//        try (PrintWriter pw = configuration.getWriter().get()) {
//            pw.format("Families (%s):%n", filter);
//            configuration.getLicenseFamilies(filter)
//                    .forEach(x -> pw.format(LICENSE_FAMILY_FORMAT, x.getFamilyCategory(), x.getFamilyName()));
//            pw.println();
//        }
//    }
//
//    /**
//     * lists the licenses on the configured output stream.
//     * @param configuration The configuration for the system
//     * @throws IOException if PrintWriter can not be retrieved from configuration.
//     */
//    public static void listLicenses(ReportConfiguration configuration, LicenseFilter filter) throws IOException {
//        try (PrintWriter pw = configuration.getWriter().get()) {
//            pw.format("Licenses (%s):%n", filter);
//            configuration.getLicenses(filter)
//                    .forEach(lic -> pw.format(LICENSE_FORMAT, lic.getLicenseFamily().getFamilyCategory(),
//                            lic.getLicenseFamily().getFamilyName(), lic.getNotes()));
//            pw.println();
//        }
//    }

}
