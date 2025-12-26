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
package org.apache.rat.maven;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.collections4.map.UnmodifiableMap;
import org.apache.commons.collections4.set.UnmodifiableSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.commandline.Arg;
import org.apache.rat.ui.AbstractOption;
import org.apache.rat.ui.OptionFactory;
import org.apache.rat.utils.CasedString;

import static java.lang.String.format;

/**
 * A representation of a CLI option as a Maven option
 */
public final class MavenOption extends AbstractOption<MavenOption> {

    /**
     * Default values for CLI options
     */
    public static final UnmodifiableMap<Arg, String> DEFAULT_VALUES;
    /**
     * List of CLI options that are not supported by Maven.
     */
    public static final UnmodifiableSet<Option> UNSUPPORTED_SET;
    /** A mapping of external name to internal name if not standard. */
    public static final UnmodifiableMap<String, String> RENAME_MAP;
    /** The additional options for the maven plugin */
    static final Options ADDITIONAL_OPTIONS = new Options();
    /**
     * Filter to remove options not supported by Maven.
     */
    static final Predicate<Option> MAVEN_FILTER;
    /**
     * The OptionFactory configuration for MavenOptions.
     */
    public static final OptionFactory.Config<MavenOption> FACTORY_CONFIG;


    static {
        HashMap<String, String> map = new HashMap<>();
        map.put("addLicense", "add-license");
        RENAME_MAP = (UnmodifiableMap<String, String>) UnmodifiableMap.unmodifiableMap(map);
        HashMap<Arg, String> values = new HashMap<>();
        values.put(Arg.OUTPUT_FILE, "${project.build.directory}/rat.txt");
        DEFAULT_VALUES = (UnmodifiableMap<Arg, String>) UnmodifiableMap.unmodifiableMap(values);

        Set<Option> unsupportedOptions = new HashSet<>();
        unsupportedOptions.addAll(Arg.DIR.group().getOptions());
        unsupportedOptions.addAll(Arg.LOG_LEVEL.group().getOptions());
        UNSUPPORTED_SET = (UnmodifiableSet<Option>) UnmodifiableSet.unmodifiableSet(unsupportedOptions);

        MAVEN_FILTER =  option -> !(UNSUPPORTED_SET.contains(option) || option.getLongOpt() == null);
        FACTORY_CONFIG = new OptionFactory.Config<>(MavenOption.MAVEN_FILTER, MavenOption::new, MavenOption.ADDITIONAL_OPTIONS);
    }

    /**
     * Constructor.
     *
     * @param option The CLI option
     */
    public MavenOption(final Option option) {
        super(option, createName(option));
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
    public Options getAdditionalOptions() {
        return ADDITIONAL_OPTIONS;
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

    @Override
    public String getExample() {
        if (UNSUPPORTED_SET.contains(option)) {
            return "-- not supported --";
        }
        if (hasArg()) {
            return format("<%1$s>%2$s</%1$s>", getName(), getArgName());
        } else {
            return format("<%s />", getName());
        }
    }

    public String getExample(final String[] args) {
        StringBuilder sb = new StringBuilder("<").append(getName());
        if (hasArg()) {
            sb.append(">");
            if (hasArgs()) {
                sb.append(System.lineSeparator());
                for (String arg : args) {
                    sb.append(String.format("  <%1$s>%2$s</%1$s>%n", getArgName(), arg));
                }
            } else {
                sb.append(args[0]);
            }
            sb.append("</").append(getName()).append(">");
        } else {
            sb.append("/>");
        }
        return sb.toString();
    }
}
