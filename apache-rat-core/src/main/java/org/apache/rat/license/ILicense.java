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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.config.parameters.Component;
import org.apache.rat.config.parameters.DescriptionImpl;

/**
 * The definition of a License.
 */
public interface ILicense extends IHeaderMatcher, Comparable<ILicense>, Component {
    /**
     * @return the ILicenseFamily implementation for this license.
     */
    ILicenseFamily getLicenseFamily();

    /**
     * @return the notes associated with this license. May be null or empty.
     */
    String getNotes();

    /**
     * @return the id of a license that this license is derived from. May be null.
     */
    String derivedFrom();

    /**
     * Returns the name of this license. If no name was specified then the name of
     * the family is returned.
     * @return the name of this license.
     */
    String getName();

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

    class ILicenseDescription extends DescriptionImpl {
        private ILicense self;
        private IHeaderMatcher matcher;

        Description[] children = {
                new DescriptionImpl(Type.Parameter, "name", "The name of this license", self::getName),
                new DescriptionImpl(Type.Parameter, "id", "The id of this license", self::getId),
                new DescriptionImpl(Type.Parameter, "family", "The family this license belongs to",
                        self.getLicenseFamily()::getFamilyCategory),
                new DescriptionImpl(Type.Parameter, "notes", "Any notes related to this family", self::getNotes), };

        public ILicenseDescription(ILicense license, IHeaderMatcher matcher) {
            super(Type.License, "license", "A license definition", null);
            self = license;
            this.matcher = matcher;
        }

        @Override
        public Collection<Description> getChildren() {
            List<Description> result = new ArrayList<>();
            result.addAll(Arrays.asList(children));
            result.add(matcher.getDescription());
            return result;
        }

    }

    /**
     * A builder for ILicense instances.
     */
    class Builder {

        private IHeaderMatcher.Builder matcher;

        private String notes;

        private String derivedFrom;

        private String name;

        private String id;

        private final ILicenseFamily.Builder licenseFamily = ILicenseFamily.builder();

        /**
         * Sets the matcher from a builder.
         * @param matcher the builder for the matcher for the license.
         * @return this builder for chaining.
         */
        public Builder setMatcher(IHeaderMatcher.Builder matcher) {
            this.matcher = matcher;
            return this;
        }

        /**
         * Sets the matcher.
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
         * @param id the ID for the license
         * @return this builder for chaining.
         */
        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the derived from fields in the license.
         * @param derivedFrom the family category of the license this license was
         * derived from.
         * @return this builder for chaining.
         */
        public Builder setDerivedFrom(String derivedFrom) {
            this.derivedFrom = derivedFrom;
            return this;
        }

        /**
         * Set the family category for this license. The category must be unique across
         * all licenses and must be 5 characters. If more than 5 characters are provided
         * then only the first 5 are taken. If fewer than 5 characters are provided the
         * category is padded with spaces.
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
            return new SimpleLicense(family, matcher.build(), derivedFrom, notes, name, id);
        }
    }
}
