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
import org.apache.rat.DeprecationReporter;
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
 * <li>xml - RAT's native XML output.</li>
 * <li>styled - transforms the XML output using the given stylesheet. The
 * stylesheet attribute must be set as well if this attribute is used.</li>
 * <li>plain - plain text using RAT's built-in stylesheet. This is the
 * default.</li>
 * </ul>
 */
public class Report extends BaseAntTask {
    /**
     * The list of licenses
     */
    @Deprecated
    private final List<License> licenses = new ArrayList<>();
    /**
     * The list of license families
     */
    @Deprecated
    private final List<Family> families = new ArrayList<>();
    /**
     * the options that are deprecated.  TODO remove this.
     */
    private final DeprecatedConfig deprecatedConfig = new DeprecatedConfig();
    /**
     * will hold any nested resource collection
     */
    private Union nestedResources;

    /**
     * Collection of objects that support Ant specific deprecated options
     */
    private static final class DeprecatedConfig {
        /**
         * The input file filter
         */
        private IOFileFilter inputFileFilter;
        /**
         * the set of approved licence categories
         */
        private final Set<String> approvedLicenseCategories = new HashSet<>();
        /**
         * the set of removed (unapproved) license categories
         */
        private final Set<String> removedLicenseCategories = new HashSet<>();
    }

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
     * Adds an input file filter.
     *
     * @param inputFileFilter The input file filter to add.
     */
    @Deprecated
    public void setInputFileFilter(final IOFileFilter inputFileFilter) {
        DeprecationReporter.logDeprecated("element inputFileFilter", "0.17", true, "outputFile element");
        deprecatedConfig.inputFileFilter = inputFileFilter;
    }

    /**
     * Sets the report file.
     *
     * @param reportFile the report file.
     * @deprecated use outputFile element
     */
    @Deprecated
    public void setReportFile(final File reportFile) {
        DeprecationReporter.logDeprecated("element reportFile", "0.17", true, "outputFile element");
        addArg("output-file", reportFile.getAbsolutePath());
    }

    /**
     * Adds an inline License definition to the system.
     *
     * @param license the license to add.
     * @deprecated Create a custom configuration file and use the config option.
     */
    @Deprecated
    public void addLicense(final License license) {
        licenses.add(license);
    }

    /**
     * Add an inline license family definition to the system.
     *
     * @param family the license family to add.
     * @deprecated Create a custom configuration file and use the config option.
     */
    @Deprecated
    public void addFamily(final Family family) {
        families.add(family);
    }

    /**
     * Adds a style sheet to the system.
     *
     * @param styleSheet The style sheet to use for formatting.
     * @deprecated use {@link #setStylesheet(String)}
     */
    @Deprecated
    public void addStylesheet(final Resource styleSheet) {
        DeprecationReporter.logDeprecated("element stylesheet", "0.17", true, "<outputStyle> element");
        setStylesheet(styleSheet.getName());
    }

    /**
     * Adds a given style sheet to the report.
     *
     * @param styleSheet style sheet to use in this report.
     * @deprecated use {@link #setOutputStyle(String)}
     */
    @Deprecated
    public void addStyleSheet(final Resource styleSheet) {
        DeprecationReporter.logDeprecated("attribute styleSheet", "0.17", true, "<outputStyle> element");
        setOutputStyle(styleSheet.getName());
    }

    /**
     * Styles the report or deliver xml document.
     *
     * @param styleReport true to use the plain-rat style
     * @deprecated use {@link #setOutputStyle(String)} and pass "xml" or "plain-rat".
     */
    @Deprecated
    public void setStyleReport(final boolean styleReport) {
        setOutputStyle(styleReport ? "xml" : "plain-rat");
    }

    /**
     * Determines if the output should be styled.
     *
     * @param style the name of the style sheet ot use, or "styled" for plain-rat style
     * @deprecated use {@link #setStylesheet(String)}
     */
    @Deprecated
    public void setFormat(final String style) {
        DeprecationReporter.logDeprecated("attribute format", "0.17", true, "outputStyle element");
        if ("styled".equalsIgnoreCase(style)) {
            setOutputStyle("plain-rat");
        } else {
            setOutputStyle(style);
        }
    }

    /**
     * Adds as a file containing the definitions of licenses to the system.
     *
     * @param fileName the file to add.
     * @deprecated create a configuration file and use the &lt;config&gt; child element.
     */
    @Deprecated
    public void setLicenses(final File fileName) {
        DeprecationReporter.logDeprecated("attribute licenses", "0.17", true, "<licences> element");
        setArg(Arg.CONFIGURATION.option().getLongOpt(), fileName.getAbsolutePath());
    }

    /**
     * Specifies whether to add the default list of license matchers.
     *
     * @param useDefaultLicenses if {@code true} use the default licenses.
     * @deprecated use noDefaultLicenses attribute
     */
    @Deprecated
    public void setUseDefaultLicenses(final boolean useDefaultLicenses) {
        DeprecationReporter.logDeprecated("attribute useDefaultLicenses", "0.17", true, "noDefaultLicenses attribute");
        setNoDefaultLicenses(!useDefaultLicenses);
    }

