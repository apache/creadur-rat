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
package org.apache.rat;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.license.ILicenseFamily;

import java.util.List;


/**
 * A configuration object is used by the frontend to invoke the
 * {@link Report}. Basically, the sole purpose of the frontends is
 * to create the configuration and invoke the {@link Report}.
 */
public class ReportConfiguration {
    private IHeaderMatcher headerMatcher;
    private ILicenseFamily[] approvedLicenseNames;
    private boolean addingLicenses;
    private boolean addingLicensesForced;
    private String copyrightMessage;
    private boolean approveDefaultLicenses = true;

    /**
     * @return whether default licenses shall be approved by default.
     */
    public boolean isApproveDefaultLicenses() {
        return approveDefaultLicenses;
    }

    public void setApproveDefaultLicenses(boolean approveDefaultLicenses) {
        this.approveDefaultLicenses = approveDefaultLicenses;
    }

    /**
     * Returns the header matcher.
     *
     * @return the header matcher.
     */
    public IHeaderMatcher getHeaderMatcher() {
        return headerMatcher;
    }

    /**
     * Sets the header matcher.
     *
     * @param headerMatcher header matcher.
     */
    public void setHeaderMatcher(IHeaderMatcher headerMatcher) {
        this.headerMatcher = headerMatcher;
    }

    /**
     * Returns the set of approved license names.
     *
     * @return the set of approved license names.
     */
    public ILicenseFamily[] getApprovedLicenseNames() {
        return approvedLicenseNames;
    }

    /**
     * Sets the set of approved license names.
     *
     * @param approvedLicenseNames set of approved license names.
     */
    public void setApprovedLicenseNames(ILicenseFamily[] approvedLicenseNames) {
        this.approvedLicenseNames = approvedLicenseNames;
    }

    /**
     * Sets the set of approved license names (convenience).
     *
     * @param approvedLicenseNames set of approved license names.
     */
    public void setApprovedLicenseNames(List<ILicenseFamily> approvedLicenseNames) {
        if (approvedLicenseNames != null && approvedLicenseNames.size() > 0) {
            setApprovedLicenseNames(approvedLicenseNames.toArray(new ILicenseFamily[approvedLicenseNames.size()]));
        }
    }

    /**
     * @return If Rat is adding license headers: Returns the optional
     * copyright message. This value is ignored, if no
     * license headers are added.
     * @see #isAddingLicenses()
     */
    public String getCopyrightMessage() {
        return copyrightMessage;
    }

    /**
     * If Rat is adding license headers: Sets the optional
     * copyright message. This value is ignored, if no
     * license headers are added.
     *
     * @param copyrightMessage message to set.
     * @see #setAddingLicenses(boolean)
     */
    public void setCopyrightMessage(String copyrightMessage) {
        this.copyrightMessage = copyrightMessage;
    }

    /**
     * @return If Rat is adding license headers: Returns, whether adding
     * license headers is enforced. This value is ignored, if no
     * license headers are added.
     * @see #isAddingLicenses()
     */
    public boolean isAddingLicensesForced() {
        return addingLicensesForced;
    }

    /**
     * If Rat is adding license headers: Sets, whether adding
     * license headers is enforced. This value is ignored, if no
     * license headers are added.
     *
     * @param addingLicensesForced enable/disable forcibly adding licenses.
     * @see #isAddingLicenses()
     */
    public void setAddingLicensesForced(boolean addingLicensesForced) {
        this.addingLicensesForced = addingLicensesForced;
    }

    /**
     * @return Returns, whether Rat should add missing license headers.
     * @see #isAddingLicensesForced()
     * @see #getCopyrightMessage()
     */
    public boolean isAddingLicenses() {
        return addingLicenses;
    }

    /**
     * Returns, whether Rat should add missing license headers.
     *
     * @param addingLicenses enables/disables adding of licenses.
     * @see #setAddingLicensesForced(boolean)
     * @see #setCopyrightMessage(String)
     */
    public void setAddingLicenses(boolean addingLicenses) {
        this.addingLicenses = addingLicenses;
    }

}
