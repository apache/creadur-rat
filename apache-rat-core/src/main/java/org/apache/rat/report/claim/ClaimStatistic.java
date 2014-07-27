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
 * This class provides a numerical overview about
 * the report.
 */
public class ClaimStatistic {
    private Map<String, Integer> documentCategoryMap, licenseFamilyCodeMap, licenseFamilyNameMap;
    private int numApproved, numUnApproved, numGenerated, numUnknown;

    /**
     * @return Returns the number of files with approved licenses.
     */
    public int getNumApproved() {
        return numApproved;
    }

    /**
     * Sets the number of files with approved licenses.
     * @param pNumApproved number of files with approved licenses.
     */
    public void setNumApproved(int pNumApproved) {
        numApproved = pNumApproved;
    }

    /**
     * @return Returns the number of files with unapproved licenses.
     * <em>Note:</em> This might include files with unknown
     * licenses.
     * @see #getNumUnknown()
     */
    public int getNumUnApproved() {
        return numUnApproved;
    }

    /**
     * Sets the number of files with unapproved licenses.
     * @param pNumUnApproved number of files with unapproved licenses.
     */
    public void setNumUnApproved(int pNumUnApproved) {
        numUnApproved = pNumUnApproved;
    }

    /**
     * @return Returns the number of generated files.
     */
    public int getNumGenerated() {
        return numGenerated;
    }

    /**
     * Sets the number of generated files.
     * @param pNumGenerated the number of generated files.
     */
    public void setNumGenerated(int pNumGenerated) {
        numGenerated = pNumGenerated;
    }

    /**
     * @return Returns the number of files, which are neither
     * generated nor have a known license header.
     */
    public int getNumUnknown() {
        return numUnknown;
    }

    /**
     * Sets the number of files, which are neither
     * generated nor have a known license header.
     * @param pNumUnknown set number of files. 
     */
    public void setNumUnknown(int pNumUnknown) {
        numUnknown = pNumUnknown;
    }

    /**
     * Sets a map with the file types. The map keys
     * are file type names and the map values
     * are integers with the number of resources matching
     * the file type.
     * @param pDocumentCategoryMap doc-category map.
     */
    public void setDocumentCategoryMap(Map<String, Integer> pDocumentCategoryMap) {
        documentCategoryMap = pDocumentCategoryMap;
    }

    /**
     * @return Returns a map with the file types. The map keys
     * are file type names and the map values
     * are integers with the number of resources matching
     * the file type.
     */
    public Map<String, Integer> getDocumentCategoryMap() {
        return documentCategoryMap;
    }

    /**
     * @return Returns a map with the license family codes. The map
     * keys are license family category names,
     * the map values are integers with the number of resources
     * matching the license family code.
     */
    public Map<String, Integer> getLicenseFileCodeMap() {
        return licenseFamilyCodeMap;
    }

    /**
     * Sets a map with the license family codes. The map
     * keys are instances of license family category names and
     * the map values are integers with the number of resources
     * matching the license family code.
     * @param pLicenseFamilyCodeMap license family map.
     */
    public void setLicenseFileCodeMap(Map<String, Integer> pLicenseFamilyCodeMap) {
        licenseFamilyCodeMap = pLicenseFamilyCodeMap;
    }

    /**
     * @return Returns a map with the license family codes. The map
     * keys are the names of the license families and
     * the map values are integers with the number of resources
     * matching the license family name.
     */
    public Map<String, Integer> getLicenseFileNameMap() {
        return licenseFamilyNameMap;
    }

    /**
     * Sets map with the license family codes. The map
     * keys are the name of the license families and
     * the map values are integers with the number of resources
     * matching the license family name.
     * @param pLicenseFamilyNameMap license family-name map.
     */
    public void setLicenseFileNameMap(Map<String, Integer> pLicenseFamilyNameMap) {
        licenseFamilyNameMap = pLicenseFamilyNameMap;
    }
}
