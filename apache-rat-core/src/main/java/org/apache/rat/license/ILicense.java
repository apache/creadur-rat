package org.apache.rat.license;

import org.apache.rat.analysis.IHeaderMatcher;

public interface ILicense extends IHeaderMatcher {
    ILicenseFamily getLicenseFamily();
    String getNotes();
    ILicense derivedFrom();
}
