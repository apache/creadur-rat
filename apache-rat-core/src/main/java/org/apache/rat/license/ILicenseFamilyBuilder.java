package org.apache.rat.license;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.ConfigurationException;
import org.apache.rat.license.ILicenseFamily.Builder;

public class ILicenseFamilyBuilder implements Builder {

    private String licenseFamilyCategory;
    private String licenseFamilyName;

    @Override
    public Builder setLicenseFamilyCategory(String licenseFamilyCategory) {
        this.licenseFamilyCategory = licenseFamilyCategory;
        return this;
    }

    @Override
    public Builder setLicenseFamilyName(String licenseFamilyName) {
        this.licenseFamilyName = licenseFamilyName;
        return this;
    }

    @Override
    public ILicenseFamily build() {
        if (StringUtils.isBlank(licenseFamilyCategory)) {
            throw new ConfigurationException("LicenseFamily Category must be specified");
        }
        if (StringUtils.isBlank(licenseFamilyName)) {
            throw new ConfigurationException("LicenseFamily Name must be specified");
        }
        return new SimpleLicenseFamily(licenseFamilyCategory, licenseFamilyName);
    }
}