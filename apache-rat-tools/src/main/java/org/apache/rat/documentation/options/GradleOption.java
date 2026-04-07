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
package org.apache.rat.documentation.options;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.WordUtils;
import org.apache.rat.OptionCollection;
import org.apache.rat.commandline.Arg;
import org.apache.rat.commandline.Converters;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.utils.CasedString;

import static java.lang.String.format;

/**
 * A representation of a CLI option for a Gradle plugin extension and task property.
 */
public class GradleOption extends AbstractOption {
    /**
     * Default values for CLI options
     */
    private static final Map<Arg, String> DEFAULT_VALUES = new HashMap<>();
    /**
     * List of CLI options that are not supported by Gradle.
     */
    private static final Set<Option> UNSUPPORTED_LIST = new HashSet<>();
    /** A mapping of external name to internal name if not standard. */
    private static final Map<String, String> RENAME_MAP = new HashMap<>();

    /**
     * Filter to remove options not supported by Gradle.
     */
    private static final Predicate<Option> GRADLE_FILTER;

    static {
        RENAME_MAP.put("addLicense", "add-license");
        DEFAULT_VALUES.put(Arg.OUTPUT_FILE, "${project.build.directory}/rat.txt"); // TODO isn't this HTML?
        UNSUPPORTED_LIST.addAll(Arg.OUTPUT_FILE.group().getOptions());
        UNSUPPORTED_LIST.addAll(Arg.DIR.group().getOptions());
        UNSUPPORTED_LIST.addAll(Arg.LOG_LEVEL.group().getOptions());
        UNSUPPORTED_LIST.add(OptionCollection.HELP);

        Set<Option> filteredOptions = getFilteredOptions();
        GRADLE_FILTER = option -> !(filteredOptions.contains(option) || option.getLongOpt() == null);
    }

    public static List<GradleOption> getGradleOptions() {
        return OptionCollection.buildOptions().getOptions().stream().filter(GRADLE_FILTER)
                .map(GradleOption::new).collect(Collectors.toList());
    }

    /**
     * Constructor.
     *
     * @param option The CLI option
     */
    public GradleOption(final Option option) {
        super(option, createName(option));
    }

    public static Map<String, String> getRenameMap() {
        return Collections.unmodifiableMap(RENAME_MAP);
    }

    /**
     * Gets the set of options that are not supported by Gradle.
     *
     * @return The set of options that are not supported by Gradle.
     */
    public static Set<Option> getFilteredOptions() {
        return Collections.unmodifiableSet(UNSUPPORTED_LIST);
    }

    /**
     * Creates the Gradle element name for the specified option.
     * @param option The option to process.
     * @return the Gradle based name in camel-case syntax.
     */
    static String createName(final Option option) {
        String name = StringUtils.defaultIfEmpty(option.getLongOpt(), option.getOpt());
        name = StringUtils.defaultIfEmpty(RENAME_MAP.get(name), name).toLowerCase(Locale.ROOT);
        return new CasedString(CasedString.StringCase.KEBAB, name).toCase(CasedString.StringCase.CAMEL);
    }

    @Override
    protected String cleanupName(final Option option) {
        return format("<%s>", createName(option));
    }

    @Override
    public String getText() {
        return cleanupName(option);
    }

    @Override
    public String getDefaultValue() {
        Arg arg = Arg.findArg(option);
        String result = DEFAULT_VALUES.get(arg);
        if (result == null) {
            result = arg.defaultValue();
        }
        return result;
    }

    public String getPropertyFunctionName() {
        boolean multiple = option.hasArgs();
        String fname = WordUtils.capitalize(name);
        if (multiple) {
            if (!(fname.endsWith("s") || fname.endsWith("Approved") || fname.endsWith("Denied"))) {
                fname = fname + "s";
            }
        }
        return "get" + fname;
    }

    public String getMethodSignature(final String indent) {
        StringBuilder sb = new StringBuilder();
        if (isDeprecated()) {
            sb.append(format("%s@Deprecated%n", indent));
        }

        Class<?> optionType = (Class<?>) option.getType();
        if (optionType == File.class) {
            String propertyType = "RegularFileProperty";
            if (option.hasArgs()) {
                propertyType = "ConfigurableFileCollection";
            }
            return sb.append(format("%1$s%3$s %2$s()",
                            indent, getPropertyFunctionName(), propertyType))
                    .toString();
        } else {

            String propertyType = "Property";
            if (option.hasArgs()) {
                propertyType = "ListProperty";
            }

            String argType;
            if (option.hasArg()) {
                if (optionType.isEnum()) {
                    argType = optionType.getName();
                } else if (optionType == Integer.class) {
                    argType = "Integer";
                } else if (optionType == Long.class) {
                    argType = "Long";
                } else if (optionType == Pair.class && option.getConverter() == Converters.COUNTER_CONVERTER) {
                    argType = ClaimStatistic.Counter.class.getName().replace('$', '.') + ", Integer";
                    propertyType = "MapProperty";
                } else {
                    argType = "String";
                }
            } else {
                argType = "Boolean";
            }

            return sb.append(format("%1$s%3$s<%4$s> %2$s()",
                            indent, getPropertyFunctionName(), propertyType, argType))
                    .toString();
        }
    }

