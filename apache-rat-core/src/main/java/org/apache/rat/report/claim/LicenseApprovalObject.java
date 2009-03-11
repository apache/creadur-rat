package org.apache.rat.report.claim;

import org.apache.rat.analysis.Claims;



/**
 * Implementation of {@link IObject} for the
 * {@link Claims#LICENSE_APPROVAL_PREDICATE}.
 */
public class LicenseApprovalObject implements IObject {
    private final boolean value;

    /**
     * Creates a new instance with the given value.
     */
    private LicenseApprovalObject(boolean pValue) {
        value = pValue;
    }
    
    public String getValue() {
        return String.valueOf(value);
    }

    public boolean getBooleanValue() {
        return value;
    }

    public static final LicenseApprovalObject TRUE = new LicenseApprovalObject(true);
    public static final LicenseApprovalObject FALSE = new LicenseApprovalObject(false);
}
