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
import org.apache.rat.OptionCollection;
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
 * A simple tool to convert CLI options Ant report base class .
 */
public class AntGenerator {

    /**
     * The list of Options that are not supported by Ant.
     */
    private static final List<Option> ANT_FILTER_LIST = Arrays.asList(OptionCollection.HELP, OptionCollection.LOG_LEVEL,
            OptionCollection.DIR);

    /**
     * the filter to filter out CLI options that Ant does not support.
     */
    public static Predicate<Option> ANT_FILTER = option -> !(ANT_FILTER_LIST.contains(option) || option.getLongOpt() == null);


    private AntGenerator() {}

    /**
     * Creates a base class for an Ant task.
     * Requires 3 arguments:
     * <ol>
     *     <li>the package name for the class</li>
     *     <li>the simple class name</li>
     *     <li>the directory in which to write the class file.</li>
     * </ol>
     * @param args the arguments.
     * @throws IOException on error
     */
    public static void main(String[] args) throws IOException {
        String packageName = args[0];
        String className = args[1];
        String destDir = args[2];

        List<AntOption> options = OptionCollection.buildOptions().getOptions().stream().filter(ANT_FILTER).map(AntOption::new)
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
        return option.hasArg() ? format("        setArg(%s, %s);%n", option.keyValue(), option.name)
            : format("        if (%s) {%n            setArg(%s, null);%n        }%n", option.name, option.keyValue());
    }

    private static String getElementClass(AntOption option) throws IOException {
        return format("    public class %1$s extends Child { %1$s() {super(%2$s);}}%n%n", WordUtils.capitalize(option.name), option.keyValue());
    }

    /**
     * A class that wraps the CLI option and provides Ant specific values.
     */
    private static class AntOption {
        final Option option;
        /** An uncapitalized name */
        final String name;

        AntOption(Option option) {
            this.option = option;
            name = WordUtils.uncapitalize(new CasedString(StringCase.Kebab, option.getLongOpt()).toCase(StringCase.Camel));
        }

        /**
         * Returns {@code true} if the option should be an attribute of the &lt;rat:report> element.
         * @return {@code true} if the option should be an attribute of the &lt;rat:report> element.
         */
        public boolean isAttribute() {
            return (!option.hasArgs());
        }
        /**
         * Returns {@code true} if the option should be a child element of the &lt;rat:report> element.
         * @return {@code true} if the option should be a child element of the &lt;rat:report> element.
         */
        public boolean isElement() {
            return !isAttribute() || option.getType() != String.class;
        }

        /**
         * Returns {@code true} if the enclosed option has one or more arguments.
         * @return {@code true} if the enclosed option has one or more arguments.
         */
        public boolean hasArg() {
            return option.hasArg();
        }

        /**
         * Returns The key value for the option.   This is the long opt enclosed in quotes and with leading dashes.
         * @return The key value for the option.
         */
        public String keyValue() {
            return format("\"--%s\"", option.getLongOpt());
        }

        /**
         * Get the method comment for this option.
         * @param addParam if {@code true} the param annotation is added.
         * @return the Comment block for the function.
         */
        public String getComment(boolean addParam) {
            StringBuilder sb = new StringBuilder()
            .append(format("    /**%n     * %s%n", option.getDescription().replaceAll("<", "&lt;").replaceAll(">", "&gt;")));
            if (option.isDeprecated()) {
                sb.append(format("     * %s%n     * @deprecated%n",option.getDeprecated()));
            }
            if (addParam && option.hasArg()) {
                sb.append(format("     * @param %s The value to set%n", name));
            }
            return sb.append(format("     */%n")).toString();
        }

        /**
         * Get the signature of th eattribute function.
         * @return the signature of the attribue function.
         */
        public String getAttributeFunctionName() {
            return "set" +
                    WordUtils.capitalize(name) +
                    (option.hasArg() ?"(String " : "(boolean ") +
                    name +
                    ")";
        }
    }
}
