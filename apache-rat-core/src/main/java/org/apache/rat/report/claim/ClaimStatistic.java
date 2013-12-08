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

package org.apache.rat.report.claim;

import java.util.Map;

/**
 * This class provides a numerical overview about the report.
 */
public class ClaimStatistic {

	/** The license family name map. */
	private Map<String, Integer> documentCategoryMap, licenseFamilyCodeMap,
			licenseFamilyNameMap;

	/** The num unknown. */
	private int numApproved, numUnApproved, numGenerated, numUnknown;

	/**
	 * Returns the number of files with approved licenses.
	 * 
	 * @return the num approved
	 */
	public int getNumApproved() {
		return numApproved;
	}

	/**
	 * Sets the number of files with approved licenses.
	 * 
	 * @param pNumApproved
	 *            the new num approved
	 */
	public void setNumApproved(final int pNumApproved) {
		numApproved = pNumApproved;
	}

	/**
	 * Returns the number of files with unapproved licenses. <em>Note:</em> This
	 * might include files with unknown licenses.
	 * 
	 * @return the num un approved
	 * @see #getNumUnknown()
	 */
	public int getNumUnApproved() {
		return numUnApproved;
	}

	/**
	 * Returns the number of files with unapproved licenses. <em>Note:</em> This
	 * might include files with unknown licenses.
	 * 
	 * @param pNumUnApproved
	 *            the new num un approved
	 * @see #setNumUnknown(int)
	 */
	public void setNumUnApproved(final int pNumUnApproved) {
		numUnApproved = pNumUnApproved;
	}

	/**
	 * Returns the number of generated files.
	 * 
	 * @return the num generated
	 */
	public int getNumGenerated() {
		return numGenerated;
	}

	/**
	 * Returns the number of generated files.
	 * 
	 * @param pNumGenerated
	 *            the new num generated
	 */
	public void setNumGenerated(final int pNumGenerated) {
		numGenerated = pNumGenerated;
	}

	/**
	 * Returns the number of files, which are neither generated nor have a known
	 * license header.
	 * 
	 * @return the num unknown
	 */
	public int getNumUnknown() {
		return numUnknown;
	}

	/**
	 * Sets the number of files, which are neither generated nor have a known
	 * license header.
	 * 
	 * @param pNumUnknown
	 *            the new num unknown
	 */
	public void setNumUnknown(final int pNumUnknown) {
		numUnknown = pNumUnknown;
	}

	/**
	 * Sets a map with the file types. The map keys are file type names and the
	 * map values are integers with the number of resources matching the file
	 * type.
	 * 
	 * @param pDocumentCategoryMap
	 *            the document category map
	 */
	public void setDocumentCategoryMap(final Map<String, Integer> pDocumentCategoryMap) {
		documentCategoryMap = pDocumentCategoryMap;
	}

	/**
	 * Returns a map with the file types. The map keys are file type names and
	 * the map values are integers with the number of resources matching the
	 * file type.
	 * 
	 * @return the document category map
	 */
	public Map<String, Integer> getDocumentCategoryMap() {
		return documentCategoryMap;
	}

	/**
	 * Returns a map with the license family codes. The map keys are license
	 * family category names, the map values are integers with the number of
	 * resources matching the license family code.
	 * 
	 * @return the license file code map
	 */
	public Map<String, Integer> getLicenseFileCodeMap() {
		return licenseFamilyCodeMap;
	}

	/**
	 * Sets a map with the license family codes. The map keys are instances of
	 * license family category names and the map values are integers with the
	 * number of resources matching the license family code.
	 * 
	 * @param pLicenseFamilyCodeMap
	 *            the license family code map
	 */
	public void setLicenseFileCodeMap(final Map<String, Integer> pLicenseFamilyCodeMap) {
		licenseFamilyCodeMap = pLicenseFamilyCodeMap;
	}

	/**
	 * Returns a map with the license family codes. The map keys are the names
	 * of the license families and the map values are integers with the number
	 * of resources matching the license family name.
	 * 
	 * @return the license file name map
	 */
	public Map<String, Integer> getLicenseFileNameMap() {
		return licenseFamilyNameMap;
	}

	/**
	 * Returns a map with the license family codes. The map keys are the name of
	 * the license families and the map values are integers with the number of
	 * resources matching the license family name.
	 * 
	 * @param pLicenseFamilyNameMap
	 *            the license family name map
	 */
	public void setLicenseFileNameMap(final Map<String, Integer> pLicenseFamilyNameMap) {
		licenseFamilyNameMap = pLicenseFamilyNameMap;
	}
}
