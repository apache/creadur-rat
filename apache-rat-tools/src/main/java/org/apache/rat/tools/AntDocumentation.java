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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;


import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.OptionCollection;
import org.apache.rat.documentation.options.AntOption;
import org.apache.rat.utils.DefaultLog;

import static java.lang.String.format;

/**
 * A simple tool to convert CLI options into an Ant report base class.
 */
public final class AntDocumentation {
    /** The directory to write to. */
    private final File outputDir;

    /**
     * Creates apt documentation files for Ant.
     * Requires 1 argument:
     * <ol>
     *     <li>the directory in which to write the documentation files.</li>
     * </ol>
     * @param args the arguments.
     */
    public static void main(final String[] args) {

        if (args.length == 0) {
            System.err.println("Output directory must be specified");
            System.exit(1);
        }
        File outputDir = new File(args[0]);
        if (outputDir.exists()) {
            if (!outputDir.isDirectory()) {
                DefaultLog.getInstance().error(format("%s is not a directory", args[0]));
                System.exit(1);
            }
        } else {
            if (!outputDir.mkdirs()) {
                DefaultLog.getInstance().error(format("Can not create directory %s", args[0]));
                System.exit(1);
            }
        }
        new AntDocumentation(outputDir).execute();
    }

   private AntDocumentation(final File outputDir) {
        this.outputDir = outputDir;
    }

    public void execute() {
        List<AntOption> options = AntOption.getAntOptions();

        writeAttributes(options);
        writeElements(options);
        printValueTypes();
    }

    public void writeAttributes(final List<AntOption> options) {
        File f = new File(outputDir, "report_attributes.txt");
        try (Writer out = new OutputStreamWriter(Files.newOutputStream(f.toPath()), StandardCharsets.UTF_8)) {
            printOptions(out, options, AntOption::isAttribute,
                    "The attribute value types are listed in a table at the bottom of this page.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeElements(final List<AntOption> options) {
        File f = new File(outputDir, "report_elements.txt");
        try (Writer out = new OutputStreamWriter(Files.newOutputStream(f.toPath()), StandardCharsets.UTF_8)) {
            printOptions(out, options, AntOption::isElement,
                    "The element value types are listed in a table at the bottom of this page.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void printOptions(final Writer out, final List<AntOption> options,
                              final Predicate<AntOption> typeFilter, final String tableCaption) throws IOException {
        boolean hasDeprecated = options.stream().anyMatch(typeFilter.and(AntOption::isDeprecated));

        if (hasDeprecated) {
            AptFormat.writeHeader(out, 2, "Current");
        }

        List<List<String>> table = new ArrayList<>();
        table.add(Arrays.asList("Name", "Description", "Value Type", "Required"));
        options.stream().filter(typeFilter.and(o -> !o.isDeprecated()))
                .map(o -> Arrays.asList(o.getName(), o.getDescription(),
                        o.hasArg() ? StringUtils.defaultIfEmpty(o.getArgName(), "String") : "boolean",
                        o.isRequired() ? "true" : "false"))
                .forEach(table::add);

        AptFormat.writeTable(out, table, "*--+--+--+--+", tableCaption);

        if (hasDeprecated) {
            AptFormat.writeHeader(out, 2, "Deprecated ");

            table.clear();
            table.add(Arrays.asList("Name", "Description", "Argument Type", "Deprecated"));

            options.stream().filter(typeFilter.and(AntOption::isDeprecated))
                    .map(o -> Arrays.asList(o.getName(), o.getDescription(),
                            o.hasArg() ? StringUtils.defaultIfEmpty(o.getArgName(), "String") : "boolean",
                            o.getDeprecated()))
                    .forEach(table::add);

            AptFormat.writeTable(out, table, "*--+--+--+--+", tableCaption);
        }
    }

    private void printValueTypes() {

        File f = new File(outputDir, "report_arg_types.txt");
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(f.toPath()), StandardCharsets.UTF_8)) {

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

    /**
     * A class to write APT formatted text.
     */
    private static final class AptFormat  {

        /**
         * Copy the "license.apt" from the resources to the writer.
         * @param writer the writer to write to.
         * @throws IOException on error.
         */
        public static void writeLicense(final Writer writer) throws IOException {
            try (InputStream in = AntDocumentation.class.getResourceAsStream("/license.apt")) {
                if (in == null) {
                    throw new FileNotFoundException("Could not find license.apt");
                }
                IOUtils.copy(in, writer, StandardCharsets.UTF_8);
            }
        }

        /**
         * Write a title.
         * @param writer the writer to write to.
         * @param title the title to write.
         * @throws IOException on error.
         */
        public static void writeTitle(final Writer writer, final String title) throws IOException {
            writer.write(format("        -----%n        %1$s%n        -----%n%n%1$s%n%n", title));
        }

        /**
         * Write a paragraph.
         * @param writer the writer to write to.
         * @param paragraph the paragraph to write.
         * @throws IOException on error.
         */
        public static void writePara(final Writer writer, final String paragraph) throws IOException {
            writer.write(format("  %s%n%n", paragraph));
        }

        /**
         * Write a header.
         * @param writer the writer to write to.
         * @param level the level of the header
         * @param text the text for the header
         * @throws IOException on error.
         */
        public static void writeHeader(final Writer writer, final int level, final String text) throws IOException {
            writer.write(System.lineSeparator());
            for (int i = 0; i < level; i++) {
                writer.write("*");
            }
            writer.write(format(" %s%n%n", text));
        }

        /**
         * Write a list.
         * @param writer the writer to write to.
         * @param list the list to write.
         * @throws IOException on error.
         */
        public static void writeList(final Writer writer, final Collection<String> list) throws IOException {
            for (String s : list) {
                writer.write(format("    * %s%n", s));
            }
            writer.write(System.lineSeparator());
        }

        /**
         * Write a table.
         * @param writer the Writer to write to.
         * @param table the Table to write. A collection of collections of Strings.
         * @param pattern the pattern before and after the table.
         * @param caption the caption for the table.
         * @throws IOException on error.
         */
        public static void writeTable(final Writer writer, final Collection<? extends Collection<String>> table,
                                      final String pattern, final String caption) throws IOException {
            writer.write(format("%s%n", pattern));
            for (Collection<String> row : table) {
                for (String cell : row) {
                    writer.write(format("| %s ", cell));
                }
                writer.write(format("|%n%s%n", pattern));
            }
            if (caption != null) {
                writer.write(caption);
            }
            writer.write(System.lineSeparator());
        }

        /**
         * Write a table entry.
         * @param writer the Writer to write to.
         * @param table the Table to write
         * @param pattern the pattern before and after the table.
         * @throws IOException on error
         */
        public static void writeTable(final Writer writer, final Collection<? extends Collection<String>> table,
                                      final String pattern) throws IOException {
            writeTable(writer, table, pattern, null);
        }
    }
}
