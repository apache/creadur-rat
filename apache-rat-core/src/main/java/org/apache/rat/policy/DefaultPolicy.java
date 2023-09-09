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

import org.apache.rat.Defaults;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.config.ConfigurationUtil;
import org.apache.rat.document.IDocumentAnalyser;
import org.apache.rat.document.RatDocumentAnalysisException;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.SimpleLicenseFamily;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class DefaultPolicy implements IDocumentAnalyser {
    private SortedSet<ILicenseFamily> approvedLicenseNames;

    /**
     * Creates a policy that matches the default licenses.
     * Mainly used for testing purposes.
     */
    DefaultPolicy() {
        this(new ArrayList<ILicenseFamily>(0), true);
    }

    public DefaultPolicy(final ILicenseFamily[] approvedLicenses, boolean mergeWithDefault) {
        this(Arrays.asList(approvedLicenses), mergeWithDefault);
    }

    public DefaultPolicy(final Collection<ILicenseFamily> approvedLicenseNames, boolean mergeWithDefault) {
        this.approvedLicenseNames = new TreeSet<>();

        if (mergeWithDefault) {
            this.approvedLicenseNames.addAll(Defaults.getLicenseFamilies());
        }
        if (approvedLicenseNames != null) {
            this.approvedLicenseNames.addAll(approvedLicenseNames);
        }
    }

    @Override
    public void analyse(final Document subject) throws RatDocumentAnalysisException {
        if (subject != null) {
            final ILicenseFamily licenseFamily = new SimpleLicenseFamily( 
                    subject.getMetaData().value(MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY),
                    subject.getMetaData().value(MetaData.RAT_URL_LICENSE_FAMILY_NAME));
           
            reportLicenseApprovalClaim(subject, approvedLicenseNames.contains(licenseFamily));
        }
    }

    public void reportLicenseApprovalClaim(final Document subject, final boolean isAcceptable) {
        subject.getMetaData().set(//
         isAcceptable ? MetaData.RAT_APPROVED_LICENSE_DATIM_TRUE: MetaData.RAT_APPROVED_LICENSE_DATIM_FALSE);
    }

    public SortedSet<ILicenseFamily> getApprovedLicenseNames() {
        return Collections.unmodifiableSortedSet(approvedLicenseNames);
    }
}
