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

import org.apache.commons.cli.AlreadySelectedException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DeprecatedAttributes;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.ConfigurationException;
import org.apache.rat.Defaults;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.config.AddLicenseHeaders;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.PatternSyntaxException;

import static java.lang.String.format;


public enum Arg {

    ///////////////////////// EDIT OPTIONS
    /**
     * Defines options to add copyright to files
     */
    EDIT_COPYRIGHT(new OptionGroup()
            .addOption(Option.builder("c")
                            .longOpt("copyright").hasArg()
                            .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                                    .setDescription("Use '--edit-copyright' instead.").get())
                            .build())
                    .addOption(Option.builder().longOpt("edit-copyright").hasArg()
                            .desc("The copyright message to use in the license headers, usually in the form of \"Copyright 2008 Foo\".  Only valid with --edit-license")
                            .build())),

    /**
     * Causes file updates to overwrite existing files.
     */
    EDIT_OVERWRITE(new OptionGroup()
            .addOption(Option.builder("f").longOpt("force")
                            .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                                    .setDescription("Use '--edit-overwrite' instead.").get())
                            .build())
                    .addOption(Option.builder().longOpt("edit-overwrite")
                            .desc("Forces any changes in files to be written directly to the source files (i.e. new files are not created).  "+
                            "Only valid with --edit-license")
                            .build())),

    /**
     * Defines options to add licenses to files
     */
    EDIT_ADD(new OptionGroup()
            .addOption(Option.builder("a")
                            .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                                    .setDescription("Use '--edit-license' instead.").get())
                            .build())
                    .addOption(Option.builder("A").longOpt("addLicense")
                            .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                                    .setDescription("Use '--edit-license' instead.").get())
                            .build())
                    .addOption(Option.builder().longOpt("edit-license").desc(
                            "Add the default license header to any file with an unknown license that is not in the exclusion list. "
                                    +"By default new files will be created with the license header, "
                                    +"to force the modification of existing files use the --edit-overwrite option.").build()
                    )),

    //////////////////////////// CONFIGURATION OPTIONS


    /** group of options that read a configuraiton file */
    CONFIGURATION(new OptionGroup()
            .addOption(Option.builder().longOpt("config").hasArgs().argName("File")
            .desc("File names for system configuration.  May be followed by multiple arguments. "
                          + "Note that '--' or a following option is required when using this parameter.")
            .build())
            .addOption(Option.builder().longOpt("licenses").hasArgs().argName("File")
            .desc("File names for system configuration.  May be followed by multiple arguments. "
                          + "Note that '--' or a following option is required when using this parameter.")
            .deprecated(DeprecatedAttributes.builder().setSince("0.17.0").setForRemoval(true).setDescription("Use --config").get())
            .build())),

    /** group of options that skip the default configuration file */
    CONFIGURATION_NO_DEFAULTS(new OptionGroup()
            .addOption(Option.builder().longOpt("configuration-no-defaults")
                    .desc("Ignore default configuration.").build())
            .addOption(Option.builder().longOpt("no-default-licenses")
                    .deprecated(DeprecatedAttributes.builder().setSince("0.17.0").setForRemoval(true).setDescription("Use --configuration-no-defaults").get())
                    .build())),

    /** Option that add approved licenses to the list */
    LICENSES_APPROVED(new OptionGroup().addOption(Option.builder().longOpt("licenses-approved").hasArgs().argName("LicenseID")
            .desc("The approved License IDs.  These licenses will be added to the list of approved licenses. " +
                    "May be followed by multiple arguments. Note that '--' or a following option is required when using this parameter.")
            .build())),

    /** Option that add approved licenses from a file */
    LICENSES_APPROVED_FILE(new OptionGroup().addOption(Option.builder().longOpt("licenses-approved-file").hasArg().argName("File")
            .desc("Name of file containing the approved license IDs.")
            .type(File.class)
            .build())),

    /** Option that specifies approved license families */
    FAMILIES_APPROVED(new OptionGroup().addOption(Option.builder().longOpt("license-families-approved").hasArgs().argName("FamilyID")
            .desc("The approved License Family IDs.  These licenses families will be added to the list of approved licenses families " +
                    "May be followed by multiple arguments. Note that '--' or a following option is required when using this parameter.")
            .build())),

    /** Option that specifies approved license families from a file */
    FAMILIES_APPROVED_FILE(new OptionGroup().addOption(Option.builder().longOpt("license-families-approved-file").hasArg().argName("File")
            .desc("Name of file containing the approved family IDs.")
            .type(File.class)
            .build())),

    /** Option to remove licenses from the approved list */
    LICENSES_DENIED(new OptionGroup().addOption(Option.builder().longOpt("licenses-denied").hasArgs().argName("LicenseID")
            .desc("The approved License IDs.  These licenses will be added to the list of approved licenses. " +
                    "May be followed by multiple arguments. Note that '--' or a following option is required when using this parameter.")
            .build())),

    /** Option to read a file licenses to be removed from the approved list */
    LICENSES_DENIED_FILE(new OptionGroup().addOption(Option.builder().longOpt("licenses-denied-file").hasArg().argName("File")
            .desc("Name of File containing the approved license IDs.")
            .type(File.class)
            .build())),

    /** Option to list license families to remove from the approved list */
    FAMILIES_DENIED(new OptionGroup().addOption(Option.builder().longOpt("license-families-denied").hasArgs().argName("FamilyID")
            .desc("The denied License family IDs.  These license families will be removed from the list of approved licenses. " +
                    "May be followed by multiple arguments. Note that '--' or a following option is required when using this parameter.")
            .build())),

    /** Option to read a list of license families to remove from the approved list */
    FAMILIES_DENIED_FILE(new OptionGroup().addOption(Option.builder().longOpt("license-families-denied-file").hasArg().argName("File")
            .desc("Name of file containing the denied license IDs.")
            .type(File.class)
            .build())),

