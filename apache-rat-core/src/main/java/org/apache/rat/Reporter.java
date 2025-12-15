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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.function.IOSupplier;
import org.apache.rat.api.RatException;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.report.RatReport;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.report.xml.XmlReportFactory;
import org.apache.rat.report.xml.writer.IXmlWriter;
import org.apache.rat.report.xml.writer.XmlWriter;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Class that executes the report as defined in a {@link ReportConfiguration} and stores
 * the result for later handling.
 */
public class Reporter {

    /**  Format used for listing licenses. */
    private static final String LICENSE_FORMAT = "%s:\t%s%n\t\t%s%n";

    /** The configuration for the report */
    private final ReportConfiguration configuration;

    /**
     * The output from the execution.
     */
    private Output output;

    /**
     * Create the reporter.
     *
     * @param configuration the configuration to use.
     */
    public Reporter(final ReportConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Executes the report and builds the output.
     * @return the Output object.
     * @throws RatException on error.
     */
    public Output execute() throws RatException {
        try {
            if (configuration.hasSource()) {
                StringBuilder sb = new StringBuilder();
                try (IXmlWriter writer = new XmlWriter(sb)) {
                    writer.startDocument();
                    ClaimStatistic statistic = new ClaimStatistic();
                    RatReport report = XmlReportFactory.createStandardReport(writer, statistic, configuration);
                    report.startReport();
                    configuration.getSources().build().run(report);
                    report.endReport();
                    InputSource inputSource = new InputSource(new StringReader(sb.toString()));
                    output = new Output(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSource), statistic);
                }
            } else {
                output = new Output(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
            }
        } catch (Exception e) {
            throw RatException.makeRatException(e);
        }
        return output;
    }

    /**
     * Gets the output from the last {@link #execute} call or {@code null} if {@link #execute} has not been called.
     * @return the output
     */
    public Output getOutput() {
        return output;
    }

    /**
     * Lists the licenses on the configured output stream.
     * @param configuration The configuration for the system
     * @param filter the license filter that specifies which licenses to output.
     * @throws IOException if PrintWriter can not be retrieved from configuration.
     */
    public static void listLicenses(final ReportConfiguration configuration, final LicenseFilter filter) throws IOException {
        try (PrintWriter pw = configuration.getWriter().get()) {
            pw.format("Licenses (%s):%n", filter);
            configuration.getLicenses(filter)
                    .forEach(lic -> pw.format(LICENSE_FORMAT, lic.getLicenseFamily().getFamilyCategory(),
                            lic.getLicenseFamily().getFamilyName(), lic.getNote()));
            pw.println();
        }
    }


    /**
     * The output from a report run.
     */
    public static class Output {
        /** The XML output document */
        private final Document document;
        /**
         * The claim staticsic from the execution that generated the document.
         * May be empty if the Document was read from disk.
         */
        private final ClaimStatistic statistic;

        /**
         * Create an output with an empty statistics.
         * @param document the Document from the output.
         */
        public Output(final Document document) {
            this(document, new ClaimStatistic());
        }

        /**
         * Create an output with statistics.
         * @param document the Document from the execution.
         * @param statistic the statistics from the execution.
         */
        public Output(final Document document, final ClaimStatistic statistic) {
            this.document = document;
            this.statistic = statistic;
        }

        /**
         * Gets the document that was generated during execution.
         * @return the document that was generated during execution.
         */
        public Document getDocument() {
            return document;
        }

        public ClaimStatistic getStatistic() {
            return statistic;
        }

        /**
         * Formats the report to the output and using the stylesheet found in the report configuration.
         *
         * @param config s RAT report configuration.
         * @throws RatException on error.
         */
        public void format(final ReportConfiguration config) throws RatException {
            format(config.getStyleSheet(), config.getOutput());
        }

        /**
         * Formats the report to the specified output using the stylesheet. It is safe to call this method more than once
         * in order to generate multiple reports from the same run.
         *
         * @param stylesheet the style sheet to use for XSLT formatting.
         * @param output the output stream to write to.
         * @throws RatException on error.
         */
        public void format(final IOSupplier<InputStream> stylesheet, final IOSupplier<OutputStream> output) throws RatException {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer;
            try (OutputStream out = output.get();
                 InputStream styleIn = stylesheet.get()) {
                transformer = tf.newTransformer(new StreamSource(styleIn));
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                transformer.transform(new DOMSource(document),
                        new StreamResult(new OutputStreamWriter(out, StandardCharsets.UTF_8)));
            } catch (TransformerException | IOException e) {
                throw new RatException(e);
            }
        }

        /**
         * Writes a text summary of issues with the run.
         * @param appendable the appendable to write to.
         * @throws IOException on error.
         */
        public void writeSummary(final Appendable appendable) throws IOException {
            appendable.append("RAT summary:").append(System.lineSeparator());
            for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
                appendable.append("  ").append(counter.displayName()).append(":  ")
                        .append(Integer.toString(statistic.getCounter(counter)))
                        .append(System.lineSeparator());
            }
        }

    }
}
