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

import org.apache.rat.api.RatException;
import org.apache.rat.report.IReportable;
import org.apache.rat.report.RatReport;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.report.xml.XmlReportFactory;
import org.apache.rat.report.xml.writer.IXmlWriter;
import org.apache.rat.report.xml.writer.impl.base.XmlWriter;
import org.apache.rat.walker.ArchiveWalker;
import org.apache.rat.walker.DirectoryWalker;

import javax.xml.transform.TransformerConfigurationException;
import java.io.*;

public class Report {

    private final File baseDirectory;

    private FilenameFilter inputFileFilter = null;

    public Report(File baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    /**
     * Sets the current filter used to select files.
     *
     * @param inputFileFilter filter, or null when no filter has been set
     */
    public void setInputFileFilter(FilenameFilter inputFileFilter) {
        this.inputFileFilter = inputFileFilter;
    }

    /**
     * @param out - the output stream to receive the styled report
     * @return the currently collected numerical statistics.
     * @throws Exception in case of errors.
     * @deprecated use {@link #report(PrintStream, ReportConfiguration)} instead
     */
    @Deprecated
    public ClaimStatistic report(PrintStream out) throws Exception {
        final ReportConfiguration configuration = new ReportConfiguration();
        configuration.setHeaderMatcher(Defaults.createDefaultMatcher());
        configuration.setApproveDefaultLicenses(true);
        return report(out, configuration);
    }

    /**
     * @param out           - the output stream to receive the styled report
     * @param configuration - current configuration options.
     * @return the currently collected numerical statistics.
     * @throws Exception in case of errors.
     * @since Rat 0.8
     */
    public ClaimStatistic report(PrintStream out,
                                 ReportConfiguration configuration)
            throws Exception {
        final IReportable base = getDirectory(out);
        if (base != null) {
            return report(base, new OutputStreamWriter(out), configuration);
        }
        return null;
    }

    public IReportable getDirectory(PrintStream out) {
        if (!baseDirectory.exists()) {
            out.print("ERROR: ");
            out.print(baseDirectory);
            out.print(" does not exist.\n");
            return null;
        }

        if (baseDirectory.isDirectory()) {
            return new DirectoryWalker(baseDirectory, inputFileFilter);
        }

        try {
            return new ArchiveWalker(baseDirectory, inputFileFilter);
        } catch (IOException ex) {
            out.print("ERROR: ");
            out.print(baseDirectory);
            out.print(" is not valid gzip data.\n");
            return null;
        }
    }

    /**
     * Output a report in the default style and default license
     * header matcher.
     *
     * @param out - the output stream to receive the styled report
     * @throws Exception in case of errors.
     * @deprecated use {@link #styleReport(PrintStream, ReportConfiguration)} instead
     */
    @Deprecated
    public void styleReport(PrintStream out) throws Exception {
        final ReportConfiguration configuration = new ReportConfiguration();
        configuration.setHeaderMatcher(Defaults.createDefaultMatcher());
        configuration.setApproveDefaultLicenses(true);
        styleReport(out, configuration);
    }

    /**
     * Output a report in the default style and default license
     * header matcher.
     *
     * @param out           - the output stream to receive the styled report
     * @param configuration the configuration to use
     * @throws Exception in case of errors.
     * @since Rat 0.8
     */
    public void styleReport(PrintStream out,
                            ReportConfiguration configuration)
            throws Exception {
        final IReportable base = getDirectory(out);
        if (base != null) {
            InputStream style = Defaults.getDefaultStyleSheet();
            report(out, base, style, configuration);
        }
    }

    /**
     * Output a report that is styled using a defined stylesheet.
     *
     * @param out            the stream to write the report to
     * @param base           the files or directories to report on
     * @param style          an input stream representing the stylesheet to use for styling the report
     * @param pConfiguration current report configuration.
     * @throws IOException                       in case of I/O errors.
     * @throws TransformerConfigurationException in case of XML errors.
     * @throws InterruptedException              in case of threading errors.
     * @throws RatException                      in case of internal errors.
     */
    public void report(PrintStream out, IReportable base, final InputStream style,
                              ReportConfiguration pConfiguration)
            throws IOException, TransformerConfigurationException, InterruptedException, RatException {
        report(new OutputStreamWriter(out), base, style, pConfiguration);
    }

    /**
     * Output a report that is styled using a defined stylesheet.
     *
     * @param out            the writer to write the report to
     * @param base           the files or directories to report on
     * @param style          an input stream representing the stylesheet to use for styling the report
     * @param pConfiguration current report configuration.
     * @return the currently collected numerical statistics.
     * @throws IOException                       in case of I/O errors.
     * @throws TransformerConfigurationException in case of XML errors.
     * @throws InterruptedException              in case of threading errors.
     * @throws RatException                      in case of internal errors.
     */
    public ClaimStatistic report(Writer out, IReportable base, final InputStream style,
                                        ReportConfiguration pConfiguration)
            throws IOException, TransformerConfigurationException, InterruptedException, RatException {
        PipedReader reader = new PipedReader();
        PipedWriter writer = new PipedWriter(reader);
        ReportTransformer transformer = new ReportTransformer(out, style, reader);
        Thread transformerThread = new Thread(transformer);
        transformerThread.start();
        final ClaimStatistic statistic = report(base, writer, pConfiguration);
        writer.flush();
        writer.close();
        transformerThread.join();
        return statistic;
    }

    /**
     * @param container      the files or directories to report on
     * @param out            the writer to write the report to
     * @param pConfiguration current report configuration.
     * @return the currently collected numerical statistics.
     * @throws IOException  in case of I/O errors.
     * @throws RatException in case of internal errors.
     */
    public ClaimStatistic report(final IReportable container, final Writer out,
                                        ReportConfiguration pConfiguration) throws IOException, RatException {
        IXmlWriter writer = new XmlWriter(out);
        final ClaimStatistic statistic = new ClaimStatistic();
        RatReport report = XmlReportFactory.createStandardReport(writer, statistic, pConfiguration);
        report.startReport();
        container.run(report);
        report.endReport();
        writer.closeDocument();
        return statistic;
    }
}
