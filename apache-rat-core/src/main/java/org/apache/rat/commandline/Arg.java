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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.PatternSyntaxException;

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

/**
 * An enumeration of options.
 * Each Arg contains an OptionGroup that contains the individual options that all resolve to the same option.  This allows
 * us to deprecate options as we move forward in development.
 */
public enum Arg {

    ///////////////////////// EDIT OPTIONS
    /**
     * Defines options to add copyright to files
     */
    EDIT_COPYRIGHT(new OptionGroup()
            .addOption(Option.builder("c")
                            .longOpt("copyright").hasArg()
                            .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                                    .setDescription(StdMsgs.useMsg("--edit-copyright")).get())
                            .desc("The copyright message to use in the license headers.")
                            .build())
                    .addOption(Option.builder().longOpt("edit-copyright").hasArg()
                            .desc("The copyright message to use in the license headers. Usually in the form of \"Copyright 2008 Foo\".  "
                                    + "Only valid with --edit-license")
                            .build())),

    /**
     * Causes file updates to overwrite existing files.
     */
    EDIT_OVERWRITE(new OptionGroup()
            .addOption(Option.builder("f").longOpt("force")
                            .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                                    .setDescription(StdMsgs.useMsg("--edit-overwrite")).get())
                    .desc("Forces any changes in files to be written directly to the source files (i.e. new files are not created).")
                            .build())
                    .addOption(Option.builder().longOpt("edit-overwrite")
                            .desc("Forces any changes in files to be written directly to the source files (i.e. new files are not created). "
                                    + "Only valid with --edit-license")
                            .build())),

    /**
     * Defines options to add licenses to files
     */
    EDIT_ADD(new OptionGroup()
            .addOption(Option.builder("a")
                            .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                                    .setDescription(StdMsgs.useMsg("--edit-license")).get())
                            .build())
                    .addOption(Option.builder("A").longOpt("addLicense")
                            .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                                    .setDescription(StdMsgs.useMsg("--edit-license")).get())
                            .desc("Add the default license header to any file with an unknown license that is not in the exclusion list.")
                            .build())
                    .addOption(Option.builder().longOpt("edit-license").desc(
                            "Add the default license header to any file with an unknown license that is not in the exclusion list. "
                                    + "By default new files will be created with the license header, "
                                    + "to force the modification of existing files use the --edit-overwrite option.").build()
                    )),

    //////////////////////////// CONFIGURATION OPTIONS


    /** Group of options that read a configuration file */
    CONFIGURATION(new OptionGroup()
            .addOption(Option.builder().longOpt("config").hasArgs().argName("File")
            .desc("File names for system configuration.")
            .build())
            .addOption(Option.builder().longOpt("licenses").hasArgs().argName("File")
            .desc("File names for system configuration.")
            .deprecated(DeprecatedAttributes.builder().setSince("0.17.0").setForRemoval(true).setDescription(StdMsgs.useMsg("--config")).get())
            .build())),

    /** Group of options that skip the default configuration file */
    CONFIGURATION_NO_DEFAULTS(new OptionGroup()
            .addOption(Option.builder().longOpt("configuration-no-defaults")
                    .desc("Ignore default configuration.").build())
            .addOption(Option.builder().longOpt("no-default-licenses")
                    .deprecated(DeprecatedAttributes.builder().setSince("0.17").setForRemoval(true).setDescription(StdMsgs.useMsg("--configuration-no-defaults")).get())
                    .desc("Ignore default configuration.")
                    .build())),

    /** Option that adds approved licenses to the list */
    LICENSES_APPROVED(new OptionGroup().addOption(Option.builder().longOpt("licenses-approved").hasArgs().argName("LicenseID")
            .desc("The approved License IDs.  These licenses will be added to the list of approved licenses.")
            .build())),

    /** Option that adds approved licenses from a file */
    LICENSES_APPROVED_FILE(new OptionGroup().addOption(Option.builder().longOpt("licenses-approved-file").hasArg().argName("File")
            .desc("Name of file containing the approved license IDs.")
            .type(File.class)
            .build())),

    /** Option that specifies approved license families */
    FAMILIES_APPROVED(new OptionGroup().addOption(Option.builder().longOpt("license-families-approved").hasArgs().argName("FamilyID")
            .desc("The approved License Family IDs.  These licenses families will be added to the list of approved licenses families.")
            .build())),

    /** Option that specifies approved license families from a file */
    FAMILIES_APPROVED_FILE(new OptionGroup().addOption(Option.builder().longOpt("license-families-approved-file").hasArg().argName("File")
            .desc("Name of file containing the approved family IDs.")
            .type(File.class)
            .build())),

    /** Option to remove licenses from the approved list */
    LICENSES_DENIED(new OptionGroup().addOption(Option.builder().longOpt("licenses-denied").hasArgs().argName("LicenseID")
            .desc("The denied License IDs.  These licenses will be removed to the list of approved licenses. " +
                    "Once licenses are removed they can not be added back.")
            .build())),

    /** Option to read a file licenses to be removed from the approved list */
    LICENSES_DENIED_FILE(new OptionGroup().addOption(Option.builder().longOpt("licenses-denied-file").hasArg().argName("File")
            .desc("Name of File containing the approved license IDs.")
            .type(File.class)
            .build())),

    /** Option to list license families to remove from the approved list */
    FAMILIES_DENIED(new OptionGroup().addOption(Option.builder().longOpt("license-families-denied").hasArgs().argName("FamilyID")
            .desc("The denied License family IDs.  These license families will be removed from the list of approved licenses.")
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
                            .setDescription(StdMsgs.useMsg("--input-exclude")).get())
                    .desc("Excludes files matching wildcard <Expression>.")
            .build())
            .addOption(Option.builder().longOpt("input-exclude").hasArgs().argName("Expression")
                    .desc("Excludes files matching wildcard <Expression>.")
                    .build())),

    /** Excludes files based on content of file */
    EXCLUDE_FILE(new OptionGroup()
            .addOption(Option.builder("E").longOpt("exclude-file")
                    .argName("File")
                    .hasArg()
                    .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                            .setDescription(StdMsgs.useMsg("--input-exclude-file")).get())
                    .desc("Excludes files matching regular expression in the input file.")
                    .build())
            .addOption(Option.builder().longOpt("input-exclude-file")
                    .argName("File")
                    .hasArg().desc("Excludes files matching regular expression in the input file.")
                    .build())),

    /** Scan hidden directories */
    SCAN_HIDDEN_DIRECTORIES(new OptionGroup().addOption(new Option(null, "scan-hidden-directories", false,
            "Scans hidden directories."))),

    /** Stop processing an input stream and declare an input file */
    DIR(new OptionGroup().addOption(Option.builder().option("d").longOpt("dir").hasArg()
            .desc("Used to indicate end of list when using options that take multiple arguments.").argName("DirOrArchive")
            .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                    .setDescription("Use the standard '--' to signal the end of arguments.").get()).build())),

    /////////////// OUTPUT OPTIONS


    /** Defines the stylesheet to use */
    OUTPUT_STYLE(new OptionGroup()
            .addOption(Option.builder().longOpt("output-style").hasArg().argName("StyleSheet")
                    .desc("XSLT stylesheet to use when creating the report. "
                            + "Either an external xsl file may be specified or one of the internal named sheets.")
                    .build())
            .addOption(Option.builder("s").longOpt("stylesheet").hasArg().argName("StyleSheet")
                    .deprecated(DeprecatedAttributes.builder().setSince("0.17").setForRemoval(true).setDescription(StdMsgs.useMsg("--output-style")).get())
                    .desc("XSLT stylesheet to use when creating the report.")
                    .build())
            .addOption(Option.builder("x").longOpt("xml")
                    .deprecated(DeprecatedAttributes.builder().setSince("0.17").setForRemoval(true).setDescription(StdMsgs.useMsg("--output-style with the 'xml' argument")).get())
                    .desc("forces XML output rather than the report.")
                    .build())),

    /** Specifies the license definitions that should be included in the output */
    OUTPUT_LICENSES(new OptionGroup()
            .addOption(Option.builder().longOpt("output-licenses").hasArg().argName("LicenseFilter")
                    .desc("List the defined licenses.")
                    .converter(s -> LicenseSetFactory.LicenseFilter.valueOf(s.toUpperCase()))
                    .build())
            .addOption(Option.builder().longOpt("list-licenses").hasArg().argName("LicenseFilter")
                    .desc("List the defined licenses.")
                    .converter(s -> LicenseSetFactory.LicenseFilter.valueOf(s.toUpperCase()))
                    .deprecated(DeprecatedAttributes.builder().setSince("0.17").setForRemoval(true).setDescription(StdMsgs.useMsg("--output-licenses")).get())
                    .build())),

    /** Specifies the license families that should be included in the output */
    OUTPUT_FAMILIES(new OptionGroup()
            .addOption(Option.builder().longOpt("output-families").hasArg().argName("LicenseFilter")
                    .desc("List the defined license families.")
                    .converter(s -> LicenseSetFactory.LicenseFilter.valueOf(s.toUpperCase()))
                    .build())
            .addOption(Option.builder().longOpt("list-families").hasArg().argName("LicenseFilter")
                    .desc("List the defined license families.")
                    .converter(s -> LicenseSetFactory.LicenseFilter.valueOf(s.toUpperCase()))
                    .deprecated(DeprecatedAttributes.builder().setSince("0.17").setForRemoval(true).setDescription(StdMsgs.useMsg("--output-families")).get())
                    .build())),

    /** Specifies the log level to log messages at. */
    LOG_LEVEL(new OptionGroup().addOption(Option.builder().longOpt("log-level")
            .hasArg().argName("LogLevel")
            .desc("sets the log level.")
            .converter(s -> Log.Level.valueOf(s.toUpperCase()))
            .build())),

    /** Specifies that the run should not perform any updates to files. */
    DRY_RUN(new OptionGroup().addOption(Option.builder().longOpt("dry-run")
            .desc("If set do not update the files but generate the reports.")
            .build())),


    /** Specifies where the output should be written. */
    OUTPUT_FILE(new OptionGroup()
            .addOption(Option.builder().option("o").longOpt("out").hasArg().argName("File")
                    .desc("Define the output file where to write a report to.")
                    .deprecated(DeprecatedAttributes.builder().setSince("0.17.0").setForRemoval(true).setDescription(StdMsgs.useMsg("--output-file")).get())
                    .type(File.class).build())
            .addOption(Option.builder().longOpt("output-file").hasArg().argName("File")
                    .desc("Define the output file where to write a report to.")
                    .type(File.class).build())),

    /** Specifies the level of reporting detail for archive files. */
    OUTPUT_ARCHIVE(new OptionGroup()
            .addOption(Option.builder().longOpt("output-archive").hasArg().argName("ProcessingType")
                    .desc("Specifies the level of detail in ARCHIVE file reporting.")
                    .converter(s -> ReportConfiguration.Processing.valueOf(s.toUpperCase()))
                    .build())),

    /** Specifies the level of reporting detail for standard files. */
    OUTPUT_STANDARD(new OptionGroup()
            .addOption(Option.builder().longOpt("output-standard").hasArg().argName("ProcessingType")
                    .desc("Specifies the level of detail in STANDARD file reporting.")
                    .converter(s -> ReportConfiguration.Processing.valueOf(s.toUpperCase()))
                    .build())),

    /** Provide license definition listing */
    HELP_LICENSES(new OptionGroup()
    .addOption(Option.builder().longOpt("help-licenses").desc("Print information about registered licenses.").build()));

    /** The option group for the argument */
    private final OptionGroup group;

    /**
     * Creates an Arg from an Option group.
     * @param group The option group.
     */
    Arg(final OptionGroup group) {
        this.group = group;
    }

    /**
     * Determines if the group has a selected element.
     * @return {@code true} if the group has a selected element.
     */
    public boolean isSelected() {
        return group.getSelected() != null;
    }

    /**
     * Gets the select element from the group.
     * @return the selected element or {@code null} if no element is selected.
     */
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

    /**
     * Finds the element associated with the key within the element group.
     * @param key the key to search for.
     * @return the matching Option.
     * @throws IllegalArgumentException if the key can not be found.
     */
    public Option find(final String key) {
        for (Option result : group.getOptions()) {
            if (key.equals(result.getKey()) || key.equals(result.getLongOpt())) {
                return result;
            }
        }
        throw new IllegalArgumentException("Can not find " + key);
    }

    public String defaultValue() {
        return DEFAULT_VALUES.get(this);
    }

    /**
     * Gets the group for this arg.
     * @return the option gorup for this arg.
     */
    public OptionGroup group() {
        return group;
    }

    /**
     * Returns the first non-deprecated option from the group.
     * @return the first non-deprecated option or null if no non-deprecated option is available.
     */
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
        ctxt.getConfiguration().setFrom(defaultBuilder.build());

        if (FAMILIES_APPROVED.isSelected()) {
            for (String cat : ctxt.getCommandLine().getOptionValues(FAMILIES_APPROVED.getSelected())) {
                ctxt.getConfiguration().addApprovedLicenseCategory(cat);
            }
        }
        if (FAMILIES_APPROVED_FILE.isSelected()) {
            try {
                File f = ctxt.getCommandLine().getParsedOptionValue(FAMILIES_APPROVED_FILE.getSelected());
                try (InputStream in = Files.newInputStream(f.toPath())) {
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
                try (InputStream in = Files.newInputStream(f.toPath())) {
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
                try (InputStream in = Files.newInputStream(f.toPath())) {
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
                try (InputStream in = Files.newInputStream(f.toPath())) {
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
     * @param excludes the list of patterns to exclude.
     * @return the FilenameFilter tht excludes the patterns or an empty optional.
     */
    static Optional<IOFileFilter> parseExclusions(final List<String> excludes) {
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
            DefaultLog.getInstance().info("Ignored " + ignoredLines + " lines in your exclusion files as comments or empty lines.");
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
                parseExclusions(Arrays.asList(excludes)).ifPresent(ctxt.getConfiguration()::setFilesToIgnore);
            }
        }
        if (EXCLUDE_FILE.isSelected()) {
            String excludeFileName = ctxt.getCommandLine().getOptionValue(EXCLUDE_FILE.getSelected());
            if (excludeFileName != null) {
                parseExclusions(FileUtils.readLines(new File(excludeFileName), StandardCharsets.UTF_8))
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
     */
    public static void processLogLevel(final CommandLine commandLine) {
        if (LOG_LEVEL.getSelected() != null) {
            if (DefaultLog.getInstance() instanceof DefaultLog) {
                DefaultLog dLog = (DefaultLog) DefaultLog.getInstance();
                try {
                    dLog.setLevel(commandLine.getParsedOptionValue(LOG_LEVEL.getSelected()));
                } catch (ParseException e) {
                    logParseException(DefaultLog.getInstance(), e, LOG_LEVEL.getSelected(), commandLine, dLog.getLevel());
                }
            } else {
                DefaultLog.getInstance().error("log was not a DefaultLog instance. LogLevel not set.");
            }
        }
    }

    /**
    * Process the arguments.
    * @param ctxt the context in which to process the args.
    */
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
                    DefaultLog.getInstance().error("Could not create report parent directory " + f);
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
                    DefaultLog.getInstance().error("Please specify a single stylesheet");
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

    /**
     * Finds the Arg that an Option is in.
     * @param optionToFind the Option to locate.
     * @return The Arg or {@code null} if no Arg is found.
     */
    public static Arg findArg(Option optionToFind) {
        for (Arg arg : Arg.values()) {
            for (Option candidate : arg.group.getOptions()) {
                if (optionToFind.equals(candidate)) {
                    return arg;
                }
            }
        }
        return null;
    }

    /**
     * Finds the Arg that contains an Option with the specified key.
     * @param key the key for the Option to locate.
     * @return The Arg or {@code null} if no Arg is found.
     */
    public static Arg findArg(String key) {
        for (Arg arg : Arg.values()) {
            for (Option candidate : arg.group.getOptions()) {
                if (key.equals(candidate.getKey()) || key.equals(candidate.getLongOpt())) {
                    return arg;
                }
            }
        }
        return null;
    }

    /**
     * Standard messages used in descriptions.
     */
    public static class StdMsgs {
        public static String useMsg(String name) {
            return format("Use %s instead.", name);
        }
    }

   private static final Map<Arg, String> DEFAULT_VALUES = new HashMap<>();

   static {
       DEFAULT_VALUES.put(OUTPUT_FILE, "System.out");
       DEFAULT_VALUES.put(LOG_LEVEL, Log.Level.WARN.name());
       DEFAULT_VALUES.put(OUTPUT_ARCHIVE, Defaults.ARCHIVE_PROCESSING.name());
       DEFAULT_VALUES.put(OUTPUT_STANDARD, Defaults.STANDARD_PROCESSING.name());
       DEFAULT_VALUES.put(OUTPUT_LICENSES, Defaults.LIST_LICENSES.name());
       DEFAULT_VALUES.put(OUTPUT_FAMILIES, Defaults.LIST_FAMILIES.name());

   }
}
