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

import static java.lang.String.format;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.rat.configuration.Format;
import org.apache.rat.configuration.LicenseReader;
import org.apache.rat.configuration.MatcherReader;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseSetFactory;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;
import org.apache.rat.walker.NameBasedHiddenFileFilter;

/**
 * A class that provides the standard system defaults for the ReportConfiguration.
 * Properties in this class may be overridden or added to by configuration options in the various UIs.
 * See the specific UI for details.
 */
public final class Defaults {
    /** The path to the default configuration file */
    private static final String DEFAULT_CONFIG_PATH = "/org/apache/rat/default.xml";
    /** The default configuration file from the package. */
    private static final URI DEFAULT_CONFIG_URI;
   /** The default files to ignore if none are specified. */
    public static final IOFileFilter FILES_TO_IGNORE = FalseFileFilter.FALSE;
    /** The default directories to ignore */
    public static final IOFileFilter DIRECTORIES_TO_IGNORE = NameBasedHiddenFileFilter.HIDDEN;
    /** The default ARCHIVES processing style */
    public static final ReportConfiguration.Processing ARCHIVE_PROCESSING = ReportConfiguration.Processing.NOTIFICATION;
    /** The default STANDARD processing style */
    public static final ReportConfiguration.Processing STANDARD_PROCESSING = ReportConfiguration.Processing.ABSENCE;
    /** The default license families to list */
    public static final LicenseFilter LIST_FAMILIES = LicenseFilter.NONE;
    /** The default licenses to list */
    public static final LicenseFilter LIST_LICENSES = LicenseFilter.NONE;

    /** The license set factory to build license sets based upon default options */
    private final LicenseSetFactory setFactory;

    // TODO look at this static block with respect to the init() static method and figure out if we need both.
    static {
         URL url = Defaults.class.getResource(DEFAULT_CONFIG_PATH);
         URI uri = null;
         if (url == null) {
             DefaultLog.getInstance().error(format("Unable to read '%s'", DEFAULT_CONFIG_PATH));
         } else {
             try {
                 uri = url.toURI();
             } catch (URISyntaxException e) {
                 DefaultLog.getInstance().error("Unable to read " + url, e);
             }
         }
         DEFAULT_CONFIG_URI = uri;
    }
    /**
     * Initialize the system configuration reader.
     */
    public static void init() {
        Format fmt = Format.from(DEFAULT_CONFIG_URI);
        MatcherReader mReader = fmt.matcherReader();
        if (mReader != null) {
            mReader.addMatchers(DEFAULT_CONFIG_URI);
            mReader.readMatcherBuilders();
        } else {
            DefaultLog.getInstance().error("Unable to construct MatcherReader from" + DEFAULT_CONFIG_URI);
        }
    }

    /**
     * Builder constructs instances.
     * @param log The log to write messages to.
     * @param uris The set of URIs to read.
     */
    private Defaults(final Log log, final Set<URI> uris) {
        this.setFactory = Defaults.readConfigFiles(log, uris);
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
     * @param log The log to write messages to.
     * @param uris the URIs to read.
     */
    private static LicenseSetFactory readConfigFiles(final Log log, final Collection<URI> uris) {

        SortedSet<ILicense> licenses = new TreeSet<>();

        SortedSet<String> approvedLicenseCategories = new TreeSet<>();

        for (URI uri : uris) {
            Format fmt = Format.from(uri);
            MatcherReader mReader = fmt.matcherReader();
            if (mReader != null) {
                mReader.addMatchers(uri);
                mReader.readMatcherBuilders();
            }

            LicenseReader lReader = fmt.licenseReader();
            if (lReader != null) {
                lReader.setLog(log);
                lReader.addLicenses(uri);
                licenses.addAll(lReader.readLicenses());
                lReader.approvedLicenseId().stream().map(ILicenseFamily::makeCategory).forEach(approvedLicenseCategories::add);
            }
        }

        LicenseSetFactory result = new LicenseSetFactory(licenses);
        approvedLicenseCategories.forEach(result::addLicenseCategory);
        return result;
    }

    /**
     * Gets the default license set factory.
     * @return the default license set factory.
     */
    public LicenseSetFactory getLicenseSetFactory() {
        return setFactory;
    }

    /**
     * The Defaults builder.
     */
    public static final class Builder {
        /** The list of URIs that we wil read to configure the Defaults */
        private final Set<URI> fileNames = new TreeSet<>();

        private Builder() {
            if (DEFAULT_CONFIG_URI == null) {
                DefaultLog.getInstance().error("Unable to read default.xml");
            } else {
               fileNames.add(DEFAULT_CONFIG_URI);
            }
        }

        /**
         * Adds a URL to a configuration file to be read.
         * @param uri the URI to add
         * @return this Builder for chaining
         */
        public Builder add(final URI uri) {
            fileNames.add(uri);
            return this;
        }

        /**
         * Adds the name of a configuration file to be read.
         * @param fileName the name of the file to add.
         * @return this Builder for chaining
         * @throws MalformedURLException in case the fileName cannot be found.
         */
        public Builder add(final String fileName) throws MalformedURLException {
            return add(new File(fileName));
        }

        /**
         * Adds a configuration file to be read.
         * @param file the File to add.
         * @return this Builder for chaining
         * @throws MalformedURLException in case the file cannot be found.
         */
        public Builder add(final File file) throws MalformedURLException {
            return add(file.toURI());
        }

        /**
         * Removes a file from the list of configuration files to process.
         * @param uri the URI of the file to remove.
         * @return this Builder for chaining
         */
        public Builder remove(final URI uri) {
            fileNames.remove(uri);
            return this;
        }

        /**
         * Removes a file name from the list of configuration files to process.
         * @param fileName the fileName of the file to remove.
         * @return this Builder for chaining
         * @throws MalformedURLException in case the fileName cannot be found.
         */
        public Builder remove(final String fileName) throws MalformedURLException {
            return remove(new File(fileName));
        }

        /**
         * Removes a file from the list of configuration files to process.
         * @param file the File of the file to remove.
         * @return this Builder for chaining
         * @throws MalformedURLException in case the file cannot be found.
         */
        public Builder remove(final File file) throws MalformedURLException {
            return remove(file.toURI());
        }

        /**
         * Removes the default definitions from the list of files to process.
         * @return this Builder for chaining
         */
        public Builder noDefault() {
            return remove(DEFAULT_CONFIG_URI);
        }

        /**
         * Builds the defaults object.
         * @param log the Log to use to report errors when building the defaults.
         * @return the current defaults object.
         */
        public Defaults build(final Log log) {
            return new Defaults(log, fileNames);
        }
    }
}
