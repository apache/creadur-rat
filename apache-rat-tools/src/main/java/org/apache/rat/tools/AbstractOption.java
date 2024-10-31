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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.commandline.Arg;

import static java.lang.String.format;

public abstract class AbstractOption {
    /** The pattern to match CLI options in text */
    protected static final Pattern PATTERN = Pattern.compile("-(-[a-z0-9]+)+");
    /** The CLI that the Maven option is wrapping */
    protected final Option option;
    /** The Maven name for the option */
    protected final String name;

    /**
     * Constructor.
     *
     * @param option The CLI option
     */
    AbstractOption(final Option option, final String name) {
        this.option = option;
        this.name = name;
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
     * Replaces CLI pattern options with Maven pattern options.
     * @param str the string to clean.
     * @return the string with CLI names replaced with Maven names.
     */
    protected String cleanup(final String str) {
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
     * Gets the Maven name for the CLI option.
     * @return The Maven name for the CLI option.
     */
    public final String getName() {
        return name;
    }

    /**
     * Gets the description escaped for XML format.
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
        return option.getArgName();
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
