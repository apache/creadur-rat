package org.example;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.license.BaseLicense;
import org.apache.rat.report.claim.IClaimReporter;
import org.apache.rat.analysis.RatHeaderAnalysisException;


public class Matcher extends BaseLicense implements IHeaderMatcher {
    public Matcher() {
    	super("EXMPL", "Example License", "");
    }
    public void reset() {}
    
    public boolean match(String subject, String line, IClaimReporter reporter) throws RatHeaderAnalysisException {
    	reportOnLicense(subject, reporter);
    	return true;
    }
}
