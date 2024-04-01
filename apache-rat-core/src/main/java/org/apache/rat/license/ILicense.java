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

import java.util.Comparator;
import java.util.Objects;
import java.util.SortedSet;

import org.apache.rat.analysis.IHeaderMatcher;

/**
 * The definition of a License.
 */
public interface ILicense extends IHeaderMatcher, Comparable<ILicense> {
    /**
     * @return the ILicenseFamily implementation for this license.
     */
    ILicenseFamily getLicenseFamily();

    /**
     * @return the notes associated with this license. May be null or empty.
     */
    String getNotes();

    /**
     * Returns the name of this license. If no name was specified then the name of
     * the family is returned.
     * 
     * @return the name of this license.
     */
    String getName();

    default String getFamilyName() {
        return getLicenseFamily().getFamilyName();
    }

    IHeaderMatcher getMatcher();

    /**
     * @return An ILicense.Builder instance.
     */
    static ILicense.Builder builder() {
        return new Builder();
    }

    /**
     * @return The comparator for used to sort Licenses.
     */
    static Comparator<ILicense> getComparator() {
        return Comparator.comparing(IHeaderMatcher::getId);
    }

    /**
     * A builder for ILicense instances.
     */
    class Builder {

        private IHeaderMatcher.Builder matcher;

        private String notes;

        private String name;

        private String id;

        private final ILicenseFamily.Builder licenseFamily = ILicenseFamily.builder();

        /**
         * Sets the matcher from a builder.
         * 
         * @param matcher the builder for the matcher for the license.
         * @return this builder for chaining.
         */
        public Builder setMatcher(IHeaderMatcher.Builder matcher) {
            this.matcher = matcher;
            return this;
        }

        /**
         * Sets the matcher.
         * 
         * @param matcher the matcher for the license.
         * @return this builder for chaining.
         */
        public Builder setMatcher(IHeaderMatcher matcher) {
            this.matcher = () -> matcher;
            return this;
        }

        /**
         * Sets the notes for the license. If called multiple times the notes are
         * concatenated to create a single note.
         * 
         * @param notes the notes for the license.
         * @return this builder for chaining.
         */
        public Builder setNotes(String notes) {
            this.notes = notes;
            return this;
        }

        /**
         * Sets the ID of the license. If the ID is not set then the ID of the license
         * family is used.
         * 
         * @param id the ID for the license
         * @return this builder for chaining.
         */
        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        /**
         * Set the family category for this license. The category must be unique across
         * all licenses and must be 5 characters. If more than 5 characters are provided
         * then only the first 5 are taken. If fewer than 5 characters are provided the
         * category is padded with spaces.
         * 
         * @param licenseFamilyCategory the family category for the license.
         * @return this builder for chaining.
         */
        public Builder setLicenseFamilyCategory(String licenseFamilyCategory) {
            this.licenseFamily.setLicenseFamilyCategory(licenseFamilyCategory);
            this.licenseFamily.setLicenseFamilyName("License Family for searching");
            return this;
        }

        /**
         * Sets the name of the license. If the name is not set then the name of the
         * license family is used.
         * 
         * @param name the name for the license
         * @return this builder for chaining.
         */
        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * @param licenseFamilies the set of defined license families.
         * @return A new License implementation.
         */
        public ILicense build(SortedSet<ILicenseFamily> licenseFamilies) {
            Objects.requireNonNull(matcher, "Matcher must not be null");
            ILicenseFamily family = LicenseFamilySetFactory.search(licenseFamily.build(), licenseFamilies);
            Objects.requireNonNull(family, "License family " + licenseFamily.getCategory() + " not found.");
            return new SimpleLicense(family, matcher.build(), notes, name, id);
        }
    }
}
