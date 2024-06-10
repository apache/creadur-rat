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

/**
 * A representation of a CLI option as a Maven option
 */
public class MavenOption {
    /** The CLI that the Maven option is wrapping */
    private final Option option;
    /** The Maven name for the option */
    private final String name;

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
        return StringUtils.defaultIfEmpty(option.getDescription(), "")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    /**
     * Returns the value as an POM xml node.
     *
     * @param value the value
     * @return the pom xml node.
     */
    public String xmlNode(final String value) {
        return format("<%1$s>%2$s</%1$s>%n", name, value == null ? "false" : value);
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
        return "\"--" + option.getLongOpt() + "\"";
    }

    public String getDeprecated() {
        return option.getDeprecated().toString();
    }

    public String getMethodSignature(final String indent) {
        StringBuilder sb = new StringBuilder();
        if (isDeprecated()) {
            sb.append(format("%s@Deprecated%n", indent));
        }
        return sb.append(format("%1$s@Parameter(property = \"rat.%2$s\")%n%1$spublic void set%3$s(%4$s %2$s)",
                        indent, name, WordUtils.capitalize(name), option.hasArg() ? "String" : "boolean"))
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
