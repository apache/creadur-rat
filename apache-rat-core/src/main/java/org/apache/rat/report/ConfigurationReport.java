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
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.api.RatException;
import org.apache.rat.config.parameters.Component;
import org.apache.rat.config.parameters.Component.Type;
import org.apache.rat.config.parameters.Description;
import org.apache.rat.config.parameters.DescriptionBuilder;
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
                        writeDescription(license.getDescription(), license);
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

    private void writeDescriptions(Collection<Description> descriptions, Component component) throws RatException {
        for (Description description : descriptions) {
            writeDescription(description, component);
        }
    }

    private void writeChildren(Description description, Component component) throws RatException {
        writeDescriptions(description.childrenOfType(Type.Parameter), component);
        writeDescriptions(description.childrenOfType(Type.Unlabeled), component);
        writeDescriptions(description.childrenOfType(Type.Matcher), component);
        writeDescriptions(description.childrenOfType(Type.License), component);
    }

    private void writeComment(Description description) throws IOException {
        if (StringUtils.isNotBlank(description.getDescription())) {
            writer.comment(description.getDescription());
        }
    }

    private void writeContent(Description description, Component component) throws IOException {
        String paramValue = description.getParamValue(component);
        if (paramValue != null) {
            writer.content(paramValue);
        }
    }

    private void writeAttribute(Description description, Component component) throws IOException {
        String paramValue = description.getParamValue(component);
        if (paramValue != null) {
            writer.attribute(description.getCommonName(), paramValue);
        }
    }

    /* package privete for testing */
    void writeDescription(Description description, Component component) throws RatException {
        try {
            switch (description.getType()) {
            case Matcher:
                // see if id was registered
                Optional<Description> id = description.childrenOfType(Type.Parameter).stream()
                        .filter(i -> XMLConfigurationReader.ATT_ID.equals(i.getCommonName())).findFirst();
                if (id.isPresent()) {
                    String idValue = id.get().getParamValue(component);
                    // if we have seen the ID before just put a reference to the other one.
                    if (matchers.contains(idValue.toString())) {
                        component = new MatcherRefBuilder.IHeaderMatcherProxy(idValue.toString(), null);
                        description = component.getDescription();
                    } else {
                        matchers.add(idValue.toString());
                    }
                }
                Optional<Description> resource = description.childrenOfType(Type.Parameter).stream()
                        .filter(i -> "resource".equals(i.getCommonName())).findFirst();
                if (resource.isPresent()) {
                    Iterator<Map.Entry<String, Description>> iter = description.getChildren().entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<String, Description> entry = iter.next();
                        if (entry.getValue().isCollection()
                                && IHeaderMatcher.class.isAssignableFrom(entry.getValue().getChildType())) {
                            iter.remove();
                        }
                    }
                }
                writeComment(description);
                writer.openElement(description.getCommonName());
                writeChildren(description, component);
                writer.closeElement();
                break;
            case License:
                writer.openElement(XMLConfigurationReader.LICENSE);
                Description notes = null;
                for (Description desc : description.childrenOfType(Component.Type.Parameter)) {
                    if ("notes".equals(desc.getCommonName())) {
                        notes = desc;
                    } else {
                        writeAttribute(desc, component);
                    }
                }
                for (Description desc : description.childrenOfType(Component.Type.BuilderParam)) {
                    if ("family".equals(desc.getCommonName())) {
                        try {
                            ILicenseFamily family = (ILicenseFamily) desc.getter(component.getClass())
                                    .invoke(component);
                            if (family != null) {
                                writer.attribute("family", family.getFamilyCategory().trim());
                            }
                        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException
                                | InvocationTargetException | SecurityException e) {
                            configuration.getLog().error(e.toString());
                        }
                    }
                }
                // end of attributes
                if (notes != null && StringUtils.isNotBlank(notes.getParamValue(component))) {
                    writeComment(notes);
                    writer.openElement(notes.getCommonName());
                    writeContent(notes, component);
                    writer.closeElement();
                }
                for (Description desc : description.childrenOfType(Component.Type.Unlabeled)) {
                    writeDescription(desc, component);
                }
                writer.closeElement();
                break;
            case Parameter:
                if ("id".equals(description.getCommonName())) {
                    try {
                        String idValue = description.getParamValue(component);
                        // if a UUID skip it.
                        if (idValue != null) {
                            UUID.fromString(idValue.toString());
                            return;
                        }
                    } catch (IllegalArgumentException expected) {
                        // do nothing.
                    }
                }
                writeAttribute(description, component);
                break;
            case Unlabeled:
                try {
                    Object obj = description.getter(component.getClass()).invoke(component);
                    if (obj instanceof Iterable) {
                        for (Object o2 : (Iterable<?>) obj) {
                            processUnlabled(o2);
                        }
                    } else {
                        processUnlabled(obj);
                    }
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | NoSuchMethodException | SecurityException | RatException e) {
                    configuration.getLog().error(e.getMessage());
                }
                break;
            case BuilderParam:
                // ignore;
                break;
            }
        } catch (IOException e) {
            throw new RatException(e);
        }
    }

    private void processUnlabled(Object obj) throws RatException, IOException {
        if (obj instanceof Component) {
            Description d = DescriptionBuilder.build(obj);
            if (d != null) {
                writeDescription(d, (Component) obj);
            }
        } else {
            if (obj != null) {
                writer.content(obj.toString());
            }
        }
    }

}
