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
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.apache.commons.cli.AlreadySelectedException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DeprecatedAttributes;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.rat.ConfigurationException;
import org.apache.rat.Defaults;
import org.apache.rat.ImplementationException;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.config.AddLicenseHeaders;
import org.apache.rat.config.exclusion.ExclusionUtils;
import org.apache.rat.config.exclusion.StandardCollection;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.report.claim.ClaimStatistic.Counter;
import org.apache.rat.ui.UIOptionCollection;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;

import static java.lang.String.format;

/**
 * An enumeration of options that are recommended across all UIs.  A UI may not implement some options if they are unsupportable
 * within the UI.
 * Each Arg contains:
 * <ul>
 *      <li>An OptionGroup that contains the individual options that all resolve to the same option.
 * This allows us to deprecate options as we move forward in development.</li>
 * <li>A {@code BiConsumer<ArgumentContext, Option>} that defines the process to configure the option in
 * the {@code ArgumentContext.configuration}.</li>
 * </ul>
 *
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
                    .build()),
    Arg::doNotExecute
    ),

    /**
     * Causes file updates to overwrite existing files.
     */
    EDIT_OVERWRITE(new OptionGroup()
            .addOption(Option.builder("f").longOpt("force")
                    .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                            .setDescription(StdMsgs.useMsg("--edit-overwrite")).get())
                    .desc("Forces any changes in files to be written directly to the source files so that new files are not created.")
                    .build())
            .addOption(Option.builder().longOpt("edit-overwrite")
                    .desc("Forces any changes in files to be written directly to the source files so that new files are not created. "
                            + "Only valid with --edit-license.")
                    .build()),
            Arg::doNotExecute
    ),

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
                    .desc("Add the Apache-2.0 license header to any file with an unknown license that is not in the exclusion list.")
                    .build())
            .addOption(Option.builder().longOpt("edit-license").desc(
                    "Add the Apache-2.0 license header to any file with an unknown license that is not in the exclusion list. "
                            + "By default new files will be created with the license header, "
                            + "to force the modification of existing files use the --edit-overwrite option.").build()
            ),
            Arg::doNotExecute
    ),

    //////////////////////////// CONFIGURATION OPTIONS
    /**
     * Group of options that read a configuration file
     */
    CONFIGURATION(new OptionGroup()
            .addOption(Option.builder().longOpt("config").hasArgs().argName("File")
                    .desc("File names for system configuration.")
                    .converter(Converters.FILE_CONVERTER)
                    .type(File.class)
                    .build())
            .addOption(Option.builder().longOpt("licenses").hasArgs().argName("File")
                    .desc("File names for system configuration.")
                    .deprecated(DeprecatedAttributes.builder().setSince("0.17").setForRemoval(true).setDescription(StdMsgs.useMsg("--config")).get())
                    .converter(Converters.FILE_CONVERTER)
                    .type(File.class)
                    .build()),
            Arg::doNotExecute
    ),

    /**
     * Group of options that skip the default configuration file
     */
    CONFIGURATION_NO_DEFAULTS(new OptionGroup()
            .addOption(Option.builder().longOpt("configuration-no-defaults")
                    .desc("Ignore default configuration.").build())
            .addOption(Option.builder().longOpt("no-default-licenses")
                    .deprecated(DeprecatedAttributes.builder()
                            .setSince("0.17")
                            .setForRemoval(true)
                            .setDescription(StdMsgs.useMsg("--configuration-no-defaults")).get())
                    .desc("Ignore default configuration.")
                    .build()),
            Arg::doNotExecute
    ),

    /**
     * Option that adds approved licenses to the list
     */
    LICENSES_APPROVED(new OptionGroup().addOption(Option.builder().longOpt("licenses-approved").hasArg().argName("LicenseID")
            .desc("A comma separated list of approved License IDs. These licenses will be added to the list of approved licenses.")
            .converter(Converters.TEXT_LIST_CONVERTER)
            .type(String[].class)
            .build()),
            (context, selected) ->
                context.getConfiguration().addApprovedLicenseIds(processArrayArg(context, selected))
    ),

    /**
     * Option that adds approved licenses from a file
     */
    LICENSES_APPROVED_FILE(new OptionGroup().addOption(Option.builder().longOpt("licenses-approved-file").hasArg().argName("File")
            .desc("Name of file containing comma separated lists of approved License IDs.")
            .converter(Converters.FILE_CONVERTER)
            .type(File.class)
            .build()),
            (context, selected) ->
                    context.getConfiguration().addApprovedLicenseIds(processArrayFile(context, selected))),

    /**
     * Option that specifies approved license families
     */
    FAMILIES_APPROVED(new OptionGroup().addOption(Option.builder().longOpt("license-families-approved").hasArg().argName("FamilyID")
            .desc("A comma separated list of approved license family IDs. These license families will be added to the list of approved license families.")
            .converter(Converters.TEXT_LIST_CONVERTER)
            .type(String[].class)
            .build()),
            (context, selected) -> context.getConfiguration().addApprovedLicenseCategories(processArrayArg(context, selected))),

    /**
     * Option that specifies approved license families from a file
     */
    FAMILIES_APPROVED_FILE(new OptionGroup().addOption(Option.builder().longOpt("license-families-approved-file").hasArg().argName("File")
            .desc("Name of file containing comma separated lists of approved family IDs.")
            .converter(Converters.FILE_CONVERTER)
            .type(File.class)
            .build()),
            (context, selected) -> context.getConfiguration().addApprovedLicenseCategories(processArrayFile(context, selected))
    ),

    /**
     * Option to remove licenses from the approved list
     */
    LICENSES_DENIED(new OptionGroup().addOption(Option.builder().longOpt("licenses-denied").hasArg().argName("LicenseID")
            .desc("A comma separated list of denied License IDs. " +
                    "These licenses will be removed from the list of approved licenses. " +
                    "Once licenses are removed they can not be added back.")
            .converter(Converters.TEXT_LIST_CONVERTER)
            .type(String[].class)
            .build()),
            (context, selected) -> context.getConfiguration().removeApprovedLicenseIds(processArrayArg(context, selected))),

    /**
     * Option to read a file licenses to be removed from the approved list.
     */
    LICENSES_DENIED_FILE(new OptionGroup().addOption(Option.builder().longOpt("licenses-denied-file")
            .hasArg().argName("File").type(File.class)
            .converter(Converters.FILE_CONVERTER)
            .desc("Name of file containing comma separated lists of the denied license IDs. " +
                    "These licenses will be removed from the list of approved licenses. " +
                    "Once licenses are removed they can not be added back.")
            .build()),
            (context, selected) -> context.getConfiguration().removeApprovedLicenseIds(processArrayFile(context, selected))),

    /**
     * Option to list license families to remove from the approved list.
     */
    FAMILIES_DENIED(new OptionGroup().addOption(Option.builder().longOpt("license-families-denied")
            .hasArg().argName("FamilyID")
            .desc("A comma separated list of denied License family IDs. " +
                    "These license families will be removed from the list of approved licenses. " +
                    "Once license families are removed they can not be added back.")
            .converter(Converters.TEXT_LIST_CONVERTER)
            .type(String[].class)
            .build()),
            (context, selected) -> context.getConfiguration().removeApprovedLicenseCategories(processArrayArg(context, selected))),

    /**
     * Option to read a list of license families to remove from the approved list.
     */
    FAMILIES_DENIED_FILE(new OptionGroup().addOption(Option.builder().longOpt("license-families-denied-file").hasArg().argName("File")
            .desc("Name of file containing comma separated lists of denied license IDs. " +
                    "These license families will be removed from the list of approved licenses. " +
                    "Once license families are removed they can not be added back.")
            .type(File.class)
            .converter(Converters.FILE_CONVERTER)
            .build()),
            (context, selected) -> context.getConfiguration().removeApprovedLicenseCategories(processArrayFile(context, selected))),

    /**
     * Option to specify an acceptable number of various counters.
     */
    COUNTER_MAX(new OptionGroup().addOption(Option.builder().longOpt("counter-max").hasArgs().argName("CounterPattern")
            .desc("The acceptable maximum number for the specified counter. A value of '-1' specifies an unlimited number.")
            .converter(Converters.COUNTER_CONVERTER)
            .type(Pair.class)
            .build()),
            (context, selected) -> {
                for (String arg : context.getCommandLine().getOptionValues(selected)) {
                    Pair<Counter, Integer> pair = Converters.COUNTER_CONVERTER.apply(arg);
                    int limit = pair.getValue();
                    context.getConfiguration().getClaimValidator().setMax(pair.getKey(), limit < 0 ? Integer.MAX_VALUE : limit);
                }
            }),

    /**
     * Option to specify an acceptable number of various counters.
     */
    COUNTER_MIN(new OptionGroup().addOption(Option.builder().longOpt("counter-min").hasArgs().argName("CounterPattern")
            .desc("The minimum number for the specified counter.")
            .converter(Converters.COUNTER_CONVERTER)
            .type(Pair.class)
            .build()),
            (context, selected) -> {
                for (String arg : context.getCommandLine().getOptionValues(selected)) {
                    Pair<Counter, Integer> pair = Converters.COUNTER_CONVERTER.apply(arg);
                    context.getConfiguration().getClaimValidator().setMin(pair.getKey(), pair.getValue());
                }
            }),

