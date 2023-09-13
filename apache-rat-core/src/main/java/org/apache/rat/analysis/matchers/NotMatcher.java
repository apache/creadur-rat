package org.apache.rat.analysis.matchers;

import org.apache.rat.analysis.IHeaderMatcher;

public class NotMatcher extends AbstractHeaderMatcher {

    private final IHeaderMatcher enclosed;

    public NotMatcher(IHeaderMatcher enclosed) {
        super();
        this.enclosed = enclosed;
    }

    public NotMatcher(String id, IHeaderMatcher enclosed) {
        super(id);
        this.enclosed = enclosed;
    }

    @Override
    public boolean matches(String line) {
        return !enclosed.matches(line);
    }

    @Override
    public void reset() {
        enclosed.reset();
    }
}
