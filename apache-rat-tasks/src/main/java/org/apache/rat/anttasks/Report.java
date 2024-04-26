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
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.rat.ConfigurationException;
import org.apache.rat.Defaults;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.Reporter;
import org.apache.rat.configuration.Format;
import org.apache.rat.configuration.LicenseReader;
import org.apache.rat.configuration.MatcherReader;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.utils.Log;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
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
public class Report extends Task {

    private final Defaults.Builder defaultsBuilder;
    private final ReportConfiguration configuration;
    private final List<License> licenses = new ArrayList<>();
    private final List<Family> families = new ArrayList<>();
    /**
     * will hold any nested resource collection
     */
    private Union nestedResources;

    public Report() {
        configuration = new ReportConfiguration(new Logger());
        configuration.setOut(() -> new LogOutputStream(this, Project.MSG_INFO));
        defaultsBuilder = Defaults.builder();
    }

    /**
     * Adds resources that will be checked.
     * 
     * @param rc resource to check.
     */
    public void add(ResourceCollection rc) {
        if (nestedResources == null) {
            nestedResources = new Union();
        }
        nestedResources.add(rc);
    }

    public void setInputFileFilter(FilenameFilter inputFileFilter) {
        configuration.setFilesToIgnore(inputFileFilter);
    }

    public void setReportFile(File reportFile) {
        configuration.setOut(reportFile);
    }

    public void addLicense(License lic) {
        licenses.add(lic);
    }

    public void addFamily(Family family) {
        families.add(family);
    }

    /**
     * 
     * @param styleSheet
     * @deprecated use {@link #addStyleSheet(Resource)}
     */
    @Deprecated
    public void addStylesheet(Resource styleSheet) {
        addStyleSheet(styleSheet);
    }

    /**
     * Adds a given style sheet to the report.
     * @param styleSheet style sheet to use in this report.
     */
    public void addStyleSheet(Resource styleSheet) {
        configuration.setStyleSheet(styleSheet::getInputStream);
        configuration.setStyleReport(true);
    }

    public void setStyleReport(boolean styleReport) {
        configuration.setStyleReport(styleReport);
    }

    /**
     * 
     * @param style
     * @deprecated use #setStyleReport
     */
    @Deprecated
    public void setFormat(String style) {
        setStyleReport("styled".equalsIgnoreCase(style));

    }

    public void setLicenses(File fileName) {
        try {
            URL url = fileName.toURI().toURL();
            Format fmt = Format.fromFile(fileName);
            MatcherReader mReader = fmt.matcherReader();
            if (mReader != null) {
                mReader.addMatchers(url);
            }
            LicenseReader lReader = fmt.licenseReader();
            if (lReader != null) {
                lReader.addLicenses(url);
                configuration.addLicenses(lReader.readLicenses());
                configuration.addApprovedLicenseCategories(lReader.approvedLicenseId());
            }
        } catch (MalformedURLException e) {
            throw new BuildException("Can not read license file " + fileName, e);
        }
    }

    /**
     * @param useDefaultLicenses Whether to add the default list of license
     * matchers.
     */
    public void setUseDefaultLicenses(boolean useDefaultLicenses) {
        if (!useDefaultLicenses) {
            defaultsBuilder.noDefault();
        }
    }

    public void setAddApprovedLicense(String familyCategory) {
        configuration.addApprovedLicenseCategory(familyCategory);
    }

    public void addAddApprovedLicense(String familyCategory) {
        configuration.addApprovedLicenseCategory(familyCategory);
    }

    public void setRemoveApprovedLicense(String familyCategory) {
        configuration.removeApprovedLicenseCategory(familyCategory);
    }

    public void setRemoveApprovedLicense(String[] familyCategory) {
        configuration.removeApprovedLicenseCategories(Arrays.asList(familyCategory));
    }

    public void setCopyrightMessage(String copyrightMessage) {
        configuration.setCopyrightMessage(copyrightMessage);
    }

    public void setAddLicenseHeaders(AddLicenseHeaders setting) {
        configuration.setAddLicenseHeaders(setting.getNative());
    }

    public void setAddDefaultDefinitions(File fileName) {
        try {
            defaultsBuilder.add(fileName);
        } catch (MalformedURLException e) {
            throw new BuildException("Can not open additional default definitions: " + fileName.toString(), e);
        }
    }

    public ReportConfiguration getConfiguration() {
        Defaults defaults = defaultsBuilder.build(configuration.getLog());

        configuration.setFrom(defaults);
        configuration.setReportable(new ResourceCollectionContainer(nestedResources));
        families.stream().map(Family::build).forEach(configuration::addFamily);
        licenses.stream().map(License::asBuilder)
                .forEach(l -> configuration.addApprovedLicenseCategory(configuration.addLicense(l).getLicenseFamily()));
        return configuration;
    }

    /**
     * Generates the report.
     */
    @Override
    public void execute() {
        try {
            Reporter r = new Reporter(validate(getConfiguration()));
            r.output(null, () -> new ReportConfiguration.NoCloseOutputStream(System.out));
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
    private ReportConfiguration validate(ReportConfiguration cfg) {
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
     */
    public static class AddLicenseHeaders extends EnumeratedAttribute {
        static final String TRUE = "true";
        static final String FALSE = "false";
        static final String FORCED = "forced";

        public AddLicenseHeaders() {
        }

        public AddLicenseHeaders(String s) {
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
     */
    public static class ApprovalFilter extends EnumeratedAttribute {

        public ApprovalFilter() {
        }

        public ApprovalFilter(String s) {
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
    
    private class Logger implements Log {

        private void write(int level, String msg) {
            try (PrintWriter pw = new PrintWriter(new LogOutputStream(Report.this, level)))
            {
               pw.write(msg);
            }
        }

        @Override
        public void log(Level level, String msg) {
            switch (level) {
            case DEBUG:
                write(Project.MSG_DEBUG, msg);
                break;
            case INFO:
                write(Project.MSG_INFO, msg);
                break;
            case WARN:
                write(Project.MSG_WARN, msg);
                break;
            case ERROR:
                write(Project.MSG_ERR, msg);
                break;
			case OFF:
				break;
			default:
				break;
            }
        }
        
    }
}
