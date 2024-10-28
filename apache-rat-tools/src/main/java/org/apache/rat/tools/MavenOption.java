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

import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.rat.commandline.Arg;

import static java.lang.String.format;

/**
 * A representation of a CLI option as a Maven option
 */
public class MavenOption extends AbstractOption {
    /** Default values for cli options */
    private static final Map<Arg, String> DEFAULT_VALUES = new HashMap<>();

    static {
        DEFAULT_VALUES.put(Arg.OUTPUT_FILE, "${project.build.directory}/rat.txt");
    }

    /**
     * Constructor.
     *
     * @param option The CLI option
     */
    MavenOption(final Option option) {
        super(option, MavenGenerator.createName(option));
    }

    @Override
    protected String cleanupName(final Option option) {
        return format("<%s>", MavenGenerator.createName(option));
    }

    @Override
    public String getDefaultValue() {
        Arg arg = Arg.findArg(option);
        String result = DEFAULT_VALUES.get(arg);
        if (result == null) {
            result = arg.defaultValue();
        }
        return result;
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
}
