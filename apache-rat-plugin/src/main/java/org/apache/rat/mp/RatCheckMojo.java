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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.Reporter;
import org.apache.rat.commandline.Arg;
import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.utils.DefaultLog;

import static java.lang.String.format;

/**
 * Run Rat to perform a violation check.
 * <p>
 *     This documentation mentions data types for some of the arguments. An <a href="data_types.html">explanation of the data types</a> is included
 *     in ths documentation package.
 * </p>
 */
@Mojo(name = "check", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public class RatCheckMojo extends AbstractRatMojo {

    /** The default output file if no other is specified.
     * @deprecated Use &lt;outputFile&gt; instead.
     */
    @Deprecated
    @Parameter(defaultValue = "${project.build.directory}/rat.txt")
    private File defaultReportFile;

    /**
     * Where to store the report.
     * @deprecated use 'out' property.
     */
    @Deprecated
    @Parameter
    public void setReportFile(final File reportFile) {
        if (!reportFile.getParentFile().exists()) {
            if (!reportFile.getParentFile().mkdirs()) {
                getLog().error("Unable to create directory " + reportFile.getParentFile());
            }
        }
        setOutputFile(reportFile.getAbsolutePath());
    }

    /**
     * Output style of the report. Use "plain" (the default) for a plain text report
     * or "xml" for the raw XML report. Alternatively you can give the path of an
     * XSL transformation that will be applied on the raw XML to produce the report
     * written to the output file.
     * @deprecated use setStyleSheet or xml
     */
    @Deprecated
    @Parameter(property = "rat.outputStyle")
    public void setReportStyle(final String value) {
        if (value.equalsIgnoreCase("xml")) {
            setXml(true);
        } else if (value.equalsIgnoreCase("plain")) {
            setStylesheet("plain-rat");
        } else {
            setStylesheet(value);
        }
    }

    /**
     * Maximum number of files with unapproved licenses.
     * @deprecated use &lt;counterMax&gt;Unapproved:value&lt;/counterMax&gt;
     */
    @Deprecated
    @Parameter(property = "rat.numUnapprovedLicenses", defaultValue = "0")
    private int numUnapprovedLicenses;

    /**
     * Whether to add license headers; possible values are {@code forced},
     * {@code true}, and {@code false} (default).
     * @deprecated use &lt;editLicense&gt; and &lt;editOverwrite&gt;
     */
    @Deprecated
    @Parameter(property = "rat.addLicenseHeaders")
    public void setAddLicenseHeaders(final String addLicenseHeaders) {
        switch (addLicenseHeaders.trim().toUpperCase()) {
            case "FALSE":
                // do nothing;
                break;
            case "TRUE":
                setAddLicense(true);
                break;
            case "FORCED":
                setAddLicense(true);
                setForce(true);
                break;
            default:
                throw new IllegalArgumentException("Unknown addlicense header: " + addLicenseHeaders);
        }
    }

    /**
     * Copyright message to add to license headers.
     * @deprecated Deprecated for removal since 0.17: Use &lt;editCopyright&gt; instead.
     */
    @Deprecated
    @Parameter(property = "rat.copyrightMessage")
    public void setCopyrightMessage(final String copyrightMessage) {
        setCopyright(copyrightMessage);
    }

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

    /** The reporter that this mojo uses */
    private Reporter reporter;

    @Override
    protected ReportConfiguration getConfiguration() throws MojoExecutionException {
        ReportConfiguration result = super.getConfiguration();
        if (numUnapprovedLicenses > 0) {
            result.getClaimValidator().set(ClaimStatistic.Counter.UNAPPROVED, numUnapprovedLicenses);
        }
        return result;
    }

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

        if (getValues(Arg.OUTPUT_FILE).isEmpty()) {
            setArg(Arg.OUTPUT_FILE.option().getLongOpt(), defaultReportFile.getAbsolutePath());
        }

        ReportConfiguration config = getConfiguration();

        logLicenses(config.getLicenses(LicenseFilter.ALL));
        try {
            this.reporter = new Reporter(config);
            reporter.output();
            check(config);
        } catch (MojoFailureException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    protected void check(final ReportConfiguration config) throws MojoFailureException {
        ClaimStatistic statistics = reporter.getClaimsStatistic();
        try {
           reporter.writeSummary(DefaultLog.getInstance().asWriter());
           if (config.getClaimValidator().hasErrors()) {
               config.getClaimValidator().logIssues(statistics);
               if (consoleOutput &&
                       !config.getClaimValidator().isValid(ClaimStatistic.Counter.UNAPPROVED, statistics.getCounter(ClaimStatistic.Counter.UNAPPROVED))) {
                   try {
                       ByteArrayOutputStream baos = new ByteArrayOutputStream();
                       reporter.output(StyleSheets.UNAPPROVED_LICENSES.getStyleSheet(), () -> baos);
                       getLog().warn(baos.toString(StandardCharsets.UTF_8.name()));
                   } catch (Exception e) {
                       getLog().warn("Unable to print the files with unapproved licenses to the console.");
                   }
               }

               String msg = format("Too many files of type(s) %s. See RAT report in: '%s'",
                       String.join(", ", config.getClaimValidator().listIssues(statistics)),
                       getRatTxtFile());

               if (!ignoreErrors) {
                   throw new RatCheckException(msg);
               } else {
                   getLog().info(msg);
               }
           }
        } catch (IOException e) {
           throw new MojoFailureException(e);
       }
    }

    /**
     * Reads the location of the rat text file from the Mojo.
     *
     * @return Value of the "reportFile" property.
     * @throws MojoFailureException If no output file was specified.
     */
    public File getRatTxtFile() throws MojoFailureException {
        List<String> args = getValues(Arg.OUTPUT_FILE);
        if (args != null) {
            return new File(args.get(0));
        }
        throw new MojoFailureException("No output file specified");
    }
}
