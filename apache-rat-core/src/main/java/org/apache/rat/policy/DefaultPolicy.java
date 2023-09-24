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
import java.util.TreeSet;

import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.document.IDocumentAnalyser;
import org.apache.rat.license.ILicenseFamily;

public class DefaultPolicy implements IDocumentAnalyser {
    private SortedSet<ILicenseFamily> approvedLicenseNames;

    public DefaultPolicy(final Collection<ILicenseFamily> approvedLicenseNames) {
        this.approvedLicenseNames = new TreeSet<>();
        this.approvedLicenseNames.addAll(approvedLicenseNames);
    }

    public void add(ILicenseFamily approvedLicense) {
        this.approvedLicenseNames.add(approvedLicense);
    }

    @Override
    public void analyse(final Document document) {
        if (document != null) {
            boolean approval = false;
            if (document.getMetaData().value(MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY) != null) {
                ILicenseFamily licenseFamily = ILicenseFamily.builder()
                        .setLicenseFamilyCategory(
                                document.getMetaData().value(MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY))
                        .setLicenseFamilyName(document.getMetaData().value(MetaData.RAT_URL_LICENSE_FAMILY_NAME))
                        .build();
                approval = approvedLicenseNames.contains(licenseFamily);
            }
            reportLicenseApprovalClaim(document, approval);
        }
    }

    public void reportLicenseApprovalClaim(final Document subject, final boolean isAcceptable) {
        subject.getMetaData().set(//
                isAcceptable ? MetaData.RAT_APPROVED_LICENSE_DATIM_TRUE : MetaData.RAT_APPROVED_LICENSE_DATIM_FALSE);
    }

    public SortedSet<ILicenseFamily> getApprovedLicenseNames() {
        return Collections.unmodifiableSortedSet(approvedLicenseNames);
    }
}
