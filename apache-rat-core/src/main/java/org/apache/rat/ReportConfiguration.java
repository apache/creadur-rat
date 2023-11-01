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
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

import org.apache.commons.io.function.IOSupplier;
import org.apache.rat.config.AddLicenseHeaders;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseFamilySetFactory;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.report.IReportable;

/**
 * A configuration object is used by the front end to invoke the
 * {@link Reporter}. The sole purpose of the front ends is to create the
 * configuration and invoke the {@link Reporter}.
 */
public class ReportConfiguration {    
    private SortedSet<ILicenseFamily> families = LicenseFamilySetFactory.emptyLicenseFamilySet();
    private SortedSet<ILicense> licenses = LicenseSetFactory.emptyLicenseSet();
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

    /**
     * @return The filename filter for the potential input files.
     */
    public FilenameFilter getInputFileFilter() {
        return inputFileFilter;
    }

    /**
     * @param inputFileFilter the filename filter to filter the input files.
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
     * @param styleSheet the XSLT style sheet to style the report with.
     */
    public void setStyleSheet(IOSupplier<InputStream> styleSheet) {
        this.styleSheet = styleSheet;
    }

    /**
     * Adds the licenses and approved licenses from the defaults object to the
     * configuration. <em>Side effect: </em> if the report should be styled and no
     * style sheet has been set the plain stylesheet from the defaults will be used.
     * 
     * @param defaults The defaults to set.
     */
    public void setFrom(Defaults defaults) {
        addLicenses(defaults.getLicenses(LicenseFilter.all));
        addApprovedLicenseCategories(defaults.getLicenseIds(LicenseFilter.approved));
        if (isStyleReport() && getStyleSheet() == null) {
            setStyleSheet(Defaults.getPlainStyleSheet());
        }
    }

    /**
     * 
     * @param styleSheet the XSLT style sheet to style the report with.
     * @throws IOException
     */
    public void setStyleSheet(File styleSheet) {
        Objects.requireNonNull(styleSheet, "styleSheet file should not be null");
        setStyleSheet(() -> styleSheet.toURI().toURL().openStream());
    }

    /**
     * @return {@code true} if the XML report should be styled.
     */
    public boolean isStyleReport() {
        return styleReport;
    }

    /**
     * @param styleReport specifies whether or not the XML report should be styled.
     */
    public void setStyleReport(boolean styleReport) {
        this.styleReport = styleReport;
    }

    /**
     * @param out The OutputStream supplier that provides the output stream to write
     * the report to. (may be null)
     */
    public void setOut(IOSupplier<OutputStream> out) {
        this.out = out;
    }

    /**
     * Sets the OutputStream supplier to use the specified file.
     * 
     * @see #setOut(IOSupplier)
     * @param file The file to create the supplier with.
     */
    public void setOut(File file) {
        Objects.requireNonNull(file, "output file should not be null");
        setOut(() -> new FileOutputStream(file));
    }

    /**
     * Returns the output stream supplier. If no stream has been set returns a
     * supplier for System.out.
     * 
     * @return The supplier of the output stream to write the report to.
     */
    public IOSupplier<OutputStream> getOutput() {
        return out == null ? () -> System.out : out;
    }

    /**
     * @return A supplier for a PrintWriter that wraps the output stream.
     * @see #getOutput()
     */
    public IOSupplier<PrintWriter> getWriter() {
        return () -> new PrintWriter(new OutputStreamWriter(getOutput().get(), Charset.forName("UTF-8")));
    }

    /**
     * Adds a license to the list of licenses. Does not add the license to the list
     * of approved licenses.
     * 
     * @param license The license to add to the list of licenses.
     */
    public void addLicense(ILicense license) {
        if (license != null) {
            this.licenses.add(license);
            this.families.add(license.getLicenseFamily());
        }
    }
    
    /**
     * Adds a license to the list of licenses. Does not add the license to the list
     * of approved licenses.
     * 
     * @param builder The license builder to build and add to the list of licenses.
     */
    public ILicense addLicense(ILicense.Builder builder) {
        if (builder != null) {
            ILicense license = builder.build(families);
            this.licenses.add(license);
            return license;
        }
        return null;
    }

    /**
     * Adds multiple licenses to the list of licenses. Does not add the licenses to
     * the list of approved licenses.
     *
     * @param license The license to add.
     */
    public void addLicenses(Collection<ILicense> licenses) {
        this.licenses.addAll(licenses);
        licenses.stream().map(ILicense::getLicenseFamily).forEach(families::add);
    }
    
    /**
     * Adds a license family to the list of families. Does not add the family to the list
     * of approved licenses.
     * 
     * @param family The license family to add to the list of license families.
     */
    public void addFamily(ILicenseFamily family) {
        if (family != null) {
            this.families.add(family);
        }
    }

    /**
     * Adds a license family to the list of families. Does not add the family to the list
     * of approved licenses.
     * 
     * @param builder The licenseFamily.Builder to build and add to the list of licenses.
     */
    public void addFamily(ILicenseFamily.Builder builder) {
        if (builder != null) {
            this.families.add(builder.build());
        }
    }
    
