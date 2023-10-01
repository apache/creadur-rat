package org.apache.rat.configuration;

import java.net.URL;
import java.util.Collection;
import java.util.SortedSet;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.license.ILicense;

public interface MatcherReader {
    /**
     * Adds a URL to the set of files to be read.
     * 
     * @param url the URL to read.
     */
    void add(URL url);

    /**
     * Reads the configuration and extracts the BaseLicenses.
     * 
     * @return A collection of Base licenses.
     */
    Collection<IHeaderMatcher> readMatchers();
}
