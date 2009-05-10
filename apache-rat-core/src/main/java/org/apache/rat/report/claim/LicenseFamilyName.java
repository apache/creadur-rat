package org.apache.rat.report.claim;


/**
 * A license family name.
 */
public class LicenseFamilyName {
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

    public static final LicenseFamilyName UNKNOWN_LICENSE_FAMILY = new LicenseFamilyName("?????");
}
