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
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.IHeaders;

/**
 * Class to take a set of ILicenses and collection of approved license
 * categories and extract Subsets.
 */
public class LicenseSetFactory {

    /**
     * Search a SortedSet of ILicenseFamily instances looking for a matching instance.
     * @param target The instance to search for.
     * @param licenseFamilies the license families to search
     * @return the matching instance of the target given.
     */
    public static ILicenseFamily familySearch(String target, SortedSet<ILicenseFamily> licenseFamilies) {
        ILicenseFamily family = ILicenseFamily.builder().setLicenseFamilyCategory(target).setLicenseFamilyName("Searching family")
                .build();
        return familySearch( family, licenseFamilies);
    }

    /**
     * Search a SortedSet of ILicenseFamily instances looking for a matching instance.
     * @param target The instance to search for.
     * @param licenseFamilies the license families to search
     * @return the matching instance of the target given.
     */
    public static ILicenseFamily familySearch(ILicenseFamily target, SortedSet<ILicenseFamily> licenseFamilies) {
        SortedSet<ILicenseFamily> part = licenseFamilies.tailSet(target);
        return (!part.isEmpty() && part.first().compareTo(target) == 0) ? part.first() : null;
    }

    /**
     * An enum that defines the types of Licenses to extract.
     */
    public enum LicenseFilter {
        /** All defined licenses are returned */
        ALL,
        /** Only approved licenses are returned */
        APPROVED,
        /** No licenses are returned */
        NONE;

        /**
         * Converts from a String to an enum value.
         * 
         * @param s String representation.
         * @return given licenseFilter for the given String representation.
         */
        static public LicenseFilter fromText(String s) {
            return LicenseFilter.valueOf(s.toUpperCase());
        }
    }

    /**
     * The set of defined families.
     */
    private final SortedSet<ILicenseFamily> families;
    /**
     * The set of defined licenses
     */
    private final SortedSet<ILicense> licenses;
    /**
     * The set of approved license family categories. If the category is not listed the family is not approved.
     */
    private final SortedSet<String> approvedLicenseCategories;
    /**
     * The set of license categories that are to be removed from consideration.  These are categories that were
     * added but should now be removed.
     */
    private final SortedSet<String> removedLicenseCategories;
    /**
     * The set of approved license ids.  This set contains the set of licenses that are explicitly approved even if
     * the family is not.
     */
    private final SortedSet<String> approvedLicenseIds;
    /**
     * The set of license ids that are to be removed from consideration.  This set contains licenses that are to be
     * removed even if the family is approved or if an earlier license approval was granted.
     */
    private final SortedSet<String> removedLicenseIds;

    /**
     * Constructs a factory with the specified set of Licenses and the approved
     * license collection.
     *
     * @param families the set of defined license families.
     * @param licenses the set of defined licenses.
     */
    public LicenseSetFactory(SortedSet<ILicenseFamily> families, SortedSet<ILicense> licenses) {
        this.families = families;
        this.licenses = licenses;
        approvedLicenseCategories = new TreeSet<>();
        removedLicenseCategories = new TreeSet<>();
        approvedLicenseIds = new TreeSet<>();
        removedLicenseIds = new TreeSet<>();
    }

    /**
     * Constructs a factory with the specified set of Licenses and the approved
     * license collection.
     *
     * @param licenses the set of defined licenses.  families will be extracted from the licenses.
     */
    public LicenseSetFactory(SortedSet<ILicense> licenses) {
        this.families = new TreeSet<>();
        this.licenses = licenses;
        licenses.forEach(l -> families.add(l.getLicenseFamily()));
        approvedLicenseCategories = new TreeSet<>();
        removedLicenseCategories = new TreeSet<>();
        approvedLicenseIds = new TreeSet<>();
        removedLicenseIds = new TreeSet<>();
    }

