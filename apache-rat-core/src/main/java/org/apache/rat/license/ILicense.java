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
package org.apache.rat.license;

import java.util.Objects;
import java.util.SortedSet;

import org.apache.rat.analysis.IHeaderMatcher;

/**
 * The definition of a License.
 */
public interface ILicense extends IHeaderMatcher, Comparable<ILicense> {

    /**
     * Gets the license family.
     *
     * @return the ILicenseFamily implementation for this license.
     */
    ILicenseFamily getLicenseFamily();

    /**
     * Gets the note associated with the license.
     *
     * @return the note associated with this license. May be null or empty.
     */
    String getNote();

    /**
     * Returns the name of this license. If no name was specified then the name of
     * the family is returned.
     *
     * @return the name of this license.
     */
    String getName();

    /**
     * Gets the name of the family that this license if part of.
     *
     * @return the name of the license family that this license is part of.
     */
    default String getFamilyName() {
        return getLicenseFamily().getFamilyName();
    }

    /**
     * Get the header matcher for this license.
     *
     * @return the header matcher for this license.
     */
    IHeaderMatcher getMatcher();

    @Override
    default int compareTo(ILicense other) {
        int result = getLicenseFamily().compareTo(other.getLicenseFamily());
            return result == 0 ? getId().compareTo(other.getId()) : result;
    }

    /**
     * A default implementation of a License hash
     * @param license the license to hash
     * @return the license hash value
     */
    static int hash(ILicense license) {
        return Objects.hash(license.getLicenseFamily(), license.getId());
    }

    /**
     * A default implementation of equals.
     * @param license1 The license to check for equality.
     * @param o the object to check for equality to.
     * @return true if the object is equal to the license1.
     */
    static boolean equals(ILicense license1, Object o) {
        if (license1 == o) {
            return true;
        }
        if (!(o instanceof ILicense that)) {
            return false;
        }
        return license1.compareTo(that) == 0;
    }

    /**
     * Gets a builder for licenses.
     *
     * @return An ILicense.Builder instance.
     */
    static ILicense.Builder builder() {
        return new SimpleLicense.Builder();
    }

    /**
     * A builder for ILicense instances.
     */
    interface Builder extends IHeaderMatcher.Builder {

        /**
         * Sets the matcher from a builder.
         *
         * @param matcher the builder for the matcher for the license.
         * @return this builder for chaining.
         */
        Builder setMatcher(IHeaderMatcher.Builder matcher);

        /**
         * Sets the matcher.
         *
         * @param matcher the matcher for the license.
         * @return this builder for chaining.
         */
        Builder setMatcher(IHeaderMatcher matcher);

        /**
         * Sets the notes for the license. If called multiple times the notes are
         * concatenated to create a single note.
         *
         * @param notes the notes for the license.
         * @return this builder for chaining.
         */
        Builder setNote(String notes);

        /**
         * Sets the ID of the license. If the ID is not set then the ID of the license
         * family is used.
         *
         * @param id the ID for the license
         * @return this builder for chaining.
         */
        Builder setId(String id);

        /**
         * Set the family category for this license. The category must be unique across
         * all licenses and must be 5 characters. If more than 5 characters are provided
         * then only the first 5 are taken. If fewer than 5 characters are provided the
         * category is padded with spaces.
         *
         * @param licenseFamilyCategory the family category for the license.
         * @return this builder for chaining.
         */
        Builder setFamily(String licenseFamilyCategory);

        /**
         * Sets the name of the license. If the name is not set then the name of the
         * license family is used.
         *
         * @param name the name for the license
         * @return this builder for chaining.
         */
        Builder setName(String name);

        /**
         * Sets the set of license families to use during build.
         *
         * @param licenseFamilies the license families to use
         * @return this builder.
         */
        Builder setLicenseFamilies(SortedSet<ILicenseFamily> licenseFamilies);

        /**
         * Builds the license.
         *
         * @return A new License implementation.
         */
        @Override
        ILicense build();
    }
}
