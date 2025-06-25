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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.Option;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.rat.OptionCollection;
import org.apache.rat.documentation.options.AntOption;
import org.apache.rat.utils.CasedString;
import org.apache.rat.utils.CasedString.StringCase;

import static java.lang.String.format;

/**
 * A simple tool to convert CLI options into an Ant report base class.
 */
public final class AntGenerator {

    /**
     * A map of type patterns for that type.
     */
    private static final Map<OptionCollection.ArgumentType, GenerateType> GENERATE_TYPE_MAP = new HashMap<>();

    static {
        String defaultFmt = "        public void add%$1s(String %2$s) {%n" +
                "            addArg(%%1$s, %2$s);%n" +
                "        }%n%n";
        GenerateType generateType;
        for (OptionCollection.ArgumentType type : OptionCollection.ArgumentType.values()) {
            switch (type) {
                case FILE:
                case DIRORARCHIVE:
                    generateType = new GenerateType("fileset") {
                        protected String getMethodFormat(final AntOption antOption) {
                            return "        public void addConfiguredFileset(FileSet fileSet) {\n" +
                                    "            for (Resource resource : fileSet) {\n" +
                                    "                if (resource.isFilesystemOnly()) {\n" +
                                    "                    addArg(%1$s, ((FileResource) resource).getFile().getAbsolutePath());\n" +
                                    "                }\n" +
                                    "            }\n" +
                                    "        }\n\n";
                        }
                    };
                    break;
                case NONE:
                    generateType = new GenerateType("") {
                        protected String getMethodFormat(final AntOption antOption) {
                            return "";
                        }
                    };
                    break;
                case STANDARDCOLLECTION:
                    generateType = new GenerateType("Std");
                    break;
                case EXPRESSION:
                    generateType = new GenerateType("Expr");
                    break;
                case COUNTERPATTERN:
                    generateType = new GenerateType("Cntr");
                    break;
                case LICENSEID:
                case FAMILYID:
                    generateType = new GenerateType("Lst");
                    break;
                default:
                    generateType = new GenerateType(type.getDisplayName()) {

                        protected String getMethodFormat(final AntOption antOption) {
                            return String.format(defaultFmt, innerClass, WordUtils.uncapitalize(antOption.getArgName()));
                        }
                    };
            }
            GENERATE_TYPE_MAP.put(type, generateType);
        }
    }

    private AntGenerator() { }


    /**
     * Gets the key for the Args array.
     * @param option the option to get the key for.
     * @return the key for the option.
     */
    private static String argsKey(final Option option) {
        return StringUtils.defaultIfEmpty(option.getLongOpt(), option.getOpt());
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
     * @throws IOException on error.
     */
    public static void main(final String[] args) throws IOException {
        if (args == null || args.length < 3) {
            System.err.println("At least three arguments are required: package, simple class name, target directory.");
            return;
        }

        String packageName = args[0];
        String className = args[1];
        String destDir = args[2];

        List<AntOption> options = AntOption.getAntOptions();

        String pkgName = String.join(File.separator, new CasedString(StringCase.DOT, packageName).getSegments());
        File file = new File(new File(new File(destDir), pkgName), className + ".java");
        file.getParentFile().mkdirs();
        try (InputStream template = AntGenerator.class.getResourceAsStream("/Ant.tpl");
             FileWriter writer = new FileWriter(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             OutputStreamWriter customClasses = new OutputStreamWriter(bos)) {
            if (template == null) {
                throw new RuntimeException("Template /Ant.tpl not found");
            }
            LineIterator iter = IOUtils.lineIterator(new InputStreamReader(template, StandardCharsets.UTF_8));
            while (iter.hasNext()) {
                String line = iter.next();
                switch (line.trim()) {
                    case "${static}":
                        for (Map.Entry<String, String> entry : AntOption.getRenameMap().entrySet()) {
                            writer.append(format("        xlateName.put(\"%s\", \"%s\");%n", entry.getKey(), entry.getValue()));
                        }
                        for (Option option : AntOption.getFilteredOptions()) {
                            writer.append(format("        unsupportedArgs.add(\"%s\");%n", argsKey(option)));
                        }
                        for (AntOption option : options) {
                            if (option.isDeprecated()) {
                                writer.append(format("        deprecatedArgs.put(\"%s\", \"%s\");%n", argsKey(option.getOption()),
                                        format("Use of deprecated option '%s'. %s", option.getName(), option.getDeprecated())));
                            }
                        }
                        break;
                    case "${methods}":
                        writeMethods(writer, options, customClasses);
                        break;
                    case "${package}":
                        writer.append(format("package %s;%n", packageName));
                        break;
                    case "${constructor}":
                        writer.append(format("    protected %s() {\n" +
                                "        setDeprecationReporter();\n" +
                                "    }%n", className));
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
                            if (argsTpl == null) {
                                throw new RuntimeException("Args.tpl not found");
                            }
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
            } else {
                customClasses.append(option.getComment(false));
                customClasses.append(format("    public %1$s create%1$s() {%n        return new %1$s();%n    }%n%n",
                        WordUtils.capitalize(option.getName())));
                customClasses.append(getElementClass(option));
            }
        }
    }

    private static String getAttributeBody(final AntOption option) {
        return option.hasArg() ? format("        setArg(%s, %s);%n", option.keyValue(), option.getName())
            : format("        if (%1$s) { setArg(%2$s, null); } else { removeArg(%2$s); }", option.getName(), option.keyValue());
    }

    private static String getElementClass(final AntOption option) {

        String elementConstructor =
                "    public class %1$s {\n" +
                        "        %1$s() { }\n\n";

        String funcName = WordUtils.capitalize(option.getName());
        StringBuilder result = new StringBuilder(format(elementConstructor, funcName));
        Set<AntOption> implementedOptions = new HashSet<>();
        implementedOptions.add(option);
        option.convertedFrom().stream().filter(o -> !AntOption.getUnsupportedOptions().contains(o)).forEach(opt -> implementedOptions.add(new AntOption(opt)));
        implementedOptions.forEach(o -> result.append(GENERATE_TYPE_MAP.get(o.getArgType()).getPattern(option, o)));
        result.append(format("    }%n"));

        return result.toString();
    }

    public static class GenerateType {
        /** the inner class name text */
        protected final String innerClass;

        GenerateType(final String innerClass) {
            this.innerClass = innerClass;
        }

        protected String getMethodFormat(final AntOption antOption) {
            return String.format("        public void addConfigured%1$s(%1$s %%2$s) {\n" +
                    "            addArg(%%1$s, %%2$s.value);\n" +
                    "        }\n\n", innerClass);
        }

        public String getPattern(final AntOption delegateOption, final AntOption antOption) {
            if (delegateOption.isAttribute()) {
                String fmt = "<rat:report %s='%s' />";
                return format(fmt, delegateOption.getName(), antOption.hasArg() ? antOption.getArgName() : "true");
            } else {
                return format(getMethodFormat(antOption), antOption.keyValue(),
                        WordUtils.uncapitalize(antOption.getArgName()));
            }
        }
    }

}
