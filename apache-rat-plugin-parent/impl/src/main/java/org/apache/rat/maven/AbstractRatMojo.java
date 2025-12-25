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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.rat.OptionCollection;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.commandline.Arg;
import org.apache.rat.commandline.ArgumentContext;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.FileDocument;
import org.apache.rat.license.ILicense;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;
import org.apache.rat.walker.DirectoryWalker;

import static java.lang.String.format;

/**
 * Abstract base class for Mojos, which are running Rat.
 */
public abstract class AbstractRatMojo extends AbstractMaven {
    /** Report configuration for report */
    private ReportConfiguration reportConfiguration;
    /**
     * The base directory, in which to search for files.
     */
    @Parameter(property = "rat.basedir", defaultValue = "${basedir}", required = true)
    protected File basedir;

    /**
     * Specifies the verbose output.
     * @since 0.8
     */
    @Parameter(property = "rat.verbose", defaultValue = "false")
    protected boolean verbose;

    /** The xml output file. */
    @Parameter(defaultValue = "${project.build.directory}/.rat.xml", readonly = true)
    protected File xmlOutputFile;

    /**
     * Specifies the include files character set.
     * If ${project.build.sourceEncoding} is not set defaults to UTF-8.
     */
    @Parameter(property = "rat.includesFileCharset", defaultValue = "${project.build.sourceEncoding}")
    private String includesFileCharset;

    /**
     * Specifies the include files character set.
     * If ${project.build.sourceEncoding} is not set defaults to UTF-8.
     */
    @Parameter(property = "rat.excludesFileCharset", defaultValue = "${project.build.sourceEncoding}")
    private String excludesFileCharset;

    /**
     * Whether to exclude subprojects. This is recommended, if you want a separate
     * apache-rat-plugin report for each subproject.
     */
    @Parameter(property = "rat.excludeSubprojects", defaultValue = "true")
    private boolean excludeSubProjects;

    /**
     * Will skip the plugin execution, e.g. for technical builds that do not take
     * license compliance into account.
     *
     * @since 0.11
     */
    @Parameter(property = "rat.skip", defaultValue = "false")
    protected boolean skip;

    /**
     * Holds the maven-internal project to allow resolution of artifact properties
     * during mojo runs.
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;

    protected AbstractRatMojo() {
        DefaultLog.setInstance(makeLog());
    }

    /**
     * @return the Maven project.
     */
    protected MavenProject getProject() {
        return project;
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
                List<String> args = argumentTracker.getArg(option.getLongOpt());
                if (args != null) {
                    result.addAll(args);
                }
            }
        }
        return result;
    }

    /**
     * Removes all values for an Arg.
     * @param arg The arg to remove values for.
     */
    protected void removeKey(final Arg arg) {
        for (Option option : arg.group().getOptions()) {
            if (option.getLongOpt() != null) {
                argumentTracker.removeArg(option.getLongOpt());
            }
        }
    }

    private org.apache.rat.utils.Log makeLog() {
        return new org.apache.rat.utils.Log() {
            @Override
            public Level getLevel() {
                final org.apache.maven.plugin.logging.Log log = getLog();
                if (log.isDebugEnabled()) {
                    return Level.DEBUG;
                }
                if (log.isInfoEnabled()) {
                    return Level.INFO;
                }
                if (log.isWarnEnabled()) {
                    return Level.WARN;
                }
                if (log.isErrorEnabled()) {
                    return Level.ERROR;
                }
                return Level.OFF;
            }

            @Override
            public void log(final Level level, final String message, final Throwable throwable) {
                final org.apache.maven.plugin.logging.Log log = getLog();
                switch (level) {
                    case DEBUG:
                        if (throwable != null) {
                            log.debug(message, throwable);
                        } else {
                            log.debug(message);
                        }
                        break;
                    case INFO:
                        if (throwable != null) {
                            log.info(message, throwable);
                        } else {
                            log.info(message);
                        }
                        break;
                    case WARN:
                        if (throwable != null) {
                            log.warn(message, throwable);
                        } else {
                            log.warn(message);
                        }
                        break;
                    case ERROR:
                        if (throwable != null) {
                            log.error(message, throwable);
                        } else {
                            log.error(message);
                        }
                        break;
                    case OFF:
                        break;
                }
            }

            @Override
            public void log(final Level level, final String msg) {
                final org.apache.maven.plugin.logging.Log log = getLog();
                switch (level) {
                    case DEBUG:
                        log.debug(msg);
                        break;
                    case INFO:
                        log.info(msg);
                        break;
                    case WARN:
                        log.warn(msg);
                        break;
                    case ERROR:
                        log.error(msg);
                        break;
                    case OFF:
                        break;
                }
            }
        };
    }

    /**
     * Processes the excludeSubProjects option and adds appropriate modules to the include/excludes
     */
    private void setIncludeExclude() {
        if (excludeSubProjects && project != null && project.getModules() != null) {
            List<String> subModules = new ArrayList<>();
            project.getModules().forEach(s -> subModules.add(format("%s/**", s)));
            setInputExcludes(subModules.toArray(new String[0]));
        }
    }

    protected ReportConfiguration getConfiguration() throws MojoExecutionException {
        Log log = DefaultLog.getInstance();
        if (reportConfiguration == null) {
            try {
                if (getLog().isDebugEnabled()) {
                    log.debug("Start BaseRatMojo Configuration options");
                    for (Map.Entry<String, List<String>> entry : argumentTracker.entrySet()) {
                        log.debug(format(" * %s %s", entry.getKey(), String.join(", ", entry.getValue())));
                    }
                    log.debug("End BaseRatMojo Configuration options");
                }

                boolean helpLicenses = !getValues(Arg.HELP_LICENSES).isEmpty();
                removeKey(Arg.HELP_LICENSES);
                setIncludeExclude();

                ArgumentContext ctxt = OptionCollection.parseCommands(basedir, argumentTracker.args().toArray(new String[0]));
                DocumentName dirName = DocumentName.builder(basedir).build();
                ctxt.getConfiguration().addSource(new DirectoryWalker(new FileDocument(dirName, basedir,
                        ctxt.getConfiguration().getDocumentExcluder(dirName))));

                if (helpLicenses) {
                    new org.apache.rat.help.Licenses(ctxt.getConfiguration(), new PrintWriter(log.asWriter())).printHelp();
                }
                reportConfiguration = ctxt.getConfiguration();
            } catch (IOException | ParseException e) {
                throw new MojoExecutionException(e);
            }
        }
        return reportConfiguration;
    }

    protected void logLicenses(final Collection<ILicense> licenses) {
        if (getLog().isDebugEnabled()) {
            getLog().debug("The following " + licenses.size() + " licenses are activated:");
            for (ILicense license : licenses) {
                getLog().debug("* " + license.toString());
            }
        }
    }
}
