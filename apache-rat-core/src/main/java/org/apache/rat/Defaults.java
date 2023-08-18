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

import java.io.InputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.license.BaseLicense;
import org.apache.rat.analysis.license.SPDXMatcher;
import org.apache.rat.analysis.util.HeaderMatcherMultiplexer;
import org.apache.rat.api.MetaData;
import org.apache.rat.configuration.Reader;
import org.apache.rat.configuration.Readers;

/**
 * Utility class that holds constants shared by the CLI tool and the Ant tasks.
 */
public class Defaults {
    static final URL DEFAULT_CONFIG_URL = Defaults.class
            .getResource("/org/apache/rat/default.config");
    
    /**
     * no instances
     */
    private Defaults() {
    }

    private static Map<String,BaseLicense> licenses = new LinkedHashMap<>();
    private static Map<String, MetaData> licenseFamilies = new HashMap<>();

    public static Builder builder() {
        return new Builder();
    }
    
    private static void readConfigFiles(Collection<URL> urls) {
        for (URL url : urls) {
            Reader reader = Readers.get(url);
            reader.add(url);
            licenseFamilies.putAll(reader.readFamilies());
            licenses.putAll(reader.readLicenses());
        }
    }

//    /**
//     * The standard list of licenses to include in the reports.
//     * Needs to match configuration in {@link org.apache.rat.policy.DefaultPolicy}.
//     */
//    public static final List<IHeaderMatcher> DEFAULT_MATCHERS = Collections.unmodifiableList(
//            Arrays.asList(new ApacheSoftwareLicense20(),
//                    new GPL1License(),
//                    new GPL2License(),
//                    new GPL3License(),
//                    new MITLicense(),
//                    new W3CLicense(),
//                    new W3CDocLicense(),
//                    new OASISLicense(),
//                    new JavaDocLicenseNotRequired(), // does not have a MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_... entry
//                    new GeneratedLicenseNotRequired(), // does not have a MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_... entry
//                    new DojoLicenseHeader(),
//                    new TMF854LicenseHeader(),
//                    new CDDL1License(),
//                    SPDXMatcher.Factory.getDefault()));
//
//    // all classes in license package implementing ILicenseFamily
//    public static final List<String> DEFAULT_LICENSE_FAMILIES = Collections.unmodifiableList(
//            Arrays.asList(
//                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_ACADEMIC_FREE_LICENSE_VERSION_2_1, //
//                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_APACHE_LICENSE_VERSION_2_0, //
//                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_CDDL1, //
//                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_GPL_VERSION_1, //
//                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_GPL_VERSION_2, //
//                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_GPL_VERSION_3, //
//                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_MIT, //
//                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_MODIFIED_BSD_LICENSE, //
//                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_OASIS_OPEN_LICENSE, //
//                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_W3C_DOCUMENT_COPYRIGHT, //
//                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_W3C_SOFTWARE_COPYRIGHT
//                    //
//            ));

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

    public static IHeaderMatcher createDefaultMatcher() {
        List<IHeaderMatcher> matchers = getLicenses().stream()
                .filter( x -> ! (x instanceof SPDXMatcher.Match))
                .collect(Collectors.toList());
        if (SPDXMatcher.INSTANCE.isActive()) {
            matchers.add(SPDXMatcher.INSTANCE);
        }
        return new HeaderMatcherMultiplexer(matchers);
    }

    public static Collection<BaseLicense> getLicenses() {
        return Collections.unmodifiableCollection(licenses.values());
    }

    public static Map<String,BaseLicense> getLicenseMap() {
        return Collections.unmodifiableMap(licenses);
    }

    public static List<String> getLicenseNames() {
        return licenseFamilies.values().stream().map(x -> x.get(MetaData.RAT_URL_LICENSE_FAMILY_NAME).getValue())
                .collect(Collectors.toList());
    }
    
    public static Collection<MetaData> getLicenseFamilies() {
        return licenseFamilies.values();
    }
    
    public static class Builder {
        private Set<URL> fileNames = new TreeSet<>(new Comparator<URL>() {

            @Override
            public int compare(URL url1, URL url2) {
                return url1.toString().compareTo(url2.toString());
            }});

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
}
