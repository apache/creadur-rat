package org.apache.rat.configuration;

import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;

class ILicenseFamilyProxy implements ILicenseFamily {
    private final ILicense wrapped;

    static public ILicenseFamily create(ILicense license) {
        return (license instanceof ILicenseProxy) ? new ILicenseFamilyProxy(license) : license.getLicenseFamily();
    }
    
    private ILicenseFamilyProxy(ILicense license) {
        this.wrapped = license;
    }

    @Override
    public String getFamilyName() {
        return wrapped.getLicenseFamily().getFamilyName();
    }

    @Override
    public String getFamilyCategory() {
        return wrapped.getLicenseFamily().getFamilyCategory();
    }

}
