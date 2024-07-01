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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.cli.Option;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.OptionCollection;
import org.apache.rat.utils.CasedString;
import org.apache.rat.utils.CasedString.StringCase;

/**
 * A simple tool to convert CLI options to Maven Mojo base class
 */
public final class MavenGenerator {

    /** A mapping of external name to internal name if not standard */
    private static final Map<String, String> RENAME_MAP = new HashMap<>();

    static {
        RENAME_MAP.put("licenses", "config");
        RENAME_MAP.put("addLicense", "add-license");
    }
    /**
     * List of CLI Options that are not supported by Maven.
     */
    private static final List<Option> MAVEN_FILTER_LIST = Arrays.asList(OptionCollection.HELP, OptionCollection.DIR);

    /**
     * Filter to remove Options not supported by Maven.
     */
    private static final Predicate<Option> MAVEN_FILTER = option -> !(MAVEN_FILTER_LIST.contains(option) || option.getLongOpt() == null);

    /**
     * Returns the Option predicate that removes all unsupported Options for the Maven UI.
     * @return the Option predicate that removes all unsupported Options for the Maven UI.
     */
    public static Predicate<Option> getFilter() {
        return MAVEN_FILTER;
    }

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
    public static void main(final String[] args) throws IOException {
        String packageName = args[0];
        String className = args[1];
        String destDir = args[2];
        List<MavenOption> options = OptionCollection.buildOptions().getOptions().stream().filter(MAVEN_FILTER)
                .map(MavenOption::new).collect(Collectors.toList());
        String pkgName = String.join(File.separator, new CasedString(CasedString.StringCase.DOT, packageName).getSegments());
        File file = new File(new File(new File(destDir), pkgName), className + ".java");
        System.out.println("Creating " + file);
        file.getParentFile().mkdirs();
        try (InputStream template = MavenGenerator.class.getResourceAsStream("/Maven.tpl");
             FileWriter writer = new FileWriter(file)) {
            if (template == null) {
                throw new RuntimeException("Template /Maven.tpl not found");
            }
            LineIterator iter = IOUtils.lineIterator(new InputStreamReader(template, StandardCharsets.UTF_8));
            while (iter.hasNext()) {
                String line = iter.next();
                switch (line.trim()) {
                    case "${static}":
                        for (Map.Entry<String, String> entry : RENAME_MAP.entrySet()) {
                            writer.append(format("        xlateName.put(\"%s\", \"%s\");%n", entry.getKey(), entry.getValue()));
                        }
                        break;
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

    private static String getComment(final MavenOption option) {
        String desc = option.getDescription().replace("<", "&lt;").replace(">", "&gt;");
        StringBuilder sb = new StringBuilder()
            .append(format("    /**%n     * %s%n     * @param %s the argument.%n", desc, option.getName()));
        if (option.isDeprecated()) {
            sb.append(format("     * %s%n     * @deprecated%n", option.getDeprecated()));
        }
        return sb.append(format("     */%n")).toString();
    }

    private static void writeMethods(final FileWriter writer, final List<MavenOption> options) throws IOException {
        for (MavenOption option : options) {
            writer.append(getComment(option))
                    .append(option.getMethodSignature("    ")).append(" {").append(System.lineSeparator())
                    .append(getBody(option))
                    .append("    }").append(System.lineSeparator());
        }
    }

    private static String getBody(final MavenOption option) {
        if (option.hasArg()) {
            return format("        %sArg(%s, %s);%n", option.hasArgs() ? "add" : "set", option.keyValue(), option.getName());
        } else {
            return format("        if (%1$s) {%n            setArg(%2$s, null);%n" +
                            "        } else {%n            removeArg(%2$s);%n        }%n",
                    option.getName(), option.keyValue());
        }
    }

    static String createName(final Option option) {
        String name = option.getLongOpt();
        name = StringUtils.defaultIfEmpty(RENAME_MAP.get(name), name).toLowerCase(Locale.ROOT);
        return new CasedString(CasedString.StringCase.KEBAB, name).toCase(StringCase.CAMEL);
    }

}
