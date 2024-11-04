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
    public enum Elements { RAT_REPORT("rat-report"), VERSION("version"),
        RESOURCE("resource"), LICENSE("license"), NOTES("notes"),
        SAMPLE("sample"), STATISTICS("statistics");

        private String elementName;

        Elements(String elementName) {
            this.elementName = elementName;
        }

        public String getElementName() {
            return elementName;
        }
    };

    public enum Attributes { TIMESTAMP, VERSION, PRODUCT, VENDOR,
        APPROVAL, FAMILY, NOTES, SAMPLE, TYPE, ID, NAME, COUNT };

    private final IXmlWriter writer;

    public XmlElements(IXmlWriter xmlWriter) {
        this.writer = xmlWriter;
    }

    public XmlElements ratReport() throws RatException {
        return write(Elements.RAT_REPORT).
                write(Attributes.TIMESTAMP, DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.format(Calendar.getInstance()))
                .version();
    }

    public XmlElements version() throws RatException {
        VersionInfo versionInfo = new VersionInfo();
        return write(Elements.VERSION)
                .write(Attributes.PRODUCT, versionInfo.getTitle())
                .write(Attributes.VENDOR, versionInfo.getVendor())
                .write(Attributes.VERSION, versionInfo.getVersion())
                .closeElement();

    }

    public XmlElements license(final ILicense license, boolean approved) throws RatException {
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

    private XmlElements cdata(String data) throws IOException {
        writer.cdata(data);
        return this;
    }

    public XmlElements document(final Document document) throws RatException {
        final MetaData metaData = document.getMetaData();
        return write(Elements.RESOURCE)
                .write(Attributes.NAME, document.getName().localized("/"))
                .write(Attributes.TYPE, metaData.getDocumentType().toString());
    }

    public XmlElements sample(String sample) throws RatException {
        try {
        return write(Elements.SAMPLE).cdata(sample).closeElement();
        } catch (IOException e) {
            throw new RatException("Can not write cdata for 'notes' element", e);
        }
    }

    public XmlElements statistics(String name, int count, boolean isOk) throws RatException {
        return write(Elements.STATISTICS)
                .write(Attributes.NAME, name)
                .write(Attributes.COUNT, Integer.toString(count))
                .write(Attributes.APPROVAL, Boolean.toString(isOk))
                .closeElement();
    }

    public XmlElements closeElement() throws RatException {
        try {
            writer.closeElement();
            return this;
        } catch (IOException e) {
            throw new RatException("Cannot open close element", e);
        }

    }

    private XmlElements write(Elements element) throws RatException {
        try {
            writer.openElement(element.getElementName());
            return this;
        } catch (IOException e) {
            throw new RatException("Cannot open start element: " + element.elementName, e);
        }
    }

    public XmlElements write(Attributes attribute, String value) throws RatException {
        try {
            writer.attribute(attribute.name().toLowerCase(Locale.ROOT), value);
            return this;
        } catch (IOException e) {
            throw new RatException("Cannot open add attribute: " + attribute, e);
        }
    }
}
