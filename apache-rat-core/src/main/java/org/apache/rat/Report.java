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
import java.io.PrintStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.function.IOSupplier;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.config.AddLicenseHeaders;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.report.IReportable;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;
import org.apache.rat.utils.Log.Level;
import org.apache.rat.walker.ArchiveWalker;
import org.apache.rat.walker.DirectoryWalker;

/**
 * The CLI based configuration object for report generation.
 */
public class Report {
    /**
     * Adds license headers to files missing headers.
     */
    private static final String ADD = "A";
    private static final String ADD_OLD = "a";
    /**
     * Forces changes to be written to new files.
     */
    private static final String FORCE = "f";
    /**
     * Defines the copyright header to add to the file.
     */
    private static final String COPYRIGHT = "c";
    /**
     * Name of File to exclude from report consideration.
     */
    private static final String EXCLUDE_CLI = "e";
    /**
     * Name of file that contains a list of files to exclude from report
     * consideration.
     */
    private static final String EXCLUDE_FILE_CLI = "E";
    /**
     * The stylesheet to use to style the XML output.
     */
    private static final String STYLESHEET_CLI = "s";
    /**
     * Produce help
     */
    private static final String HELP = "h";
    /**
     * Flag to identify a file with license definitions.
     */
    private static final String LICENSES = "licenses";
    /**
     * Do not use the default files.
     */
    private static final String NO_DEFAULTS = "no-default-licenses";
    /**
     * Scan hidden directories.
     */
    private static final String SCAN_HIDDEN_DIRECTORIES = "scan-hidden-directories";
    /**
     * List the licenses that were used for the run.
     */
    private static final String LIST_LICENSES = "list-licenses";

    /**
     * List the all families for the run.
     */
    private static final String LIST_FAMILIES = "list-families";

    private static final String LOG_LEVEL = "log-level";
    private static final String DRY_RUN = "dry-run";
    /**
     * Set unstyled XML output
     */
    private static final String XML = "x";


    /**
     * Processes the command line and builds a configuration and executes the
     * report.
     * 
     * @param args the arguments.
     * @throws Exception on error.
     */
    public static void main(String[] args) throws Exception {
        Options opts = buildOptions();
        CommandLine cl;
        try {
            cl = new DefaultParser().parse(opts, args);
        } catch (ParseException e) {
            DefaultLog.INSTANCE.error(e.getMessage());
            DefaultLog.INSTANCE.error("Please use the \"--help\" option to see a list of valid commands and options");
            System.exit(1);
            return; // dummy return (won't be reached) to avoid Eclipse complaint about possible NPE
                    // for "cl"
        }

        if (cl.hasOption(LOG_LEVEL)) {
            try {
                Log.Level level = Log.Level.valueOf(cl.getOptionValue(LOG_LEVEL).toUpperCase());
                DefaultLog.INSTANCE.setLevel(level);
            } catch (IllegalArgumentException e) {
                DefaultLog.INSTANCE.warn(String.format("Invalid Log Level (%s) specified.", cl.getOptionValue(LOG_LEVEL)));
                DefaultLog.INSTANCE.warn(String.format("Log level set at: %s", DefaultLog.INSTANCE.getLevel()));
            }
        }
        if (cl.hasOption(HELP)) {
            printUsage(opts);
        }

        args = cl.getArgs();
        if (args == null || args.length != 1) {
            printUsage(opts);
        } else {
            ReportConfiguration configuration = createConfiguration(args[0], cl);
            configuration.validate(DefaultLog.INSTANCE::error);

            boolean dryRun = false;
            
            if (cl.hasOption(LIST_FAMILIES)) {
                LicenseFilter f = LicenseFilter.fromText(cl.getOptionValue(LIST_FAMILIES));
                if (f != LicenseFilter.none) {                        
                    dryRun = true;
                    Reporter.listLicenseFamilies(configuration, f);
                }
            }
            if (cl.hasOption(LIST_LICENSES)) {
                LicenseFilter f = LicenseFilter.fromText(cl.getOptionValue(LIST_LICENSES));
                if (f != LicenseFilter.none) {                        
                    dryRun = true;
                    Reporter.listLicenses(configuration, f);
                }
            }
            
            if (!dryRun) {
                new Reporter(configuration).output();
            }
        }
    }

