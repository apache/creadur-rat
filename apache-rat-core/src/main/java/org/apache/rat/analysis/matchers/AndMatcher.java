package org.apache.rat.analysis.matchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.analysis.license.BaseLicense;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.license.ILicenseFamily;

public class AndMatcher extends AbstractMatcherContainer {
    private boolean[] flags = null;
    
    public AndMatcher(String id, Collection<IHeaderMatcher> enclosed) {
        super(id, enclosed);
        flags = new boolean[enclosed.size()];
    }
    
    public AndMatcher(Collection<IHeaderMatcher> enclosed) {
        super(enclosed);
        flags = new boolean[enclosed.size()];
    }

    @Override
    public void reset() {
        super.reset();
        flags = new boolean[enclosed.size()];
    }

    @Override
    public boolean matches(String line) throws RatHeaderAnalysisException {
        boolean result = true;
        Iterator<IHeaderMatcher> iter = enclosed.iterator();
        int i=0;
        while(iter.hasNext()) {
            if (flags[i]) {
                iter.next();
            } else {
                flags[i] = iter.next().matches(line);
                result &= flags[i];
            }
        }
        return result;
    }
}
