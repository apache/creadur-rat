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
package org.apache.rat.ant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.rat.ConfigurationException;
import org.apache.rat.OptionCollection;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.Reporter;
import org.apache.rat.commandline.Arg;
import org.apache.rat.commandline.ArgumentContext;
import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.config.exclusion.StandardCollection;
import org.apache.rat.document.DocumentName;
import org.apache.rat.ui.ArgumentTracker;
import org.apache.rat.ui.OptionFactory;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.Union;

public abstract class AbstractAntReport extends Task {
    /** The argument tracker for this report */
    protected final ArgumentTracker argumentTracker;
    /** will hold any nested resource collection */
    private Union nestedResources;

    protected AbstractAntReport() {
        super();
        List<AntOption> antList = OptionFactory.getOptions(AntOption.FACTORY_CONFIG).collect(Collectors.toList());
        argumentTracker = new ArgumentTracker(antList);
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
    final void add(final ResourceCollection rc) {
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
    final List<String> getValues(final Arg arg) {
        List<String> result = new ArrayList<>();
        for (Option option : arg.group().getOptions()) {
            if (option.getLongOpt() != null) {
                List<String> args = argumentTracker.getArg(ArgumentTracker.extractKey(option));
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
    final void removeKey(final Arg arg) {
        for (Option option : arg.group().getOptions()) {
            argumentTracker.removeArg(ArgumentTracker.extractKey(option));
        }
    }

    /**
     * Creates the ReportConfiguration from the Ant options.
     *
     * @return the ReportConfiguration.
     */
    public final ReportConfiguration getConfiguration() throws IOException, ParseException {
//        try{
        final ArgumentContext argumentContext = OptionCollection.parseCommands(new File("."), argumentTracker.args().toArray(new String[0]));
        if (getValues(Arg.OUTPUT_FILE).isEmpty()) {
            argumentContext.getConfiguration().setOut(() -> new LogOutputStream(this, Project.MSG_INFO));
        }
        DocumentName name = DocumentName.builder(getProject().getBaseDir()).build();

        argumentContext.getConfiguration().addSource(new ResourceCollectionContainer(name, argumentContext.getConfiguration(), nestedResources));
//            if (helpLicenses) {
//                new org.apache.rat.help.Licenses(configuration, new PrintWriter(DefaultLog.getInstance().asWriter())).printHelp();
//            }
        return argumentContext.getConfiguration();
//        } catch (IOException | ImplementationException e) {
//            throw new BuildException(e.getMessage(), e);
//        }
    }

    /**
     * Validates the task's configuration.
     */
    final ReportConfiguration validate(final ReportConfiguration cfg) {
        try {
            cfg.validate();
        } catch (ConfigurationException e) {
            throw new BuildException(e.getMessage(), e.getCause());
        }
        if (nestedResources == null) {
            throw new BuildException("You must specify at least one file to create the report for.");
        }
        return cfg;
    }

    /**
     * Generates the report.
     */
    @Override
    public final void execute() {
        try {
            Reporter reporter = new Reporter(validate(getConfiguration()));
            Reporter.Output output = reporter.execute();
            output.format(StyleSheets.PLAIN.getStyleSheet(), () -> CloseShieldOutputStream.wrap(System.out));
        } catch (BuildException e) {
            throw e;
        } catch (Exception ioex) {
            throw new BuildException(ioex);
        }
    }

    /* TYPE CLASSES */

    /**
     * Base class for all none text command line types.
     */
    protected static class TxtValue {
        /** The value for this object */
        private String value;
        protected TxtValue() { }

        @Override
        public String toString() {
            return value;
        }

        public void addText(final String text) {
            value = text.trim();
        }
    }

    /**
     * Handles parameters that are a {@link StandardCollection}.
     */
    public static class Std extends TxtValue {
        public Std() { }
    }

    /**
     * Handles parameters that are {@link OptionCollection.ArgumentType#EXPRESSION}.
     */
    public static class Expr extends TxtValue {
        public Expr() { }
    }

    /**
     * Handles parameters that are {@link OptionCollection.ArgumentType#COUNTERPATTERN}.
     */
    public static class Cntr extends TxtValue {
        public Cntr() { }
    }

    /**
     * Handles parameters that are string representations of {@link OptionCollection.ArgumentType#FILE}.
     */
    public static class Filename extends TxtValue {
        public Filename() { }
    }

    /**
     * Handles parameters that are {@link OptionCollection.ArgumentType#LICENSEID} or
     * {@link OptionCollection.ArgumentType#FAMILYID}.
     */
    public static class Lst extends TxtValue {
        public Lst() { }
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
        return switch (level) {
            case Project.MSG_DEBUG, Project.MSG_VERBOSE -> Log.Level.DEBUG;
            case Project.MSG_INFO -> Log.Level.INFO;
            case Project.MSG_WARN -> Log.Level.WARN;
            case Project.MSG_ERR -> Log.Level.ERROR;
            default -> Log.Level.OFF;
        };
    }

    /**
     * Converts RAT log level to Ant Project log level.
     * @param level the RAT log level to convert.
     * @return the equivalent Ant Project log level.
     */
    static int toProjectLevel(final Log.Level level) {
        return switch (level) {
            case DEBUG -> Project.MSG_DEBUG;
            case INFO -> Project.MSG_INFO;
            case WARN -> Project.MSG_WARN;
            case ERROR -> Project.MSG_ERR;
            default -> -1;
        };
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
            AbstractAntReport.this.log(msg, toProjectLevel(level));
        }
    }
}
