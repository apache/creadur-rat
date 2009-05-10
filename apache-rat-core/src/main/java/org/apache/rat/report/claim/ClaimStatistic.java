package org.apache.rat.report.claim;

import java.util.Map;

import org.apache.rat.report.claim.impl.xml.CustomClaim;


/**
 * This class provides a numerical overview about
 * the report.
 */
public class ClaimStatistic {
    private Map fileTypeMap, licenseFamilyCodeMap, licenseFamilyNameMap;
    private int numApproved, numUnApproved, numGenerated, numUnknown, numCustom;

    /**
     * Returns the number of files with approved licenses.
     */
    public int getNumApproved() {
        return numApproved;
    }

    /**
     * Sets the number of files with approved licenses.
     */
    public void setNumApproved(int pNumApproved) {
        numApproved = pNumApproved;
    }

    /**
     * Returns the number of files with unapproved licenses.
     * {@em Note:} This might include files with unknown
     * licenses.
     * @see #getNumUnknown()
     */
    public int getNumUnApproved() {
        return numUnApproved;
    }

    /**
     * Returns the number of files with unapproved licenses.
     * {@em Note:} This might include files with unknown
     * licenses.
     * @see #setNumUnknown(int)
     */
    public void setNumUnApproved(int pNumUnApproved) {
        numUnApproved = pNumUnApproved;
    }

    /**
     * Returns the number of generated files.
     */
    public int getNumGenerated() {
        return numGenerated;
    }

    /**
     * Returns the number of generated files.
     */
    public void setNumGenerated(int pNumGenerated) {
        numGenerated = pNumGenerated;
    }

    /**
     * Returns the number of files, which are neither
     * generated nor have a known license header.
     */
    public int getNumUnknown() {
        return numUnknown;
    }

    /**
     * Sets the number of files, which are neither
     * generated nor have a known license header.
     */
    public void setNumUnknown(int pNumUnknown) {
        numUnknown = pNumUnknown;
    }

    /**
     * Returns the number of custom claims.
     * @see CustomClaim
     */
    public int getNumCustom() {
        return numCustom;
    }

    /**
     * Sets the number of custom claims.
     * @see CustomClaim
     */
    public void setNumCustom(int pNumCustom) {
        numCustom = pNumCustom;
    }

    /**
     * Sets a map with the file types. The map keys
     * are instances of {@link FileType} and the map values
     * are integers with the number of resources matching
     * the file type.
     */
    public void setFileTypeMap(Map pFileTypeMap) {
        fileTypeMap = pFileTypeMap;
    }

    /**
     * Returns a map with the file types. The map keys
     * are instances of {@link FileType} and the map values
     * are integers with the number of resources matching
     * the file type.
     */
    public Map getFileTypeMap() {
        return fileTypeMap;
    }

    /**
     * Returns a map with the license family codes. The map
     * keys are instances of {@link LicenseFamilyCode} and
     * the map values are integers with the number of resources
     * matching the license family code.
     */
    public Map getLicenseFileCodeMap() {
        return licenseFamilyCodeMap;
    }

    /**
     * Sets a map with the license family codes. The map
     * keys are instances of {@link LicenseFamilyCode} and
     * the map values are integers with the number of resources
     * matching the license family code.
     */
    public void setLicenseFileCodeMap(Map pLicenseFamilyCodeMap) {
        licenseFamilyCodeMap = pLicenseFamilyCodeMap;
    }

    /**
     * Returns a map with the license family codes. The map
     * keys are instances of {@link LicenseFamilyName} and
     * the map values are integers with the number of resources
     * matching the license family name.
     */
    public Map getLicenseFileNameMap() {
        return licenseFamilyNameMap;
    }

    /**
     * Returns a map with the license family codes. The map
     * keys are instances of {@link LicenseFamilyName} and
     * the map values are integers with the number of resources
     * matching the license family name.
     */
    public void setLicenseFileNameMap(Map pLicenseFamilyNameMap) {
        licenseFamilyNameMap = pLicenseFamilyNameMap;
    }
}
