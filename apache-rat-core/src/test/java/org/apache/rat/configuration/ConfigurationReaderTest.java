package org.apache.rat.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.rat.analysis.license.BaseLicense;
import org.apache.rat.api.MetaData;
import org.junit.Test;

public class ConfigurationReaderTest {
    
    private static String[] FAMILIES = {"GEN", "?????", "AL", "OASIS", "W3CD", "W3C", "GPL1", "GPL2", "GPL3", "MIT", "CDDL1", "BSD_m"};
    
    @Test
    public void readDefault() throws ConfigurationException {
        ConfigurationReader reader = new ConfigurationReader();
        URL url = ConfigurationReader.class.getResource("/org/apache/rat/default.config");
        reader.read(url.getFile());
        Map<String,MetaData> families = reader.readFamilies();
        assertTrue(families.keySet().containsAll(Arrays.asList(FAMILIES)));
        families.keySet().removeAll(Arrays.asList(FAMILIES));
        assertTrue(families.isEmpty());
        
        Collection<BaseLicense> licenses = reader.readLicenses();
        assertEquals(24, licenses.size());
        Map<String,Integer> result = new TreeMap<>();
        licenses.stream().map(BaseLicense::getLicenseFamilyCategory).forEach( x -> {
            Integer i = result.get(x);
            if (i == null) {
                result.put(x, 1);
            } else {
                result.put(x, 1+i.intValue());
            }
        });
        assertEquals(4,result.get("AL   ").intValue());
        assertEquals(2,result.get("BSD_m").intValue());
        assertEquals(3,result.get("CDDL1").intValue());
        assertEquals(2,result.get("GEN  ").intValue());
        assertEquals(2,result.get("GPL1 ").intValue());
        assertEquals(2,result.get("GPL2 ").intValue());
        assertEquals(2,result.get("GPL3 ").intValue());
        assertEquals(2,result.get("MIT  ").intValue());
        assertEquals(2,result.get("OASIS").intValue());
        assertEquals(2,result.get("W3C  ").intValue());
        assertEquals(1,result.get("W3CD ").intValue());
    }

}
