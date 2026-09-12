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

import java.util.function.Function;

import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.rat.ConfigurationException;
import org.apache.rat.ui.UIOption;
import org.apache.rat.utils.CasedString;

import static java.lang.String.format;

/**
 * A representation of a Maven option based on an Option.
 */
public final class MavenOption extends UIOption<MavenOption> {

    /** The format to start an XML entity */
    private static final String XML_FMT = "<%s>";

    /**
     * Constructor.
     *
     * @param builder The MavenOption builder.
     */
    private MavenOption(final MavenOptionBuilder builder) {
        super(builder);
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Gets the method name for this option.
     * @return the method name for this option.
     */
    public String getMethodName() {
        return "set" + name.toCase(CasedString.StringCase.PASCAL);
    }

    @Override
    protected String cleanupName(final Option option) {
        // only parse the option if we need to.
        return option == this.option ? format(XML_FMT, this.name) : format(XML_FMT,
                MavenOptionBuilder.createName(optionCollection.rename(option)));
    }

    @Override
    public String getText() {
        return cleanupName(option);
    }

    @Override
    public String getExample() {
        if (hasArgs()) {
            return getExample(getArgName() + "1", getArgName() + "2");
        }
        if (hasArg()) {
            return getExample(getArgName());
        }
            return getExample("");
    }

    /**
     * Create example text for the option.
     * @param args the example arguments for the option.
     * @return a formatted option.
     */
    public String getExample(final String... args) {
        StringBuilder sb = new StringBuilder(String.format(XML_FMT, getName()));
        if (hasArg()) {
            if (hasArgs()) {
                sb.append(System.lineSeparator());
                for (String arg : args) {
                    sb.append(String.format("  <%1$s>%2$s</%1$s>%n", WordUtils.uncapitalize(getArgName()), arg));
                }
            } else {
                sb.append(args[0]);
            }
        } else {
            sb.append(Boolean.TRUE);
        }
        sb.append("</").append(getName()).append(">");
        return sb.toString();
    }

    /**
     * Gets the Maven Mojo method signature for this Option.
     * @param indent the number of spaces to indent the text.
     * @param multiple {@code true} if this option takes multiple arguments.
     * @return the method signature for this Option.
     */
    public String getMethodSignature(final String indent, final boolean multiple) {
        StringBuilder sb = new StringBuilder();
        if (isDeprecated()) {
            sb.append(format("%s@Deprecated%n", indent));
        }
        // the camel case name for this option.
        String camelName = name.toCase(CasedString.StringCase.CAMEL);
        if (camelName == null) {
            throw new ConfigurationException("Name can not be null");
        }
        String args = option.hasArg() ? "String" : "boolean";
        if (multiple) {
            if (!(camelName.endsWith("s") || camelName.endsWith("Approved") || camelName.endsWith("Denied"))) {
                camelName = camelName + "s";
            }
            args = args + "[]";
        }

        return sb.append(format("%1$s%5$s%n%1$spublic void set%3$s(%4$s %2$s)",
                        indent, name, camelName, args, getParameterAnnotation(camelName)))
                .toString();
    }

    /**
     * Creates the {@code @Parameter} annotation for this option.
     * @param camelName The camel cased name for this option.
     * @return the string that is the parameter annotation.
     */
    public String getParameterAnnotation(final String camelName) {
        StringBuilder sb = new StringBuilder("@Parameter");
        String property = option.hasArgs() ? null : format("property = \"rat.%s\"", camelName);
        String defaultValue = option.isDeprecated() ? null : getDefaultValue();
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

    /**
     * The builder for the MavenOptions.
     */
    public static class MavenOptionBuilder extends UIOption.Builder<MavenOption, MavenOptionBuilder> {

        @Override
        protected Function<Option, CasedString> getNameFactory() {
            return o -> createName(optionCollection().rename(o));
        }

        @Override
        protected MavenOption doBuild() {
            return new MavenOption(this);
        }

        /**
         * Create the cased name
         * @param key the renamed key from the collection.
         * @return the name for the key.
         * @see MavenOptionCollection#rename(Option)
         */
        static CasedString createName(final String key) {
            return new CasedString(CasedString.StringCase.KEBAB, key).as(CasedString.StringCase.PASCAL);
        }
    }
}
