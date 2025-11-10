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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.rat.ConfigurationException;
import org.apache.rat.ImplementationException;
import org.apache.rat.OptionCollection;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.Reporter;
import org.apache.rat.commandline.Arg;
import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.document.DocumentName;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.Union;

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
 * <li>xml - RAT's native XML output.</li>
 * <li>styled - transforms the XML output using the given stylesheet. The
 * stylesheet attribute must be set as well if this attribute is used.</li>
 * <li>plain - plain text using RAT's built-in stylesheet. This is the
 * default.</li>
 * </ul>
 */
public class Report extends BaseAntTask {

    /**
     * will hold any nested resource collection
     */
    private Union nestedResources;

    /**
     * Constructor.
     */
    public Report() {
        super();
        // replace the logger only if it has not already been set.
        Log oldLog = DefaultLog.getInstance();
        if (oldLog instanceof DefaultLog) {
            DefaultLog.setInstance(new Logger());
            DefaultLog.getInstance().setLevel(oldLog.getLevel());
        }
    }

    /**
     * Adds resources that will be checked.
     *
     * @param rc resource to check.
     */
    public void add(final ResourceCollection rc) {
        if (nestedResources == null) {
            nestedResources = new Union();
        }
        nestedResources.add(rc);
    }

    /**
     * Reads values for the Arg.
     *
     * @param arg The Arg to get the values for.
     * @return The list of values or an empty list.
     */
    protected List<String> getValues(final Arg arg) {
        List<String> result = new ArrayList<>();
        for (Option option : arg.group().getOptions()) {
            if (option.getLongOpt() != null) {
                List<String> args = getArg(option.getLongOpt());
                if (args != null) {
                    result.addAll(args);
                }
            }
        }
        return result;
    }

    /**
     * Removes the values for the arg.
     *
     * @param arg the arg to remove the values for.
     */
    protected void removeKey(final Arg arg) {
        for (Option option : arg.group().getOptions()) {
            if (option.getLongOpt() != null) {
                removeArg(option.getLongOpt());
            }
        }
    }

    /**
     * Creates the ReportConfiguration from the Ant options.
     *
     * @return the ReportConfiguration.
     */
    public ReportConfiguration getConfiguration() {
        try {
            boolean helpLicenses = !getValues(Arg.HELP_LICENSES).isEmpty();
            removeKey(Arg.HELP_LICENSES);

            final ReportConfiguration configuration = OptionCollection.parseCommands(new File("."), args().toArray(new String[0]),
                    o -> DefaultLog.getInstance().warn("Help option not supported"),
                    true);
            if (getValues(Arg.OUTPUT_FILE).isEmpty()) {
                configuration.setOut(() -> new LogOutputStream(this, Project.MSG_INFO));
            }
            DocumentName name = DocumentName.builder(getProject().getBaseDir()).build();
            configuration.addSource(new ResourceCollectionContainer(name, configuration, nestedResources));
            if (helpLicenses) {
                new org.apache.rat.help.Licenses(configuration, new PrintWriter(DefaultLog.getInstance().asWriter())).printHelp();
            }
            return configuration;
        } catch (IOException | ParseException | ImplementationException e) {
            throw new BuildException(e.getMessage(), e);
        }
    }

    /**
     * Generates the report.
     */
    @Override
    public void execute() {
        try {
            Reporter r = new Reporter(validate(getConfiguration()));
            r.output(StyleSheets.PLAIN.getStyleSheet(), () -> new ReportConfiguration.NoCloseOutputStream(System.out));
            r.output();
        } catch (BuildException e) {
            throw e;
        } catch (Exception ioex) {
            throw new BuildException(ioex);
        }
    }

    /**
     * validates the task's configuration.
     */
    protected ReportConfiguration validate(final ReportConfiguration cfg) {
        try {
            cfg.validate(s -> log(s, Project.MSG_WARN));
        } catch (ConfigurationException e) {
            throw new BuildException(e.getMessage(), e.getCause());
        }
        if (nestedResources == null) {
            throw new BuildException("You must specify at least one file to create the report for.");
        }
        return cfg;
    }

    @Override
    public void log(final String msg, final int msgLevel) {
        if (getProject() != null) {
            getProject().log(msg, msgLevel);
        } else {
            DefaultLog.createDefault().log(fromProjectLevel(msgLevel), msg);
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
     * Converts to RAT log level from Ant Project log level.
     * @param level the Ant Project log level to convert.
     * @return the equivalent RAT log level.
     */
    public static Log.Level fromProjectLevel(final int level) {
        switch (level) {
            case Project.MSG_DEBUG:
            case Project.MSG_VERBOSE:
                return Log.Level.DEBUG;
            case Project.MSG_INFO:
                return Log.Level.INFO;
            case Project.MSG_WARN:
                return Log.Level.WARN;
            case Project.MSG_ERR:
                return Log.Level.ERROR;
            default:
                return Log.Level.OFF;
        }
    }

    /**
     * Converts RAT log level to Ant Project log level.
     * @param level the RAT log level to convert.
     * @return the equivalent Ant Project log level.
     */
    static int toProjectLevel(final Log.Level level) {
        switch (level) {
            case DEBUG:
                return Project.MSG_DEBUG;
            case INFO:
                return Project.MSG_INFO;
            case WARN:
                return Project.MSG_WARN;
            case ERROR:
                return Project.MSG_ERR;
            case OFF:
            default:
                return -1;
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
        public void log(final Log.Level level, final String message, final Throwable throwable) {
            log(level, Log.formatLogEntry(message, throwable));
        }

        @Override
        public void log(final Level level, final String msg) {
            Report.this.log(msg, toProjectLevel(level));
        }
    }
}
