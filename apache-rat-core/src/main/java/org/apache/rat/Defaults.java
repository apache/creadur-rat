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
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.function.IOSupplier;
import org.apache.rat.configuration.Format;
import org.apache.rat.configuration.LicenseReader;
import org.apache.rat.configuration.MatcherReader;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;

/**
 * A class that holds the list of licenses and approved licences from one or more Configuration file.
 */
public class Defaults {

    /**
     * The default configuration file from the package.
     */
    private static final URL DEFAULT_CONFIG_URL = Defaults.class.getResource("/org/apache/rat/default.xml");
    /**
     * The default XSLT stylesheet to produce a text output file.
     */
    public static final String PLAIN_STYLESHEET = "org/apache/rat/plain-rat.xsl";
    /**
     * The default XSLT stylesheet to produce a list of unapproved licenses.
     */
    public static final String UNAPPROVED_LICENSES_STYLESHEET = "org/apache/rat/unapproved-licenses.xsl";

    private LicenseSetFactory setFactory;
    
    /**
     * Initialize the system configuration reader..
     */
    public static void init() {
        Format fmt = Format.fromURL(DEFAULT_CONFIG_URL);
        MatcherReader mReader = fmt.matcherReader();
        mReader.addMatchers(DEFAULT_CONFIG_URL);
        mReader.readMatcherBuilders();
    }

    /**
     * Builder constructs instances.
     */
    private Defaults(Set<URL> urls) {
        this.setFactory = Defaults.readConfigFiles(urls);
    }

    /**
     * Gets a builder for a Defaults object.
     * @return the Builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Reads the configuration files.
     * @param urls the URLS to read.
     */
    private static LicenseSetFactory readConfigFiles(Collection<URL> urls) {

        SortedSet<ILicense> licenses = LicenseSetFactory.emptyLicenseSet();

        SortedSet<String> approvedLicenseIds = new TreeSet<String>();

        for (URL url : urls) {
            Format fmt = Format.fromURL(url);
            MatcherReader mReader = fmt.matcherReader();
            if (mReader != null) {
                mReader.addMatchers(url);
                mReader.readMatcherBuilders();
            }

            LicenseReader lReader = fmt.licenseReader();
            if (lReader != null) {
                lReader.addLicenses(url);
                licenses.addAll(lReader.readLicenses());
                lReader.approvedLicenseId().stream().map(ILicenseFamily::makeCategory).forEach(approvedLicenseIds::add);
            }
        }
        return new LicenseSetFactory(licenses, approvedLicenseIds);
    }

    /**
     * Gets a supplier for the "plain" text styleseet.
     * @return an IOSupplier for the plain text stylesheet.
     */
    public static IOSupplier<InputStream> getPlainStyleSheet() {
        return () -> Defaults.class.getClassLoader().getResourceAsStream(Defaults.PLAIN_STYLESHEET);
    }

    /**
     * Gets a supplier for the unapproved licences list stylesheet
     * @return an IOSupplier for the unapproved licenses list stylesheet.
     */
    public static IOSupplier<InputStream> getUnapprovedLicensesStyleSheet() {
        return () -> Defaults.class.getClassLoader().getResourceAsStream(Defaults.UNAPPROVED_LICENSES_STYLESHEET);
    }

    public SortedSet<ILicense> getLicenses(LicenseFilter filter) {
        return setFactory.getLicenses(filter);
    }
    
    public SortedSet<ILicenseFamily> getLicenseFamilies(LicenseFilter filter) {
        return setFactory.getLicenseFamilies(filter);
    }

    /**
     * Gets the sorted set of approved license ids.
     * If not licenses have been explicitly listed as approved all licenses are assumed to be approved.
     * @return The sorted set of approved licenseIds.
     */
    public SortedSet<String> getLicenseIds(LicenseFilter filter) {
        return setFactory.getLicenseFamilyIds(filter);
    }
    
    /**
     * The Defaults builder.
     */
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
         * Adds a URL to a configuration file to be read.
         * 
         * @param url the URL to add
         * @return this Builder for chaining
         */
        public Builder add(URL url) {
            fileNames.add(url);
            return this;
        }

        /**
         * Adds the name of a configuration file to be read.
         * 
         * @param fileName the name of the file to add.
         * @return this Builder for chaining
         */
        public Builder add(String fileName) throws MalformedURLException {
            return add(new File(fileName));
        }

        /**
         * Adds a configuration file to be read.
         * 
         * @param file the File to add.
         * @return this Builder for chaining
         */
        public Builder add(File file) throws MalformedURLException {
            return add(file.toURI().toURL());
        }

        /**
         * Removes a file from the list of configuration files to process.
         * 
         * @param url the URL of the file to remove.
         * @return this Builder for chaining
         */
        public Builder remove(URL url) {
            fileNames.remove(url);
            return this;
        }

        /**
         * Removes a file name from the list of configuration files to process.
         * 
         * @param fileName the fileName of the file to remove.
         * @return this Builder for chaining
         */
        public Builder remove(String fileName) throws MalformedURLException {
            return remove(new File(fileName));
        }

        /**
         * Removes a file from the list of configuration files to process.
         * 
         * @param file the File of the file to remove.
         * @return this Builder for chaining
         */
        public Builder remove(File file) throws MalformedURLException {
            return remove(file.toURI().toURL());
        }

        /**
         * Removes the default definitions from the list of files to process.
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
            return new Defaults(fileNames);
        }
    }
}