////////////////// INPUT OPTIONS

    /** Excludes files by expression */
    EXCLUDE(new OptionGroup()
            .addOption(Option.builder("e").longOpt("exclude").hasArgs().argName("Expression")
                    .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                            .setDescription("Use '--input-exclude' instead.").get())
            .build())
            .addOption(Option.builder().longOpt("input-exclude").hasArgs().argName("Expression")
                    .desc("Excludes files matching wildcard <Expression>. May be followed by multiple arguments. "
                                  + "Note that '--' or a following option is required when using this parameter.")
                    .build())),

    /** Excludes files based on content of file */
    EXCLUDE_FILE(new OptionGroup()
            .addOption(Option.builder("E").longOpt("exclude-file")
                    .argName("File")
                    .hasArg()
                    .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                            .setDescription("Use '--input-exclude-file' instead.").get())
                    .build())
            .addOption(Option.builder().longOpt("input-exclude-file")
                    .argName("File")
                    .hasArg().desc("Excludes files matching regular expression in the input file.")
                    .build())),

    /** Scan hidden directories */
    SCAN_HIDDEN_DIRECTORIES(new OptionGroup().addOption(new Option(null, "scan-hidden-directories", false,
            "Scan hidden directories"))),

    /** Stop processing an input stream and declare an input file */
    DIR(new OptionGroup().addOption(Option.builder().option("d").longOpt("dir").hasArg()
            .desc("Used to indicate end of list when using --exclude.").argName("DirOrArchive")
            .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                    .setDescription("Use '--'").get()).build())),

    /////////////// OUTPUT OPTIONS


    /** Defines the stylesheet to use */
    OUTPUT_STYLE(new OptionGroup()
            .addOption(Option.builder().longOpt("output-style").hasArg().argName("StyleSheet")
                    .desc("XSLT stylesheet to use when creating the report. "
                            + "Either an external xsl file may be specified or one of the internal named sheets.")
                    .build())
            .addOption(Option.builder("s").longOpt("stylesheet").hasArg().argName("StyleSheet")
                    .deprecated(DeprecatedAttributes.builder().setSince("0.17.0").setForRemoval(true).setDescription("Use --output-style").get())
                    .build())
            .addOption(Option.builder("x").longOpt("xml")
                    .deprecated(DeprecatedAttributes.builder().setSince("0.17.0").setForRemoval(true).setDescription("Use --output-style xml").get())
                    .build())),

    /** Specifies that  license definitions that should be included in the output */
    OUTPUT_LICENSES(new OptionGroup()
            .addOption(Option.builder().longOpt("output-licenses").hasArg().argName("LicenseFilter")
                    .desc("List the defined licenses (default is NONE).")
                    .converter(s -> LicenseSetFactory.LicenseFilter.valueOf(s.toUpperCase()))
                    .build())
            .addOption(Option.builder().longOpt("list-licenses").hasArg().argName("LicenseFilter")
                    .desc("List the defined licenses (default is NONE).")
                    .converter(s -> LicenseSetFactory.LicenseFilter.valueOf(s.toUpperCase()))
                    .deprecated(DeprecatedAttributes.builder().setSince("0.17.0").setForRemoval(true).setDescription("Use --output-licenses").get())
                    .build())),

    /** Specifies that the license families that should be included in the output */
    OUTPUT_FAMILIES(new OptionGroup()
            .addOption(Option.builder().longOpt("output-families").hasArg().argName("LicenseFilter")
                    .desc("List the defined license families (default is NONE).")
                    .converter(s -> LicenseSetFactory.LicenseFilter.valueOf(s.toUpperCase()))
                    .build())
            .addOption(Option.builder().longOpt("list-families").hasArg().argName("LicenseFilter")
                    .desc("List the defined license families (default is NONE).")
                    .converter(s -> LicenseSetFactory.LicenseFilter.valueOf(s.toUpperCase()))
                    .deprecated(DeprecatedAttributes.builder().setSince("0.17.0").setForRemoval(true).setDescription("Use --output-families").get())
                    .build())),

    /** Specifies the log level to log messages at. */
    LOG_LEVEL(new OptionGroup().addOption(Option.builder().longOpt("log-level")
            .hasArg().argName("LogLevel")
            .desc("sets the log level.")
            .converter(s -> Log.Level.valueOf(s.toUpperCase()))
            .build())),

    /** Specifies that the run should not perform any updates to files.  */
    DRY_RUN(new OptionGroup().addOption(Option.builder().longOpt("dry-run")
            .desc("If set do not update the files but generate the reports.")
            .build())),


    /** Specifies where the output should be written.  */
    OUTPUT_FILE(new OptionGroup()
            .addOption(Option.builder().option("o").longOpt("out").hasArg().argName("File")
                    .desc("Define the output file where to write a report to (default is System.out).")
                    .deprecated(DeprecatedAttributes.builder().setSince("0.17.0").setForRemoval(true).setDescription("Use --output-file").get())
                    .type(File.class).build())
            .addOption(Option.builder().longOpt("output-file").hasArg().argName("File")
                    .desc("Define the output file where to write a report to (default is System.out).")
                    .type(File.class).build())),

    /** Specifies the level of reporting detail for archive files. */
    OUTPUT_ARCHIVE(new OptionGroup()
            .addOption(Option.builder().longOpt("output-archive").hasArg().argName("ProcessingType")
                    .desc(format("Specifies the level of detail in ARCHIVE file reporting. (default is %s)",
                            Defaults.ARCHIVE_PROCESSING))
                    .converter(s -> ReportConfiguration.Processing.valueOf(s.toUpperCase()))
                    .build())),

    /** Specifies the level or reporting detail for standard files. */
    OUTPUT_STANDARD(new OptionGroup()
            .addOption(Option.builder().longOpt("output-standard").hasArg().argName("ProcessingType")
                    .desc(format("Specifies the level of detail in STANDARD file reporting. (default is %s)",
                            Defaults.STANDARD_PROCESSING))
                    .converter(s -> ReportConfiguration.Processing.valueOf(s.toUpperCase()))
                    .build()));


    private final OptionGroup group;
    Arg(OptionGroup group) {
        this.group = group;
    }

    public boolean isSelected() {
        return group.getSelected() != null;
    }

    public Option getSelected() {
        String s = group.getSelected();
        if (s != null) {
            for (Option result : group.getOptions()) {
                if (result.getKey().equals(s)) {
                    return result;
                }
            }
        }
        return null;
    }

    public Option find(String key) {
        for (Option result : group.getOptions()) {
            if (key.equals(result.getKey()) || key.equals(result.getLongOpt())) {
                return result;
            }
        }
        throw new IllegalArgumentException("Can not find "+key);
    }

    public OptionGroup group() {
        return group;
    }

    public Option option() {
        for (Option result : group.getOptions()) {
            if (!result.isDeprecated()) {
                return result;
            }
        }
        return null;
    }

    /**
     * Gets the full set of options.
     */
    public static Options getOptions() {
        Options options = new Options();
        for (Arg arg: Arg.values()) {
            options.addOptionGroup(arg.group);
        }
        return options;
    }

    public static Option findOption(String key) {
        for (Arg arg: Arg.values()) {
            Option result = arg.find(key);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Processes the edit arguments.
     * @param ctxt the context to work with.
     */
    private static void processEditArgs(final ArgumentContext ctxt) {
        if (EDIT_ADD.isSelected()) {
            ctxt.getCommandLine().hasOption(Arg.EDIT_ADD.getSelected());
            boolean force = EDIT_OVERWRITE.isSelected();
            if (force) {
                ctxt.getCommandLine().hasOption(EDIT_OVERWRITE.getSelected());
            }
            ctxt.getConfiguration().setAddLicenseHeaders(force ? AddLicenseHeaders.FORCED : AddLicenseHeaders.TRUE);
            if (EDIT_COPYRIGHT.isSelected()) {
                ctxt.getConfiguration().setCopyrightMessage(ctxt.getCommandLine().getOptionValue(EDIT_COPYRIGHT.getSelected()));
            }
        }
    }

    /**
     * Processes the configuration options.
     * @param ctxt the context to process.
     * @throws MalformedURLException if configuration files can not be read.
     */
    private static void processConfigurationArgs(final ArgumentContext ctxt) throws MalformedURLException {
        Defaults.Builder defaultBuilder = Defaults.builder();
        if (CONFIGURATION.isSelected()) {
            for (String fn : ctxt.getCommandLine().getOptionValues(CONFIGURATION.getSelected())) {
                defaultBuilder.add(fn);
            }
        }
        if (CONFIGURATION_NO_DEFAULTS.isSelected()) {
            // display deprecation log if needed.
            ctxt.getCommandLine().hasOption(CONFIGURATION.getSelected());
            defaultBuilder.noDefault();
        }
        ctxt.getConfiguration().setFrom(defaultBuilder.build(ctxt.getLog()));

        if (FAMILIES_APPROVED.isSelected()) {
            for (String cat : ctxt.getCommandLine().getOptionValues(FAMILIES_APPROVED.getSelected())) {
                ctxt.getConfiguration().addApprovedLicenseCategory(cat);
            }
        }
        if (FAMILIES_APPROVED_FILE.isSelected()) {
            try {
                File f = ctxt.getCommandLine().getParsedOptionValue(FAMILIES_APPROVED_FILE.getSelected());
                try (InputStream in = new FileInputStream(f)) {
                    ctxt.getConfiguration().addApprovedLicenseCategories(IOUtils.readLines(in, StandardCharsets.UTF_8));
                }
            } catch (IOException | ParseException e) {
                throw new ConfigurationException(e);
            }
        }
        if (FAMILIES_DENIED.isSelected()) {
            for (String cat : ctxt.getCommandLine().getOptionValues(FAMILIES_DENIED.getSelected())) {
                ctxt.getConfiguration().removeApprovedLicenseCategory(cat);
            }
        }
        if (FAMILIES_DENIED_FILE.isSelected()) {
            try {
                File f = ctxt.getCommandLine().getParsedOptionValue(FAMILIES_DENIED_FILE.getSelected());
                try (InputStream in = new FileInputStream(f)) {
                    ctxt.getConfiguration().removeApprovedLicenseCategories(IOUtils.readLines(in, StandardCharsets.UTF_8));
                }
            } catch (IOException | ParseException e) {
                throw new ConfigurationException(e);
            }
        }

        if (LICENSES_APPROVED.isSelected()) {
            for (String id : ctxt.getCommandLine().getOptionValues(LICENSES_APPROVED.getSelected())) {
                ctxt.getConfiguration().addApprovedLicenseId(id);
            }
        }
        if (LICENSES_APPROVED_FILE.isSelected()) {
            try {
                File f = ctxt.getCommandLine().getParsedOptionValue(LICENSES_APPROVED_FILE.getSelected());
                try (InputStream in = new FileInputStream(f)) {
                    ctxt.getConfiguration().addApprovedLicenseIds(IOUtils.readLines(in, StandardCharsets.UTF_8));
                }
            } catch (IOException | ParseException e) {
                throw new ConfigurationException(e);
            }
        }
        if (LICENSES_DENIED.isSelected()) {
            for (String id : ctxt.getCommandLine().getOptionValues(LICENSES_DENIED.getSelected())) {
                ctxt.getConfiguration().removeApprovedLicenseId(id);
            }
        }
        if (LICENSES_DENIED_FILE.isSelected()) {
            try {
                File f = ctxt.getCommandLine().getParsedOptionValue(LICENSES_DENIED_FILE.getSelected());
                try (InputStream in = new FileInputStream(f)) {
                    ctxt.getConfiguration().removeApprovedLicenseIds(IOUtils.readLines(in, StandardCharsets.UTF_8));
                }
            } catch (IOException | ParseException e) {
                throw new ConfigurationException(e);
            }
        }
    }

    /**
     * Creates a filename filter from patterns to exclude.
     * Package provide for use in testing.
     * @param log the Logger to use.
     * @param excludes the list of patterns to exclude.
     * @return the FilenameFilter tht excludes the patterns or an empty optional.
     */
    static Optional<IOFileFilter> parseExclusions(final Log log, final List<String> excludes) {
        final OrFileFilter orFilter = new OrFileFilter();
        int ignoredLines = 0;
        for (String exclude : excludes) {

            // skip comments
            if (exclude.startsWith("#") || StringUtils.isEmpty(exclude)) {
                ignoredLines++;
                continue;
            }

            String exclusion = exclude.trim();
            // interpret given patterns as regular expression, direct file names or
            // wildcards to give users more choices to configure exclusions
            try {
                orFilter.addFileFilter(new RegexFileFilter(exclusion));
            } catch (PatternSyntaxException e) {
                // report nothing, an acceptable outcome.
            }
            orFilter.addFileFilter(new NameFileFilter(exclusion));
            if (exclude.contains("?") || exclude.contains("*")) {
                orFilter.addFileFilter(WildcardFileFilter.builder().setWildcards(exclusion).get());
            }
        }
        if (ignoredLines > 0) {
            log.info("Ignored " + ignoredLines + " lines in your exclusion files as comments or empty lines.");
        }
        return orFilter.getFileFilters().isEmpty() ? Optional.empty() : Optional.of(orFilter);
    }

    /**
     * Process the input setup.
     * @param ctxt the context to work in.
     * @throws IOException if an exclude file can not be read.
     */
    private static void processInputArgs(final ArgumentContext ctxt) throws IOException {
        if (SCAN_HIDDEN_DIRECTORIES.isSelected()) {
            ctxt.getConfiguration().setDirectoriesToIgnore(FalseFileFilter.FALSE);
        }

        // TODO when include/exclude processing is updated check calling methods to ensure that all specified
        // directories are handled in the list of directories.
        if (EXCLUDE.isSelected()) {
            String[] excludes = ctxt.getCommandLine().getOptionValues(EXCLUDE.getSelected());
            if (excludes != null) {
                parseExclusions(ctxt.getConfiguration().getLog(), Arrays.asList(excludes)).ifPresent(ctxt.getConfiguration()::setFilesToIgnore);
            }
        }
        if (EXCLUDE_FILE.isSelected()) {
            String excludeFileName = ctxt.getCommandLine().getOptionValue(EXCLUDE_FILE.getSelected());
            if (excludeFileName != null) {
                parseExclusions(ctxt.getConfiguration().getLog(), FileUtils.readLines(new File(excludeFileName), StandardCharsets.UTF_8))
                        .ifPresent(ctxt.getConfiguration()::setFilesToIgnore);
            }
        }
    }

    /**
     * Logs a ParseException as a warning.
     * @param log the Log to write to
     * @param exception the parse exception to log
     * @param opt the option being processed
     * @param cl the command line being processed
     * @param dflt The default value the option is being set to.
     */
    private static void logParseException(final Log log, final ParseException exception, final Option opt, final CommandLine cl, final Object dflt) {
        log.warn(format("Invalid %s specified: %s ", opt.getOpt(), cl.getOptionValue(opt)));
        log.warn(format("%s set to: %s", opt.getOpt(), dflt));
        log.debug(exception);
    }

    /**
     * Process the log level setting.
     * @param commandLine The command line to process.
     * @param log The log to set.
     */
    public static void processLogLevel(final CommandLine commandLine, final Log log) {
        if (LOG_LEVEL.getSelected() != null) {
            if (log instanceof DefaultLog) {
                DefaultLog dLog = (DefaultLog) log;
                try {
                    dLog.setLevel(commandLine.getParsedOptionValue(LOG_LEVEL.getSelected()));
                } catch (ParseException e) {
                    logParseException(log, e, LOG_LEVEL.getSelected(), commandLine, dLog.getLevel());
                }
            } else {
                log.error("log was not a DefaultLog instance. LogLevel not set.");
            }
        }
    }

    public static void processArgs(final ArgumentContext ctxt) throws IOException {
        processOutputArgs(ctxt);
        processEditArgs(ctxt);
        processInputArgs(ctxt);
        processConfigurationArgs(ctxt);
    }
    /**
     * Process the arguments that can be processed together.
     * @param ctxt the context in which to process the args.
     */
    private static void processOutputArgs(final ArgumentContext ctxt) {
        ctxt.getConfiguration().setDryRun(DRY_RUN.isSelected());

        if (OUTPUT_FAMILIES.isSelected()) {
            try {
                ctxt.getConfiguration().listFamilies(ctxt.getCommandLine().getParsedOptionValue(OUTPUT_FAMILIES.getSelected()));
            } catch (ParseException e) {
                ctxt.logParseException(e, OUTPUT_FAMILIES.getSelected(), Defaults.LIST_FAMILIES);
            }
        }

        if (OUTPUT_LICENSES.isSelected()) {
            try {
                ctxt.getConfiguration().listLicenses(ctxt.getCommandLine().getParsedOptionValue(OUTPUT_LICENSES.getSelected()));
            } catch (ParseException e) {
                ctxt.logParseException(e, OUTPUT_LICENSES.getSelected(), Defaults.LIST_LICENSES);
            }
        }

        if (OUTPUT_ARCHIVE.isSelected()) {
            try {
                ctxt.getConfiguration().setArchiveProcessing(ctxt.getCommandLine().getParsedOptionValue(OUTPUT_ARCHIVE.getSelected()));
            } catch (ParseException e) {
                ctxt.logParseException(e, OUTPUT_ARCHIVE.getSelected(), Defaults.ARCHIVE_PROCESSING);
            }
        }

        if (OUTPUT_STANDARD.isSelected()) {
            try {
                ctxt.getConfiguration().setStandardProcessing(ctxt.getCommandLine().getParsedOptionValue(OUTPUT_STANDARD.getSelected()));
            } catch (ParseException e) {
                ctxt.logParseException(e, OUTPUT_STANDARD.getSelected(), Defaults.STANDARD_PROCESSING);
            }
        }

        if (OUTPUT_FILE.isSelected()) {
            try {
                File f = ctxt.getCommandLine().getParsedOptionValue(OUTPUT_FILE.getSelected());
                if (f.getParentFile().mkdirs() && !f.isDirectory()) {
                    ctxt.getLog().error("Could not create report parent directory " + f);
                }
                ctxt.getConfiguration().setOut(f);
            } catch (ParseException e) {
                ctxt.logParseException(e, OUTPUT_FILE.getSelected(), "System.out");
            }
        }

        if (OUTPUT_STYLE.isSelected()) {
            String selected = OUTPUT_STYLE.getSelected().getKey();
            if (selected.equals("x")) {
                // display deprecated message.
                ctxt.getCommandLine().hasOption("x");
                ctxt.getConfiguration().setStyleSheet(StyleSheets.getStyleSheet("xml"));
            } else {
                String[] style = ctxt.getCommandLine().getOptionValues(OUTPUT_STYLE.getSelected());
                if (style.length != 1) {
                    ctxt.getLog().error("Please specify a single stylesheet");
                    throw new ConfigurationException("Please specify a single stylesheet");
                }
                ctxt.getConfiguration().setStyleSheet(StyleSheets.getStyleSheet(style[0]));
            }
        }
    }

    public static void reset() {
        for (Arg a : Arg.values()) {
            try {
                a.group.setSelected(null);
            } catch (AlreadySelectedException e) {
                throw new RuntimeException("Should not happen", e);
            }
        }
    }
}
