package org.apache.rat.license;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.RatHeaderAnalysisException;

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
    public boolean matches(String line) throws RatHeaderAnalysisException {
        return matcher.matches(line);
    }

    @Override
    public void reportFamily(Consumer<ILicenseFamily> consumer) {
        matcher.reportFamily(consumer);
    }

    @Override
    public void extractMatcher(Consumer<IHeaderMatcher> consumer, Predicate<ILicenseFamily> comparator) {
        matcher.extractMatcher(consumer, comparator);
    }

    @Override
    public ILicenseFamily getLicenseFamily() {
        return family;
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
