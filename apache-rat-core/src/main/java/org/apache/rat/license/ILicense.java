package org.apache.rat.license;

import java.util.Comparator;

import org.apache.rat.analysis.IHeaderMatcher;

public interface ILicense extends IHeaderMatcher, Comparable<ILicense> {
    ILicenseFamily getLicenseFamily();
    String getNotes();
    ILicense derivedFrom();
    
    static Comparator<ILicense> getComparator() {
        return (x,y) -> x.getLicenseFamily().compareTo(y.getLicenseFamily());
    }
}
