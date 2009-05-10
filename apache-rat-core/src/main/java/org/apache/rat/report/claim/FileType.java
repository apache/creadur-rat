package org.apache.rat.report.claim;


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

    public static final FileType GENERATED = new FileType("GEN  ");
    public static final FileType UNKNOWN = new FileType("?????");
    public static final FileType ARCHIVE = new FileType("archive");
    public static final FileType NOTICE = new FileType("notice");
    public static final FileType BINARY = new FileType("binary");
    public static final FileType STANDARD = new FileType("standard");
}
