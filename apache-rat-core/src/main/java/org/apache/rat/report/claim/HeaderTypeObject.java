package org.apache.rat.report.claim;

import org.apache.rat.analysis.Claims;


/**
 * Implementation of {@link BaseObject} for
 * {@link Claims#HEADER_TYPE_PREDICATE}.
 */
public class HeaderTypeObject extends BaseObject {
    /**
     * Creates a new instance with the given value.
     */
    public HeaderTypeObject(String pValue) {
        super(pValue);
    }

    public static final HeaderTypeObject GENERATED = new HeaderTypeObject("GEN  ");
    public static final HeaderTypeObject UNKNOWN = new HeaderTypeObject("?????");
    public static final HeaderTypeObject ARCHIVE = new HeaderTypeObject("archive");
    public static final HeaderTypeObject NOTICE = new HeaderTypeObject("notice");
    public static final HeaderTypeObject BINARY = new HeaderTypeObject("binary");
    public static final HeaderTypeObject STANDARD = new HeaderTypeObject("standard");
}
