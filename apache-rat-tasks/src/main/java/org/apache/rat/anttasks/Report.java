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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.cli.Option;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.rat.ConfigurationException;
import org.apache.rat.ImplementationException;
import org.apache.rat.OptionCollection;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.Reporter;
import org.apache.rat.commandline.Arg;
import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.document.DocumentName;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Resource;
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
 * <li>xml - Rat's native XML output.</li>
 * <li>styled - transforms the XML output using the given stylesheet. The
 * stylesheet attribute must be set as well if this attribute is used.</li>
 * <li>plain - plain text using Rat's built-in stylesheet. This is the
 * default.</li>
 * </ul>
 */
public class Report extends BaseAntTask {
    /** The list of licenses */
    private final List<License> licenses = new ArrayList<>();
    /** The list of license families */
    private final List<Family> families = new ArrayList<>();
    /** the options that are deprecated.  TODO remove this. */
    private final DeprecatedConfig deprecatedConfig = new DeprecatedConfig();
    /**
     * will hold any nested resource collection
     */
    private Union nestedResources;

    /**
     * Collection of objects that support Ant specific deprecated options
     */
    private static class DeprecatedConfig {
        /** The input file filter */
        private IOFileFilter inputFileFilter;
        /** the set of approved licence categories */
        private final Set<String> approvedLicenseCategories = new HashSet<>();
        /** the set of removed (unapproved) license categories */
        private final Set<String> removedLicenseCategories = new HashSet<>();
    }

    /**
     * Constructor.
     */
    public Report() {
        // replace the logger only if it has not already been set.
        if (DefaultLog.getInstance() instanceof DefaultLog) {
            DefaultLog.setInstance(new Logger());
        }
    }

    /**
     * Adds resources that will be checked.
     * @param rc resource to check.
     */
    public void add(final ResourceCollection rc) {
        if (nestedResources == null) {
            nestedResources = new Union();
        }
        nestedResources.add(rc);
    }

    /**
     * Adds an input file filter.
     * @param inputFileFilter The input file filter to add.
     */
    @Deprecated
    public void setInputFileFilter(final IOFileFilter inputFileFilter) {
        deprecatedConfig.inputFileFilter = inputFileFilter;
    }

    /**
     * Sets the report file.
     * @param reportFile the report file.
     * @deprecated use {@link #setOut(String)}
     */
    @Deprecated
    public void setReportFile(final File reportFile) {
        setOut(reportFile.getAbsolutePath());
    }

    /**
     * Adds an inline License definition to the system.
     * @param license the license to add.
     */
    public void addLicense(final License license) {
        licenses.add(license);
    }

    /**
     * Add an inline license family definition to the system.
     * @param family the license family to add.
     */
    public void addFamily(final Family family) {
        families.add(family);
    }

    /**
     * Adds a style sheet to the system.
     * @param styleSheet
     * @deprecated use {@link #setStylesheet(String)}
     */
    @Deprecated
    public void addStylesheet(final Resource styleSheet) {
        setStylesheet(styleSheet.getName());
    }

    /**
     * Adds a given style sheet to the report.
     * @param styleSheet style sheet to use in this report.
     * @deprecated use {@link #setStylesheet(String)}
     */
    @Deprecated
    public void addStyleSheet(final Resource styleSheet) {
        setStylesheet(styleSheet.getName());
    }

    /**
     * Sets a stylesheet for the report.
     * @param styleReport
     * @deprecated use {@link #setXml(boolean)}.  Note reversal of boolean value
     */
    @Deprecated
    public void setStyleReport(final boolean styleReport) {
        setXml(!styleReport);
    }

    /**
     * Determines if the output should be styled.
     * @param style
     * @deprecated use {@link #setStylesheet(String)} or {@link #setXml(boolean)}
     */
    @Deprecated
    public void setFormat(final String style) {
        setStyleReport("styled".equalsIgnoreCase(style));
    }

    /**
     * Adds as a file containing the definitions of licenses to the system.
     * @param fileName the file to add.
     * @deprecated use licenses child element.
     */
    public void setLicenses(final File fileName) {
        try {
            createLicenses().addText(fileName.getCanonicalPath());
        } catch (IOException e) {
            throw new BuildException("Unable to read license file " + fileName, e);
        }
    }

    /**
     * Specifies whether to add the default list of license matchers.
     * @param useDefaultLicenses if {@code true} use the default licenses.
     * @deprecated  use noDefaultLicenses attribute
     */
    @Deprecated
    public void setUseDefaultLicenses(final boolean useDefaultLicenses) {
        setNoDefaultLicenses(!useDefaultLicenses);
    }

    /**
     * Adds a family category to the list of approved licenses.
     * @param familyCategory the category to add.
     * @deprecated use addApprovedLicense child element.
     */
    @Deprecated
    public void setAddApprovedLicense(final String familyCategory) {
        deprecatedConfig.approvedLicenseCategories.add(familyCategory);
    }

    /**
     * Adds a family category to the list of approved licenses.
     * @param familyCategory the category to add
     */
    public void addAddApprovedLicense(final String familyCategory) {
        deprecatedConfig.approvedLicenseCategories.add(familyCategory);
    }

    /**
     * Removes a family category to the list of approved licenses.
     * @param familyCategory the category to add.
     * @deprecated use removeApprovedLicense child element
     */
    @Deprecated
    public void setRemoveApprovedLicense(final String familyCategory) {
        deprecatedConfig.removedLicenseCategories.add(familyCategory);
    }

    /**
     * Removes a family category to the list of approved licenses.
     * @param familyCategory the category to add.
     */
    public void addRemoveApprovedLicense(final String familyCategory) {
        deprecatedConfig.removedLicenseCategories.add(familyCategory);
    }

