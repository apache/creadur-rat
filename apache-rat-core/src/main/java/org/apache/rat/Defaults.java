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
import java.util.function.Supplier;

import org.apache.commons.io.function.IOSupplier;
import org.apache.rat.configuration.LicenseReader;
import org.apache.rat.configuration.Readers;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;

/**
 * Utility class that holds constants shared by the CLI tool and the Ant tasks.
 */
public class Defaults {
    private static final URL DEFAULT_CONFIG_URL = Defaults.class.getResource("/org/apache/rat/default.xml");
    public static final String PLAIN_STYLESHEET = "org/apache/rat/plain-rat.xsl";
    public static final String UNAPPROVED_LICENSES_STYLESHEET = "org/apache/rat/unapproved-licenses.xsl";

    private final SortedSet<ILicense> licenses;
    private final SortedSet<String> approvedLicenseIds;

    /**
     * Builder constructs instances.
     */
    private Defaults() {
        licenses = new TreeSet<>(ILicense.getComparator());
        approvedLicenseIds = new TreeSet<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public void clear() {
        licenses.clear();
        approvedLicenseIds.clear();
    }

    private void readConfigFiles(Collection<URL> urls) {
        for (URL url : urls) {
            LicenseReader reader = Readers.get(url);
            licenses.addAll(reader.readLicenses());
            reader.approvedLicenseId().stream().map(ILicenseFamily::makeCategory).forEach(approvedLicenseIds::add);
        }
    }

    public static IOSupplier<InputStream> getPlainStyleSheet() {
        return ()->Defaults.class.getClassLoader().getResourceAsStream(Defaults.PLAIN_STYLESHEET);
    }

    public static IOSupplier<InputStream> getUnapprovedLicensesStyleSheet() {
        return ()->Defaults.class.getClassLoader().getResourceAsStream(Defaults.UNAPPROVED_LICENSES_STYLESHEET);
    }

    public SortedSet<ILicense> getLicenses() {
        return Collections.unmodifiableSortedSet(licenses);
    }

    public SortedSet<String> getLicenseIds() {
        if (approvedLicenseIds.isEmpty()) {
            SortedSet<String> result = new TreeSet<>();
            licenses.stream().map(x -> x.getLicenseFamily().getFamilyCategory()).forEach(result::add);
            return result;
        }
        return Collections.unmodifiableSortedSet(approvedLicenseIds);
    }

    public static class Builder {
        private Set<URL> fileNames = new TreeSet<>(new Comparator<URL>() {

            @Override
            public int compare(URL url1, URL url2) {
                return url1.toString().compareTo(url2.toString());
            }
        });

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
         * Builds the defaults object.
         */
        public Defaults build() {
            Defaults result = new Defaults();
            result.readConfigFiles(fileNames);
            return result;
        }
    }
}
