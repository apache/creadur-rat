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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.rat.OptionCollection;
import org.apache.rat.VersionInfo;
import org.apache.rat.commandline.Arg;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.String.format;

public abstract class AbstractHelp {
    private static final String END_OF_OPTION_MSG = " Multiple values may be specified.  Note that '--' or a following option is required when using this parameter.";

    /** The width of the help report in chars. */
    public static final int HELP_WIDTH = 120;
    /** The number of chars to indent output with */
    public static final int HELP_PADDING = 4;

    protected final RatHelpFormatter helpFormatter;
    protected final VersionInfo versionInfo;

    protected AbstractHelp() {
        helpFormatter = new RatHelpFormatter();
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

    public class RatHelpFormatter extends HelpFormatter {

        RatHelpFormatter() {
            super();
            this.optionComparator = new OptionCollection.OptionComparator();
            this.setWidth(HELP_WIDTH);
        }

        public void printHelp(final PrintWriter pw, final String cmdLineSyntax, final String header, final Options options, final String footer) {
            if (StringUtils.isEmpty(cmdLineSyntax)) {
                throw new IllegalArgumentException("cmdLineSyntax not provided");
            }

            helpFormatter.printUsage(pw, HELP_WIDTH, cmdLineSyntax);

            if (header != null && !header.isEmpty()) {
                helpFormatter.printWrapped(pw, HELP_WIDTH, header);
            }
            printOptions(pw, HELP_WIDTH, options, helpFormatter.getLeftPadding(), helpFormatter.getDescPadding());
            if (footer != null && !footer.isEmpty()) {
                helpFormatter.printWrapped(pw, helpFormatter.getWidth(), footer);
            }
        }


        protected StringBuffer renderOptions(final StringBuffer sb, final int width, final Options options, final int leftPad, final int descPad) {
            final String lpad = createPadding(leftPad);
            final String dpad = createPadding(descPad);
            // first create list containing only <lpad>-a,--aaa where
            // -a is opt and --aaa is long opt; in parallel look for
            // the longest opt string this list will be then used to
            // sort options ascending
            int max = 0;
            final List<StringBuffer> prefixList = new ArrayList<>();
            final List<Option> optList = new ArrayList<>(options.getOptions());
            optList.sort(helpFormatter.getOptionComparator());

            for (final Option option : optList) {
                final StringBuffer optBuf = new StringBuffer();
                if (option.getOpt() == null) {
                    optBuf.append(lpad).append("   ").append(getLongOptPrefix()).append(option.getLongOpt());
                } else {
                    optBuf.append(lpad).append(getOptPrefix()).append(option.getOpt());
                    if (option.hasLongOpt()) {
                        optBuf.append(',').append(getLongOptPrefix()).append(option.getLongOpt());
                    }
                }
                if (option.hasArg()) {
                    final String argName = option.getArgName();
                    if (argName != null && argName.isEmpty()) {
                        // if the option has a blank argname
                        optBuf.append(' ');
                    } else {
                        optBuf.append(option.hasLongOpt() ? helpFormatter.getLongOptSeparator() : " ");
                        optBuf.append("<").append(argName != null ? option.getArgName() : getArgName()).append(">");
                    }
                }
                prefixList.add(optBuf);
                max = Math.max(optBuf.length(), max);
            }
            int x = 0;
            for (final Iterator<Option> it = optList.iterator(); it.hasNext(); ) {
                final Option option = it.next();
                final StringBuilder optBuf = new StringBuilder(prefixList.get(x++).toString());
                if (optBuf.length() < max) {
                    optBuf.append(createPadding(max - optBuf.length()));
                }
                optBuf.append(dpad);
                final int nextLineTabStop = max + descPad;
                // check for deprecation
                if (option.isDeprecated()) {
                    optBuf.append(DEPRECATED_MSG.apply(option).trim());
                } else if (option.getDescription() != null) {
                    optBuf.append(option.getDescription());
                }
                // check for multiple values
                if (option.hasArgs()) {
                    optBuf.append(END_OF_OPTION_MSG);
                }
                // check for default value
                Arg arg = Arg.findArg(option);
                String defaultValue = arg == null ? null : arg.defaultValue();
                if (defaultValue != null) {
                    optBuf.append(format(" (Default value = %s)", defaultValue));
                }
                renderWrappedText(sb, width, nextLineTabStop, optBuf.toString());
                if (it.hasNext()) {
                    sb.append(getNewLine());
                }
            }
            return sb;
        }
    }
}
