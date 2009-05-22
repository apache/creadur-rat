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

import java.io.IOException;

import org.apache.rat.api.MetaData;
import org.apache.rat.document.IDocument;
import org.apache.rat.report.RatReportFailedException;
import org.apache.rat.report.claim.IClaim;
import org.apache.rat.report.claim.IClaimReporter;
import org.apache.rat.report.claim.impl.FileTypeClaim;
import org.apache.rat.report.xml.writer.IXmlWriter;

public class SimpleXmlClaimReporter implements IClaimReporter {
    public static final String LICENSE_APPROVAL_PREDICATE = "license-approval";
    public static final String LICENSE_FAMILY_PREDICATE = "license-family";
    public static final String HEADER_SAMPLE_PREDICATE = "header-sample";
    public static final String HEADER_TYPE_PREDICATE = "header-type";
    public static final String FILE_TYPE_PREDICATE = "type";
    public static final String ARCHIVE_TYPE_PREDICATE = "archive-type";
    public static final String ARCHIVE_TYPE_UNREADABLE = "unreadable";
    public static final String ARCHIVE_TYPE_READABLE = "readable";

    private static final String NAME = "name";
    private final IXmlWriter writer;
    private IDocument lastSubject;
    private IDocument subject;
    private boolean writtenDocumentClaims = false;
    
    public SimpleXmlClaimReporter(final IXmlWriter writer) {
        this.writer = writer;
    }

    protected void handleClaim(FileTypeClaim pClaim)
            throws IOException, RatReportFailedException {
        writeClaim(FILE_TYPE_PREDICATE, pClaim.getType().getName(), false);
    }


    protected void handleClaim(CustomClaim pClaim)
            throws IOException, RatReportFailedException {
        writeClaim(pClaim.getPredicate(), pClaim.getObject(), pClaim.isLiteral());
    }

    /**
     * Writes a single claim to the XML file.
     * @param pPredicate The claims predicate.
     * @param pObject The claims object.
     * @param pLiteral Whether to write the object as an element (true),
     *   or an attribute (false).
     * @throws IOException An I/O error occurred while writing the claim.
     * @throws RatReportFailedException Another error occurred while writing the claim.
     */
    protected void writeClaim(String pPredicate, String pObject, boolean pLiteral)
            throws IOException, RatReportFailedException {
        if (pLiteral) {
            writer.openElement(pPredicate).content(pObject).closeElement();
        } else {
            writer.openElement(pPredicate).attribute(NAME, pObject).closeElement();
        }
    }
    
    protected void handleClaim(IClaim pClaim) throws IOException, RatReportFailedException {
        if (pClaim instanceof FileTypeClaim) {
            handleClaim((FileTypeClaim) pClaim);
        } else if (pClaim instanceof CustomClaim) {
            handleClaim((CustomClaim) pClaim);
        } else {
            throw new IllegalStateException("Invalid claim type: " + pClaim.getClass().getName());
        }
    }

    public void claim(IClaim pClaim) throws RatReportFailedException {
        try {
            if(!writtenDocumentClaims) {
                writeDocumentClaims(subject);
                writtenDocumentClaims = true;
            }
            handleClaim(pClaim);
        } catch (IOException e) {
            throw new RatReportFailedException("XML writing failure: " + e.getMessage()
                    + " subject: " + subject + " claim type: "
                    + pClaim.getClass().getName(), e);
        }
    }

    public void report(final IDocument subject) throws RatReportFailedException {
        this.subject = subject;
        try {
            if (!(subject.equals(lastSubject))) {
                if (lastSubject != null) {
                    writer.closeElement();
                    if(!writtenDocumentClaims) {
                        writeDocumentClaims(lastSubject);
                    }
                }
                writer.openElement("resource").attribute(NAME, subject.getName());
            }
            lastSubject = subject;
            writtenDocumentClaims = false;
        } catch (IOException e) {
            throw new RatReportFailedException("XML writing failure: " + e.getMessage()
                    + " subject: " + subject, e);
        }
    }

    private void writeDocumentClaims(final IDocument subject) throws IOException, RatReportFailedException {
        final MetaData metaData = subject.getMetaData();
        writeHeaderSample(metaData);
        writeLicenseFamilyCategory(metaData);
        writeHeaderCategory(metaData);
        writeLicenseFamilyName(metaData);
        writeApprovedLicense(metaData);
        
    }

    private void writeApprovedLicense(final MetaData metaData) throws IOException, RatReportFailedException {
        final MetaData.Datum approvedLicenseDatum = metaData.get(MetaData.RAT_URL_APPROVED_LICENSE);
        if (approvedLicenseDatum != null) {
            final String approvedLicense = approvedLicenseDatum.getValue();
            if (approvedLicense != null) {
                writeClaim(LICENSE_APPROVAL_PREDICATE, approvedLicense, false);
            }
        }
    }

    private void writeLicenseFamilyName(final MetaData metaData) throws IOException, RatReportFailedException {
        final MetaData.Datum licenseFamilyNameDatum = metaData.get(MetaData.RAT_URL_LICENSE_FAMILY_NAME);
        if (licenseFamilyNameDatum != null) {
            final String licenseFamilyName = licenseFamilyNameDatum.getValue();
            if (licenseFamilyName != null) {
                writeClaim(LICENSE_FAMILY_PREDICATE, licenseFamilyName, false);
            }
        }
    }

    private void writeHeaderCategory(final MetaData metaData) throws IOException, RatReportFailedException {
        final MetaData.Datum headerCategoryDatum = metaData.get(MetaData.RAT_URL_HEADER_CATEGORY);
        if (headerCategoryDatum != null) {
            final String headerCategory = headerCategoryDatum.getValue();
            writeClaim(HEADER_TYPE_PREDICATE, headerCategory, false);
        }
    }

    private void writeLicenseFamilyCategory(final MetaData metaData) throws IOException, RatReportFailedException {
        final MetaData.Datum licenseFamilyCategoryDatum = metaData.get(MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY);
        if (licenseFamilyCategoryDatum != null) {
            final String licenseFamilyCategory = licenseFamilyCategoryDatum.getValue();
            writeClaim(LICENSE_FAMILY_PREDICATE, licenseFamilyCategory, false);
        }
    }

    private void writeHeaderSample(final MetaData metaData) throws IOException, RatReportFailedException {
        final MetaData.Datum sampleDatum = metaData.get(MetaData.RAT_URL_HEADER_SAMPLE);
        if (sampleDatum != null) {
            final String sample = sampleDatum.getValue();
            if (sample != null) {
                writeClaim(HEADER_SAMPLE_PREDICATE, sample, true);
            }
        }
    }

}
