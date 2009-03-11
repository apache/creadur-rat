package org.apache.rat.report.claim;


/**
 * A license family name.
 */
public class LicenseFamilyName extends BaseObject {
    public LicenseFamilyName(String name) {
        super(name);
    }

    public static final LicenseFamilyName UNKNOWN_LICENSE_FAMILY = new LicenseFamilyName("?????");
}
