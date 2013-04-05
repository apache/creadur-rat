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

package org.apache.rat.report.claim.impl;

import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.api.RatException;
import org.apache.rat.report.AbstractReport;


/**
 * Abstract base implementation of {@link AbstractReport}.
 * It is strongly suggested, that implementations derive from
 * this class.
 */
public abstract class AbstractClaimReporter extends AbstractReport {
    protected void handleDocumentCategoryClaim(String documentCategoryName) {
        // Does nothing
    }

    protected void handleApprovedLicenseClaim(String licenseApproved) {
        // Does nothing
    }

    protected void handleLicenseFamilyNameClaim(String licenseFamilyName) {
        // Does Nothing
    }

    protected void handleHeaderCategoryClaim(String headerCategory) {
        // Does nothing
    }

    private void writeDocumentClaim(Document subject)  {
        final MetaData metaData = subject.getMetaData();
        writeHeaderCategory(metaData);
        writeLicenseFamilyName(metaData);
        writeDocumentCategory(metaData);
        writeApprovedLicenseClaim(metaData);
    }

    private void writeApprovedLicenseClaim(final MetaData metaData) {
        final MetaData.Datum approvedLicenseDatum = metaData.get(MetaData.RAT_URL_APPROVED_LICENSE);
        if (approvedLicenseDatum != null) {
            final String approvedLicense = approvedLicenseDatum.getValue();
            if (approvedLicense != null) {
                handleApprovedLicenseClaim(approvedLicense);
            }
        }
    }

    private void writeHeaderCategory(final MetaData metaData) {
        final MetaData.Datum headerCategoryDatum = metaData.get(MetaData.RAT_URL_HEADER_CATEGORY);
        if (headerCategoryDatum != null) {
            final String headerCategory = headerCategoryDatum.getValue();
            if (headerCategory != null) {
                handleHeaderCategoryClaim(headerCategory);
            }
        }
    }

    private void writeLicenseFamilyName(final MetaData metaData) {
        final MetaData.Datum licenseFamilyNameDatum = metaData.get(MetaData.RAT_URL_LICENSE_FAMILY_NAME);
        if (licenseFamilyNameDatum != null) {
            final String licenseFamilyName = licenseFamilyNameDatum.getValue();
            if (licenseFamilyName != null) {
                handleLicenseFamilyNameClaim(licenseFamilyName);
            }
        }
    }
    
    private void writeDocumentCategory(final MetaData metaData) {
        final MetaData.Datum documentCategoryDatum = metaData.get(MetaData.RAT_URL_DOCUMENT_CATEGORY);
        if (documentCategoryDatum != null) {
            final String documentCategory = documentCategoryDatum.getValue();
            if (documentCategory != null) {
                handleDocumentCategoryClaim(documentCategory);
            }
        }
    }
    
    @Override
    public void report(Document subject) throws RatException {
        writeDocumentClaim(subject);
    }
}
