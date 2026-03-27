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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.cli.Option;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.rat.OptionCollection;
import org.apache.rat.documentation.options.GradleOption;
import org.apache.rat.utils.CasedString;
import org.apache.rat.utils.CasedString.StringCase;

import static java.lang.String.format;

/**
 * A simple tool to convert CLI options to Gradle types.
 */
public final class GradleGenerator {

    private GradleGenerator() {
    }

    private static String argsKey(final Option option) {
        return StringUtils.defaultIfEmpty(option.getLongOpt(), option.getOpt());
    }

    /**
     * Creates the Gradle interfaces and configuration converter class.
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
        if (args == null || args.length < 3) {
            System.err.println("At least three arguments are required: package, simple class name, target directory.");
            return;
        }

        String packageName = args[0];
        String className = args[1];
        String destDir = args[2];
        List<GradleOption> options = GradleOption.getGradleOptions();
        String pkgName = String.join(File.separator, new CasedString(StringCase.DOT, packageName).getSegments());
        File packageDir = new File(new File(destDir), pkgName);
        generateFile("/GradleOptions.tpl", packageDir, className, className, options, packageName, false);
        generateFile("/GradleTaskBase.tpl", packageDir, className + "TaskBase", className, options, packageName, false);
        generateFile("/GradleConfiguration.tpl", packageDir, className + "ToConfiguration", className, options, packageName, true);
    }

    private static void generateFile(final String templateResource, final File packageDir, final String className, final String baseName,
                                     final List<GradleOption> options, final String packageName, final boolean isInternal) throws IOException {
        File dir = isInternal ? new File(packageDir, "internal") : packageDir;
        File file = new File(dir, className + ".java");
        System.out.println("Creating " + file);
        file.getParentFile().mkdirs();
        try (InputStream template = GradleGenerator.class.getResourceAsStream(templateResource);
             FileWriter writer = new FileWriter(file)) {
            if (template == null) {
                throw new RuntimeException("Template /Gradle.tpl not found");
            }
            LineIterator iter = IOUtils.lineIterator(new InputStreamReader(template, StandardCharsets.UTF_8));
            while (iter.hasNext()) {
                String line = iter.next();
                switch (line.trim()) {
                    case "${static}":
                        for (Map.Entry<String, String> entry : GradleOption.getRenameMap().entrySet()) {
                            writer.append(format("        xlateName.put(\"%s\", \"%s\");%n", entry.getKey(), entry.getValue()));
                        }
                        for (Option option : GradleOption.getFilteredOptions()) {
                            writer.append(format("        unsupportedArgs.add(\"%s\");%n", argsKey(option)));
                        }
                        for (GradleOption option : options) {
                            if (option.isDeprecated()) {
                                writer.append(format("        deprecatedArgs.put(\"%s\", \"%s\");%n", argsKey(option.getOption()),
                                        format("Use of deprecated option '%s'. %s", option.getName(), option.getDeprecated())));
                            }
                        }
                        break;
                    case "${properties}":
                        writeProperties(writer, options);
                        break;
                    case "${applyConventions}":
                        writeApplyConventions(writer, options, baseName);
                        break;
                    case "${converterBody}":
                        writeConverterMethodBody(writer, options);
                        break;
                    case "${taskBaseBody}":
                        writeTaskBaseBody(writer, options);
                        break;
                    case "${package}":
                        writer.append(format("package %s%s;%n", packageName, isInternal ? ".internal" : ""));
                        break;
                    case "${baseImport}":
                        writer.append(format("import %s.*;%n", packageName));
                        break;
                    case "${interface}":
                        writer.append(format("public interface %s {%n", className));
                        break;
                    case "${taskBaseInterface}":
                        writer.append(format("public interface %1$s extends %2$s {%n", className, baseName));
                        break;
                    case "${class}":
                        writer.append(format("public class %s {%n", className));
                        break;
                    case "${constructor}":
                        writer.append(format("    public %1$s(%2$s options) {%n", className, baseName));
                        break;
                    case "${constructorBody}":
                        writer.append("            setDeprecationReporter();").append(System.lineSeparator());
                        break;
                    case "${commonArgs}":
                        try (InputStream argsTpl = GradleGenerator.class.getResourceAsStream("/Args.tpl")) {
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

    private static String getComment(final GradleOption option) {
        String desc = option.getDescription();
        if (desc == null) {
            throw new IllegalStateException(format("Description for %s may not be null", option.getName()));
        }
        if (!desc.contains(".")) {
            throw new IllegalStateException(format("First sentence of description for %s must end with a '.'", option.getName()));
        }
        if (option.hasArg() && option.getArgName() != null) {
            Supplier<String> sup = OptionCollection.getArgumentTypes().get(option.getArgName());
            if (sup == null) {
                throw new IllegalStateException(format("Argument type %s must be in OptionCollection.ARGUMENT_TYPES", option.getArgName()));
            }
            desc = format("%s Argument%s should be %s%s. (See Argument Types for clarification)", desc, option.hasArgs() ? "s" : "",
                    option.hasArgs() ? "" : "a ", option.getArgName());
        }
        StringBuilder sb = new StringBuilder()
                .append(format("    /**%n     * %s%n", StringEscapeUtils.escapeHtml4(desc)));
        if (option.isDeprecated()) {
            sb.append(format("     * @deprecated %s%n", StringEscapeUtils.escapeHtml4(option.getDeprecated())));
        }
        return sb.append(format("     */%n")).toString();
    }

    private static void writeApplyConventions(final FileWriter writer, final List<GradleOption> options, final String baseName) throws IOException {
        writer.append(format("    default void applyConventions(%1$s from) {", baseName))
                .append(System.lineSeparator());
        for (GradleOption option : options) {
            writer.append(format("        %1$s().convention(from.%1$s());", option.getPropertyFunctionName()))
                    .append(System.lineSeparator());
        }
        writer.append("    }")
                .append(System.lineSeparator());
    }

    private static void writeConverterMethodBody(final FileWriter writer, final List<GradleOption> options) throws IOException {
        for (GradleOption option : options) {
            writer.append(option.getConverterBody("            "))
                    .append(System.lineSeparator());
        }
    }

    private static void writeProperties(final FileWriter writer, final List<GradleOption> options) throws IOException {
        for (GradleOption option : options) {
            writer.append(getComment(option))
                    .append(option.getMethodSignature("    ")).append(";").append(System.lineSeparator());
        }
    }

    private static void writeTaskBaseBody(final FileWriter writer, final List<GradleOption> options) throws IOException {
        for (GradleOption option : options) {
            writer.append(option.getTaskBaseOverride("    "))
                    .append(System.lineSeparator());
        }
    }
}
