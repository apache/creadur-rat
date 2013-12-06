/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.api.domain;

/**
 * Builds {@link LicenseFamily} instances.
 */
public final class LicenseFamilyBuilder {

    /**
     * Begins to build a license family.
     * 
     * @return a builder for a license family, not null
     */
    public static LicenseFamilyBuilder aLicenseFamily() {
        return new LicenseFamilyBuilder();
    }

    /** Further information associated with the license family. Human readable. */
    private String notes;
    /**
     * Names of the category containing this license family. Recommended that
     * this be an URI.
     */
    private String category;
    /** Uniquely identifies this family. Choosing a suitable URI is recommended */
    private String name;

    /** Use {@link #aLicenseFamily()  */
    private LicenseFamilyBuilder() {
    }

    /**
     * Builds a family.
     * 
     * @return not null
     */
    public LicenseFamily build() {
        return new LicenseFamily(this.name, this.category, this.notes);
    }

    /**
     * Builds family with further information associated with the license
     * family. Human readable.
     * 
     * @param notes
     *            possibly null
     * @return this instance, not null
     */
    public LicenseFamilyBuilder withNotes(final String notes) {
        this.notes = notes;
        return this;
    }

    /**
     * Builds family with category containing this license family. Choosing a
     * suitable URI is recommended
     * 
     * @param category
     *            possible null
     * @return this instance, not null
     */
    public LicenseFamilyBuilder withCategory(final String category) {
        this.category = category;
        return this;
    }

    /**
     * 
     * Uniquely identifies this family. Choosing a suitable URI is recommended.
     * 
     * @param name
     *            not null
     * @return this instance, not null
     */
    public LicenseFamilyBuilder withName(final String name) {
        this.name = name;
        return this;
    }
}
