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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import org.xml.sax.SAXException;

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
            Output.Builder builder = Output.builder().configuration(configuration);
            if (configuration.hasSource()) {
                StringBuilder sb = new StringBuilder();
                try (IXmlWriter writer = new XmlWriter(sb)) {
                    writer.startDocument();
                    ClaimStatistic statistic = new ClaimStatistic();
                    builder.statistic(statistic);
                    RatReport report = XmlReportFactory.createStandardReport(writer, statistic, configuration);
                    report.startReport();
                    configuration.getSources().build().run(report);
                    report.endReport();
                    InputSource inputSource = new InputSource(new StringReader(sb.toString()));
                    builder.document(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSource));
                }
            }
            this.output = builder.build();
            return output;
        } catch (Exception e) {
            throw RatException.makeRatException(e);
        }
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
    public static final class Output {
        /** The XML output document */
        private final Document document;
        /**
         * The claim statics from the execution that generated the document.
         * May be empty if the Document was read from disk.
         */
        private final ClaimStatistic statistic;
        /**
         * The configuration that generated the document
         */
        private final ReportConfiguration configuration;

        /**
         * Create an output with statistics.
         * @param builder the Builder
         */
        private Output(final Builder builder) {
            this.document = builder.document;
            this.statistic = builder.statistic == null ? new ClaimStatistic() : builder.statistic;
            this.configuration = builder.configuration == null ? new ReportConfiguration() : builder.configuration;
        }

        public static Builder builder() {
            return new Builder();
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

        public ReportConfiguration getConfiguration() {
            return configuration;
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

        public static final class Builder {
            /** The document that was generated */
            private Document document;
            /**
             * The claim statistic from the execution that generated the document.
             * May be empty if the Document was read from disk.
             */
            private ClaimStatistic statistic;
            /**
             * The configuration that generated the document
             */
            private ReportConfiguration configuration;

            public Builder document(final Document document) {
                this.document = document;
                return this;
            }

            public Builder document(final File documentFile) {
                DocumentBuilder builder;
                try {
                    builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                } catch (ParserConfigurationException e) {
                    throw new ConfigurationException("Unable to create DOM builder", e);
                }

                try (InputStream inputStream = new FileInputStream(documentFile)) {
                    this.document = builder.parse(inputStream);
                } catch (SAXException | IOException e) {
                    throw new ConfigurationException("Unable to read file: " + documentFile, e);
                }
                return this;
            }

            public Output build() {
                return new Output(this);
            }

            public Builder statistic(final ClaimStatistic statistic) {
                this.statistic = statistic;
                return this;
            }

            public Builder statistic(final File statisticFile) {
                try {
                    ClaimStatistic statistic = new ClaimStatistic();
                    statistic.serde().deserialize(() -> new FileInputStream(statisticFile));
                    this.statistic = statistic;
                    return this;
                } catch (IOException e) {
                    throw new ConfigurationException("Unable to read file: " + statisticFile, e);
                }
            }

            public Builder configuration(final ReportConfiguration configuration) {
                this.configuration = configuration;
                return this;
            }

            public Builder configuration(final File configurationFile) {
                try {
                    ReportConfiguration config = new ReportConfiguration();
                    config.serde().deserialize(() -> new FileInputStream(configurationFile));
                    this.configuration = config;
                    return this;
                } catch (IOException e) {
                    throw new ConfigurationException("Unable to read file: " + configurationFile, e);
                }
            }
        }
    }
}
