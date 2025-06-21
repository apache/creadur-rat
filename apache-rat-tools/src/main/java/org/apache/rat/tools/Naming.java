/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.tools;

import java.io.CharArrayWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.OptionCollection;
import org.apache.rat.documentation.options.AntOption;
import org.apache.rat.documentation.options.MavenOption;
import org.apache.rat.help.AbstractHelp;

/**
 * A simple tool to convert CLI options to Maven and Ant format and produce a CSV file.
 * <br>
 * Options
 * <ul>
 *     <li>--ant   Produces Ant options in result</li>
 *     <li>--maven Produces Maven options in result</li>
 *     <li>--csv   Produces CSV output text</li>
 * </ul>
 * Note: if neither --ant nor --maven are included both will be listed.
 */
public final class Naming {

    private Naming() { }
    /** The maximum width of the output. */
    private static final Option WIDTH = Option.builder().longOpt("width").type(Integer.class)
            .desc("Set the display width of the output").hasArg().build();
    /** Option to output Maven names. */
    private static final Option MAVEN = Option.builder().longOpt("maven").desc("Produce Maven name mapping").build();
    /** Option to output Ant names. */
    private static final Option ANT = Option.builder().longOpt("ant").desc("Produce Ant name mapping").build();
    /** Option to output CSV format. */
    private static final Option CSV = Option.builder().longOpt("csv").desc("Produce CSV format").build();
    /** Options to output cli names. */
    private static final Option CLI = Option.builder().longOpt("cli").desc("Produce CLI name mapping").build();
    /** Option for including deprecated options. */
    private static final Option INCLUDE_DEPRECATED = Option.builder().longOpt("include-deprecated")
            .desc("Include deprecated options.").build();
    /** The all option. */
    private static final Options OPTIONS = new Options().addOption(MAVEN).addOption(ANT).addOption(CLI)
            .addOption(CSV)
            .addOption(INCLUDE_DEPRECATED)
            .addOption(WIDTH);

    /**
     * Creates the CSV file.
     * Requires 1 argument:
     * <ol>
     *    <li>the name of the output file with path if desired</li>
     * </ol>
     * @throws IOException on error
     * @param args arguments, only 1 is required.
     */
    public static void main(final String[] args) throws IOException, ParseException {
        if (args == null || args.length < 1) {
            System.err.println("At least one argument is required: path to file is missing.");
            return;
        }
        CommandLine cl = DefaultParser.builder().build().parse(OPTIONS, args);
        int width = Math.max(cl.getParsedOptionValue(WIDTH, AbstractHelp.HELP_WIDTH), AbstractHelp.HELP_WIDTH);

        boolean showMaven = cl.hasOption(MAVEN);

        boolean showAnt = cl.hasOption(ANT);
        boolean includeDeprecated = cl.hasOption(INCLUDE_DEPRECATED);
        Predicate<Option> filter = o -> o.hasLongOpt() && (!o.isDeprecated() || includeDeprecated);

        List<String> columns = new ArrayList<>();

        if (cl.hasOption(CLI)) {
            columns.add("CLI");
        }

        if (showAnt) {
            columns.add("Ant");
        }

        if (showMaven) {
            columns.add("Maven");
        }
        columns.add("Description");
        columns.add("Argument Type");

        Function<Option, String> descriptionFunction;

        if (cl.hasOption(CLI) || !showAnt && !showMaven) {
            descriptionFunction = o -> {
                StringBuilder desc = new StringBuilder();
            if (o.isDeprecated()) {
                desc.append("[").append(o.getDeprecated().toString()).append("] ");
            }
            return desc.append(StringUtils.defaultIfEmpty(o.getDescription(), "")).toString();
            };
        } else if (showAnt) {
            descriptionFunction = o -> {
                StringBuilder desc = new StringBuilder();
                AntOption antOption = new AntOption(o);
                if (antOption.isDeprecated()) {
                    desc.append("[").append(antOption.getDeprecated()).append("] ");
                }
                return desc.append(StringUtils.defaultIfEmpty(antOption.getDescription(), "")).toString();
            };
        } else {
            descriptionFunction = o -> {
                StringBuilder desc = new StringBuilder();
                MavenOption mavenOption = new MavenOption(o);
                if (mavenOption.isDeprecated()) {
                    desc.append("[").append(mavenOption.getDeprecated()).append("] ");
                }
                return desc.append(StringUtils.defaultIfEmpty(mavenOption.getDescription(), "")).toString();
            };
        }

        try (Writer underWriter = cl.getArgs().length != 0 ? new FileWriter(cl.getArgs()[0]) : new OutputStreamWriter(System.out, StandardCharsets.UTF_8)) {
            if (cl.hasOption(CSV)) {
                printCSV(columns, filter, cl.hasOption(CLI), showMaven, showAnt, descriptionFunction, underWriter);
            }
            else {
                printText(columns, filter, cl.hasOption(CLI), showMaven, showAnt, descriptionFunction, underWriter, width);
            }
        }
    }

