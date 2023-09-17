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
package org.apache.rat;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.rat.Defaults.LicenseCollectionMatcher;
import org.apache.rat.configuration.ILicenseFamilyProxy;
import org.apache.rat.configuration.ILicenseProxy;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.policy.DefaultPolicy;
import org.apache.rat.report.IReportable;

/**
 * A configuration object is used by the front end to invoke the
 * {@link Reporter}. Basically, the sole purpose of the front ends is to create
 * the configuration and invoke the {@link Reporter}.
 */
public class ReportConfiguration implements AutoCloseable {
    private static final Log logger = LogFactory.getLog(ReportConfiguration.class);
    private SortedSet<ILicense> licenses = new TreeSet<>(ILicense.getComparator());
    private List<ILicenseFamily> approvedLicenseNames = new ArrayList<>();
    private boolean addingLicenses;
    private boolean addingLicensesForced;
    private String copyrightMessage;
    private OutputStream out = null;
    private boolean styleReport = true;
    private InputStream styleSheet = null;
    private IReportable reportable = null;
    private FilenameFilter inputFileFilter = null;

    /**
     * @return The filename filter for the potential input files.
     */
    public FilenameFilter getInputFileFilter() {
        return inputFileFilter;
    }

    /**
     * @param inputFileFilter the filter to filter the on disk files being
     * evaluated.
     */
    public void setInputFileFilter(FilenameFilter inputFileFilter) {
        this.inputFileFilter = inputFileFilter;
    }

    /**
     * @return the thing being reported on.
     */
    public IReportable getReportable() {
        return reportable;
    }

    /**
     * @param reportable the thing being reported on.
     */
    public void setReportable(IReportable reportable) {
        this.reportable = reportable;
    }

    /**
     * @return the XSLT style sheet to style the report with.
     */
    public InputStream getStyleSheet() {
        return styleSheet;
    }

    /**
     * 
     * @param styleSheet the XSLT style sheet to style the report with.
     */
    public void setStyleSheet(InputStream styleSheet) {
        if (this.styleSheet != null) {
            try {
                this.styleSheet.close();
            } catch (IOException e) {
                logger.warn("Error closing earlier style sheet", e);
            }
        }
        this.styleSheet = styleSheet;
    }
    
    public void setFrom(Defaults defaults) {
        addLicense(defaults.createDefaultMatcher());
        addApprovedLicenseNames(defaults.getLicenseFamilies());
        if (isStyleReport() && getStyleSheet() == null) {
            setStyleSheet(Defaults.getPlainStyleSheet());
        }
    }

    /**
     * 
     * @param styleSheet the XSLT style sheet to style the report with.
     * @throws IOException
     * @throws MalformedURLException
     */
    public void setStyleSheet(File styleSheet) throws MalformedURLException, IOException {
        setStyleSheet(styleSheet.toURI().toURL().openStream());
    }

    /**
     * 
     * @return True if the XML report should be styled.
     */
    public boolean isStyleReport() {
        return styleReport;
    }

    /**
     * 
     * @param styleReport true if the XML report should be styled
     */
    public void setStyleReport(boolean styleReport) {
        this.styleReport = styleReport;
    }

    /**
     * @param out The stream to write the output to.
     */
    public void setOut(OutputStream out) {
        this.out = out;
    }

    /**
     * Returns the output stream. If no stream has been set returns System.out.
     * 
     * @return The output stream to write to.
     */
    public OutputStream getOutput() {
        return out == null ? System.out : out;
    }

    /**
     * 
     * @return A PrintWriter that wraps the output stream.
     */
    public PrintWriter getWriter() {
        return new PrintWriter(new OutputStreamWriter(getOutput(), Charset.forName("UTF-8")));
    }

    /**
     * Returns the license that is the combination of all licenses being tested.
     *
     * @return the license matcher, or null if no licenses are specified.
     */
    public ILicense getLicense() {
        if (licenses.isEmpty()) {
            return null;
        }
        if (licenses.size() == 1) {
            return licenses.first();
        }
        return new LicenseCollectionMatcher(licenses);
    }

    /**
     * Adds licenses to the list of licenses to be checked.
     *
     * @param license The license t oadd.
     */
    public void addLicense(ILicense license) {
        if (license != null) {
            this.licenses.add(license);
        }
    }

