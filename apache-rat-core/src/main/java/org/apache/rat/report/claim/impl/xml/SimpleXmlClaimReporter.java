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
import java.util.Calendar;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.report.AbstractReport;
import org.apache.rat.report.xml.writer.IXmlWriter;

/**
 * The Class SimpleXmlClaimReporter.
 */
public class SimpleXmlClaimReporter extends AbstractReport {

	/** The Constant LICENSE_APPROVAL_PREDICATE. */
	public static final String LICENSE_APPROVAL_PREDICATE = "license-approval";

	/** The Constant LICENSE_FAMILY_PREDICATE. */
	public static final String LICENSE_FAMILY_PREDICATE = "license-family";

	/** The Constant HEADER_SAMPLE_PREDICATE. */
	public static final String HEADER_SAMPLE_PREDICATE = "header-sample";

	/** The Constant HEADER_TYPE_PREDICATE. */
	public static final String HEADER_TYPE_PREDICATE = "header-type";

	/** The Constant FILE_TYPE_PREDICATE. */
	public static final String FILE_TYPE_PREDICATE = "type";

	/** The Constant ARCHIVE_TYPE_PREDICATE. */
	public static final String ARCHIVE_TYPE_PREDICATE = "archive-type";

	/** The Constant ARCHIVE_TYPE_UNREADABLE. */
	public static final String ARCHIVE_TYPE_UNREADABLE = "unreadable";

	/** The Constant ARCHIVE_TYPE_READABLE. */
	public static final String ARCHIVE_TYPE_READABLE = "readable";

	/** The Constant NAME. */
	private static final String NAME = "name";

	/** The writer. */
	private final IXmlWriter writer;

	/** The first time. */
	private boolean firstTime = true;

	/**
	 * Instantiates a new simple xml claim reporter.
	 * 
	 * @param writer
	 *            the writer
	 */
	public SimpleXmlClaimReporter(final IXmlWriter writer) {
		super();
		this.writer = writer;
	}

	/**
	 * Writes a single claim to the XML file.
	 * 
	 * @param pPredicate
	 *            The claims predicate.
	 * @param pObject
	 *            The claims object.
	 * @param pLiteral
	 *            Whether to write the object as an element (true), or an
	 *            attribute (false).
	 * @throws IOException
	 *             An I/O error occurred while writing the claim.
	 */
	protected void writeClaim(final String pPredicate, final String pObject,
			final boolean pLiteral) throws IOException {
		if (pLiteral) {
			writer.openElement(pPredicate).content(pObject).closeElement();
		} else {
			writer.openElement(pPredicate).attribute(NAME, pObject)
					.closeElement();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.rat.report.AbstractReport#report(org.apache.rat.api.Document)
	 */
	@Override
	public void report(final Document subject) throws IOException {
		if (firstTime) {
			firstTime = false;
		} else {
			writer.closeElement();
		}
		writer.openElement("resource").attribute(NAME, subject.getName());
		writeDocumentClaims(subject);
	}

	/**
	 * Write document claims.
	 * 
	 * @param subject
	 *            the subject
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void writeDocumentClaims(final Document subject)
 throws IOException {
		final MetaData metaData = subject.getMetaData();
		writeHeaderSample(metaData);
		writeHeaderCategory(metaData);
		writeLicenseFamilyName(metaData);
		writeApprovedLicense(metaData);
		writeDocumentCategory(metaData);
	}

	/**
	 * Write approved license.
	 * 
	 * @param metaData
	 *            the meta data
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void writeApprovedLicense(final MetaData metaData)
			throws IOException {
		final String approvedLicense = metaData
				.value(MetaData.RAT_URL_APPROVED_LICENSE);
		if (approvedLicense != null) {
			writeClaim(LICENSE_APPROVAL_PREDICATE, approvedLicense, false);
		}
	}

	/**
	 * Write license family name.
	 * 
	 * @param metaData
	 *            the meta data
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void writeLicenseFamilyName(final MetaData metaData)
			throws IOException {
		final String licenseFamilyName = metaData
				.value(MetaData.RAT_URL_LICENSE_FAMILY_NAME);
		if (licenseFamilyName != null) {
			writeClaim(LICENSE_FAMILY_PREDICATE, licenseFamilyName, false);
		}
	}

	/**
	 * Write header category.
	 * 
	 * @param metaData
	 *            the meta data
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void writeHeaderCategory(final MetaData metaData)
			throws IOException {
		final String headerCategory = metaData
				.value(MetaData.RAT_URL_HEADER_CATEGORY);
		if (headerCategory != null) {
			writeClaim(HEADER_TYPE_PREDICATE, headerCategory, false);
		}
	}

	/**
	 * Write header sample.
	 * 
	 * @param metaData
	 *            the meta data
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void writeHeaderSample(final MetaData metaData) throws IOException {
		final String sample = metaData.value(MetaData.RAT_URL_HEADER_SAMPLE);
		if (sample != null) {
			writeClaim(HEADER_SAMPLE_PREDICATE, sample, true);
		}
	}

	/**
	 * Write document category.
	 * 
	 * @param metaData
	 *            the meta data
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void writeDocumentCategory(final MetaData metaData)
			throws IOException {
		final String documentCategory = metaData
				.value(MetaData.RAT_URL_DOCUMENT_CATEGORY);
		if (documentCategory != null) {
			writeClaim(FILE_TYPE_PREDICATE, documentCategory, false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.rat.report.AbstractReport#startReport()
	 */
	@Override
	public void startReport() throws IOException {
		writer.openElement("rat-report").attribute(
				"timestamp",
				DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(Calendar
						.getInstance()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.rat.report.AbstractReport#endReport()
	 */
	@Override
	public void endReport() throws IOException {
		writer.closeDocument();
	}
}
