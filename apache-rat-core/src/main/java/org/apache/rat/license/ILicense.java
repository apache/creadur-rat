package org.apache.rat.license;

import org.apache.rat.analysis.IHeaderMatcher;

public interface ILicense extends IHeaderMatcher {
    ILicenseFamily getLicenseFamily();
    String getNotes();
    ILicense derivedFrom();
    
    /*
     * <license id=id name=name derived-from="">
        <notes></notes>
        <text>  </text>
        <copyright start='' end='' owner=''/>
        <spdx></spdx> 
        <and> <license>...</and>
        <or> <license>...</or> 
    </license>
     */
}
