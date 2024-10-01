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

import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.commandline.Arg;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

public abstract class AbstractOption {
    /** The pattern to match CLI options in text */
    protected static final Pattern pattern = Pattern.compile( "\\-(\\-[a-z0-9]+){1,}");
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

    abstract protected String cleanupName(final Option option);

    /**
     * Replaces CLI pattern options with Maven pattern options.
     * @param str the string to clean.
     * @return the string with CLI names replaced with Maven names.
     */
    protected String cleanup(String str) {
        if (StringUtils.isNotBlank(str)) {
            Map<String, String> maps = new HashMap<>();
            Matcher matcher = pattern.matcher(str);
            while (matcher.find()) {
                String key = matcher.group();
                String optKey = key.substring(2);
                Optional<Option> maybeResult = Arg.getOptions().getOptions().stream().filter(o -> optKey.equals(o.getOpt()) || optKey.equals(o.getLongOpt())).findFirst();
                if (maybeResult.isPresent()) {
                    maps.put(key, cleanupName(maybeResult.get()));
                }
            }
            for (Map.Entry<String, String> entry : maps.entrySet()) {
                str = str.replaceAll(Pattern.quote(format("%s", entry.getKey())), entry.getValue());
            }
        }
        return str;
    }

    /**
     * Gets the Maven name for the CLI option.
     * @return The Maven name for the CLI option.
     */
    final public String getName() {
        return name;
    }

    /**
     * Gets the description escaped for XML format.
     *
     * @return the description or an empty string.
     */
    final public String getDescription() {
        return cleanup(option.getDescription());
    }

    /**
     * Gets the simple class name for the data type for this option.
     * Normally "String".
     * @return the simple class name for the type.
     */
    final public Class<?> getType() {
        return option.hasArg() ? ((Class<?>) option.getType()) : boolean.class;
    }

    /**
     * Gets the argument name if there is one.
     * @return the Argument name
     */
    final public String getArgName() {
        return option.getArgName();
    }

    /**
     * Determines if the option is deprecated.
     * @return {@code true} if the option is deprecated
     */
    final public boolean isDeprecated() {
        return option.isDeprecated();
    }

    /**
     * Determines if the option is required.
     * @return {@code true} if the option is required.
     */
    final public boolean isRequired() {
        return option.isRequired();
    }

    /**
     * Determine if the enclosed option expects an argument.
     * @return {@code true} if the enclosed option expects at least one argument.
     */
    final public boolean hasArg() {
        return option.hasArg();
    }

    /**
     * Returns {@code true} if the option has multiple arguments.
     * @return {@code true} if the option has multiple arguments.
     */
    final public boolean hasArgs() {
        return option.hasArgs();
    }

    /**
     * The key value for the option.
     * @return the key value for the CLI argument map.
     */
    final public String keyValue() {
        return format("\"%s\"", StringUtils.defaultIfEmpty(option.getLongOpt(), option.getOpt()));
    }

    /**
     * Gets the deprecated string if the option is deprecated, or an empty string otherwise.
     * @return the deprecated string if the option is deprecated, or an empty string otherwise.
     */
    final public String getDeprecated() {
        return  option.isDeprecated() ? cleanup(StringUtils.defaultIfEmpty(option.getDeprecated().toString(), StringUtils.EMPTY)) : StringUtils.EMPTY;
    }
}
