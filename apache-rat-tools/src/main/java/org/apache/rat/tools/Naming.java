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

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.text.WordUtils;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.FileWriter;
import java.util.function.Predicate;

import org.apache.rat.OptionCollection;
import org.apache.rat.utils.CasedString;
import org.apache.rat.utils.CasedString.StringCase;

import static java.lang.String.format;

/**
 * A simple tool to convert CLI options to Maven and Ant format and produce a CSV file.
 */
public class Naming {

    public Naming() {}

    /**
     * Creates the CSV file.
     * Requires 1 argument:
     * <ol>
     *    <li>the name of the output file with path if desired</li>
     * </ol>
     * @throws IOException on error
     */
    public static void main(String[] args) throws IOException {
        Options options = OptionCollection.buildOptions();
        Predicate<Option> mavenFilter = MavenGenerator.MAVEN_FILTER;
        Predicate<Option> antFilter = AntGenerator.ANT_FILTER;
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(args[0]), CSVFormat.DEFAULT)) {
            printer.printRecord("CLI", "Maven", "Ant", "Description");
            for (Option option : options.getOptions()) {
                if (option.getLongOpt() != null) {
                    CasedString opt = new CasedString(StringCase.KEBAB, option.getLongOpt());
                    String mavenCell = mavenFilter.test(option) ? mavenFunctionName(option, opt) : "-- not supported --";
                    String antCell = antFilter.test(option) ? antFunctionName(option, opt) : "-- not supported --";
                    printer.printRecord(opt, mavenCell, antCell, option.getDescription());
                }
            }
        }
    }

    public static String mavenFunctionName(Option option, CasedString name) {
        StringBuilder sb = new StringBuilder();
        if (option.isDeprecated()) {
            sb.append("@Deprecated").append(System.lineSeparator());
        }
        sb.append(format("@Parameter(property = 'rat.%s'", name.toCase(StringCase.CAMEL)));
        if (option.isRequired()) {
            sb.append(" required = true");
        }
        sb.append(format(")%n public void %s%s(%s %s)", option.hasArgs() ? "add" : "set",
                WordUtils.capitalize(name.toCase(StringCase.CAMEL)), option.hasArg() ? "String " : "boolean ",
               name.toCase(StringCase.CAMEL)));
        return sb.toString();
    }

    private static String antFunctionName(Option option, CasedString name) {
        StringBuilder sb = new StringBuilder();
        if (option.isDeprecated()) {
            sb.append("@Deprecated").append(System.lineSeparator());
        }
        if (option.hasArgs()) {
            sb.append(format("<rat:report>%n  <%1$s>text</%1$s>%n</rat:report>", name.toCase(StringCase.CAMEL)));
        } else {
            sb.append(format("<rat:report %s = 'text'/>", name.toCase(StringCase.CAMEL)));
        }
        return sb.toString();
    }
}
