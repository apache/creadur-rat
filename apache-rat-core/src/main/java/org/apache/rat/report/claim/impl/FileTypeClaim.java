package org.apache.rat.report.claim.impl;

import org.apache.rat.document.IResource;
import org.apache.rat.report.claim.FileType;
import org.apache.rat.report.claim.IClaim;


/**
 * Implementation of {@link IClaim}, which asserts that
 * the subject has a particular file type.
 */
public class FileTypeClaim extends AbstractClaim {
    private final FileType type;

    /**
     * Creates a new instance with the given file type.
     */
    public FileTypeClaim(IResource pSubject, FileType pType) {
        super(pSubject);
        type = pType;
    }

    /**
     * Returns the file type.
     */
    public FileType getType() {
        return type;
    }
}
