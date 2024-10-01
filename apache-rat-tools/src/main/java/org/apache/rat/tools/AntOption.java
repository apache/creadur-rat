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
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.WordUtils;

/**
 * A class that wraps the CLI option and provides Ant specific values.
 */
public class AntOption extends AbstractOption{

    /**
     * Constructor.
     * @param option the option to wrap.
     */
    AntOption(final Option option) {
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

    protected String cleanupName(Option option) {
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
        StringBuilder sb = new StringBuilder()
                .append(format("    /**%n     * %s%n", StringEscapeUtils.escapeHtml4(getDescription())));
        if (option.isDeprecated()) {
            sb.append(format("     * %s%n     * @deprecated%n", option.getDeprecated()));
        }
        if (addParam && option.hasArg()) {
            sb.append(format("     * @param %s The value to set%n", name));
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
