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

import java.util.function.Supplier;

import org.apache.commons.cli.Option;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.WordUtils;
import org.apache.rat.OptionCollection;

import static java.lang.String.format;

/**
 * A class that wraps the CLI option and provides Ant specific values.
 */
public class AntOption extends AbstractOption {

    /**
     * Constructor.
     * @param option the option to wrap.
     */
    public AntOption(final Option option) {
        super(option, AntGenerator.createName(option));
    }

    /**
     * Returns {@code true} if the option should be an attribute of the &lt;rat:report&gt; element.
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

    protected String cleanupName(final Option option) {
        AntOption antOption = new AntOption(option);
        String fmt = antOption.isAttribute() ? "%s attribute" : "<%s>";
        return  format(fmt, AntGenerator.createName(option));
    }

    /**
     * Get the method comment for this option.
     *
     * @param addParam if {@code true} the param annotation is added.
     * @return the Comment block for the function.
     */
    public String getComment(final boolean addParam) {
        StringBuilder sb = new StringBuilder();
        String desc = getDescription();
        if (desc == null) {
            throw new IllegalStateException(format("Description for %s may not be null", getName()));
        }
        if (!desc.contains(".")) {
            throw new IllegalStateException(format("First sentence of description for %s must end with a '.'", getName()));
        }
        if (addParam) {
            String arg;
            if (option.hasArg()) {
                arg = desc.substring(desc.indexOf(" ") + 1, desc.indexOf(".") + 1);
                arg = WordUtils.capitalize(arg.substring(0, 1)) + arg.substring(1);
            } else {
                arg = "The state";
            }
            if (option.getArgName() != null) {
                Supplier<String> sup = OptionCollection.getArgumentTypes().get(option.getArgName());
                if (sup == null) {
                    throw new IllegalStateException(format("Argument type %s must be in OptionCollection.ARGUMENT_TYPES", option.getArgName()));
                }
                desc = format("%s Argument%s should be %s%s. (See Argument Types for clarification)", desc, option.hasArgs() ? "s" : "",
                        option.hasArgs() ? "" : "a ", option.getArgName());
            }
            sb.append(format("    /**%n     * %s%n     * @param %s %s%n", StringEscapeUtils.escapeHtml4(desc), getName(),
             StringEscapeUtils.escapeHtml4(arg)));
        } else {
            sb.append(format("    /**%n     * %s%n", StringEscapeUtils.escapeHtml4(desc)));
        }
        if (option.isDeprecated()) {
            sb.append(format("     * @deprecated %s%n", StringEscapeUtils.escapeHtml4(getDeprecated())));
        }
        return sb.append(format("     */%n")).toString();
    }


    /**
     * Get the signature of the attribute function.
     *
     * @return the signature of the attribute function.
     */
    public String getAttributeFunctionName() {
        return "set" +
                WordUtils.capitalize(name) +
                (option.hasArg() ? "(String " : "(boolean ") +
                name +
                ")";
    }
}
