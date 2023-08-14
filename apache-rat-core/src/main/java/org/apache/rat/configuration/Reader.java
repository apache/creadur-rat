package org.apache.rat.configuration;

import java.util.Collection;
import java.util.Map;

import org.apache.rat.analysis.license.BaseLicense;
import org.apache.rat.api.MetaData;

public interface Reader {
    void add(String name);

    /**
     * Reads the configuration and extracts the Family metadata indexed by category id.
     * @return Map of Category id to associated Metadata.
     */
    Map<String,MetaData> readFamilies();
    
    /**
     * Reads the configuration and extracts the BaseLicenses.
     * @return A collection of Base licenses.
     */
    Collection<BaseLicense> readLicenses();
}
