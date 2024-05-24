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
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.text.WordUtils;
import org.apache.rat.OptionTools;
import org.apache.rat.utils.CasedString;
import org.apache.rat.utils.CasedString.StringCase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static java.lang.String.format;

/**
 * A simple tool to convert CLI options to Maven format
 */
public class MavenGenerator {

    private MavenGenerator() {}

    /**
     * Creates the documentation.  Writes to the output specified by the -o or --out option.  Defaults to System.out.
     * @param args the arguments.  Try --help for help.
     * @throws IOException on error
     */
    public static void main(String[] args) throws IOException {
        String packageName = args[0];
        String className = args[1];
        String destDir = args[2];
        Options options = new Options();

        OptionTools.buildOptions().getOptions().stream().filter(Naming.optionFilter(Naming.mavenFilterList)).forEach(options::addOption);

        File file = new File(new File(new File(destDir), packageName.replaceAll("\\.", File.separator)),className+".java");
        System.out.println("Creating "+file);
        file.getParentFile().mkdirs();
        try (InputStream template = MavenGenerator.class.getResourceAsStream("/Maven.tpl");
             FileWriter writer = new FileWriter(file)) {
            if (template == null) {
                throw new RuntimeException("Template /Maven.tpl not found");
            }
            LineIterator iter = IOUtils.lineIterator(new InputStreamReader(template));
            while (iter.hasNext()) {
                String line = iter.next();
                switch (line.trim()) {
                    case "${methods}":
                        writeMethods(writer, options);
                        break;
                    case "${package}":
                        writer.append(format("package %s;%n", packageName));
                        break;
                    case "${constructor}":
                        writer.append(format("    protected %s() {}%n", className));
                        break;
                    case "${class}":
                        writer.append(format("public abstract class %s extends AbstractMojo {%n",className));
                        break;
                    default:
                        writer.append(line).append(System.lineSeparator());
                        break;
                }
            }
        }
    }

    private static void writeMethods(FileWriter writer, Options options) throws IOException {
        for (Option option : options.getOptions()) {
            if (option.getLongOpt() != null) {
                String name = WordUtils.uncapitalize(new CasedString(StringCase.Kebab, option.getLongOpt()).toCase(StringCase.Camel));
                writer.append(format("    /**%n     * %s%n", option.getDescription()));
                if (option.isDeprecated()) {
                    writer.append(format("     * %s%n     * @deprecated", option.getDeprecated()));
                }
                writer.append(format("     */%n    public void set%1$s(String %2$s) {%n",
                        WordUtils.capitalize(name), name))
                .append(getBody(option, name))
                .append(format("    }%n"));
            }
        }
    }

    private static String getBody(Option option, String name) throws IOException {
        String longArg = Naming.asLongArg(option);
        if (option.hasArg()) {
            return format( "        args.add(\"%s\");%n        args.add(%s);%n", Naming.asLongArg(option), name);
        } else {
            return format( "        if (%1$s) {%n            args.add(\"%2$s\");%n" +
                            "        } else {%n            args.remove(\"%2$s\");%n        }%n        }n",
            name, Naming.asLongArg(option));
        }
    }
}
