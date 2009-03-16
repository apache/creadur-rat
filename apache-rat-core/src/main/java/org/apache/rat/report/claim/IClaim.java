package org.apache.rat.report.claim;

import org.apache.rat.document.IResource;



/**
 * Claims are received by the report. A claim is
 * an information about a particular item, the
 * subject.
 */
public interface IClaim {
    /**
     * Returns the claims subject.
     */
    IResource getSubject();
}
