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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.function.IOSupplier;
import org.apache.rat.config.AddLicenseHeaders;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseFamilySetFactory;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.report.IReportable;
import org.apache.rat.utils.Log;
import org.apache.rat.utils.ReportingSet;

/**
 * A configuration object is used by the front end to invoke the
 * {@link Reporter}. The sole purpose of the front ends is to create the
 * configuration and invoke the {@link Reporter}.
 */
public class ReportConfiguration {

    /**
     * The styles of processing for various categories of documents.
     */
    public enum Processing {
        /** List file as present only */
        NOTIFICATION("List file as present"),
        /** List all present licenses */
        PRESENCE("List any licenses found"),
        /** List all present licenses and unknown licenses */
        ABSENCE("List licenses found and any unknown licences");

        /**
         * the description of the processing.
         */
        private final String description;


        Processing(final String description) {
            this.description = description;
        }

        /**
         * Gets the description of the processing type.
         * @return
         */
        public String desc() {
            return description;
        }
    }

    /**
     * The set of defined families.
     */
    private final ReportingSet<ILicenseFamily> families;
    /**
     * The set of defined licenses
     */
    private final ReportingSet<ILicense> licenses;
    /**
     * The set of approved license family categories.
     */
    private final SortedSet<String> approvedLicenseCategories;
    /**
     * The set of license categories that have are to be removed from consideration.
     */
    private final SortedSet<String> removedLicenseCategories;
    /**
     * {@code true} if we are adding license headers to the files.
     */
    private boolean addingLicenses;
    /**
     * {@code true} if we are adding license headers in place (no *.new files)
     */
    private boolean addingLicensesForced;
    /**
     * The copyright message to add if we are adding headers.  Will be null if we are not
     * adding copyright messages.
     */
    private String copyrightMessage;
    /**
     * The IOSupplier that provides the output stream to write the report to.
     */
    private IOSupplier<OutputStream> out;
    /**
     * {@code true} if the report should be styled.  {@code false} if XML should be produced.
     */
    private boolean styleReport;
    /**
     * The IOSupplier that provides the stylesheet to style the XML output.
     */
    private IOSupplier<InputStream> styleSheet;
    /**
     * The Reportable instance that provides the documents to process.
     */
    private IReportable reportable;
    /**
     * A file filter that matches files to ignore.
     */
    private IOFileFilter filesToIgnore;
    /**
     * A file filter that matches directories to ignore.
     */
    private IOFileFilter directoriesToIgnore;
    /**
     * the log to write messages to.
     */
    private final Log log;
    /**
     * The default filter for displaying families.
     */
    private LicenseFilter listFamilies;
    /**
     * The default filter for displaying licenses.
     */
    private LicenseFilter listLicenses;
    /**
     * {@code true} if this is a dry run and no processing is to take place.
     */
    private boolean dryRun;
    /**
     * How to process ARCHIVE document types.
     */
    private Processing archiveProcessing;
    /**
     * How to process STANDARD document types.
     */
    private Processing standardProcessing;
    /**
     * Constructor
     * @param log The Log implementation that messages will be written to.
     */
    public ReportConfiguration(final Log log) {
        this.log = log;
        families = new ReportingSet<>(LicenseFamilySetFactory.emptyLicenseFamilySet()).setLog(log)
                .setMsgFormat(s -> String.format("Duplicate LicenseFamily category: %s", s.getFamilyCategory()));
        licenses = new ReportingSet<>(LicenseSetFactory.emptyLicenseSet()).setLog(log)
                .setMsgFormat(s -> String.format("Duplicate License %s (%s) of type %s", s.getName(), s.getId(), s.getLicenseFamily().getFamilyCategory()));
        approvedLicenseCategories = new TreeSet<>();
        removedLicenseCategories = new TreeSet<>();
        styleReport = true;
        listFamilies = Defaults.LIST_FAMILIES;
        listLicenses = Defaults.LIST_LICENSES;
        dryRun = false;
    }

    /**
     * Retrieves the archive processing type.
     * @return The archive processing type.
     */
    public Processing getArchiveProcessing() {
        return archiveProcessing == null ? Defaults.ARCHIVE_PROCESSING : archiveProcessing;
    }

