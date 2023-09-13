package org.apache.rat.analysis.matchers;

import java.util.Collection;

import org.apache.rat.analysis.IHeaderMatcher;

public class OrMatcher extends AbstractMatcherContainer {

    public OrMatcher(Collection<IHeaderMatcher> enclosed) {
        super(enclosed);
    }

    public OrMatcher(String id, Collection<IHeaderMatcher> enclosed) {
        super(id, enclosed);
    }

    @Override
    public boolean matches(String line) {
        for (IHeaderMatcher matcher : enclosed) {
            if (matcher.matches(line)) {
                return true;
            }
        }
        return false;
    }
}
