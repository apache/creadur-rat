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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Converter;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.function.IOSupplier;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.rat.config.AddLicenseHeaders;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.report.IReportable;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;
import org.apache.rat.utils.Log.Level;
import org.apache.rat.walker.ArchiveWalker;
import org.apache.rat.walker.DirectoryWalker;

import static java.lang.String.format;

/**
 * The CLI based configuration object for report generation.
 */
public class Report {

    private static final String[] NOTES = {
            "Rat highlights possible issues.",
            "Rat reports require interpretation.",
            "Rat often requires some tuning before it runs well against a project.",
            "Rat relies on heuristics: it may miss issues"
    };

    private enum StyleSheets { PLAIN("plain-rat", "The default style"),
        MISSING_HEADERS("missing-headers", "Produces a report of files that are missing headers"),
        UNAPPROVED_LICENSES("unapproved-licenses", "Produces a report of the files with unapproved licenses");
        private final String arg;
        private final String desc;
        StyleSheets(String arg, String description) {
            this.arg = arg;
            this.desc = description;
        }

        public String arg() {
            return arg;
        }

        public String desc() {
            return desc;
        }
    }

    private static final Map<String, Supplier<String>> ARGUMENT_TYPES;

    static {
        ARGUMENT_TYPES = new TreeMap<>();
        ARGUMENT_TYPES.put("FileOrURI", () -> "A file name or URI");
        ARGUMENT_TYPES.put("DirOrArchive", () -> "A director or archive file to scan");
        ARGUMENT_TYPES.put("Expression", () -> "A wildcard file matching pattern. example: *-test-*.txt");
        ARGUMENT_TYPES.put("LicenseFilter", () -> format("A defined filter for the licenses to include.  Valid values: %s.",
                asString(LicenseFilter.values())));
        ARGUMENT_TYPES.put("LogLevel", () -> format("The log level to use.  Valid values %s.", asString(Log.Level.values())));
        ARGUMENT_TYPES.put("ProcessingType", () -> format("Specifies how to process file types.  Valid values are: %s",
                Arrays.stream(ReportConfiguration.Processing.values())
                        .map(v -> format("\t%s: %s", v.name(), v.desc()))
                        .collect(Collectors.joining(""))));
        ARGUMENT_TYPES.put("StyleSheet", () -> format("Either an external xsl file may be one of the internal named sheets.  Internal sheets are: %s.",
                Arrays.stream(StyleSheets.values())
                        .map(v -> format("\t%s: %s", v.arg(), v.desc()))
                        .collect(Collectors.joining(""))));
    }

    // RAT-85/RAT-203: Deprecated! added only for convenience and for backwards
    // compatibility
    /**
     * Adds license headers to files missing headers.
     */
    private static final OptionGroup ADD = new OptionGroup()
            .addOption(new Option("a", false,
                    "(deprecated) Add the default license header to any file with an unknown license.  Use '-A' or ---addLicense instead."))
            .addOption(new Option("A", "addLicense", false,
                    "Add the default license header to any file with an unknown license that is not in the exclusion list. "
                            + "By default new files will be created with the license header, "
                            + "to force the modification of existing files use the --force option.")
            );

    /**
     * Defines the output for the file.
     */
    static final Option OUT = Option.builder().option("o").longOpt("out").hasArg()
            .desc("Define the output file where to write a report to (default is System.out).")
            .converter(Converter.FILE).build();

    static final Option DIR = Option.builder().option("d").longOpt("dir").hasArg()
            .desc("(deprecated, use '--') Used to indicate source when using --exclude.").argName("DirOrArchive").build();

    /**
     * Forces changes to be written to new files.
     */
    static final Option FORCE = new Option("f", "force", false,
            "Forces any changes in files to be written directly to the source files (i.e. new files are not created).");
    /**
     * Defines the copyright header to add to the file.
     */
    static final Option COPYRIGHT = Option.builder().option("c").longOpt("copyright").hasArg()
            .desc("The copyright message to use in the license headers, usually in the form of \"Copyright 2008 Foo\"")
            .build();
    /**
     * Name of File to exclude from report consideration.
     */
    static final Option EXCLUDE_CLI = Option.builder("e").longOpt("exclude").hasArgs().argName("Expression")
            .desc("Excludes files matching wildcard <expression>. May be followed by multiple arguments. "
                    + "Note that '--' or a following option is required when using this parameter.")
            .build();
    /**
     * Name of file that contains a list of files to exclude from report
     * consideration.
     */
    static final Option EXCLUDE_FILE_CLI = Option.builder("E").longOpt("exclude-file")
            .argName("FileOrURI")
            .hasArg().desc("Excludes files matching regular expression in the input file.")
            .build();

