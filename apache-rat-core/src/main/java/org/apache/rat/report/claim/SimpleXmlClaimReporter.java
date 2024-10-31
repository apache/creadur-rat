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
package org.apache.rat.report.claim;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.api.RatException;
import org.apache.rat.license.ILicense;
import org.apache.rat.report.AbstractReport;
import org.apache.rat.report.xml.writer.IXmlWriter;

/**
 * A claim reporter to write the XML document.
 */
public class SimpleXmlClaimReporter extends AbstractReport {
    /** the resource element name */
    private static final String RESOURCE = "resource";
    /** the license element name */
    private static final String LICENSE = "license";
    /** the approval attribute name */
    private static final String APPROVAL = "approval";
    /** the family attribute name */
    private static final String FAMILY = "family";
    /** the notes attribute name */
    private static final String NOTES = "notes";
    /** the sample attribute name */
    private static final String SAMPLE = "sample";
    /** the type attribute name */
    private static final String TYPE = "type";
    /** the ID attribute name */
    private static final String ID = "id";
    /** name attribute name */
    private static final String NAME = "name";

    /** the writer to write to */
    private final IXmlWriter writer;

    /**
     * Constructor.
     * @param writer The writer to write the report to.
     */
    public SimpleXmlClaimReporter(final IXmlWriter writer) {
        this.writer = writer;
    }

    @Override
    public void report(final Document subject) throws RatException {
        try {
            writeDocumentClaims(subject);
        } catch (IOException e) {
            throw new RatException("XML writing failure: " + e.getMessage() + " subject: " + subject, e);
        }
    }

    private void writeLicenseClaims(final ILicense license, final MetaData metaData) throws IOException {
        writer.openElement(LICENSE).attribute(ID, license.getId()).attribute(NAME, license.getName())
                .attribute(APPROVAL, Boolean.valueOf(metaData.isApproved(license)).toString())
                .attribute(FAMILY, license.getLicenseFamily().getFamilyCategory());
        if (StringUtils.isNotBlank(license.getNote())) {
            writer.openElement(NOTES).cdata(license.getNote()).closeElement();
        }
        writer.closeElement();
    }

    private void writeDocumentClaims(final Document document) throws IOException {
        final MetaData metaData = document.getMetaData();
        writer.openElement(RESOURCE).attribute(NAME, document.getName().localized("/")).attribute(TYPE,
                metaData.getDocumentType().toString());
        for (Iterator<ILicense> iter = metaData.licenses().iterator(); iter.hasNext();) {
            writeLicenseClaims(iter.next(), metaData);
        }
        writeHeaderSample(metaData);
        writer.closeElement();
    }

    private void writeHeaderSample(final MetaData metaData) throws IOException {
        final String sample = metaData.getSampleHeader();
        if (StringUtils.isNotBlank(sample)) {
            writer.openElement(SAMPLE).cdata(sample).closeElement();
        }
    }

    @Override
    public void startReport() {
        // does nothing
    }

    @Override
    public void endReport() {
        // does nothing
    }
}
