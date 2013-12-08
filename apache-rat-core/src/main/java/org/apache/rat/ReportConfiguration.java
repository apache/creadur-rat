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

/**
 * A configuration object is used by the frontend to invoke the {@link Report}.
 * Basically, the sole purpose of the frontends is to create the configuration
 * and invoke the {@link Report}.
 */
public class ReportConfiguration {

	/** The header matcher. */
	private IHeaderMatcher headerMatcher;

	/** The approved license names. */
	private ILicenseFamily[] approvedLicenseNames;

	/** The adding licenses. */
	private boolean addingLicenses;

	/** The adding licenses forced. */
	private boolean addingLicensesForced;

	/** The copyright message. */
	private String copyrightMessage;

	/**
	 * Returns the header matcher.
	 * 
	 * @return the header matcher
	 */
	public IHeaderMatcher getHeaderMatcher() {
		return headerMatcher;
	}

	/**
	 * Sets the header matcher.
	 * 
	 * @param headerMatcher
	 *            the new header matcher
	 */
	public void setHeaderMatcher(IHeaderMatcher headerMatcher) {
		this.headerMatcher = headerMatcher;
	}

	/**
	 * Returns the set of approved license names.
	 * 
	 * @return the approved license names
	 */
	public ILicenseFamily[] getApprovedLicenseNames() {
		return approvedLicenseNames;
	}

	/**
	 * Sets the set of approved license names.
	 * 
	 * @param approvedLicenseNames
	 *            the new approved license names
	 */
	public void setApprovedLicenseNames(ILicenseFamily[] approvedLicenseNames) {
		this.approvedLicenseNames = approvedLicenseNames;
	}

	/**
	 * If Rat is adding license headers: Returns the optional copyright message.
	 * This value is ignored, if no license headers are added.
	 * 
	 * @return the copyright message
	 * @see #isAddingLicenses()
	 */
	public String getCopyrightMessage() {
		return copyrightMessage;
	}

	/**
	 * If Rat is adding license headers: Sets the optional copyright message.
	 * This value is ignored, if no license headers are added.
	 * 
	 * @param copyrightMessage
	 *            the new copyright message
	 * @see #setAddingLicenses(boolean)
	 */
	public void setCopyrightMessage(String copyrightMessage) {
		this.copyrightMessage = copyrightMessage;
	}

	/**
	 * If Rat is adding license headers: Returns, whether adding license headers
	 * is enforced. This value is ignored, if no license headers are added.
	 * 
	 * @return true, if is adding licenses forced
	 * @see #isAddingLicenses()
	 */
	public boolean isAddingLicensesForced() {
		return addingLicensesForced;
	}

	/**
	 * If Rat is adding license headers: Sets, whether adding license headers is
	 * enforced. This value is ignored, if no license headers are added.
	 * 
	 * @param addingLicensesForced
	 *            the new adding licenses forced
	 * @see #isAddingLicenses()
	 */
	public void setAddingLicensesForced(boolean addingLicensesForced) {
		this.addingLicensesForced = addingLicensesForced;
	}

	/**
	 * Returns, whether Rat should add missing license headers.
	 * 
	 * @return true, if is adding licenses
	 * @see #isAddingLicensesForced()
	 * @see #getCopyrightMessage()
	 */
	public boolean isAddingLicenses() {
		return addingLicenses;
	}

	/**
	 * Returns, whether Rat should add missing license headers.
	 * 
	 * @param addingLicenses
	 *            the new adding licenses
	 * @see #setAddingLicensesForced(boolean)
	 * @see #setCopyrightMessage(String)
	 */
	public void setAddingLicenses(boolean addingLicenses) {
		this.addingLicenses = addingLicenses;
	}

}
