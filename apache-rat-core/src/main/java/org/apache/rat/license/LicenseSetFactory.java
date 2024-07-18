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
import org.apache.rat.utils.Log;
import org.apache.rat.utils.ReportingSet;

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
    public static ILicenseFamily familySearch(final String target, final SortedSet<ILicenseFamily> licenseFamilies) {
        ILicenseFamily family = ILicenseFamily.builder().setLicenseFamilyCategory(target).setLicenseFamilyName("Searching family")
                .build();
        return familySearch(family, licenseFamilies);
    }

    /**
     * Search a SortedSet of ILicenseFamily instances looking for a matching instance.
     * @param target The instance to search for.
     * @param licenseFamilies the license families to search
     * @return the matching instance of the target given.
     */
    public static ILicenseFamily familySearch(final ILicenseFamily target, final SortedSet<ILicenseFamily> licenseFamilies) {
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
    }

    /** The set of defined families. */
    private final ReportingSet<ILicenseFamily> families;
    /** The set of defined licenses */
    private final ReportingSet<ILicense> licenses;

    /** The set of approved license family categories. If the category is not listed the family is not approved.  */
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
     */
    public LicenseSetFactory() {
        families = new ReportingSet<>(new TreeSet<ILicenseFamily>())
                .setMsgFormat(s -> String.format("Duplicate LicenseFamily category: %s", s.getFamilyCategory()));
        licenses = new ReportingSet<>(new TreeSet<ILicense>())
                .setMsgFormat(s -> String.format("Duplicate License %s (%s) of type %s", s.getName(), s.getId(), s.getLicenseFamily().getFamilyCategory()));

        approvedLicenseCategories = new TreeSet<>();
        removedLicenseCategories = new TreeSet<>();
        approvedLicenseIds = new TreeSet<>();
        removedLicenseIds = new TreeSet<>();
    }

    /**
     * Constructs a factory with the specified set of Licenses and the approved
     * license collection.
     * @param licenses the set of defined licenses. Families will be extracted from the licenses.
     */
    public LicenseSetFactory(final SortedSet<ILicense> licenses) {
        this();
        this.licenses.addAll(licenses);
        licenses.forEach(l -> families.addIfNotPresent(l.getLicenseFamily()));
    }

    public void add(final LicenseSetFactory other) {
        this.families.addAll(other.families);
        this.licenses.addAll(other.licenses);
        this.approvedLicenseCategories.addAll(other.approvedLicenseCategories);
        this.removedLicenseCategories.addAll(other.removedLicenseCategories);
        this.approvedLicenseIds.addAll(other.approvedLicenseIds);
        this.removedLicenseIds.addAll(other.removedLicenseIds);
    }

    /**
     * Set the log level for reporting collisions in the set of license families.
     * <p>NOTE: should be set before licenses or license families are added.</p>
     * @param level The log level to use.
     */
    public void logFamilyCollisions(final Log.Level level) {
        families.setLogLevel(level);
    }

    /**
     * Sets the reporting option for duplicate license families.
     * @param state The ReportingSet.Option to use for reporting.
     */
    public void familyDuplicateOption(final ReportingSet.Options state) {
        families.setDuplicateOption(state);
    }

    /**
     * Sets the log level for reporting license collisions.
     * @param level The log level.
     */
    public void logLicenseCollisions(final Log.Level level) {
        licenses.setLogLevel(level);
    }

    /**
     * Sets the reporting option for duplicate licenses.
     * @param state the ReportingSt.Option to use for reporting.
     */
    public void licenseDuplicateOption(final ReportingSet.Options state) {
        licenses.setDuplicateOption(state);
    }


    /**
     * Create a sorted set of licenses families from the collection.
     * @param licenses the collection of all licenses.
     * @return a SortedSet of license families from the collection.
     */
    private static SortedSet<ILicenseFamily> extractFamily(final Collection<ILicense> licenses) {
        SortedSet<ILicenseFamily> result = new TreeSet<>();
        licenses.stream().map(ILicense::getLicenseFamily).forEach(result::add);
        return result;
    }

    /**
     * Adds a license to the list of licenses. Does not add the license to the list
     * of approved licenses.
     * @param license The license to add to the list of licenses.
     */
    public void addLicense(final ILicense license) {
        if (license != null) {
            this.licenses.add(license);
            this.families.addIfNotPresent(license.getLicenseFamily());
        }
    }

    /**
     * Adds a license to the list of licenses. Does not add the license to the list
     * of approved licenses.
     * @param builder The license builder to build and add to the list of licenses.
     * @return The ILicense implementation that was added.
     */
    public ILicense addLicense(final ILicense.Builder builder) {
        if (builder != null) {
            ILicense license = builder.setLicenseFamilies(families).build();
            this.licenses.add(license);
            return license;
        }
        return null;
    }

    /**
     * Adds multiple licenses to the list of licenses. Does not add the licenses to
     * the list of approved licenses.
     * @param licenses The licenses to add.
     */
    public void addLicenses(final Collection<ILicense> licenses) {
        this.licenses.addAll(licenses);
        licenses.stream().map(ILicense::getLicenseFamily).forEach(families::add);
    }

    /**
     * Adds a license family to the list of families. Does not add the family to the
     * list of approved licenses.
     * @param family The license family to add to the list of license families.
     */
    public void addFamily(final ILicenseFamily family) {
        if (family != null) {
            this.families.add(family);
        }
    }

    /**
     * Adds a license family to the list of families. Does not add the family to the
     * list of approved licenses.
     * @param builder The licenseFamily.Builder to build and add to the list of
     * licenses.
     */
    public void addFamily(final ILicenseFamily.Builder builder) {
        if (builder != null) {
            this.families.add(builder.build());
        }
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
     * @param family the license family to test, must be in category format.
     * @return return {@code true} if the category is approved.
     */
    private boolean isApprovedCategory(final ILicenseFamily family) {
        return approvedLicenseCategories.contains(family.getFamilyCategory()) && !removedLicenseCategories.contains(family.getFamilyCategory());
    }

    /**
     * Gets the License objects based on the filter.
     * @param filter the types of LicenseFamily objects to return.
     * @return a SortedSet of ILicense objects.
     */
    public SortedSet<ILicense> getLicenses(final LicenseFilter filter) {
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
     * @param filter the types of LicenseFamily objects to return.
     * @return a SortedSet of ILicenseFamily objects.
     */
    public SortedSet<ILicenseFamily> getLicenseFamilies(final LicenseFilter filter) {
        SortedSet<ILicenseFamily> result;
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
    public SortedSet<String> getLicenseCategories(final LicenseFilter filter) {
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
    public SortedSet<String> getLicenseIds(final LicenseFilter filter) {
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
                licenses.stream().filter(approved).forEach(l -> result.add(l.getId()));
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
    public static Optional<ILicense> search(final String familyId, final String licenseId, final SortedSet<ILicense> licenses) {
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
            public boolean matches(final IHeaders headers) {
                return false;
            }

            @Override
            public boolean equals(final Object o) {
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
    public static Optional<ILicense> search(final ILicense target, final SortedSet<ILicense> licenses) {
        SortedSet<ILicense> part = licenses.tailSet(target);
        return Optional.ofNullable((!part.isEmpty() && part.first().compareTo(target) == 0) ? part.first() : null);
    }
}
