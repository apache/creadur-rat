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
package org.apache.rat.analysis;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.rat.ConfigurationException;
import org.apache.rat.Defaults;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.api.Document;
import org.apache.rat.api.RatException;
import org.apache.rat.document.DocumentAnalyser;
import org.apache.rat.document.RatDocumentAnalysisException;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;
import org.apache.rat.walker.ArchiveWalker;

/**
 * Creates default analysers.
 */
public final class AnalyserFactory {

    private AnalyserFactory() {
        // do not instantiate
    }

    /**
     * Creates an analyser that adds the approved license predicate to the document metadata.
     * <p>
     *     Note you probably do not want this as it is automatically added to {@link #createConfiguredAnalyser}.
     * </p>
     * @param approvalPredicate the predicate to approve licenses.
     * @return A document analyser that sets the approvalPredicate in document metadata.
     */
    public static DocumentAnalyser createPolicy(final Predicate<ILicense> approvalPredicate) {
        return document -> {
            if (document != null) {
                document.getMetaData().setApprovalPredicate(approvalPredicate);
            }
        };
    }

    /**
     * Creates an analyser that calls each of the provided analysers in order.
     * @param analysers the array of analysers to call.
     * @return an analyser that will call all the provided analysers.
     */
    public static DocumentAnalyser createMultiplexer(final DocumentAnalyser... analysers) {
        return document -> {
            for (DocumentAnalyser analyser : analysers) {
                analyser.analyse(document);
            }
        };
    }

    /**
     * Creates a DocumentAnalyser from the report configuration.
     * @param configuration the ReportConfiguration
     * @return A document analyser that uses the provided licenses.
     */
    public static DocumentAnalyser createConfiguredAnalyser(final ReportConfiguration configuration) {
        LicenseSetFactory licenseSetFactory = configuration.getLicenseSetFactory();
        Set<ILicense> licenses = licenseSetFactory.getLicenses(LicenseSetFactory.LicenseFilter.ALL);
        if (licenses.isEmpty()) {
            throw new ConfigurationException("At least one license must be defined");
        }
        if (DefaultLog.getInstance().isEnabled(Log.Level.DEBUG)) {
            DefaultLog.getInstance().debug("Currently active licenses are:");
            licenses.forEach(DefaultLog.getInstance()::debug);
        }
        return createMultiplexer(createPolicy(licenseSetFactory.getApprovedLicensePredicate()),
         new DefaultAnalyser(configuration, licenses));
    }

    /**
     * A DocumentAnalyser a collection of licenses.
     */
    private static final class DefaultAnalyser implements DocumentAnalyser {

        /** The licenses to analyze */
        private final Collection<ILicense> licenses;
        /** the Report Configuration */
        private final ReportConfiguration configuration;
        /** The matcher for generated files */
        private final IHeaderMatcher generatedMatcher;

        /**
         * Constructs a DocumentAnalyser for the specified license.
         * @param config the ReportConfiguration
         * @param licenses The licenses to analyse
         */
        DefaultAnalyser(final ReportConfiguration config, final Collection<ILicense> licenses) {
            this.licenses = licenses;
            this.configuration = config;
            this.generatedMatcher = configuration.getGeneratedMatcher();
        }

        /**
         * Generates a predicate to filter out licenses that should not be reported.
         * @param proc the processing status to filter.
         * @return a Predicate to do the filtering.
         */
        private Predicate<ILicense> licenseFilter(final ReportConfiguration.Processing proc)  {
            return license -> {
                return switch (proc) {
                    case PRESENCE -> !license.getLicenseFamily().equals(UnknownLicense.INSTANCE.getLicenseFamily());
                    case ABSENCE -> true;
                    default -> false;
                };
            };
        }

        @Override
        public void analyse(final Document document) throws RatDocumentAnalysisException {
            Predicate<ILicense> licensePredicate;

            TikaProcessor.process(document);
            switch (document.getMetaData().getDocumentType()) {
                case STANDARD:
                    new DocumentHeaderAnalyser(generatedMatcher, licenses).analyse(document);
                    if (configuration.getStandardProcessing() != Defaults.STANDARD_PROCESSING) {
                        licensePredicate = licenseFilter(configuration.getStandardProcessing()).negate();
                        document.getMetaData().removeLicenses(licensePredicate);
                    }
                    break;
                case ARCHIVE:
                    if (configuration.getArchiveProcessing() != ReportConfiguration.Processing.NOTIFICATION) {
                        ArchiveWalker archiveWalker = new ArchiveWalker(document);
                        try {
                            licensePredicate = licenseFilter(configuration.getArchiveProcessing());
                            for (Document doc : archiveWalker.getDocuments()) {
                                analyse(doc);
                                doc.getMetaData().licenses().filter(licensePredicate).forEach(lic -> document.getMetaData().reportOnLicense(lic));
                            }
                        } catch (RatException e) {
                            throw new RatDocumentAnalysisException(e);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
