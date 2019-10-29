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
package org.apache.rat.cli;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.Defaults;
import org.apache.rat.Report;
import org.apache.rat.ReportConfiguration;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class RatCommandLine {
    private static final String EXCLUDE_CLI = "e";
    private static final String EXCLUDE_FILE_CLI = "E";
    private static final String STYLESHEET_CLI = "s";
    private static final String HELP = "h";

    public static final void main(String[] args) throws Exception {
        final ReportConfiguration configuration = new ReportConfiguration();
        configuration.setHeaderMatcher(Defaults.createDefaultMatcher());
        configuration.setApproveDefaultLicenses(true);
        Options opts = buildOptions();

        CommandLine cl = null;
        try {
            cl = new DefaultParser().parse(opts, args);
        } catch (ParseException e) {
            System.err.println("Please use the \"--help\" option to see a list of valid commands and options");
            System.exit(1);
            return; // dummy return (won't be reached) to avoid Eclipse complaint about possible NPE for "cl"
        }

        if (cl.hasOption(HELP)) {
            printUsage(opts);
            System.exit(0);
        }

        args = cl.getArgs();
        if (args == null || args.length != 1) {
            printUsage(opts);
            System.exit(0);
        } else {
            Report report = new Report(new File(args[0]));

            if (cl.hasOption('a') || cl.hasOption('A')) {
                configuration.setAddingLicenses(true);
                configuration.setAddingLicensesForced(cl.hasOption('f'));
                configuration.setCopyrightMessage(cl.getOptionValue("c"));
            }

            if (cl.hasOption(EXCLUDE_CLI)) {
                String[] excludes = cl.getOptionValues(EXCLUDE_CLI);
                if (excludes != null) {
                    final FilenameFilter filter = parseExclusions(Arrays.asList(excludes));
                    report.setInputFileFilter(filter);
                }
            } else if (cl.hasOption(EXCLUDE_FILE_CLI)) {
                String excludeFileName = cl.getOptionValue(EXCLUDE_FILE_CLI);
                if (excludeFileName != null) {
                    final FilenameFilter filter = parseExclusions(FileUtils.readLines(new File(excludeFileName), Charset.forName("UTF-8")));
                    report.setInputFileFilter(filter);
                }
            }
            if (cl.hasOption('x')) {
                report.report(System.out, configuration);
            } else {
                if (!cl.hasOption(STYLESHEET_CLI)) {
                    report.styleReport(System.out, configuration);
                } else {
                    String[] style = cl.getOptionValues(STYLESHEET_CLI);
                    if (style.length != 1) {
                        System.err.println("please specify a single stylesheet");
                        System.exit(1);
                    }
                    try {
                        report.report(System.out,
                                report.getDirectory(System.out),
                                new FileInputStream(style[0]),
                                configuration);
                    } catch (FileNotFoundException fnfe) {
                        System.err.println("stylesheet " + style[0]
                                + " doesn't exist");
                        System.exit(1);
                    }
                }
            }
        }
    }

    static FilenameFilter parseExclusions(List<String> excludes) throws IOException {
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
                // interpret given patterns as regular expression, direct file names or wildcards to give users more choices to configure exclusions
                orFilter.addFileFilter(new RegexFileFilter(exclusion));
                orFilter.addFileFilter(new NameFileFilter(exclusion));
                orFilter.addFileFilter(new WildcardFileFilter(exclusion));
            } catch (PatternSyntaxException e) {
                System.err.println("Will skip given exclusion '" + exclude + "' due to " + e);
            }
        }
        System.out.println("Ignored " + ignoredLines + " lines in your exclusion files as comments or empty lines.");
        return new NotFileFilter(orFilter);
    }


    private static Options buildOptions() {
        Options opts = new Options();

        Option help = new Option(HELP, "help", false,
                "Print help for the RAT command line interface and exit");
        opts.addOption(help);

        OptionGroup addLicenseGroup = new OptionGroup();
        String addLicenseDesc = "Add the default license header to any file with an unknown license that is not in the exclusion list. " +
                "By default new files will be created with the license header, " +
                "to force the modification of existing files use the --force option.";

        // RAT-85/RAT-203: Deprecated! added only for convenience and for backwards compatibility
        Option addLicence = new Option(
                "a",
                "addLicence",
                false,
                addLicenseDesc);
        addLicenseGroup.addOption(addLicence);
        Option addLicense = new Option(
                "A",
                "addLicense",
                false,
                addLicenseDesc);
        addLicenseGroup.addOption(addLicense);
        opts.addOptionGroup(addLicenseGroup);

        Option write = new Option(
                "f",
                "force",
                false,
                "Forces any changes in files to be written directly to the source files (i.e. new files are not created)");
        opts.addOption(write);

        Option copyright = new Option(
                "c",
                "copyright",
                true,
                "The copyright message to use in the license headers, usually in the form of \"Copyright 2008 Foo\"");
        opts.addOption(copyright);

        final Option exclude = Option.builder(EXCLUDE_CLI)
                .argName("expression")
                .longOpt("exclude")
                .hasArgs()
                .desc("Excludes files matching wildcard <expression>. " +
                        "Note that --dir is required when using this parameter. " +
                        "Allows multiple arguments.")
                .build();
        opts.addOption(exclude);

        final Option excludeFile = Option.builder(EXCLUDE_FILE_CLI)
                .argName("fileName")
                .longOpt("exclude-file")
                .hasArgs()
                .desc("Excludes files matching regular expression in <file> " +
                        "Note that --dir is required when using this parameter. ")
                .build();
        opts.addOption(excludeFile);

        Option dir = new Option(
                "d",
                "dir",
                false,
                "Used to indicate source when using --exclude");
        opts.addOption(dir);

        OptionGroup outputType = new OptionGroup();

        Option xml = new Option(
                "x",
                "xml",
                false,
                "Output the report in raw XML format.  Not compatible with -s");
        outputType.addOption(xml);

        Option xslt = new Option(STYLESHEET_CLI,
                "stylesheet",
                true,
                "XSLT stylesheet to use when creating the"
                        + " report.  Not compatible with -x");
        outputType.addOption(xslt);
        opts.addOptionGroup(outputType);

        return opts;
    }

    private static void printUsage(Options opts) {
        HelpFormatter f = new HelpFormatter();
        String header = "\nAvailable options";

        String footer = "\nNOTE:\n" +
                "Rat is really little more than a grep ATM\n" +
                "Rat is also rather memory hungry ATM\n" +
                "Rat is very basic ATM\n" +
                "Rat highlights possible issues\n" +
                "Rat reports require interpretation\n" +
                "Rat often requires some tuning before it runs well against a project\n" +
                "Rat relies on heuristics: it may miss issues\n";

        f.printHelp("java -jar apache-rat/target/apache-rat-CURRENT-VERSION.jar [options] [DIR|TARBALL]",
                header, opts, footer, false);
    }

}
