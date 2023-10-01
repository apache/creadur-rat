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
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.io.function.IOSupplier;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.rat.ReportConfiguration.LicenseFilter;
import org.apache.rat.config.AddLicenseHeaders;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.policy.DefaultPolicy;
import org.apache.rat.report.IReportable;

/**
 * A configuration object is used by the front end to invoke the
 * {@link Reporter}. Basically, the sole purpose of the front ends is to create
 * the configuration and invoke the {@link Reporter}.
 */
public class ReportConfiguration {
    public enum LicenseFilter {
        all, approved, none
    }

    private static final Log logger = LogFactory.getLog(ReportConfiguration.class);
    private SortedSet<ILicense> licenses = new TreeSet<>(ILicense.getComparator());
    private SortedSet<String> approvedLicenseCategories = new TreeSet<>();
    private SortedSet<String> removedLicenseCategories = new TreeSet<>();
    private boolean addingLicenses;
    private boolean addingLicensesForced;
    private String copyrightMessage;
    private IOSupplier<OutputStream> out = null;
    private boolean styleReport = true;
    private IOSupplier<InputStream> styleSheet = null;
    private IReportable reportable = null;
    private FilenameFilter inputFileFilter = null;
    private LicenseFilter approvalFilter = LicenseFilter.approved;

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
    public IOSupplier<InputStream> getStyleSheet() {
        return styleSheet;
    }

    /**
     * 
     * @param styleSheet the XSLT style sheet to style the report with.
     */
    public void setStyleSheet(IOSupplier<InputStream> styleSheet) {
        this.styleSheet = styleSheet;
    }

    public void setFrom(Defaults defaults) {
        addLicenses(defaults.getLicenses());
        addApprovedLicenseCategories(defaults.getLicenseIds());
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
        Objects.requireNonNull(styleSheet, "styleSheet file should not be null");
        final URL url = styleSheet.toURI().toURL();
        setStyleSheet(()->url.openStream());
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
    public void setOut(IOSupplier<OutputStream> out) {
        this.out = out;
    }

    /**
     * Returns the output stream. If no stream has been set returns System.out.
     * 
     * @return The output stream to write to.
     */
    public IOSupplier<OutputStream> getOutput() {
        return out == null ? ()->System.out : out;
    }

    /**
     * 
     * @return A PrintWriter that wraps the output stream.
     */
    public IOSupplier<PrintWriter> getWriter() {
        return () -> new PrintWriter(new OutputStreamWriter(getOutput().get(), Charset.forName("UTF-8")));
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
     * Adds a license to the list of approved license names.
     *
     * @param approvedLicenseNames set of approved license names.
     */
    public void addApprovedLicenseCategory(ILicenseFamily approvedLicenseName) {
        approvedLicenseCategories.add(approvedLicenseName.getFamilyCategory());
    }

    public void addApprovedLicenseCategory(String familyCategory) {
        approvedLicenseCategories.add(ILicenseFamily.makeCategory(familyCategory));
    }

    /**
     * Adds to the set of approved license names (convenience).
     *
     * @param approvedLicenseNames set of approved license names.
     */
    public void addApprovedLicenseCategories(Collection<String> approvedLicenseNames) {
        approvedLicenseNames.stream().forEach(this::addApprovedLicenseCategory);
    }

    public void removeApprovedLicenseCategory(String familyCategory) {
        removedLicenseCategories.add(ILicenseFamily.makeCategory(familyCategory));
    }

    public void removeApprovedLicenseCategories(Collection<String> familyCategory) {
        familyCategory.stream().forEach(this::removeApprovedLicenseCategory);
    }
    
    public SortedSet<String> getApprovedLicenseCategories() {
        SortedSet<String> result = new TreeSet<>(approvedLicenseCategories);
        result.removeAll(removedLicenseCategories);
        return result;
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
    public void setAddLicenseHeaders(AddLicenseHeaders addLicenseHeaders) {
        addingLicenses = false;
        addingLicensesForced = false;
        switch (addLicenseHeaders) {
        case FALSE:
            // do nothing
            break;
        case FORCED:
            addingLicensesForced = true;
            // fall through
        case TRUE:
            addingLicenses = true;
            break;
        }
    }

    /**
     * Returns a set Licenses of depending on the approvalFilter setting.
     * if approvalFilter is set:
     * <ul>
     * <li>{@code all} - All licenses will be returned.</li>
     * <li>{@code approved} - (default) All approved licenses will be returned</li>
     * <li>{@code none} - No licenses will be returned</li>
     * </ul>
     * 
     * @return The set of defined licenses.
     */
    public SortedSet<ILicense> getLicenses(LicenseFilter filter) {
        switch (filter) {
        case all:
            return Collections.unmodifiableSortedSet(licenses);
        case approved:
            SortedSet<String> approvedLicenses = getApprovedLicenseCategories();
            SortedSet<ILicense> result = new TreeSet<>(ILicense.getComparator());
            licenses.stream().filter(x -> approvedLicenses.contains(x.getLicenseFamily().getFamilyCategory()))
                    .forEach(result::add);
            return result;
        case none:
        default:
            return Collections.emptySortedSet();
        }
    }

    public SortedSet<ILicenseFamily> getLicenseFamilies(LicenseFilter filter) {
        SortedSet<ILicenseFamily> result = new TreeSet<>();
        getLicenses(filter).stream().map(ILicense::getLicenseFamily).forEach(result::add);
        return result;
    }

    /**
     * Set which files will be listed as approved.
     * 
     * @param filter the fileter type.
     * @return this Builder for chaining
     */
    public void setLicenseFilter(LicenseFilter filter) {
        approvalFilter = filter;
    }

    public LicenseFilter getLicenseFilter() {
        return approvalFilter;
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
}