    /**
     * The stylesheet to use to style the XML output.
     */
    static final Option STYLESHEET_CLI = Option.builder("s").longOpt("stylesheet").hasArg()
            .desc("XSLT stylesheet to use when creating the report.  Not compatible with -x.  " +
                    "Either an external xsl file may be specified or one of the internal named sheets: plain-rat (default), missing-headers, or unapproved-licenses")
            .build();
    /**
     * Produce help
     */
    static final Option HELP = new Option("h", "help", false, "Print help for the RAT command line interface and exit.");
    /**
     * Flag to identify a file with license definitions.
     */
    static final Option LICENSES = Option.builder().longOpt("licenses").hasArgs().argName("FileOrURI")
            .desc("File names or URLs for license definitions")
            .build();
    /**
     * Do not use the default files.
     */
    static final Option NO_DEFAULTS = new Option(null, "no-default-licenses", false, "Ignore default configuration. By default all approved default licenses are used");

    /**
     * Scan hidden directories.
     */
    static final Option SCAN_HIDDEN_DIRECTORIES = new Option(null, "scan-hidden-directories", false, "Scan hidden directories");

    /**
     * List the licenses that were used for the run.
     */
    static final Option LIST_LICENSES = Option.builder().longOpt("list-licenses").hasArg().argName("LicenseFilter")
            .desc("List the defined licenses (default is NONE). Valid options are: " + asString(LicenseFilter.values()))
            .converter(s -> LicenseFilter.valueOf(s.toUpperCase()))
            .build();

    /**
     * List the all families for the run.
     */
    static final Option LIST_FAMILIES = Option.builder().longOpt("list-families").hasArg().argName("LicenseFilter")
            .desc("List the defined license families (default is NONE). Valid options are: " + asString(LicenseFilter.values()))
            .converter(s -> LicenseFilter.valueOf(s.toUpperCase()))
            .build();

    static final Option LOG_LEVEL = Option.builder().longOpt("log-level")
            .hasArgs().argName("LogLevel")
            .desc("sets the log level.")
            .converter(s -> Log.Level.valueOf(s.toUpperCase()))
            .build();

    static final Option DRY_RUN = Option.builder().longOpt("dry-run")
            .desc("If set do not update the files but generate the reports.")
            .build();
    /**
     * Set unstyled XML output
     */
    static final Option XML = new Option("x", "xml", false, "Output the report in raw XML format.  Not compatible with -s");

    /**
     * Specify the processing of ARCHIVE files.
     */
    static final Option ARCHIVE = Option.builder().longOpt("archive").hasArg().argName("ProcessingType")
            .desc(format("Specifies how ARCHIVE processing will be handled. (default is %s)",
                    ReportConfiguration.Processing.NOTIFICATION))
            .converter(s -> ReportConfiguration.Processing.valueOf(s.toUpperCase()))
            .build();

    private static String asString(Object[] args) {
        return Arrays.stream(args).map(Object::toString).collect(Collectors.joining(", "));
    }

    /**
     * Processes the command line and builds a configuration and executes the
     * report.
     *
     * @param args the arguments.
     * @throws Exception on error.
     */
    public static void main(String[] args) throws Exception {
        ReportConfiguration configuration = parseCommands(args, Report::printUsage);
        if (configuration != null) {
            configuration.validate(DefaultLog.INSTANCE::error);
            new Reporter(configuration).output();
        }
    }

    /**
     * Parses the standard options to create a ReportConfiguraton.
     *
     * @param args    the arguments to parse
     * @param helpCmd the help command to run when necessary.
     * @return a ReportConfiguration or null if Help was printed.
     * @throws IOException on error.
     */
    public static ReportConfiguration parseCommands(String[] args, Consumer<Options> helpCmd) throws IOException {
        return parseCommands(args, helpCmd, false);
    }

    /**
     * Parses the standard options to create a ReportConfiguraton.
     *
     * @param args    the arguments to parse
     * @param helpCmd the help command to run when necessary.
     * @param noArgs  If true then the commands do not need extra arguments
     * @return a ReportConfiguration or null if Help was printed.
     * @throws IOException on error.
     */
    public static ReportConfiguration parseCommands(String[] args, Consumer<Options> helpCmd, boolean noArgs) throws IOException {
        Options opts = buildOptions();
        CommandLine cl;
        try {
            cl = new DefaultParser().parse(opts, args);
        } catch (ParseException e) {
            DefaultLog.INSTANCE.error(e.getMessage());
            DefaultLog.INSTANCE.error("Please use the \"--help\" option to see a list of valid commands and options");
            System.exit(1);
            return null; // dummy return (won't be reached) to avoid Eclipse complaint about possible NPE
            // for "cl"
        }

        if (cl.hasOption(LOG_LEVEL)) {
            try {
                DefaultLog.INSTANCE.setLevel(cl.getParsedOptionValue(LOG_LEVEL));
            } catch (ParseException e) {
                logParseException(e, LOG_LEVEL, cl, DefaultLog.INSTANCE.getLevel());
            }
        }
        if (cl.hasOption(HELP)) {
            helpCmd.accept(opts);
            return null;
        }

        if (!noArgs) {
            args = cl.getArgs();
            if (args == null || args.length != 1) {
                helpCmd.accept(opts);
                return null;
            }
        } else {
            args = new String[]{null};
        }
        return createConfiguration(args[0], cl);
    }

