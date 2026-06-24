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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.rat.VersionInfo;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.api.RatException;
import org.apache.rat.config.results.ClaimValidator;
import org.apache.rat.license.ILicense;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.report.xml.writer.XmlWriter;
import org.apache.rat.utils.CasedString;

import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

/**
 * Creates the elements in the XML report.
 */
public final class XmlElements {
    private XmlElements() {
        // do not instantiate
    }

    /**
     * Converts an enum name to snake case.
     *
     * @param name the attribute to normalize
     * @return a pascal cased name.
     */
    private static String normalizeName(final String name) {
        CasedString casedName = new CasedString(CasedString.StringCase.SNAKE, name.toLowerCase(Locale.ROOT));
        return casedName.toCase(CasedString.StringCase.PASCAL);
    }

    /**
     * Create the RAT report element. Includes the timestamp and the version element.
     * Does not close the report.
     *
     * @throws RatException on error
     */
    public static void ratReport(final XmlWriter writer) throws RatException {
        try {
            writer.startElement(Elements.RAT_REPORT.elementName)
                    .attribute(Attributes.TIMESTAMP.attributeName(),
                            DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.format(Calendar.getInstance()));
            version(writer);
        } catch (IOException e) {
            throw new RatException(e);
        }
    }

    /**
     * Creates the version element with all version attributes populated. Closes the version element.
     *
     * @throws RatException on error
     */
    public static void version(final XmlWriter writer) throws RatException {
        VersionInfo versionInfo = new VersionInfo();
        try {
            writer.startElement(Elements.VERSION.elementName)
                    .attribute(Attributes.PRODUCT.attributeName(), versionInfo.getTitle())
                    .attribute(Attributes.VENDOR.attributeName(), versionInfo.getVendor())
                    .attribute(Attributes.VERSION.attributeName(), versionInfo.getVersion())
                    .closeElement();
        } catch (IOException e) {
            throw new RatException(e);
        }
    }

    /**
     * Creates a license element. Closes the element before exit.
     *
     * @param license  the license for the element.
     * @param approved {@code true} if the license is approved.
     * @throws RatException on error.
     */
    public static void license(final XmlWriter writer, final ILicense license, final boolean approved) throws RatException {
        try {
            writer.startElement(Elements.LICENSE.elementName)
                    .attribute(Attributes.ID.attributeName(), license.getId())
                    .attribute(Attributes.NAME.attributeName(), license.getName())
                    .attribute(Attributes.APPROVAL.attributeName(), Boolean.toString(approved))
                    .attribute(Attributes.FAMILY.attributeName(), license.getLicenseFamily().getFamilyCategory());

            if (StringUtils.isNotBlank(license.getNote())) {
                writer.startElement(Elements.NOTES.elementName).cdata(license.getNote()).closeElement();
            }
            writer.closeElement();
        } catch (IOException e) {
            throw new RatException(e);
        }
    }

    /**
     * Creates a document element with attributes. Does <strong>NOT</strong> close the document element.
     *
     * @param document the document to write.
     * @throws RatException on error.
     */
    public static void document(final XmlWriter writer, final Document document) throws RatException {
        final MetaData metaData = document.getMetaData();
        try {
            writer.startElement(Elements.RESOURCE.elementName)
                    .attribute(Attributes.NAME.attributeName(), document.getName().localized("/"))
                    .attribute(Attributes.TYPE.attributeName(), metaData.getDocumentType().toString())
                    .attribute(Attributes.MEDIA_TYPE.attributeName(), metaData.getMediaType().toString());
            if (Document.Type.STANDARD == metaData.getDocumentType() || metaData.hasCharset()) {
                writer.attribute(Attributes.ENCODING.attributeName(), metaData.getCharset().displayName());
            }
            if (document.isIgnored()) {
                writer.attribute(Attributes.IS_DIRECTORY.attributeName(), Boolean.toString(document.isDirectory()));
            }
        } catch (IOException e) {
            throw new RatException(e);
        }
    }