    /**
     * Sets the archive processing type.  If not set will default to NOTIFICATION.
     * @param archiveProcessing the type of processing archives should have.
     */
    public void setArchiveProcessing(final Processing archiveProcessing) {
        this.archiveProcessing = archiveProcessing;
    }

    /**
     * Retrieves the archive processing type.
     * @return The archive processing type.
     */
    public Processing getStandardProcessing() {
        return standardProcessing == null ? Defaults.STANDARD_PROCESSING : standardProcessing;
    }

    /**
     * Sets the archive processing type.  If not set will default to NOTIFICATION.
     * @param standardProcessing the type of processing archives should have.
     */
    public void setStandardProcessing(final Processing standardProcessing) {
        this.standardProcessing = standardProcessing;
    }

    /**
     * Retrieves the Log that was provided in the constructor.
     * @return the Log for the system.
     */
    public Log getLog() {
        return log;
    }
    /**
     * Set the log level for reporting collisions in the set of license families.
     * <p>NOTE: should be set before licenses or license families are added.</p>
     * @param level The log level to use.
     */
    public void logFamilyCollisions(final Log.Level level) {
        families.setLogLevel(level);
    }
    
    /**
     * Sets the reporting option for duplicate license families.
     * @param state The ReportingSet.Option to use for reporting.
     */
    public void familyDuplicateOption(final ReportingSet.Options state) {
        families.setDuplicateOption(state);
    }

    /**
     * Sets the log level for reporting license collisions.
     * @param level The log level.
     */
    public void logLicenseCollisions(final Log.Level level) {
        licenses.setLogLevel(level);
    }
    
    /**
     * Sets the reporting option for duplicate licenses.
     * @param state the ReportingSt.Option to use for reporting.
     */
    public void licenseDuplicateOption(final ReportingSet.Options state) {
        licenses.setDuplicateOption(state);
    }
    
    /**
     * Set the level of license families that should be output in the XML document.
     * @param filter the license families to list.
     */
    public void listFamilies(final LicenseFilter filter) {
        listFamilies = filter;
    }
    
    public LicenseFilter listFamilies() {
        return listFamilies;
    }
    
    /**
     * Set the level of licenses that should be output in the XML document.
     * @param filter the licenses to list.
     */
    public void listLicenses(final LicenseFilter filter) {
        listLicenses = filter;
    }

    /**
     * Gets the selected license filter.
     * @return the filter to limit license display.
     */
    public LicenseFilter listLicenses() {
        return listLicenses;
    }
    
    /**
     * Sets the dry run flag.
     * @param state the state for the dry run flag.
     */
    public void setDryRun(final boolean state) {
        dryRun = state;
    }
    
    /**
     * Returns the state of the dry run flag.
     * @return the state of the dry run flag.
     */
    public boolean isDryRun() {
        return dryRun;
    }
    
    /**
     * Gets the file name filter for files to ignore.
     * @return The filename filter that identifies files to ignore.
     */
    public IOFileFilter getFilesToIgnore() {
        return filesToIgnore == null ? Defaults.FILES_TO_IGNORE : filesToIgnore;
    }

    /**
     * Sets the files name filter for files to ignore.
     * If not set or set to {@code null} uses the Default.FILES_TO_IGNORE value.
     * @param filesToIgnore the filename filter to filter the input files.
     * @see Defaults#FILES_TO_IGNORE
     */
    public void setFilesToIgnore(final IOFileFilter filesToIgnore) {
        this.filesToIgnore = filesToIgnore;
    }

    /**
     * Gets the directories filter.
     * @return the directories filter.
     */
    public IOFileFilter getDirectoriesToIgnore() {
        return directoriesToIgnore == null ? Defaults.DIRECTORIES_TO_IGNORE : directoriesToIgnore;
    }

    /**
     * Sets the directories to ignore.
     * If {@code directoriesToIgnore} is null removes all directories to ignore.
     * If not set {@code Defaults.DIRECTORIES_TO_IGNORE} is used.
     * @param directoriesToIgnore the filter that defines the directories to ignore.
     * @see Defaults#DIRECTORIES_TO_IGNORE
     */
    public void setDirectoriesToIgnore(final IOFileFilter directoriesToIgnore) {
        this.directoriesToIgnore = directoriesToIgnore == null ? FalseFileFilter.FALSE : directoriesToIgnore;
    }