    public String getConverterBody(final String indent) {
        StringBuilder sb = new StringBuilder();

        String propFunc = format("options.%1$s()", getPropertyFunctionName());
        if (option.hasArg()) {
            if (option.getType() == File.class) {
                String propertyType = "RegularFileProperty";
                if (option.hasArgs()) {
                    // ConfigurableFileCollection
                    sb.append(indent).append("for (File file : options.getConfigs().getFiles()) {").append(System.lineSeparator());
                    sb.append(indent).append(format("  addArg(%1$s, file.getAbsolutePath());", keyValue())).append(System.lineSeparator());
                } else {
                    // RegularFileProperty
                    sb.append(indent).append(format("if (%1$s.isPresent()) {",
                            propFunc)).append(System.lineSeparator());
                    sb.append(indent).append(format("    setArg(%1$s, %2$s.get().getAsFile().getAbsolutePath());",
                            keyValue(), propFunc)).append(System.lineSeparator());
                }
                sb.append(indent).append("}").append(System.lineSeparator());
            } else {
                sb.append(indent).append(format("if (%1$s.isPresent()) {",
                        propFunc)).append(System.lineSeparator());
                if (option.hasArgs()) {
                    if (option.getType() == Pair.class && option.getConverter() == Converters.COUNTER_CONVERTER) {
                        // Gradle: MapProperty<X, Y>
                        sb.append(indent).append(format("    for (Map.Entry<?, ?> entry : %1$s.get().entrySet()) {",
                                propFunc)).append(System.lineSeparator());
                        sb.append(indent).append(format("        addArg(%1$s, entry.getKey().toString() + ':' + entry.getValue());",
                                keyValue())).append(System.lineSeparator());
                        sb.append(indent).append("    }").append(System.lineSeparator());
                    } else {
                        // Gradle: ListProperty<X>
                        sb.append(indent).append(format("    for (Object elem : %1$s.get()) {",
                                propFunc)).append(System.lineSeparator());
                        sb.append(indent).append(format("        addArg(%1$s, elem.toString());",
                                keyValue())).append(System.lineSeparator());
                        sb.append(indent).append("    }").append(System.lineSeparator());
                    }
                } else {
                    // Gradle: Property<X>
                    sb.append(indent).append(format("    setArg(%1$s, %2$s.get().toString());",
                            keyValue(), propFunc)).append(System.lineSeparator());
                }
                sb.append(indent).append("}").append(System.lineSeparator());
            }
        } else {
            // Gradle: Property<Boolean>
            sb.append(indent).append(format("if (%1$s.isPresent() && %1$s.get()) {",
                    propFunc)).append(System.lineSeparator());
            sb.append(indent).append(format("    setArg(%1$s, null);",
                    keyValue())).append(System.lineSeparator());
            sb.append(indent).append("} else {").append(System.lineSeparator());
            sb.append(indent).append(format("    removeArg(%1$s);",
                    keyValue())).append(System.lineSeparator());
            sb.append(indent).append("}").append(System.lineSeparator());
        }

        return sb.toString();
    }

    public String getTaskBaseOverride(final String indent) {
        StringBuilder sb = new StringBuilder();

        sb.append(indent).append("@Override")
                .append(System.lineSeparator());
        String inputAnnotation = "@Input";
        if (option.getType() == File.class) {
            sb.append(indent).append("@PathSensitive(PathSensitivity.RELATIVE)")
                    .append(System.lineSeparator());
            if (option.hasArgs()) {
                inputAnnotation = "@InputFiles";
            } else {
                inputAnnotation = "@InputFile";
            }
        }
        sb.append(indent).append(inputAnnotation)
                .append(System.lineSeparator());
        sb.append(indent).append("@Optional")
                .append(System.lineSeparator());
        sb.append(getMethodSignature(indent)).append(";").append(System.lineSeparator());

        return sb.toString();
    }


    @Override
    public String getExample() {
        if (UNSUPPORTED_LIST.contains(option)) {
            return "-- not supported --";
        }
        if (hasArg()) {
            return format("<%1$s>%2$s</%1$s>", getName(), getArgName());
        } else {
            return format("<%s />", getName());
        }
    }
}
