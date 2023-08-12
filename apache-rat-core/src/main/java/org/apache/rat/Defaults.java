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

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.builder.fluent.XMLBuilderParameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.generation.GeneratedLicenseNotRequired;
import org.apache.rat.analysis.generation.JavaDocLicenseNotRequired;
import org.apache.rat.analysis.license.ApacheSoftwareLicense20;
import org.apache.rat.analysis.license.BaseLicense;
import org.apache.rat.analysis.license.CDDL1License;
import org.apache.rat.analysis.license.CopyrightHeader;
import org.apache.rat.analysis.license.DojoLicenseHeader;
import org.apache.rat.analysis.license.FullTextMatchingLicense;
import org.apache.rat.analysis.license.GPL1License;
import org.apache.rat.analysis.license.GPL2License;
import org.apache.rat.analysis.license.GPL3License;
import org.apache.rat.analysis.license.MITLicense;
import org.apache.rat.analysis.license.OASISLicense;
import org.apache.rat.analysis.license.SPDXMatcher;
import org.apache.rat.analysis.license.TMF854LicenseHeader;
import org.apache.rat.analysis.license.W3CDocLicense;
import org.apache.rat.analysis.license.W3CLicense;
import org.apache.rat.analysis.util.HeaderMatcherMultiplexer;
import org.apache.rat.api.MetaData;
import org.apache.rat.api.MetaData.Datum;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;


/**
 * Utility class that holds constants shared by the CLI tool and the Ant tasks.
 */
public class Defaults {
    /**
     * no instances
     */
    private Defaults() {
    }
    
    private static List<BaseLicense> licenses;
    private static Map<String,MetaData> licenseFamilies = new HashMap<>();
    
    private static Configuration readConfigFiles(String... fileNames) throws ConfigurationException {

        CompositeConfiguration composite = new CompositeConfiguration();
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<>(
                PropertiesConfiguration.class);
//        PropertiesBuilderParameters propertyParameters = new Parameters().properties()
//                .setThrowExceptionOnMissing(true);

        
        XMLBuilderParameters parameters = new Parameters().xml().setThrowExceptionOnMissing(true);
                
      for (String fname : fileNames) {
      parameters.setFile(new File(fname));
      composite.addConfiguration(builder.configure(parameters).getConfiguration());
  }
        return composite;
    }
    
    private static void readFamilies(Configuration families) {
        Iterator<String> iter = families.getKeys();
        while (iter.hasNext()) {
            MetaData meta = new MetaData();
            String id=iter.next();
            Configuration fam = families.subset(id);
            
            String category = fam.getString("category").concat("     ").substring(0,5);
            meta.add(new MetaData.Datum(MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY, category));
            meta.add(new MetaData.Datum(MetaData.RAT_URL_LICENSE_FAMILY_NAME, fam.getString("name")));
            licenseFamilies.put(id, meta);
        }
    }
    
    private static void readLicenses(Configuration licenseConfig) {
        Iterator<String> iter = licenseConfig.getKeys();
        while (iter.hasNext()) {
            String id=iter.next();
            Configuration lic = licenseConfig.subset(id);
            String familyId = lic.getString("family");
            MetaData family = licenseFamilies.get(familyId);
            if (family == null) {
                throw new IllegalArgumentException( String.format( "license %s uses family %s which is missing", id, familyId));
            }
            if (lic.containsKey("fullText"))
            {
                licenses.add( new FullTextMatchingLicense(
                        family.get(MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY),
                        family.get(MetaData.RAT_URL_LICENSE_FAMILY_NAME),
                        lic.getString("notes", ""),
                        lic.getString("fullText")));
            }
            if (lic.containsKey("copyright")) {
                licenses.add( new CopyrightHeader(
                        family.get(MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY),
                        family.get(MetaData.RAT_URL_LICENSE_FAMILY_NAME),
                        lic.getString("notes", ""),
                        lic.getString("copyright")));
            }
            if (lic.containsKey("text")) {
                
                //lic.get
            }
            if (lic.containsKey("spdx")) {
                licenses.add( new SPDXMatcher( lic.getString("spdx"),
                        family.get(MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY),
                        family.get(MetaData.RAT_URL_LICENSE_FAMILY_NAME)
                        ));
            }
        }
    }
    
    public static void readConfig(String... fileNames) throws ConfigurationException {
        Configuration config = readConfigFiles(fileNames);
        readFamilies( config.subset("family"));
        readLicenses( config.subset("license"));
        
        /*
         * --- JSON ---
         *  {
         *      families : {
         *          xxx: 'name',
         *          },
         *      licenses : [
         *      {
         *          family : 'xxx',
         *          notes: 'notes',
         *          spdx: 'spdx',
         *          fulltext: 'text',
         *          copyright: 'copyright',
         *          text = ['text','text2','text3'],
         *          }],
         *  }
         * 
         * --- RDF ---
         *
         * family:category [
         *   name='   '
         *   ] 
         *
         * license:id [
         *   family family:category
         *   notes=''
         *   spdx=''
         *   fulltext=''
         *   copyright=''
         *   text=''
         *   text=''
         *   text=''
         * ]
         *
         * --- XML ---
         * <config>
         * <family id='fxx' category='xxxxx'>
         *      name text
         * </family>
         * 
         * <license id="lxx' family='fxx' spdx='spdxtx'>
         *  <notes>notes text</notes>
         *  <fulltext>full text</fulltext>
         *  <copyright>copyright</copyright>
         *  <text>text</text>
         *  </license>
         *  </config>
         *  
         *  --- CONFIG ---
         * family.famid.category=xxxxx
         * family.famid.name=text
         * 
         * 
         * license.id.family=famid
         * license.id.notes=
         * license.id.fullText=
         * license.id.copyright=
         * license.id.text=
         * license.id.spdx=
         */
    }

    /**
     * The standard list of licenses to include in the reports.
     * Needs to match configuration in {@link org.apache.rat.policy.DefaultPolicy}.
     */
    public static final List<IHeaderMatcher> DEFAULT_MATCHERS = Collections.unmodifiableList(
            Arrays.asList(new ApacheSoftwareLicense20(),
                    new GPL1License(),
                    new GPL2License(),
                    new GPL3License(),
                    new MITLicense(),
                    new W3CLicense(),
                    new W3CDocLicense(),
                    new OASISLicense(),
                    new JavaDocLicenseNotRequired(), // does not have a MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_... entry
                    new GeneratedLicenseNotRequired(), // does not have a MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_... entry
                    new DojoLicenseHeader(),
                    new TMF854LicenseHeader(),
                    new CDDL1License(),
                    SPDXMatcher.Factory.getDefault()));

    // all classes in license package implementing ILicenseFamily
    public static final List<String> DEFAULT_LICENSE_FAMILIES = Collections.unmodifiableList(
            Arrays.asList(
                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_ACADEMIC_FREE_LICENSE_VERSION_2_1, //
                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_APACHE_LICENSE_VERSION_2_0, //
                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_CDDL1, //
                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_GPL_VERSION_1, //
                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_GPL_VERSION_2, //
                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_GPL_VERSION_3, //
                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_MIT, //
                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_MODIFIED_BSD_LICENSE, //
                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_OASIS_OPEN_LICENSE, //
                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_W3C_DOCUMENT_COPYRIGHT, //
                    MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_W3C_SOFTWARE_COPYRIGHT
                    //
            ));

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
        return new HeaderMatcherMultiplexer(Defaults.DEFAULT_MATCHERS);
    }
}
