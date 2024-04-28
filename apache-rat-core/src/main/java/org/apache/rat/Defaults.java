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
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.function.IOSupplier;
import org.apache.rat.configuration.Format;
import org.apache.rat.configuration.LicenseReader;
import org.apache.rat.configuration.MatcherReader;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.utils.Log;
import org.apache.rat.walker.NameBasedHiddenFileFilter;

/**
 * A class that provides the standard system defaults for the ReportConfiguration.
 *
 * Properties in this class may be overridden or added to by configuration options in the various UIs.
 * See the specific UI for details.
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

    private final LicenseSetFactory setFactory;

    private static final FilenameFilter FILES_TO_IGNORE = WildcardFileFilter.builder().setWildcards("*.json").setIoCase(IOCase.INSENSITIVE).get();

    private static final IOFileFilter DIRECTORIES_TO_IGNORE = NameBasedHiddenFileFilter.HIDDEN;

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
    private Defaults(Log log, Set<URL> urls) {
        this.setFactory = Defaults.readConfigFiles(log, urls);
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
     * @param urls the URLs to read.
     */
    private static LicenseSetFactory readConfigFiles(Log log, Collection<URL> urls) {

        SortedSet<ILicense> licenses = LicenseSetFactory.emptyLicenseSet();

        SortedSet<String> approvedLicenseIds = new TreeSet<>();

        for (URL url : urls) {
            Format fmt = Format.fromURL(url);
            MatcherReader mReader = fmt.matcherReader();
            if (mReader != null) {
                mReader.addMatchers(url);
                mReader.readMatcherBuilders();
            }

            LicenseReader lReader = fmt.licenseReader();
            if (lReader != null) {
                lReader.setLog(log);
                lReader.addLicenses(url);
                licenses.addAll(lReader.readLicenses());
                lReader.approvedLicenseId().stream().map(ILicenseFamily::makeCategory).forEach(approvedLicenseIds::add);
            }
        }
        return new LicenseSetFactory(licenses, approvedLicenseIds);
    }

    /**
     * Gets a supplier for the "plain" text stylesheet.
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

    /**
     * Gets the sorted set of approved licenses for a given filter condition.
     * @param filter define which type of licenses to return.
     * @return sorted set of licenses.
     */
    public SortedSet<ILicense> getLicenses(LicenseFilter filter) {
        return setFactory.getLicenses(filter);
    }
    
    /**
     * Gets the sorted set of approved licenses for a given filter condition.
     * @param filter define which type of licenses to return.
     * @return sorted set of license families.
     */
    public SortedSet<ILicenseFamily> getLicenseFamilies(LicenseFilter filter) {
        return setFactory.getLicenseFamilies(filter);
    }

    /**
     * Gets the sorted set of approved license ids for a given filter condition.
     * If no licenses have been explicitly listed as approved, all licenses are assumed to be approved.
     * @param filter define which type of licenses to return.
     * @return The sorted set of approved licenseIds.
     */
    public SortedSet<String> getLicenseIds(LicenseFilter filter) {
        return setFactory.getLicenseFamilyIds(filter);
    }

    public static FilenameFilter getFilesToIgnore() {
        return FILES_TO_IGNORE;
    }

    public static IOFileFilter getDirectoriesToIgnore() {
        return DIRECTORIES_TO_IGNORE;
    }
    
    /**
     * The Defaults builder.
     */
    public static class Builder {
        private final Set<URL> fileNames = new TreeSet<>(Comparator.comparing(URL::toString));

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
         * @throws MalformedURLException in case the fileName cannot be found.
         */
        public Builder add(String fileName) throws MalformedURLException {
            return add(new File(fileName));
        }

        /**
         * Adds a configuration file to be read.
         * 
         * @param file the File to add.
         * @return this Builder for chaining
         * @throws MalformedURLException in case the file cannot be found.
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
         * @throws MalformedURLException in case the fileName cannot be found.
         */
        public Builder remove(String fileName) throws MalformedURLException {
            return remove(new File(fileName));
        }

        /**
         * Removes a file from the list of configuration files to process.
         * 
         * @param file the File of the file to remove.
         * @return this Builder for chaining
         * @throws MalformedURLException in case the file cannot be found.
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
         * @param log the Log to use to report errors when building the defaults.
         * @return the current defaults object.
         */
        public Defaults build(Log log) {
            return new Defaults(log, fileNames);
        }
    }
}
