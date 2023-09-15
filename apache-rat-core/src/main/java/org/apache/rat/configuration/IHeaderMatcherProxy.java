package org.apache.rat.configuration;

import java.util.Map;

import org.apache.rat.analysis.IHeaderMatcher;

class IHeaderMatcherProxy implements IHeaderMatcher {
    private final String proxyId;
    private IHeaderMatcher wrapped;
    private Map<String,IHeaderMatcher> matchers;
    
    public static IHeaderMatcher create(String proxyId, Map<String,IHeaderMatcher> matchers) {
        IHeaderMatcher result = matchers.get(proxyId);
        return result != null ? result : new IHeaderMatcherProxy(proxyId, matchers);
    }

    private IHeaderMatcherProxy(String proxyId, Map<String,IHeaderMatcher> matchers) {
        this.proxyId = proxyId;
        this.matchers = matchers;
    }

    private void checkProxy() {
        if (wrapped == null) {
            wrapped = matchers.get(proxyId);
            if (wrapped == null) {
                throw new IllegalStateException(String.format("%s is not a valid matcher id", proxyId)); 
            }
            matchers = null;
        }
    }

    @Override
    public String getId() {
        checkProxy();
        return wrapped.getId();
    }

    @Override
    public void reset() {
        checkProxy();
        wrapped.reset();
    }

    @Override
    public boolean matches(String line) {
        checkProxy();
        return wrapped.matches(line);
    }
}
