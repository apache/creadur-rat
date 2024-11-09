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
package org.apache.rat.report.xml;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.rat.VersionInfo;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.api.RatException;
import org.apache.rat.license.ILicense;
import org.apache.rat.report.xml.writer.IXmlWriter;

/**
 * Creates the elements in the XML report.
 */
public class XmlElements {
    /**
     * The elements in the report.
     */
    public enum Elements {
        /** The start of the rat report */
        RAT_REPORT("rat-report"),
        /** The version of Rat being run */
        VERSION("version"),
        /** A resource element */
        RESOURCE("resource"),
        /** A license element */
        LICENSE("license"),
        /** A notes element */
        NOTES("notes"),
        /** A sample from the file */
        SAMPLE("sample"),
        /** A statistics element */
        STATISTICS("statistics"),
        /** A statistic entry */
        STATISTIC("statistic"),
        /** A license name entry */
        LICENSE_NAME("licenseName"),
        /** A license category entry */
        LICENSE_CATEGORY("licenseCategory"),
        /** A document type entry */
        DOCUMENT_TYPE("documentType");

        /** The XML name for the element */
        private String elementName;

        /**
         * Constructor.
         * @param elementName the XML name for the element.
         */
        Elements(final String elementName) {
            this.elementName = elementName;
        }

        /**
         * Gets the XML element name.
         * @return The XML element name.
         */
        public String getElementName() {
            return elementName;
        }
    };

    /**
     * The attributes of elements in the report.
     */
    public enum Attributes {
        /** A time stamp */
        TIMESTAMP,
        /** A vesion string */
        VERSION,
        /** The product identifier */
        PRODUCT,
        /** The vendor identifier */
        VENDOR,
        /** The approval flag */
        APPROVAL,
        /** The family category */
        FAMILY,
        /** The type */
        TYPE,
        /** The ID */
        ID,
        /** THe name */
        NAME,
        /** A counter */
        COUNT,
        /** A description */
        DESCRIPTION
    };

    /** The XMLWriter that we write to */
    private final IXmlWriter writer;

    /**
     * Constructor.
     * @param xmlWriter The writer to use.
     */
    public XmlElements(final IXmlWriter xmlWriter) {
        this.writer = xmlWriter;
    }

    /**
     * Create the rat report element.  Includes the timestamp and the version element.
     * @return this.
     * @throws RatException on error
     */
    public XmlElements ratReport() throws RatException {
        return write(Elements.RAT_REPORT).
                write(Attributes.TIMESTAMP, DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.format(Calendar.getInstance()))
                .version();
    }

    /**
     * Creates the version element with all version attributes populated.  Closes the version element.
     * @return this.
     * @throws RatException on error
     */
    public XmlElements version() throws RatException {
        VersionInfo versionInfo = new VersionInfo();
        return write(Elements.VERSION)
                .write(Attributes.PRODUCT, versionInfo.getTitle())
                .write(Attributes.VENDOR, versionInfo.getVendor())
                .write(Attributes.VERSION, versionInfo.getVersion())
                .closeElement();
    }

    /**
     * Creates a license element.  Closes the element before exit.
     * @param license the license for the element.
     * @param approved {@code true} if the license is approved.
     * @return this
     * @throws RatException on error.
     */
    public XmlElements license(final ILicense license, final boolean approved) throws RatException {
        write(Elements.LICENSE).write(Attributes.ID, license.getId())
                .write(Attributes.NAME, license.getName())
                .write(Attributes.APPROVAL, Boolean.valueOf(approved).toString())
                .write(Attributes.FAMILY, license.getLicenseFamily().getFamilyCategory());
        if (StringUtils.isNotBlank(license.getNote())) {
            try {
                write(Elements.NOTES).cdata(license.getNote()).closeElement();
            } catch (IOException e) {
                throw new RatException("Can not write cdata for 'notes' element", e);
            }
        }
        return closeElement();
    }

    /**
     * Writes a cdata block
     * @param data the data to write.
     * @return this
     * @throws IOException on error.
     */
    private XmlElements cdata(final String data) throws IOException {
        writer.cdata(data);
        return this;
    }

