package org.apache.rat.configuration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.io.File;
import java.util.ArrayList;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.rat.analysis.license.BaseLicense;
import org.apache.rat.analysis.license.CopyrightHeader;
import org.apache.rat.analysis.license.FullTextMatchingLicense;
import org.apache.rat.analysis.license.SPDXMatcher;
import org.apache.rat.analysis.license.SimplePatternBasedLicense;
import org.apache.rat.api.MetaData;

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
public class ConfigurationReader implements Reader {
    private final CompositeConfiguration configuration;

    public ConfigurationReader() {
        configuration = new CompositeConfiguration();
    }
    
    public void add(String fileName) {
        try {
            read( fileName );
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
        
    }
    
    public void read(String... fileNames) throws ConfigurationException {
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<>(
                PropertiesConfiguration.class);
        PropertiesBuilderParameters parameters = new Parameters().properties()
                .setThrowExceptionOnMissing(true);

                
      for (String fname : fileNames) {
          parameters.setFile(new File(fname));
          add(builder.configure(parameters).getConfiguration());
      }
    }
    
    public void add(Configuration cfg) {
        configuration.addConfiguration(cfg);
    }
    
    @Override
    public Map<String, MetaData> readFamilies() {
        Map<String, MetaData> licenseFamilies = new HashMap<>();
        Configuration families = configuration.subset("family");
        Iterator<String> iter = families.getKeys();
        while (iter.hasNext()) {
            MetaData meta = new MetaData();
            String cat = iter.next();
            String category = cat.concat("     ").substring(0, 5);
            Configuration fam = families.subset(cat);
            meta.add(new MetaData.Datum(MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY, category));
            meta.add(new MetaData.Datum(MetaData.RAT_URL_LICENSE_FAMILY_NAME, fam.getString("name")));
            licenseFamilies.put(cat.trim(), meta);
        }
        return licenseFamilies;
    }

    @Override
    public Collection<BaseLicense> readLicenses() {
        Map<String,MetaData> families = readFamilies();
        List<BaseLicense> licenses = new ArrayList<BaseLicense>();
        Configuration licenseConfig = configuration.subset("license");
        Iterator<String> iter = licenseConfig.getKeys();
        while (iter.hasNext()) {
            String id=iter.next();
            Configuration lic = licenseConfig.subset(id);
            String familyId = lic.getString("family").trim();
            MetaData family = families.get(familyId);
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
                Configuration text = lic.subset("text");
                List<String> texts = new ArrayList<>();
                Iterator<String> textIter = text.getKeys();
                while (textIter.hasNext()) {
                    texts.add(text.getString(textIter.next()));
                }
                licenses.add( new SimplePatternBasedLicense(
                        family.get(MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY),
                        family.get(MetaData.RAT_URL_LICENSE_FAMILY_NAME),
                        lic.getString("notes", ""),
                        texts.toArray( new String[texts.size()])));
                //lic.get
            }
            if (lic.containsKey("spdx")) {
                licenses.add( new SPDXMatcher( lic.getString("spdx"),
                        family.get(MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY),
                        family.get(MetaData.RAT_URL_LICENSE_FAMILY_NAME)
                        ));
            }
        }
        return licenses;
    }
}
