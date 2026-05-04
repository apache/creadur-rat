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

import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.rat.ui.UIOption;
import org.apache.rat.ui.UIOptionCollection;
import org.apache.rat.utils.CasedString;

import static java.lang.String.format;

/**
 * A representation of a Maven option based on a CLI option.
 */
public final class MavenOption extends UIOption<MavenOption> {

    /**
     * Constructor.
     *
     * @param option The CLI option
     */
    MavenOption(final UIOptionCollection<MavenOption> collection, final Option option) {
        super(collection, option, MavenOptionCollection.createName(option));
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
        if (option == this.option) {
            return format("<%s>", this.name);
        }
        return format("<%s>", ((MavenOptionCollection) getOptionCollection()).createName(option));
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
        StringBuilder sb = new StringBuilder(String.format("<%s>", getName()));
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

    public String getMethodSignature(final String indent, final boolean multiple) {
        StringBuilder sb = new StringBuilder();
        if (isDeprecated()) {
            sb.append(format("%s@Deprecated%n", indent));
        }
        String fname = name.toCase(CasedString.StringCase.CAMEL);
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


    public String getPropertyAnnotation(final String fname) {
        StringBuilder sb = new StringBuilder("@Parameter");
        String property = option.hasArgs() ? null : format("property = \"rat.%s\"", fname);
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

}
