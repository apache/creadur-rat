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
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Converter;
import org.apache.commons.cli.DeprecatedAttributes;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.function.IOSupplier;
import org.apache.rat.ConfigurationException;
import org.apache.rat.Defaults;
import org.apache.rat.Report;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;

/**
 * Processes output arguments
 * @since 0.17
 */
public final class OutputArgs {

    /** Defines the stylesheet to use */
    private static final OptionGroup OUTPUT_STYLE = new OptionGroup()
            .addOption(Option.builder().longOpt("output-style").hasArg().argName("StyleSheet")
            .desc("XSLT stylesheet to use when creating the report. "
                    + "Either an external xsl file may be specified or one of the internal named sheets.")
            .build())
            .addOption(Option.builder("s").longOpt("stylesheet").hasArg().argName("StyleSheet")
            .desc("XSLT stylesheet to use when creating the report.  Not compatible with -x. "
                    + "Either an external xsl file may be specified or one of the internal named sheets.")
            .deprecated(DeprecatedAttributes.builder().setSince("0.17.0").setForRemoval(true).setDescription("Use --output-style").get())
            .build())
            .addOption(Option.builder("x").longOpt("xml")
            .desc("Output the report in raw XML format.  Not compatible with -s")
            .deprecated(DeprecatedAttributes.builder().setSince("0.17.0").setForRemoval(true).setDescription("Use --output-style xml").get())
            .build());

    /** Specifies that  license definitions that should be included in the output */
    private static final OptionGroup OUTPUT_LICENSES = new OptionGroup()
            .addOption(Option.builder().longOpt("output-licenses").hasArg().argName("LicenseFilter")
            .desc("List the defined licenses (default is NONE).")
            .converter(s -> LicenseSetFactory.LicenseFilter.valueOf(s.toUpperCase()))
            .build())
            .addOption(Option.builder().longOpt("list-licenses").hasArg().argName("LicenseFilter")
            .desc("List the defined licenses (default is NONE).")
            .converter(s -> LicenseSetFactory.LicenseFilter.valueOf(s.toUpperCase()))
            .deprecated(DeprecatedAttributes.builder().setSince("0.17.0").setForRemoval(true).setDescription("Use --output-licenses").get())
            .build());

    /** Specifies that the license families that should be included in the output */
    private static final OptionGroup OUTPUT_FAMILIES = new OptionGroup()
            .addOption(Option.builder().longOpt("output-families").hasArg().argName("LicenseFilter")
            .desc("List the defined license families (default is NONE).")
            .converter(s -> LicenseSetFactory.LicenseFilter.valueOf(s.toUpperCase()))
            .build())
            .addOption(Option.builder().longOpt("list-families").hasArg().argName("LicenseFilter")
            .desc("List the defined license families (default is NONE).")
            .converter(s -> LicenseSetFactory.LicenseFilter.valueOf(s.toUpperCase()))
            .deprecated(DeprecatedAttributes.builder().setSince("0.17.0").setForRemoval(true).setDescription("Use --output-families").get())
            .build());

    /** Specifies the log level to log messages at. */
    public static final Option LOG_LEVEL = Option.builder().longOpt("log-level")
            .hasArg().argName("LogLevel")
            .desc("sets the log level.")
            .converter(s -> Log.Level.valueOf(s.toUpperCase()))
            .build();

    /** Specifies that the run should not perform any updates to files.  */
    private static final Option DRY_RUN = Option.builder().longOpt("dry-run")
            .desc("If set do not update the files but generate the reports.")
            .build();


    /** Specifies where the output should be written.  */
    private static final OptionGroup OUTPUT_FILE = new OptionGroup()
            .addOption(Option.builder().option("o").longOpt("out").hasArg()
            .desc("Define the output file where to write a report to (default is System.out).")
            .deprecated(DeprecatedAttributes.builder().setSince("0.17.0").setForRemoval(true).setDescription("Use --output-file").get())
            .type(File.class)
            .converter(Converter.FILE).build())
            .addOption(Option.builder().longOpt("output-file").hasArg()
            .desc("Define the output file where to write a report to (default is System.out).")
            .type(File.class)
            .converter(Converter.FILE).build());

    /** Specifies the level of reporting detail for archive files. */
    private static final OptionGroup OUTPUT_ARCHIVE = new OptionGroup()
            .addOption(Option.builder().longOpt("archive").hasArg().argName("ProcessingType")
            .desc(format("Specifies the level of detail in ARCHIVE file reporting. (default is %s)",
                    Defaults.ARCHIVE_PROCESSING))
                    .deprecated(DeprecatedAttributes.builder().setSince("0.17.0").setForRemoval(true).setDescription("Use --output-archive").get())
                    .converter(s -> ReportConfiguration.Processing.valueOf(s.toUpperCase()))
            .build())
            .addOption(Option.builder().longOpt("output-archive").hasArg().argName("ProcessingType")
                    .desc(format("Specifies the level of detail in ARCHIVE file reporting. (default is %s)",
                            Defaults.ARCHIVE_PROCESSING))
                    .converter(s -> ReportConfiguration.Processing.valueOf(s.toUpperCase()))
                    .build());

