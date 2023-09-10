package org.apache.rat.license;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.api.MetaData;

public class SimpleLicense implements ILicense {

    ILicenseFamily family;
    IHeaderMatcher matcher;
    ILicense derivedFrom;
    String notes;
    
    public SimpleLicense(ILicenseFamily family, IHeaderMatcher matcher, ILicense derivedFrom, String notes)
    {
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

    public String getId() {
        return matcher.getId();
    }

    public void reset() {
        matcher.reset();
    }

    public boolean match(MetaData metadata, String line) throws RatHeaderAnalysisException {
        return matcher.match(metadata, line);
    }

    public void reportFamily(Consumer<ILicenseFamily> consumer) {
        matcher.reportFamily(consumer);
    }

    public void extractMatcher(Consumer<IHeaderMatcher> consumer, Predicate<ILicenseFamily> comparator) {
        matcher.extractMatcher(consumer, comparator);
    }

    @Override
    public ILicenseFamily getLicenseFamily() {
        return family;
    }

    @Override
    public String getNotes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ILicense derivedFrom() {
        return derivedFrom;
    }
}
