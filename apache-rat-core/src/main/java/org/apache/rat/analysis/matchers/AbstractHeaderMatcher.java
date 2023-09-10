package org.apache.rat.analysis.matchers;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.api.MetaData;
import org.apache.rat.license.ILicenseFamily;

public abstract class AbstractHeaderMatcher implements IHeaderMatcher {
    
    private String id;
    
    protected AbstractHeaderMatcher() {
        this(UUID.randomUUID().toString());
    }
    
    protected AbstractHeaderMatcher(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void reportFamily(Consumer<ILicenseFamily> consumer) {
        // by default do nothing.
    }

    @Override
    public void extractMatcher(Consumer<IHeaderMatcher> consumer, Predicate<ILicenseFamily> comparator) {
        // by default do nothing
    }

    @Override
    public String toString() {
        return getId();
    }
}