    /**
     * Adds a directory filter to the directories to ignore.
     * @param directoryToIgnore the directory filter to add.
     */
    public void addDirectoryToIgnore(final IOFileFilter directoryToIgnore) {
        this.directoriesToIgnore = this.directoriesToIgnore.or(directoryToIgnore);
    }

    /**
     * Gets the reportable object.
     * @return the thing being reported on.
     */
    public IReportable getReportable() {
        return reportable;
    }

    /**
     * Sets the reportable object.
     * @param reportable the thing being reported on.
     */
    public void setReportable(final IReportable reportable) {
        this.reportable = reportable;
    }

    /**
     * Gets the IOSupplier with the style sheet.
     * @return the Supplier of the InputStream that is the XSLT style sheet to style
     * the report with.
     */
    public IOSupplier<InputStream> getStyleSheet() {
        return styleSheet;
    }

    /**
     * Sets the style sheet for custom processing. The IOSupplier may be called
     * multiple times, so the input stream must be able to be opened and closed
     * multiple times.
     * @param styleSheet the XSLT style sheet to style the report with.
     */
    public void setStyleSheet(final IOSupplier<InputStream> styleSheet) {
        this.styleSheet = styleSheet;
    }

    /**
     * Adds the licenses and approved licenses from the defaults object to the
     * configuration. <em>Side effect: </em> if the report should be styled and no
     * style sheet has been set the plain stylesheet from the defaults will be used.
     * @param defaults The defaults to set.
     */
    public void setFrom(final Defaults defaults) {
        addLicensesIfNotPresent(defaults.getLicenses(LicenseFilter.ALL));
        addApprovedLicenseCategories(defaults.getLicenseIds(LicenseFilter.APPROVED));
        if (isStyleReport() && getStyleSheet() == null) {
            setStyleSheet(Defaults.getPlainStyleSheet());
        }
    }

    /**
     * Sets the style sheet.
     * @param styleSheet the XSLT style sheet file to style the report with.
     */
    public void setStyleSheet(final File styleSheet) {
        Objects.requireNonNull(styleSheet, "styleSheet file should not be null");
        setStyleSheet(styleSheet.toURI());
    }

    /**
     * Sets the style sheet for custom processing. The stylesheet may be opened
     * multiple times so the URI must be capable of being opened multiple times.
     * @param styleSheet the URI of the XSLT style sheet to style the report with.
     */
    public void setStyleSheet(final URI styleSheet) {
        Objects.requireNonNull(styleSheet, "styleSheet file should not be null");
        try {
            setStyleSheet(styleSheet.toURL());
        } catch (MalformedURLException e) {
            throw new ConfigurationException("Unable to process stylesheet", e);
        }
    }

    /**
     * Sets the style sheet for custom processing. The stylesheet may be opened
     * multiple times so the URL must be capable of being opened multiple times.
     * @param styleSheet the URL of the XSLT style sheet to style the report with.
     */
    public void setStyleSheet(final URL styleSheet) {
        Objects.requireNonNull(styleSheet, "styleSheet file should not be null");
        setStyleSheet(styleSheet::openStream);
    }

    /**
     * Returns {@code true} if the XML report should be styled.
     * @return {@code true} if the XML report should be styled.
     */
    public boolean isStyleReport() {
        return styleReport;
    }

    /**
     * Specifies that the XML report should or should not be styled.
     * @param styleReport specifies whether the XML report should be styled.
     */
    public void setStyleReport(final boolean styleReport) {
        this.styleReport = styleReport;
    }

    /**
     * Sets the supplier for the output stream. The supplier may be called multiple
     * times to provide the stream. Suppliers should prepare streams that are
     * appended to and that can be closed. If an {@code OutputStream} should not be
     * closed consider wrapping it in a {@code NoCloseOutputStream}
     * @param out The OutputStream supplier that provides the output stream to write
     * the report to. A null value will use System.out.
     * @see NoCloseOutputStream
     */
    public void setOut(final IOSupplier<OutputStream> out) {
        this.out = out;
    }

