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
package org.apache.rat.report;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;

import org.apache.rat.ReportConfiguration;
import org.apache.rat.api.RatException;
import org.apache.rat.config.parameters.Component.Description;
import org.apache.rat.config.parameters.Component.Type;
import org.apache.rat.configuration.MatcherBuilderTracker;
import org.apache.rat.configuration.XMLConfigurationReader;
import org.apache.rat.configuration.builders.MatcherRefBuilder;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.report.xml.writer.IXmlWriter;

public class ConfigurationReport extends AbstractReport {

    private final ReportConfiguration configuration;
    private final IXmlWriter writer;
    private final Set<String> matchers;

    public ConfigurationReport(IXmlWriter writer, ReportConfiguration configuration) {
        this.configuration = configuration;
        this.writer = writer;
        this.matchers = new HashSet<>();
    }

    @Override
    public void startReport() throws RatException {
        if (configuration.listFamilies() != LicenseFilter.none || configuration.listLicenses() != LicenseFilter.none) {
            try {
                writer.openElement(XMLConfigurationReader.ROOT);

                // write Families section
                SortedSet<ILicenseFamily> families = configuration.getLicenseFamilies(configuration.listFamilies());
                if (!families.isEmpty()) {
                    writer.openElement(XMLConfigurationReader.FAMILIES);
                    for (ILicenseFamily family : families) {
                        writeFamily(family);
                    }
                    writer.closeElement(); // FAMILIES
                }

                // write licenses section
                SortedSet<ILicense> licenses = configuration.getLicenses(configuration.listLicenses());
                if (!licenses.isEmpty()) {
                    writer.openElement(XMLConfigurationReader.LICENSES);
                    for (ILicense license : licenses) {
                        writeDescription(license.getDescription());
                    }
                    writer.closeElement();// LICENSES
                }

                // write approved section
                writer.openElement(XMLConfigurationReader.APPROVED);
                for (String family : configuration.getApprovedLicenseCategories()) {
                    writer.openElement(XMLConfigurationReader.APPROVED)
                            .attribute(XMLConfigurationReader.ATT_LICENSE_REF, family.trim()).closeElement();
                }
                writer.closeElement(); // APPROVED

                // write matchers section
                MatcherBuilderTracker tracker = MatcherBuilderTracker.INSTANCE;
                writer.openElement(XMLConfigurationReader.MATCHERS);
                for (Class<?> clazz : tracker.getClasses()) {
                    writer.openElement(XMLConfigurationReader.MATCHER)
                            .attribute(XMLConfigurationReader.ATT_CLASS_NAME, clazz.getCanonicalName()).closeElement();
                }
                writer.closeElement(); // MATCHERS

                writer.closeElement(); // ROOT
            } catch (IOException e) {
                throw new RatException(e);
            }
        }
    }

    private void writeFamily(ILicenseFamily family) throws RatException {
        try {
            writer.openElement(XMLConfigurationReader.FAMILY)
                    .attribute(XMLConfigurationReader.ATT_ID, family.getFamilyCategory().trim())
                    .attribute(XMLConfigurationReader.ATT_NAME, family.getFamilyName());
            writer.closeElement();
        } catch (IOException e) {
            throw new RatException(e);
        }
    }

    private void writeDescriptions(Collection<Description> descriptions) throws RatException {
        for (Description description : descriptions) {
            writeDescription(description);
        }
    }

    private void writeDescription(Description description) throws RatException {
        if (description == null) {
            return;
        }
        try {
            switch (description.getType()) {
            case Matcher:
                // see if id was registered
                Optional<Description> id = description.getChildren().stream().filter(
                        i -> i.getType() == Type.Parameter && XMLConfigurationReader.ATT_ID.equals(i.getCommonName()))
                        .findFirst();
                if (id.isPresent()) {
                    String idStr = id.get().getParamValue();
                    if (matchers.contains(idStr)) {
                        writer.openElement("matcherRef").attribute(MatcherRefBuilder.ATT_REF_ID, idStr).closeElement();
                        return;
                    }
                    matchers.add(idStr);
                }
                writer.openElement(description.getCommonName());
                writeDescriptions(description.getChildren());
                writer.closeElement();
                break;
            case License:
                writer.openElement(XMLConfigurationReader.LICENSE);
                writeDescriptions(description.getChildren());
                writer.closeElement();
                break;
            case Parameter:
                if ("id".equals(description.getCommonName())) {
                    try {
                        // if a UUID skip it.
                        UUID.fromString(description.getParamValue());
                        return;
                    } catch (IllegalArgumentException expected) {
                        // do nothing.
                    }
                }
                String paramValue = description.getParamValue();
                if (paramValue != null) {
                    writer.attribute(description.getCommonName(), paramValue);
                }
                break;
            case Text:
                writer.content(description.getParamValue());
                break;
            }
        } catch (IOException e) {
            throw new RatException(e);
        }
    }

}
