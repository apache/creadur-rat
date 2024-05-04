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

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.rat.ConfigurationException;
import org.apache.rat.Report;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.api.Document;
import org.apache.rat.api.RatException;
import org.apache.rat.document.IDocumentAnalyser;
import org.apache.rat.document.RatDocumentAnalysisException;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.utils.Log;
import org.apache.rat.walker.ArchiveWalker;

/**
 * Creates default analysers.
 */
public class DefaultAnalyserFactory {

    /**
     * Creates a DocumentAnalyser from a collection of ILicenses.
     * 
     * @param configuration the ReportConfiguration
     * @return A document analyser that uses the provides licenses.
     */
    public static IDocumentAnalyser createDefaultAnalyser(final ReportConfiguration configuration) {
        Set<ILicense> licenses = configuration.getLicenses(LicenseSetFactory.LicenseFilter.ALL);
        if (licenses.isEmpty()) {
            throw new ConfigurationException("At least one license must be defined");
        }
        configuration.getLog().debug("Licenses in Test");
        licenses.forEach(configuration.getLog()::debug);
        return new DefaultAnalyser(configuration, licenses);
    }

    /**
     * A DocumentAnalyser for the license
     */
    private final static class DefaultAnalyser implements IDocumentAnalyser {

        /** The licenses to analyze */
        private final Collection<ILicense> licenses;

        /** the Report Configuration */
        private final ReportConfiguration configuration;

        /**
         * Constructs a DocumentAnalyser for the specified license.
         * @param config the ReportConfiguration
         * @param licenses The licenses to analyse
         */
        public DefaultAnalyser(ReportConfiguration config, final Collection<ILicense> licenses) {
            this.licenses = licenses;
            this.configuration = config;
        }

        @Override
        public void analyse(Document document) throws RatDocumentAnalysisException {

            TikaProcessor.process(configuration.getLog(), document);

            switch (document.getMetaData().getDocumentType()) {
            case STANDARD:
                new DocumentHeaderAnalyser(configuration.getLog(), licenses).analyse(document);
                break;
            case ARCHIVE:
                if (configuration.getArchiveProcessing() != ReportConfiguration.Processing.NOTIFICATION) {
                    ArchiveWalker archiveWalker = new ArchiveWalker(new File(document.getName()), configuration.getFilesToIgnore());
                    Predicate<ILicense> filter = configuration.getArchiveProcessing() == ReportConfiguration.Processing.ABSENCE ?
                            l -> Boolean.TRUE : lic -> !lic.getLicenseFamily().equals(UnknownLicense.INSTANCE.getLicenseFamily());
                    try {
                        for (Document doc : archiveWalker.getDocuments()) {
                            analyse(doc);
                            doc.getMetaData().licenses().filter(filter).forEach(lic -> document.getMetaData().reportOnLicense(lic));
                        }
                    } catch (RatException e) {
                        throw new RatDocumentAnalysisException(e);
                    }
                }
                break;
            case NOTICE:
            case BINARY:
            case UNKNOWN:
            default:
                break;
            }
        }
    }
}
