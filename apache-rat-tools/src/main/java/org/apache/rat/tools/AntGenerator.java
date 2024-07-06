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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
import org.apache.commons.text.WordUtils;
import org.apache.rat.OptionCollection;
import org.apache.rat.commandline.Arg;
import org.apache.rat.utils.CasedString;
import org.apache.rat.utils.CasedString.StringCase;

/**
 * A simple tool to convert CLI options into an Ant report base class.
 */
public final class AntGenerator {

    /**
     * The list of Options that are not supported by Ant.
     */
    private static final List<Option> ANT_FILTER_LIST = new ArrayList<>();

    static {
        ANT_FILTER_LIST.addAll(Arg.LOG_LEVEL.group().getOptions());
        ANT_FILTER_LIST.addAll(Arg.DIR.group().getOptions());
        ANT_FILTER_LIST.add(OptionCollection.HELP);
    }

    /**
     * the filter to filter out CLI options that Ant does not support.
     */
    private static final Predicate<Option> ANT_FILTER = option -> !(ANT_FILTER_LIST.contains(option) || option.getLongOpt() == null);

    /** A mapping of external name to internal name if not standard */
    private static final Map<String, String> RENAME_MAP = new HashMap<>();

    static {
        RENAME_MAP.put("addLicense", "add-license");
    }

    private AntGenerator() { }

    /**
     * Gets the Option predicate that removes unsupported CLI options.
     * @return The Option predicate that removes unsupported CLI options.
     */
    public static Predicate<Option> getFilter() {
        return ANT_FILTER;
    }
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
    public static void main(final String[] args) throws IOException {
        String packageName = args[0];
        String className = args[1];
        String destDir = args[2];

        List<AntOption> options = Arg.getOptions().getOptions().stream().filter(ANT_FILTER).map(AntOption::new)
                .collect(Collectors.toList());

        String pkgName = String.join(File.separator, new CasedString(StringCase.DOT, packageName).getSegments());
        File file = new File(new File(new File(destDir), pkgName), className + ".java");
        file.getParentFile().mkdirs();
        try (InputStream template = AntGenerator.class.getResourceAsStream("/Ant.tpl");
             FileWriter writer = new FileWriter(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             OutputStreamWriter customClasses = new OutputStreamWriter(bos);
        ) {
            if (template == null) {
                throw new RuntimeException("Template /Ant.tpl not found");
            }
            LineIterator iter = IOUtils.lineIterator(new InputStreamReader(template, StandardCharsets.UTF_8));
            while (iter.hasNext()) {
                String line = iter.next();
                switch (line.trim()) {
                    case "${static}":
                        for (Map.Entry<String, String> entry : RENAME_MAP.entrySet()) {
                            writer.append(format("        xlateName.put(\"%s\", \"%s\");%n", entry.getKey(), entry.getValue()));
                        }
                        for (Option option : ANT_FILTER_LIST) {
                            writer.append(format("        unsupportedArgs.add(\"%s\");%n", StringUtils.defaultIfEmpty(option.getLongOpt(), option.getOpt())));
                        }
                        break;
                    case "${methods}":
                        writeMethods(writer, options, customClasses);
                        break;
                    case "${package}":
                        writer.append(format("package %s;%n", packageName));
                        break;
                    case "${constructor}":
                        writer.append(format("    protected %s() {}%n", className));
                        break;
                    case "${class}":
                        writer.append(format("public abstract class %s extends Task {%n", className));
                        break;
                    case "${classes}":
                        customClasses.flush();
                        customClasses.close();
                        writer.write(bos.toString());
                        break;
                    case "${commonArgs}":
                        try (InputStream argsTpl = MavenGenerator.class.getResourceAsStream("/Args.tpl")) {
                            IOUtils.copy(argsTpl, writer, StandardCharsets.UTF_8);
                        }
                        break;
                    default:
                        writer.append(line).append(System.lineSeparator());
                        break;
                }
            }
        }
    }

    private static void writeMethods(final FileWriter writer, final List<AntOption> options, final Writer customClasses) throws IOException {
        for (AntOption option : options) {

            if (option.isAttribute()) {
                writer.append(option.getComment(true));
                writer.append(format("    public void %s {%n%s%n    }%n%n", option.getAttributeFunctionName(), getAttributeBody(option)));
            }

            if (option.isElement()) {
                customClasses.append(option.getComment(false));
                customClasses.append(format("    public %1$s create%1$s() {%n        return new %1$s();%n    }%n%n",
                        WordUtils.capitalize(option.getName())));
                customClasses.append(getElementClass(option));
            }
        }
    }

    private static String getAttributeBody(final AntOption option) throws IOException {
        return option.hasArg() ? format("        setArg(%s, %s);%n", option.keyValue(), option.getName())
            : format("        if (%1$s) { setArg(%2$s, null); } else { removeArg(%2$s); }", option.getName(), option.keyValue());
    }

    private static String getElementClass(final AntOption option) throws IOException {
        return format("    public class %1$s extends Child { %1$s() {super(%2$s);}}%n%n", WordUtils.capitalize(option.getName()),
                option.keyValue());
    }

    static String createName(final Option option) {
        String name = option.getLongOpt();
        name = StringUtils.defaultIfEmpty(RENAME_MAP.get(name), name).toLowerCase(Locale.ROOT);
        return new CasedString(StringCase.KEBAB, name).toCase(StringCase.CAMEL);
    }
}
