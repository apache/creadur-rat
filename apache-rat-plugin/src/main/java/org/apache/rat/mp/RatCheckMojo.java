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
package org.apache.rat.mp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.rat.Defaults;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.Reporter;
import org.apache.rat.config.AddLicenseHeaders;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.report.claim.ClaimStatistic;

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

    @Parameter(property = "rat.scanHiddenDirectories", defaultValue = "false")
    private boolean scanHiddenDirectories;

    /**
     * Output style of the report. Use "plain" (the default) for a plain text report
     * or "xml" for the raw XML report. Alternatively you can give the path of an
     * XSL transformation that will be applied on the raw XML to produce the report
     * written to the output file.
     */
    @Parameter(property = "rat.outputStyle", defaultValue = "plain")
    private String reportStyle;

    /**
     * Maximum number of files with unapproved licenses.
     */
    @Parameter(property = "rat.numUnapprovedLicenses", defaultValue = "0")
    private int numUnapprovedLicenses;

    /**
     * Whether to add license headers; possible values are {@code forced},
     * {@code true}, and {@code false} (default).
     */
    @Parameter(property = "rat.addLicenseHeaders", defaultValue = "false")
    private String addLicenseHeaders;

    /**
     * Copyright message to add to license headers. This option is ignored, unless
     * {@code addLicenseHeaders} is set to {@code true}, or {@code forced}.
     */
    @Parameter(property = "rat.copyrightMessage")
    private String copyrightMessage;

    /**
     * Will ignore rat errors and display a log message if any. Its use is NOT
     * RECOMMENDED, but quite convenient on occasion.
     *
     * @since 0.9
     */
    @Parameter(property = "rat.ignoreErrors", defaultValue = "false")
    private boolean ignoreErrors;

    /**
     * Whether to output the names of files that have unapproved licenses to the
     * console. Defaults to {@code true} to ease builds in containers where you are
     * unable to access rat.txt easily.
     *
     * @since 0.12
     */
    @Parameter(property = "rat.consoleOutput", defaultValue = "true")
    private boolean consoleOutput;

    /**
     * Invoked by Maven to execute the Mojo.
     *
     * @throws MojoFailureException An error in the plugin configuration was
     * detected.
     * @throws MojoExecutionException Another error occurred while executing the
     * plugin.
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("RAT will not execute since it is configured to be skipped via system property 'rat.skip'.");
            return;
        }
        ReportConfiguration config = getConfiguration();
        logLicenses(config.getLicenses(LicenseFilter.all));
        final File parent = reportFile.getParentFile();
        if (!parent.mkdirs() && !parent.isDirectory()) {
            throw new MojoExecutionException("Could not create report parent directory " + parent);
        }

        try {
            final ClaimStatistic report = Reporter.report(config);
            check(report, config);
        } catch (MojoExecutionException | MojoFailureException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected void check(ClaimStatistic statistics, ReportConfiguration config) throws MojoFailureException {
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
                    config.setStyleSheet(Defaults.getUnapprovedLicensesStyleSheet());
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    config.setOut(()->baos);
                    Reporter.report(config);
                    getLog().warn(baos.toString());
                } catch (Exception e) {
                    getLog().warn("Unable to print the files with unapproved licenses to the console.");
                }
            }

            final String seeReport = " See RAT report in: " + reportFile;
            if (!ignoreErrors) {
                throw new RatCheckException(
                        "Too many files with unapproved license: " + statistics.getNumUnApproved() + seeReport);
            }
            getLog().warn(
                    "Rat check: " + statistics.getNumUnApproved() + " files with unapproved licenses." + seeReport);
        }
    }

    @Override
    protected ReportConfiguration getConfiguration() throws MojoExecutionException {
        final ReportConfiguration configuration = super.getConfiguration();
        if (StringUtils.isNotBlank(addLicenseHeaders)) {
            configuration.setAddLicenseHeaders(AddLicenseHeaders.valueOf(addLicenseHeaders.toUpperCase()));
        }
        if (StringUtils.isNotBlank(copyrightMessage)) {
            configuration.setCopyrightMessage(copyrightMessage);
        }
        if (scanHiddenDirectories) {
            configuration.setDirectoryFilter(null);
        }
        if (reportFile != null) {
            if (!reportFile.exists()) {
                reportFile.getParentFile().mkdirs();
            }
            configuration.setOut(reportFile);
        }
        if (StringUtils.isNotBlank(reportStyle)) {
            if ("xml".equalsIgnoreCase(reportStyle)) {
                configuration.setStyleReport(false);
            } else {
                configuration.setStyleReport(true);
                if (!"plain".equalsIgnoreCase(reportStyle)) {
                    configuration.setStyleSheet(() -> new FileInputStream(reportStyle));
                }
            }
        }
        return configuration;
    }
}
