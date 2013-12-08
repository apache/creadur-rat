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
import org.apache.rat.report.AbstractReport;

/**
 * Abstract base implementation of {@link AbstractReport}. It is strongly
 * suggested, that implementations derive from this class.
 */
public abstract class AbstractClaimReporter extends AbstractReport {

	/**
	 * Handle document category claim.
	 * 
	 * @param documentCategoryName
	 *            the document category name
	 */
	protected void handleDocumentCategoryClaim(final String documentCategoryName) {
		// Does nothing
	}

	/**
	 * Handle approved license claim.
	 * 
	 * @param licenseApproved
	 *            the license approved
	 */
	protected void handleApprovedLicenseClaim(final String licenseApproved) {
		// Does nothing
	}

	/**
	 * Handle license family name claim.
	 * 
	 * @param licenseFamilyName
	 *            the license family name
	 */
	protected void handleLicenseFamilyNameClaim(final String licenseFamilyName) {
		// Does Nothing
	}

	/**
	 * Handle header category claim.
	 * 
	 * @param headerCategory
	 *            the header category
	 */
	protected void handleHeaderCategoryClaim(final String headerCategory) {
		// Does nothing
	}

	/**
	 * Write document claim.
	 * 
	 * @param subject
	 *            the subject
	 */
	private void writeDocumentClaim(final Document subject) {
		final MetaData metaData = subject.getMetaData();
		writeHeaderCategory(metaData);
		writeLicenseFamilyName(metaData);
		writeDocumentCategory(metaData);
		writeApprovedLicenseClaim(metaData);
	}

	/**
	 * Write approved license claim.
	 * 
	 * @param metaData
	 *            the meta data
	 */
	private void writeApprovedLicenseClaim(final MetaData metaData) {
		final MetaData.Datum approvedLicenseDatum = metaData
				.get(MetaData.RAT_URL_APPROVED_LICENSE);
		if (approvedLicenseDatum != null) {
			final String approvedLicense = approvedLicenseDatum.getValue();
			if (approvedLicense != null) {
				handleApprovedLicenseClaim(approvedLicense);
			}
		}
	}

	/**
	 * Write header category.
	 * 
	 * @param metaData
	 *            the meta data
	 */
	private void writeHeaderCategory(final MetaData metaData) {
		final MetaData.Datum headerCategoryDatum = metaData
				.get(MetaData.RAT_URL_HEADER_CATEGORY);
		if (headerCategoryDatum != null) {
			final String headerCategory = headerCategoryDatum.getValue();
			if (headerCategory != null) {
				handleHeaderCategoryClaim(headerCategory);
			}
		}
	}

	/**
	 * Write license family name.
	 * 
	 * @param metaData
	 *            the meta data
	 */
	private void writeLicenseFamilyName(final MetaData metaData) {
		final MetaData.Datum licenseFamilyNameDatum = metaData
				.get(MetaData.RAT_URL_LICENSE_FAMILY_NAME);
		if (licenseFamilyNameDatum != null) {
			final String licenseFamilyName = licenseFamilyNameDatum.getValue();
			if (licenseFamilyName != null) {
				handleLicenseFamilyNameClaim(licenseFamilyName);
			}
		}
	}

	/**
	 * Write document category.
	 * 
	 * @param metaData
	 *            the meta data
	 */
	private void writeDocumentCategory(final MetaData metaData) {
		final MetaData.Datum documentCategoryDatum = metaData
				.get(MetaData.RAT_URL_DOCUMENT_CATEGORY);
		if (documentCategoryDatum != null) {
			final String documentCategory = documentCategoryDatum.getValue();
			if (documentCategory != null) {
				handleDocumentCategoryClaim(documentCategory);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.rat.report.AbstractReport#report(org.apache.rat.api.Document)
	 */
	@Override
	public void report(final Document subject) {
		writeDocumentClaim(subject);
	}
}