    /**
     * Removes a family category to the list of approved licenses.
     * @param familyCategory the category to add.
     * @deprecated use removeApprovedLicense element
     */
    @Deprecated
    public void setRemoveApprovedLicense(final String[] familyCategory) {
        deprecatedConfig.removedLicenseCategories.addAll(Arrays.asList(familyCategory));
    }

    /**
     * Removes a family category to the list of approved licenses.
     * @param familyCategory the category to add.
     */
    public void addRemoveApprovedLicense(final String[] familyCategory) {
        deprecatedConfig.removedLicenseCategories.addAll(Arrays.asList(familyCategory));
    }
    /**
     * Sets the copyright message
     * @param copyrightMessage the copyright message
     * @deprecated use copyright attribute
     */
    @Deprecated
    public void setCopyrightMessage(final String copyrightMessage) {
       setCopyright(copyrightMessage);
    }

    /**
     * Determines if license headers should be added.
     * @param setting the setting.
     * @deprecated use addLicense and force attributes
     */
    @Deprecated
    public void setAddLicenseHeaders(final AddLicenseHeaders setting) {
        switch (setting.getNative()) {
            case TRUE:
                setAddLicense(true);
                break;
            case FALSE:
                setAddLicense(false);
                break;
            case FORCED:
                setAddLicense(true);
                setForce(true);
                break;
        }
    }

    /**
     * Adds definition information
     * @param fileName the file to add
     * @deprecated Use {@link #addLicense}
     */
    @Deprecated
    public void setAddDefaultDefinitions(final File fileName) {
        try {
            Licenses lic = createLicenses();
            lic.addText(fileName.getCanonicalPath());
        } catch (IOException e) {
            throw new BuildException("Unable to read license file " + fileName, e);
        }
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
     * Creates the ReportConfiguration from the ant options.
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
            configuration.addApprovedLicenseCategories(deprecatedConfig.approvedLicenseCategories);
            configuration.removeApprovedLicenseCategories(deprecatedConfig.removedLicenseCategories);
            if (deprecatedConfig.inputFileFilter != null) {
                configuration.addExcludedFilter(deprecatedConfig.inputFileFilter);
            }
            families.stream().map(Family::build).forEach(configuration::addFamily);
            licenses.stream().map(License::asBuilder)
                    .forEach(l -> configuration.addApprovedLicenseCategory(configuration.addLicense(l).getLicenseFamily()));
            if (helpLicenses) {
                new org.apache.rat.help.Licenses(configuration, new PrintWriter(DefaultLog.getInstance().asWriter())).printHelp();
            }
            return configuration;
        } catch (IOException | ImplementationException e) {
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

    /**
     * Type for the addLicenseHeaders attribute.
     * @deprecated No longer required, use stylesheet or xml attributes.
     */
    @Deprecated
    public static class AddLicenseHeaders extends EnumeratedAttribute {
        /** add license headers and create *.new file */
        static final String TRUE = "true";
        /** do not add license headers */
        static final String FALSE = "false";
        /** add license headers and overwrite existing files */
        static final String FORCED = "forced";

        public AddLicenseHeaders() {
        }

        public AddLicenseHeaders(final String s) {
            setValue(s);
        }

        @Override
        public String[] getValues() {
            return new String[] { TRUE, FALSE, FORCED };
        }

        public org.apache.rat.config.AddLicenseHeaders getNative() {
            return org.apache.rat.config.AddLicenseHeaders.valueOf(getValue().toUpperCase());
        }
    }

    /**
     * Type for the addLicenseHeaders attribute.
     * @deprecated use listLicenses or listFamilies attributes.
     */
    @Deprecated
    public static class ApprovalFilter extends EnumeratedAttribute {

        public ApprovalFilter() {
        }

        public ApprovalFilter(final String s) {
            setValue(s);
        }

        @Override
        public String[] getValues() {
            return Arrays.stream(LicenseSetFactory.LicenseFilter.values()).map(LicenseSetFactory.LicenseFilter::name)
                    .collect(Collectors.toList()).toArray(new String[LicenseSetFactory.LicenseFilter.values().length]);
        }

        public LicenseSetFactory.LicenseFilter internalFilter() {
            return LicenseSetFactory.LicenseFilter.valueOf(getValue());
        }
    }

    /**
     * A facade for the Logger provided by Ant.
     */
    private class Logger implements Log {
        /** the actual logger */
        private final org.apache.tools.ant.DefaultLogger delegate = new org.apache.tools.ant.DefaultLogger();

        @Override
        public Level getLevel() {
            switch (delegate.getMessageOutputLevel()) {
                case Project.MSG_ERR:
                    return Level.ERROR;
                case Project.MSG_WARN:
                    return Level.WARN;
                case Project.MSG_INFO:
                    return Level.INFO;
                case Project.MSG_VERBOSE:
                case Project.MSG_DEBUG:
                    return Level.DEBUG;
                default:
                    return Level.OFF;
            }
        }

        @Override
        public void log(final Level level, final String msg) {
            log(level, msg, null);
        }

        @Override
        public void log(final Level level, final String message, final Throwable throwable) {
            BuildEvent event = new BuildEvent(Report.this);
            switch (level) {
                case DEBUG:
                    event.setMessage(message, Project.MSG_DEBUG);
                    break;
                case INFO:
                    event.setMessage(message, Project.MSG_INFO);
                    break;
                case WARN:
                    event.setMessage(message, Project.MSG_WARN);
                    break;
                case ERROR:
                    event.setMessage(message, Project.MSG_ERR);
                    break;
                case OFF:
                default:
                    return;
            }
            if (throwable != null) {
                event.setException(throwable);
            }
            delegate.messageLogged(event);
        }
    }
}
