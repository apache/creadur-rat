package org.apache.rat.license;

import org.apache.rat.analysis.IHeaderMatcher;

public class SimpleLicense implements ILicense {

    private ILicenseFamily family;
    private IHeaderMatcher matcher;
    private ILicense derivedFrom;
    private String notes;

    public SimpleLicense(ILicenseFamily family, IHeaderMatcher matcher, ILicense derivedFrom, String notes) {
        this.family = family;
        this.matcher = matcher;
        this.derivedFrom = derivedFrom;
        this.notes = notes;
    }

    @Override
    public String toString() {
        return family.toString();
    }

    public ILicenseFamily getFamily() {
        return family;
    }

    public void setFamily(ILicenseFamily family) {
        this.family = family;
    }

    public IHeaderMatcher getMatcher() {
        return matcher;
    }

    public void setMatcher(IHeaderMatcher matcher) {
        this.matcher = matcher;
    }

    public void setDerivedFrom(ILicense derivedFrom) {
        this.derivedFrom = derivedFrom;
    }

    @Override
    public String getId() {
        return matcher.getId();
    }

    @Override
    public void reset() {
        matcher.reset();
    }

    @Override
    public boolean matches(String line) {
        return matcher.matches(line);
    }

    @Override
    public ILicenseFamily getLicenseFamily() {
        return family;
    }

    @Override
    public int compareTo(ILicense other) {
        return ILicense.getComparator().compare(this, other);
    }

    @Override
    public String getNotes() {
        return notes;
    }

    @Override
    public ILicense derivedFrom() {
        return derivedFrom;
    }
}
