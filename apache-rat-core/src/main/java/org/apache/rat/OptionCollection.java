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

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.api.Document;
import org.apache.rat.commandline.Arg;
import org.apache.rat.commandline.ArgumentContext;
import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.document.impl.FileDocument;
import org.apache.rat.help.Licenses;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.report.IReportable;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log.Level;
import org.apache.rat.walker.ArchiveWalker;
import org.apache.rat.walker.DirectoryWalker;

/**
 * The collection of standard options for the CLI as well as utility methods to manage them and methods to create the
 * ReportConfiguration from the options and an array of arguments.
 */
public final class OptionCollection {

    private OptionCollection() {
        // do not instantiate
    }

    public static final  Comparator<Option> optionComparator = new OptionComparator();

    /**
     * Produce help
     */
    public static final Option HELP = new Option("?", "help", false, "Print help for the RAT command line interface and exit.");

    /**
     * Provide license definition listing
     */
    public static final Option HELP_LICENSES = Option.builder().longOpt("help-licenses").desc("Print help for the RAT command line interface and exit.").build();

    /**
     * A mapping of {@code argName(value)} values to a description of those values.
     */
    private static final Map<String, Supplier<String>> ARGUMENT_TYPES;
    static {
        ARGUMENT_TYPES = new TreeMap<>();
        ARGUMENT_TYPES.put("File", () -> "A file name.");
        ARGUMENT_TYPES.put("DirOrArchive", () -> "A directory or archive file to scan.");
        ARGUMENT_TYPES.put("Expression", () -> "A wildcard file matching pattern. example: *-test-*.txt");
        ARGUMENT_TYPES.put("LicenseFilter", () -> format("A defined filter for the licenses to include. Valid values: %s.",
                asString(LicenseSetFactory.LicenseFilter.values())));
        ARGUMENT_TYPES.put("LogLevel", () -> format("The log level to use. Valid values %s.", asString(Level.values())));
        ARGUMENT_TYPES.put("ProcessingType", () -> format("Specifies how to process file types. Valid values are: %s",
                Arrays.stream(ReportConfiguration.Processing.values())
                        .map(v -> format("\t%s: %s", v.name(), v.desc()))
                        .collect(Collectors.joining(""))));
        ARGUMENT_TYPES.put("StyleSheet", () -> format("Either an external xsl file or maybe one of the internal named sheets. Internal sheets are: %s.",
                Arrays.stream(StyleSheets.values())
                        .map(v -> format("\t%s: %s", v.arg(), v.desc()))
                        .collect(Collectors.joining(""))));
        ARGUMENT_TYPES.put("LicenseID", () -> "The ID for a license.");
        ARGUMENT_TYPES.put("FamilyID", () -> "The ID for a license family.");
    }

    /**
     * Gets the mapping of {@code argName(value)} values to a description of those values.
     * @return the mapping of {@code argName(value)} values to a description of those values.
     */
    public static Map<String, Supplier<String>> getArgumentTypes() {
        return Collections.unmodifiableMap(ARGUMENT_TYPES);
    }

    /**
     * Join a collection of objects together as a comma separated list of their string values.
     * @param args the objects to join together.
     * @return the comma separated string.
     */
    private static String asString(final Object[] args) {
        return Arrays.stream(args).map(Object::toString).collect(Collectors.joining(", "));
    }

    /**
     * Parses the standard options to create a ReportConfiguration.
     *
     * @param args the arguments to parse
     * @param helpCmd the help command to run when necessary.
     * @return a ReportConfiguration or null if Help was printed.
     * @throws IOException on error.
     */
    public static ReportConfiguration parseCommands(final String[] args, final Consumer<Options> helpCmd) throws IOException {
        return parseCommands(args, helpCmd, false);
    }

