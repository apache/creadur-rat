package org.apache.rat.report.claim;

import org.apache.rat.api.MetaData;


/**
 * This class provides a pseudo enumeration for license family
 * codes.
 */
public class LicenseFamilyCode {
    private final String name;
    public static final LicenseFamilyCode TMF854 = new LicenseFamilyCode(MetaData.RAT_LICENSE_FAMILY_VALUE_TMF);
    public static final LicenseFamilyCode DOJO = new LicenseFamilyCode(MetaData.RAT_LICENSE_FAMILY_VALUE_DOJO);
    public static final LicenseFamilyCode W3C_CODE = new LicenseFamilyCode(MetaData.RAT_LICENSE_FAMILY_VALUE_W3C);
    public static final LicenseFamilyCode W3CD_CODE = new LicenseFamilyCode(MetaData.RAT_LICENSE_FAMILY_VALUE_W3CD);
    public static final LicenseFamilyCode OASIS_CODE = new LicenseFamilyCode(MetaData.RAT_LICENSE_FAMILY_VALUE_OASIS);
    public static final LicenseFamilyCode ASL_CODE = new LicenseFamilyCode(MetaData.RAT_LICENSE_FAMILY_VALUE_ASL);

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