    private static void logParseException(ParseException e, Option opt, CommandLine cl, Object dflt) {
        DefaultLog.INSTANCE.warn(format("Invalid %s specified: %s ", opt.getOpt(), cl.getOptionValue(opt)));
        DefaultLog.INSTANCE.warn(format("%s set to: %s", opt.getOpt(), dflt));
    }

    static ReportConfiguration createConfiguration(String baseDirectory, CommandLine cl) throws IOException {
        final ReportConfiguration configuration = new ReportConfiguration(DefaultLog.INSTANCE);

        configuration.setDryRun(cl.hasOption(DRY_RUN));
        if (cl.hasOption(LIST_FAMILIES)) {
            try {
                configuration.listFamilies(cl.getParsedOptionValue(LIST_FAMILIES));
            } catch (ParseException e) {
                logParseException(e, LIST_FAMILIES, cl, Defaults.LIST_FAMILIES);
            }
        }

        if (cl.hasOption(LIST_LICENSES)) {
            try {
                configuration.listFamilies(cl.getParsedOptionValue(LIST_LICENSES));
            } catch (ParseException e) {
                logParseException(e, LIST_LICENSES, cl, Defaults.LIST_LICENSES);
            }
        }

        if (cl.hasOption(ARCHIVE)) {
            try {
                configuration.setArchiveProcessing(cl.getParsedOptionValue(ARCHIVE));
            } catch (ParseException e) {
                logParseException(e, ARCHIVE, cl, Defaults.ARCHIVE_PROCESSING);
            }
        }

        if (cl.hasOption(OUT)) {
            try {
                configuration.setOut((File) cl.getParsedOptionValue(OUT));
            } catch (ParseException e) {
                logParseException(e, OUT, cl, "System.out");
            }
        }

        if (cl.hasOption(SCAN_HIDDEN_DIRECTORIES)) {
            configuration.setDirectoriesToIgnore(FalseFileFilter.FALSE);
        }

        if (ADD.getSelected() != null) {
            configuration.setAddLicenseHeaders(cl.hasOption(FORCE) ? AddLicenseHeaders.FORCED : AddLicenseHeaders.TRUE);
            configuration.setCopyrightMessage(cl.getOptionValue(COPYRIGHT));
        }

        if (cl.hasOption(EXCLUDE_CLI)) {
            String[] excludes = cl.getOptionValues(EXCLUDE_CLI);
            if (excludes != null) {
                final FilenameFilter filter = parseExclusions(Arrays.asList(excludes));
                configuration.setFilesToIgnore(filter);
            }
        } else if (cl.hasOption(EXCLUDE_FILE_CLI)) {
            String excludeFileName = cl.getOptionValue(EXCLUDE_FILE_CLI);
            if (excludeFileName != null) {
                final FilenameFilter filter = parseExclusions(
                        FileUtils.readLines(new File(excludeFileName), StandardCharsets.UTF_8));
                configuration.setFilesToIgnore(filter);
            }
        }

        if (cl.hasOption(XML)) {
            configuration.setStyleReport(false);
        } else {
            configuration.setStyleReport(true);
            if (cl.hasOption(STYLESHEET_CLI)) {
                String[] style = cl.getOptionValues(STYLESHEET_CLI);
                if (style.length != 1) {
                    DefaultLog.INSTANCE.error("Please specify a single stylesheet");
                    System.exit(1);
                }

                URL url = Report.class.getClassLoader().getResource(String.format("org/apache/rat/%s.xsl", style[0]));
                IOSupplier<InputStream> ioSupplier = (url == null) ?
                        () -> Files.newInputStream(Paths.get(style[0])) :
                        url::openStream;

                configuration.setStyleSheet(ioSupplier);
            }
        }

        Defaults.Builder defaultBuilder = Defaults.builder();
        if (cl.hasOption(NO_DEFAULTS)) {
            defaultBuilder.noDefault();
        }
        if (cl.hasOption(LICENSES)) {
            for (String fn : cl.getOptionValues(LICENSES)) {
                defaultBuilder.add(fn);
            }
        }
        Defaults defaults = defaultBuilder.build(DefaultLog.INSTANCE);
        configuration.setFrom(defaults);
        if (baseDirectory != null) {
            configuration.setReportable(getDirectory(baseDirectory, configuration));
        }
        return configuration;
    }