    /**
     * Parses the standard options to create a ReportConfiguration.
     *
     * @param args the arguments to parse
     * @param helpCmd the help command to run when necessary.
     * @param noArgs If true then the commands do not need extra arguments
     * @return a ReportConfiguration or null if Help was printed.
     * @throws IOException on error.
     */
    public static ReportConfiguration parseCommands(final String[] args, final Consumer<Options> helpCmd, final boolean noArgs) throws IOException {
        Options opts = buildOptions();
        CommandLine commandLine;
        try {
            commandLine = DefaultParser.builder().setDeprecatedHandler(DeprecationReporter.getLogReporter())
                    .setAllowPartialMatching(true).build().parse(opts, args);
        } catch (ParseException e) {
            DefaultLog.getInstance().error(e.getMessage());
            DefaultLog.getInstance().error("Please use the \"--help\" option to see a list of valid commands and options", e);
            System.exit(1);
            return null; // dummy return (won't be reached) to avoid Eclipse complaint about possible NPE
            // for "commandLine"
        }

        Arg.processLogLevel(commandLine);

        if (commandLine.hasOption(HELP)) {
            helpCmd.accept(opts);
            return null;
        }

        if (commandLine.hasOption(HELP_LICENSES)) {
            new Licenses(createConfiguration(log, null, commandLine), new PrintWriter(System.out)).printHelp();
            return null;
        }

        // DIR or end of command line can provide args.
        String[] clArgs = {null};
        List<String> lst = new ArrayList<>();
        String dirValue = commandLine.getOptionValue(Arg.DIR.getSelected());
        if (dirValue != null) {
            lst.add(dirValue);
        }
        lst.addAll(Arrays.asList(commandLine.getArgs()));
        if (!noArgs && lst.size() != 1) {
            helpCmd.accept(opts);
            return null;
        }
        if (!lst.isEmpty()) {
            clArgs[0] = lst.get(0);
        }
        return createConfiguration(clArgs[0], commandLine);
    }

    /**
     * Create the report configuration.
     * Note: this method is package private for testing. You probably want one of the {@code ParseCommands} methods.
     * @param baseDirectory the base directory where files will be found.
     * @param cl the parsed command line.
     * @return a ReportConfiguration
     * @throws IOException on error.
     * @see #parseCommands(String[], Consumer)
     * @see #parseCommands(String[], Consumer, boolean)
     */
    static ReportConfiguration createConfiguration(final String baseDirectory, final CommandLine cl) throws IOException {
        final ReportConfiguration configuration = new ReportConfiguration();
        new ArgumentContext(configuration, cl).processArgs();
        if (StringUtils.isNotBlank(baseDirectory)) {
            configuration.setReportable(getDirectory(baseDirectory, configuration));
        }
        return configuration;
    }

    /**
     * Create an {@code Options} object from the list of defined Options.
     * Mutually exclusive options must be listed in an OptionGroup.
     * @return the Options comprised of the Options defined in this class.
     */
    public static Options buildOptions() {
        return Arg.getOptions().addOption(HELP);
    }

    /**
     * Creates an IReportable object from the directory name and ReportConfiguration
     * object.
     *
     * @param baseDirectory the directory that contains the files to report on.
     * @param config the ReportConfiguration.
     * @return the IReportable instance containing the files.
     */
    private static IReportable getDirectory(final String baseDirectory, final ReportConfiguration config) {
        File base = new File(baseDirectory);

        if (!base.exists()) {
            DefaultLog.getInstance().error( "Directory '" + baseDirectory + "' does not exist");
            return null;
        }

        Document doc = new FileDocument(base);
        if (base.isDirectory()) {
            return new DirectoryWalker(config, doc);
        }

        return new ArchiveWalker(config, doc);
    }

    /**
     * This class implements the {@code Comparator} interface for comparing Options.
     */
    private static class OptionComparator implements Comparator<Option>, Serializable {
        /** The serial version UID.  */
        private static final long serialVersionUID = 5305467873966684014L;

        private String getKey(final Option opt) {
            String key = opt.getOpt();
            key = key == null ? opt.getLongOpt() : key;
            return key;
        }

        /**
         * Compares its two arguments for order. Returns a negative integer, zero, or a
         * positive integer as the first argument is less than, equal to, or greater
         * than the second.
         *
         * @param opt1 The first Option to be compared.
         * @param opt2 The second Option to be compared.
         * @return a negative integer, zero, or a positive integer as the first argument
         * is less than, equal to, or greater than the second.
         */
        @Override
        public int compare(final Option opt1, final Option opt2) {
            return getKey(opt1).compareToIgnoreCase(getKey(opt2));
        }
    }
}
