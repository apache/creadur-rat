package org.apache.rat.report.claim.impl;

import org.apache.rat.document.IDocument;
import org.apache.rat.report.claim.IClaim;
import org.apache.rat.report.claim.LicenseFamilyCode;


/**
 * Implementation of {@link IClaim}, which indicates that
 * a certain type of license header has been detected in
 * the subject.
 */
public class LicenseHeaderClaim extends AbstractClaim {
    private final LicenseFamilyCode licenseFamilyCode;

    /**
     * Creates a new instance with the given subject, license
     * family code, and header sample.
     */
    public LicenseHeaderClaim(IDocument pSubject, LicenseFamilyCode pCode) {
        super(pSubject);
        licenseFamilyCode = pCode;
    }

    /**
     * Returns the license family code.
     */
    public LicenseFamilyCode getLicenseFamilyCode() {
        return licenseFamilyCode;
    }
}