    /**
     * Sets the OutputStream supplier to use the specified file. The file may be
     * opened and closed several times. File is deleted first and then may be
     * repeatedly opened in append mode.
     * @see #setOut(IOSupplier)
     * @param file The file to create the supplier with.
     */
    public void setOut(final File file) {
        Objects.requireNonNull(file, "output file should not be null");
        if (file.exists()) {
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                log.warn("Unable to delete file:" + file);
            }
        }
        setOut(() -> new FileOutputStream(file, true));
    }

    /**
     * Returns the output stream supplier. If no stream has been set returns a
     * supplier for System.out.
     * @return The supplier of the output stream to write the report to.
     */
    public IOSupplier<OutputStream> getOutput() {
        return out == null ? () -> new NoCloseOutputStream(System.out) : out;
    }

    /**
     * Gets a PrintWriter that wraps the output stream.
     * @return A supplier for a PrintWriter that wraps the output stream.
     * @see #getOutput()
     */
    public IOSupplier<PrintWriter> getWriter() {
        return () -> new PrintWriter(new OutputStreamWriter(getOutput().get(), StandardCharsets.UTF_8));
    }

    /**
     * Adds a license to the list of licenses. Does not add the license to the list
     * of approved licenses.
     * @param license The license to add to the list of licenses.
     */
    public void addLicense(final ILicense license) {
        if (license != null) {
            this.licenses.add(license);
            this.families.addIfNotPresent(license.getLicenseFamily());
        }
    }

    /**
     * Adds a license to the list of licenses. Does not add the license to the list
     * of approved licenses.
     * @param builder The license builder to build and add to the list of licenses.
     * @return The ILicense implementation that was added.
     */
    public ILicense addLicense(final ILicense.Builder builder) {
        if (builder != null) {
            ILicense license = builder.setLicenseFamilies(families).build();
            this.licenses.add(license);
            return license;
        }
        return null;
    }

    /**
     * Adds multiple licenses to the list of licenses. Does not add the licenses to
     * the list of approved licenses.
     * @param licenses The licenses to add.
     */
    public void addLicenses(final Collection<ILicense> licenses) {
        this.licenses.addAll(licenses);
        licenses.stream().map(ILicense::getLicenseFamily).forEach(families::add);
    }

    /**
     * Adds multiple licenses to the list of licenses. Does not add the licenses to
     * the list of approved licenses.
     * @param licenses The licenses to add.
     */
    public void addLicensesIfNotPresent(final Collection<ILicense> licenses) {
        this.licenses.addAllIfNotPresent(licenses);
        licenses.stream().map(ILicense::getLicenseFamily).forEach(families::addIfNotPresent);
    }
    
    /**
     * Adds a license family to the list of families. Does not add the family to the
     * list of approved licenses.
     * @param family The license family to add to the list of license families.
     */
    public void addFamily(final ILicenseFamily family) {
        if (family != null) {
            this.families.add(family);
        }
    }

    /**
     * Adds a license family to the list of families. Does not add the family to the
     * list of approved licenses.
     * @param builder The licenseFamily.Builder to build and add to the list of
     * licenses.
     */
    public void addFamily(final ILicenseFamily.Builder builder) {
        if (builder != null) {
            this.families.add(builder.build());
        }
    }

    /**
     * Adds multiple families to the list of license families. Does not add the
     * licenses to the list of approved licenses.
     * @param families The license families to add.
     */
    public void addFamilies(final Collection<ILicenseFamily> families) {
        this.families.addAll(families);
    }

    /**
     * Adds an ILicenseFamily to the list of approved licenses.
     * @param approvedILicenseFamily the LicenseFamily to add.
     */
    public void addApprovedLicenseCategory(final ILicenseFamily approvedILicenseFamily) {
        approvedLicenseCategories.add(approvedILicenseFamily.getFamilyCategory());
    }

    /**
     * Adds a license family category (id) to the list of approved licenses
     * @param familyCategory the category to add.
     */
    public void addApprovedLicenseCategory(final String familyCategory) {
        approvedLicenseCategories.add(ILicenseFamily.makeCategory(familyCategory));
    }

    /**
     * Adds a collection of license family categories to the set of approved license
     * names.
     * @param approvedLicenseCategories set of approved license categories.
     */
    public void addApprovedLicenseCategories(final Collection<String> approvedLicenseCategories) {
        approvedLicenseCategories.forEach(this::addApprovedLicenseCategory);
    }

    /**
     * Adds a license family category to the list of approved licenses. <em>Once a
     * license has been removed from the approved list it cannot be re-added</em>
     * @param familyCategory the category to add.
     */
    public void removeApprovedLicenseCategory(final String familyCategory) {
        removedLicenseCategories.add(ILicenseFamily.makeCategory(familyCategory));
    }

    /**
     * Removes a license family category from the list of approved licenses.
     * <em>Once a license has been removed from the approved list it cannot be
     * re-added</em>
     * @param familyCategory the family category to remove.
     */
    public void removeApprovedLicenseCategories(final Collection<String> familyCategory) {
        familyCategory.forEach(this::removeApprovedLicenseCategory);
    }

    /**
     * Gets the SortedSet of approved license categories. <em>Once a license has
     * been removed from the approved list it cannot be re-added</em>
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
     * @return the optional copyright message.
     * @see #isAddingLicenses()
     */
    public String getCopyrightMessage() {
        return copyrightMessage;
    }

    /**
     * Sets the optional copyright message used if RAT is adding license headers.
     * This value is ignored, if no license headers are added.
     * @param copyrightMessage message to set.
     * @see #isAddingLicenses()
     */
    public void setCopyrightMessage(final String copyrightMessage) {
        this.copyrightMessage = copyrightMessage;
    }

    /**
     * This value is ignored if RAT is not adding licenses.
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
     * Sets whether RAT should enable, disable, or force the adding of license
     * headers.
     * @param addLicenseHeaders enables/disables or forces adding of licenses
     * headers.
     * @see #isAddingLicenses()
     * @see #setCopyrightMessage(String)
     */
    public void setAddLicenseHeaders(final AddLicenseHeaders addLicenseHeaders) {
        addingLicenses = false;
        addingLicensesForced = false;
        switch (addLicenseHeaders) {
        case FALSE:
            // do nothing
            break;
        case FORCED:
            addingLicensesForced = true;
            addingLicenses = true;
            break;
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
     * @param filter The license filter.
     * @return The set of defined licenses.
     */
    public SortedSet<ILicense> getLicenses(final LicenseFilter filter) {
        switch (filter) {
        case ALL:
            return Collections.unmodifiableSortedSet(licenses);
        case APPROVED:
            return new LicenseSetFactory(licenses, getApprovedLicenseCategories()).getLicenses(filter);
        case NONE:
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
     * @param filter The license filter.
     * @return The set of defined licenses.
     */
    public SortedSet<ILicenseFamily> getLicenseFamilies(final LicenseFilter filter) {
        return new LicenseFamilySetFactory(families, getApprovedLicenseCategories()).getFamilies(filter);
    }

    /**
     * Validates that the configuration is valid.
     * @param logger String consumer to log warning messages to.
     */
    public void validate(final Consumer<String> logger) {
        if (reportable == null) {
            throw new ConfigurationException("Reportable may not be null");
        }
        if (licenses.isEmpty()) {
            throw new ConfigurationException("You must specify at least one license");
        }
        if (styleSheet != null && !isStyleReport()) {
            logger.accept("Ignoring stylesheet because styling is not selected");
        }
        if (styleSheet == null && isStyleReport()) {
            throw new ConfigurationException("Stylesheet must be specified if report styling is selected");
        }
    }

    /**
     * A wrapper around an output stream that does not close the output stream.
     */
    public static class NoCloseOutputStream extends OutputStream {
        /** the output stream this stream wraps */
        private final OutputStream delegate;

        /**
         * Constructor.
         * @param delegate the output stream to wrap.
         */
        public NoCloseOutputStream(final OutputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public void write(final int arg0) throws IOException {
            delegate.write(arg0);
        }

        /**
         * Does not actually close the delegate.  But does perform a flush.
         * @throws IOException on Error.
         */
        @Override
        public void close() throws IOException {
            this.delegate.flush();
        }

        @Override
        public boolean equals(final Object obj) {
            return delegate.equals(obj);
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

        @Override
        public void write(final byte[] arg0, final int arg1, final int arg2) throws IOException {
            delegate.write(arg0, arg1, arg2);
        }

        @Override
        public void write(final byte[] b) throws IOException {
            delegate.write(b);
        }
    }
}