    private static List<String> fillColumns(final List<String> columns, final Option option, final boolean addCLI, final boolean showMaven,
                                            final boolean showAnt, final Function<Option, String> descriptionFunction) {
        if (addCLI) {
            if (option.hasLongOpt()) {
                columns.add("--" + option.getLongOpt());
            } else {
                columns.add("-" + option.getOpt());
            }
        }
        if (showAnt) {
            columns.add(new AntOption(option).getExample());
        }
        if (showMaven) {
            columns.add(new MavenOption(option).getExample());
        }

        columns.add(descriptionFunction.apply(option));
        columns.add(option.hasArgName() ? option.getArgName() : option.hasArgs() ? "Strings" : option.hasArg() ? "String" : "-- none --");
        columns.add(option.hasArgName() ? option.getArgName() : option.hasArgs() ? "Strings" : option.hasArg() ? "String" : "-- none --");
        columns.add(option.hasArgName() ? option.getArgName() : option.hasArgs() ? "Strings" : option.hasArg() ? "String" : "-- none --");
        return columns;
    }

    private static void printCSV(final List<String> columns, final Predicate<Option> filter, final boolean addCLI, final boolean showMaven,
                                 final boolean showAnt, final Function<Option, String> descriptionFunction,
                                 final Writer underWriter) throws IOException {
        try (CSVPrinter printer = new CSVPrinter(underWriter, CSVFormat.DEFAULT.builder().setQuoteMode(QuoteMode.ALL).get())) {
            printer.printRecord(columns);
            for (Option option : OptionCollection.buildOptions().getOptions()) {
                if (filter.test(option)) {
                    columns.clear();
                    printer.printRecord(fillColumns(columns, option, addCLI, showMaven, showAnt, descriptionFunction));
                }
            }
        }
    }

    private static int[] calculateColumnWidth(final int width, final int columnCount, final List<List<String>> page) {
        int[] columnWidth = new int[columnCount];
        for (List<String> row : page) {
            for (int i = 0; i < columnCount; i++) {
                columnWidth[i] = Math.max(columnWidth[i], row.get(i).length());
            }
        }
        int extra = 0;
        int averageWidth = (width - ((columnCount - 1) * 2)) / columnCount;
        int[] overage = new int[columnCount];
        int totalOverage = 0;
        for (int i = 0; i < columnCount; i++) {
            if (columnWidth[i] < averageWidth) {
                extra += averageWidth - columnWidth[i];
            } else if (columnWidth[i] > averageWidth) {
                overage[i] = columnWidth[i] - averageWidth;
                totalOverage += overage[i];
            }
        }

        for (int i = 0; i < columnCount; i++) {
            if (overage[i] > 0) {
                int addl = (int) (extra * overage[i] * 1.0 / totalOverage);
                columnWidth[i] = averageWidth + addl;
            }
        }
        return columnWidth;
    }

    private static void printText(final List<String> columns, final Predicate<Option> filter, final boolean addCLI,
                                  final boolean showMaven, final boolean showAnt,
                                  final Function<Option, String> descriptionFunction, final Writer underWriter, final int width) throws IOException {
        List<List<String>> page = new ArrayList<>();

        int columnCount = columns.size();
        page.add(columns);

        for (Option option : OptionCollection.buildOptions().getOptions()) {
            if (filter.test(option)) {
                page.add(fillColumns(new ArrayList<>(), option, addCLI, showMaven, showAnt, descriptionFunction));
            }
        }
        int[] columnWidth = calculateColumnWidth(width, columnCount, page);
        HelpFormatter helpFormatter;
        helpFormatter = new HelpFormatter.Builder().get();
        helpFormatter.setWidth(width);


        List<Deque<String>> entries = new ArrayList<>();
        CharArrayWriter cWriter = new CharArrayWriter();

        // process one line at a time
        for (List<String> cols : page) {
            entries.clear();
            PrintWriter writer = new PrintWriter(cWriter);
            // print each column into a block of strings.
            for (int i = 0; i < columnCount; i++) {
                String col = cols.get(i);
                // split on end of line within a column
                for (String line : col.split("\\v")) {
                    helpFormatter.printWrapped(writer, columnWidth[i], 2, line);
                }
                writer.flush();
                // please the block of strings into a queue.
                Deque<String> entryLines = new LinkedList<>(Arrays.asList(cWriter.toString().split("\\v")));
                // put the queue into the entries for this line.
                entries.add(entryLines);
                cWriter.reset();
            }
            // print the entries by printing the items from the queues until all queues are empty.
            boolean cont = true;
            while (cont) {
                cont = false;
                for (int columnNumber = 0; columnNumber < entries.size(); columnNumber++) {
                    Deque<String> queue = entries.get(columnNumber);
                    if (queue.isEmpty()) {
                        underWriter.append(AbstractHelp.createPadding(columnWidth[columnNumber] + 2));
                    } else {
                        String ln = queue.pop();
                        underWriter.append(ln);
                        underWriter.append(AbstractHelp.createPadding(columnWidth[columnNumber] - ln.length() + 2));
                        if (!queue.isEmpty()) {
                            cont = true;
                        }
                    }
                }
                underWriter.append(System.lineSeparator());
            }
            underWriter.append(System.lineSeparator());
        }
    }
}
