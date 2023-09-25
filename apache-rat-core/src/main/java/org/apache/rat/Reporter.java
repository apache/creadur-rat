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

import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Writer;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.api.RatException;
import org.apache.rat.report.RatReport;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.report.xml.XmlReportFactory;
import org.apache.rat.report.xml.writer.IXmlWriter;
import org.apache.rat.report.xml.writer.impl.base.XmlWriter;

public class Reporter {

    static FilenameFilter parseExclusions(List<String> excludes) {
        final OrFileFilter orFilter = new OrFileFilter();
        int ignoredLines = 0;
        for (String exclude : excludes) {
            try {
                // skip comments
                if (exclude.startsWith("#") || StringUtils.isEmpty(exclude)) {
                    ignoredLines++;
                    continue;
                }

                String exclusion = exclude.trim();
                // interpret given patterns as regular expression, direct file names or
                // wildcards to give users more choices to configure exclusions
                orFilter.addFileFilter(new RegexFileFilter(exclusion));
                orFilter.addFileFilter(new NameFileFilter(exclusion));
                orFilter.addFileFilter(new WildcardFileFilter(exclusion));
            } catch (PatternSyntaxException e) {
                System.err.println("Will skip given exclusion '" + exclude + "' due to " + e);
            }
        }
        System.err.println("Ignored " + ignoredLines + " lines in your exclusion files as comments or empty lines.");
        return new NotFileFilter(orFilter);
    }

    private Reporter() {
        // Do not instantiate
    }

    /**
     * @param out - the output stream to receive the styled report
     * @param configuration - current configuration options.
     * @return the currently collected numerical statistics.
     * @throws Exception in case of errors.
     * @since Rat 0.8
     */
    public static ClaimStatistic report(ReportConfiguration configuration) throws Exception {
        if (configuration.getReportable() != null) {
            if (configuration.isStyleReport()) {
                PipedReader reader = new PipedReader();
                PipedWriter writer = new PipedWriter(reader);
                ReportTransformer transformer = new ReportTransformer(configuration.getWriter(),
                        configuration.getStyleSheet(), reader);
                Thread transformerThread = new Thread(transformer);
                transformerThread.start();
                final ClaimStatistic statistic = report(writer, configuration);
                writer.flush();
                writer.close();
                transformerThread.join();
                return statistic;
            }
            return report(configuration.getWriter(), configuration);
        }
        return null;
    }

    /**
     * @param container the files or directories to report on
     * @param out the writer to write the report to
     * @param configuration current report configuration.
     * @return the currently collected numerical statistics.
     * @throws IOException in case of I/O errors.
     * @throws RatException in case of internal errors.
     */
    private static ClaimStatistic report(Writer outputWriter, ReportConfiguration configuration)
            throws IOException, RatException {
        IXmlWriter writer = new XmlWriter(outputWriter);
        final ClaimStatistic statistic = new ClaimStatistic();
        RatReport report = XmlReportFactory.createStandardReport(writer, statistic, configuration);
        report.startReport();
        configuration.getReportable().run(report);
        report.endReport();
        writer.closeDocument();
        return statistic;
    }
}
