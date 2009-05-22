package org.apache.rat.report.claim.impl;

import org.apache.rat.api.MetaData;
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

    private IDocument subject;
    private boolean writtenDocumentClaims = false;
    
    protected void handleClaim(FileTypeClaim pClaim) {
        // Does nothing
    }

    protected void handleClaim(LicenseApprovalClaim pClaim) {
        // Does nothing
    }

    protected void handleLicenseFamilyNameClaim(String licenseFamilyName) {
        // Does Nothing
    }

    protected void handleHeaderCategoryClaim(String headerCategory) {
        // Does nothing
    }

    protected void handleClaim(CustomClaim pClaim) {
        // Does nothing
    }

    protected void handleClaim(IClaim pClaim) {
        writeDocumentClaimsWhenNecessary(subject);
        if (pClaim instanceof FileTypeClaim) {
            handleClaim((FileTypeClaim) pClaim);
        } else if (pClaim instanceof LicenseApprovalClaim) {
            handleClaim((LicenseApprovalClaim) pClaim);
        } else if (pClaim instanceof CustomClaim) {
            handleClaim((CustomClaim) pClaim);
        } else {
            throw new IllegalStateException("Unsupported type of claim: " + pClaim.getClass().getName());
        }
    }
    
    public void claim(IClaim pClaim) throws RatReportFailedException {
        handleClaim(pClaim);
    }

    private void writeDocumentClaim(IDocument subject)  {
        final MetaData.Datum headerCategoryDatum = subject.getMetaData().get(MetaData.RAT_URL_HEADER_CATEGORY);
        if (headerCategoryDatum != null) {
            final String headerCategory = headerCategoryDatum.getValue();
            if (headerCategory != null) {
                handleHeaderCategoryClaim(headerCategory);
            }
        }
        final MetaData.Datum licenseFamilyNameDatum = subject.getMetaData().get(MetaData.RAT_URL_LICENSE_FAMILY_NAME);
        if (licenseFamilyNameDatum != null) {
            final String licenseFamilyName = licenseFamilyNameDatum.getName();
            if (licenseFamilyName != null) {
                handleLicenseFamilyNameClaim(licenseFamilyName);
            }
        }
    }
    
    public void report(IDocument subject) throws RatReportFailedException {
        writeDocumentClaimsWhenNecessary(subject);
        this.subject = subject;
        writtenDocumentClaims = false;
    }

    private void writeDocumentClaimsWhenNecessary(IDocument subject) {
        if (!writtenDocumentClaims && subject != null) {
            writeDocumentClaim(subject);
            writtenDocumentClaims = true;
        }
    }
}
