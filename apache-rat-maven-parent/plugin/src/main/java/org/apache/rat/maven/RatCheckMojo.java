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
package org.apache.rat.maven;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.function.IOSupplier;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.Reporter;
import org.apache.rat.api.RatException;
import org.apache.rat.commandline.Arg;
import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.ui.ArgumentTracker;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.FileUtils;

import static java.lang.String.format;

/**
 * Run RAT to perform a violation check.
 * <p>
 *     This documentation mentions data types for some arguments.
 * </p>
 */
@Mojo(name = "check", defaultPhase = LifecyclePhase.VALIDATE, threadSafe = true)
public final class RatCheckMojo extends AbstractRatMojo {

    /**
     * The output from the last {@link #execute()}.
     */
    private Reporter.Output output;

    public RatCheckMojo() {
        super();
    }

    public Reporter.Output getOutput() {
        return output;
    }

    /**
     * Will ignore RAT errors and display a log message if any. Its use is NOT
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
     * @throws MojoExecutionException if another error occurred while executing the
     * plugin.
     */
    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("RAT will not execute since it is configured to be skipped via system property 'rat.skip'.");
            return;
        }

        if (getValues(Arg.OUTPUT_FILE).isEmpty()) {
            argumentTracker.setArg(ArgumentTracker.extractKey(Arg.OUTPUT_FILE.option()), "target/rat.txt");
        }

        try (Writer logWriter = DefaultLog.getInstance().asWriter()) {
            ReportConfiguration config = getConfiguration();
            logLicenses(config.getLicenses(LicenseFilter.ALL));
            if (verbose) {
                config.reportExclusions(logWriter);
            }
            try {
                final Reporter reporter = new Reporter(config);
                output = reporter.execute();
                writeMojoRatReport(output);
                if (verbose) {
                    output.writeSummary(DefaultLog.getInstance().asWriter());
                }
                // produce the requested output.
                output.format(config.getStyleSheet(), config.getOutput());
                // check for errors and fail if necessary
                check(config, output);
            } catch (Exception e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        } catch (IOException e) {
            DefaultLog.getInstance().warn("Unable to close writable log.", e);
        }
    }

    /**
     * Saves the Maven Mojo version of the XML for later.
     *
     * @param output the output to write to the XML file.
     */

    private void writeMojoRatReport(final Reporter.Output output) {
        Path ratPath = Path.of(getProject().getBuild().getDirectory(), "RAT");
        FileUtils.mkDir(ratPath.toFile());
        Path xmlPath = ratPath.resolve("rat.xml");
        IOSupplier<OutputStream> outputStream = () -> Files.newOutputStream(xmlPath);
        final IOSupplier<InputStream> stylesheet = StyleSheets.XML.getStyleSheet();
        try {
            output.format(stylesheet, outputStream);
        } catch (RatException e) {
            getLog().warn(e.getMessage(), e);
        }
        Path claimPath = ratPath.resolve("claimStatistic.xml");
        Path configPath = ratPath.resolve("configuration.xml");
        try (FileWriter claimFile = new FileWriter(claimPath.toFile(), StandardCharsets.UTF_8);
        FileWriter configFile = new FileWriter(configPath.toFile(), StandardCharsets.UTF_8);) {
            output.getStatistic().serde().serialize(claimFile);
            output.getConfiguration().serde().serialize(configFile);
        } catch (IOException e) {
            getLog().warn(e.getMessage(), e);
        }
    }

    private void check(final ReportConfiguration config, final Reporter.Output output) throws MojoFailureException {
        ClaimStatistic statistics = output.getStatistic();

        if (config.getClaimValidator().hasErrors()) {
            config.getClaimValidator().logIssues(statistics);
            if (consoleOutput &&
                    !config.getClaimValidator().isValid(ClaimStatistic.Counter.UNAPPROVED, statistics.getCounter(ClaimStatistic.Counter.UNAPPROVED))) {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    output.format(StyleSheets.UNAPPROVED_LICENSES.getStyleSheet(), () -> baos);
                    getLog().warn(baos.toString(StandardCharsets.UTF_8));
                } catch (RuntimeException rte) {
                    throw rte;
                } catch (Exception e) {
                    getLog().warn("Unable to print the files with unapproved licenses to the console.");
                }
            }

            String msg = format("Counter(s) %s exceeded minimum or maximum values. See RAT report in: '%s'.",
                    String.join(", ", config.getClaimValidator().listIssues(statistics)),
                    getRatTxtFile());

            if (!ignoreErrors) {
                throw new RatCheckException(msg);
            } else {
                getLog().info(msg);
            }
        } else {
            DefaultLog.getInstance().info("No issues found.");
        }
    }


    /**
     * Reads the location of the RAT text file from the Mojo.
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
