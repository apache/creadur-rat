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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.commons.cli.AlreadySelectedException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.ConfigurationException;
import org.apache.rat.OptionCollectionParser;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.document.DocumentName;
import org.apache.rat.ui.UIOptionCollection;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;

import static java.lang.String.format;

/**
 * Provides the context necessary to process various arguments.
 * @since 0.17
 */
public final class ArgumentContext {
    /** The report configuration that is being built */
    private final ReportConfiguration configuration;
    /** The command line that is building the configuration */
    private final CommandLine commandLine;
    /** The directory from which relative file names will be resolved */
    private final DocumentName workingDirectory;

    /**
     * Creates a context with the specified configuration.
     * @param workingDirectory the directory from which relative file names will be resolved.
     * @param configuration the configuration that is being built.
     * @param opts the Options for the command line.
     * @param args the arguments for the options.
     * @throws ParseException if the options can not parse the arguments.
     */
    public ArgumentContext(final File workingDirectory, final ReportConfiguration configuration, final Options opts, final String[] args)
            throws ParseException {
        this.workingDirectory = DocumentName.builder(workingDirectory).build();
        this.commandLine = OptionCollectionParser.parseCommandLine(clearSelected(opts), args);
        this.configuration = configuration;
    }

    /**
     * Creates a context with an empty configuration.
     * @param workingDirectory the directory from which to resolve relative file names.
     * @param opts the Options for the command line.
     * @param args the arguments for the options.
     * @throws ParseException if the options can not parse the arguments.
     */
    public ArgumentContext(final File workingDirectory, final Options opts, final String[] args) throws ParseException {
        this(workingDirectory, new ReportConfiguration(), opts, args);
    }

    /**
     * Gets a logging/debug string describing the Option.
     * @param option the option to describe.
     * @return the logging/debug string describing the Option.
     */
    public static String toString(final Option option) {
        return String.format("Option[%s v:[%s]",
                StringUtils.defaultIfEmpty(option.getLongOpt(), option.getOpt()),
                option.getValues() == null ? new String[0] : option.getValues()
                );
    }
    /**
     * Clears the group selections in the options.
     * @param options the options to clear.
     * @return the options with all selections cleared.
     */
    private static Options clearSelected(final Options options) {
        for (Option opt : options.getOptions()) {
            OptionGroup group = options.getOptionGroup(opt);
            if (group != null) {
                try {
                    group.setSelected(null);
                } catch (AlreadySelectedException e) {
                    throw new RuntimeException("Unexpected error with null argument", e);
                }
            }
        }
        return options;
    }

    /**
     * Process the arguments specified in this context.
     */
    public void processArgs(final UIOptionCollection<?> uiOptionCollection) {
        Arg.processArgs(this, uiOptionCollection);
    }

    /**
     * Gets the configuration.
     * @return the configuration that is being built.
     */
    public ReportConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Gets the Options from the command line.
     * @return the Options from the command line.
     */
    public Option[] getOptions() {
        return commandLine.getOptions();
    }

    /**
     * Gets the arguments that followed all options on the command line.
     * @return the arguments that followed all options.
     */
    public List<String> getArgs() {
        return commandLine.getArgList();
    }

    /**
     * Determines if option was specified on the command line.
     * @param option the option to check.
     * @return {@code true}, if option was specified on the command line.
     */
    public boolean hasOption(final Option option) {
        return commandLine.hasOption(option);
    }

    /**
     * Determines if option was specified on the command line.
     * @param option the option to check.
     * @return {@code true}, if option was specified on the command line.
     */
    public boolean hasOption(final String option) {
        return commandLine.hasOption(option);
    }

    /**
     * Gets the option value or {@code null}} if it is not set on the command line.
     * @param selected the option to retreive the value for.
     * @return the option value or {@code null}} if it is not set.
     */
    public String getOptionValue(final Option selected) {
        return commandLine.getOptionValue(selected);
    }

    /**
     * Gets the list of option values from the command line.
     * @param selected the option to get values for.
     * @return The list of options from the command line.  May be an empty list but never {@code null}
     */
    public List<String> getOptionValues(final Option selected) {
        String[] result = commandLine.getOptionValues(selected);
        return result == null ? Collections.emptyList() : List.of(result);
    }

