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

import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.api.MetaData.Datum;
import org.apache.rat.document.IDocumentAnalyser;
import org.apache.rat.license.ILicenseFamily;

import java.util.Arrays;

public class DefaultPolicy implements IDocumentAnalyser {
    private static final String[] APPROVED_LICENSES = {
        MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_APACHE_LICENSE_VERSION_2_0,
        MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_OASIS_OPEN_LICENSE,
        MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_W3C_SOFTWARE_COPYRIGHT,
        MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_W3C_DOCUMENT_COPYRIGHT,
        MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_MODIFIED_BSD_LICENSE,
        MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_MIT,
        MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_CDDL1,
    };
    
    private static final String[] toNames(final ILicenseFamily[] approvedLicenses) {
        String[] results = null;
        if (approvedLicenses != null) {
            final int length = approvedLicenses.length;
            results = new String[length];
            for (int i=0;i<length;i++) {
                results[i] = approvedLicenses[i].getFamilyName();
            }
        }
        return results;
    }

    private final String[] approvedLicenseNames;
    
    public DefaultPolicy() {
        this(APPROVED_LICENSES);
    }
    
    public DefaultPolicy(final ILicenseFamily[] approvedLicenses) {
        this(toNames(approvedLicenses));
    }

    public DefaultPolicy(final String[] approvedLicenseNames) {
        if (approvedLicenseNames == null) {
            this.approvedLicenseNames = APPROVED_LICENSES;
        } else {
            final int length = approvedLicenseNames.length;
            this.approvedLicenseNames = new String[length];
            System.arraycopy(approvedLicenseNames, 0, this.approvedLicenseNames, 0, length);
        }
        Arrays.sort(this.approvedLicenseNames);
    }

    public void reportLicenseApprovalClaim(final Document subject, final boolean isAcceptable) {
        final Datum datum;
        if (isAcceptable) {
            datum = MetaData.RAT_APPROVED_LICENSE_DATIM_TRUE;
        } else {
            datum = MetaData.RAT_APPROVED_LICENSE_DATIM_FALSE;
        }
        subject.getMetaData().set(datum);
    }
    
    public void analyse(final Document subject) {
        if (subject != null) {
            final String name = subject.getMetaData().value(MetaData.RAT_URL_LICENSE_FAMILY_NAME);
            if (name != null) {
                final boolean isApproved = Arrays.binarySearch(approvedLicenseNames, name) >= 0;
                reportLicenseApprovalClaim(subject, isApproved);
            }
        }
    }
}
