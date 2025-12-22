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
package org.apache.rat.cli;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.commons.io.function.IOSupplier;
import org.apache.rat.OptionCollection;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.Reporter;
import org.apache.rat.VersionInfo;
import org.apache.rat.commandline.ArgumentContext;
import org.apache.rat.document.RatDocumentAnalysisException;
import org.apache.rat.utils.DefaultLog;

import static java.lang.String.format;

/**
 * The CLI based configuration object for report generation.
 */
public final class Report {


    /**
     * Processes the command line and builds a configuration and executes the
     * report.
     *
     * @param args the arguments.
     * @throws Exception on error.
     */
    public static void main(final String[] args) throws Exception {
        VersionInfo versionInfo = new VersionInfo(Report.class);
        DefaultLog.getInstance().info(String.format("%s %s on %s %s (%s)",
                versionInfo.getTitle(), versionInfo.getVersion(), versionInfo.getSpecTitle(), versionInfo.getSpecVersion(),
                versionInfo.getSpecVendor()));

        if (args == null || args.length == 0) {
            DefaultLog.getInstance().info("Please use the \"--help\" option to see a " +
                    "list of valid commands and options, as you did not provide any arguments.");
            System.exit(0);
        }

        CLIOutput result = generateReport(new File("."), args);
        if (result.output != null) {
            result.output.writeSummary(DefaultLog.getInstance().asWriter());

            if (result.configuration.getClaimValidator().hasErrors()) {
                result.configuration.getClaimValidator().logIssues(result.output.getStatistic());
                throw new RatDocumentAnalysisException(format("Issues with %s",
                        String.join(", ",
                                result.configuration.getClaimValidator().listIssues(result.output.getStatistic()))));
            }
        }
    }

    /**
     * Prints the usage message on the specified output stream.
     * @param out The OutputStream supplier
     */
    private static void printUsage(final IOSupplier<OutputStream> out) {
        try (OutputStream stream = out.get();
             PrintWriter writer = new PrintWriter(stream)) {
            new Help(writer).printUsage(OptionCollection.buildOptions(CLIOption.ADDITIONAL_OPTIONS));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates the report
     * @param workingDirectory the direcotry that we are executing in
     * @param args the arguments from the command line.
     * @return The Client output.
     * @throws Exception on error.
     */
    static CLIOutput generateReport(final File workingDirectory, final String[] args) throws Exception {
        Reporter.Output output = null;
        ArgumentContext argumentContext = OptionCollection.parseCommands(workingDirectory, args, CLIOption.ADDITIONAL_OPTIONS);
        ReportConfiguration configuration = argumentContext.getConfiguration();
        if (configuration != null) {
            if (argumentContext.getCommandLine().hasOption(CLIOption.HELP)) {
                printUsage(argumentContext.getConfiguration().getOutput());
            } else if (!configuration.hasSource()) {
                    String msg = "No directories or files specified for scanning. Did you forget to close a multi-argument option?";
                    DefaultLog.getInstance().error(msg);
                    printUsage(argumentContext.getConfiguration().getOutput());
            } else {
                configuration.validate();
                Reporter reporter = new Reporter(configuration);
                output = reporter.execute();
                output.format(configuration);
            }
        }
        return new CLIOutput(configuration, output);
    }

    private Report() {
        // do not instantiate
    }

    /**
     * Output from the UI
     */
    @SuppressWarnings("VisibilityModifier")
    static class CLIOutput {
        /** Output from the Reporter */
        Reporter.Output output;
        /** THe configuration that generated the output. */
        ReportConfiguration configuration;

        /**
         * Construct client output.
         * @param configuration the configuration that was used to generate the output.
         * @param output The output from the reporter.
         */
        CLIOutput(final ReportConfiguration configuration, final Reporter.Output output) {
            this.output = output;
            this.configuration = configuration;
        }
    }
}
