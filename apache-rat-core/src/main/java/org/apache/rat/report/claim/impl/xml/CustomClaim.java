/**
 * 
 */
package org.apache.rat.report.claim.impl.xml;

import org.apache.rat.document.IDocument;
import org.apache.rat.report.claim.IClaim;


/**
 * Custom claim implementation, basically used by the test
 * suite and possibly not so useful otherwise.
 */
public class CustomClaim implements IClaim {
    private final IDocument subject;
    private final String predicate;
    private final String object;
    private final boolean isLiteral;

    public CustomClaim(final IDocument subject, final String predicate, final String object, final boolean isLiteral) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.isLiteral = isLiteral;
    }
    
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof CustomClaim) {
            CustomClaim claim = (CustomClaim) obj;
            result = subject.equals(claim.subject) && predicate.equals(claim.predicate) 
            && object.equals(claim.object) && isLiteral == claim.isLiteral;
        }
        return result;
    }
    
    public IDocument getSubject() {
        return subject;
    }

    public String getPredicate() {
        return predicate;
    }

    public String getObject() {
        return object;
    }

    public boolean isLiteral() {
        return isLiteral;
    }
}