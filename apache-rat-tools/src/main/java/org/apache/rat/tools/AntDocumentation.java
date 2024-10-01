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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.ConfigurationException;
import org.apache.rat.DeprecationReporter;
import org.apache.rat.OptionCollection;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.commandline.Arg;
import org.apache.rat.help.AbstractHelp;
import org.apache.rat.utils.DefaultLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * A simple tool to convert CLI options into an Ant report base class.
 */
public final class AntDocumentation {

    private final ReportConfiguration config;
    private final File outputDir;

    /**
     * Creates apt documentation fileis for Ant.
     * Requires 1 argument:
     * <ol>
     *     <li>the directory in which to write the documentation files.</li>
     * </ol>
     * @param args the arguments.
     * @throws IOException on error
     */
    public static void main(final String[] args) throws IOException {
        // process the command line to get the output directory.
        Options opts = OptionCollection.buildOptions();
        CommandLine commandLine;
        try {
            commandLine = DefaultParser.builder().setDeprecatedHandler(DeprecationReporter.getLogReporter())
                    .setAllowPartialMatching(true).build().parse(opts, args);
        } catch (ParseException e) {
            DefaultLog.getInstance().error(e.getMessage());
            DefaultLog.getInstance().error("Please use the \"--help\" option to see a list of valid commands and options", e);
            throw new ConfigurationException(e);
        }

        String[] remainingArgs = commandLine.getArgs();
        if (remainingArgs.length == 0) {
            System.err.println("Output directory must be specified");
            printUsage(opts);
            System.exit(1);
        }
        File outputDir = new File(remainingArgs[0]);
        if (outputDir.exists()) {
            if (!outputDir.isDirectory()) {
                DefaultLog.getInstance().error(format("%s is not a directory", remainingArgs[0]));
                System.exit(1);
            }
        } else {
            if (!outputDir.mkdirs()) {
                DefaultLog.getInstance().error(format("Can not create directory %s", remainingArgs[0]));
                System.exit(1);
            }
        }
        // remove any excess arguments and create the configuration.
        List<String> argsList = new ArrayList<>();
        argsList.addAll(Arrays.asList(args));
        argsList.removeAll(Arrays.asList(remainingArgs));

        ReportConfiguration config = OptionCollection.parseCommands(argsList.toArray(new String[0]), AntDocumentation::printUsage, true);
        if (config != null) {
            new AntDocumentation(config, outputDir).execute();
        }
    }

    private static void printUsage(final Options opts) {
        HelpFormatter f = new HelpFormatter();
        f.setOptionComparator(OptionCollection.optionComparator);
        f.setWidth(AbstractHelp.HELP_WIDTH);
        String header = "\nAvailable options";
        String footer = "";
        String cmdLine = format("java -jar apache-rat/target/apache-rat-CURRENT-VERSION.jar %s",
                AntDocumentation.class.getName());
        f.printHelp(cmdLine, header, opts, footer, false);
        System.exit(0);
    }

    private AntDocumentation(ReportConfiguration config, File outputDir) {
        this.config = config;
        this.outputDir = outputDir;
    }

    public void execute() throws IOException {

        List<AntOption> options = Arg.getOptions().getOptions().stream().filter(AntGenerator.getFilter()).map(AntOption::new)
                .collect(Collectors.toList());

        writeAttributes(options);
        writeElements(options);
        printValueTypes();
    }

    public void writeAttributes(List<AntOption> options) {
        File f = new File(outputDir, "report_attributes.txt");
        try (Writer out = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
            printOptions(out, options, AntOption::isAttribute,
                    "The attribute value types are listed in a table at the bottom of this page.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeElements(List<AntOption> options) {
        File f = new File(outputDir, "report_elements.txt");
        try (Writer out = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {

            printOptions(out, options, AntOption::isElement,
                    "The element value types are listed in a table at the bottom of this page.");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void printOptions(Writer out, List<AntOption> options, Predicate<AntOption> typeFilter, String tableCaption) throws IOException {
        boolean hasDeprecated = options.stream().anyMatch(typeFilter.and(AntOption::isDeprecated));

        if (hasDeprecated) {
            AptFormat.writeHeader(out, 2, "Current");
        }

        List<List<String>> table = new ArrayList<>();
        table.add(Arrays.asList("Name", "Description", "Value Type", "Required"));
        options.stream().filter(typeFilter.and(o -> !o.isDeprecated()))
                .map( o -> Arrays.asList( o.getName(), o.getDescription(),
                        o.hasArg() ? StringUtils.defaultIfEmpty(o.getArgName(), "String") : "boolean",
                        o.isRequired() ? "true" : "false"))
                .forEach(table::add);

        AptFormat.writeTable(out, table, "*--+--+--+--+", tableCaption);

        if (hasDeprecated) {
            AptFormat.writeHeader(out, 2, "Deprecated ");

            table.clear();
            table.add(Arrays.asList("Name", "Description", "Argument Type", "Deprecated"));

            options.stream().filter(typeFilter.and(AntOption::isDeprecated))
                    .map( o -> Arrays.asList( o.getName(), o.getDescription(),
                            o.hasArg() ? StringUtils.defaultIfEmpty(o.getArgName(), "String") : "boolean",
                            o.getDeprecated()))
                    .forEach(table::add);

            AptFormat.writeTable(out, table, "*--+--+--+--+", tableCaption);
        }
    }

    private void printValueTypes() throws IOException {

        File f = new File(outputDir, "report_arg_types.txt");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {

        List<List<String>> table = new ArrayList<>();
        table.add(Arrays.asList("Value Type", "Description"));

        for (Map.Entry<String, Supplier<String>> argInfo : OptionCollection.getArgumentTypes().entrySet()) {
            table.add(Arrays.asList(argInfo.getKey(), argInfo.getValue().get()));
        }

        AptFormat.writeTable(writer, table, "*--+--+");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class AptFormat  {

        public static void writeLicense(Writer writer) throws IOException {
            try (InputStream in = AntDocumentation.class.getResourceAsStream("/license.apt")) {
                IOUtils.copy(in, writer);
            }
        }

        public static void writeTitle(Writer writer, String title) throws IOException {
            writer.write(format("        -----%n        %1$s%n        -----%n%n%1$s%n%n", title));
        }

        public static void writePara(Writer writer, String paragraph) throws IOException {
            writer.write(format("  %s%n%n", paragraph));
        }

        public static void writeHeader(Writer writer, int level, String text) throws IOException {
            writer.write(System.lineSeparator());
            for (int i = 0; i < level; i++) {
                writer.write("*");
            }
            writer.write(format(" %s%n%n", text));
        }

        public static void writeList(Writer writer, Collection<String> list) throws IOException {
            for (String s : list) {
                writer.write(format("    * %s%n", s));
            }
            writer.write(System.lineSeparator());
        }

        public static void writeTable(Writer writer, Collection<? extends Collection<String>> table, String pattern, String caption) throws IOException {
            writer.write(format("%s%n", pattern));
            for (Collection<String> row : table) {
                for (String cell : row) {
                    writer.write(format("| %s ", cell ));
                }
                writer.write(format("|%n%s%n", pattern));
            }
            if (caption != null) {
                writer.write(caption);
            }
            writer.write(System.lineSeparator());
        }

        public static void writeTable(Writer writer, Collection<? extends Collection<String>> table, String pattern) throws IOException {
            writeTable(writer, table, pattern, null);
        }
    }
}
