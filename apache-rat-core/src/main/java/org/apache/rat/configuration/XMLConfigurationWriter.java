/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.configuration;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.ImplementationException;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.api.RatException;
import org.apache.rat.config.parameters.ComponentType;
import org.apache.rat.config.parameters.Description;
import org.apache.rat.configuration.builders.MatcherRefBuilder;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.report.xml.writer.IXmlWriter;
import org.apache.rat.report.xml.writer.impl.base.XmlWriter;

/**
 * Writes the XML configuration file format.
 */
public class XMLConfigurationWriter {
    /** The configuration that is being written */
    private final ReportConfiguration configuration;
    /** The set of defined matcher IDs */
    private final Set<String> matchers;
    /** The set of defined license IDs */
    private final Set<String> licenseChildren;

    /**
     * Constructor
     * @param configuration the configuration information to write.
     */
    public XMLConfigurationWriter(final ReportConfiguration configuration) {
        this.configuration = configuration;
        this.matchers = new HashSet<>();
        licenseChildren = new HashSet<>(Arrays.asList(XMLConfig.LICENSE_CHILDREN));
    }

    private Predicate<Description> attributeFilter(final Description parent) {
        return d -> {
            if (d.getType() == ComponentType.PARAMETER) {
                switch (parent.getType()) {
                case MATCHER:
                    return !XMLConfig.isInlineNode(parent.getCommonName(), d.getCommonName());
                case LICENSE:
                    return !licenseChildren.contains(d.getCommonName());
                default:
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Writes the configuration to the specified writer.
     * @param plainWriter a writer to write the XML to.
     * @throws RatException on error.
     */
    public void write(final Writer plainWriter) throws RatException {
        write(new XmlWriter(plainWriter));
    }

    /**
     * Writes the configuration to an IXmlWriter instance.
     * @param writer the IXmlWriter to write to.
     * @throws RatException on error.
     */
    public void write(final IXmlWriter writer) throws RatException {
        if (configuration.listFamilies() != LicenseFilter.NONE || configuration.listLicenses() != LicenseFilter.NONE) {
            try {
                writer.openElement(XMLConfig.ROOT);

                // write Families section
                SortedSet<ILicenseFamily> families = configuration.getLicenseFamilies(configuration.listFamilies());
                if (!families.isEmpty()) {
                    writer.openElement(XMLConfig.FAMILIES);
                    for (ILicenseFamily family : families) {
                        writeFamily(writer, family);
                    }
                    writer.closeElement(); // FAMILIES
                }

                // write licenses section
                SortedSet<ILicense> licenses = configuration.getLicenses(configuration.listLicenses());
                if (!licenses.isEmpty()) {
                    writer.openElement(XMLConfig.LICENSES);
                    for (ILicense license : licenses) {
                        writeDescription(writer, license.getDescription(), license);
                    }
                    writer.closeElement(); // LICENSES
                }

                // write approved section
                writer.openElement(XMLConfig.APPROVED);
                for (String family : configuration.getLicenseCategories(LicenseFilter.APPROVED)) {
                    writer.openElement(XMLConfig.APPROVED).attribute(XMLConfig.ATT_LICENSE_REF, family.trim())
                            .closeElement();
                }
                writer.closeElement(); // APPROVED

                // write matchers section
                MatcherBuilderTracker tracker = MatcherBuilderTracker.instance();
                writer.openElement(XMLConfig.MATCHERS);
                for (Class<?> clazz : tracker.getClasses()) {
                    writer.openElement(XMLConfig.MATCHER).attribute(XMLConfig.ATT_CLASS_NAME, clazz.getCanonicalName())
                            .closeElement();
                }
                writer.closeElement(); // MATCHERS

                writer.closeElement(); // ROOT
            } catch (IOException e) {
                throw new RatException(e);
            }
        }
    }

    private void writeFamily(final IXmlWriter writer, final ILicenseFamily family) throws RatException {
        try {
            writer.openElement(XMLConfig.FAMILY).attribute(XMLConfig.ATT_ID, family.getFamilyCategory().trim())
                    .attribute(XMLConfig.ATT_NAME, family.getFamilyName());
            writer.closeElement();
        } catch (IOException e) {
            throw new RatException(e);
        }
    }

    private void writeDescriptions(final IXmlWriter writer, final Collection<Description> descriptions, final IHeaderMatcher component)
            throws RatException {
        for (Description description : descriptions) {
            writeDescription(writer, description, component);
        }
    }

    private void writeChildren(final IXmlWriter writer, final Description description, final IHeaderMatcher component)
            throws RatException {
        writeAttributes(writer, description.filterChildren(attributeFilter(component.getDescription())), component);
        writeDescriptions(writer, description.filterChildren(attributeFilter(component.getDescription()).negate()),
                component);
    }

    private void writeAttributes(final IXmlWriter writer, final Collection<Description> descriptions, final IHeaderMatcher component)
            throws RatException {
        for (Description d : descriptions) {
            try {
                writeAttribute(writer, d, component);
            } catch (IOException e) {
                throw new RatException(e);
            }
        }
    }

    private void writeComment(final IXmlWriter writer, final Description description) throws IOException {
        if (StringUtils.isNotBlank(description.getDescription())) {
            writer.comment(description.getDescription());
        }
    }

    private void writeAttribute(final IXmlWriter writer, final Description description, final IHeaderMatcher component)
            throws IOException {
        String paramValue = description.getParamValue(configuration.getLog(), component);
        if (paramValue != null) {
            writer.attribute(description.getCommonName(), paramValue);
        }
    }

    /* package private for testing */
    @SuppressWarnings("unchecked")
    void writeDescription(final IXmlWriter writer, final Description desc, final IHeaderMatcher comp) throws RatException {
        Description description = desc;
        IHeaderMatcher component = comp;
        try {
            switch (description.getType()) {
            case MATCHER:
                // see if id was registered
                Optional<Description> id = description.childrenOfType(ComponentType.PARAMETER).stream()
                        .filter(i -> XMLConfig.ATT_ID.equals(i.getCommonName())).findFirst();

                // id will not be present in matcherRef
                if (id.isPresent()) {
                    String matcherId = id.get().getParamValue(configuration.getLog(), component);
                    // if we have seen the ID before just put a reference to the other one.
                    if (matchers.contains(matcherId.toString())) {
                        component = new MatcherRefBuilder.IHeaderMatcherProxy(matcherId.toString(), null);
                        description = component.getDescription();
                    } else {
                        matchers.add(matcherId.toString());
                    }
                    // remove the matcher id if it is a UUID
                    try {
                        UUID.fromString(matcherId);
                        description.getChildren().remove(XMLConfig.ATT_ID);
                    } catch (IllegalArgumentException expected) {
                        if (description.getCommonName().equals("spdx")) {
                            description.getChildren().remove(XMLConfig.ATT_ID);
                        }
                    }
                }

                // if resource only list the resource not the contents of the matcher
                Optional<Description> resource = description.childrenOfType(ComponentType.PARAMETER).stream()
                        .filter(i -> XMLConfig.ATT_RESOURCE.equals(i.getCommonName())).findFirst();
                if (resource.isPresent()) {
                    String resourceStr = resource.get().getParamValue(configuration.getLog(), component);
                    if (StringUtils.isNotBlank(resourceStr)) {
                        description.getChildren().remove("enclosed");
                    }
                }
                writeComment(writer, description);
                writer.openElement(description.getCommonName());
                writeChildren(writer, description, component);
                writer.closeElement();
                break;
            case LICENSE:
                writer.openElement(XMLConfig.LICENSE);
                writeChildren(writer, description, component);
                writer.closeElement();
                break;
            case PARAMETER:
                if ("id".equals(description.getCommonName())) {
                    try {
                        String paramId = description.getParamValue(configuration.getLog(), component);
                        // if a UUID skip it.
                        if (paramId != null) {
                            UUID.fromString(paramId.toString());
                            return;
                        }
                    } catch (IllegalArgumentException expected) {
                        // do nothing.
                    }
                }
                if (description.getChildType() == String.class) {

                    boolean inline = XMLConfig.isInlineNode(component.getDescription().getCommonName(),
                            description.getCommonName());
                    String s = description.getParamValue(configuration.getLog(), component);
                    if (StringUtils.isNotBlank(s)) {
                        if (!inline) {
                            writer.openElement(description.getCommonName());
                        }
                        writer.content(description.getParamValue(configuration.getLog(), component));
                        if (!inline) {
                            writer.closeElement();
                        }
                    }
                } else {
                    try {
                        if (description.isCollection()) {
                            for (IHeaderMatcher matcher : (Collection<IHeaderMatcher>) description
                                    .getter(component.getClass()).invoke(component)) {
                                writeDescription(writer, matcher.getDescription(), matcher);
                            }
                        } else {
                            IHeaderMatcher matcher = (IHeaderMatcher) description.getter(component.getClass())
                                    .invoke(component);
                            writeDescription(writer, matcher.getDescription(), matcher);
                        }
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                            | NoSuchMethodException | SecurityException | RatException e) {
                        throw new ImplementationException(e);
                    }
                }
                break;
            case BUILD_PARAMETER:
                // ignore;
                break;
            }
        } catch (IOException e) {
            throw new RatException(e);
        }
    }
}
