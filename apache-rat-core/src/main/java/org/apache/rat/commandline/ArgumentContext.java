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
package org.apache.rat.commandline;

import static java.lang.String.format;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.utils.Log;

/**
 * Provides the context necessary to process various arguments.
 * @since 0.17
 */
public class ArgumentContext {
    /** The report configuration that is being built */
    private final ReportConfiguration configuration;
    /** The command line that is building the configuration */
    private final CommandLine commandLine;

    /**
     * Constructor.
     * @param configuration The configuration that is being built.
     * @param commandLine The command line that is building the configuration.
     */
    public ArgumentContext(final ReportConfiguration configuration, final CommandLine commandLine) {
        this.commandLine = commandLine;
        this.configuration = configuration;
    }

    public void processArgs() throws IOException {
        Arg.processArgs(this);
    }


    /**
     * Gets the configuration.
     * @return The configuration that is being built.
     */
    public ReportConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Gets the command line.
     * @return The caommand line that is driving the configuration.
     */
    public CommandLine getCommandLine() {
        return commandLine;
    }

    /**
     * Gets the log.
     * @return The log to write messages to.
     */
    public Log getLog() {
        return configuration.getLog();
    }

    /**
     * Logs a ParseException as a warning.
     * @param exception the parse exception to log
     * @param opt the option being processed
     * @param dflt The default value the option is being set to.
     */
    public void logParseException(final ParseException exception, final String opt, final Object dflt) {
        configuration.getLog().warn(format("Invalid %s specified: %s ", opt, commandLine.getOptionValue(opt)));
        configuration.getLog().warn(format("%s set to: %s", opt, dflt));
        configuration.getLog().debug(exception);
    }

    /**
     * Logs a ParseException as a warning.
     * @param exception the parse exception to log
     * @param opt the option being processed
     * @param dflt The default value the option is being set to.
     */
    public void logParseException(final ParseException exception, final Option opt, final Object dflt) {
        configuration.getLog().warn(format("Invalid %s specified: %s ", opt, commandLine.getOptionValue(opt)));
        configuration.getLog().warn(format("%s set to: %s", opt, dflt));
        configuration.getLog().debug(exception);
    }
}
