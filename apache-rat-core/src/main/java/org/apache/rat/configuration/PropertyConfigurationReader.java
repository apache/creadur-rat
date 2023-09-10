/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.configuration;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.license.CopyrightHeader;
import org.apache.rat.analysis.license.FullTextMatchingLicense;
import org.apache.rat.analysis.license.SimplePatternBasedLicense;
import org.apache.rat.analysis.matchers.OrMatcher;
import org.apache.rat.analysis.matchers.SPDXMatcherFactory;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.SimpleLicenseFamily;

/*
 *          *  
         *  --- CONFIG ---
         * family.category.name=text
         * 
         * license.id.family=famid
         * license.id.notes=
         * license.id.fullText=
         * license.id.copyright=
         * license.id.text=
         * license.id.spdx=
 */
public class PropertyConfigurationReader implements Reader {
    private final CompositeConfiguration configuration;

    public PropertyConfigurationReader() {
        configuration = new CompositeConfiguration();
    }

    @Override
    public void add(URL url) {
        try {
            read(url);
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }

    }

    public void read(URL... urls) throws ConfigurationException {
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<>(
                PropertiesConfiguration.class);
        PropertiesBuilderParameters parameters = new Parameters().properties().setThrowExceptionOnMissing(true);

        for (URL url : urls) {
            parameters.setURL(url);
            add(builder.configure(parameters).getConfiguration());
        }
    }

    public void add(Configuration cfg) {
        configuration.addConfiguration(cfg);
    }

    @Override
    public SortedSet<ILicenseFamily> readFamilies() {
        SortedSet<ILicenseFamily> licenseFamilies = new TreeSet<>();
        Configuration families = configuration.subset("family");
        Iterator<String> iter = families.getKeys();
        while (iter.hasNext()) {
            String cat = iter.next();
            ILicenseFamily fam = new SimpleLicenseFamily(cat, families.getString(cat));
            licenseFamilies.add(new SimpleLicenseFamily(cat, families.getString(cat)));
        }
        return licenseFamilies;
    }

    Set<String> extractKeys(Configuration cfg) {
        Iterator<String> iter = cfg.getKeys();
        Set<String> ids = new TreeSet<>();
        while (iter.hasNext()) {
            ids.add(iter.next().split("\\.")[0]);
        }
        return ids;
    }

    @Override
    public Collection<IHeaderMatcher> readLicenses() {
        SortedSet<ILicenseFamily> families = readFamilies();
        Collection<IHeaderMatcher> result = new ArrayList<>();
        List<IHeaderMatcher> licenses = new ArrayList<>();
        Configuration licenseConfig = configuration.subset("license");
        for (String id : extractKeys(licenseConfig)) {
            Configuration lic = licenseConfig.subset(id);
            ILicenseFamily licenseFamily = ILicenseFamily.searchSet(families, lic.getString("family"));
            if (licenseFamily == null) {
                throw new IllegalArgumentException(
                        String.format("license %s uses family %s which is missing", id, lic.getString("family")));
            }
            Set<String> keys = extractKeys(lic);
            String notes = lic.getString("notes", "");
            if (keys.contains("fullText")) {
                licenses.add(new FullTextMatchingLicense(id, licenseFamily, notes, lic.getString("fullText")));
            }
            if (keys.contains("copyright")) {
                licenses.add(new CopyrightHeader(id, licenseFamily, notes, lic.getString("copyright")));
            }
            if (keys.contains("text")) {
                Configuration text = lic.subset("text");
                Set<String> txtKeys = extractKeys(text);
                List<String> texts = txtKeys.stream().map(text::getString).collect(Collectors.toList());
                licenses.add(
                        new SimplePatternBasedLicense(id, licenseFamily, notes, texts.toArray(new String[texts.size()])));
            }
            if (keys.contains("spdx")) {
                licenses.add(SPDXMatcherFactory.INSTANCE.register(id, licenseFamily, notes, lic.getString("spdx")));
            }
            if (licenses.size() == 1) {
                result.add(licenses.get(0));
                licenses.clear();
            } else if (licenses.size() > 1) {
                result.add(new OrMatcher(id, licenses));
                licenses = new ArrayList<>();
            }
        }
        return result;
    }
}
