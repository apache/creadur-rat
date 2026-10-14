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
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.cli.Options;
import org.apache.commons.io.function.IOSupplier;
import org.apache.rat.commandline.ArgumentContext;
import org.apache.rat.document.RatDocumentAnalysisException;
import org.apache.rat.help.Help;
import org.apache.rat.utils.DefaultLog;

import static java.lang.String.format;

/**
 * The CLI based configuration object for report generation.
 */
public final class Report {
    /**
     * The option collection that this report is using.
     */
    private static final CLIOptionCollection OPTION_COLLECTION = new CLIOptionCollection();
    /**
     * Processes the command line and builds a configuration and executes the
     * report.
     *
     * @param args the arguments.
     * @throws Exception on error.
     */
    public static void main(final String[] args) throws Exception {
        DefaultLog.getInstance().info(new VersionInfo().toString());

        if (args == null || args.length == 0) {
            DefaultLog.getInstance().info("Please use the \"--help\" option to see a " +
                    "list of valid commands and options, as you did not provide any arguments.");
            System.exit(0);
        }
        OptionCollectionParser<CLIOption> cliOptionParser = new OptionCollectionParser<CLIOption>(OPTION_COLLECTION);
        ArgumentContext argumentContext = cliOptionParser.parseCommands(new File("."), args);
        ReportConfiguration configuration = argumentContext.getConfiguration();

        if (argumentContext.hasOption(CLIOptionCollection.HELP)) {
            printUsage(OPTION_COLLECTION.getOptions(), configuration.getOutput());
        } else if (!configuration.hasSource()) {
            String msg = "No directories or files specified for scanning. Did you forget to close a multi-argument option?";
            DefaultLog.getInstance().error(msg);
            printUsage(OPTION_COLLECTION.getOptions(), configuration.getOutput());
        } else {
            configuration.validate(DefaultLog.getInstance()::error);
            Reporter.Output output = new Reporter(configuration).execute();
            output.format(argumentContext.getConfiguration());
            output.writeSummary(DefaultLog.getInstance().asWriter());

            if (configuration.getClaimValidator().hasErrors()) {
                configuration.getClaimValidator().logIssues(output.getStatistic());
                throw new RatDocumentAnalysisException(format("Issues with %s",
                        String.join(", ",
                                configuration.getClaimValidator().listIssues(output.getStatistic()))));
            }
        }
    }

    /**
     * Prints the usage message on the output stream from {@code out}.
     * @param opts the defined options.
     * @param out the A supplier of an OutputStream.
     */
    private static void printUsage(final Options opts, final IOSupplier<OutputStream> out) {
        try (Writer writer = new OutputStreamWriter(out.get())) {
            new Help(OPTION_COLLECTION, writer).printUsage(opts);
        } catch (IOException e) {
            DefaultLog.getInstance().error("Unable to open output stream", e);
        }
    }

    private Report() {
        // do not instantiate
    }
}
