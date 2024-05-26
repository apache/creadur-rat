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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * A simple tool to convert CLI options to Maven Mojo base class
 */
public class MavenGenerator {

    /**
     * List of CLI Options that are not supported by Maven.
     */
    private static final List<Option> MAVEN_FILTER_LIST = Arrays.asList(OptionCollection.HELP, OptionCollection.DIR);

    /**
     * Filter to remove Options not supported by Maven.
     */
    public static Predicate<Option> MAVEN_FILTER = option -> !(MAVEN_FILTER_LIST.contains(option) || option.getLongOpt() == null);

    private MavenGenerator() {
    }

    /**
     * Creates the Maven MojoClass
     * Requires 3 arguments:
     * <ol>
     *     <li>the package name for the class</li>
     *     <li>the simple class name</li>
     *     <li>the directory in which to write the class file.</li>
     * </ol>
     *
     * @param args the arguments
     * @throws IOException on error
     */
    public static void main(String[] args) throws IOException {

        String packageName = args[0];
        String className = args[1];
        String destDir = args[2];
        List<MavenOption> options = OptionCollection.buildOptions().getOptions().stream().filter(MAVEN_FILTER).map(MavenOption::new).collect(Collectors.toList());

        String pkgName = String.join(File.separator, new CasedString(StringCase.DOT, packageName).getSegments());
        File file = new File(new File(new File(destDir), pkgName), className + ".java");
        System.out.println("Creating " + file);
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
//                    case "${imports}":
//                        writer.append(getImports(options));
//                        break;
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
                        writer.append(format("public abstract class %s extends AbstractMojo {%n", className));
                        break;
                    default:
                        writer.append(line).append(System.lineSeparator());
                        break;
                }
            }
        }
    }

    private static void writeMethods(FileWriter writer, List<MavenOption> options) throws IOException {
        for (MavenOption option : options) {
            String desc = option.getDescription().replace("<", "&lt;").replace(">", "&gt;");

            writer.append(format("    /**%n     * %s%n     * @param %s the argument.%n", desc, option.name));
            if (option.isDeprecated()) {
                writer.append(format("     * %s%n     * @deprecated", option.getDeprecated()));
            }
            writer.append(format("     */%n    @Parameter(property = \"rat.%2$s\")%n    public void set%1$s(%3$s %2$s) {%n",
                            WordUtils.capitalize(option.name), option.name, option.hasArg() ? "String" : "boolean"))
                    .append(getBody(option))
                    .append(format("    }%n"));
        }
    }

    private static String getBody(MavenOption option) throws IOException {
        if (option.hasArg()) {
            return format("        %sArg(%s, %s);%n", option.option.hasArgs() ? "add" : "set" , option.keyValue(), option.name);
        } else {
            return format("        if (%1$s) {%n            setArg(%2$s, null);%n" +
                            "        } else {%n            removeArg(%2$s);%n        }%n",
                    option.name, option.keyValue());
        }
    }

//    private static String getImports(List<MavenOption> options) {
//        Set<Class<?>> includes = new TreeSet<>((o1, o2) -> o1.getName().compareTo(o2.getName()));
//        includes.add(java.util.ArrayList.class);
//        includes.add(java.util.List.class);
//        options.stream().map(o -> ((Class<?>)o.option.getType())).filter(t -> !t.getName().startsWith("java.lang.")).forEach(includes::add);
//        StringBuilder sb = new StringBuilder();
//        includes.stream().map(t -> format("import %s;%n", t.getName())).forEach(sb::append);
//        return sb.toString();
//    }

    private static class MavenOption {
        final Option option;
        final String name;

        public static String createName(Option option) {
            return new CasedString(CasedString.StringCase.KEBAB, option.getLongOpt()).toCase(CasedString.StringCase.CAMEL);
        }

        /**
         * Constructor.
         *
         * @param option The CLI option
         */
        MavenOption(Option option) {
            this.option = option;
            this.name = createName(option);
        }

        /**
         * Get the description escaped for XML format.
         *
         * @return the description.
         */
        public String getDescription() {
            return option.getDescription().replace("<", "&lt;").replace(">", "&gt;");
        }

        /**
         * Returns the value as an POM xml node.
         *
         * @param value the value
         * @return the pom xml node.
         */
        public String xmlNode(String value) {
            return format("<%1$s>%2$s</%1$s>%n", name, value == null ? "false" : value);//: format("<%s>%n", name);
        }

        /**
         * Gets the simple class name for the data type for this option.
         * Normally "String".
         *
         * @return the simple class name for the type.
         */
        public Class<?> getType() {
            return option.hasArg() ? ((Class<?>) option.getType()) : boolean.class;
        }

        public boolean isDeprecated() {
            return option.isDeprecated();
        }

        /**
         * Determine if true if the enclosed option expects an argument.
         *
         * @return {@code true} if the enclosed option expects at least one argument.
         */
        public boolean hasArg() {
            return option.hasArg();
        }

        /**
         * the key value for the option.
         *
         * @return the key value for the CLI argument map.
         */
        public String keyValue() {
            return "\"--" + option.getLongOpt() + "\"";
        }

        public String getDeprecated() {
            return option.getDeprecated().toString();
        }
    }
}
