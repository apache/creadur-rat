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

import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Predicate;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.text.WordUtils;
import org.apache.commons.csv.CSVPrinter;
import org.apache.rat.OptionCollection;
import org.apache.rat.Report;
import org.apache.rat.tools.CasedString.StringCase;

/**
 * A simple tool to convert CLI options to Maven and Ant format and produce a CSV file.
 */
public final class Naming {

    private Naming() { }

    /**
     * Creates the CSV file.
     * Requires 1 argument:
     * <ol>
     *    <li>the name of the output file with path if desired</li>
     * </ol>
     * @throws IOException on error
     */
    public static void main(final String[] args) throws IOException {
        Options options = OptionCollection.buildOptions();
        Predicate<Option> mavenFilter = MavenGenerator.getFilter();
        Predicate<Option> antFilter = AntGenerator.getFilter();
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(args[0]), CSVFormat.DEFAULT)) {
            printer.printRecord("CLI", "Maven", "Ant", "Description");
            for (Option option : options.getOptions()) {
                if (option.getLongOpt() != null) {
                    String mavenCell = mavenFilter.test(option) ? mavenFunctionName(option) : "-- not supported --";
                    String antCell = antFilter.test(option) ? antFunctionName(option) : "-- not supported --";
                    printer.printRecord("--" + option.getLongOpt(), mavenCell, antCell, option.getDescription());
                }
            }
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
