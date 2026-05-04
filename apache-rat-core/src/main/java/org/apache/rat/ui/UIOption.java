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
package org.apache.rat.ui;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.OptionCollectionParser;
import org.apache.rat.utils.CasedString;

import static java.lang.String.format;

/**
 * Abstract class that provides the framework for UI-specific RAT options.
 * In this context UI option means an option expressed in the specific UI.
 * @param <T> the concrete implementation of AbstractOption.
 */
public abstract class UIOption<T extends UIOption<T>> {
    /** The pattern to match CLI options in text */
    protected static final Pattern PATTERN = Pattern.compile("-(-[a-z0-9]+)+");
    /** The actual UI-specific name for the option */
    protected final Option option;
    /** The name for the option */
    protected final CasedString name;
    /** The argument type for this option */
    protected final OptionCollectionParser.ArgumentType argumentType;
    /** The AbstractOptionCollection associated with this AbstractOption */
    protected final UIOptionCollection<T> optionCollection;

    /**
     * Constructor.
     *
     * @param option The CLI option
     * @param name the UI-specific name for the option
     */
    protected <C extends UIOptionCollection<T>> UIOption(final C optionCollection, final Option option, final CasedString name) {
        this.optionCollection = optionCollection;
        this.option = option;
        this.name = name;
        OptionCollectionParser.ArgumentType argType;
        if (option.hasArg()) {
            if (option.getArgName() == null) {
                argType = OptionCollectionParser.ArgumentType.ARG;
            } else {
                // extract the name of the argument type.
                argType = OptionCollectionParser.ArgumentType.valueOf(option.getArgName().toUpperCase(Locale.ROOT));
            }
        } else {
            argType = OptionCollectionParser.ArgumentType.NONE;
        }
        this.argumentType = argType;
    }

    /**
     * Gets the AbstractOptionCollection that this option is a member of.
     * @return the AbstractOptionCollection that this option is a member of.
     */
    public final <X extends UIOptionCollection<T>> X getOptionCollection() {
        return (X) optionCollection;
    }

    /**
     * Gets the option this abstract option is wrapping.
     * @return the original Option.
     */
    public final Option getOption() {
        return option;
    }

    /**
     * Return default value.
     * @return default value or {@code null} if no argument given.
     */
    public final String getDefaultValue() {
        return optionCollection.defaultValue(option);
    }

    /**
     * Provide means to wrap the given option depending on the UI-specific option implementation.
     * @param option The CLI option
     * @return the cleaned up option name.
     */
    protected abstract String cleanupName(Option option);

    /**
     * Gets an example of how to use this option in the native UI.
     * @return An example of how to use this option in the native UI.
     */
    public abstract String getExample();

    /**
     * Replaces CLI pattern options with implementation specific pattern options.
     * @param str the string to clean.
     * @return the string with CLI names replaced with implementation specific names.
     */
    public String cleanup(final String str) {
        String workingStr = str;
        if (StringUtils.isNotBlank(workingStr)) {
            Map<String, String> maps = new HashMap<>();
            Matcher matcher = PATTERN.matcher(workingStr);
            while (matcher.find()) {
                String key = matcher.group();
                String optKey = key.substring(2);
                Optional<Option> maybeResult = getOptionCollection().getOptions().getOptions().stream()
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
     * Gets the implementation specific name for the native UI.
     * @return The implementation specific name for the native UI.
     */
    public final String getName() {
        return name.toString();
    }

    /**
     * Gets the CasedString version of the name for the native UI..
     * @return the CasedString version of the name for the native UI..
     */
    public final CasedString getCasedName() {
        return name;
    }

    /**
     * return a string showing long and short options if they are available. Will return
     * a string.
     * @return A string showing long and short options if they are available. Never {@code null}.
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
    public final OptionCollectionParser.ArgumentType getArgType() {
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
     * The key value for the option.
     * @return the key value for the CLI argument map.
     */
    public final String keyValue() {
        return StringUtils.defaultIfEmpty(option.getLongOpt(), option.getOpt());
    }

    /**
     * Gets the deprecated string if the option is deprecated, or an empty string otherwise.
     * @return the deprecated string if the option is deprecated, or an empty string otherwise.
     */
    public final String getDeprecated() {
        return  option.isDeprecated() ? cleanup(StringUtils.defaultIfEmpty(option.getDeprecated().toString(), StringUtils.EMPTY)) : StringUtils.EMPTY;
    }
}