    public void add(LicenseSetFactory other) {
        this.families.addAll(other.families);
        this.licenses.addAll(other.licenses);
        this.approvedLicenseCategories.addAll(other.approvedLicenseCategories);
        this.removedLicenseCategories.addAll(other.removedLicenseCategories);
        this.approvedLicenseIds.addAll(other.approvedLicenseIds);
        this.removedLicenseIds.addAll(other.removedLicenseIds);
    }

    /**
     * Create a sorted set of licenses families from the collection.
     * 
     * @param licenses the collection of all licenses.
     * @return a SortedSet of license families from the collection.
     */
    private static SortedSet<ILicenseFamily> extractFamily(Collection<ILicense> licenses) {
        SortedSet<ILicenseFamily> result = new TreeSet<>();
        licenses.stream().map(ILicense::getLicenseFamily).forEach(result::add);
        return result;
    }

    /**
     * Adds a license family category (id) to the list of approved licenses
     * @param familyCategory the category to add.
     */
    public void addLicenseCategory(final String familyCategory) {
        approvedLicenseCategories.add(ILicenseFamily.makeCategory(familyCategory));
    }

    /**
     * Adds a license family category (id) to the list of approved licenses
     * @param familyCategory the category to add.
     */
    public void removeLicenseCategory(final String familyCategory) {
        removedLicenseCategories.add(ILicenseFamily.makeCategory(familyCategory));
    }

    /**
     * Adds a license family category (id) to the list of approved licenses
     * @param licenseId the license ID to add.
     */
    public void addLicenseId(final String licenseId) {
        approvedLicenseIds.add(licenseId);
    }

    /**
     * Removes a license ID from the list of approved licenses.
     * @param licenseId the license ID to remove.
     */
    public void removeLicenseId(final String licenseId) {
        removedLicenseIds.add(licenseId);
    }

    /**
     * Test for approved family category.
     * @param family the license family to test. must be in category format.
     * @return return {@code true} if the category is approved.
     */
    private boolean isApprovedCategory(final ILicenseFamily family) {
        return approvedLicenseCategories.contains(family.getFamilyCategory()) && !removedLicenseCategories.contains(family.getFamilyCategory());
    }

    /**
     * Gets the License objects based on the filter.
     * 
     * @param filter the types of LicenseFamily objects to return.
     * @return a SortedSet of ILicense objects.
     */
    public SortedSet<ILicense> getLicenses(LicenseFilter filter) {
        Predicate<ILicense> approved =  l -> (isApprovedCategory(l.getLicenseFamily()) ||
                approvedLicenseIds.contains(l.getId())) && !removedLicenseIds.contains(l.getId());

        switch (filter) {
        case ALL:
            return Collections.unmodifiableSortedSet(licenses);
        case APPROVED:
            SortedSet<ILicense> result = new TreeSet<>();
            licenses.stream().filter(approved).forEach(result::add);
            return result;
        case NONE:
        default:
            return Collections.emptySortedSet();
        }
    }

    /**
     * Gets the LicenseFamily objects based on the filter.
     * 
     * @param filter the types of LicenseFamily objects to return.
     * @return a SortedSet of ILicenseFamily objects.
     */
    public SortedSet<ILicenseFamily> getLicenseFamilies(LicenseFilter filter) {
        SortedSet<ILicenseFamily> result ;
        switch (filter) {
        case ALL:
            result = extractFamily(licenses);
            result.addAll(families);
            return result;
        case APPROVED:
            result = new TreeSet<>();
            licenses.stream().map(ILicense::getLicenseFamily).filter(this::isApprovedCategory).forEach(result::add);
            return result;
        case NONE:
        default:
            return Collections.emptySortedSet();
        }
    }

