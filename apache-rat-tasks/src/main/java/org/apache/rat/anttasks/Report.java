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

 import org.apache.commons.io.IOUtils;
 import org.apache.rat.Defaults;
 import org.apache.rat.ReportConfiguration;
 import org.apache.rat.ConfigurationException;
import org.apache.rat.Reporter;
import org.apache.rat.analysis.IHeaderMatcher;
 import org.apache.rat.analysis.util.HeaderMatcherMultiplexer;
 import org.apache.rat.api.RatException;
 import org.apache.rat.license.ILicenseFamily;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.taskdefs.LogOutputStream;
 import org.apache.tools.ant.types.EnumeratedAttribute;
 import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.Union;
import org.apache.tools.ant.util.FileUtils;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A basic Ant task that generates a report on all files specified by
 * the nested resource collection(s).
 *
 * <p>IHeaderMatcher(s) can be specified as nested elements as well.</p>
 *
 * <p>The attribute <code>format</code> defines the output format and
 * can take the values
 * <ul>
 *   <li>xml - Rat's native XML output.</li>
 *   <li>styled - transforms the XML output using the given
 *   stylesheet.  The stylesheet attribute must be set as well if this
 *   attribute is used.</li>
 *   <li>plain - plain text using Rat's built-in stylesheet.  This is
 *   the default.</li>
 * </ul>
 */
public class Report extends Task {
    
    private org.apache.rat.Reporter reporter;
    private Defaults.Builder defaultsBuilder;
    private final ReportConfiguration configuration;

    /**
     * will hold any nested resource collection
     */
    private Union nestedResources;
    /**
     * The licenses we want to match on.
     */
    //private final ArrayList<IHeaderMatcher> licenseMatchers = new ArrayList<>();

    //private final ArrayList<ILicenseFamily> licenseNames = new ArrayList<>();

    /**
     * Whether to add the default list of license matchers.
     */
    //private boolean addDefaultLicenseMatchers = true;
    /**
     * Where to send the report.
     */
    //private File reportFile;
    /**
     * Which stylesheet to use.
     */
    //private Resource stylesheet;
    /**
     * Whether to add license headers.
     */
    //private AddLicenseHeaders addLicenseHeaders = new AddLicenseHeaders(AddLicenseHeaders.FALSE);
    /**
     * The copyright message.
     */
    //private String copyrightMessage;

    public Report() {
        configuration = new ReportConfiguration();
        configuration.setOut(new LogOutputStream(this, Project.MSG_INFO));
        defaultsBuilder = Defaults.builder();
    }
    /**
     * Adds resources that will be checked.
     * @param rc resource to check.
     */
    public void add(ResourceCollection rc) {
        if (nestedResources == null) {
            nestedResources = new Union();
        }
        nestedResources.add(rc);
    }

    /**
     * @param matcher Adds a license matcher.
     */
    public void add(IHeaderMatcher matcher) {
        configuration.addHeaderMatcher(matcher);
    }

    public void add(ILicenseFamily license) {
        configuration.addApprovedLicenseName(license);
    }

    /**
     * @param addDefaultLicenseMatchers Whether to add the default list of license matchers.
     */
    public void setAddDefaultLicenseMatchers(boolean addDefaultLicenseMatchers) {
        if (!addDefaultLicenseMatchers) {
            defaultsBuilder.noDefault();
        }
    }

    /**
     * Where to send the report to.
     * @param f report output file.
     */
    public void setReportFile(File f) {
        try {
            configuration.setOut(new FileOutputStream(f));
        } catch (FileNotFoundException e) {
            throw new BuildException("Can not open output file.", e);
        }
    }

    /**
     * Which format to use.
     * @param f format. 
     */
    public void setFormat(Format f) {
        if (f == null) {
            throw new IllegalArgumentException("format must not be null");
        }
        configuration.setStyleReport(! f.getValue().equals(Format.XML_KEY));
    }

