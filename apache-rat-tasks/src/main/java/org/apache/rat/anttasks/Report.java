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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
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

    /**
     * will hold any nested resource collection
     */
    private Union nestedResources;
    /**
     * The licenses we want to match on.
     */
    private final ArrayList<IHeaderMatcher> licenseMatchers = new ArrayList<>();

    private final ArrayList<ILicenseFamily> licenseNames = new ArrayList<>();

    /**
     * Whether to add the default list of license matchers.
     */
    private boolean addDefaultLicenseMatchers = true;
    /**
     * Where to send the report.
     */
    private File reportFile;
    /**
     * Which format to use.
     */
    private Format format = Format.PLAIN;
    /**
     * Which stylesheet to use.
     */
    private Resource stylesheet;
    /**
     * Whether to add license headers.
     */
    private AddLicenseHeaders addLicenseHeaders = new AddLicenseHeaders(AddLicenseHeaders.FALSE);
    /**
     * The copyright message.
     */
    private String copyrightMessage;

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
        licenseMatchers.add(matcher);
    }

    public void add(ILicenseFamily license) {
        licenseNames.add(license);
    }

    /**
     * @param addDefaultLicenseMatchers Whether to add the default list of license matchers.
     */
    public void setAddDefaultLicenseMatchers(boolean addDefaultLicenseMatchers) {
        this.addDefaultLicenseMatchers = addDefaultLicenseMatchers;
    }

    /**
     * Where to send the report to.
     * @param f report output file.
     */
    public void setReportFile(File f) {
        reportFile = f;
    }

    /**
     * Which format to use.
     * @param f format. 
     */
    public void setFormat(Format f) {
        if (f == null) {
            throw new IllegalArgumentException("format must not be null");
        }
        format = f;
    }

    /**
     * @param pAdd Whether to add license headers. 
     */
    public void setAddLicenseHeaders(AddLicenseHeaders pAdd) {
        if (pAdd == null) {
            throw new IllegalArgumentException("addLicenseHeaders must not be null");
        }
        addLicenseHeaders = pAdd;
    }

    /**
     * @param pMessage copyright message to set.
     */
    public void setCopyrightMessage(String pMessage) {
        copyrightMessage = pMessage;
    }
    
    /**
     * Which stylesheet to use (only meaningful with format='styled').
     * @param u stylesheet.
     */
    public void addConfiguredStylesheet(Union u) {
        if (stylesheet != null || u.size() != 1) {
            throw new BuildException("You must not specify more than one stylesheet.");
        }
        stylesheet = u.iterator().next();
    }

    /**
     * Generates the report.
     */
    @Override
    public void execute() {
        validate();

        PrintWriter out = null;
        try {
            if (reportFile == null) {
                out = new PrintWriter(
                          new OutputStreamWriter(
                              new LogOutputStream(this, Project.MSG_INFO)
                          ));
            } else {
                out = new PrintWriter(new OutputStreamWriter(
                        new FileOutputStream(reportFile),
                        Charset.forName("UTF-8")
                ));
            }
            createReport(out);
            out.flush();
        } catch (IOException | RatException | InterruptedException | TransformerException ioex) {
            throw new BuildException(ioex);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * validates the task's configuration.
     */
    private void validate() {
        if (nestedResources == null) {
            throw new BuildException("You must specify at least one file to"
                                     + " create the report for.");
        }
        if (!addDefaultLicenseMatchers && licenseMatchers.size() == 0) {
            throw new BuildException("You must specify at least one license"
                                     + " matcher");
        }
        if (format.getValue().equals(Format.STYLED_KEY)) {
            if (stylesheet == null) {
                throw new BuildException("You must specify a stylesheet when"
                                         + " using the 'styled' format");
            }
            if (!stylesheet.isExists()) {
                throw new BuildException("Cannot find specified stylesheet '"
                                         + stylesheet + "'");
            }
        } else if (stylesheet != null) {
            log("Ignoring stylesheet '" + stylesheet + "' when using format '"
                + format.getValue() + "'", Project.MSG_WARN);
        }
    }

    /**
     * Writes the report to the given stream.
     * 
     * @param out stream to write report to.
     * 
     * @throws IOException in case of I/O errors.
     * @throws InterruptedException in case of threading errors.
     * @throws TransformerException in case of XML errors.
     * @throws RatException in case of general errors.
     */
    private void createReport(PrintWriter out) throws IOException, TransformerException, InterruptedException, RatException {
        final ReportConfiguration configuration = new ReportConfiguration();
        configuration.setHeaderMatcher(new HeaderMatcherMultiplexer(getLicenseMatchers()));
        configuration.setApprovedLicenseNames(getApprovedLicenseNames());
        configuration.setApproveDefaultLicenses(addDefaultLicenseMatchers);
        
        if (AddLicenseHeaders.FORCED.equalsIgnoreCase(addLicenseHeaders.getValue())) {
            configuration.setAddingLicenses(true);
            configuration.setAddingLicensesForced(true);
            configuration.setCopyrightMessage(copyrightMessage);
        } else if (AddLicenseHeaders.TRUE.equalsIgnoreCase(addLicenseHeaders.getValue())) {
            configuration.setAddingLicenses(true);
            configuration.setCopyrightMessage(copyrightMessage);
        } else if (!AddLicenseHeaders.FALSE.equalsIgnoreCase(addLicenseHeaders.getValue())) {
            throw new BuildException("Invalid value for addLicenseHeaders: " + addLicenseHeaders.getValue());
        }
        ResourceCollectionContainer rcElement = new ResourceCollectionContainer(nestedResources);
        if (format.getValue().equals(Format.XML_KEY)) {
            org.apache.rat.Report.report(rcElement, out, configuration);
        } else {
            InputStream style = null;
            try {
                if (format.getValue().equals(Format.PLAIN_KEY)) {
                    style = Defaults.getPlainStyleSheet();
                } else if (format.getValue().equals(Format.STYLED_KEY)) {
                    style = stylesheet.getInputStream();
                } else {
                    throw new BuildException("unsupported format '"
                                             + format.getValue() + "'");
                }
                org.apache.rat.Report.report(out, rcElement, style,
                                             configuration);
            } finally {
                FileUtils.close(style);
            }
        }
    }

    /**
     * Flattens all nested matchers plus the default matchers (if
     * required) into a single array.
     */
    private List<IHeaderMatcher> getLicenseMatchers() {
        List<IHeaderMatcher> matchers = new ArrayList<>(
                (addDefaultLicenseMatchers ? Defaults.DEFAULT_MATCHERS.size() : 0) + licenseMatchers.size());
        if (addDefaultLicenseMatchers) {
            matchers.addAll(Defaults.DEFAULT_MATCHERS);
            matchers.addAll(licenseMatchers);
        } else {
            matchers = new ArrayList<>(licenseMatchers);
        }
        return matchers;
    }

    private ILicenseFamily[] getApprovedLicenseNames() {
        // TODO: add support for adding default licenses
        ILicenseFamily[] results = null;
        if (licenseNames.size() > 0) {
            results = licenseNames.toArray(new ILicenseFamily[0]);
        }
        return results;
    }

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
