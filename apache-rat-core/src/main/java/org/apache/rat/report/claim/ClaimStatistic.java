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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.api.Document;

/**
 * This class provides a numerical overview about
 * the report.
 */
public class ClaimStatistic {
    // keep the counter types in alphabetical order
    /** The counter types */
    public enum Counter {
        /** count of approved files */
        APPROVED("A count of approved licenses.", -1, 0),
        /** count of archive files */
        ARCHIVES("A count of archive files.", -1, 0),
        /** count of binary  files */
        BINARIES("A count of binary files.", -1, 0),
        /** count of distinct document types */
        DOCUMENT_TYPES("A count of distinct document types.", -1, 1),
        /** count of generated/ignored files */
        IGNORED("A count of ignored files.", -1, 0),
        /** count of license categories */
        LICENSE_CATEGORIES("A count of distinct license categories.", -1, 1),
        /** count of distinct license names */
        LICENSE_NAMES("A count of distinct license names.", -1, 1),
        /** count of note files */
        NOTICES("A count of notice files.", -1, 0),
        /** count of standard files */
        STANDARDS("A count of standard files.", -1, 1),
        /** count of unapproved files */
        UNAPPROVED("A count of unapproved licenses.", 0, 0),
        /** count of unknown files */
        UNKNOWN("A count of unknown file types.", -1, 0);

        /** The description of the counter */
        private final String description;
        /** The default max value for the counter */
        private final int defaultMaxValue;
        /** The default minimum value for the counter */
        private final int defaultMinValue;

        Counter(final String description, final int defaultMaxValue, final int defaultMinValue) {
            this.description = description;
            this.defaultMaxValue = defaultMaxValue;
            this.defaultMinValue = defaultMinValue;
        }

        /**
         * Gets the description of the counter.
         * @return The description of the counter.
         */
        public String getDescription() {
            return description;
        }

        /**
         * Gets the default maximum value for the counter.
         * @return the default maximum value for the counter.
         */
        public int getDefaultMaxValue() {
            return defaultMaxValue;
        }
        /**
         * Gets the default minimum value for the counter.
         * @return the default maximum value for the counter.
         */
        public int getDefaultMinValue() {
            return defaultMinValue;
        }

        /**
         * Display name is capitalized and any underscores are replaced by spaces.
         * @return displayName of the counter, capitalized and without underscores.
         */
        public String displayName() {
            return StringUtils.capitalize(name().replaceAll("_", " ").toLowerCase(Locale.ROOT));
        }
    }

    /** Count of license family name to counter */
    private final ConcurrentHashMap<String, IntCounter> licenseNameMap = new ConcurrentHashMap<>();
    /** Map of license family category to counter */
    private final ConcurrentHashMap<String, IntCounter> licenseFamilyCategoryMap = new ConcurrentHashMap<>();
    /** Map of document type to counter */
    private final ConcurrentHashMap<Document.Type, IntCounter> documentTypeMap = new ConcurrentHashMap<>();
    /** Map of counter type to value */
    private final ConcurrentHashMap<ClaimStatistic.Counter, IntCounter> counterMap = new ConcurrentHashMap<>();

    /**
     * Converts null counter to 0.
     *
     * @param counter the Counter to retrieve the value from.
     * @return 0 if counter is {@code null} or counter value otherwise.
     */
    private int getValue(final IntCounter counter) {
        return counter == null ? 0 : counter.value();
    }

    /**
     * Returns the counts for the counter.
     * @param counter the counter to get the value for.
     * @return the number times the counter type was seen.
     */
    public int getCounter(final Counter counter) {
        return getValue(counterMap.get(counter));
    }

    /**
     * Increments the counts for the counter.
     * @param counter the counter to increment.
     * @param value the value to increment the counter by.
     */
    public void incCounter(final Counter counter, final int value) {
        counterMap.compute(counter, (k, v) -> v == null ? new IntCounter().increment(value) : v.increment(value));
    }

    /**
     * Gets the counts for the Document.Type.
     * @param documentType the Document.Type to get the counter for.
     * @return the number times the Document.Type was seen.
     */
    public int getCounter(final Document.Type documentType) {
        return getValue(documentTypeMap.get(documentType));
    }

