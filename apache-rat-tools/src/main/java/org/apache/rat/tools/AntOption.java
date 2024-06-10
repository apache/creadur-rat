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
 * A class that wraps the CLI option and provides Ant specific values.
 */
public class AntOption {
    /** The CLI option we are wrapping */
    private final Option option;
    /** An uncapitalized name */
    private final String name;

    /**
     * Constructor.
     * @param option the option to wrap.
     */
    AntOption(final Option option) {
        this.option = option;
        name = AntGenerator.createName(option);
    }

    /**
     * Gets the Ant name for the option.
     * @return the Ant name for the option.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns {@code true} if the option should be an attribute of the &lt;rat:report&gt; element.
     *
     * @return {@code true} if the option should be an attribute of the &lt;rat:report&gt; element.
     */
    public boolean isAttribute() {
        return !option.hasArgs();
    }

    /**
     * Returns {@code true} if the option should be a child element of the &lt;rat:report&gt; element.
     *
     * @return {@code true} if the option should be a child element of the &lt;rat:report&gt; element.
     */
    public boolean isElement() {
        return !isAttribute() || option.getType() != String.class;
    }

    /**
     * Returns {@code true} if the enclosed option has one or more arguments.
     *
     * @return {@code true} if the enclosed option has one or more arguments.
     */
    public boolean hasArg() {
        return option.hasArg();
    }

    /**
     * Returns The key value for the option.   This is the long opt enclosed in quotes and with leading dashes.
     *
     * @return The key value for the option.
     */
    public String keyValue() {
        return format("\"--%s\"", option.getLongOpt());
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
     * Get the method comment for this option.
     *
     * @param addParam if {@code true} the param annotation is added.
     * @return the Comment block for the function.
     */
    public String getComment(final boolean addParam) {
        StringBuilder sb = new StringBuilder()
                .append(format("    /**%n     * %s%n", getDescription()));
        if (option.isDeprecated()) {
            sb.append(format("     * %s%n     * @deprecated%n", option.getDeprecated()));
        }
        if (addParam && option.hasArg()) {
            sb.append(format("     * @param %s The value to set%n", name));
        }
        return sb.append(format("     */%n")).toString();
    }

    /**
     * Get the signature of th eattribute function.
     *
     * @return the signature of the attribue function.
     */
    public String getAttributeFunctionName() {
        return "set" +
                WordUtils.capitalize(name) +
                (option.hasArg() ? "(String " : "(boolean ") +
                name +
                ")";
    }
}
