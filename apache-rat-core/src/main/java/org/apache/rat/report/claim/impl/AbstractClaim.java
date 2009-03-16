package org.apache.rat.report.claim.impl;

import org.apache.rat.document.IResource;
import org.apache.rat.report.claim.IClaim;



/**
 * Abstract base class for deriving instances of {@link IClaim}.
 */
public abstract class AbstractClaim implements IClaim {
    private final IResource subject;

    protected AbstractClaim(IResource pSubject) {
        subject = pSubject;
    }
    
    public IResource getSubject() {
        return subject;
    }
}
