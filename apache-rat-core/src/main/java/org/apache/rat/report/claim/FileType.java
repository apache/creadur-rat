package org.apache.rat.report.claim;


/**
 * This class is used to provide a pseudo enumeration
 * for possible file types.
 */
public class FileType {
    
    public static final String TYPE_STANDARD = "standard";
    public static final String TYPE_BINARY = "binary";
    public static final String TYPE_NOTICE = "notice";
    public static final String TYPE_ARCHIVE = "archive";
    public static final String TYPE_UNKNOWN = "?????";
    public static final String TYPE_GENERATED = "GEN  ";
    
    public static final String RAT_FILE_CATEGORY ="http://org/apache/rat/meta-data#FileCategory";
    
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

    public static final FileType GENERATED = new FileType(TYPE_GENERATED);
    public static final FileType UNKNOWN = new FileType(TYPE_UNKNOWN);
    public static final FileType ARCHIVE = new FileType(TYPE_ARCHIVE);
    public static final FileType NOTICE = new FileType(TYPE_NOTICE);
    public static final FileType BINARY = new FileType(TYPE_BINARY);
    public static final FileType STANDARD = new FileType(TYPE_STANDARD);
}
