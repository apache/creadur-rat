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
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import org.apache.rat.api.Document;
import org.apache.rat.commandline.Arg;
import org.apache.rat.commandline.ArgumentContext;
import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.config.exclusion.StandardCollection;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.document.FileDocument;
import org.apache.rat.help.Licenses;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.report.IReportable;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log.Level;
import org.apache.rat.walker.ArchiveWalker;
import org.apache.rat.walker.DirectoryWalker;

import static java.lang.String.format;

/**
 * The collection of standard options for the CLI as well as utility methods to manage them and methods to create the
 * ReportConfiguration from the options and an array of arguments.
 */
public final class OptionCollection {

    private OptionCollection() {
        // do not instantiate
    }

    /** The Option comparator to sort the help */
    public static final Comparator<Option> OPTION_COMPARATOR = new OptionComparator();

    /** The Help option */
    public static final Option HELP = new Option("?", "help", false, "Print help for the RAT command line interface and exit.");

    /** Provide license definition listing */
    public static final Option HELP_LICENSES = Option.builder().longOpt("help-licenses")
            .desc("Print help for the RAT command line interface and exit.").build();

    /** A mapping of {@code argName(value)} values to a description of those values. */
    @Deprecated
    private static final Map<String, Supplier<String>> ARGUMENT_TYPES;
    static {
        ARGUMENT_TYPES = new TreeMap<>();
        for (ArgumentType argType : ArgumentType.values()) {
            ARGUMENT_TYPES.put(argType.getDisplayName(), argType.description);
        }
    }

    /**
     * Gets the mapping of {@code argName(value)} values to a description of those values.
     * @return the mapping of {@code argName(value)} values to a description of those values.
     * @deprecated use {@link ArgumentType}
     */
    @Deprecated
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
     * @param workingDirectory The directory to resolve relative file names against.
     * @param args the arguments to parse
     * @param helpCmd the help command to run when necessary.
     * @return a ReportConfiguration or {@code null} if Help was printed.
     * @throws IOException on error.
     */
    public static ReportConfiguration parseCommands(final File workingDirectory, final String[] args, final Consumer<Options> helpCmd) throws IOException {
        return parseCommands(workingDirectory, args, helpCmd, false);
    }

    /**
     * Parses the standard options to create a ReportConfiguration.
     *
     * @param workingDirectory The directory to resolve relative file names against.
     * @param args the arguments to parse
     * @param helpCmd the help command to run when necessary.
     * @param noArgs If true then the commands do not need extra arguments
     * @return a ReportConfiguration or {@code null} if Help was printed.
     * @throws IOException on error.
     */
    public static ReportConfiguration parseCommands(final File workingDirectory, final String[] args,
                                                    final Consumer<Options> helpCmd, final boolean noArgs) throws IOException {
        Options opts = buildOptions();
        CommandLine commandLine;
        try {
            commandLine = DefaultParser.builder().setDeprecatedHandler(DeprecationReporter.getLogReporter())
                    .setAllowPartialMatching(true).build().parse(opts, args);
        } catch (ParseException e) {
            DefaultLog.getInstance().error(e.getMessage());
            DefaultLog.getInstance().error("Please use the \"--help\" option to see a list of valid commands and options.", e);
            System.exit(1);
            return null; // dummy return (won't be reached) to avoid Eclipse complaint about possible NPE
            // for "commandLine"
        }

        Arg.processLogLevel(commandLine);

        ArgumentContext argumentContext = new ArgumentContext(workingDirectory, commandLine);
        if (commandLine.hasOption(HELP)) {
            helpCmd.accept(opts);
            return null;
        }

        if (commandLine.hasOption(HELP_LICENSES)) {
            new Licenses(createConfiguration(argumentContext), new PrintWriter(System.out)).printHelp();
            return null;
        }

        if (commandLine.hasOption(Arg.HELP_LICENSES.option())) {
            new Licenses(createConfiguration(argumentContext), new PrintWriter(System.out)).printHelp();
            return null;
        }

        ReportConfiguration configuration = createConfiguration(argumentContext);
        if (!noArgs && !configuration.hasSource()) {
            String msg = "No directories or files specified for scanning. Did you forget to close a multi-argument option?";
            DefaultLog.getInstance().error(msg);
            helpCmd.accept(opts);
            throw new ConfigurationException(msg);
        }

        return configuration;
    }