    /**
     * Gets the parsed option value from the command line.
     * @param selected the option to get value for.
     * @return the parsed value or null if not found.
     * @param <T> the expected parsed value type.
     */
    public <T> T getParsedOptionValue(final Option selected) {
        return this.getParsedOptionValue(selected, () -> null);
    }

    /**
     * Gets the parsed option value from the command line.
     * @param selected the option to get value for.
     * @param defaultSupplier a supplier of default values.
     * @return the parsed value or default value if not found.
     * @param <T> the expected parsed value type.
     */
    public <T> T getParsedOptionValue(final Option selected, final Supplier<T> defaultSupplier) {
        Objects.requireNonNull(selected);
        Objects.requireNonNull(defaultSupplier);
        try {
            return commandLine.getParsedOptionValue(selected, defaultSupplier);
        } catch (ParseException e) {
            logParseException(e, selected);
            throw new ConfigurationException(format("'%s' converter '%s' does not produce a class of type %s",
                    toString(selected),
                    selected.getConverter().getClass().getName(),
                    selected.getType()), e);
        }
    }

    /**
     * Gets the parsed option values from the command line.
     * @param selected the option to get value for.
     * @return the parsed value list an empty list if not found.
     * @param <T> the expected parsed value type.
     */
    public <T> List<T> getParsedOptionValues(final Option selected) {
        return this.getParsedOptionValues(selected, () -> Collections.emptyList());
    }


    /**
     * Gets the parsed option values from the command line.
     * @param selected the option to get value for.
     * @param defaultSupplier a supplier of default value list.
     * @return the parsed value list or default value if not found.
     * @param <T> the expected parsed value type.
     */
    public <T> List<T> getParsedOptionValues(final Option selected, final Supplier<List<T>> defaultSupplier) {
        Objects.requireNonNull(selected);
        Objects.requireNonNull(defaultSupplier);
        Class<? extends T> clazz = (Class<? extends T>) selected.getType();
        List<String> strings = getOptionValues(selected);
        if (strings.isEmpty()) {
            return defaultSupplier.get();
        }
        List<T> result = new ArrayList<>();
        for (String value : strings) {
            try {
                result.add((T) selected.getConverter().apply(value));
            } catch (Throwable e) {
                if (e instanceof Error err) {
                    throw err;
                }
                throw new ConfigurationException(format("'%s' converter '%s' does not produce a class of type %s",
                        toString(selected),
                        selected.getConverter().getClass().getName(),
                        selected.getType()), e);
            }
        }
        return result;
    }

    /**
     * Gets the directory name from which relative file names will be resolved.
     * @return the directory name from which relative file names will be resolved.
     */
    public DocumentName getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Logs a ParseException as a warning and the exception itself as a debug.
     * @param exception the parse exception to log.
     * @param opt the option being processed.
     */
    public void logParseException(final ParseException exception, final Option opt) {
        DefaultLog.getInstance().warn(format("Invalid %s specified: %s ", toString(opt), commandLine.getOptionValue(opt)));
        if (DefaultLog.getInstance().isEnabled(Log.Level.DEBUG)) {
            DefaultLog.getInstance().debug(exception);
        }
    }

    /**
     * Creates a visual desciption of the CommandLine.  This used to be provided by the {@code CommandLine.toString()}
     * method, but that has been removed.
     * @param commandLine the command line to format.
     * @return a String representation of the command line suitable for debugging.
     */
    public static String commandLineDescription(final CommandLine commandLine) {
        List<String> options = new ArrayList<>();
        for (Option opt : commandLine.getOptions()) {
            options.add(String.format("Option[%s v:[%s]]", StringUtils.defaultIfEmpty(opt.getLongOpt(), opt.getKey()),
                    String.join(",", opt.getValues() == null ? new String [0] : opt.getValues())));
        }
        return new StringBuilder()
                .append("[ CommandLine: [ options: ")
                .append(options)
                .append(" ] [ args: ")
                .append(commandLine.getArgList().toString())
                .append(" ] ]")
                .toString();
    }
}
