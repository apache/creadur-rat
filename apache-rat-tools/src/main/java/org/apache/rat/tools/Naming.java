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

import static java.lang.String.format;

import java.io.CharArrayWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
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
import org.apache.rat.DeprecationReporter;
import org.apache.rat.OptionCollection;
import org.apache.rat.help.AbstractHelp;

/**
 * A simple tool to convert CLI options to Maven and Ant format and produce a CSV file.
 *
 * Options
 * <ul>
 *     <li>--ant   Produces ant options in result</li>
 *     <li>--maven Produces maven options in result</li>
 *     <li>--csv   Produces CSV output text is produced</li>
 * </ul>
 * Note: if neither --ant nor --maven are included both will be listed.
 */
public final class Naming {

    private Naming() { }

    private static final Option WIDTH = Option.builder().longOpt("width").type(Integer.class)
            .desc("Set the display width of the output").hasArg().build();
    private static final Option MAVEN = Option.builder().longOpt("maven").desc("Produce Maven name mapping").build();
    private static final Option ANT = Option.builder().longOpt("ant").desc("Produce Ant name mapping").build();
    private static final Option CSV = Option.builder().longOpt("csv").desc("Produce CSV format").build();
    private static final Option CLI = Option.builder().longOpt("cli").desc("Produce CLI name mapping").build();
    private static final Option INCLUDE_DEPRECATED = Option.builder().longOpt("include-deprecated")
            .desc("Include deprecated options.").build();

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
        if(args == null || args.length < 1) {
            System.err.println("At least one argument is required: path to file is missing.");
            return;
        }
        CommandLine cl = DefaultParser.builder().build().parse(OPTIONS, args);
        int width = Math.max(cl.getParsedOptionValue(WIDTH, AbstractHelp.HELP_WIDTH), AbstractHelp.HELP_WIDTH);

        Predicate<Option> mavenFilter = cl.hasOption(MAVEN) ? MavenGenerator.getFilter() : null;

        Predicate<Option> antFilter = cl.hasOption(ANT) ? AntGenerator.getFilter() : null;
        boolean includeDeprecated = cl.hasOption(INCLUDE_DEPRECATED);
        Predicate<Option> filter = o -> o.hasLongOpt() && (!o.isDeprecated() || includeDeprecated);

        List<String> columns = new ArrayList<>();

        if (cl.hasOption(CLI)) {
            columns.add("CLI");
        }

        if (antFilter != null) {
            columns.add("Ant");
        }

        if (mavenFilter != null) {
            columns.add("Maven");
        }
        columns.add("Description");
        columns.add("Argument Type");

        try (Writer underWriter =cl.getArgs().length != 0 ? new FileWriter(cl.getArgs()[0]) : new OutputStreamWriter(System.out)) {
            if (cl.hasOption(CSV)) {
                printCSV(columns, filter, cl.hasOption(CLI), mavenFilter, antFilter, underWriter);
            }
            else {
                printText(columns, filter, cl.hasOption(CLI), mavenFilter, antFilter, underWriter, width);
            }
        }
    }

    private static List<String> fillColumns(List<String> columns, Option option, boolean addCLI, Predicate<Option> mavenFilter, Predicate<Option> antFilter) {
        if (addCLI) {
            if (option.hasLongOpt()) {
                columns.add("--" + option.getLongOpt());
            } else {
                columns.add("-" + option.getOpt());
            }
        }
        if (antFilter != null) {
            columns.add(antFilter.test(option) ? antFunctionName(option) : "-- not supported --");
        }
        if (mavenFilter != null) {
            columns.add(mavenFilter.test(option) ? mavenFunctionName(option) : "-- not supported --");
        }

        StringBuilder desc = new StringBuilder();
        if (option.isDeprecated()) {
            desc.append("[").append(option.getDeprecated().toString()).append("] ");
        }
        columns.add(desc.append(StringUtils.defaultIfEmpty(option.getDescription(),"")).toString());
        columns.add(option.hasArgName() ? option.getArgName() : option.hasArgs() ? "Strings" : option.hasArg() ? "String" : "-- none --");
        return columns;
    }

    private static void printCSV(List<String> columns, Predicate<Option> filter, boolean addCLI, Predicate<Option> mavenFilter, Predicate<Option> antFilter, Writer underWriter) throws IOException {
        try (CSVPrinter printer = new CSVPrinter(underWriter, CSVFormat.DEFAULT.builder().setQuoteMode(QuoteMode.ALL).build())) {
            printer.printRecord(columns);
            for (Option option : OptionCollection.buildOptions().getOptions()) {
                if (filter.test(option)) {
                    columns.clear();
                    printer.printRecord(fillColumns(columns, option, addCLI, mavenFilter, antFilter));
                }
            }
        }
    }

    private static int[] calculateColumnWidth(int width, int columnCount, List<List<String>> page) {
        int[] columnWidth = new int[columnCount];
        for (List<String> row : page) {
            for (int i=0; i<columnCount; i++) {
                columnWidth[i] = Math.max(columnWidth[i], row.get(i).length());
            }
        }
        int extra = 0;
        int averageWidth = (width - ((columnCount-1) * 2)) / columnCount;
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

    private static void printText(List<String> columns, Predicate<Option> filter, boolean addCLI, Predicate<Option> mavenFilter,
                                  Predicate<Option> antFilter, Writer underWriter, int width) throws IOException {
        List<List<String>> page = new ArrayList<>();

        int columnCount = columns.size();
        page.add(columns);

        for (Option option : OptionCollection.buildOptions().getOptions()) {
            if (filter.test(option)) {
                page.add(fillColumns(new ArrayList<>(), option, addCLI, mavenFilter, antFilter));
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
            for (int i=0; i<columnCount; i++) {
                String col = cols.get(i);
                // split on end of line within a column
                for (String line : col.split("\\v")) {
                    helpFormatter.printWrapped(writer, columnWidth[i], 2, line);
                }
                writer.flush();
                // please the block of strings into a queue.
                Deque<String> entryLines = new LinkedList<>();
                entryLines.addAll(Arrays.asList(cWriter.toString().split("\\v")));
                // put the queue into the entries for this line.
                entries.add(entryLines);
                cWriter.reset();
            }
            // print the entries by printing the items from the queues until all queues are empty.
            boolean cont = true;
            while (cont) {
                cont = false;
                for (int columnNumber = 0; columnNumber<entries.size(); columnNumber++) {
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

    public static String mavenFunctionName(final Option option) {
        MavenOption mavenOption = new MavenOption(option);
        StringBuilder sb = new StringBuilder();
        if (mavenOption.isDeprecated()) {
            sb.append("@Deprecated").append(System.lineSeparator());
        }
        return sb.append(format("<%s>", mavenOption.getName())).toString();
    }

    private static String antFunctionName(final Option option) {
        StringBuilder sb = new StringBuilder();
        AntOption antOption = new AntOption(option);
        if (option.isDeprecated()) {
            sb.append("@Deprecated").append(System.lineSeparator());
        }
        if (option.hasArgs()) {
            sb.append(format("<rat:report>%n  <%1$s>text</%1$s>%n</rat:report>", antOption.getName()));
        } else {
            sb.append(format("<rat:report %s = 'text'/>", antOption.getName()));
        }
        return sb.toString();
    }
}
