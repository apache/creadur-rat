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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.rat.api.Document;

/**
 * This class provides a numerical overview about
 * the report.
 */
public class ClaimStatistic {
    /** The counter types */
    public enum Counter { 
        /** count of approved files */
        APPROVED, 
        /** count of unapproved files */
        UNAPPROVED, 
        /** count of generated files */
        GENERATED, 
        /** count of unknown files */
        UNKNOWN }
    
    protected final Map<String, int[]> licenseFamilyNameMap = new HashMap<>();
    protected final Map<String, int[]> licenseFamilyCodeMap = new HashMap<>();
    protected final Map<Document.Type, int[]> documentCategoryMap = new HashMap<>();
    protected final Map<ClaimStatistic.Counter, int[]> counterMap = new HashMap<>();


    /**
     * Returns the counts for the counter.
     * @param counter the counter to get the value for.
     * @return Returns the number of files with approved licenses.
     */
    public int getCounter(Counter counter) {
        int[] count = counterMap.get(counter);
        return count == null ? 0 : count[0];
    }

    public void incCounter(Counter key, int value) {
        final int[] num = counterMap.get(key);

        if (num == null) {
            counterMap.put(key, new int[] { value });
        } else {
            num[0] += value;
        }
    }

    /**
     * Returns the counts for the counter.
     * @param documentType the document type to get the counter for.
     * @return Returns the number of files with approved licenses.
     */
    public int getCounter(Document.Type documentType) {
        int[] count = documentCategoryMap.get(documentType);
        return count == null ? 0 : count[0];
    }

    public void incCounter(Document.Type documentType, int value) {
        final int[] num = documentCategoryMap.get(documentType);

        if (num == null) {
            documentCategoryMap.put(documentType, new int[] { value });
        } else {
            num[0] += value;
        }
    }

    public int getLicenseFamilyCount(String licenseFamilyName) {
        int[] count = licenseFamilyCodeMap.get(licenseFamilyName);
        return count == null ? 0 : count[0];
    }

    public void incLicenseFamilyCount(String licenseFamilyName, int value) {
        final int[] num = licenseFamilyCodeMap.get(licenseFamilyName);

        if (num == null) {
            licenseFamilyCodeMap.put(licenseFamilyName, new int[] { value });
        } else {
            num[0] += value;
        }
    }

    public Set<String> getLicenseFamilyNames() {
        return Collections.unmodifiableSet(licenseFamilyCodeMap.keySet());
    }

    public Set<String> getLicenseFileNames() {
        return Collections.unmodifiableSet(licenseFamilyNameMap.keySet());
    }

    public int getLicenseFileNameCount(String licenseFilename) {
        int[] count = licenseFamilyNameMap.get(licenseFilename);
        return count == null ? 0 : count[0];
    }

    public void incLicenseFileNameCount(String licenseFileNameName, int value) {
        final int[] num = licenseFamilyNameMap.get(licenseFileNameName);

        if (num == null) {
            licenseFamilyNameMap.put(licenseFileNameName, new int[] { value });
        } else {
            num[0] += value;
        }
    }
}
