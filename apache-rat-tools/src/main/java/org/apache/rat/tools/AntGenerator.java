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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.commons.cli.Option;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.WordUtils;
import org.apache.rat.OptionCollection;
import org.apache.rat.documentation.options.AntOption;
import org.apache.rat.documentation.options.AntOptionCollection;
import org.apache.rat.utils.CasedString;
import org.apache.rat.utils.CasedString.StringCase;

import static java.lang.String.format;

/**
 * A simple tool to convert CLI options into an Ant report base class.
 */
public final class AntGenerator {

    /**
     * Create a GenerateType for the option
     * @param antOption the ant option to generate the type for.
     */
    private static GenerateType getGenerateType(final AntOption antOption) {
        String defaultFmt = """
                        public void add%$1s(String %2$s) {
                            addArg(%%1$s, %2$s);
                        }
                        """;

        GenerateType generateType = null;
        return switch (antOption.getArgType()) {
            case FILE, DIRORARCHIVE -> new GenerateType("FileSet") {
                @Override
                public String getMethod(final AntOption antOption) {
                    return format("""
                                    public void addConfiguredFileset(FileSet fileSet) {
                                        for (Resource resource : fileSet) {
                                            if (resource.isFilesystemOnly()) {
                                                addArg("%1$s", ((FileResource) resource).getFile().getAbsolutePath());
                                            }
                                        }
                                    }
                            """, antOption.keyValue());
                }
            };
            case NONE -> new GenerateType("") {
                @Override
                public String getMethod(final AntOption antOption) {
                    return "";
                }
            };
            case STANDARDCOLLECTION -> new GenerateType("Std");
            case EXPRESSION -> new GenerateType("Expr");
            case COUNTERPATTERN -> new GenerateType("Cntr");
            case LICENSEID, FAMILYID -> new GenerateType("Lst");
            default -> new GenerateType(antOption.getArgType().getDisplayName()) {
                @Override
                public String getMethod(final AntOption antOption) {
                    return String.format(defaultFmt, innerClass, WordUtils.uncapitalize(antOption.getArgName()));
                }
            };
        };
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

        List<AntOption> options = AntOptionCollection.INSTANCE.getMappedOptions().toList();

        String pkgName = String.join(File.separator, new CasedString(StringCase.DOT, packageName).getSegments());
        File file = new File(new File(new File(destDir), pkgName), className + ".java");
        file.getParentFile().mkdirs();
        try (InputStream template = AntGenerator.class.getResourceAsStream("/Ant.tpl");
             FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8);
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             OutputStreamWriter customClasses = new OutputStreamWriter(bos, StandardCharsets.UTF_8);) {
            if (template == null) {
                throw new RuntimeException("Template /Ant.tpl not found");
            }
            LineIterator iter = IOUtils.lineIterator(new InputStreamReader(template, StandardCharsets.UTF_8));
            while (iter.hasNext()) {
                String line = iter.next();
                switch (line.trim()) {
                    case "${static}":
                        for (Map.Entry<?, ?> entry : AntOptionCollection.getRenameMap().entrySet()) {
                            writer.append(format("        xlateName.put(\"%s\", \"%s\");%n", entry.getKey(), entry.getValue()));
                        }

                        for (Option option : AntOptionCollection.INSTANCE.getUnsupportedOptions()
                                .getOptions()) {
                            writer.append(format("        unsupportedArgs.add(\"%s\");%n", argsKey(option)));
                        }

                        for (AntOption option : AntOptionCollection.INSTANCE.getMappedOptions().filter(AntOption::isDeprecated).toList()) {
                            writer.append(format("        deprecatedArgs.put(\"%s\", \"%s\");%n", argsKey(option.getOption()),
                                    format("Use of deprecated option '%s'. %s", option.getName(), option.getDeprecated())));
                        }
                        break;
                    case "${methods}":
                        writeMethods(writer, options, customClasses);
                        break;
                    case "${package}":
                        writer.append(format("package %s;%n", packageName));
                        break;
                    case "${constructor}":
                        writer.append(format("""
                                    protected %s() {
                                        setDeprecationReporter();
                                    }%n""", className));
                        break;
                    case "${class}":
                        writer.append(format("public abstract class %s extends Task {%n", className));
                        break;
                    case "${classes}":
                        customClasses.flush();
                        customClasses.close();
                        writer.write(bos.toString(StandardCharsets.UTF_8));
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
        for (AntOption antOption : options) {

            if (antOption.isAttribute()) {
                writer.append(getComment(antOption, true));
                if (antOption.isDeprecated()) {
                    writer.append("    @Deprecated\n");
                }
                writer.append(format("    public void %s {%n%s%n    }%n%n", getAttributeFunctionName(antOption), getAttributeBody(antOption)));
            } else {
                customClasses.append(getComment(antOption, false));
                customClasses.append(format("    public %1$s create%1$s() {%n        return new %1$s();%n    }%n%n",
                        antOption.getCasedName().toCase(StringCase.CAMEL)));
                customClasses.append(getElementClass(antOption));
            }
        }
    }

    private static String getAttributeBody(final AntOption option) {
        return option.hasArg() ? format("        setArg(\"%s\", %s);%n", option.keyValue(), option.getName())
            : format("        if (%1$s) { setArg(\"%2$s\", null); } else { removeArg(\"%2$s\"); }", option.getName(), option.keyValue());
    }

    private static String getElementClass(final AntOption option) {

        String elementConstructor =
                """
                            public class %1$s {
                                %1$s() { }%n""";

        String funcName = WordUtils.capitalize(option.getName());
        StringBuilder result = new StringBuilder(format(elementConstructor, funcName));
        Set<AntOption> implementedOptions = new HashSet<>();
        implementedOptions.add(option);
        implementedOptions.addAll(option.convertedFrom());
        implementedOptions.forEach(antOption -> result.append(getGenerateType(antOption).getMethod(antOption)));
        result.append(format("    }%n"));

        return result.toString();
    }

    public static class GenerateType {
        /** the inner class name text */
        protected final String innerClass;

        GenerateType(final String innerClass) {
            this.innerClass = innerClass;
        }

        public String getMethod(final AntOption antOption) {
            String variableName = WordUtils.uncapitalize(antOption.getArgName());
            return String.format("""
                            public void addConfigured%1$s(%1$s %2$s) {
                               addArg("%3$s", %2$s.value);
                            }%n""", innerClass, variableName, antOption.keyValue());
        }

        public String getPattern(final AntOption delegateOption, final AntOption antOption) {
            if (delegateOption.isAttribute()) {
                String fmt = "<rat:report %s='%s' />";
                return format(fmt, delegateOption.getName(), antOption.hasArg() ? antOption.getArgName() : "true");
            } else {
                String fmt = """
                    <rat:report>
                      <%1$s>
                        <%2$s>%3$s</%2$s>
                      </%1$s>
                    </rat:report>
                    """;
                return format(fmt, delegateOption.getName(), innerClass, antOption.getArgName());
            }
        }
    }

    /**
     * Get the method comment for this option.
     *
     * @param addParam if {@code true} the param annotation is added.
     * @return the Comment block for the function.
     */
    private static String getComment(final AntOption antOption, final boolean addParam) {
        StringBuilder sb = new StringBuilder();
        String desc = antOption.getDescription();
        if (desc == null) {
            throw new IllegalStateException(format("Description for %s may not be null", antOption.getName()));
        }
        if (!desc.contains(".")) {
            throw new IllegalStateException(format("First sentence of description for %s must end with a '.'", antOption.getName()));
        }
        if (addParam) {
            String arg;
            if (antOption.hasArg()) {
                arg = desc.substring(desc.indexOf(" ") + 1, desc.indexOf(".") + 1);
                arg = WordUtils.capitalize(arg.substring(0, 1)) + arg.substring(1);
            } else {
                arg = "The state";
            }
            if (antOption.getArgName() != null) {
                Supplier<String> sup = OptionCollection.getArgumentTypes().get(antOption.getArgName());
                if (sup == null) {
                    throw new IllegalStateException(format("Argument type %s must be in OptionCollection.ARGUMENT_TYPES", antOption.getArgName()));
                }
                desc = format("%s Argument%s should be %s%s. (See Argument Types for clarification)", desc, antOption.hasArgs() ? "s" : "",
                        antOption.hasArgs() ? "" : "a ", antOption.getArgName());
            }
            sb.append(format("    /**%n     * %s%n     * @param %s %s%n", StringEscapeUtils.escapeHtml4(desc), antOption.getName(),
                    StringEscapeUtils.escapeHtml4(arg)));
        } else {
            sb.append(format("    /**%n     * %s%n", StringEscapeUtils.escapeHtml4(desc)));
        }
        if (antOption.isDeprecated()) {
            sb.append(format("     * @deprecated %s%n", StringEscapeUtils.escapeHtml4(antOption.getDeprecated())));
        }
        return sb.append(format("     */%n")).toString();
    }

    /**
     * Get the signature of the attribute function.
     *
     * @return the signature of the attribute function.
     */
    public static String getAttributeFunctionName(final AntOption antOption) {
        return "set" +
                WordUtils.capitalize(antOption.getName()) +
                (antOption.hasArg() ? "(String " : "(boolean ") +
                antOption.getName() +
                ")";
    }

}
