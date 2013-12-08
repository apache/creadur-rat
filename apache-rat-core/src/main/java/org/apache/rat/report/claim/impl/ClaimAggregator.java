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

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.rat.api.MetaData;
import org.apache.rat.report.claim.ClaimStatistic;

/**
 * The aggregator is used to create a numerical statistic of claims.
 */
public class ClaimAggregator extends AbstractClaimReporter {

	/** The statistic. */
	private final ClaimStatistic statistic;

	/** The nums by license family name. */
	private final Map<String, Integer> numsByLicenseFamilyName = new ConcurrentHashMap<String, Integer>();

	/** The nums by license family code. */
	private final Map<String, Integer> numsByLicenseFamilyCode = new ConcurrentHashMap<String, Integer>();

	/** The nums by file type. */
	private final Map<String, Integer> numsByFileType = new ConcurrentHashMap<String, Integer>();

	/** The num unknown. */
	private int numApproved, numUnApproved, numGenerated, numUnknown;

	/**
	 * Instantiates a new claim aggregator.
	 * 
	 * @param pStatistic
	 *            the statistic
	 */
	public ClaimAggregator(final ClaimStatistic pStatistic) {
		super();
		statistic = pStatistic;
	}

	/**
	 * Inc map value.
	 * 
	 * @param pMap
	 *            the map
	 * @param pKey
	 *            the key
	 */
	private void incMapValue(final Map<String, Integer> pMap, final String pKey) {
		final Integer num = pMap.get(pKey);
		int newNum;
		if (num == null) {
			newNum = 1;
		} else {
			newNum = num.intValue() + 1;
		}
		pMap.put(pKey, Integer.valueOf(newNum));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.rat.report.claim.impl.AbstractClaimReporter#
	 * handleDocumentCategoryClaim(java.lang.String)
	 */
	@Override
	protected void handleDocumentCategoryClaim(final String documentCategoryName) {
		incMapValue(numsByFileType, documentCategoryName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.rat.report.claim.impl.AbstractClaimReporter#
	 * handleApprovedLicenseClaim(java.lang.String)
	 */
	@Override
	protected void handleApprovedLicenseClaim(final String licenseApproved) {
		if (MetaData.RAT_APPROVED_LICENSE_VALUE_TRUE.equals(licenseApproved)) {
			numApproved++;
		} else {
			numUnApproved++;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.rat.report.claim.impl.AbstractClaimReporter#
	 * handleLicenseFamilyNameClaim(java.lang.String)
	 */
	@Override
	protected void handleLicenseFamilyNameClaim(final String licenseFamilyName) {
		incMapValue(numsByLicenseFamilyName, licenseFamilyName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.rat.report.claim.impl.AbstractClaimReporter#
	 * handleHeaderCategoryClaim(java.lang.String)
	 */
	@Override
	protected void handleHeaderCategoryClaim(final String headerCategory) {

		if (MetaData.RAT_LICENSE_FAMILY_CATEGORY_VALUE_GEN
				.equals(headerCategory)) {
			numGenerated++;
			incMapValue(numsByLicenseFamilyCode,
					MetaData.RAT_LICENSE_FAMILY_CATEGORY_VALUE_GEN);
		} else if (MetaData.RAT_LICENSE_FAMILY_CATEGORY_VALUE_UNKNOWN
				.equals(headerCategory)) {
			numUnknown++;
			incMapValue(numsByLicenseFamilyCode,
					MetaData.RAT_LICENSE_FAMILY_CATEGORY_VALUE_UNKNOWN);
		}
	}

	/**
	 * Fill claim statistic.
	 * 
	 * @param pStatistic
	 *            the statistic
	 */
	public void fillClaimStatistic(final ClaimStatistic pStatistic) {
		pStatistic.setDocumentCategoryMap(numsByFileType);
		pStatistic.setLicenseFileCodeMap(numsByLicenseFamilyCode);
		pStatistic.setLicenseFileNameMap(numsByLicenseFamilyName);
		pStatistic.setNumApproved(numApproved);
		pStatistic.setNumGenerated(numGenerated);
		pStatistic.setNumUnApproved(numUnApproved);
		pStatistic.setNumUnknown(numUnknown);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.rat.report.AbstractReport#endReport()
	 */
	@Override
	public void endReport() throws IOException {
		super.endReport();
		fillClaimStatistic(statistic);
	}
}
