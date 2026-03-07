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
package org.apache.rat.maven;

import org.apache.commons.cli.Option;
import org.apache.commons.text.WordUtils;
import org.apache.rat.ui.AbstractOption;
import org.apache.rat.utils.CasedString;

import static java.lang.String.format;

/**
 * A representation of a CLI option as a Maven option
 */
public final class MavenOption extends AbstractOption<MavenOption> {
    /** The cased string version of the name */
    private final CasedString casedName;

    /**
     * Constructor.
     *
     * @param option The CLI option
     */
    MavenOption(final MavenOptionCollection collection, final Option option, final CasedString casedName) {
        super(collection, option, casedName.toCase(CasedString.StringCase.CAMEL));
        this.casedName = casedName;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getMethodName() {
        return "set" + casedName.toCase(CasedString.StringCase.PASCAL);
    }

    @Override
    protected String cleanupName(final Option option) {
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
            return getExample(new String[]{getArgName() + "1", getArgName() + "2"});
        }
        if (hasArg()) {
            return getExample(getArgName());
        }
            return getExample("");
    }

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
}