    static ReportConfiguration createConfiguration(String baseDirectory, CommandLine cl) throws IOException {
        final ReportConfiguration configuration = new ReportConfiguration(DefaultLog.INSTANCE);

        configuration.setDryRun(cl.hasOption(DRY_RUN));
        if (cl.hasOption(LIST_FAMILIES)) {
           configuration.listFamilies( LicenseFilter.valueOf(cl.getOptionValue(LIST_FAMILIES).toLowerCase()));
        }
        
        if (cl.hasOption(LIST_LICENSES)) {
            configuration.listFamilies( LicenseFilter.valueOf(cl.getOptionValue(LIST_LICENSES).toLowerCase()));
        }
        
        if (cl.hasOption('o')) {
            configuration.setOut(new File(cl.getOptionValue('o')));
        }

        if (cl.hasOption(SCAN_HIDDEN_DIRECTORIES)) {
            configuration.setDirectoryFilter(null);
        }

        if (cl.hasOption('a') || cl.hasOption('A')) {
            configuration.setAddLicenseHeaders(cl.hasOption('f') ? AddLicenseHeaders.FORCED : AddLicenseHeaders.TRUE);
            configuration.setCopyrightMessage(cl.getOptionValue("c"));
        }

        if (cl.hasOption(EXCLUDE_CLI)) {
            String[] excludes = cl.getOptionValues(EXCLUDE_CLI);
            if (excludes != null) {
                final FilenameFilter filter = parseExclusions(Arrays.asList(excludes));
                configuration.setInputFileFilter(filter);
            }
        } else if (cl.hasOption(EXCLUDE_FILE_CLI)) {
            String excludeFileName = cl.getOptionValue(EXCLUDE_FILE_CLI);
            if (excludeFileName != null) {
                final FilenameFilter filter = parseExclusions(
                        FileUtils.readLines(new File(excludeFileName), StandardCharsets.UTF_8));
                configuration.setInputFileFilter(filter);
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
                IOSupplier<InputStream> ioSupplier = null;

                URL url = Report.class.getClassLoader().getResource(String.format("org/apache/rat/%s.xsl", style[0]));
                if (url == null) {
                    ioSupplier = () -> Files.newInputStream(Paths.get(style[0]));
                } else {
                    ioSupplier = () -> url.openStream();
                }
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
        Defaults defaults = defaultBuilder.build();
        configuration.setFrom(defaults);
        configuration.setReportable(getDirectory(baseDirectory, configuration));
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
        String licFilterValues = String.join(", ", 
                Arrays.stream(LicenseFilter.values()).map(LicenseFilter::name).collect(Collectors.toList()));

        Options opts = new Options()
        .addOption(Option.builder().longOpt(DRY_RUN)
                .desc("If set do not update the files but generate the reports.")
                .build())
        .addOption(
                Option.builder().hasArg(true).longOpt(LIST_FAMILIES)
                .desc("List the defined license families (default is none). Valid options are: "+licFilterValues+".")
                .build())
        .addOption(
                Option.builder().hasArg(true).longOpt(LIST_LICENSES)
                .desc("List the defined licenses (default is none). Valid options are: "+licFilterValues+".")
                .build())

        .addOption(new Option(HELP, "help", false, "Print help for the RAT command line interface and exit."));

        Option out = new Option("o", "out", true,
                "Define the output file where to write a report to (default is System.out).");
        opts.addOption(out);

        String defaultHandlingText = " By default all approved default licenses are used";
        Option noDefaults = new Option(null, NO_DEFAULTS, false, "Ignore default configuration." + defaultHandlingText);
        opts.addOption(noDefaults);

        opts.addOption(null, LICENSES, true, "File names or URLs for license definitions");
        opts.addOption(null, SCAN_HIDDEN_DIRECTORIES, false, "Scan hidden directories");

        OptionGroup addLicenseGroup = new OptionGroup();
        // RAT-85/RAT-203: Deprecated! added only for convenience and for backwards
        // compatibility
        Option addLicence = new Option(ADD_OLD, false, "(deprecated) Add the default license header to any file with an unknown license.  Use '-A' or ---addLicense instead.");
        addLicenseGroup.addOption(addLicence);
        Option addLicense = new Option(ADD, "addLicense", false, "Add the default license header to any file with an unknown license that is not in the exclusion list. "
                + "By default new files will be created with the license header, "
                + "to force the modification of existing files use the --force option.");
        addLicenseGroup.addOption(addLicense);
        opts.addOptionGroup(addLicenseGroup);

        Option write = new Option(FORCE, "force", false,
                "Forces any changes in files to be written directly to the source files (i.e. new files are not created).");
        opts.addOption(write);

        Option copyright = new Option(COPYRIGHT, "copyright", true,
                "The copyright message to use in the license headers, usually in the form of \"Copyright 2008 Foo\"");
        opts.addOption(copyright);

        final Option exclude = Option.builder(EXCLUDE_CLI).argName("expression").longOpt("exclude").hasArgs()
                .desc("Excludes files matching wildcard <expression>. "
                        + "Note that --dir is required when using this parameter. " + "Allows multiple arguments.")
                .build();
        opts.addOption(exclude);

        final Option excludeFile = Option.builder(EXCLUDE_FILE_CLI).argName("fileName").longOpt("exclude-file")
                .hasArgs().desc("Excludes files matching regular expression in <file> "
                        + "Note that --dir is required when using this parameter. ")
                .build();
        opts.addOption(excludeFile);

        Option dir = new Option("d", "dir", false, "Used to indicate source when using --exclude");
        opts.addOption(dir);

        opts.addOption( Option.builder().argName("level").longOpt(LOG_LEVEL)
                .hasArgs().desc("sets the log level.  Valid options are: DEBUG, INFO, WARN, ERROR, OFF")
                .build() );
        
        OptionGroup outputType = new OptionGroup();

        Option xml = new Option(XML, "xml", false, "Output the report in raw XML format.  Not compatible with -s");
        outputType.addOption(xml);

        Option xslt = new Option(STYLESHEET_CLI, "stylesheet", true,
                "XSLT stylesheet to use when creating the report.  Not compatible with -x.  Either an external xsl file may be specified or one of the internal named sheets: plain-rat (default), missing-headers, or unapproved-licenses");
        outputType.addOption(xslt);
        opts.addOptionGroup(outputType);
        
        

        return opts;
    }

    private static void printUsage(Options opts) {
        HelpFormatter f = new HelpFormatter();
        f.setOptionComparator(new OptionComparator());
        String header = "\nAvailable options";

        String footer = "\nNOTE:\n" + "Rat is really little more than a grep ATM\n"
                + "Rat is also rather memory hungry ATM\n" + "Rat is very basic ATM\n"
                + "Rat highlights possible issues\n" + "Rat reports require interpretation\n"
                + "Rat often requires some tuning before it runs well against a project\n"
                + "Rat relies on heuristics: it may miss issues\n";

        f.printHelp("java -jar apache-rat/target/apache-rat-CURRENT-VERSION.jar [options] [DIR|TARBALL]", header, opts,
                footer, false);
        System.exit(0);
    }

    private Report() {
        // do not instantiate
    }

    /**
     * Creates an IReportable object from the directory name and ReportConfiguration
     * object.
     * 
     * @param baseDirectory the directory that contains the files to report on.
     * @param config the ReportConfiguration.
     * @return the IReportable instance containing the files.
     */
    private static IReportable getDirectory(String baseDirectory, ReportConfiguration config) {
        try (PrintStream out = new PrintStream(config.getOutput().get())) {
            File base = new File(baseDirectory);
            
            if (!base.exists()) {
                config.getLog().log(Level.ERROR, "Directory '"+baseDirectory+"' does not exist");
                return null;
            }

            if (base.isDirectory()) {
                return new DirectoryWalker(base, config.getInputFileFilter(), config.getDirectoryFilter());
            }

            try {
                return new ArchiveWalker(base, config.getInputFileFilter());
            } catch (IOException ex) {
                config.getLog().log(Level.ERROR, "file '"+baseDirectory+"' is not valid gzip data.");
                return null;
            }
        } catch (IOException e) {
            throw new ConfigurationException("Error opening output", e);
        }
    }

    /**
     * This class implements the {@code Comparator} interface for comparing Options.
     */
    private static class OptionComparator implements Comparator<Option>, Serializable {
        /** The serial version UID. */
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