    /**
     * @param pAdd Whether to add license headers. 
     */
    public void setAddLicenseHeaders(AddLicenseHeaders pAdd) {
        if (pAdd == null) {
            throw new IllegalArgumentException("addLicenseHeaders must not be null");
        }
        if (!Arrays.asList(pAdd.getValues()).contains(pAdd.getValue().toUpperCase())) {
            throw new IllegalArgumentException("Invalid value for addLicenseHeaders: " + pAdd.getValue());
        }
        if (!pAdd.getValue().equalsIgnoreCase(AddLicenseHeaders.FALSE)) {
            configuration.setAddingLicenses(true);
            configuration.setAddingLicensesForced(pAdd.getValue().equalsIgnoreCase(AddLicenseHeaders.FORCED));
        }
    }

    /**
     * @param copyrightMessage copyright message to set.
     */
    public void setCopyrightMessage(String copyrightMessage) {
        configuration.setCopyrightMessage(copyrightMessage);
    }
    
    /**
     * Which stylesheet to use (only meaningful with format='styled').
     * @param u stylesheet.
     */
    public void addConfiguredStylesheet(Union u) {
        if (configuration.getStyleSheet() != null || u.size() != 1) {
            throw new BuildException("You must not specify more than one stylesheet.");
        }
        Resource stylesheet = u.iterator().next(); 
        try {
            configuration.setStyleSheet(stylesheet.getInputStream());
        } catch (IOException e) {
            throw new BuildException("Stylesheet not readable", e);
        }
        
    }

    /**
     * Generates the report.
     */
    @Override
    public void execute() {
        configuration.setReportable(new ResourceCollectionContainer(nestedResources));
        try {
            validate();
            Reporter.report(configuration);
        } catch (BuildException e) {
            throw e;
        }catch (Exception ioex) {
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
            configuration.validate( s -> log(s,Project.MSG_WARN));
        } catch (ConfigurationException e) {
            throw new BuildException(e.getMessage(), e.getCause());
        }
        if (nestedResources == null) {
            throw new BuildException("You must specify at least one file to"
                                     + " create the report for.");
        }
    }

//    /**
//     * Flattens all nested matchers plus the default matchers (if
//     * required) into a single array.
//     */
//    private List<IHeaderMatcher> getLicenseMatchers() {
//        List<IHeaderMatcher> matchers = new ArrayList<>(
//                (addDefaultLicenseMatchers ? Defaults.DEFAULT_MATCHERS.size() : 0) + licenseMatchers.size());
//        if (addDefaultLicenseMatchers) {
//            matchers.addAll(Defaults.DEFAULT_MATCHERS);
//            matchers.addAll(licenseMatchers);
//        } else {
//            matchers = new ArrayList<>(licenseMatchers);
//        }
//        return matchers;
//    }
//
//    private ILicenseFamily[] getApprovedLicenseNames() {
//        // TODO: add support for adding default licenses
//        ILicenseFamily[] results = null;
//        if (licenseNames.size() > 0) {
//            results = licenseNames.toArray(new ILicenseFamily[0]);
//        }
//        return results;
//    }

    /**
     * Type for the format attribute.
     */
    public static class Format extends EnumeratedAttribute {
        static final String XML_KEY = "xml";
        static final String STYLED_KEY = "styled";
        static final String PLAIN_KEY = "plain";

        static final Format PLAIN = new Format(PLAIN_KEY);

        public Format() { super(); }

        private Format(String s) {
            this();
            setValue(s);
        }

        @Override
        public String[] getValues() {
            return new String[] {
                XML_KEY, STYLED_KEY, PLAIN_KEY
            };
        }
    }

    /**
     * Type for the addLicenseHeaders attribute.
     */
    public static class AddLicenseHeaders extends EnumeratedAttribute {
        static final String TRUE = "true";
        static final String FALSE = "false";
        static final String FORCED = "forced";

        public AddLicenseHeaders() {}
        public AddLicenseHeaders(String s) {
            setValue(s);
        }
        
        @Override
        public String[] getValues() {
            return new String[] {
                TRUE, FALSE, FORCED
            };
        }
    }
}
