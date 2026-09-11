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
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.rat.api.RatException;
import org.apache.rat.commandline.Arg;
import org.apache.rat.commandline.ArgumentContext;
import org.apache.rat.help.Licenses;
import org.apache.rat.report.Reportable;
import org.apache.rat.ui.UIOption;
import org.apache.rat.ui.UIOptionCollection;
import org.apache.rat.utils.DefaultLog;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Uses the AbstractOptionCollection to parse the command line options.
 * Contains utility methods to ReportConfiguration from the options and an array of arguments.
 *
 * @param <T> The UIOption type that this parser is handling.
 */
@SuppressFBWarnings("EI_EXPOSE_REP2")
public final class OptionCollectionParser<T extends UIOption<T>> {
    /**
     * The OptionCollection that we are working with.
     */
    private final UIOptionCollection<T> uiOptionCollection;

    /**
     * Constructor.
     * @param optionCollection the option collection to use for.
     */
    public OptionCollectionParser(final UIOptionCollection<T> optionCollection) {
        this.uiOptionCollection = optionCollection;
    }

    /**
     * Parses the standard options to create a ReportConfiguration.
     *
     * @param workingDirectory the directory to resolve relative file names against.
     * @param args the arguments to parse.
     * @return the ArgumentContext for the process.
     * @throws RatException on error.
     */
    public ArgumentContext parseCommands(final File workingDirectory, final String[] args)
            throws RatException {
        return parseCommands(workingDirectory, args, uiOptionCollection.getOptions());
    }

    /**
     * Parse the options into the command line.
     * @param opts the option definitions.
     * @param args the argument to apply the definitions to.
     * @return the CommandLine
     * @throws ParseException on option parsing error.
     */
    public static CommandLine parseCommandLine(final Options opts, final String[] args) throws ParseException {
        try {
            return DefaultParser.builder().setDeprecatedHandler(DeprecationReporter.getLogReporter())
                    .setAllowPartialMatching(true).build().parse(opts, args);
        } catch (ParseException e) {
            DefaultLog.getInstance().error(e.getMessage());
            DefaultLog.getInstance().error("Please use the \"--help\" option to see a list of valid commands and options.", e);
            throw e;
        }
    }

    // visible for testing
    void printHelp(final ArgumentContext argumentContext) throws RatException {
        try {
            new Licenses(argumentContext.getConfiguration(),
                    new PrintWriter(argumentContext.getConfiguration().getOutput().get(),
                            false, StandardCharsets.UTF_8)).printHelp();
        } catch (IOException e) {
            throw new RatException("Unable to print help: " + e.getMessage(), e);
        }
    }

    /**
     * Parses the standard options to create a ReportConfiguration.
     *
     * @param workingDirectory The directory to resolve relative file names against.
     * @param args the arguments to parse.
     * @param options an Options object containing Apache command line options.
     * @return the ArgumentContext for the process.
     * @throws RatException on error.
     */
    // visible for testing
    ArgumentContext parseCommands(final File workingDirectory, final String[] args,
                                                                       final Options options) throws RatException {
        try {
            ArgumentContext argumentContext = new ArgumentContext(workingDirectory, options, args);
            Arg.processLogLevel(argumentContext, uiOptionCollection);
            populateConfiguration(argumentContext);
            if (uiOptionCollection.isSelected(Arg.HELP_LICENSES)) {
                printHelp(argumentContext);
            }
            return argumentContext;
        } catch (ParseException e) {
            throw new RatException("Unable to parse command line: " + e.getMessage(), e);
        }
    }

    /**
     * Create the report configuration.
     * Note: this method is package private for testing.
     * You probably want one of the {@code parseCommands(..)} methods.
     * @param argumentContext The context to execute in.
     * @return a ReportConfiguration
     */
    private ReportConfiguration populateConfiguration(final ArgumentContext argumentContext) {
        argumentContext.processArgs(uiOptionCollection);
        final ReportConfiguration configuration = argumentContext.getConfiguration();
        final CommandLine commandLine = argumentContext.getCommandLine();
        if (!configuration.hasSource()) {
            for (String s : commandLine.getArgs()) {
                Reportable reportable = OptionCollection.getReportable(new File(s), configuration);
                if (reportable != null) {
                    configuration.addSource(reportable);
                }
            }
        }
        return configuration;
    }
}