    /**
     * Create the report configuration.
     * Note: this method is package private for testing.
     * You probably want one of the {@code ParseCommands} methods.
     * @param argumentContext The context to execute in.
     * @return a ReportConfiguration
     * @see #parseCommands(File, String[], Consumer)
     * @see #parseCommands(File, String[], Consumer, boolean)
     */
    static ReportConfiguration createConfiguration(final ArgumentContext argumentContext) {
        argumentContext.processArgs();
        final ReportConfiguration configuration = argumentContext.getConfiguration();
        final CommandLine commandLine = argumentContext.getCommandLine();
        if (Arg.DIR.isSelected()) {
            try {
                configuration.addSource(getReportable(commandLine.getParsedOptionValue(Arg.DIR.getSelected()), configuration));
            } catch (ParseException e) {
                throw new ConfigurationException("Unable to set parse " + Arg.DIR.getSelected(), e);
            }
        }
        for (String s : commandLine.getArgs()) {
            IReportable reportable = getReportable(new File(s), configuration);
            if (reportable != null) {
                configuration.addSource(reportable);
            }
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
     * @param base the directory that contains the files to report on.
     * @param config the ReportConfiguration.
     * @return the IReportable instance containing the files.
     */
    static IReportable getReportable(final File base, final ReportConfiguration config) {
        File absBase = base.getAbsoluteFile();
        DocumentName documentName = DocumentName.builder(absBase).build();
        if (!absBase.exists()) {
            DefaultLog.getInstance().error("Directory '" + documentName + "' does not exist.");
            return null;
        }
        DocumentNameMatcher documentExcluder = config.getDocumentExcluder(documentName);

        Document doc = new FileDocument(documentName, absBase, documentExcluder);
        if (!documentExcluder.matches(doc.getName())) {
            DefaultLog.getInstance().error("Directory '" + documentName + "' is in excluded list.");
            return null;
        }

        if (absBase.isDirectory()) {
            return new DirectoryWalker(doc);
        }

        return new ArchiveWalker(doc);
    }

    /**
     * This class implements the {@code Comparator} interface for comparing Options.
     */
    private static final class OptionComparator implements Comparator<Option>, Serializable {
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

    public enum ArgumentType {
        /**
         * A plain file.
         */
        FILE("File", () -> "A file name."),
        /**
         * An Integer.
         */
        INTEGER("Integer", () -> "An integer value."),
        /**
         * A directory or archive.
         */
        DIRORARCHIVE("DirOrArchive", () -> "A directory or archive file to scan."),
        /**
         * A matching expression.
         */
        EXPRESSION("Expression", () -> "A file matching pattern usually of the form used in Ant build files and " +
                "'.gitignore' files (see https://ant.apache.org/manual/dirtasks.html#patterns for examples). " +
                "Regular expression patterns may be specified by surrounding the pattern with '%regex[' and ']'. " +
                "For example '%regex[[A-Z].*]' would match files and directories that start with uppercase latin letters."),
        /**
         * A license filter.
         */
        LICENSEFILTER("LicenseFilter", () -> format("A defined filter for the licenses to include. Valid values: %s.",
                asString(LicenseSetFactory.LicenseFilter.values()))),
        /**
         * A log level.
         */
        LOGLEVEL("LogLevel", () -> format("The log level to use. Valid values %s.", asString(Level.values()))),
        /**
         * A processing type.
         */
        PROCESSINGTYPE("ProcessingType", () -> format("Specifies how to process file types. Valid values are: %s%n",
                Arrays.stream(ReportConfiguration.Processing.values())
                        .map(v -> format("\t%s: %s", v.name(), v.desc()))
                        .collect(Collectors.joining(System.lineSeparator())))),
        /**
         * A style sheet.
         */
        STYLESHEET("StyleSheet", () -> format("Either an external xsl file or one of the internal named sheets. Internal sheets are: %n%s",
                Arrays.stream(StyleSheets.values())
                        .map(v -> format("\t%s: %s%n", v.arg(), v.desc()))
                        .collect(Collectors.joining(System.lineSeparator())))),
        /**
         * A license id.
         */
        LICENSEID("LicenseID", () -> "The ID for a license."),
        /**
         * A license family id.
         */
        FAMILYID("FamilyID", () -> "The ID for a license family."),
        /**
         * A standard collection name.
         */
        STANDARDCOLLECTION("StandardCollection", () -> format("Defines standard expression patterns (see above). Valid values are: %n%s%n",
                Arrays.stream(StandardCollection.values())
                        .map(v -> format("\t%s: %s%n", v.name(), v.desc()))
                        .collect(Collectors.joining(System.lineSeparator())))),
        /**
         * A Counter pattern name
         */
        COUNTERPATTERN("CounterPattern", () -> format("A pattern comprising one of the following prefixes followed by " +
                        "a colon and a count (e.g. %s:5).  Prefixes are %n%s.", ClaimStatistic.Counter.UNAPPROVED,
                Arrays.stream(ClaimStatistic.Counter.values())
                        .map(v -> format("\t%s: %s Default range [%s, %s]%n", v.name(), v.getDescription(),
                                v.getDefaultMinValue(),
                                v.getDefaultMaxValue() == -1 ? "unlimited" : v.getDefaultMaxValue()))
                        .collect(Collectors.joining(System.lineSeparator())))),
        /**
         * A generic argument.
         */
        ARG("Arg", () -> "A string"),
        /**
         * No argument.
         */
        NONE("", () -> "");

        /**
         * The display name
         */
        private final String displayName;
        /**
         * A supplier of the description
         */
        private final Supplier<String> description;

        ArgumentType(final String name,
                     final Supplier<String> description) {
            this.displayName = name;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Supplier<String> description() {
            return description;
        }
    }
}
