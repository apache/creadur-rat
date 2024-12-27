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

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.document.DocumentName;
import org.apache.rat.utils.DefaultLog;

import static java.lang.String.format;

/**
 * Provides the context necessary to process various arguments.
 * @since 0.17
 */
public class ArgumentContext {
    /** The report configuration that is being built */
    private final ReportConfiguration configuration;
    /** The command line that is building the configuration */
    private final CommandLine commandLine;
    /** The directory from which relative file names will be resolved */
    private final DocumentName workingDirectory;

    /**
     * Creates a context with the specified configuration.
     * @param workingDirectory the directory from which relative file names will be resolved.
     * @param configuration The configuration that is being built.
     * @param commandLine The command line that is building the configuration.
     */
    public ArgumentContext(final File workingDirectory, final ReportConfiguration configuration, final CommandLine commandLine) {
        this.workingDirectory = DocumentName.builder().setName(workingDirectory.getAbsoluteFile()).build();
        this.commandLine = commandLine;
        this.configuration = configuration;
    }

    /**
     * Creates a context with an emtpy configuration.
     * @param workingDirectory The directory from which to resolve relative file names.
     * @param commandLine The command line.
     */
    public ArgumentContext(final File workingDirectory, final CommandLine commandLine) {
        this(workingDirectory, new ReportConfiguration(), commandLine);
    }

    /**
     * Process the arguments specified in this context.
     */
    public void processArgs() {
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
     * @return The command line that is driving the configuration.
     */
    public CommandLine getCommandLine() {
        return commandLine;
    }

    /**
     * Gets the directory name from which releative file names will be resolved.
     * @return The directory name from which releative file names will be resolved.
     */
    public DocumentName getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Resolves the file name to the working directory if necessayr.
     * @param fileName the file name to resolve.
     * @return the file name string.
     */
    public File resolve(final String fileName) {
        if (!fileName.startsWith("/")) {
            DocumentName fn = DocumentName.builder().setName(fileName).setBaseName("/").build();
            return new File(workingDirectory.resolve(fn.localized()).getName());
        }
        return new File(fileName);
    }

    /**
     * Logs a ParseException as a warning.
     * @param exception the parse exception to log
     * @param opt the option being processed
     * @param defaultValue The default value the option is being set to.
     */
    public void logParseException(final ParseException exception, final Option opt, final Object defaultValue) {
        DefaultLog.getInstance().warn(format("Invalid %s specified: %s ", opt, commandLine.getOptionValue(opt)));
        DefaultLog.getInstance().warn(format("%s set to: %s", opt, defaultValue));
        DefaultLog.getInstance().debug(exception);
    }
}
