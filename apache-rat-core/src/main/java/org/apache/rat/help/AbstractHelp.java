/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */
package org.apache.rat.help;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.text.WordUtils;
import org.apache.rat.OptionCollection;
import org.apache.rat.VersionInfo;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.String.format;

public abstract class AbstractHelp {

    /** The width of the help report in chars. */
    public static final int HELP_WIDTH = 120;
    /** The number of chars to indent output with */
    public static final int HELP_PADDING = 4;

    protected final HelpFormatter helpFormatter;
    protected final VersionInfo versionInfo;

    protected AbstractHelp() {
        helpFormatter = new HelpFormatter.Builder().setShowDeprecated(DEPRECATED_MSG).get();
        helpFormatter.setWidth(HELP_WIDTH);
        helpFormatter.setOptionComparator(new OptionCollection.OptionComparator());
        versionInfo = new VersionInfo();
    }

    /** Function to format deprecated display */
    public static final Function<Option, String> DEPRECATED_MSG = o -> {
        StringBuilder sb = new StringBuilder("[").append(o.getDeprecated().toString()).append("]");
        if (o.getDescription() != null) {
            sb.append(" ").append(o.getDescription());
        }
        return sb.toString();
    };

    /**
     * Create a padding.
     * @param len The length of the padding in characters.
     * @return a string with len blanks.
     */
    public static String createPadding(final int len) {
        char[] padding = new char[len];
        Arrays.fill(padding, ' ');
        return new String(padding);
    }

    /**
     * Create a section header for the output.
     * @param txt the text to put in the header.
     * @return the Header string.
     */
    public static String header(final String txt) {
        return String.format("%n====== %s ======%n", WordUtils.capitalizeFully(txt));
    }

}
