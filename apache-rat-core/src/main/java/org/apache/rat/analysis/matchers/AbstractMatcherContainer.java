package org.apache.rat.analysis.matchers;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.rat.analysis.IHeaderMatcher;

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
}