////////////////// INPUT OPTIONS
    /**
     * Reads files to test from a file.
     */
    SOURCE(new OptionGroup()
            .addOption(Option.builder().longOpt("input-source").hasArgs().argName("File")
                    .desc("A file containing file names to process. " +
                            "File names must use linux directory separator ('/') or none at all. " +
                            "File names that do not start with '/' are relative to the directory where the " +
                            "argument is located.")
                    .converter(Converters.FILE_CONVERTER)
                    .type(File.class)
                    .build()),
            (context, selected) -> {
                File[] files = getParsedOptionValues(selected, context.getCommandLine());
                for (File f : files) {
                    context.getConfiguration().addSource(f);
                }
            }),

    /**
     * Excludes files by expression
     */
    EXCLUDE(new OptionGroup()
            .addOption(Option.builder("e").longOpt("exclude").hasArgs().argName("Expression")
                    .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                            .setDescription(StdMsgs.useMsg("--input-exclude")).get())
                    .desc("Excludes files matching <Expression>.")
                    .build())
            .addOption(Option.builder().longOpt("input-exclude").hasArgs().argName("Expression")
                    .desc("Excludes files matching <Expression>.")
                    .build()),
            (context, selected) -> {
                String[] excludes = context.getCommandLine().getOptionValues(selected);
                if (excludes != null) {
                    context.getConfiguration().addExcludedPatterns(Arrays.asList(excludes));
                }
            }),

    /**
     * Excludes files based on the contents of a file.
     */
    EXCLUDE_FILE(new OptionGroup()
            .addOption(Option.builder("E").longOpt("exclude-file")
                    .argName("File").hasArg().type(File.class)
                    .converter(Converters.FILE_CONVERTER)
                    .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                            .setDescription(StdMsgs.useMsg("--input-exclude-file")).get())
                    .desc("Reads <Expression> entries from a file. Entries will be excluded from processing.")
                    .build())
            .addOption(Option.builder().longOpt("input-exclude-file")
                    .argName("File").hasArg().type(File.class)
                    .converter(Converters.FILE_CONVERTER)
                    .desc("Reads <Expression> entries from a file. Entries will be excluded from processing.")
                    .build()),
            (context, selected) -> {
                try {
                    File excludeFileName = context.getCommandLine().getParsedOptionValue(selected);
                    if (excludeFileName != null) {
                        context.getConfiguration().addExcludedPatterns(ExclusionUtils.asIterable(excludeFileName, "#"));
                    }
                } catch (Exception e) {
                    throw ConfigurationException.from(e);
                }
            }),
    /**
     * Excludes files based on standard groupings.
     */
    EXCLUDE_STD(new OptionGroup()
            .addOption(Option.builder().longOpt("input-exclude-std").argName("StandardCollection")
                    .hasArgs().converter(s -> StandardCollection.valueOf(s.toUpperCase()))
                    .desc("Excludes files defined in standard collections based on commonly occurring groups. " +
                            "Excludes any path matcher actions but DOES NOT exclude any file processor actions.")
                    .type(StandardCollection.class)
                    .build()),
            (context, selected) -> {
                for (String s : context.getCommandLine().getOptionValues(selected)) {
                    context.getConfiguration().addExcludedCollection(StandardCollection.valueOf(s));
                }
            }),

    /**
     * Excludes files if they are smaller than the given threshold.
     */
    EXCLUDE_SIZE(new OptionGroup()
            .addOption(Option.builder().longOpt("input-exclude-size").argName("Integer")
                    .hasArg().type(Integer.class)
                    .desc("Excludes files with sizes less than the number of bytes specified.")
                    .build()),
            (context, selected) -> {
                try {
                    final int maxSize = context.getCommandLine().getParsedOptionValue(selected);
                    DocumentNameMatcher matcher = new DocumentNameMatcher(String.format("File size < %s bytes", maxSize),
                            (Predicate<DocumentName>) documentName -> {
                                File f = new File(documentName.getName());
                                return f.isFile() && f.length() < maxSize;
                            });
                    context.getConfiguration().addExcludedMatcher(matcher);
                } catch (Exception e) {
                    throw ConfigurationException.from(e);
                }
            }),
    /**
     * Excludes files by expression.
     */
    INCLUDE(new OptionGroup()
            .addOption(Option.builder().longOpt("input-include").hasArgs().argName("Expression")
                    .desc("Includes files matching <Expression>. Will override excluded files.")
                    .build())
            .addOption(Option.builder().longOpt("include").hasArgs().argName("Expression")
                    .desc("Includes files matching <Expression>. Will override excluded files.")
                    .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                            .setDescription(StdMsgs.useMsg("--input-include")).get())
                    .build()),
            (context, selected) -> {
                String[] includes = context.getCommandLine().getOptionValues(selected);
                if (includes != null) {
                    context.getConfiguration().addIncludedPatterns(Arrays.asList(includes));
                }
            }),

    /**
     * Includes files based on the contents of a file.
     */
    INCLUDE_FILE(new OptionGroup()
            .addOption(Option.builder().longOpt("input-include-file")
                    .argName("File").hasArg().type(File.class)
                    .converter(Converters.FILE_CONVERTER)
                    .desc("Reads <Expression> entries from a file. Entries will override excluded files.")
                    .build())
            .addOption(Option.builder().longOpt("includes-file")
                    .argName("File").hasArg().type(File.class)
                    .converter(Converters.FILE_CONVERTER)
                    .desc("Reads <Expression> entries from a file. Entries will be excluded from processing.")
                    .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                            .setDescription(StdMsgs.useMsg("--input-include-file")).get())
                    .build()),
            (context, selected) -> {
                try {
                    File includeFileName = context.getCommandLine().getParsedOptionValue(selected);
                    if (includeFileName != null) {
                        context.getConfiguration().addIncludedPatterns(ExclusionUtils.asIterable(includeFileName, "#"));
                    }
                } catch (Exception e) {
                    throw ConfigurationException.from(e);
                }
            }),

    /**
     * Includes files based on standard groups.
     */
    INCLUDE_STD(new OptionGroup()
            .addOption(Option.builder().longOpt("input-include-std").argName("StandardCollection")
                    .hasArgs().converter(s -> StandardCollection.valueOf(s.toUpperCase()))
                    .desc("Includes files defined in standard collections based on commonly occurring groups. " +
                            "Includes any path matcher actions but DOES NOT include any file processor actions.")
                    .type(StandardCollection.class)
                    .build())
            .addOption(Option.builder().longOpt("scan-hidden-directories")
                    .desc("Scans hidden directories.")
                    .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                            .setDescription(StdMsgs.useMsg("--input-include-std with 'HIDDEN_DIR' argument")).get()).build()
            ),
            (context, selected) -> {
                // display deprecation log if needed.
                if (context.getCommandLine().hasOption("scan-hidden-directories")) {
                    context.getConfiguration().addIncludedCollection(StandardCollection.HIDDEN_DIR);
                } else {
                    for (String s : context.getCommandLine().getOptionValues(selected)) {
                        context.getConfiguration().addIncludedCollection(StandardCollection.valueOf(s));
                    }
                }
            }),

    /**
     * Excludes files based on SCM exclusion file processing.
     */
    EXCLUDE_PARSE_SCM(new OptionGroup()
            .addOption(Option.builder().longOpt("input-exclude-parsed-scm")
                    .argName("StandardCollection")
                    .hasArgs().converter(s -> StandardCollection.valueOf(s.toUpperCase()))
                    .desc("Parse SCM based exclusion files to exclude specified files and directories. " +
                            "This action can apply to any standard collection that implements a file processor.")
                    .type(StandardCollection.class)
                    .build()),
            (context, selected) -> {
                StandardCollection[] collections = getParsedOptionValues(selected, context.getCommandLine());
                final ReportConfiguration configuration = context.getConfiguration();
                for (StandardCollection collection : collections) {
                    if (collection == StandardCollection.ALL) {
                        Arrays.asList(StandardCollection.values()).forEach(configuration::addExcludedFileProcessor);
                        Arrays.asList(StandardCollection.values()).forEach(configuration::addExcludedCollection);
                    } else {
                        configuration.addExcludedFileProcessor(collection);
                        configuration.addExcludedCollection(collection);
                    }
                }
            }),

    /**
     * Stop processing an input stream and declare an input file.
     */
    DIR(new OptionGroup().addOption(Option.builder().option("d").longOpt("dir").hasArg()
            .type(File.class)
            .desc("Used to indicate end of list when using options that take multiple arguments.").argName("DirOrArchive")
            .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                    .setDescription("Use the standard '--' to signal the end of arguments.").get()).build()),
            Arg::doNotExecute
    ),

    /////////////// OUTPUT OPTIONS
    /**
     * Defines the stylesheet to use.
     */
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
                    .deprecated(DeprecatedAttributes.builder()
                            .setSince("0.17")
                            .setForRemoval(true)
                            .setDescription(StdMsgs.useMsg("--output-style with the 'xml' argument")).get())
                    .desc("forces XML output rather than the textual report.")
                    .build()),
            (context, selected) -> {
                String key = selected.getKey(); // is not null due to above isSelected()-call
                if ("x".equals(key)) {
                    // display deprecated message.
                    context.getCommandLine().hasOption("x");
                    context.getConfiguration().setStyleSheet(StyleSheets.getStyleSheet("xml", context.getWorkingDirectory()));
                } else {
                    String[] style = context.getCommandLine().getOptionValues(selected);
                    if (style.length != 1) {
                        DefaultLog.getInstance().error("Please specify a single stylesheet");
                        throw new ConfigurationException("Please specify a single stylesheet");
                    }
                    context.getConfiguration().setStyleSheet(StyleSheets.getStyleSheet(style[0], context.getWorkingDirectory()));
                }
            }),

    /**
     * Specifies the license definitions that should be included in the output.
     */
    OUTPUT_LICENSES(new OptionGroup()
            .addOption(Option.builder().longOpt("output-licenses").hasArg().argName("LicenseFilter")
                    .desc("List the defined licenses.")
                    .converter(s -> LicenseSetFactory.LicenseFilter.valueOf(s.toUpperCase()))
                    .build())
            .addOption(Option.builder().longOpt("list-licenses").hasArg().argName("LicenseFilter")
                    .desc("List the defined licenses.")
                    .converter(s -> LicenseSetFactory.LicenseFilter.valueOf(s.toUpperCase()))
                    .deprecated(DeprecatedAttributes.builder().setSince("0.17").setForRemoval(true).setDescription(StdMsgs.useMsg("--output-licenses")).get())
                    .build()),
            (context, selected) -> {
                try {
                    context.getConfiguration().listLicenses(context.getCommandLine().getParsedOptionValue(selected));
                } catch (ParseException e) {
                    context.logParseException(e, selected, Defaults.LIST_LICENSES);
                }
            }),

    /**
     * Specifies the license families that should be included in the output.
     */
    OUTPUT_FAMILIES(new OptionGroup()
            .addOption(Option.builder().longOpt("output-families").hasArg().argName("LicenseFilter")
                    .desc("List the defined license families.")
                    .converter(s -> LicenseSetFactory.LicenseFilter.valueOf(s.toUpperCase()))
                    .build())
            .addOption(Option.builder().longOpt("list-families").hasArg().argName("LicenseFilter")
                    .desc("List the defined license families.")
                    .converter(s -> LicenseSetFactory.LicenseFilter.valueOf(s.toUpperCase()))
                    .deprecated(DeprecatedAttributes.builder().setSince("0.17").setForRemoval(true).setDescription(StdMsgs.useMsg("--output-families")).get())
                    .build()),
            (context, selected) -> {
                try {
                    context.getConfiguration().listFamilies(context.getCommandLine().getParsedOptionValue(selected));
                } catch (ParseException e) {
                    context.logParseException(e, selected, Defaults.LIST_FAMILIES);
                }
            }),

    /**
     * Specifies the log level to log messages at.
     */
    LOG_LEVEL(new OptionGroup().addOption(Option.builder().longOpt("log-level")
            .hasArg().argName("LogLevel")
            .desc("Sets the log level.")
            .converter(s -> Log.Level.valueOf(s.toUpperCase()))
            .build()),
            (context, selected) -> {
                Log dLog = DefaultLog.getInstance();
                try {
                    dLog.setLevel(context.getCommandLine().getParsedOptionValue(selected));
                } catch (ParseException e) {
                    logParseException(DefaultLog.getInstance(), e, selected, context.getCommandLine(), dLog.getLevel());
                }
            }),

    /**
     * Specifies that the run should not perform any updates to files.
     */
    DRY_RUN(new OptionGroup().addOption(Option.builder().longOpt("dry-run")
            .desc("If set do not update the files but generate the reports.")
            .build()),
            (context, selected) ->
                    context.getConfiguration().setDryRun(true)
    ),

    /**
     * Specifies where the output should be written.
     */
    OUTPUT_FILE(new OptionGroup()
            .addOption(Option.builder().option("o").longOpt("out").hasArg().argName("File")
                    .desc("Define the output file where to write a report to.")
                    .deprecated(DeprecatedAttributes.builder().setSince("0.17").setForRemoval(true).setDescription(StdMsgs.useMsg("--output-file")).get())
                    .type(File.class)
                    .converter(Converters.FILE_CONVERTER)
                    .build())
            .addOption(Option.builder().longOpt("output-file").hasArg().argName("File")
                    .desc("Define the output file where to write a report to.")
                    .type(File.class)
                    .converter(Converters.FILE_CONVERTER)
                    .build()),
            (context, selected) -> {
                try {
                    File file = context.getCommandLine().getParsedOptionValue(selected);
                    File parent = file.getParentFile();
                    if (!parent.mkdirs() && !parent.isDirectory()) {
                        DefaultLog.getInstance().error("Could not create report parent directory " + file);
                    }
                    context.getConfiguration().setOut(file);
                } catch (ParseException e) {
                    // we write to system out by default.
                    context.logParseException(e, selected, "System.out");
                    context.getConfiguration().setOut(ReportConfiguration.SYSTEM_OUT);
                }
            }),

    /**
     * Specifies the level of reporting detail for archive files.
     */
    OUTPUT_ARCHIVE(new OptionGroup()
            .addOption(Option.builder().longOpt("output-archive").hasArg().argName("ProcessingType")
                    .desc("Specifies the level of detail in ARCHIVE file reporting.")
                    .converter(s -> ReportConfiguration.Processing.valueOf(s.toUpperCase()))
                    .build()),
            (context, selected) -> {
                try {
                    context.getConfiguration().setArchiveProcessing(context.getCommandLine().getParsedOptionValue(selected));
                } catch (ParseException e) {
                    context.logParseException(e, selected, Defaults.ARCHIVE_PROCESSING);
                }
            }
    ),

    /**
     * Specifies the level of reporting detail for standard files.
     */
    OUTPUT_STANDARD(new OptionGroup()
            .addOption(Option.builder().longOpt("output-standard").hasArg().argName("ProcessingType")
                    .desc("Specifies the level of detail in STANDARD file reporting.")
                    .converter(s -> ReportConfiguration.Processing.valueOf(s.toUpperCase()))
                    .build()),
            (context, selected) -> {
                try {
                    context.getConfiguration().setStandardProcessing(context.getCommandLine().getParsedOptionValue(selected));
                } catch (ParseException e) {
                    context.logParseException(e, selected, Defaults.STANDARD_PROCESSING);
                }
            }),

    /**
     * Provide license definition listing of registered licenses.
     */
    HELP_LICENSES(new OptionGroup()
            .addOption(Option.builder().longOpt("help-licenses") //
                    .desc("Print information about registered licenses.").build()),
            Arg::doNotExecute
    );

    /**
     * The option group for the argument
     */
    private final OptionGroup group;

    /**
     * The BiConsumer to apply the option to update the state of the context.configuration.
     */
    private final BiConsumer<ArgumentContext, Option> process;


    private static void doNotExecute(final ArgumentContext context, final Option selected) {
        throw new ImplementationException(String.format("'%s' should not be executed directly", selected));
    }

    /**
     * Creates an Arg from an Option group.
     *
     * @param group The option group.
     */
    Arg(final OptionGroup group, final BiConsumer<ArgumentContext, Option> process) {
        this.group = group;
        this.process = process;
    }

    /**
     * Executes the process associated with this Arg if the collection has an Option from this group selected.
     * @param context the ArgumentContext that is being processed.
     * @param optionCollection the OptionCollection that is available.
     */
    private void execute(final ArgumentContext context, final UIOptionCollection<?> optionCollection) {
        optionCollection.getSelected(this)
                .ifPresent(selected -> this.process.accept(context, selected));
    }

    /**
     * Determines if all the options have been removed from this argument.
     *
     * @return {@code true} if all the options have been removed from this argument.
     */
    public boolean isEmpty() {
        return this.group().getOptions().isEmpty();
    }

    /**
     * Finds the element associated with the key within the element group.
     *
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

    /**
     * Gets the group for this arg.
     *
     * @return the option group for this arg.
     */
    public OptionGroup group() {
        return group;
    }

    /**
     * Returns the first non-deprecated option from the group.
     *
     * @return the first non-deprecated option or, if no non-deprecated option is available, the first option.
     */
    public Option option() {
        Option first = null;
        for (Option result : group.getOptions()) {
            if (first == null) {
                first = result;
            }
            if (!result.isDeprecated()) {
                return result;
            }
        }
        return first;
    }

    /**
     * Gets the full set of options.
     *
     * @return the full set of options for this Arg.
     */
    public static Options getOptions() {
        Options options = new Options();
        for (Arg arg : Arg.values()) {
            options.addOptionGroup(arg.group);
        }
        return options;
    }

    /**
     * Processes the edit arguments.
     *
     * @param context the context to work with.
     */
    private static void processEditArgs(final ArgumentContext context, final UIOptionCollection<?> optionCollection) {
        optionCollection.getSelected(EDIT_ADD).ifPresent(option -> {
            // prints deprecation
            context.getCommandLine().hasOption(option);
            boolean force = optionCollection.isSelected(EDIT_OVERWRITE);
            if (force) {
                // prints deprecation
                optionCollection.getSelected(EDIT_OVERWRITE).ifPresent(context.getCommandLine()::hasOption);
            }
            context.getConfiguration().setAddLicenseHeaders(force ? AddLicenseHeaders.FORCED : AddLicenseHeaders.TRUE);
            optionCollection.getSelected(EDIT_COPYRIGHT).
                    ifPresent(editOption -> context.getConfiguration().setCopyrightMessage(context.getCommandLine().getOptionValue(editOption)));
        });
    }

    /**
     * Gets the list of Strings that are arguments for the option.
     * @param context the ArgumentContext containing the command line.
     * @param selected the selected option.
     * @return the list of Strings that are aguments.
     */
    private static List<String> processArrayArg(final ArgumentContext context, final Option selected) {
        try {
            return Arrays.asList(context.getCommandLine().getParsedOptionValue(selected));
        } catch (ParseException e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * parses the option as a file.
     * @param context the Argument context that provides the command line.
     * @param selected the selected option.
     * @return Option as a file.
     */
    private static File commandLineFile(final ArgumentContext context, final Option selected) throws ParseException {
        return context.getCommandLine().getParsedOptionValue(selected);
    }

    /**
     * parses lines with comma separated tokens from a file and returns the entire collection of tokens as a list of strings.
     * @param context the Argument context that provides the command line.
     * @param selected the selected option.
     * @return the list of strings parsed from the file.
     */
    private static List<String> processArrayFile(final ArgumentContext context, final Option selected) {
        List<String> result = new ArrayList<>();
        try (InputStream in = Files.newInputStream(commandLineFile(context, selected)
                .toPath())) {
            for (String line : IOUtils.readLines(in, StandardCharsets.UTF_8)) {
                String[] ids = Converters.TEXT_LIST_CONVERTER.apply(line);
                result.addAll(Arrays.asList(ids));
            }
            return result;
        } catch (IOException e) {
            throw new ConfigurationException(e);

        } catch (ParseException e) {
            throw ConfigurationException.from(e);
        }
    }

    /**
     * Processes the configuration options.
     *
     * @param context the context to process.
     * @throws ConfigurationException if configuration files can not be read.
     */
    private static void processConfigurationArgs(final ArgumentContext context, final UIOptionCollection<?> optionCollection) throws ConfigurationException {

        Defaults.Builder defaultBuilder = Defaults.builder();

        optionCollection.getSelected(CONFIGURATION).ifPresent(
                selected -> {
                    File[] files = getParsedOptionValues(selected, context.getCommandLine());
                    for (File file : files) {
                        defaultBuilder.add(file);
                    }
                });
        optionCollection.getSelected(CONFIGURATION_NO_DEFAULTS).ifPresent(selected -> {
            // display deprecation log if needed.
            context.getCommandLine().hasOption(selected);
            defaultBuilder.noDefault();
        });
        context.getConfiguration().setFrom(defaultBuilder.build());

        for (Arg arg : new Arg[]{FAMILIES_APPROVED, FAMILIES_APPROVED_FILE, FAMILIES_DENIED, FAMILIES_DENIED_FILE,
                LICENSES_APPROVED, LICENSES_APPROVED_FILE, LICENSES_DENIED, LICENSES_DENIED_FILE,
                COUNTER_MAX, COUNTER_MIN}) {
            arg.execute(context, optionCollection);
        }
    }

    /**
     * Process the input setup.
     *
     * @param context the context to work in.
     * @throws ConfigurationException if an exclude file can not be read.
     */
    private static void processInputArgs(final ArgumentContext context, final UIOptionCollection<?> optionCollection) throws ConfigurationException {
        for (Arg arg : new Arg[]{SOURCE, EXCLUDE, EXCLUDE_FILE, EXCLUDE_STD, EXCLUDE_PARSE_SCM, EXCLUDE_SIZE,
                INCLUDE, INCLUDE_FILE, INCLUDE_STD}) {
            arg.execute(context, optionCollection);
        }
    }

    /**
     * Logs a ParseException as a warning.
     *
     * @param log the Log to write to
     * @param exception the parse exception to log
     * @param opt the option being processed
     * @param cl the command line being processed
     * @param defaultValue The default value the option is being set to.
     */
    private static void logParseException(final Log log, final ParseException exception, final Option opt, final CommandLine cl, final Object defaultValue) {
        log.warn(format("Invalid %s specified: %s ", opt.getOpt(), cl.getOptionValue(opt)));
        log.warn(format("%s set to: %s", opt.getOpt(), defaultValue));
        log.debug(exception);
    }

    /**
     * Process the log level setting.
     *
     * @param context The argument context
     */
    public static void processLogLevel(final ArgumentContext context, final UIOptionCollection<?> optionCollection) throws ConfigurationException {
        LOG_LEVEL.execute(context, optionCollection);
    }

    /**
     * Process the arguments.
     *
     * @param context the context in which to process the args.
     * @throws ConfigurationException on error
     */
    public static void processArgs(final ArgumentContext context, final UIOptionCollection<?> optionCollection) throws ConfigurationException {
        Converters.FILE_CONVERTER.setWorkingDirectory(context.getWorkingDirectory());
        processOutputArgs(context, optionCollection);
        processEditArgs(context, optionCollection);
        processInputArgs(context, optionCollection);
        processConfigurationArgs(context, optionCollection);
    }

    /**
     * Process the arguments that can be processed together.
     *
     * @param context the context in which to process the args.
     */
    private static void processOutputArgs(final ArgumentContext context, final UIOptionCollection<?> optionCollection) throws ConfigurationException {
        for (Arg arg : new Arg[]{DRY_RUN, OUTPUT_FAMILIES, OUTPUT_LICENSES, OUTPUT_ARCHIVE, OUTPUT_STANDARD, OUTPUT_FILE, OUTPUT_STYLE}) {
            arg.execute(context, optionCollection);
        }
    }

    /**
     * Resets the groups in the Args so that they are unused and ready to detect the next set of arguments.
     */
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
     * Finds the Arg that contains an Option with the specified key.
     *
     * @param key the key for the Option to locate.
     * @return The Arg or {@code null} if no Arg is found.
     */
    public static Arg findArg(final String key) {
        if (key != null) {
            for (Arg arg : Arg.values()) {
                for (Option candidate : arg.group.getOptions()) {
                    if (key.equals(candidate.getKey()) || key.equals(candidate.getLongOpt())) {
                        return arg;
                    }
                }
            }
        }
        return null;
    }

    private static <T> T[] getParsedOptionValues(final Option selected, final CommandLine commandLine) {
        try {
            Class<? extends T> clazz = (Class<? extends T>) selected.getType();
            String[] values = commandLine.getOptionValues(selected);
            T[] result = (T[]) Array.newInstance(clazz, values.length);
            for (int i = 0; i < values.length; i++) {
                result[i] = clazz.cast(selected.getConverter().apply(values[i]));
            }
            return result;
        } catch (Throwable t) {
            throw new ConfigurationException(format("'%s' converter for %s '%s' does not produce a class of type %s", selected,
                    selected.getKey(), selected.getConverter().getClass().getName(), selected.getType()), t);
        }
    }

    /**
     * Standard messages used in descriptions.
     */
    public static final class StdMsgs {
        private StdMsgs() {
            // do not instantiate
        }

        /**
         * Gets the standard "use instead" message for the specific name.
         *
         * @param name the name of the option to use instead.
         * @return combined "use instead" message.
         */
        public static String useMsg(final String name) {
            return format("Use %s instead.", name);
        }
    }
}
