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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.configuration.LicenseReader;
import org.apache.rat.configuration.Readers;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.SimpleLicenseFamily;

/**
 * Utility class that holds constants shared by the CLI tool and the Ant tasks.
 */
public class Defaults {
    static final URL DEFAULT_CONFIG_URL = Defaults.class.getResource("/org/apache/rat/default.xml");

    /**
     * no instances
     */
    private Defaults() {
    }

    private static SortedSet<ILicense> licenses = new TreeSet<>();
    private static SortedSet<ILicenseFamily> licenseFamilies = new TreeSet<>();

    public static Builder builder() {
        return new Builder();
    }

    public static void clear() {
        licenses.clear();
        licenseFamilies.clear();
    }

    private static void readConfigFiles(Collection<URL> urls) {
        for (URL url : urls) {
            LicenseReader reader = Readers.get(url);
            reader.add(url);
            licenseFamilies.addAll(reader.readFamilies());
            licenses.addAll(reader.readLicenses());
        }
    }

    public static final String PLAIN_STYLESHEET = "org/apache/rat/plain-rat.xsl";
    public static final String UNAPPROVED_LICENSES_STYLESHEET = "org/apache/rat/unapproved-licenses.xsl";

    public static InputStream getPlainStyleSheet() {
        return Defaults.class.getClassLoader().getResourceAsStream(Defaults.PLAIN_STYLESHEET);
    }

    public static InputStream getUnapprovedLicensesStyleSheet() {
        return Defaults.class.getClassLoader().getResourceAsStream(Defaults.UNAPPROVED_LICENSES_STYLESHEET);
    }

    public static InputStream getDefaultStyleSheet() {
        return getPlainStyleSheet();
    }

    public static ILicense createDefaultMatcher() {
        return new LicenseCollectionMatcher(licenses);
    }

    public static Set<ILicense> getLicenses() {
        return Collections.unmodifiableSet(licenses);
    }

    public static Set<String> getLicenseNames() {
        return licenseFamilies.stream().map(ILicenseFamily::getFamilyName).collect(Collectors.toSet());
    }

    public static SortedSet<ILicenseFamily> getLicenseFamilies() {
        return Collections.unmodifiableSortedSet(licenseFamilies);
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

        public Builder add(URL url) {
            fileNames.add(url);
            return this;
        }

        public Builder add(String fileName) throws MalformedURLException {
            return add(new File(fileName));
        }

        public Builder add(File file) throws MalformedURLException {
            return add(file.toURI().toURL());
        }

        public Builder remove(URL url) {
            fileNames.remove(url);
            return this;
        }

        public Builder remove(String fileName) throws MalformedURLException {
            return remove(new File(fileName));
        }

        public Builder remove(File file) throws MalformedURLException {
            return remove(file.toURI().toURL());
        }

        public Builder noDefault() {
            return remove(DEFAULT_CONFIG_URL);
        }

        public void build() {
            Defaults.readConfigFiles(fileNames);
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
        public boolean matches(String line) throws RatHeaderAnalysisException {
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
