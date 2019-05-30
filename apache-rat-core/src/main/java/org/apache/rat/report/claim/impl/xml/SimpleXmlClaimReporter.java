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
package org.apache.rat.report.claim.impl.xml;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.api.RatException;
import org.apache.rat.report.AbstractReport;
import org.apache.rat.report.xml.writer.IXmlWriter;

import java.io.IOException;
import java.util.Calendar;

public class SimpleXmlClaimReporter extends AbstractReport {
    private static final String RAT_REPORT = "rat-report";
    private static final String TIMESTAMP = "timestamp";
    private static final String LICENSE_APPROVAL_PREDICATE = "license-approval";
    private static final String LICENSE_FAMILY_PREDICATE = "license-family";
    private static final String HEADER_SAMPLE_PREDICATE = "header-sample";
    private static final String HEADER_TYPE_PREDICATE = "header-type";
    private static final String FILE_TYPE_PREDICATE = "type";

    private final IXmlWriter writer;
    private static final String NAME = "name";
    private boolean firstTime = true;

    public SimpleXmlClaimReporter(final IXmlWriter writer) {
        this.writer = writer;
    }


    /**
     * Writes a single claim to the XML file.
     * @param pPredicate The claims predicate.
     * @param pObject The claims object.
     * @param pLiteral Whether to write the object as an element (true),
     *   or an attribute (false).
     * @throws IOException An I/O error occurred while writing the claim.
     * @throws RatException Another error occurred while writing the claim.
     */
    protected void writeClaim(String pPredicate, String pObject, boolean pLiteral)
    throws IOException, RatException {
        if (pLiteral) {
            writer.openElement(pPredicate).content(pObject).closeElement();
        } else {
            writer.openElement(pPredicate).attribute(NAME, pObject).closeElement();
        }
    }

    @Override
    public void report(final Document subject) throws RatException {
        try {
            if (firstTime) {
                firstTime = false;
            } else {
                writer.closeElement();
            }
            writer.openElement("resource").attribute(NAME, subject.getName());
            writeDocumentClaims(subject);
        } catch (IOException e) {
            throw new RatException("XML writing failure: " + e.getMessage()
                    + " subject: " + subject, e);
        }
    }

    private void writeDocumentClaims(final Document subject) throws IOException, RatException {
        final MetaData metaData = subject.getMetaData();
        writeHeaderSample(metaData);
        writeHeaderCategory(metaData);
        writeLicenseFamilyName(metaData);
        writeApprovedLicense(metaData);
        writeDocumentCategory(metaData);
    }

    private void writeApprovedLicense(final MetaData metaData) throws IOException, RatException {
        final String approvedLicense = metaData.value(MetaData.RAT_URL_APPROVED_LICENSE);
        if (approvedLicense != null) {
            writeClaim(LICENSE_APPROVAL_PREDICATE, approvedLicense, false);
        }
    }

    private void writeLicenseFamilyName(final MetaData metaData) throws IOException, RatException {
        final String licenseFamilyName = metaData.value(MetaData.RAT_URL_LICENSE_FAMILY_NAME);
        if (licenseFamilyName != null) {
            writeClaim(LICENSE_FAMILY_PREDICATE, licenseFamilyName, false);
        }
    }

    private void writeHeaderCategory(final MetaData metaData) throws IOException, RatException {
        final String headerCategory = metaData.value(MetaData.RAT_URL_HEADER_CATEGORY);
        if (headerCategory != null) {
            writeClaim(HEADER_TYPE_PREDICATE, headerCategory, false);
        }
    }

    private void writeHeaderSample(final MetaData metaData) throws IOException, RatException {
        final String sample = metaData.value(MetaData.RAT_URL_HEADER_SAMPLE);
        if (sample != null) {
            writeClaim(HEADER_SAMPLE_PREDICATE, sample, true);
        }
    }

    private void writeDocumentCategory(final MetaData metaData) throws IOException, RatException {
        final String documentCategory = metaData.value(MetaData.RAT_URL_DOCUMENT_CATEGORY);
        if (documentCategory != null) {
            writeClaim(FILE_TYPE_PREDICATE, documentCategory, false);
        }
    }

    @Override
    public void startReport() throws RatException {
        try {
            writer.openElement(RAT_REPORT)
                .attribute(TIMESTAMP,
                           DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT
                           .format(Calendar.getInstance()));
        } catch (IOException e) {
            throw new RatException("Cannot open start element", e);
        }
    }

    @Override
    public void endReport() throws RatException {
        try {
            writer.closeDocument();
        } catch (IOException e) {
            throw new RatException("Cannot close last element", e);
        }
    }
}