    /**
     * Adds licenses to the list of licenses to be checked.
     *
     * @param license The license t oadd.
     */
    public void addLicenses(Collection<ILicense> licenses) {
        this.licenses.addAll(licenses);
    }

    /**
     *
     * @return the set of approved license names.
     */
    public ILicenseFamily[] getApprovedLicenseNames() {
        return approvedLicenseNames.toArray(new ILicenseFamily[approvedLicenseNames.size()]);
    }

    /**
     * Adds to the set of approved license names.
     *
     * @param approvedLicenseNames set of approved license names.
     */
    public void addApprovedLicenseNames(ILicenseFamily[] approvedLicenseNames) {
        addApprovedLicenseNames(Arrays.asList(approvedLicenseNames));
    }

    /**
     * Adds a license to the list of approved license names.
     *
     * @param approvedLicenseNames set of approved license names.
     */
    public void addApprovedLicenseName(ILicenseFamily approvedLicenseName) {
        approvedLicenseNames.add(approvedLicenseName);
    }

    public void addApprovedLicenseName(String familyCategory) {
        ILicense licenseProxy = ILicenseProxy.create(familyCategory, licenses);
        addApprovedLicenseName(ILicenseFamilyProxy.create(licenseProxy));
    }

    /**
     * Adds to the set of approved license names (convenience).
     *
     * @param approvedLicenseNames set of approved license names.
     */
    public void addApprovedLicenseNames(Collection<ILicenseFamily> approvedLicenseNames) {
        this.approvedLicenseNames.addAll(approvedLicenseNames);
    }

    /**
     * @return If Rat is adding license headers: Returns the optional copyright
     * message. This value is ignored, if no license headers are added.
     * @see #isAddingLicenses()
     */
    public String getCopyrightMessage() {
        return copyrightMessage;
    }

    /**
     * If Rat is adding license headers: Sets the optional copyright message. This
     * value is ignored, if no license headers are added.
     *
     * @param copyrightMessage message to set.
     * @see #setAddingLicenses(boolean)
     */
    public void setCopyrightMessage(String copyrightMessage) {
        this.copyrightMessage = copyrightMessage;
    }

    /**
     * @return If Rat is adding license headers: Returns, whether adding license
     * headers is enforced. This value is ignored, if no license headers are added.
     * @see #isAddingLicenses()
     */
    public boolean isAddingLicensesForced() {
        return addingLicensesForced;
    }

    /**
     * If Rat is adding license headers: Sets, whether adding license headers is
     * enforced. This value is ignored, if no license headers are added.
     *
     * @param addingLicensesForced enable/disable forcibly adding licenses.
     * @see #isAddingLicenses()
     */
    public void setAddingLicensesForced(boolean addingLicensesForced) {
        this.addingLicensesForced = addingLicensesForced;
    }

    /**
     * @return Returns, whether Rat should add missing license headers.
     * @see #isAddingLicensesForced()
     * @see #getCopyrightMessage()
     */
    public boolean isAddingLicenses() {
        return addingLicenses;
    }

    /**
     * Returns, whether Rat should add missing license headers.
     *
     * @param addingLicenses enables/disables adding of licenses.
     * @see #setAddingLicensesForced(boolean)
     * @see #setCopyrightMessage(String)
     */
    public void setAddingLicenses(boolean addingLicenses) {
        this.addingLicenses = addingLicenses;
    }

    /**
     * Validates that the configuration is valid.
     * 
     * @param logger the system to write warnings to.
     */
    public void validate(Consumer<String> logger) {
        if (reportable == null) {
            throw new ConfigurationException("Reportable may not be null");
        }
        if (licenses.size() == 0) {
            throw new ConfigurationException("You must specify at least one license");
        }
        if (styleSheet != null && !isStyleReport()) {
            logger.accept("Ignoring stylesheet because styling is not selected");
        }
        if (styleSheet == null && isStyleReport()) {
            throw new ConfigurationException("Stylesheet must be specified if report styling is selected");
        }
    }

    public DefaultPolicy getDefaultPolicy() {
        return new DefaultPolicy(getApprovedLicenseNames());
    }

    @Override
    public void close() {
        if (styleSheet != null) {
            try {
                styleSheet.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                styleSheet = null;
            }
        }
        if (out != null) {
            if (out != System.out && out != System.err) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            out = null;
        }
    }
}
