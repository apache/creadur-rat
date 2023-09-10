package org.apache.rat.analysis.matchers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.license.ILicenseFamily;

public class OrMatcher extends AbstractMatcherContainer {

    public OrMatcher(Collection<IHeaderMatcher> enclosed) {
        super(enclosed);
    }

    public OrMatcher(String id, Collection<IHeaderMatcher> enclosed) {
        super(id, enclosed);
    }

    @Override
    public boolean matches(String line) throws RatHeaderAnalysisException {
        for (IHeaderMatcher matcher : enclosed) {
            if (matcher.matches(line)) {
                return true;
            }
        }
        return false;
    }
}