    /**
     * Creates a statistics element.
     *
     * @throws RatException on error.
     */
    public static void statistics(final XmlWriter writer, final ClaimStatistic statistic, final ClaimValidator validator) throws RatException {
        try {
            writer.startElement(Elements.STATISTICS.elementName);
            for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
                int count = statistic.getCounter(counter);
                XmlElements.statistic(writer, counter.displayName(), count, counter.getDescription(), validator.isValid(counter, count));
            }
            for (String category : statistic.getLicenseFamilyCategories()) {
                XmlElements.licenseCategory(writer, category, statistic.getLicenseCategoryCount(category));
            }
            for (String category : statistic.getLicenseNames()) {
                XmlElements.licenseName(writer, category, statistic.getLicenseNameCount(category));
            }
            for (Document.Type type : statistic.getDocumentTypes()) {
                XmlElements.documentType(writer, type.name(), statistic.getCounter(type));
            }

            writer.closeElement();
        } catch (IOException e) {
            throw new RatException(e);
        }
    }

    /**
     * Creates a statistic element. Closes the element before returning.
     *
     * @param name        the name of the statistics element.
     * @param count       the count for the element.
     * @param description description of this statistic.
     * @param isOk        if {@code true} the count is within limits.
     * @throws RatException on error.
     */
    public static void statistic(final XmlWriter writer, final String name, final int count, final String description, final boolean isOk) throws RatException {
        try {
            writer.startElement(Elements.STATISTIC.elementName)
                    .attribute(Attributes.NAME.attributeName(), name)
                    .attribute(Attributes.COUNT.attributeName(), Integer.toString(count))
                    .attribute(Attributes.APPROVAL.attributeName(), Boolean.toString(isOk))
                    .attribute(Attributes.DESCRIPTION.attributeName(), description)
                    .closeElement();
        } catch (IOException e) {
            throw new RatException(e);
        }
    }

    /**
     * Creates a statistic element. Closes the element before returning.
     *
     * @param name  the name of the statistics element.
     * @param count the count for the element.
     * @throws RatException on error.
     */
    public static void licenseCategory(final XmlWriter writer, final String name, final int count) throws RatException {
        try {
            writer.startElement(Elements.LICENSE_CATEGORY.elementName)
                    .attribute(Attributes.NAME.attributeName(), name)
                    .attribute(Attributes.COUNT.attributeName(), Integer.toString(count))
                    .closeElement();
        } catch (IOException e) {
            throw new RatException(e);
        }
    }

    /**
     * Creates a statistic element. Closes the element before returning.
     *
     * @param name  the name of the statistics element.
     * @param count the count for the element.
     * @throws RatException on error.
     */
    public static void licenseName(final XmlWriter writer, final String name, final int count) throws RatException {
        try {
            writer.startElement(Elements.LICENSE_NAME.elementName)
                    .attribute(Attributes.NAME.attributeName(), name)
                    .attribute(Attributes.COUNT.attributeName(), Integer.toString(count))
                    .closeElement();
        } catch (IOException e) {
            throw new RatException(e);
        }
    }

    /**
     * Creates a statistic element. Closes the element before returning.
     *
     * @param name  the name of the statistics element.
     * @param count the count for the element.
     * @throws RatException on error.
     */
    public static void documentType(final XmlWriter writer, final String name, final int count) throws RatException {
        try {
            writer.startElement(Elements.DOCUMENT_TYPE.elementName)
                    .attribute(Attributes.NAME.attributeName(), name)
                    .attribute(Attributes.COUNT.attributeName(), Integer.toString(count))
                    .closeElement();
        } catch (IOException e) {
            throw new RatException(e);
        }
    }

    /**
     * The elements in the report.
     */
    public enum Elements {
        /**
         * The start of the RAT report.
         */
        RAT_REPORT("rat-report"),
        /**
         * The version of RAT being run.
         */
        VERSION(),
        /**
         * A resource element.
         */
        RESOURCE(),
        /**
         * A license element.
         */
        LICENSE(),
        /**
         * A notes element.
         */
        NOTES(),
        /**
         * A statistics element.
         */
        STATISTICS(),
        /**
         * A statistic entry.
         */
        STATISTIC(),
        /**
         * A license name entry.
         */
        LICENSE_NAME(),
        /**
         * A license category entry.
         */
        LICENSE_CATEGORY(),
        /**
         * A document type entry.
         */
        DOCUMENT_TYPE();

        /**
         * The XML name for the element
         */
        private final String elementName;

        /**
         * Constructor.
         *
         * @param elementName the XML name for the element.
         */
        Elements(final String elementName) {
            this.elementName = elementName;
        }

        Elements() {
            this.elementName = normalizeName(name());
        }
    }

    /**
     * The attributes of elements in the report.
     */
    public enum Attributes {
        /**
         * A timestamp.
         */
        TIMESTAMP,
        /**
         * A version string.
         */
        VERSION,
        /**
         * The product identifier.
         */
        PRODUCT,
        /**
         * The vendor identifier.
         */
        VENDOR,
        /**
         * The approval flag.
         */
        APPROVAL,
        /**
         * The family category.
         */
        FAMILY,
        /**
         * The document type.
         */
        TYPE,
        /**
         * The id.
         */
        ID,
        /**
         * The name.
         */
        NAME,
        /**
         * A counter.
         */
        COUNT,
        /**
         * A description.
         */
        DESCRIPTION,
        /**
         * The media type for a document.
         */
        MEDIA_TYPE,
        /**
         * The encoding for a text document.
         */
        ENCODING,
        /**
         * Denotes a skipped directory.
         */
        IS_DIRECTORY;

        String attributeName() {
            return normalizeName(this.name());
        }
    }
}
