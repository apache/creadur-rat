package org.apache.rat.report.claim.impl;

import org.apache.rat.document.IDocument;
import org.apache.rat.report.RatReportFailedException;
import org.apache.rat.report.claim.IClaim;
import org.apache.rat.report.claim.IClaimReporter;
import org.apache.rat.report.claim.impl.xml.CustomClaim;


/**
 * Abstract base implementation of {@link IClaimReporter}.
 * It is strongly suggested, that implementations derive from
 * this class.
 */
public abstract class AbstractClaimReporter implements IClaimReporter {

    protected void handleClaim(FileTypeClaim pClaim) {
        // Does nothing
    }

    protected void handleClaim(LicenseApprovalClaim pClaim) {
        // Does nothing
    }

    protected void handleClaim(LicenseFamilyClaim pClaim) {
        handleClaim((LicenseHeaderClaim) pClaim);
    }

    protected void handleClaim(LicenseHeaderClaim pClaim) {
        // Does nothing
    }

    protected void handleClaim(CustomClaim pClaim) {
        // Does nothing
    }

    protected void handleClaim(IClaim pClaim) {
        if (pClaim instanceof FileTypeClaim) {
            handleClaim((FileTypeClaim) pClaim);
        } else if (pClaim instanceof LicenseApprovalClaim) {
            handleClaim((LicenseApprovalClaim) pClaim);
        } else if (pClaim instanceof LicenseFamilyClaim) {
            handleClaim((LicenseFamilyClaim) pClaim);
        } else if (pClaim instanceof LicenseHeaderClaim) {
            handleClaim((LicenseHeaderClaim) pClaim);
        } else if (pClaim instanceof CustomClaim) {
            handleClaim((CustomClaim) pClaim);
        } else {
            throw new IllegalStateException("Unsupported type of claim: " + pClaim.getClass().getName());
        }
    }

    public void claim(IClaim pClaim) throws RatReportFailedException {
        handleClaim(pClaim);
    }

    public void report(IDocument document) throws RatReportFailedException {}
}
