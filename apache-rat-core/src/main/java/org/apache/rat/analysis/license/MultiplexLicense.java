package org.apache.rat.analysis.license;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.api.Document;
import org.apache.rat.license.ILicenseFamily;

public class MultiplexLicense implements IHeaderMatcher {
    
    private Collection<IHeaderMatcher> enclosed;
    private String id;
    
    public MultiplexLicense(String id, Collection<IHeaderMatcher> enclosed) {
        this.id = id;
        this.enclosed = new ArrayList<>(enclosed);
    }

    @Override
    public String toString() {
        return getId();
    }
    
    public String getId() {
        return id;
    }
    
    @Override
    public void reset() {
        enclosed.stream().forEach(x -> x.reset());
    }

    @Override
    public boolean match(Document subject, String line) throws RatHeaderAnalysisException {
        for (IHeaderMatcher matcher : enclosed) {
            if (matcher.match(subject, line)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void reportFamily(Consumer<ILicenseFamily> consumer) {
        enclosed.forEach(x -> x.reportFamily(consumer));
    }
    
    @Override
    public void extractMatcher(Consumer<IHeaderMatcher> matchers, Predicate<ILicenseFamily> comparator) {
        enclosed.forEach(x -> x.extractMatcher(matchers, comparator));
    }

}
