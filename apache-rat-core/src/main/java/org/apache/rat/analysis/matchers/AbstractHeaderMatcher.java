package org.apache.rat.analysis.matchers;

import java.util.UUID;

import org.apache.rat.analysis.IHeaderMatcher;

public abstract class AbstractHeaderMatcher implements IHeaderMatcher {

    private String id;

//    protected AbstractHeaderMatcher() {
//        this(null);
//    }

    protected AbstractHeaderMatcher(String id) {
        this.id = id == null ? UUID.randomUUID().toString() : id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return getId();
    }
}
