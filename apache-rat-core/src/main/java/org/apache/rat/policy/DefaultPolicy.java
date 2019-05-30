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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultPolicy implements IDocumentAnalyser {
    private List<String> approvedLicenseNames;

    /**
     * Creates a policy that matches the default licenses.
     * Mainly used for testing purposes.
     */
    DefaultPolicy() {
        this(new ArrayList<String>(0), true);
    }

    public DefaultPolicy(final ILicenseFamily[] approvedLicenses, boolean mergeWithDefault) {
        this(ConfigurationUtil.toNames(approvedLicenses), mergeWithDefault);
    }

    public DefaultPolicy(final List<String> approvedLicenseNames, boolean mergeWithDefault) {
        this.approvedLicenseNames = new ArrayList<>();

        if (approvedLicenseNames == null || approvedLicenseNames.isEmpty()) {
            // used in tests only, no additional licenses given but defaults requested
            if(mergeWithDefault) {
                this.approvedLicenseNames = new ArrayList<>(Defaults.DEFAULT_LICENSE_FAMILIES);
            }
        } else {
            // avoid duplicate entries and merge with defaults if requested
            Set<String> mergedLicenses = new HashSet<>(approvedLicenseNames);
            if(mergeWithDefault) {
                mergedLicenses.addAll(Defaults.DEFAULT_LICENSE_FAMILIES);
            }
            this.approvedLicenseNames = new ArrayList<>(mergedLicenses);
        }
        Collections.sort(this.approvedLicenseNames);
    }

    public void analyse(final Document subject) throws RatDocumentAnalysisException {
        if (subject != null) {
            final String name = subject.getMetaData().value(MetaData.RAT_URL_LICENSE_FAMILY_NAME);
            if (name != null) {
                final boolean isApproved = Collections.binarySearch(approvedLicenseNames, name) >= 0;
                reportLicenseApprovalClaim(subject, isApproved);
            }
        }
    }

    public void reportLicenseApprovalClaim(final Document subject, final boolean isAcceptable) {
        subject.getMetaData().set(//
         isAcceptable ? MetaData.RAT_APPROVED_LICENSE_DATIM_TRUE: MetaData.RAT_APPROVED_LICENSE_DATIM_FALSE);
    }

    public List<String> getApprovedLicenseNames() {
        return Collections.unmodifiableList(approvedLicenseNames);
    }
}
