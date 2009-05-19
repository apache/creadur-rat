package org.apache.rat.report.claim.impl;

import org.apache.rat.document.IDocument;
import org.apache.rat.report.claim.IClaim;



/**
 * Abstract base class for deriving instances of {@link IClaim}.
 */
public abstract class AbstractClaim implements IClaim {
    private final IDocument subject;

    protected AbstractClaim(IDocument pSubject) {
        subject = pSubject;
    }
    
    public IDocument getSubject() {
        return subject;
    }
}
