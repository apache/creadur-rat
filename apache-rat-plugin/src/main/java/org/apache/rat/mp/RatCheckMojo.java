package org.apache.rat.mp;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.rat.Defaults;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.config.AddLicenseHeaders;
import org.apache.rat.config.ReportFormat;
import org.apache.rat.report.claim.ClaimStatistic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Run Rat to perform a violation check.
 */
@Mojo(name = "check", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public class RatCheckMojo extends AbstractRatMojo {
    /**
     * Where to store the report.
     */
    @Parameter(property = "rat.outputFile", defaultValue = "${project.build.directory}/rat.txt")
    private File reportFile;

    /**
     * Output style of the report. Use "plain" (the default) for a plain text
     * report or "xml" for the raw XML report. Alternatively you can give the
     * path of an XSL transformation that will be applied on the raw XML to
     * produce the report written to the output file.
     */
    @Parameter(property = "rat.outputStyle", defaultValue = "plain")
    private String reportStyle;

    /**
     * Maximum number of files with unapproved licenses.
     */
    @Parameter(property = "rat.numUnapprovedLicenses", defaultValue = "0")
    private int numUnapprovedLicenses;

    /**
     * Whether to add license headers; possible values are
     * {@code forced}, {@code true}, and {@code false} (default).
     */
    @Parameter(property = "rat.addLicenseHeaders", defaultValue = "false")
    private String addLicenseHeaders;

    /**
     * Copyright message to add to license headers. This option is
     * ignored, unless {@code addLicenseHeaders} is set to {@code true},
     * or {@code forced}.
     */
    @Parameter(property = "rat.copyrightMessage")
    private String copyrightMessage;

    /**
     * Will ignore rat errors and display a log message if any.
     * Its use is NOT RECOMMENDED, but quite convenient on occasion.
     *
     * @since 0.9
     */
    @Parameter(property = "rat.ignoreErrors", defaultValue = "false")
    private boolean ignoreErrors;

    /**
     * Whether to output the names of files that have unapproved licenses to the
     * console. Defaults to {@code true} to ease builds in containers where you are unable to access rat.txt easily.
     *
     * @since 0.12
     */
    @Parameter(property = "rat.consoleOutput", defaultValue = "true")
    private boolean consoleOutput;

    private ClaimStatistic getRawReport()
            throws MojoExecutionException, MojoFailureException {
        Writer fw = null;
        try {
            fw = new OutputStreamWriter(
                   new FileOutputStream(reportFile),
                    StandardCharsets.UTF_8);
            final ClaimStatistic statistic = createReport(fw, getStyleSheet());
            fw.close();
            fw = null;
            return statistic;
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(fw);
        }
    }

    /**
     * Returns the XSL stylesheet to be used for formatting the report.
     *
     * @return report stylesheet, or <code>null</code> for raw XML
     * @throws MojoExecutionException if the stylesheet can not be found
     * @see #reportStyle
     */
    private InputStream getStyleSheet() throws MojoExecutionException {
        if (reportStyle == null || ReportFormat.PLAIN.is(reportStyle)) {
            return Defaults.getPlainStyleSheet();
        } else if (ReportFormat.XML.is(reportStyle)) {
            return null;
        } else {
            try {
                return new FileInputStream(reportStyle);
            } catch (FileNotFoundException e) {
                throw new MojoExecutionException(
                        "Unable to find report stylesheet: " + reportStyle, e);
            }
        }
    }

    /**
     * Invoked by Maven to execute the Mojo.
     *
     * @throws MojoFailureException   An error in the plugin configuration was detected.
     * @throws MojoExecutionException Another error occurred while executing the plugin.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("RAT will not execute since it is configured to be skipped via system property 'rat.skip'.");
            return;
        }

        final File parent = reportFile.getParentFile();
        if (!parent.mkdirs() && !parent.isDirectory()) {
            throw new MojoExecutionException("Could not create report parent directory " + parent);
        }

        final ClaimStatistic report = getRawReport();
        check(report);
    }

    protected void check(ClaimStatistic statistics)
            throws MojoFailureException {
        if (numUnapprovedLicenses > 0) {
            getLog().info("You requested to accept " + numUnapprovedLicenses + " files with unapproved licenses.");
        }

        int numApproved = statistics.getNumApproved();
        getLog().info("Rat check: Summary over all files. Unapproved: " + statistics.getNumUnApproved() + //
                ", unknown: " + statistics.getNumUnknown() + //
                ", generated: " + statistics.getNumGenerated() + //
                ", approved: " + numApproved + //
                (numApproved > 0 ? " licenses." : " license."));

        if (numUnapprovedLicenses < statistics.getNumUnApproved()) {
            if (consoleOutput) {
                try {
                    getLog().warn(createReport(Defaults.getUnapprovedLicensesStyleSheet()).trim());
                } catch (MojoExecutionException e) {
                    getLog().warn("Unable to print the files with unapproved licenses to the console.");
                }
            }

            final String seeReport = " See RAT report in: " + reportFile;
            if (!ignoreErrors) {
                throw new RatCheckException("Too many files with unapproved license: " + statistics.getNumUnApproved() + seeReport);
            } else {
                getLog().warn("Rat check: " + statistics.getNumUnApproved() + " files with unapproved licenses." + seeReport);
            }
        }
    }

    @Override
    protected ReportConfiguration getConfiguration()
            throws MojoFailureException, MojoExecutionException {
        final ReportConfiguration configuration = super.getConfiguration();

        if (AddLicenseHeaders.FORCED.name().equalsIgnoreCase(addLicenseHeaders)) {
            configuration.setAddingLicenses(true);
            configuration.setAddingLicensesForced(true);
            configuration.setCopyrightMessage(copyrightMessage);
        } else if (AddLicenseHeaders.TRUE.name().equalsIgnoreCase(addLicenseHeaders)) {
            configuration.setAddingLicenses(true);
            configuration.setCopyrightMessage(copyrightMessage);
        } else if (AddLicenseHeaders.FALSE.name().equalsIgnoreCase(addLicenseHeaders)) {
            // Nothing to do
        } else {
            throw new MojoFailureException("Invalid value for addLicenseHeaders: Expected " + AddLicenseHeaders.getValuesForHelp() + ", got "
                    + addLicenseHeaders);
        }
        return configuration;
    }
}
