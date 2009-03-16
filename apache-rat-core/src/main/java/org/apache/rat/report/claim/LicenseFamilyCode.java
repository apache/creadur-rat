package org.apache.rat.report.claim;


/**
 * This class provides a pseudo enumeration for license family
 * codes.
 */
public class LicenseFamilyCode {
    private final String name;

    /**
     * Creates a new instance with the given family code.
     */
    public LicenseFamilyCode(String pName) {
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
        return getName().equals(((LicenseFamilyCode) pOther).getName());
    }

    public static final LicenseFamilyCode GENERATED = new LicenseFamilyCode("GEN  ");
    public static final LicenseFamilyCode UNKNOWN = new LicenseFamilyCode("?????");
}
