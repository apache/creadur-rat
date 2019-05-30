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

import org.apache.rat.api.MetaData;
import org.apache.rat.api.RatException;
import org.apache.rat.report.claim.ClaimStatistic;

import java.util.HashMap;
import java.util.Map;


/**
 * The aggregator is used to create a numerical statistic
 * of claims.
 */
public class ClaimAggregator extends AbstractClaimReporter {
    private final ClaimStatistic statistic;
    private final Map<String, Integer> numsByLicenseFamilyName = new HashMap<>();
    private final Map<String, Integer> numsByLicenseFamilyCode = new HashMap<>();
    private final Map<String, Integer> numsByFileType = new HashMap<>();
    private int numApproved, numUnApproved, numGenerated, numUnknown;

    public ClaimAggregator(ClaimStatistic pStatistic) {
        statistic = pStatistic;
    }
    
    private void incMapValue(Map<String, Integer> pMap, String pKey) {
        final Integer num = pMap.get(pKey);
        final int newNum;
        if (num == null) {
            newNum = 1;
        } else {
            newNum = num + 1;
        }
        pMap.put(pKey, newNum);
    }
    
    @Override
    protected void handleDocumentCategoryClaim(String documentCategoryName) {
        incMapValue(numsByFileType, documentCategoryName);
    }

    @Override
    protected void handleApprovedLicenseClaim(String licenseApproved) {
        if (MetaData.RAT_APPROVED_LICENSE_VALUE_TRUE.equals(licenseApproved)) {
            numApproved++;
        } else {
            numUnApproved++;
        }
    }

    @Override
    protected void handleLicenseFamilyNameClaim(String licenseFamilyName) {
        incMapValue(numsByLicenseFamilyName, licenseFamilyName);
    }

    @Override
    protected void handleHeaderCategoryClaim(String headerCategory) {
        
        if (MetaData.RAT_LICENSE_FAMILY_CATEGORY_VALUE_GEN.equals(headerCategory)) {
            numGenerated++;
            incMapValue(numsByLicenseFamilyCode, MetaData.RAT_LICENSE_FAMILY_CATEGORY_VALUE_GEN);
        } else if (MetaData.RAT_LICENSE_FAMILY_CATEGORY_VALUE_UNKNOWN.equals(headerCategory)) {
            numUnknown++;
            incMapValue(numsByLicenseFamilyCode, MetaData.RAT_LICENSE_FAMILY_CATEGORY_VALUE_UNKNOWN);
        }
    }

    public void fillClaimStatistic(ClaimStatistic pStatistic) {
        pStatistic.setDocumentCategoryMap(numsByFileType);
        pStatistic.setLicenseFileCodeMap(numsByLicenseFamilyCode);
        pStatistic.setLicenseFileNameMap(numsByLicenseFamilyName);
        pStatistic.setNumApproved(numApproved);
        pStatistic.setNumGenerated(numGenerated);
        pStatistic.setNumUnApproved(numUnApproved);
        pStatistic.setNumUnknown(numUnknown);
    }

    @Override
    public void endReport() throws RatException {
        super.endReport();
        fillClaimStatistic(statistic);
    }
}