    /**
     * Gets the set of Document.Types seen in the run.
     * @return the set of Document.Types seen in the run.
     */
    public List<Document.Type> getDocumentTypes() {
        List<Document.Type> result = new ArrayList<>(documentTypeMap.keySet());
        result.sort(Comparator.comparing(Enum::name));
        return result;
    }

    /**
     * Increments the number of times the Document.Type was seen.
     * @param documentType the Document.Type to increment.
     * @param value the value to increment the counter by.
     */
    public void incCounter(final Document.Type documentType, final int value) {
        documentTypeMap.compute(documentType, (k, v) -> updateCounter(Counter.DOCUMENT_TYPES, v, value));
        switch (documentType) {
            case STANDARD:
                incCounter(Counter.STANDARDS, value);
                break;
            case ARCHIVE:
                incCounter(Counter.ARCHIVES, value);
                break;
            case BINARY:
                incCounter(Counter.BINARIES, value);
                break;
            case NOTICE:
                incCounter(Counter.NOTICES, value);
                break;
            case UNKNOWN:
                incCounter(Counter.UNKNOWN, value);
                break;
            case IGNORED:
                incCounter(Counter.IGNORED, value);
                break;
        }
    }

    /**
     * Gets the counts for the license category.
     * @param licenseFamilyCategory the license family category to get the count for.
     * @return the number of times the license family category was seen.
     */
    public int getLicenseCategoryCount(final String licenseFamilyCategory) {
        return getValue(licenseFamilyCategoryMap.get(licenseFamilyCategory));
    }

    /**
     * Gets the counts for the license name.
     * @param licenseName the license name to get the count for.
     * @return the number of times the license family category was seen.
     */
    public int getLicenseNameCount(final String licenseName) {
        return getValue(licenseNameMap.get(licenseName));
    }

    /**
     * Updates the intCounter with the value and if the intCounter was null creates a new one and registers the
     * creation as a counter type.
     * @param counter the Type of the counter.
     * @param intCounter the IntCounter to update. May be null.
     * @param value the value to add to the int counter.
     * @return the intCounter if it was not {@code null}, a new IntCounter otherwise.
     */
    private IntCounter updateCounter(final Counter counter, final IntCounter intCounter, final int value) {
        if (intCounter == null) {
            incCounter(counter, 1);
            return new IntCounter().increment(value);
        } else {
            return intCounter.increment(value);
        }
    }

    /**
     * Increments the number of times a license family category was seen.
     * @param licenseFamilyCategory the License family category to increment.
     * @param value the value to increment the count by.
     */
    public void incLicenseCategoryCount(final String licenseFamilyCategory, final int value) {
        licenseFamilyCategoryMap.compute(licenseFamilyCategory, (k, v) -> updateCounter(Counter.LICENSE_CATEGORIES, v, value));
    }

    /**
     * Gets the set of license family categories that were seen.
     * @return A set of license family categories.
     */
    public List<String> getLicenseFamilyCategories() {
        List<String> result = new ArrayList<>(licenseFamilyCategoryMap.keySet());
        result.sort(String::compareTo);
        return result;
    }

    /**
     * Gets the license names sorted by name.
     * @return sorted list of license names.
     */
    public List<String> getLicenseNames() {
        List<String> result = new ArrayList<>(licenseNameMap.keySet());
        result.sort(String::compareTo);
        return result;
    }

    /**
     * Increments the license family name count.
     * @param licenseName the license name to increment.
     * @param value the value to increment the count by.
     */
    public void incLicenseNameCount(final String licenseName, final int value) {
        licenseNameMap.compute(licenseName, (k, v) -> updateCounter(Counter.LICENSE_NAMES, v, value));
    }

    /**
     * A class that wraps an int and allows easy increment and retrieval.
     */
    static class IntCounter {
        /**
         * The value of the counter
         */
        private int value;

        /**
         * Increment the count.
         * @param count the count to increment by (can be negative).
         * @return this.
         */
        public IntCounter increment(final int count) {
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
