package org.apache.rat.report.claim;

import org.apache.rat.api.MetaData;


/**
 * This class is used to provide a pseudo enumeration
 * for possible file types.
 */
public class FileType {
    
    private final String name;

    /**
     * Creates a new instance with the given type name.
     */
    public FileType(String pName) {
        name = pName;
    }

    /**
     * Returns the file types name.
     */
    public String getName() {
        return name;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object pOther) {
        if (pOther == null  ||  getClass() != pOther.getClass()) {
            return false;
        }
        return getName().equals(((FileType) pOther).getName());
    }

    public static final FileType GENERATED = new FileType(MetaData.RAT_DOCUMENT_CATEGORY_VALUE_GENERATED);
    public static final FileType UNKNOWN = new FileType(MetaData.RAT_DOCUMENT_CATEGORY_VALUE_UNKNOWN);
    public static final FileType ARCHIVE = new FileType(MetaData.RAT_DOCUMENT_CATEGORY_VALUE_ARCHIVE);
    public static final FileType NOTICE = new FileType(MetaData.RAT_DOCUMENT_CATEGORY_VALUE_NOTICE);
    public static final FileType BINARY = new FileType(MetaData.RAT_DOCUMENT_CATEGORY_VALUE_BINARY);
    public static final FileType STANDARD = new FileType(MetaData.RAT_DOCUMENT_CATEGORY_VALUE_STANDARD);
}
