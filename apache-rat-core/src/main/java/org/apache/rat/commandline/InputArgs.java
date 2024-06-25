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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DeprecatedAttributes;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.utils.Log;

/**
 * Handles arguments that adjust the input files.
 * @since 0.17
 */
public final class InputArgs {

    /** Excludes files by expression */
    private static final OptionGroup EXCLUDE = new OptionGroup()
            .addOption(Option.builder("e").longOpt("exclude").hasArgs().argName("Expression")
                    .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                            .setDescription("Use '--input-exclude' instead.").get())
            .build())
            .addOption(Option.builder().longOpt("input-exclude").hasArgs().argName("Expression")
                    .desc("Excludes files matching wildcard <Expression>. May be followed by multiple arguments. "
                            + "Note that '--' or a following option is required when using this parameter.")
                    .build());

    /** Excludes files based on content of file */
    private static final OptionGroup EXCLUDE_FILE = new OptionGroup()
            .addOption(Option.builder("E").longOpt("exclude-file")
            .argName("File")
            .hasArg()
                    .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                            .setDescription("Use '--input-exclude-file' instead.").get())
            .build())
            .addOption(Option.builder().longOpt("input-exclude-file")
                    .argName("File")
                    .hasArg().desc("Excludes files matching regular expression in the input file.")
                    .build());

    /** Scan hidden directories */
    private static final Option SCAN_HIDDEN_DIRECTORIES = new Option(null, "scan-hidden-directories", false,
            "Scan hidden directories");

    /** Stop processing an input stream and declare an input file */
    public static final Option DIR = Option.builder().option("d").longOpt("dir").hasArg()
            .desc("Used to indicate end of list when using --exclude.").argName("DirOrArchive")
            .deprecated(DeprecatedAttributes.builder().setForRemoval(true).setSince("0.17")
                    .setDescription("Use '--'").get()).build();

    private InputArgs() {
        // do not instantiate
    }
    /**
     * Adds the options from this set of options to the options argument.
     * @param options the options to add to.
     */
    public static void addOptions(final Options options) {
        options.addOptionGroup(EXCLUDE)
                .addOptionGroup(EXCLUDE_FILE)
                .addOption(SCAN_HIDDEN_DIRECTORIES)
                .addOption(DIR);
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
    public static void processArgs(final ArgumentContext ctxt) throws IOException {
        if (ctxt.getCommandLine().hasOption(SCAN_HIDDEN_DIRECTORIES)) {
            ctxt.getConfiguration().setDirectoriesToIgnore(FalseFileFilter.FALSE);
        }

        // TODO when include/exclude processing is updated check calling methods to ensure that all specified
        // directories are handled in the list of directories.
        if (EXCLUDE.getSelected() != null) {
            String[] excludes = ctxt.getCommandLine().getOptionValues(EXCLUDE.getSelected());
            if (excludes != null) {
                parseExclusions(ctxt.getConfiguration().getLog(), Arrays.asList(excludes)).ifPresent(ctxt.getConfiguration()::setFilesToIgnore);
            }
        }
        if (EXCLUDE_FILE.getSelected() != null) {
            String excludeFileName = ctxt.getCommandLine().getOptionValue(EXCLUDE_FILE.getSelected());
            if (excludeFileName != null) {
                parseExclusions(ctxt.getConfiguration().getLog(), FileUtils.readLines(new File(excludeFileName), StandardCharsets.UTF_8))
                        .ifPresent(ctxt.getConfiguration()::setFilesToIgnore);
            }
        }
    }

    /**
     * Returns the DIR value from the command line.
     * @param commandLine the Command line to process.
     * @return the --dir value
     */
    public static String getDirValue(CommandLine commandLine) {
        return commandLine.getOptionValue(InputArgs.DIR);
    }
}
