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
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Converter;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.DeprecatedAttributes;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.function.IOSupplier;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.rat.api.Document;
import org.apache.rat.config.AddLicenseHeaders;
import org.apache.rat.document.impl.FileDocument;
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
public final class Report {

    /** The width of the help report in chars. */
    private static final int HELP_WIDTH = 120;
    /** The number of chars to indent output with */
    private static final int HELP_PADDING = 5;

    /**
     * An array of notes to go at the bottom of the help output
     */
    private static final String[] NOTES = {
            "Rat highlights possible issues.",
            "Rat reports require interpretation.",
            "Rat often requires some tuning before it runs well against a project.",
            "Rat relies on heuristics: it may miss issues"
    };

    /**
     * Processes the command line and builds a configuration and executes the
     * report.
     *
     * @param args the arguments.
     * @throws Exception on error.
     */
    public static void main(final String[] args) throws Exception {
        DefaultLog.getInstance().info(new VersionInfo().toString());
        ReportConfiguration configuration = OptionCollection.parseCommands(args, Report::printUsage);
        if (configuration != null) {
            configuration.validate(DefaultLog.getInstance()::error);
            new Reporter(configuration).output();
        }
    }

    /**
     * Prints the usage message on System.out
     * @param opts The defined options.
     */
    private static void printUsage(final Options opts) {
        printUsage(new PrintWriter(System.out), opts);
    }

    /**
     * Create a padding.
     * @param len The length of the padding in characters.
     * @return a string with len blanks.
     */
    private static String createPadding(final int len) {
        char[] padding = new char[len];
        Arrays.fill(padding, ' ');
        return new String(padding);
    }

    /**
     * Create a section header for the output.
     * @param txt the text to put in the header.
     * @return the Header string.
     */
    static String header(final String txt) {
        return String.format("%n====== %s ======%n", WordUtils.capitalizeFully(txt));
    }

    /** Function to format deprecated display */
    private static final Function<Option, String> DEPRECATED_MSG = o -> {
        StringBuilder sb = new StringBuilder("[").append(o.getDeprecated().toString()).append("]");
        if (o.getDescription() != null) {
            sb.append(" ").append(o.getDescription());
        }
        return sb.toString();
    };

    /**
     * Print the usage to the specific PrintWriter.
     * @param writer the PrintWriter to output to.
     * @param opts The defined options.
     */
    static void printUsage(final PrintWriter writer, final Options opts) {
        HelpFormatter helpFormatter = new HelpFormatter.Builder().setShowDeprecated(DEPRECATED_MSG).get();
        helpFormatter.setWidth(HELP_WIDTH);
        helpFormatter.setOptionComparator(new OptionCollection.OptionComparator());
        VersionInfo versionInfo = new VersionInfo();
        String syntax = format("java -jar apache-rat/target/apache-rat-%s.jar [options] [DIR|ARCHIVE]", versionInfo.getVersion());
        helpFormatter.printHelp(writer, helpFormatter.getWidth(), syntax, header("Available options"), opts,
                helpFormatter.getLeftPadding(), helpFormatter.getDescPadding(),
                header("Argument Types"), false);

        String argumentPadding = createPadding(helpFormatter.getLeftPadding() + HELP_PADDING);
        for (Map.Entry<String, Supplier<String>> argInfo : OptionCollection.getArgumentTypes().entrySet()) {
            writer.format("%n<%s>%n", argInfo.getKey());
            helpFormatter.printWrapped(writer, helpFormatter.getWidth(), helpFormatter.getLeftPadding() + HELP_PADDING + HELP_PADDING,
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
    private static IReportable getDirectory(final String baseDirectory, final ReportConfiguration config) {
        File base = new File(baseDirectory);

        if (!base.exists()) {
            config.getLog().log(Level.ERROR, "Directory '" + baseDirectory + "' does not exist");
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
    public static class OptionComparator implements Comparator<Option>, Serializable {
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
