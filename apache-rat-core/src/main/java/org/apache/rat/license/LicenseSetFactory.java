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

import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Class to take a set of ILicenses and collection of approved license
 * categories and extract Subsets.
 */
public class LicenseSetFactory {

    /**
     * An enum that defines the types of Licenses to extract.
     */
    public enum LicenseFilter {
        /** All defined licenses are returned */
        all,
        /** Only approved licenses are returned */
        approved,
        /** No licenses are returned */
        none;

        /**
         * Converts from a String to an enum value.
         * @param s String representation.
         * @return given licenseFilter for the given String representation.
         */
        static public LicenseFilter fromText(String s) {
            return LicenseFilter.valueOf(s.toLowerCase());
        }
    }

    private final SortedSet<ILicense> licenses;
    private final Collection<String> approvedLicenses;

    /**
     * Constructs a factory with the specified set of Licenses and the approved
     * license collection.
     * @param licenses the set of defined licenses.
     * @param approvedLicenses the list of approved licenses.
     */
    public LicenseSetFactory(SortedSet<ILicense> licenses, Collection<String> approvedLicenses) {
        this.licenses = licenses;
        this.approvedLicenses = approvedLicenses;
    }

    /**
     * Create an empty sorted Set with proper comparator.
     * @return An empty sorted set of ILicense objects.
     */
    public static SortedSet<ILicense> emptyLicenseSet() {
        return new TreeSet<>(ILicense.getComparator());
    }

    /**
     * Create a sorted set of licenses families from the collection.
     * @param licenses the collection of all licenses.
     * @return a SortedSet of license families from the collection.
     */
    private static SortedSet<ILicenseFamily> extractFamily(Collection<ILicense> licenses) {
        SortedSet<ILicenseFamily> result = new TreeSet<>();
        licenses.stream().map(ILicense::getLicenseFamily).forEach(result::add);
        return result;
    }

    /**
     * Gets the License objects based on the filter.
     * @param filter the types of LicenseFamily objects to return.
     * @return a SortedSet of ILicense objects.
     */
    public SortedSet<ILicense> getLicenses(LicenseFilter filter) {
        switch (filter) {
        case all:
            return Collections.unmodifiableSortedSet(licenses);
        case approved:
            SortedSet<ILicense> result = LicenseSetFactory.emptyLicenseSet();
            licenses.stream().filter(x -> approvedLicenses.contains(x.getLicenseFamily().getFamilyCategory()))
                    .forEach(result::add);
            return result;
        case none:
        default:
            return Collections.emptySortedSet();
        }
    }

    /**
     * Gets the LicenseFamily objects based on the filter.
     * @param filter the types of LicenseFamily objects to return.
     * @return a SortedSet of ILicenseFamily objects.
     */
    public SortedSet<ILicenseFamily> getLicenseFamilies(LicenseFilter filter) {
        switch (filter) {
        case all:
            return extractFamily(licenses);
        case approved:
            SortedSet<ILicenseFamily> result = LicenseFamilySetFactory.emptyLicenseFamilySet();
            licenses.stream().map(ILicense::getLicenseFamily)
                    .filter(x -> approvedLicenses.contains(x.getFamilyCategory())).forEach(result::add);
            return result;
        case none:
        default:
            return Collections.emptySortedSet();
        }
    }

    /**
     * Gets the categories of LicenseFamily objects based on the filter.
     * @param filter the types of LicenseFamily objects to return.
     * @return a SortedSet of ILicenseFamily categories.
     */
    public SortedSet<String> getLicenseFamilyIds(LicenseFilter filter) {
        SortedSet<String> result = new TreeSet<>();
        switch (filter) {
        case all:
            licenses.stream().map(x -> x.getLicenseFamily().getFamilyCategory()).forEach(result::add);
            break;
        case approved:
            result.addAll(approvedLicenses);
            break;
        case none:
        default:
            // do nothing
        }
        return result;
    }

    /**
     * Search a SortedSet of licenses for the matching license id.
     *
     * @param licenseId the id to search for.
     * @param licenses the SortedSet of licenses to search.
     * @return the matching license or {@code null} if not found.
     */
    public static ILicense search(String licenseId, SortedSet<ILicense> licenses) {
        ILicenseFamily searchFamily = ILicenseFamily.builder().setLicenseFamilyCategory(licenseId)
                .setLicenseFamilyName("searching proxy").build();
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
            public String getName() {
                return searchFamily.getFamilyName();
            }

            @Override
            public State finalizeState() {
                return State.f;
            }

            @Override
            public State currentState() {
                return State.f;
            }

            @Override
            public Description getDescription() {
                return new ILicenseDescription(this, null);
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
    public static ILicense search(ILicense target, SortedSet<ILicense> licenses) {
        SortedSet<ILicense> part = licenses.tailSet(target);
        return (!part.isEmpty() && part.first().compareTo(target) == 0) ? part.first() : null;
    }

}
