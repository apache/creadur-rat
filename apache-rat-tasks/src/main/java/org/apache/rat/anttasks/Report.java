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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.rat.ConfigurationException;
import org.apache.rat.Defaults;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.Reporter;
import org.apache.rat.configuration.LicenseReader;
import org.apache.rat.configuration.Readers;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.types.EnumeratedAttribute;
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

    private Defaults.Builder defaultsBuilder;
    private final ReportConfiguration configuration;
    /**
     * will hold any nested resource collection
     */
    private Union nestedResources;

    public Report() {
        configuration = new ReportConfiguration();
        configuration.setOut(new LogOutputStream(this, Project.MSG_INFO));
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
        configuration.setInputFileFilter(inputFileFilter);
    }
    
    public void setReportFile(File reportFile) {
        try {
            configuration.setOut( new FileOutputStream(reportFile));
        } catch (FileNotFoundException e) {
            throw new BuildException("Can not open report file", e);
        }
    }

    public void setStyleSheet(File styleSheet) {
        try {
            configuration.setStyleSheet(new FileInputStream(styleSheet));
        } catch (FileNotFoundException e) {
            throw new BuildException("Can not open style sheet", e);
        }
        configuration.setStyleReport(true);
    }

    public void setStyleReport(boolean styleReport) {
        configuration.setStyleReport(styleReport);
    }

    public void setLicenses(File fileName) {
        try {
            LicenseReader reader = Readers.get(fileName);
            reader.add(fileName.toURI().toURL());
            configuration.addLicenses(reader.readLicenses());
            configuration.addApprovedLicenseNames(reader.approvedLicenseId());
        } catch (MalformedURLException e) {
            throw new BuildException("Can not read license file " + fileName, e);
        }
    }


    /**
     * @param useDefaultLicenses Whether to add the default list of license matchers.
     */
    public void setUseDefaultLicenses(boolean useDefaultLicenses) {
        if (!useDefaultLicenses) {
            defaultsBuilder.noDefault();
        }
    }
    
    public void setApprovalFilter(ApprovalFilter filter) {
        configuration.setLicenseFilter(filter.internalFilter());
    }
    
    public void setAddApprovedLicense(String familyCategory) {
        configuration.addApprovedLicenseName(familyCategory);
    }
    
    public void setRemoveApprovedLicense(String familyCategory) {
        configuration.removeApprovedLicenseName(familyCategory);
    }

    public void setCopyrightMessage(String copyrightMessage) {
        configuration.setCopyrightMessage(copyrightMessage);
    }

    public void setAddLicenseHeaders(AddLicenseHeaders setting) {
        if (setting.getValue().equals(AddLicenseHeaders.FALSE)) {
            configuration.setAddingLicenses(false);
            configuration.setAddingLicensesForced(false);
        } else {
            configuration.setAddingLicenses(true);
            configuration.setAddingLicensesForced(setting.getValue().equals(AddLicenseHeaders.FORCED));
        }
    }

    public void setAddDefaultDefinitions(File fileName) {
        try {
            defaultsBuilder.add(fileName);
        } catch (MalformedURLException e) {
            throw new BuildException("Can not open additional default definitions: " + fileName.toString(), e);
        }
    }

    /**
     * Generates the report.
     */
    @Override
    public void execute() {
        Defaults defaults = defaultsBuilder.build();
        configuration.setFrom(defaults);
        configuration.setReportable(new ResourceCollectionContainer(nestedResources));
        try {
            validate();
            Reporter.report(configuration);
        } catch (BuildException e) {
            throw e;
        } catch (Exception ioex) {
            throw new BuildException(ioex);
        } finally {
            configuration.close();
        }
    }

    /**
     * validates the task's configuration.
     */
    private void validate() {
        try {
            configuration.validate(s -> log(s, Project.MSG_WARN));
        } catch (ConfigurationException e) {
            throw new BuildException(e.getMessage(), e.getCause());
        }
        if (nestedResources == null) {
            throw new BuildException("You must specify at least one file to" + " create the report for.");
        }
        configuration.setReportable(new ResourceCollectionContainer(nestedResources));
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
            return Arrays.stream(Defaults.Filter.values()).map(Defaults.Filter::name).collect(Collectors.toList())
                    .toArray(new String[Defaults.Filter.values().length]);
        }

        public Defaults.Filter internalFilter() {
            return Defaults.Filter.valueOf(getValue());
        }
    }
}
