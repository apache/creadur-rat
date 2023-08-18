package org.apache.rat.analysis.license;

import java.util.Collection;

import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;

public class MultiplexLicense extends BaseLicense {
    
    private Collection<BaseLicense> enclosed;
    
    public MultiplexLicense(final MetaData.Datum licenseFamilyCategory, final MetaData.Datum licenseFamilyName, final String notes, Collection<BaseLicense> enclosed) {
        super(licenseFamilyCategory, licenseFamilyName, notes);
        this.enclosed = enclosed;
    }

    @Override
    public void reset() {
        enclosed.stream().forEach(x -> x.reset());
    }

    @Override
    public boolean match(Document subject, String line) throws RatHeaderAnalysisException {
        for (BaseLicense lic : enclosed) {
            if (lic.match(subject, line)) {
                return true;
            }
        }
        return false;
    }

}
