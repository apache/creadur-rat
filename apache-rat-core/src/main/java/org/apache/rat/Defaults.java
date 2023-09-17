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
package org.apache.rat;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.rat.configuration.LicenseReader;
import org.apache.rat.configuration.Readers;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.SimpleLicenseFamily;

/**
 * Utility class that holds constants shared by the CLI tool and the Ant tasks.
 */
public class Defaults {
    public enum Filter {
        all, approved, none
    };

    private static final URL DEFAULT_CONFIG_URL = Defaults.class.getResource("/org/apache/rat/default.xml");
    public static final String PLAIN_STYLESHEET = "org/apache/rat/plain-rat.xsl";
    public static final String UNAPPROVED_LICENSES_STYLESHEET = "org/apache/rat/unapproved-licenses.xsl";

    private final Filter approvalFilter;
    private final SortedSet<ILicense> licenses;
    private final SortedSet<ILicenseFamily> licenseFamilies;
    private final SortedSet<ILicenseFamily> approvedLicenseFamilies;

    /**
     * Builder constructs instances.
     */
    private Defaults(Filter approvalFilter) {
        this.approvalFilter = approvalFilter;
        licenses = new TreeSet<>(ILicense.getComparator());
        licenseFamilies = new TreeSet<>();
        approvedLicenseFamilies = new TreeSet<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public void clear() {
        licenses.clear();
        licenseFamilies.clear();
    }

    private void readConfigFiles(Collection<URL> urls) {
        for (URL url : urls) {
            LicenseReader reader = Readers.get(url);
            reader.add(url);
            licenseFamilies.addAll(reader.readFamilies());
            licenses.addAll(reader.readLicenses());
            approvedLicenseFamilies.addAll(reader.approvedLicenseFamilies());
        }
    }

    public static InputStream getPlainStyleSheet() {
        return Defaults.class.getClassLoader().getResourceAsStream(Defaults.PLAIN_STYLESHEET);
    }

    public static InputStream getUnapprovedLicensesStyleSheet() {
        return Defaults.class.getClassLoader().getResourceAsStream(Defaults.UNAPPROVED_LICENSES_STYLESHEET);
    }

    public ILicense createDefaultMatcher() {
        return getLicenses().isEmpty() ? null : new LicenseCollectionMatcher(getLicenses());
    }

    /**
     * Returns the set of defined licenses unless filter is set to no-defaults. if
     * no-defaults was set then an empty set is returned.
     * 
     * @return The set of defined licenses.
     */
    public SortedSet<ILicense> getLicenses() {
        switch (approvalFilter) {
        case all:
        case approved:
            return Collections.unmodifiableSortedSet(licenses);
        case none:
        default:
            return Collections.emptySortedSet();
        }
    }

    public Set<String> getLicenseNames() {
        return getLicenseFamilies().stream().map(ILicenseFamily::getFamilyName).collect(Collectors.toSet());
    }

    public SortedSet<ILicenseFamily> getLicenseFamilies() {
        switch (approvalFilter) {
        case all:
            return Collections.unmodifiableSortedSet(licenseFamilies);
        case approved:
            return Collections.unmodifiableSortedSet(approvedLicenseFamilies);
        case none:
        default:
            return Collections.emptySortedSet();
        }
    }

    public static class Builder {
        private Set<URL> fileNames = new TreeSet<>(new Comparator<URL>() {

            @Override
            public int compare(URL url1, URL url2) {
                return url1.toString().compareTo(url2.toString());
            }
        });

        private Filter approvalFilter = Filter.approved;

        private Builder() {
            fileNames.add(DEFAULT_CONFIG_URL);
        }

        /**
         * Adds a URL defining the Licenses and which are appoved to the list of files
         * to read.
         * 
         * @param url the URL to add
         * @return this Builder for chaining
         */
        public Builder add(URL url) {
            fileNames.add(url);
            return this;
        }

        /**
         * Adds a File defining the Licenses and which are appoved to the list of files
         * to read.
         * 
         * @param fileName the name of the file to add
         * @return this Builder for chaining
         */
        public Builder add(String fileName) throws MalformedURLException {
            return add(new File(fileName));
        }

        /**
         * Adds a file defining the Licenses and which are appoved to the list of files
         * to read.
         * 
         * @param file the File to add
         * @return this Builder for chaining
         */
        public Builder add(File file) throws MalformedURLException {
            return add(file.toURI().toURL());
        }

        /**
         * Removes a file from the list of files to process.
         * 
         * @param url the URL of the file to remove.
         * @return this Builder for chaining
         */
        public Builder remove(URL url) {
            fileNames.remove(url);
            return this;
        }

        /**
         * Removes a file from the list of files to process.
         * 
         * @param fileName the fileName of the file to remove.
         * @return this Builder for chaining
         */
        public Builder remove(String fileName) throws MalformedURLException {
            return remove(new File(fileName));
        }

        /**
         * Removes a file from the list of files to process.
         * 
         * @param file the File of the file to remove.
         * @return this Builder for chaining
         */
        public Builder remove(File file) throws MalformedURLException {
            return remove(file.toURI().toURL());
        }

        /**
         * Removes the default definitions.
         * 
         * @return this Builder for chaining
         */
        public Builder noDefault() {
            return remove(DEFAULT_CONFIG_URL);
        }

        /**
         * Set which files will be listed as approved.
         * 
         * @param filter the fileter type.
         * @return this Builder for chaining
         */
        public Builder setApprovalFilter(Filter filter) {
            approvalFilter = filter;
            return this;
        }

        /**
         * Builds the defaults object.
         */
        public Defaults build() {
            Defaults result = new Defaults(approvalFilter);
            result.readConfigFiles(fileNames);
            return result;
        }
    }

    public static class LicenseCollectionMatcher implements ILicense {

        private Collection<ILicense> enclosed;
        private ILicenseFamily family;

        public LicenseCollectionMatcher(Collection<ILicense> enclosed) {
            family = new SimpleLicenseFamily("", "System License Collection");
            this.enclosed = enclosed;
        }

        @Override
        public String getId() {
            return "Default License Collection";
        }

        @Override
        public void reset() {
            enclosed.stream().forEach(ILicense::reset);
        }

        @Override
        public boolean matches(String line) {
            for (ILicense license : enclosed) {
                if (license.matches(line)) {
                    this.family = license.getLicenseFamily();
                    return true;
                }
            }
            return false;
        }

        @Override
        public ILicenseFamily getLicenseFamily() {
            return family;
        }

        @Override
        public String getNotes() {
            return null;
        }

        @Override
        public ILicense derivedFrom() {
            return null;
        }

        @Override
        public int compareTo(ILicense arg0) {
            return ILicense.getComparator().compare(this, arg0);
        }
    }
}
