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
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.text.WordUtils;
import org.apache.rat.OptionTools;
import org.apache.rat.utils.CasedString;
import org.apache.rat.utils.CasedString.StringCase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * A simple tool to convert CLI options  to Maven and Ant format
 */
public class AntGenerator {

    public  static final List<String> antFilterList = Arrays.asList(OptionTools.HELP.getLongOpt(), OptionTools.LOG_LEVEL.getLongOpt(),
            OptionTools.DIR.getLongOpt());

    public static Predicate<Option> antFilter = Naming.optionFilter(antFilterList);

    private AntGenerator() {}

    private static final String INDENT="    ";

    /**
     * Creates the documentation.  Writes to the output specified by the -o or --out option.  Defaults to System.out.
     * @param args the arguments.  Try --help for help.
     * @throws IOException on error
     */
    public static void main(String[] args) throws IOException {
        String packageName = args[0];
        String className = args[1];
        String destDir = args[2];

        List<AntOption> options = OptionTools.buildOptions().getOptions().stream().filter(antFilter).map(AntOption::new)
                .collect(Collectors.toList());

        File file = new File(new File(new File(destDir), packageName.replaceAll("\\.", File.separator)),className+".java");
        System.out.println("Creating "+file);
        file.getParentFile().mkdirs();
        try (InputStream template = AntGenerator.class.getResourceAsStream("/Ant.tpl");
             FileWriter writer = new FileWriter(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             OutputStreamWriter customClasses = new OutputStreamWriter(bos);
        ) {
            if (template == null) {
                throw new RuntimeException("Template /Ant.tpl not found");
            }
            LineIterator iter = IOUtils.lineIterator(new InputStreamReader(template));
            while (iter.hasNext()) {
                String line = iter.next();
                switch (line.trim()) {
                    case "${methods}":
                        writeMethods(writer, options, customClasses);
                        break;
                    case "${package}":
                        writer.append(format("package %s;%n",packageName));
                        break;
                    case "${constructor}":
                        writer.append(format("    protected %s() {}%n",className));
                        break;
                    case "${class}":
                        writer.append(format("public abstract class %s extends Task {%n",className));
                        break;
                    case "${classes}":
                        customClasses.flush();
                        customClasses.close();
                        writer.write(bos.toString());
                        break;
                    default:
                        writer.append(line).append(System.lineSeparator());
                        break;
                }
            }
        }
    }

    private static void writeMethods(FileWriter writer, List<AntOption> options, Writer customClasses) throws IOException {
        for (AntOption option : options) {

            if (option.isAttribute()) {
                writer.append(option.getComment(true));
                writer.append(format("    public void %s {%n%s%n    }%n%n", option.getAttributeFunctionName(), getAttributeBody(option)));
            }

            if (option.isElement()) {
                customClasses.append(option.getComment(false));
                customClasses.append(format("    public %1$s create%1$s() {%n        return new %1$s();%n    }%n%n", WordUtils.capitalize(option.name)));
                customClasses.append(getElementClass(option));
            }
        }
    }

    private static String getAttributeBody(AntOption option) throws IOException {
        return option.hasArg() ? format("        setArg(\"%s\", %s);%n", option.longValue(), option.name)
            : format("        if (%s) {%n            setArg(\"%s\", null);%n        }%n", option.name, option.longValue());
    }

    private static String getElementClass(AntOption option) throws IOException {
        return format("    public class %1$s extends Child { %1$s() {super(\"%2$s\");}}%n%n", WordUtils.capitalize(option.name), option.longValue());
    }


//    private static void writeFileBody(FileWriter writer, Option option, CasedString name) throws IOException {
//        String varName = WordUtils.uncapitalize(name.toCase(StringCase.Camel));
//        String longArg = Naming.asLongArg(option);
//
//        writer.append(INDENT).append(INDENT).append( "try {").append(System.lineSeparator())
//                .append(INDENT).append(INDENT).append(INDENT).append("args.add(").append(longArg).append(");").append(System.lineSeparator())
//                .append(INDENT).append(INDENT).append(INDENT).append("args.add(").append(varName).append(".getCanonicalPath());").append(System.lineSeparator())
//                .append(INDENT).append(INDENT).append( "} catch (IOException e) {").append(System.lineSeparator())
//                .append(INDENT).append(INDENT).append(INDENT).append("throw new BuildException(e.getMessage(), e);").append(System.lineSeparator())
//                .append(INDENT).append(INDENT).append( "}").append(System.lineSeparator());
//    }

    private static class AntOption {
        final Option option;
        /** An uncapitalized name */
        final String name;

        AntOption(Option option) {
            this.option = option;
            name = WordUtils.uncapitalize(new CasedString(StringCase.Kebab, option.getLongOpt()).toCase(StringCase.Camel));
        }

        public boolean isAttribute() {
            return (!option.hasArgs());
        }

        public boolean isElement() {
            return !isAttribute() || option.getType() != String.class;
        }

        public String getType() {
            return ((Class<?>) option.getType()).getSimpleName();
        }

        public boolean hasArg() {
            return option.hasArg();
        }

        public String longValue() {
            return "--" + option.getLongOpt();
        }

        public String getComment(boolean addParam) {
            StringBuilder sb = new StringBuilder()
            .append(format("    /**%n     * %s%n", option.getDescription()));
            if (option.isDeprecated()) {
                sb.append(format("     * %s%n     * @deprecated%n",option.getDeprecated()));
            }
            if (addParam && option.hasArg()) {
                sb.append(format("     * @param %s The value to set%n", name));
            }
            return sb.append(format("     */%n")).toString();
        }

        public String getAttributeFunctionName() {
            return "set" +
                    WordUtils.capitalize(name) +
                    (option.hasArg() ?"(String " : "(boolean ") +
                    name +
                    ")";
        }

        public String getElementFunctionName() {
            return "create" +
                    WordUtils.capitalize(name) +
                    "()";
        }
    }
}