    /** Specifies the level or reporting detail for standard files. */
    private static final OptionGroup OUTPUT_STANDARD = new OptionGroup()
            .addOption(Option.builder().longOpt("standard").hasArg().argName("ProcessingType")
            .desc(format("Specifies the level of detail in STANDARD file reporting. (default is %s)",
                    Defaults.STANDARD_PROCESSING))
                    .deprecated(DeprecatedAttributes.builder().setSince("0.17.0").setForRemoval(true).setDescription("Use --output-standard").get())
            .converter(s -> ReportConfiguration.Processing.valueOf(s.toUpperCase()))
            .build())
            .addOption(Option.builder().longOpt("output-standard").hasArg().argName("ProcessingType")
                    .desc(format("Specifies the level of detail in STANDARD file reporting. (default is %s)",
                            Defaults.STANDARD_PROCESSING))
                    .converter(s -> ReportConfiguration.Processing.valueOf(s.toUpperCase()))
                    .build());

    private OutputArgs() {
        // do not instantiate
    }
    /**
     * Adds the options from this set of options to the options argument.
     * @param options the options to add to.
     */
    public static void addOptions(final Options options) {
        options.addOptionGroup(OUTPUT_STYLE)
                .addOptionGroup(OUTPUT_LICENSES)
                .addOptionGroup(OUTPUT_FAMILIES)
                .addOptionGroup(OUTPUT_STANDARD)
                .addOptionGroup(OUTPUT_ARCHIVE)
                .addOption(LOG_LEVEL)
                .addOption(DRY_RUN)
                .addOptionGroup(OUTPUT_FILE);
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
        if (commandLine.hasOption(OutputArgs.LOG_LEVEL)) {
            if (log instanceof DefaultLog) {
                DefaultLog dLog = (DefaultLog) log;
                try {
                    dLog.setLevel(commandLine.getParsedOptionValue(OutputArgs.LOG_LEVEL));
                } catch (ParseException e) {
                    logParseException(log, e, OutputArgs.LOG_LEVEL, commandLine, dLog.getLevel());
                }
            } else {
                log.error("log was not a DefaultLog instance. LogLevel not set.");
            }
        }
    }

    /**
     * Process the arguments that can be processed together.
     * @param ctxt the context in which to process the args.
     */
    public static void processArgs(final ArgumentContext ctxt) {
        ctxt.getConfiguration().setDryRun(ctxt.getCommandLine().hasOption(DRY_RUN));

        if (OUTPUT_FAMILIES.getSelected() != null) {
            try {
                ctxt.getConfiguration().listFamilies(ctxt.getCommandLine().getParsedOptionValue(OUTPUT_FAMILIES.getSelected()));
            } catch (ParseException e) {
                ctxt.logParseException(e, OUTPUT_FAMILIES.getSelected(), Defaults.LIST_FAMILIES);
            }
        }

        if (OUTPUT_LICENSES.getSelected() != null) {
            try {
                ctxt.getConfiguration().listLicenses(ctxt.getCommandLine().getParsedOptionValue(OUTPUT_LICENSES.getSelected()));
            } catch (ParseException e) {
                ctxt.logParseException(e, OUTPUT_LICENSES.getSelected(), Defaults.LIST_LICENSES);
            }
        }

        if (OUTPUT_ARCHIVE.getSelected() != null) {
            try {
                ctxt.getConfiguration().setArchiveProcessing(ctxt.getCommandLine().getParsedOptionValue(OUTPUT_ARCHIVE.getSelected()));
            } catch (ParseException e) {
                ctxt.logParseException(e, OUTPUT_ARCHIVE.getSelected(), Defaults.ARCHIVE_PROCESSING);
            }
        }


        if (OUTPUT_STANDARD.getSelected() != null) {
            try {
                ctxt.getConfiguration().setStandardProcessing(ctxt.getCommandLine().getParsedOptionValue(OUTPUT_STANDARD.getSelected()));
            } catch (ParseException e) {
                ctxt.logParseException(e, OUTPUT_STANDARD.getSelected(), Defaults.STANDARD_PROCESSING);
            }
        }

        if (OUTPUT_FILE.getSelected() != null) {
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

        if (OUTPUT_STYLE.getSelected() != null) {
            String selected = OUTPUT_STYLE.getSelected();
            if (selected.equals("x")) {
                // display deprecated message.
                ctxt.getCommandLine().hasOption("x");
                ctxt.getConfiguration().setStyleSheet(getStyleSheet("xml"));
            } else {
                String[] style = ctxt.getCommandLine().getOptionValues(OUTPUT_STYLE.getSelected());
                if (style.length != 1) {
                    ctxt.getLog().error("Please specify a single stylesheet");
                    throw new ConfigurationException("Please specify a single stylesheet");
                }
                ctxt.getConfiguration().setStyleSheet(getStyleSheet(style[0]));
            }
        }
    }

    /**
     * Get the IOSupplier for a style sheet.
     * @param style the styles sheet to get the IOSupplier for.
     * @return an IOSupplier for the sheet.
     */
    public static IOSupplier<InputStream> getStyleSheet(final StyleSheets style) {
        return getStyleSheet(style.arg());
    }

    /**
     * Get the IOSupplier for a style sheet.
     * @param name the short name for or the path to a style sheet.
     * @return the IOSupplier for the style sheet.
     */
    public static IOSupplier<InputStream> getStyleSheet(final String name) {
        URL url = Report.class.getClassLoader().getResource(String.format("org/apache/rat/%s.xsl", name));
        return url == null
                ? () -> Files.newInputStream(Paths.get(name))
                : url::openStream;
    }
}