    /**
     * Gets the License ids based on the filter.
     *
     * @param filter the types of License IDs to return.
     * @return The list of all licenses in the category regardless of whether or not it is used by an ILicense implementation.
     */
    public SortedSet<String> getLicenseCategories(LicenseFilter filter) {
        Predicate<ILicense> approved = l -> (isApprovedCategory(l.getLicenseFamily()) ||
                approvedLicenseIds.contains(l.getId())) && !removedLicenseIds.contains(l.getId());
        SortedSet<String> result = new TreeSet<>();
        switch (filter) {
            case ALL:
                licenses.forEach(l -> result.add(l.getLicenseFamily().getFamilyCategory()));
                families.forEach(f -> result.add(f.getFamilyCategory()));
                result.addAll(approvedLicenseCategories);
                result.addAll(removedLicenseCategories);
                return result;
            case APPROVED:
                approvedLicenseCategories.stream().filter(s -> !removedLicenseCategories.contains(s)).forEach(result::add);
                licenses.stream().filter(approved).forEach(l -> result.add(l.getLicenseFamily().getFamilyCategory()));
                families.stream().filter(this::isApprovedCategory).forEach(f -> result.add(f.getFamilyCategory()));
                return result;
            case NONE:
            default:
                return Collections.emptySortedSet();
        }
    }

    /**
     * Gets the License ids based on the filter.
     *
     * @param filter the types of License IDs to return.
     * @return The list of all licenses in the category regardless of whether or not it is used by an ILicense implementation.
     */
    public SortedSet<String> getLicenseIds(LicenseFilter filter) {
        Predicate<ILicense> approved =  l -> (isApprovedCategory(l.getLicenseFamily()) ||
                approvedLicenseIds.contains(l.getId())) && !removedLicenseIds.contains(l.getId());
        SortedSet<String> result = new TreeSet<>();
        switch (filter) {
            case ALL:
                licenses.forEach(l -> result.add(l.getId()));
                result.addAll(approvedLicenseCategories);
                result.addAll(removedLicenseCategories);
                result.addAll(approvedLicenseIds);
                result.addAll(removedLicenseIds);
                return result;
            case APPROVED:
                licenses.stream().filter(approved).forEach(l ->result.add(l.getId()));
                families.stream().filter(this::isApprovedCategory).forEach(f -> result.add(f.getFamilyCategory()));
                approvedLicenseIds.stream().filter(s -> !removedLicenseIds.contains(s)).forEach(result::add);
                return result;
            case NONE:
            default:
                return Collections.emptySortedSet();
        }
    }

    /**
     * Search a SortedSet of licenses for the matching license id.
     *
     * @param licenseId the id to search for.
     * @param licenses the SortedSet of licenses to search.
     * @return the matching license or {@code null} if not found.
     */
    public static Optional<ILicense> search(String familyId, String licenseId, SortedSet<ILicense> licenses) {
        ILicenseFamily searchFamily = ILicenseFamily.builder().setLicenseFamilyCategory(familyId)
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
            public boolean matches(IHeaders headers) {
                return false;
            }

            @Override
            public boolean equals(Object o) {
                return ILicense.equals(this, o);
            }

            @Override
            public int hashCode() {
                return ILicense.hash(this);
            }

            @Override
            public ILicenseFamily getLicenseFamily() {
                return searchFamily;
            }

            @Override
            public String getNote() {
                return null;
            }

            @Override
            public String getName() {
                return searchFamily.getFamilyName();
            }

            @Override
            public IHeaderMatcher getMatcher() {
                return null;
            }

        };
        return search(target, licenses);
    }

    /**
     * Search a SortedSet of licenses for the matching license.
     * License must mach both family code, and license id.
     *
     * @param target the license to search for. Must not be null.
     * @param licenses the SortedSet of licenses to search.
     * @return the matching license or {@code null} if not found.
     */
    public static Optional<ILicense> search(ILicense target, SortedSet<ILicense> licenses) {
        SortedSet<ILicense> part = licenses.tailSet(target);
        return Optional.ofNullable((!part.isEmpty() && part.first().compareTo(target) == 0) ? part.first() : null);
    }
}
