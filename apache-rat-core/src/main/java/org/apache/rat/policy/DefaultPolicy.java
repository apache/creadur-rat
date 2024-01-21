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
package org.apache.rat.policy;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;

import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.document.IDocumentAnalyser;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseFamilySetFactory;

/**
 * A default Document Analyser that determines if the matched license is in the set of approved licenses.
 */
public class DefaultPolicy implements IDocumentAnalyser {
    private final SortedSet<ILicenseFamily> approvedLicenseFamilies;

    /**
     * Constructor with the list of approved license families.
     * @param approvedLicenseFamilies the approved license families.
     */
    public DefaultPolicy(final Collection<ILicenseFamily> approvedLicenseFamilies) {
        this.approvedLicenseFamilies = LicenseFamilySetFactory.emptyLicenseFamilySet();
        this.approvedLicenseFamilies.addAll(approvedLicenseFamilies);
    }

    /**
     * adds an ILicenseFamily to the list of approved licenses.
     * @param approvedLicense license to be approved.
     */
    public void add(ILicenseFamily approvedLicense) {
        this.approvedLicenseFamilies.add(approvedLicense);
    }

    @Override
    public void analyse(final Document document) {
        if (document != null) {
            boolean approval;
            if (document.getMetaData().value(MetaData.RAT_URL_HEADER_CATEGORY) != null) {
                ILicenseFamily licenseFamily = ILicenseFamily.builder()
                        .setLicenseFamilyCategory(
                                document.getMetaData().value(MetaData.RAT_URL_HEADER_CATEGORY))
                        .setLicenseFamilyName(document.getMetaData().value(MetaData.RAT_URL_LICENSE_FAMILY_NAME))
                        .build();
                approval = approvedLicenseFamilies.contains(licenseFamily);
                reportLicenseApprovalClaim(document, approval);
            }
        }
    }

    /**
     * Report if the document as either having approved license or not.
     * @param document the document to approve.
     * @param isAcceptable {@code true} if the license is an approved one, {@code false} otherwise.
     */
    public void reportLicenseApprovalClaim(final Document document, final boolean isAcceptable) {
        document.getMetaData().set(
                isAcceptable ? MetaData.RAT_APPROVED_LICENSE_DATUM_TRUE : MetaData.RAT_APPROVED_LICENSE_DATUM_FALSE);
    }

    /**
     * Gets an unmodifiable reference to the SortedSet of approved licenses that this policy is holding.
     * @return sorted set of license family definitions.
     */
    public SortedSet<ILicenseFamily> getApprovedLicenseNames() {
        return Collections.unmodifiableSortedSet(approvedLicenseFamilies);
    }
}
