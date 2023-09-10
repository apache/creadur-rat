package org.apache.rat.analysis.matchers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.license.ILicenseFamily;

public abstract class AbstractMatcherContainer extends AbstractHeaderMatcher {

    protected Collection<IHeaderMatcher> enclosed;

    public AbstractMatcherContainer(String id, Collection<? extends IHeaderMatcher> enclosed) {
        super(id);
        this.enclosed = new ArrayList<>(enclosed);
    }

    public AbstractMatcherContainer(Collection<? extends IHeaderMatcher> enclosed) {
        super();
        this.enclosed = new ArrayList<>(enclosed);
    }

    @Override
    public void reset() {
        enclosed.stream().forEach(x -> x.reset());
    }

    @Override
    public final void reportFamily(Consumer<ILicenseFamily> consumer) {
        enclosed.forEach(x -> x.reportFamily(consumer));
    }

    @Override
    public final void extractMatcher(Consumer<IHeaderMatcher> matchers, Predicate<ILicenseFamily> comparator) {
        enclosed.forEach(x -> x.extractMatcher(matchers, comparator));
    }

}
