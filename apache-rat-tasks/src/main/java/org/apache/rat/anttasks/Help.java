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
package org.apache.rat.anttasks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.commandline.Arg;
import org.apache.rat.config.exclusion.StandardCollection;
import org.apache.rat.help.AbstractHelp;
import org.apache.rat.documentation.options.AbstractOption;
import org.apache.rat.documentation.options.AntOption;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;

import static java.lang.String.format;

/**
 * A basic Ant task that generates a report on all files specified by the nested
 * resource collection(s).
 *
 * <p>
 * IHeaderMatcher(s) can be specified as nested elements as well.
 * </p>
 *
 * <p>
 * The attribute <code>format</code> defines the output format and can take the
 * values
 * <ul>
 * <li>xml - Rat's native XML output.</li>
 * <li>styled - transforms the XML output using the given stylesheet. The
 * stylesheet attribute must be set as well if this attribute is used.</li>
 * <li>plain - plain text using Rat's built-in stylesheet. This is the
 * default.</li>
 * </ul>
 */
public class Help extends BaseAntTask {

    /**
     * Constructor.
     */
    public Help() {
        // replace the logger only if it has not already been set.
        Log oldLog = DefaultLog.getInstance();
        if (oldLog instanceof DefaultLog) {
            DefaultLog.setInstance(new Logger());
            DefaultLog.getInstance().setLevel(oldLog.getLevel());
        }
    }

    /**
     * Generates the help.
     */
    @Override
    public void execute() {
        org.apache.rat.help.Help helpObj = new org.apache.rat.help.Help(System.out) {
            /**
             * Print the usage to the specific PrintWriter.
             * @param opts The defined options.
             */
            public void printUsage(final Options opts) {
                String syntax = "ant {target executing task <rat:help/>}";
                AntHelpFormatter helpFormatter = new AntHelpFormatter();
                helpFormatter.setOptPrefix("<");

                helpFormatter.printHelp(writer, helpFormatter.getWidth(), syntax, AbstractHelp.header("Available options"), opts,
                        helpFormatter.getLeftPadding(), helpFormatter.getDescPadding(), header("Argument Types"));

                String argumentPadding = printArgumentTypes();

                writer.println(header("Standard Collections"));
                for (StandardCollection sc : StandardCollection.values()) {
                    writer.format("%n<%s>%n", sc.name());
                    helpFormatter.printWrapped(writer, helpFormatter.getWidth(), helpFormatter.getLeftPadding() + HELP_PADDING + HELP_PADDING,
                            argumentPadding + sc.desc());
                    helpFormatter.printWrapped(writer, helpFormatter.getWidth(), helpFormatter.getLeftPadding() + HELP_PADDING + HELP_PADDING,
                            argumentPadding + "File patterns: " + (sc.patterns().isEmpty() ? "<none>" : String.join(", ", sc.patterns())));
                    helpFormatter.printWrapped(writer, helpFormatter.getWidth(), helpFormatter.getLeftPadding() + HELP_PADDING + HELP_PADDING,
                            argumentPadding + "Provides a path matcher: " + sc.hasStaticDocumentNameMatcher());
                    helpFormatter.printWrapped(writer, helpFormatter.getWidth(), helpFormatter.getLeftPadding() + HELP_PADDING + HELP_PADDING,
                            argumentPadding + "Provides a file processor: " + sc.fileProcessorBuilder().hasNext());
                }
                writer.println("\nA path matcher will match specific information about the file.");
                writer.println("\nA file processor will process the associated \"ignore\" file for include and exclude directives");

                writer.println(header("Notes"));
                int idx = 1;
                for (String note : NOTES) {
                    writer.format("%d. %s%n", idx++, note);
                }
                writer.flush();
            }
        };

        helpObj.printUsage(Arg.getOptions());
    }

    @Override
    public void log(final String msg, final int msgLevel) {
        if (getProject() != null) {
            getProject().log(msg, msgLevel);
        } else {
            DefaultLog.createDefault().log(Report.fromProjectLevel(msgLevel), msg);
        }
    }

    @Override
    public void log(final String msg, final Throwable t, final int msgLevel) {
        if (getProject() == null) {
            log(Log.formatLogEntry(msg, t), msgLevel);
        } else {
            getProject().log(this, msg, t, msgLevel);
        }
    }

    /**
     * A facade for the Logger provided by Ant.
     */
    private final class Logger implements Log {
        @Override
        public Level getLevel() {
            return Level.DEBUG;
        }

        @Override
        public void log(final Level level, final String message, final Throwable throwable) {
            log(level, Log.formatLogEntry(message, throwable));
        }

        @Override
        public void log(final Level level, final String msg) {
            Help.this.log(msg, Report.toProjectLevel(level));
        }
    }

    public static class AntHelpFormatter extends HelpFormatter {
        public AntHelpFormatter() {
            super();
            setWidth(180);
        }
        @Override
        public Comparator<Option> getOptionComparator() {
            return Comparator.comparing(Option::getLongOpt);
        }

        @Override
        protected StringBuffer renderOptions(final StringBuffer sb, final int width, final Options options, final int leftPad, final int descPad) {
            final String lpad = createPadding(leftPad);
            final String dpad = createPadding(descPad);
            // first create list containing only <lpad>-a,--aaa where
            // -a is opt and --aaa is long opt; in parallel look for
            // the longest opt string this list will be then used to
            // sort options ascending
            String optionTitle = " -- Option --";
            String exampleTitle = " -- Example --";
            String descriptionTitle = " -- Description --";
            int max = optionTitle.length();
            int maxExample = exampleTitle.length();
            final List<AntOption> optList = options.getOptions().stream().filter(Option::hasLongOpt)
                    .map(AntOption::new).collect(Collectors.toList());
            if (getOptionComparator() != null) {
                optList.sort(Comparator.comparing(AbstractOption::getName));
            }
            List<String> exampleList = new ArrayList<>();
            for (final AntOption option : optList) {
                String argName = StringUtils.defaultIfEmpty(option.getArgName(), "value");
                String fmt = option.isAttribute() ? "<rat:report %s='%s'>" : "<%1$s>%2$s</%1$s>";
                String example = format(fmt, option.getName(), argName);
                exampleList.add(example);
                max = Math.max(option.cleanupName().length(), max);
                maxExample = Math.max(example.length(), maxExample);
            }

            sb.append(optionTitle).append(createPadding(max - optionTitle.length()))
                    .append(dpad)
                    .append(exampleTitle).append(createPadding(maxExample - exampleTitle.length()))
                    .append(dpad)
                    .append(descriptionTitle)
                    .append(getNewLine());

            int x = 0;
            for (final Iterator<AntOption> it = optList.iterator(); it.hasNext();) {
                final AntOption option = it.next();
                String name = option.cleanupName();
                String example = exampleList.get(x++);

                final StringBuilder optBuf = new StringBuilder(name);
                if (name.length() < max) {
                    optBuf.append(createPadding(max - name.length()));
                }
                optBuf.append(dpad).append(example);
                if (example.length() < maxExample) {
                    optBuf.append(createPadding(maxExample - example.length()));
                }
                optBuf.append(dpad);
                final int nextLineTabStop = max + maxExample + 2 * descPad;
                if (option.isDeprecated()) {
                    optBuf.append(option.getDeprecated());
                } else if (option.getDescription() != null) {
                    optBuf.append(option.getDescription());
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
