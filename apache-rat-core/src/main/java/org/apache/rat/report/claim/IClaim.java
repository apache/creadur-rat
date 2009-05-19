package org.apache.rat.report.claim;

import org.apache.rat.document.IDocument;



/**
 * Claims are received by the report. A claim is
 * an information about a particular item, the
 * subject.
 */
public interface IClaim {
    /**
     * Returns the claims subject.
     */
    IDocument getSubject();
}
