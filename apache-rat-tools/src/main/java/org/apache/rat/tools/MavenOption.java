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

import static java.lang.String.format;

import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.rat.commandline.Arg;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A representation of a CLI option as a Maven option
 */
public class MavenOption {
    /** The pattern to match CLI options in text */
    private static final Pattern pattern = Pattern.compile( "\\-(\\-[a-z0-9]+){1,}");
    /** The CLI that the Maven option is wrapping */
    private final Option option;
    /** The Maven name for the option */
    private final String name;

    private static final Map<Arg,String> DEFAULT_VALUES = new HashMap<>();

    static {
        DEFAULT_VALUES.put(Arg.OUTPUT_FILE, "${project.build.directory}/rat.txt");
    }

    /**
     * Constructor.
     *
     * @param option The CLI option
     */
    MavenOption(final Option option) {
        this.option = option;
        this.name = MavenGenerator.createName(option);
    }

    /**
     * Gets the Maven name for the CLI option.
     * @return The Maven name for the CLI option.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the description escaped for XML format.
     *
     * @return the description or an empty string.
     */
    public String getDescription() {
        return cleanup(option.getDescription());
    }

    private String getPropertyValue() {
        StringBuilder sb = new StringBuilder("A ");
        if (option.hasArg()) {
            if (option.hasArgs()) {
                sb.append("collection of ");
            }
            if (option.getArgName() == null) {
                sb.append("String");
            } else {
                sb.append(option.getArgName());
            }
            sb.append(option.hasArgs() ? "s." : ".");
        } else {
            sb.append("boolean value.");
        }
        return sb.toString();
    }

    /**
     * Returns the value as an POM xml node.
     * @return the pom xml node.
     */
    public String xmlNode() {
        return format("<%1$s>", name);
    }

    /**
     * Gets the simple class name for the data type for this option.
     * Normally "String".
     *
     * @return the simple class name for the type.
     */
    public Class<?> getType() {
        return option.hasArg() ? ((Class<?>) option.getType()) : boolean.class;
    }

    public String getArgName() {
        return option.getArgName();
    }


    public boolean isDeprecated() {
        return option.isDeprecated();
    }

    /**
     * Determine if true if the enclosed option expects an argument.
     *
     * @return {@code true} if the enclosed option expects at least one argument.
     */
    public boolean hasArg() {
        return option.hasArg();
    }

    /**
     * the key value for the option.
     *
     * @return the key value for the CLI argument map.
     */
    public String keyValue() {
        return "\"" + StringUtils.defaultIfEmpty(option.getLongOpt(), option.getOpt()) + "\"";
    }

    public String getDeprecated() {
        return  option.isDeprecated() ? cleanup(StringUtils.defaultIfEmpty(option.getDeprecated().toString(), StringUtils.EMPTY)) : StringUtils.EMPTY;
    }

    private String cleanup(String str) {
        if (StringUtils.isNotBlank(str)) {
            Map<String, String> maps = new HashMap<>();
            Matcher matcher = pattern.matcher(str);
            while (matcher.find()) {
                String key = matcher.group();
                String optKey = key.substring(2);
                Optional<Option> maybeResult = Arg.getOptions().getOptions().stream().filter(o -> optKey.equals(o.getOpt()) || optKey.equals(o.getLongOpt())).findFirst();
                if (maybeResult.isPresent()) {
                    MavenOption replacement = new MavenOption(maybeResult.get());
                    maps.put(key, replacement.xmlNode());
                }
            }
            for (Map.Entry<String, String> entry : maps.entrySet()) {
                str = str.replaceAll(Pattern.quote(format("%s", entry.getKey())), entry.getValue());
            }
        }
        return str;
    }

    public String getDefaultValue() {
        Arg arg = Arg.findArg(option);
        String result = DEFAULT_VALUES.get(arg);
        if (result == null) {
            result = arg.defaultValue();
        }
        return result;
    }
    
    public String getPropertyAnnotation(String fname) {
        StringBuilder sb = new StringBuilder("@Parameter");
        String property = option.hasArgs() ? null : format("property = \"rat.%s\"", fname);
        String defaultValue = getDefaultValue();
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

    /**
     * Returns {@code true} if the option has multiple arguments.
     * @return {@code true} if the option has multiple arguments.
     */
    public boolean hasArgs() {
        return option.hasArgs();
    }
}
