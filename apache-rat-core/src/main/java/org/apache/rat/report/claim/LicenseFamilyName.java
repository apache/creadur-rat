package org.apache.rat.report.claim;

import org.apache.rat.api.MetaData;


/**
 * A license family name.
 */
public class LicenseFamilyName {
    public static final LicenseFamilyName W3C_SOFTWARE_COPYRIGHT_NAME = new LicenseFamilyName(MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_W3C_SOFTWARE_COPYRIGHT);
    public static final LicenseFamilyName W3C_DOCUMENT_COPYRIGHT_NAME = new LicenseFamilyName(MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_W3C_DOCUMENT_COPYRIGHT);
    public static final LicenseFamilyName OASIS_OPEN_LICENSE_NAME = new LicenseFamilyName(MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_OASIS_OPEN_LICENSE);
    public static final LicenseFamilyName MODIFIED_BSD_LICENSE_NAME = new LicenseFamilyName(MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_MODIFIED_BSD_LICENSE);
    public static final LicenseFamilyName APACHE_SOFTWARE_LICENSE_NAME = new LicenseFamilyName(MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_APACHE_LICENSE_VERSION_2_0);
    public static final LicenseFamilyName ACADEMIC_FREE_LICENSE_FAMILY = new LicenseFamilyName(MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_ACADEMIC_FREE_LICENSE_VERSION_2_1);
    public static final LicenseFamilyName UNKNOWN_LICENSE_FAMILY = new LicenseFamilyName(MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_UNKNOWN);

    private final String name;
    
    public LicenseFamilyName(String pName) {
        name = pName;
    }

    public String getName() {
        return name;
    }

    public boolean equals(Object other) {
        if (other == null  ||  getClass() != other.getClass()) {
            return false;
        }
        return getName().equals(((LicenseFamilyName) other).getName());
    }

    public int hashCode() {
        return name.hashCode();
    }

}
