package org.apache.rat.report.claim.impl;

import org.apache.rat.document.IResource;
import org.apache.rat.report.claim.FileType;


/**
 * Extension of {@link FileTypeClaim}, which provides additional
 * information for archives.
 */
public class ArchiveFileTypeClaim extends FileTypeClaim {
    private final boolean readable;

    /**
     * Creates a new instance with the given subject.
     */
    public ArchiveFileTypeClaim(IResource pSubject, boolean pReadable) {
        super(pSubject, FileType.ARCHIVE);
        readable = pReadable;
    }

    /**
     * Returns, whether the archive is readable.
     */
    public boolean isReadable() {
        return readable;
    }
}
