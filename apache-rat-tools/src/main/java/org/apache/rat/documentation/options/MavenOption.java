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
import org.apache.commons.text.WordUtils;
import org.apache.rat.OptionCollection;
import org.apache.rat.commandline.Arg;
import org.apache.rat.utils.CasedString;

import static java.lang.String.format;

/**
 * A representation of a CLI option as a Maven option
 */
public class MavenOption extends AbstractOption {
    /**
     * Default values for CLI options
     */
    private static final Map<Arg, String> DEFAULT_VALUES = new HashMap<>();
    /**
     * List of CLI Options that are not supported by Maven.
     */
    private static final Set<Option> UNSUPPORTED_LIST = new HashSet<>();
    /** A mapping of external name to internal name if not standard */
    private static final Map<String, String> RENAME_MAP = new HashMap<>();

    /**
     * Filter to remove Options not supported by Maven.
     */
    private static final Predicate<Option> MAVEN_FILTER;


    static {
        RENAME_MAP.put("addLicense", "add-license");
        DEFAULT_VALUES.put(Arg.OUTPUT_FILE, "${project.build.directory}/rat.txt");
        UNSUPPORTED_LIST.addAll(Arg.DIR.group().getOptions());
        UNSUPPORTED_LIST.addAll(Arg.LOG_LEVEL.group().getOptions());
        UNSUPPORTED_LIST.add(OptionCollection.HELP);

        Set<Option> filteredOptions = getFilteredOptions();
        MAVEN_FILTER =  option -> !(filteredOptions.contains(option) || option.getLongOpt() == null);
    }

    public static List<MavenOption> getMavenOptions() {
        return OptionCollection.buildOptions().getOptions().stream().filter(MAVEN_FILTER)
                .map(MavenOption::new).collect(Collectors.toList());
    }

    /**
     * Constructor.
     *
     * @param option The CLI option
     */
    public MavenOption(final Option option) {
        super(option, createName(option));
    }

    public static Map<String, String> getRenameMap() {
        return Collections.unmodifiableMap(RENAME_MAP);
    }

    /**
     * Gets the set of options that are not supported by Ant.
     *
     * @return The set of options that are not supported by Ant.
     */
    public static Set<Option> getFilteredOptions() {
        return Collections.unmodifiableSet(UNSUPPORTED_LIST);
    }

    /**
     * Creates the Maven element name for the specified option.
     * @param option The option to process.
     * @return the Maven based name in camel-case syntax.
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

    public String getPropertyAnnotation(final String fname) {
        StringBuilder sb = new StringBuilder("@Parameter");
        String property = option.hasArgs() ? null : format("property = \"rat.%s\"", fname);
        String defaultValue = option.isDeprecated() ? null : getDefaultValue();
        if (property != null || defaultValue != null) {
            sb.append("(");
            if (property != null) {
                sb.append(property).append(defaultValue != null ? ", " : StringUtils.EMPTY);
            }
            if (defaultValue != null) {
                sb.append(format("defaultValue = \"%s\"", defaultValue));
            }
            sb.append(")");
        }
        return sb.toString();
    }

    public String getMethodSignature(final String indent, final boolean multiple) {
        StringBuilder sb = new StringBuilder();
        if (isDeprecated()) {
            sb.append(format("%s@Deprecated%n", indent));
        }
        String fname = WordUtils.capitalize(name);
        String args = option.hasArg() ? "String" : "boolean";
        if (multiple) {
            if (!(fname.endsWith("s") || fname.endsWith("Approved") || fname.endsWith("Denied"))) {
                fname = fname + "s";
            }
            args = args + "[]";
        }

        return sb.append(format("%1$s%5$s%n%1$spublic void set%3$s(%4$s %2$s)",
                        indent, name, fname, args, getPropertyAnnotation(fname)))
                .toString();
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