    /**
     * Adds a family category to the list of approved licenses.
     *
     * @param familyCategory the category to add.
     * @deprecated use licensesApproved child element.
     */
    @Deprecated
    public void setAddApprovedLicense(final String familyCategory) {
        DeprecationReporter.logDeprecated("attribute addApprovedLicense", "0.17", true, "<licensesApproved> element");
        deprecatedConfig.approvedLicenseCategories.add(familyCategory);
    }

    /**
     * Adds a family category to the list of approved licenses.
     *
     * @param familyCategory the category to add
     * @deprecated use licensesFamiliesApproved child element
     */
    @Deprecated
    public void addAddApprovedLicense(final String familyCategory) {
        DeprecationReporter.logDeprecated("element <addApprovedLicense>", "0.17", true, "<licenseFamiliesApproved> element");
        deprecatedConfig.approvedLicenseCategories.add(familyCategory);
    }

    /**
     * Removes a family category to the list of approved licenses.
     *
     * @param familyCategory the category to add.
     * @deprecated use licensesFamiliesDenied child element
     */
    @Deprecated
    public void setRemoveApprovedLicense(final String familyCategory) {
        DeprecationReporter.logDeprecated("attribute addApprovedLicense", "0.17", true, "<licenseFamiliesApproved> element");
        deprecatedConfig.removedLicenseCategories.add(familyCategory);
    }

    /**
     * Removes a family category to the list of approved licenses.
     *
     * @param familyCategory the category to add.
     * @deprecated use licensesFamiliesDenied child element
     */
    @Deprecated
    public void addRemoveApprovedLicense(final String familyCategory) {
        DeprecationReporter.logDeprecated("element <removeApprovedLicense>", "0.17", true, "<licenseFamiliesDenied> element");
        deprecatedConfig.removedLicenseCategories.add(familyCategory);
    }

    /**
     * Removes a family category to the list of approved licenses.
     *
     * @param familyCategory the category to remove
     * @deprecated use licenseFamiliesDenied element
     */
    @Deprecated
    public void setRemoveApprovedLicense(final String[] familyCategory) {
        DeprecationReporter.logDeprecated("attribute removeApprovedLicense", "0.17", true, "<licenseFamiliesDenied> element");
        deprecatedConfig.removedLicenseCategories.addAll(Arrays.asList(familyCategory));
    }

    /**
     * Removes a family category to the list of approved licenses.
     *
     * @param familyCategory the category to remove.
     * @deprecated use licenseFamilyDenied element
     */
    @Deprecated
    public void addRemoveApprovedLicense(final String[] familyCategory) {
        DeprecationReporter.logDeprecated("element <removeApprovedLicense>", "0.17", true, "<licenseFamiliesDenied> element");
        deprecatedConfig.removedLicenseCategories.addAll(Arrays.asList(familyCategory));
    }

    /**
     * Sets the copyright message
     *
     * @param copyrightMessage the copyright message
     * @deprecated use copyright attribute
     */
    @Deprecated
    public void setCopyrightMessage(final String copyrightMessage) {
        setCopyright(copyrightMessage);
    }

    /**
     * Determines if license headers should be added.
     *
     * @param setting the setting.
     * @deprecated use editLicense and editOverwrite attributes
     */
    @Deprecated
    public void setAddLicenseHeaders(final AddLicenseHeaders setting) {
        DeprecationReporter.logDeprecated("attribute addLicenseHeaders", "0.17", true, "editLicense and editOverwrite attributes");
        switch (setting.getNative()) {
            case TRUE:
                setEditLicense(true);
                break;
            case FALSE:
                setEditLicense(false);
                break;
            case FORCED:
                setEditLicense(true);
                setEditOverwrite(true);
                break;
        }
    }

    /**
     * Adds definition information
     *
     * @param fileName the file to add
     * @deprecated Use Config child element
     */
    @Deprecated
    public void setAddDefaultDefinitions(final File fileName) {
        DeprecationReporter.logDeprecated("element <addDefaultDefinitions>", "0.17", true, "<config> element");
        setArg(Arg.CONFIGURATION.option().getLongOpt(), fileName.getAbsolutePath());
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
     * Add headers to files that do not have them.
     *
     * @deprecated use &lt;editCopyright&gt; amd &lt;editOverwrite&gt; instead.
     */
    @Deprecated
    public static class AddLicenseHeaders extends EnumeratedAttribute {
        /**
         * add license headers and create *.new file
         */
        static final String TRUE = "true";
        /**
         * do not add license headers
         */
        static final String FALSE = "false";
        /**
         * add license headers and overwrite existing files
         */
        static final String FORCED = "forced";

        public AddLicenseHeaders() {
        }

        public AddLicenseHeaders(final String s) {
            setValue(s);
        }

        @Override
        public String[] getValues() {
            return new String[]{TRUE, FALSE, FORCED};
        }

        public org.apache.rat.config.AddLicenseHeaders getNative() {
            return org.apache.rat.config.AddLicenseHeaders.valueOf(getValue().toUpperCase());
        }
    }

    /**
     * Specify the licenses that are approved.
     *
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
