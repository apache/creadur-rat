package org.apache.rat.report.claim;

import org.apache.rat.document.IDocument;



/**
 * Default implementation of {@link ISubject}.
 */
public class BaseSubject implements ISubject {
    private final IDocument doc;

    /**
     * Creates a new instance.
     */
    public BaseSubject(IDocument document) {
        if (document == null) {
            throw new IllegalArgumentException("The document must not be null.");
        }
        doc = document;
    }
    
    public String getName() {
        return doc.getName();
    }

    public boolean equals(Object other) {
        if (other == null  ||  getClass() != other.getClass()) {
            return false;
        }
        return doc.equals(((BaseSubject) other).doc);
    }
}
