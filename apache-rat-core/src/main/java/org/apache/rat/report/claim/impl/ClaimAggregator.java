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

import java.util.Map;

import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.api.RatException;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.report.claim.ClaimStatistic.Counter;

/**
 * The aggregator is used to create a numerical statistic of claims.
 */
public class ClaimAggregator extends AbstractClaimReporter {
    private final ClaimStatistic statistic;

    /**
     * Constructor.
     * @param statistic The statistic to store the statistics in.
     */
    public ClaimAggregator(ClaimStatistic statistic) {
        this.statistic = statistic;
    }

    private <T> void incMapValue(Map<T, int[]> map, T key, int value) {
        final int[] num = map.get(key);

        if (num == null) {
            map.put(key, new int[] { value });
        } else {
            num[0] += value;
        }
    }

    @Override
    protected void handleDocumentCategoryClaim(Document.Type documentType) {
        incMapValue(statistic.getDocumentCategoryMap(), documentType, 1);
    }

    @Override
    protected void handleApprovedLicenseClaim(MetaData metadata) {
        incValueMap(statistic.getCounterMap(), ClaimStatistic.Counter.APPROVED, metadata.approvedLicenses().count());
        incValueMap(statistic.getCounterMap(), ClaimStatistic.Counter.UNAPPROVED,
                metadata.unapprovedLicenses().count());
    }

    private void incValueMap(Map<Counter, int[]> map, Counter key, long value) {
        final int[] num = map.get(key);

        if (num == null) {
            map.put(key, new int[] { (int) value });
        } else {
            num[0] += value;
        }
    }

    @Override
    protected void handleLicenseFamilyNameClaim(String licenseFamilyName) {
        incMapValue(statistic.getLicenseFileNameMap(), licenseFamilyName, 1);
    }

    @Override
    protected void handleHeaderCategoryClaim(ILicense license) {
        String category = license.getLicenseFamily().getFamilyCategory();
        if (category.equals(ILicenseFamily.GENTERATED_CATEGORY)) {
            incValueMap(statistic.getCounterMap(), Counter.GENERATED, 1);
        } else if (category.equals(ILicenseFamily.UNKNOWN_CATEGORY)) {
            incValueMap(statistic.getCounterMap(), Counter.UNKNOWN, 1);
        }
        incMapValue(statistic.getLicenseFamilyCodeMap(), category, 1);
    }

    @Override
    public void endReport() throws RatException {
        super.endReport();
    }
}
