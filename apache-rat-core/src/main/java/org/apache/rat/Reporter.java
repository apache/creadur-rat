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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
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

/**
 * Class that executes the report as defined in a {@link ReportConfiguration} and stores
 * the result for later handling.
 */
public class Reporter {

    /**  Format used for listing licenses. */
    private static final String LICENSE_FORMAT = "%s:\t%s%n\t\t%s%n";

    /** The XML output document */
    private Document document;

    /** Statistics generated as the report was built */
    private ClaimStatistic statistic;

    /** The configuration for the report */
    private final ReportConfiguration configuration;

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
     * This method will build the internal XML document if it does not already exist.
     * If this method or either of the {@link #output()} methods have already been called this method will return
     * the previous results.
     * @return the claim statistics.
     * @throws RatException on error.
     */
    public ClaimStatistic execute() throws RatException  {
        if (document == null || statistic == null) {
            try {
                if (configuration.hasSource()) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    Writer outputWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                    try (IXmlWriter writer = new XmlWriter(outputWriter)) {
                        statistic = new ClaimStatistic();
                        RatReport report = XmlReportFactory.createStandardReport(writer, statistic, configuration);
                        report.startReport();
                        configuration.getSources().build().run(report);
                        report.endReport();

                        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                        document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
                    }
                } else {
                    document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                    statistic = new ClaimStatistic();
                }
            }  catch (Exception e) {
                throw RatException.makeRatException(e);
            }
        }
        return statistic;
    }

    /**
     * Get the claim statistics from the run.
     *
     * @return the claim statistics.
     */
    public ClaimStatistic getClaimsStatistic() {
        return statistic;
    }

    /**
     * Outputs the report using the stylesheet and output specified in the configuration.
     *
     * @throws RatException on error.
     */
    public void output() throws RatException {
        output(configuration.getStyleSheet(), configuration.getOutput());
    }

    /**
     * Outputs the report to the specified output using the stylesheet. It is safe to call this method more than once
     * in order to generate multiple reports from the same run.
     *
     * @param stylesheet the style sheet to use for XSLT formatting.
     * @param output the output stream to write to.
     * @throws RatException on error.
     */
    public void output(final IOSupplier<InputStream> stylesheet, final IOSupplier<OutputStream> output) throws RatException {
        execute();
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
     * Writes a text summary of issues with the run.
     * @param appendable the appendable to write to.
     * @throws IOException on error.
     */
    public void writeSummary(final Appendable appendable) throws IOException {
        appendable.append("RAT summary:").append(System.lineSeparator());
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            appendable.append("  ").append(counter.displayName()).append(":  ")
                    .append(Integer.toString(getClaimsStatistic().getCounter(counter)))
                    .append(System.lineSeparator());
        }
    }
}