    /**
     * Creates a filename filter from patterns to exclude.
     *
     * @param excludes the list of patterns to exclude.
     * @return the FilenameFilter tht excludes the patterns
     */
    static FilenameFilter parseExclusions(List<String> excludes) {
        final OrFileFilter orFilter = new OrFileFilter();
        int ignoredLines = 0;
        for (String exclude : excludes) {
            try {
                // skip comments
                if (exclude.startsWith("#") || StringUtils.isEmpty(exclude)) {
                    ignoredLines++;
                    continue;
                }

                String exclusion = exclude.trim();
                // interpret given patterns as regular expression, direct file names or
                // wildcards to give users more choices to configure exclusions
                orFilter.addFileFilter(new RegexFileFilter(exclusion));
                orFilter.addFileFilter(new NameFileFilter(exclusion));
                orFilter.addFileFilter(WildcardFileFilter.builder().setWildcards(exclusion).get());
            } catch (PatternSyntaxException e) {
                DefaultLog.INSTANCE.error("Will skip given exclusion '" + exclude + "' due to " + e);
            }
        }
        DefaultLog.INSTANCE.error("Ignored " + ignoredLines + " lines in your exclusion files as comments or empty lines.");
        return new NotFileFilter(orFilter);
    }

    static Options buildOptions() {
        return new Options()
                .addOption(ARCHIVE)
                .addOption(DRY_RUN)
                .addOption(LIST_FAMILIES)
                .addOption(LIST_LICENSES)
                .addOption(HELP)
                .addOption(OUT)
                .addOption(NO_DEFAULTS)
                .addOption(LICENSES)
                .addOption(SCAN_HIDDEN_DIRECTORIES)
                .addOptionGroup(ADD)
                .addOption(FORCE)
                .addOption(COPYRIGHT)
                .addOption(EXCLUDE_CLI)
                .addOption(EXCLUDE_FILE_CLI)
                .addOption(DIR)
                .addOption(LOG_LEVEL)
                .addOptionGroup(new OptionGroup()
                        .addOption(XML).addOption(STYLESHEET_CLI));
    }

    private static void printUsage(Options opts) {
        printUsage(new PrintWriter(System.out), opts);
    }

    protected static String createPadding(int len) {
        char[] padding = new char[len];
        Arrays.fill(padding, ' ');
        return new String(padding);
    }

   static String header(String txt) {
        return String.format("%n====== %s ======%n", WordUtils.capitalizeFully(txt));
   }
    static void printUsage(PrintWriter writer, Options opts) {

        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.setWidth(130);
        helpFormatter.setOptionComparator(new OptionComparator());
        String syntax = "java -jar apache-rat/target/apache-rat-CURRENT-VERSION.jar [options] [DIR|TARBALL]";
        helpFormatter.printHelp(writer, helpFormatter.getWidth(), syntax, header("Available options"), opts,
                helpFormatter.getLeftPadding(), helpFormatter.getDescPadding(),
                header("Argument Types"), false);

        String argumentPadding = createPadding(helpFormatter.getLeftPadding() + 5);
        for (Map.Entry<String, Supplier<String>> argInfo : ARGUMENT_TYPES.entrySet()) {
            writer.format("\n<%s>\n", argInfo.getKey());
            helpFormatter.printWrapped(writer, helpFormatter.getWidth(), helpFormatter.getLeftPadding() + 10,
                    argumentPadding + argInfo.getValue().get());
        }
        writer.println(header("Notes"));

        int idx = 1;
        for (String note : NOTES) {
            writer.format("%d. %s%n", idx++, note);
        }
        writer.flush();
    }

    private Report() {
        // do not instantiate
    }

    /**
     * Creates an IReportable object from the directory name and ReportConfiguration
     * object.
     *
     * @param baseDirectory the directory that contains the files to report on.
     * @param config        the ReportConfiguration.
     * @return the IReportable instance containing the files.
     */
    private static IReportable getDirectory(String baseDirectory, ReportConfiguration config) {
        File base = new File(baseDirectory);

        if (!base.exists()) {
            config.getLog().log(Level.ERROR, "Directory '" + baseDirectory + "' does not exist");
            return null;
        }

        if (base.isDirectory()) {
            return new DirectoryWalker(base, config.getFilesToIgnore(), config.getDirectoriesToIgnore());
        }

        return new ArchiveWalker(base, config.getFilesToIgnore());
    }

    /**
     * This class implements the {@code Comparator} interface for comparing Options.
     */
    public static class OptionComparator implements Comparator<Option>, Serializable {
        /**
         * The serial version UID.
         */
        private static final long serialVersionUID = 5305467873966684014L;

        private String getKey(Option opt) {
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
