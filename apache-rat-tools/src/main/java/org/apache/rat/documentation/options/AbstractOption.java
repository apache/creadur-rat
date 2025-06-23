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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.OptionCollection;
import org.apache.rat.commandline.Arg;

import static java.lang.String.format;

public abstract class AbstractOption {
    /** The pattern to match CLI options in text */
    protected static final Pattern PATTERN = Pattern.compile("-(-[a-z0-9]+)+");
    /** The CLI that the Maven option is wrapping */
    protected final Option option;
    /** The name for the option */
    protected final String name;
    /** The argument type for this option */
    protected final OptionCollection.ArgumentType argumentType;

    /**
     * Constructor.
     *
     * @param option The CLI option
     * @param name the name for the option.
     */
    AbstractOption(final Option option, final String name) {
        this.option = option;
        this.name = name;
        argumentType = option.hasArg() ?
                option.getArgName() == null ? OptionCollection.ArgumentType.ARG :
                OptionCollection.ArgumentType.valueOf(option.getArgName().toUpperCase(Locale.ROOT)) :
                OptionCollection.ArgumentType.NONE;
    }

    /**
     * Gets the option this abstract option is wrapping.
     * @return the original Option.
     */
    public Option getOption() {
        return option;
    }

    /**
     * Return default value.
     * @return default value or {@code null} if no argument given.
     */
    public String getDefaultValue() {
        Arg arg = Arg.findArg(option);
        return arg == null ? null : arg.defaultValue();
    }

    protected abstract String cleanupName(Option option);

    /**
     * Gets an example of how to use this option in the native UI.
     * @return An example of how to use this option in the native UI.
     */
    public abstract String getExample();

    /**
     * Gets this option's cleaned up name.
     * @return This option's cleaned up name.
     */
    public String cleanupName() {
        return cleanupName(option);
    }
    /**
     * Replaces CLI pattern options with implementation specific pattern options.
     * @param str the string to clean.
     * @return the string with CLI names replaced with implementatin specific names.
     */
    public String cleanup(final String str) {
        String workingStr = str;
        if (StringUtils.isNotBlank(workingStr)) {
            Map<String, String> maps = new HashMap<>();
            Matcher matcher = PATTERN.matcher(workingStr);
            while (matcher.find()) {
                String key = matcher.group();
                String optKey = key.substring(2);
                Optional<Option> maybeResult = Arg.getOptions().getOptions().stream()
                        .filter(o -> optKey.equals(o.getOpt()) || optKey.equals(o.getLongOpt())).findFirst();
                maybeResult.ifPresent(value -> maps.put(key, cleanupName(value)));
            }
            for (Map.Entry<String, String> entry : maps.entrySet()) {
                workingStr = workingStr.replaceAll(Pattern.quote(format("%s", entry.getKey())), entry.getValue());
            }
        }
        return workingStr;
    }

    /**
     * Gets the implementation specific name for the CLI option.
     * @return The implementation specific name for the CLI option.
     */
    public final String getName() {
        return name;
    }

    /**
     * return a string showing long and short options if they are available.  Will return
     * a string.
     * @return A string showing long and short options if they are available.  Never {@code null}.
     */
    public abstract String getText();


    /**
     * Gets the description in implementation specific format.
     *
     * @return the description or an empty string.
     */
    public final String getDescription() {
        return cleanup(option.getDescription());
    }

    /**
     * Gets the simple class name for the data type for this option.
     * Normally "String".
     * @return the simple class name for the type.
     */
    public final Class<?> getType() {
        return option.hasArg() ? ((Class<?>) option.getType()) : boolean.class;
    }

    /**
     * Gets the argument name if there is one.
     * @return the Argument name
     */
    public final String getArgName() {
        return argumentType.getDisplayName();
    }

    /**
     * Gets the argument type if there is one.
     * @return the Argument name
     */
    public final OptionCollection.ArgumentType getArgType() {
        return argumentType;
    }

    /**
     * Determines if the option is deprecated.
     * @return {@code true} if the option is deprecated
     */
    public final boolean isDeprecated() {
        return option.isDeprecated();
    }

    /**
     * Determines if the option is required.
     * @return {@code true} if the option is required.
     */
    public final boolean isRequired() {
        return option.isRequired();
    }

    /**
     * Determine if the enclosed option expects an argument.
     * @return {@code true} if the enclosed option expects at least one argument.
     */
    public final boolean hasArg() {
        return option.hasArg();
    }

    /**
     * Returns {@code true} if the option has multiple arguments.
     * @return {@code true} if the option has multiple arguments.
     */
    public final boolean hasArgs() {
        return option.hasArgs();
    }

    /**
     * Returns the number of arguments.
     * @return The number of arguments.
     */
    public final int argCount() {
        return option.getArgs();
    }

    /**
     * The key value for the option.
     * @return the key value for the CLI argument map.
     */
    public final String keyValue() {
        return format("\"%s\"", StringUtils.defaultIfEmpty(option.getLongOpt(), option.getOpt()));
    }

    /**
     * Gets the deprecated string if the option is deprecated, or an empty string otherwise.
     * @return the deprecated string if the option is deprecated, or an empty string otherwise.
     */
    public final String getDeprecated() {
        return  option.isDeprecated() ? cleanup(StringUtils.defaultIfEmpty(option.getDeprecated().toString(), StringUtils.EMPTY)) : StringUtils.EMPTY;
    }
}
