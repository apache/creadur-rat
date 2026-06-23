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
import org.apache.rat.report.xml.writer.XmlWriter;

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
                return switch (parent.getType()) {
                    case MATCHER -> !XMLConfig.isInlineNode(parent.getCommonName(), d.getCommonName());
                    case LICENSE -> !licenseChildren.contains(d.getCommonName());
                    default -> true;
                };
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

    private void writeFamilies(final XmlWriter writer, final SortedSet<ILicenseFamily> families) throws IOException, RatException {
        if (!families.isEmpty()) {
            writer.startElement(XMLConfig.FAMILIES);
            for (ILicenseFamily family : families) {
                writeFamily(writer, family);
            }
            writer.closeElement(); // FAMILIES
        }
    }

    private void writeLicenses(final XmlWriter writer, final SortedSet<ILicense> licenses) throws IOException, RatException {
        if (!licenses.isEmpty()) {
            writer.startElement(XMLConfig.LICENSES);
            for (ILicense license : licenses) {
                writeDescription(writer, license.getDescription(), license);
            }
            writer.closeElement(); // LICENSES
        }
    }

    private void writeApproved(final XmlWriter writer) throws IOException {
        writer.startElement(XMLConfig.APPROVED);
        for (String family : configuration.getLicenseCategories(LicenseFilter.APPROVED)) {
            writer.startElement(XMLConfig.APPROVED).attribute(XMLConfig.ATT_LICENSE_REF, family.trim())
                    .closeElement();
        }
        writer.closeElement(); // APPROVED
    }

    private void writeMatchers(final XmlWriter writer) throws IOException {
        MatcherBuilderTracker tracker = MatcherBuilderTracker.instance();
        writer.startElement(XMLConfig.MATCHERS);
        for (Class<?> clazz : tracker.getClasses()) {
            writer.startElement(XMLConfig.MATCHER).attribute(XMLConfig.ATT_CLASS_NAME, clazz.getCanonicalName())
                    .closeElement();
        }
        writer.closeElement(); // MATCHERS
    }

    /**
     * Writes the configuration to an IXmlWriter instance.
     * @param writer the IXmlWriter to write to.
     * @throws RatException on error.
     */
    public void write(final XmlWriter writer) throws RatException {
        if (configuration.listFamilies() != LicenseFilter.NONE || configuration.listLicenses() != LicenseFilter.NONE) {
            try {
                writer.startElement(XMLConfig.ROOT);

                // Families section
                writeFamilies(writer, configuration.getLicenseFamilies(configuration.listFamilies()));

                // licenses section
                writeLicenses(writer, configuration.getLicenses(configuration.listLicenses()));

                // approved section
                writeApproved(writer);

                // matchers section
                writeMatchers(writer);

                writer.closeElement(); // ROOT
            } catch (IOException e) {
                throw new RatException(e);
            }
        }
    }

    private void writeFamily(final XmlWriter writer, final ILicenseFamily family) throws RatException {
        try {
            writer.startElement(XMLConfig.FAMILY).attribute(XMLConfig.ATT_ID, family.getFamilyCategory().trim())
                    .attribute(XMLConfig.ATT_NAME, family.getFamilyName());
            writer.closeElement();
        } catch (IOException e) {
            throw new RatException(e);
        }
    }

    private void writeDescriptions(final XmlWriter writer, final Collection<Description> descriptions, final IHeaderMatcher component)
            throws RatException {
        for (Description description : descriptions) {
            writeDescription(writer, description, component);
        }
    }

    private void writeChildren(final XmlWriter writer, final Description description, final IHeaderMatcher component)
            throws RatException {
        writeAttributes(writer, description.filterChildren(attributeFilter(component.getDescription())), component);
        writeDescriptions(writer, description.filterChildren(attributeFilter(component.getDescription()).negate()),
                component);
    }

    private void writeAttributes(final XmlWriter writer, final Collection<Description> descriptions, final IHeaderMatcher component)
            throws RatException {
        for (Description d : descriptions) {
            try {
                writeAttribute(writer, d, component);
            } catch (IOException e) {
                throw new RatException(e);
            }
        }
    }

    private void writeComment(final XmlWriter writer, final Description description) throws IOException {
        if (StringUtils.isNotBlank(description.getDescription())) {
            writer.comment(description.getDescription().replace("-->", "-&ndash;>"));
        }
    }

    private void writeAttribute(final XmlWriter writer, final Description description, final IHeaderMatcher component)
            throws IOException {
        String paramValue = description.getParamValue(component);
        if (paramValue != null) {
            writer.attribute(description.getCommonName(), paramValue);
        }
    }

    private boolean hasUUIDId(final Description desc, final IHeaderMatcher comp) {
        if ("id".equals(desc.getCommonName())) {
            try {
                String paramId = desc.getParamValue(comp);
                // if a UUID skip it.
                if (paramId != null) {
                    UUID.fromString(paramId);
                    return true;
                }
            } catch (IllegalArgumentException expected) {
                // do nothing.
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private void writeParameterDescription(final XmlWriter writer, final Description description, final IHeaderMatcher component) throws IOException {
        if (hasUUIDId(description, component)) {
            return;
        }

        if (description.getChildType() == String.class) {
            boolean inline = XMLConfig.isInlineNode(component.getDescription().getCommonName(),
                    description.getCommonName());
            String s = description.getParamValue(component);
            if (StringUtils.isNotBlank(s)) {
                if (!inline) {
                    writer.startElement(description.getCommonName());
                }
                writer.content(description.getParamValue(component));
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
    }

    private void writeMatcherDescription(final XmlWriter writer, final Description desc, final IHeaderMatcher comp) throws IOException, RatException {
        Description description = desc;
        IHeaderMatcher component = comp;
        // see if id was registered
        Optional<Description> id = description.childrenOfType(ComponentType.PARAMETER).stream()
                .filter(i -> XMLConfig.ATT_ID.equals(i.getCommonName())).findFirst();

        // id will not be present in matcherRef
        if (id.isPresent()) {
            String matcherId = id.get().getParamValue(component);
            // if we have seen the ID before, put a reference to the other one.
            if (matchers.contains(matcherId)) {
                component = new MatcherRefBuilder.IHeaderMatcherProxy(matcherId, null);
                description = component.getDescription();
            } else {
                matchers.add(matcherId);
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

        // if only a resource, list the resource not the contents of the matcher
        Optional<Description> resource = description.childrenOfType(ComponentType.PARAMETER).stream()
                .filter(i -> XMLConfig.ATT_RESOURCE.equals(i.getCommonName())).findFirst();
        if (resource.isPresent()) {
            String resourceStr = resource.get().getParamValue(component);
            if (StringUtils.isNotBlank(resourceStr)) {
                description.getChildren().remove("enclosed");
            }
        }
        writeComment(writer, description);
        writer.startElement(description.getCommonName());
        writeChildren(writer, description, component);
        writer.closeElement();
    }

    /* package private for testing */
    void writeDescription(final XmlWriter writer, final Description description, final IHeaderMatcher component) throws RatException {
        try {
            switch (description.getType()) {
            case MATCHER:
                writeMatcherDescription(writer, description, component);
                break;
            case LICENSE:
                writer.startElement(XMLConfig.LICENSE);
                writeChildren(writer, description, component);
                writer.closeElement();
                break;
            case PARAMETER:
                writeParameterDescription(writer, description, component);
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
