package org.apache.rat.license;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.license.ILicense.Builder;

class ILicenseBuilder implements Builder {

    private IHeaderMatcher.Builder matcher;

    private String notes;

    private String derivedFrom;

    private final ILicenseFamily.Builder licenseFamily = ILicenseFamily.builder();

    @Override
    public Builder setMatcher(IHeaderMatcher.Builder matcher) {
        this.matcher = matcher;
        return this;
    }

    @Override
    public Builder setNotes(String notes) {
        this.notes = notes;
        return this;
    }

    @Override
    public Builder setDerivedFrom(String derivedFrom) {
        this.derivedFrom = derivedFrom;
        return this;
    }

    @Override
    public Builder setLicenseFamilyCategory(String licenseFamilyCategory) {
        this.licenseFamily.setLicenseFamilyCategory(licenseFamilyCategory);
        return this;
    }

    @Override
    public Builder setLicenseFamilyName(String licenseFamilyName) {
        this.licenseFamily.setLicenseFamilyName(licenseFamilyName);
        return this;
    }

    @Override
    public ILicense build() {
        return new SimpleLicense(licenseFamily.build(), matcher.build(), derivedFrom, notes);
    }
}