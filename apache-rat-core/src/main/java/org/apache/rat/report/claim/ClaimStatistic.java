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

import java.util.HashMap;
import java.util.Map;

import org.apache.rat.api.Document;


/**
 * This class provides a numerical overview about
 * the report.
 */
public class ClaimStatistic {
    public enum Counter { Approved, Unapproved, Generated, Unknown };
    
    private final Map<String, int[]> licenseFamilyNameMap = new HashMap<>();
    private final Map<String, int[]> licenseFamilyCodeMap = new HashMap<>();
    private final Map<Document.Type, int[]> documentCategoryMap = new HashMap<>();
    private final Map<ClaimStatistic.Counter, int[]> counterMap = new HashMap<>();


    /**
     * @return Returns the number of files with approved licenses.
     */
    public int getCounter(Counter counter) {
        int[] count = counterMap.get(counter);
        return count == null ? 0 : count[0];
    }

    /**
     * @return Returns a map with the file types. The map keys
     * are file type names and the map values
     * are integers with the number of resources matching
     * the file type.
     */
    public Map<Counter, int[]> getCounterMap() {
        return counterMap;
    }

    
    /**
     * @return Returns a map with the file types. The map keys
     * are file type names and the map values
     * are integers with the number of resources matching
     * the file type.
     */
    public Map<Document.Type, int[]> getDocumentCategoryMap() {
        return documentCategoryMap;
    }

    /**
     * @return Returns a map with the license family codes. The map
     * keys are license family category names,
     * the map values are integers with the number of resources
     * matching the license family code.
     */
    public Map<String, int[]> getLicenseFamilyCodeMap() {
        return licenseFamilyCodeMap;
    }

    /**
     * @return Returns a map with the license family codes. The map
     * keys are the names of the license families and
     * the map values are integers with the number of resources
     * matching the license family name.
     */
    public Map<String, int[]> getLicenseFileNameMap() {
        return licenseFamilyNameMap;
    }

}
