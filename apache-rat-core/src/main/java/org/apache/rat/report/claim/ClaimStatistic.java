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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
    
    private final ConcurrentHashMap<String, IntCounter> licenseFamilyNameMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, IntCounter> licenseFamilyCategoryMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Document.Type, IntCounter> documentCategoryMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ClaimStatistic.Counter, IntCounter> counterMap = new ConcurrentHashMap<>();

    /** converts null counter to 0.
     *
     * @param counter the Counter to retrieve the value from.
     * @return 0 if counter is {@code null} or counter value otherwise.
     */
    private int getValue(IntCounter counter) {
        return counter == null ? 0 : counter.value();
    }
    /**
     * Returns the counts for the counter.
     * @param counter the counter to get the value for.
     * @return Returns the number times the Counter type was seen.
     */
    public int getCounter(Counter counter) {
        return getValue(counterMap.get(counter));
    }

    /**
     * Increments the counts for hte counter.
     * @param counter the counter to increment.
     * @param value the value to increment the counter by.
     */
    public void incCounter(Counter counter, int value) {
        counterMap.compute(counter, (k,v)-> v == null? new IntCounter().increment(value) : v.increment(value));
    }

    /**
     * Gets the counts for the Document.Type.
     * @param documentType the Document.Type to get the counter for.
     * @return Returns the number times the Document.Type was seen
     */
    public int getCounter(Document.Type documentType) {
        return getValue(documentCategoryMap.get(documentType));
    }

    /**
     * Increments the number of times the Document.Type was seen.
     * @param documentType the Document.Type to increment.
     * @param value the vlaue to increment the counter by.
     */
    public void incCounter(Document.Type documentType, int value) {
        documentCategoryMap.compute(documentType, (k,v)-> v == null? new IntCounter().increment(value) : v.increment(value));
    }

    /**
     * Gets the counts for hte license category.
     * @param licenseFamilyCategory the license family category to get the count for.
     * @return the number of times the license family category was seen.
     */
    public int getLicenseCategoryCount(String licenseFamilyCategory) {
        return getValue(licenseFamilyCategoryMap.get(licenseFamilyCategory));
    }

    /**
     * Increments the number of times a license family category was seen.
     * @param licenseFamilyCategory the License family category to incmrement.
     * @param value the value to increment the count by.
     */
    public void incLicenseCategoryCount(String licenseFamilyCategory, int value) {
        licenseFamilyCategoryMap.compute(licenseFamilyCategory, (k, v)-> v == null? new IntCounter().increment(value) : v.increment(value));
    }

    /**
     * Gets the set of license family categories that were seen.
     * @return A set of license family categories.
     */
    public Set<String> getLicenseFamilyCategories() {
        return Collections.unmodifiableSet(licenseFamilyCategoryMap.keySet());
    }

    /**
     * Gets the set of license family names that were seen.
     * @return a Set of license family names that were seen.
     */
    public Set<String> getLicenseFamilyNames() {
        return Collections.unmodifiableSet(licenseFamilyNameMap.keySet());
    }

    /**
     * Retrieves the number of times a license family name was seen.
     * @param licenseFilename the license family name to look for.
     * @return the number of times the license family name was seen.
     */
    public int getLicenseFamilyNameCount(String licenseFilename) {
        return getValue(licenseFamilyNameMap.get(licenseFilename));
    }

    /**
     * Increments the license family name count.
     * @param licenseFamilyName the license family name to increment.
     * @param value the value to increment the count by.
     */
    public void incLicenseFamilyNameCount(String licenseFamilyName, int value) {
        licenseFamilyNameMap.compute(licenseFamilyName, (k,v)-> v == null? new IntCounter().increment(value) : v.increment(value));
    }

    /**
     * A class that wraps and int and allows easy increment and retrieval.
     */
    static class IntCounter {
        int value = 0;

        /**
         * Increment the count.
         * @param count the count to increment by (may be negative)
         * @return this.
         */
        public IntCounter increment(int count) {
            value += count;
            return this;
        }

        /**
         * Retrieves the count.
         * @return the count contained by this counter.
         */
        public int value() {
            return value;
        }
    }
}
