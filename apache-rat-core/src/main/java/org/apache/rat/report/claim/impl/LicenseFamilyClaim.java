package org.apache.rat.report.claim.impl;

import org.apache.rat.document.IDocument;
import org.apache.rat.report.claim.LicenseFamilyCode;
import org.apache.rat.report.claim.LicenseFamilyName;


/**
 * Implementation of claim, which is used to report that
 * the subjects header indicate a particular license family.
 */
public class LicenseFamilyClaim extends LicenseHeaderClaim {
    private final LicenseFamilyName licenseFamilyName;

    /**
     * Creates a new instance with the given subject and
     * the given license family name.
     */
    public LicenseFamilyClaim(IDocument pSubject, LicenseFamilyName pName,
            LicenseFamilyCode pCode, String pHeaderSample) {
        super(pSubject, pCode, pHeaderSample);
        licenseFamilyName = pName;
    }

    /**
     * Returns the license family name.
     */
    public LicenseFamilyName getLicenseFamilyName() {
        return licenseFamilyName;
    }
}