    /**
     * Adds multiple families to the list of license families. Does not add the licenses to
     * the list of approved licenses.
     *
     * @param license The licensefamily to add.
     */
    public void addFamilies(Collection<ILicenseFamily> families) {
        this.families.addAll(families);
    }
    
    public SortedSet<ILicenseFamily> getFamilies() {
        return Collections.unmodifiableSortedSet(families);
    }
    
    /**
     * Adds an ILicenseFamily to the list of approved licenses.
     *
     * @param approvedILicenseFamily the LicenseFamily to add..
     */
    public void addApprovedLicenseCategory(ILicenseFamily approvedILicenseFamily) {
        approvedLicenseCategories.add(approvedILicenseFamily.getFamilyCategory());
    }

    /**
     * Adds a license family category (id) to the list of approved licenses
     * 
     * @param familyCategory the category to add.
     */
    public void addApprovedLicenseCategory(String familyCategory) {
        approvedLicenseCategories.add(ILicenseFamily.makeCategory(familyCategory));
    }

    /**
     * Adds a collection of license family categories to the set of approved license
     * names.
     *
     * @param approvedLicenseCategories set of approved license categories..
     */
    public void addApprovedLicenseCategories(Collection<String> approvedLicenseCategories) {
        approvedLicenseCategories.stream().forEach(this::addApprovedLicenseCategory);
    }

    /**
     * Adds a license family category to the list of approved licenses. <em>Once a
     * license has been removed from the approved list it can not be re-added</em>
     * 
     * @param familyCategory the category to add.
     */
    public void removeApprovedLicenseCategory(String familyCategory) {
        removedLicenseCategories.add(ILicenseFamily.makeCategory(familyCategory));
    }

    /**
     * Removes a license family category from the list of approved licenses.
     * <em>Once a license has been removed from the approved list it can not be
     * re-added</em>
     * 
     * @param familyCategory the family category to remove.
     */
    public void removeApprovedLicenseCategories(Collection<String> familyCategory) {
        familyCategory.stream().forEach(this::removeApprovedLicenseCategory);
    }

    /**
     * Gets the SortedSet of approved license categories. <em>Once a license has
     * been removed from the approved list it can not be re-added</em>
     * 
     * @return the Sorted set of approved license categories.
     */
    public SortedSet<String> getApprovedLicenseCategories() {
        SortedSet<String> result = new TreeSet<>(approvedLicenseCategories);
        result.removeAll(removedLicenseCategories);
        return result;
    }

    /**
     * Returns the optional license copyright being added if RAT is adding headers.
     * This value is ignored, if no license headers are added.
     * 
     * @return the optional copyright message.
     * @see #isAddingLicenses()
     */
    public String getCopyrightMessage() {
        return copyrightMessage;
    }

    /**
     * Sets the optional copyright message used if RAT is adding license headers.
     * This value is ignored, if no license headers are added.
     *
     * @param copyrightMessage message to set.
     * @see #setAddingLicenses(boolean)
     */
    public void setCopyrightMessage(String copyrightMessage) {
        this.copyrightMessage = copyrightMessage;
    }

    /**
     * This value is ignored if RAT is not adding licenses.
     * 
     * @return {@code true} if RAT is forcing the adding license headers.
     * @see #isAddingLicenses()
     */
    public boolean isAddingLicensesForced() {
        return addingLicensesForced;
    }

    /**
     * @return whether RAT should add missing license headers.
     * @see #isAddingLicensesForced()
     * @see #getCopyrightMessage()
     */
    public boolean isAddingLicenses() {
        return addingLicenses;
    }

    /**
     * Sets whether RAT should enable, disable, or force the adidng of license
     * headers.
     * 
     * @param addLicenseHeaders enables/disables or forces adding of licenses
     * headers.
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
     * Gets a set Licenses of depending on the {@code filter} if filter is set:
     * <ul>
     * <li>{@code all} - All licenses will be returned.</li>
     * <li>{@code approved} - Only approved licenses will be returned</li>
     * <li>{@code none} - No licenses will be returned</li>
     * </ul>
     * 
     * @param filter The license filter.
     * @return The set of defined licenses.
     */
    public SortedSet<ILicense> getLicenses(LicenseFilter filter) {
        switch (filter) {
        case all:
            return Collections.unmodifiableSortedSet(licenses);
        case approved:
            return new LicenseSetFactory(licenses, getApprovedLicenseCategories()).getLicenses(filter);
        case none:
        default:
            return LicenseSetFactory.emptyLicenseSet();
        }
    }

    /**
     * Gets a sorted set of ILicenseFamily objects based on {@code filter}. if
     * filter is set:
     * <ul>
     * <li>{@code all} - All licenses families will be returned.</li>
     * <li>{@code approved} - Only approved license families will be returned</li>
     * <li>{@code none} - No license families will be returned</li>
     * </ul>
     * 
     * @param filter The license filter.
     * @return The set of defined licenses.
     */
    public SortedSet<ILicenseFamily> getLicenseFamilies(LicenseFilter filter) {
        return new LicenseFamilySetFactory(families, getApprovedLicenseCategories()).getFamilies(filter);
    }

    /**
     * Validates that the configuration is valid.
     * 
     * @param a String consumer to log warning messages to.
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
