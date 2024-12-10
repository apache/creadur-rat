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
import java.io.FileFilter;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.function.Consumer;

import org.apache.commons.io.function.IOSupplier;
import org.apache.rat.commandline.StyleSheets;
import org.apache.rat.config.AddLicenseHeaders;
import org.apache.rat.config.exclusion.ExclusionProcessor;
import org.apache.rat.config.exclusion.StandardCollection;
import org.apache.rat.config.results.ClaimValidator;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.document.FileDocument;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.report.IReportable;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log.Level;
import org.apache.rat.utils.ReportingSet;
import org.apache.rat.walker.FileListWalker;
import org.apache.rat.walker.IReportableListWalker;

/**
 * A configuration object is used by the front end to invoke the
 * {@link Reporter}. The sole purpose of the frontends is to create the
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
         * Description of the processing
         */
        private final String description;


        Processing(final String description) {
            this.description = description;
        }

        /**
         * Gets the description of the processing type.
         * @return the description of the processing type.
         */
        public String desc() {
            return description;
        }
    }

    /** The LicenseSetFactory for the configuration */
    private final LicenseSetFactory licenseSetFactory;

    /**
     * {@code true} if we are adding license headers to the files.
     */
    private boolean addingLicenses;
    /**
     * {@code true} if we are adding license headers in place (no *.new files)
     */
    private boolean addingLicensesForced;
    /**
     * The copyright message to add if we are adding headers. Will be null if we are not
     * adding copyright messages.
     */
    private String copyrightMessage;
    /**
     * The IOSupplier that provides the output stream to write the report to.
     */
    private IOSupplier<OutputStream> out;
    /**
     * The IOSupplier that provides the stylesheet to style the XML output.
     */
    private IOSupplier<InputStream> styleSheet;

    /**
     * A list of files to read file names from.
     */
    private final List<File> sources;

    /**
     * A list of reportables to process;
     */
    private final List<IReportable> reportables;

    /**
     * A predicate to test if a path should be included in the processing.
     */
    private final ExclusionProcessor exclusionProcessor;

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
     * The ClaimValidator to validate min/max counts and similar claims.
     */
    private final ClaimValidator claimValidator;
    /**
     * Constructor
     */
    public ReportConfiguration() {
        licenseSetFactory = new LicenseSetFactory();
        listFamilies = Defaults.LIST_FAMILIES;
        listLicenses = Defaults.LIST_LICENSES;
        dryRun = false;
        exclusionProcessor = new ExclusionProcessor();
        claimValidator = new ClaimValidator();
        sources = new ArrayList<>();
        reportables = new ArrayList<>();
    }

    /**
     * Adds a file as a source of files to scan.
     * The file must be a text file that lists files to be included.
     * File within the file must be in linux format with a
     * "/" file separator.
     * @param file the file to process.
     */
    public void addSource(final File file) {
        notNull(file, "File may not be null.");
        sources.add(file);
    }

    private void notNull(final Object o, final String msg) {
        if (o == null) {
            throw new ConfigurationException(msg);
        }
    }

    /**
     * Adds a Reportable as a source of files to scan.
     * @param reportable the reportable to process.
     */
    public void addSource(final IReportable reportable) {
        notNull(reportable, "Reportable may not be null.");
        reportables.add(reportable);
    }

    /**
     * Returns {@code true} if the configuration has any sources defined.
     * @return {@code true} if the configuration has any sources defined.
     */
    public boolean hasSource() {
        return !reportables.isEmpty() || !sources.isEmpty();
    }

    /**
     * Gets a builder initialized with any files specified as sources.
     * @return a configured builder.
     */
    public IReportableListWalker.Builder getSources() {
        DocumentName name = DocumentName.builder(new File(".")).build();
        IReportableListWalker.Builder builder = IReportableListWalker.builder(name);
        sources.forEach(file -> builder.addReportable(new FileListWalker(new FileDocument(file, DocumentNameMatcher.MATCHES_ALL))));
        reportables.forEach(builder::addReportable);
        return builder;
    }

    /**
     * Retrieves the archive processing type.
     * @return The archive processing type.
     */
    public Processing getArchiveProcessing() {
        return archiveProcessing == null ? Defaults.ARCHIVE_PROCESSING : archiveProcessing;
    }

    /**
     * Sets the archive processing type. If not set will default to NOTIFICATION.
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
     * Sets the archive processing type. If not set will default to NOTIFICATION.
     * @param standardProcessing the type of processing archives should have.
     */
    public void setStandardProcessing(final Processing standardProcessing) {
        this.standardProcessing = standardProcessing;
    }

    /**
     * Set the log level for reporting collisions in the set of license families.
     * <p>NOTE: should be set before licenses or license families are added.</p>
     * @param level The log level to use.
     */
    public void logFamilyCollisions(final Level level) {
        licenseSetFactory.logFamilyCollisions(level);
    }

    /**
     * Sets the reporting option for duplicate license families.
     * @param state The ReportingSet.Option to use for reporting.
     */
    public void familyDuplicateOption(final ReportingSet.Options state) {
        licenseSetFactory.familyDuplicateOption(state);
    }

    /**
     * Sets the log level for reporting license collisions.
     * @param level The log level.
     */
    public void logLicenseCollisions(final Level level) {
        licenseSetFactory.logLicenseCollisions(level);
    }

    /**
     * Sets the reporting option for duplicate licenses.
     * @param state the ReportingSt.Option to use for reporting.
     */
    public void licenseDuplicateOption(final ReportingSet.Options state) {
        licenseSetFactory.licenseDuplicateOption(state);
    }

    /**
     * Set the level of license families that should be output in the XML document.
     * @param filter the license families to list.
     */
    public void listFamilies(final LicenseFilter filter) {
        listFamilies = filter;
    }

    /**
     * Return the current filter that determines which families will be output in the XML document.
     * @return the filter that defines the families to list.
     */
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
     * Excludes a StandardCollection of patterns.
     * @param collection the StandardCollection to exclude.
     * @see ExclusionProcessor#addExcludedCollection(StandardCollection)
     */
    public void addExcludedCollection(final StandardCollection collection) {
        exclusionProcessor.addExcludedCollection(collection);
    }

    /**
     * Excludes the file processor defined in the StandardCollection.
     * @param collection the StandardCollection to exclude.
     * @see ExclusionProcessor#addFileProcessor(StandardCollection)
     */
    public void addExcludedFileProcessor(final StandardCollection collection) {
        exclusionProcessor.addFileProcessor(collection);
    }

    /**
     * Excludes files that match a FileFilter.
     * @param fileFilter the file filter to match.
     */
    public void addExcludedFilter(final FileFilter fileFilter) {
        exclusionProcessor.addExcludedMatcher(new DocumentNameMatcher(fileFilter));
    }

    /**
     * Excludes files that match a DocumentNameMatcher.
     * @param matcher the DocumentNameMatcher to match.
     */
    public void addExcludedMatcher(final DocumentNameMatcher matcher) {
        exclusionProcessor.addExcludedMatcher(matcher);
    }

    /**
     * Excludes files that match the pattern.
     *
     * @param patterns the collection of patterns to exclude.
     * @see ExclusionProcessor#addIncludedPatterns(Iterable)
     */
    public void addExcludedPatterns(final Iterable<String> patterns) {
        exclusionProcessor.addExcludedPatterns(patterns);
    }

    /**
     * Adds the patterns from the standard collection as included patterns.
     * @param collection the standard collection to include.
     */
    public void addIncludedCollection(final StandardCollection collection) {
        exclusionProcessor.addIncludedCollection(collection);
    }

    /**
     * Adds the fileFilter to filter files that should be included, this overrides any
     * exclusion of the same files.
     * @param fileFilter the filter to identify files that should be included.
     */
    public void addIncludedFilter(final FileFilter fileFilter) {
        exclusionProcessor.addIncludedMatcher(new DocumentNameMatcher(fileFilter));
    }

    /**
     * Add file patterns that are to be included. These patterns override any exclusion of
     * the same files.
     * @param patterns The iterable of Strings containing the patterns.
     */
    public void addIncludedPatterns(final Iterable<String> patterns) {
        exclusionProcessor.addIncludedPatterns(patterns);
    }

    /**
     * Get the DocumentNameMatcher based on the directory.
     * @param baseDir the DocumentName for the base directory.
     * @return the DocumentNameMatcher for the base directory.
     */
    public DocumentNameMatcher getNameMatcher(final DocumentName baseDir) {
        return exclusionProcessor.getNameMatcher(baseDir);
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
     * configuration. <em>Side effect:</em> if the report should be styled and no
     * style sheet has been set the plain stylesheet from the defaults will be used.
     * @param defaults The defaults to set.
     */
    public void setFrom(final Defaults defaults) {
        licenseSetFactory.add(defaults.getLicenseSetFactory());
        if (getStyleSheet() == null) {
            setStyleSheet(StyleSheets.PLAIN.getStyleSheet());
        }
        defaults.getStandardExclusion().forEach(this::addExcludedCollection);
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
        Objects.requireNonNull(styleSheet, "Stylesheet file must not be null");
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
        Objects.requireNonNull(styleSheet, "Stylesheet file must not be null");
        setStyleSheet(styleSheet::openStream);
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
                DefaultLog.getInstance().warn("Unable to delete file: " + file);
            }
        }
        File parent = file.getParentFile();
        if (!parent.mkdirs() && !parent.isDirectory()) {
            DefaultLog.getInstance().warn("Unable to create directory: " + file.getParentFile());
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
        licenseSetFactory.addLicense(license);
    }

    /**
     * Adds a license to the list of licenses. Does not add the license to the list
     * of approved licenses.
     * @param builder The license builder to build and add to the list of licenses.
     * @return The ILicense implementation that was added.
     */
    public ILicense addLicense(final ILicense.Builder builder) {
        return licenseSetFactory.addLicense(builder);
    }

    /**
     * Adds multiple licenses to the list of licenses. Does not add the licenses to
     * the list of approved licenses.
     * @param licenses The licenses to add.
     */
    public void addLicenses(final Collection<ILicense> licenses) {
        licenseSetFactory.addLicenses(licenses);
    }

    /**
     * Adds a license family to the list of families. Does not add the family to the
     * list of approved licenses.
     * @param family The license family to add to the list of license families.
     */
    public void addFamily(final ILicenseFamily family) {
       licenseSetFactory.addFamily(family);
    }

    /**
     * Adds a license family to the list of families. Does not add the family to the
     * list of approved licenses.
     * @param builder The licenseFamily.Builder to build and add to the list of
     * licenses.
     */
    public void addFamily(final ILicenseFamily.Builder builder) {
        licenseSetFactory.addFamily(builder);
    }

    /**
     * Adds multiple families to the list of license families. Does not add the
     * licenses to the list of approved licenses.
     * @param families The license families to add.
     */
    public void addFamilies(final Collection<ILicenseFamily> families) {
        families.forEach(this::addApprovedLicenseCategory);
    }

    /**
     * Adds an ILicenseFamily to the list of approved licenses.
     * @param approvedILicenseFamily the LicenseFamily to add.
     */
    public void addApprovedLicenseCategory(final ILicenseFamily approvedILicenseFamily) {
        addApprovedLicenseCategory(approvedILicenseFamily.getFamilyCategory());
    }

    /**
     * Adds a license family category (id) to the list of approved licenses
     * @param familyCategory the category to add.
     */
    public void addApprovedLicenseCategory(final String familyCategory) {
        licenseSetFactory.approveLicenseCategory(familyCategory);
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
        licenseSetFactory.removeLicenseCategory(ILicenseFamily.makeCategory(familyCategory));
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
     * @param filter The LicenseFilter to filter the categories by.
     * @return the Sorted set of approved license categories.
     */
    public SortedSet<String> getLicenseCategories(final LicenseFilter filter) {
        return licenseSetFactory.getLicenseCategories(filter);
    }

    /**
     * Gets the SortedSet of approved license categories. <em>Once a license has
     * been removed from the approved list it cannot be re-added</em>
     * @param filter The LicenseFilter to filter the licenses by.
     * @return the Sorted set of approved license categories.
     */
    public SortedSet<ILicense> getLicenses(final LicenseFilter filter) {
        return licenseSetFactory.getLicenses(filter);
    }

    /**
     * Gets the SortedSet of approved license categories. <em>Once a license has
     * been removed from the approved list it cannot be re-added</em>
     * @param filter The LicenseFilter to filter the licenses by.
     * @return the Sorted set of approved license categories.
     */
    public SortedSet<String> getLicenseIds(final LicenseFilter filter) {
        return licenseSetFactory.getLicenseIds(filter);
    }

    /**
     * Adds an ILicenseFamily to the list of approved licenses.
     * @param approvedLicense the License to add.
     */
    public void addApprovedLicenseId(final ILicense approvedLicense) {
        addApprovedLicenseId(approvedLicense.getId());
    }

    /**
     * Adds a license family category (id) to the list of approved licenses
     * @param licenseId the license id to add.
     */
    public void addApprovedLicenseId(final String licenseId) {
        licenseSetFactory.addLicenseId(licenseId);
    }

    /**
     * Adds a collection of license family categories to the set of approved license
     * names.
     * @param approvedLicenseIds set of approved license IDs.
     */
    public void addApprovedLicenseIds(final Collection<String> approvedLicenseIds) {
        approvedLicenseIds.forEach(this::addApprovedLicenseId);
    }

    /**
     * Adds a license family category to the list of approved licenses. <em>Once a
     * license has been removed from the approved list it cannot be re-added</em>
     * @param licenseId the license ID to add.
     */
    public void removeApprovedLicenseId(final String licenseId) {
        licenseSetFactory.removeLicenseId(licenseId);
    }

    /**
     * Removes a license family category from the list of approved licenses.
     * <em>Once a license has been removed from the approved list it cannot be
     * re-added</em>
     * @param licenseIds the license IDs to remove.
     */
    public void removeApprovedLicenseIds(final Collection<String> licenseIds) {
        licenseIds.forEach(this::removeApprovedLicenseId);
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
     * Gets the flag that determines if license headers are "forced" overwriting existing files.
     * This value is ignored if RAT is not adding licenses.
     * @return {@code true} if RAT is forcing the adding license headers.
     * @see #isAddingLicenses()
     */
    public boolean isAddingLicensesForced() {
        return addingLicensesForced;
    }

    /**
     * Gets the flag that determines if license headers should be added if missing.
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
        return licenseSetFactory.getLicenseFamilies(filter);
    }

    /**
     * Gets the ClaimValidator for the configuration.
     * @return the ClaimValidator.
     */
    public ClaimValidator getClaimValidator() {
        return claimValidator;
    }

    /**
     * Gets the enclosed LicenseSetFactory.
     * @return the license set factory.
     */
    public LicenseSetFactory getLicenseSetFactory() {
        return licenseSetFactory;
    }

    /**
     * Validates that the configuration is valid.
     * @param logger String consumer to log warning messages to.
     * @throws ConfigurationException on configuration error.
     */
    public void validate(final Consumer<String> logger) {
        if (!hasSource()) {
            String msg = "At least one source must be specified";
            logger.accept(msg);
            throw new ConfigurationException(msg);
        }
        if (licenseSetFactory.getLicenses(LicenseFilter.ALL).isEmpty()) {
            String msg = "You must specify at least one license";
            logger.accept(msg);
            throw new ConfigurationException(msg);
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
         * Does not actually close the delegate. But does perform a flush.
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