    /**
     * Creates a document element with attributes.  Does NOT close the document element.
     * @param document the document to write.
     * @return this
     * @throws RatException on error.
     */
    public XmlElements document(final Document document) throws RatException {
        final MetaData metaData = document.getMetaData();
        return write(Elements.RESOURCE)
                .write(Attributes.NAME, document.getName().localized("/"))
                .write(Attributes.TYPE, metaData.getDocumentType().toString());
    }

    /**
     * Creates a sample element.  Closes the sample element before returning.
     * @param sample the sample ot display.
     * @return this
     * @throws RatException on error.
     */
    public XmlElements sample(final String sample) throws RatException {
        try {
        return write(Elements.SAMPLE).cdata(sample).closeElement();
        } catch (IOException e) {
            throw new RatException("Can not write cdata for 'notes' element", e);
        }
    }

    /**
     * Creates a statistices element.
     * @return this
     * @throws RatException on error.
     */
    public XmlElements statistics() throws RatException {
        return write(Elements.STATISTICS);
    }

    /**
     * Creates a statistic element.  Closes the element before returning.
     * @param name the name of the statistics element.
     * @param count the count for the element.
     * @param description description of this statistic.
     * @param isOk if {@code} true the count is within limits.
     * @return this
     * @throws RatException on error.
     */
    public XmlElements statistic(final String name, final int count, String description, final boolean isOk) throws RatException {
        return write(Elements.STATISTIC)
                .write(Attributes.NAME, name)
                .write(Attributes.COUNT, Integer.toString(count))
                .write(Attributes.APPROVAL, Boolean.toString(isOk))
                .write(Attributes.DESCRIPTION, description)
                .closeElement();
    }

    /**
     * Creates a statistic element.  Closes the element before returning.
     * @param name the name of the statistics element.
     * @param count the count for the element.
     * @return this
     * @throws RatException on error.
     */
    public XmlElements licenseCategory(final String name, final int count) throws RatException {
        return write(Elements.LICENSE_CATEGORY)
                .write(Attributes.NAME, name)
                .write(Attributes.COUNT, Integer.toString(count))
                .closeElement();
    }

    /**
     * Creates a statistic element.  Closes the element before returning.
     * @param name the name of the statistics element.
     * @param count the count for the element.
     * @return this
     * @throws RatException on error.
     */
    public XmlElements licenseName(final String name, final int count) throws RatException {
        return write(Elements.LICENSE_NAME)
                .write(Attributes.NAME, name)
                .write(Attributes.COUNT, Integer.toString(count))
                .closeElement();
    }

    /**
     * Creates a statistic element.  Closes the element before returning.
     * @param name the name of the statistics element.
     * @param count the count for the element.
     * @return this
     * @throws RatException on error.
     */
    public XmlElements documentType(final String name, final int count) throws RatException {
        return write(Elements.DOCUMENT_TYPE)
                .write(Attributes.NAME, name)
                .write(Attributes.COUNT, Integer.toString(count))
                .closeElement();
    }

    /**
     * Closes the currently open element.
     * @return this
     * @throws RatException on error.
     */
    public XmlElements closeElement() throws RatException {
        try {
            writer.closeElement();
            return this;
        } catch (IOException e) {
            throw new RatException("Cannot open close element", e);
        }

    }

    /**
     * Write an element. The element is not closed.
     * @param element the element to write.
     * @return this
     * @throws RatException on error.
     */
    private XmlElements write(final Elements element) throws RatException {
        try {
            writer.openElement(element.getElementName());
            return this;
        } catch (IOException e) {
            throw new RatException("Cannot open start element: " + element.elementName, e);
        }
    }

    /**
     * Write an attribute.
     * @param attribute the attribute name.
     * @param value the attribute value.
     * @return this
     * @throws RatException on error.
     */
    public XmlElements write(final Attributes attribute, final String value) throws RatException {
        try {
            writer.attribute(attribute.name().toLowerCase(Locale.ROOT), value);
            return this;
        } catch (IOException e) {
            throw new RatException("Cannot open add attribute: " + attribute, e);
        }
    }
}
