package org.apache.rat.report.claim.impl;

import org.apache.rat.document.IResource;
import org.apache.rat.report.claim.IClaim;
import org.apache.rat.report.claim.LicenseFamilyCode;


/**
 * Implementation of {@link IClaim}, which indicates that
 * a certain type of license header has been detected in
 * the subject.
 */
public class LicenseHeaderClaim extends AbstractClaim {
    private final LicenseFamilyCode licenseFamilyCode;
    private final String headerSample;

    /**
     * Creates a new instance with the given subject, license
     * family code, and header sample.
     */
    public LicenseHeaderClaim(IResource pSubject, LicenseFamilyCode pCode, String pHeaderSample) {
        super(pSubject);
        licenseFamilyCode = pCode;
        headerSample = pHeaderSample;
    }

    /**
     * Returns the license family code.
     */
    public LicenseFamilyCode getLicenseFamilyCode() {
        return licenseFamilyCode;
    }

    /**
     * Returns the header sample.
     */
    public String getHeaderSample() {
        return headerSample;
    }
}
