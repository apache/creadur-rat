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
 */package org.apache.rat.license;

import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.rat.license.LicenseSetFactory.LicenseFilter;

/**
 * Class to take a set of ILicenses and collection of approved license categories and extract Subsets.
 */
public class LicenseFamilySetFactory {

    private final SortedSet<ILicenseFamily> families;
    private final Collection<String> approvedLicenses;
    
    /**
     * Constructs a factory with the specified set of Licenses and the approved license collection.
     * @param licenses the set of defined licenses.
     * @param approvedLicenses the list of approved licenses.
     */
    public LicenseFamilySetFactory(SortedSet<ILicenseFamily> licenses, Collection<String> approvedLicenses) {
        this.families = licenses;
        this.approvedLicenses = approvedLicenses;
    }
    
    /**
     * Create an empty sorted Set with proper comparator.
     * @return An empty sorted set of ILicenseFamily objects.
     */
    public static SortedSet<ILicenseFamily> emptyLicenseFamilySet() {
        return new TreeSet<>();
    }


    /**
     * Gets the License objects based on the filter.
     * @param filter the types of LicenseFamily objects to return.
     * @return a SortedSet of ILicense objects.
     */
    public SortedSet<ILicenseFamily> getFamilies(LicenseFilter filter) {
        switch (filter) {
        case ALL:
            return Collections.unmodifiableSortedSet(families);
        case APPROVED:
            SortedSet<ILicenseFamily> result = emptyLicenseFamilySet();
            families.stream().filter(x -> approvedLicenses.contains(x.getFamilyCategory()))
                    .forEach(result::add);
            return result;
        case NONE:
        default:
            return Collections.emptySortedSet();
        }
    }
    
    
    /**
     * Gets the categories of LicenseFamily objects based on the filter.
     * @param filter the types of LicenseFamily objects to return.
     * @return a SortedSet of ILicenseFamily categories.
     */
    public SortedSet<String> getFamilyIds(LicenseFilter filter) {
        SortedSet<String> result = new TreeSet<>();
        switch (filter) {
        case ALL:
            families.stream().map(ILicenseFamily::getFamilyCategory)
                    .forEach(result::add);
            break;
        case APPROVED:
            result.addAll(approvedLicenses);
            break;
        case NONE:
        default:
            // do nothing
        }
        return result;
    }

    
    /**
     * Search a SortedSet of ILicenseFamily instances looking for a matching instance.
     * @param target The instance to search for.
     * @param licenseFamilies the license families to search
     * @return the matching instance of the target given.
     */
    public static ILicenseFamily search(String target, SortedSet<ILicenseFamily> licenseFamilies) {
        ILicenseFamily family = ILicenseFamily.builder().setLicenseFamilyCategory(target).setLicenseFamilyName("Searching family")
                .build();
        return search( family, licenseFamilies);
    }

    /**
     * Search a SortedSet of ILicenseFamily instances looking for a matching instance.
     * @param target The instance to search for.
     * @param licenseFamilies the license families to search
     * @return the matching instance of the target given.
     */
    public static ILicenseFamily search(ILicenseFamily target, SortedSet<ILicenseFamily> licenseFamilies) {
        SortedSet<ILicenseFamily> part = licenseFamilies.tailSet(target);
        return (!part.isEmpty() && part.first().compareTo(target) == 0) ? part.first() : null;
    }
    
}
