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
     * @return the notes associated with this license.  May be null or empty.
     */
    String getNotes();

    /**
     * @return the id of a licens that this license is derived from. May be null.
     */
    String derivedFrom();

    /**
     * @return An ILicense.Builder instance.
     */
    public static ILicense.Builder builder() {
        return new ILicenseBuilder();
    }

    /**
     * Search a SortedSet of licenses for the matching license id.
     * 
     * @param licenseId the id to search for.
     * @param licenses the SortedSet of licenses to search.
     * @return the matching license or {@code null} if not found.
     */
    static ILicense search(String licenseId, SortedSet<ILicense> licenses) {
        ILicenseFamily searchFamily = ILicenseFamily.builder().setLicenseFamilyCategory(licenseId)
                .setLicenseFamilyCategory("searching proxy").build();
        ILicense target = new ILicense() {

            @Override
            public String getId() {
                return licenseId;
            }

            @Override
            public void reset() {
                // do nothing
            }

            @Override
            public State matches(String line) {
                return State.f;
            }

            @Override
            public int compareTo(ILicense arg0) {
                return searchFamily.compareTo(arg0.getLicenseFamily());
            }

            @Override
            public ILicenseFamily getLicenseFamily() {
                return searchFamily;
            }

            @Override
            public String getNotes() {
                return null;
            }

            @Override
            public String derivedFrom() {
                return null;
            }

            @Override
            public State finalizeState() {
                return State.f;
            }

            @Override
            public State currentState() {
                return State.f;
            }

        };
        return search(target, licenses);
    }

    /**
     * Search a SortedSet of licenses for the matching license.
     * 
     * @param target the license to search for.
     * @param licenses the SortedSet of licenses to search.
     * @return the matching license or {@code null} if not found.
     */
    static ILicense search(ILicense target, SortedSet<ILicense> licenses) {
        SortedSet<ILicense> part = licenses.tailSet(target);
        return (!part.isEmpty() && part.first().compareTo(target) == 0) ? part.first() : null;
    }

    /**
     * @return The comparator for used to sort Licenses. 
     */
    static Comparator<ILicense> getComparator() {
        return (x, y) -> x.getLicenseFamily().compareTo(y.getLicenseFamily());
    }

    /**
     * The definition of a ILicense Builder.
     */
    interface Builder {
        /**
         * @return A new License implementatin.
         */
        ILicense build();
        
        /**
         * Sets the matcher from a builder.
         * @param matcher the builder for the matcher for the license.
         * @return this builder for chaining.
         */
        Builder setMatcher(IHeaderMatcher.Builder matcher);
        
        /**
         * Sets the matcher.
         * @param matcher the matcher for the license.
         * @return this builder for chaining.
         */
        Builder setMatcher(IHeaderMatcher matcher);

        /**
         * Sets the notes for the license.
         * If called multiple times the notes are concatenated to create a single note.
         * @param notes the notes for the license.
         * @return this builder for chaining.
         */
        Builder setNotes(String notes);

        /**
         * Sets the derived from fields in the license.
         * @param derivedFrom the family category of the license this license was derived from.
         * @return this builder for chaining.
         */
        Builder setDerivedFrom(String derivedFrom);

        /**
         * Set the family category for this license.
         * The category must be unique across all licenses and must be 5 characters. If more than 
         * 5 characters are provided then only the first 5 are taken.  If fewer than 5 characters are provided
         * the category is padded with spaces.
         * @param licenseFamilyCategory the family category for the license.
         * @return this builder for chaining.
         */
        Builder setLicenseFamilyCategory(String licenseFamilyCategory);

        /**
         * Set the family name for this license.
         * @param licenseFamilyName the family name for the license.
         * @return this builder for chaining.
         */
        Builder setLicenseFamilyName(String licenseFamilyName);
    }

}
