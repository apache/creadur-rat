package org.apache.rat.report.claim.impl;

import org.apache.rat.document.IResource;
import org.apache.rat.report.claim.IClaim;


/**
 * Implementation of {@link IClaim}, which indicates, whether
 * the configured RAT policy is approving a license or not.
 */
public class LicenseApprovalClaim extends AbstractClaim {
    private final boolean approved;

    /**
     * Creates a new instance with the given subject,
     * which indicates whether a license was approved or
     * not.
     */
    public LicenseApprovalClaim(IResource pSubject, boolean pApproved) {
        super(pSubject);
        approved = pApproved;
    }

    /**
     * Returns, whether the license was approved.
     */
    public boolean isApproved() {
        return approved;
    }
}
