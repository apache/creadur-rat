package org.apache.rat.license;

import java.util.Comparator;
import java.util.SortedSet;

import org.apache.rat.analysis.IHeaderMatcher;

public interface ILicense extends IHeaderMatcher, Comparable<ILicense> {
    ILicenseFamily getLicenseFamily();
    String getNotes();
    ILicense derivedFrom();
    
    /**
     * Search a set
     * @param licenseId
     * @param licenses
     * @return
     */
    static ILicense search(String licenseId, SortedSet<ILicense> licenses) {
        ILicenseFamily searchFamily = new SimpleLicenseFamily(licenseId, "searching proxy");
        ILicense target = new ILicense() {
    
            @Override
            public String getId() {
                return licenseId;
            }
    
            @Override
            public void reset() {
                // do nothing
            }
    
            @Override
            public boolean matches(String line) {
                return false;
            }
    
            @Override
            public int compareTo(ILicense arg0) {
                return searchFamily.compareTo(arg0.getLicenseFamily());
            }
    
            @Override
            public ILicenseFamily getLicenseFamily() {
                return searchFamily;
            }
    
            @Override
            public String getNotes() {
                return null;
            }
    
            @Override
            public ILicense derivedFrom() {
                return null;
            }
            
        };
        return search(target,licenses);
    }
    
    static ILicense search(ILicense target, SortedSet<ILicense> licenses) {
        SortedSet<ILicense> part = licenses.tailSet(target);
        return (!part.isEmpty() && part.first().compareTo(target) == 0) ? part.first() : null;
    }
    
    static Comparator<ILicense> getComparator() {
        return (x,y) -> x.getLicenseFamily().compareTo(y.getLicenseFamily());
    }
}
