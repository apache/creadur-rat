package org.apache.rat;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.license.ILicenseFamily;


/**
 * A configuration object is used by the frontend to invoke the
 * {@link Report}. Basically, the sole purpose of the frontends is
 * to create the configuration and invoke the {@link Report}.
 */
public class ReportConfiguration {
    private IHeaderMatcher headerMatcher;
    private ILicenseFamily[] approvedLicenseNames;
    private boolean addingLicenses;
    private boolean addingLicensesForced;
    private String copyrightMessage;

    /**
     * Returns the header matcher.
     */
    public IHeaderMatcher getHeaderMatcher() {
        return headerMatcher;
    }

    /**
     * Sets the header matcher.
     */
    public void setHeaderMatcher(IHeaderMatcher headerMatcher) {
        this.headerMatcher = headerMatcher;
    }

    /**
     * Returns the set of approved license names.
     */
    public ILicenseFamily[] getApprovedLicenseNames() {
        return approvedLicenseNames;
    }

    /**
     * Sets the set of approved license names.
     */
    public void setApprovedLicenseNames(ILicenseFamily[] approvedLicenseNames) {
        this.approvedLicenseNames = approvedLicenseNames;
    }

    /**
     * If RAT is adding license headers: Returns the optional
     * copyright message. This value is ignored, if no
     * license headers are added.
     * @see #isAddingLicenses()
     */
    public String getCopyrightMessage() {
        return copyrightMessage;
    }

    /**
     * If RAT is adding license headers: Sets the optional
     * copyright message. This value is ignored, if no
     * license headers are added.
     * @see #setAddingLicenses(boolean)
     */
    public void setCopyrightMessage(String copyrightMessage) {
        this.copyrightMessage = copyrightMessage;
    }

    /**
     * If RAT is adding license headers: Returns, whether adding
     * license headers is enforced. This value is ignored, if no
     * license headers are added.
     * @see #isAddingLicenses()
     */
    public boolean isAddingLicensesForced() {
        return addingLicensesForced;
    }

    /**
     * If RAT is adding license headers: Sets, whether adding
     * license headers is enforced. This value is ignored, if no
     * license headers are added.
     * @see #isAddingLicenses()
     */
    public void setAddingLicensesForced(boolean addingLicensesForced) {
        this.addingLicensesForced = addingLicensesForced;
    }

    /**
     * Returns, whether RAT should add missing license headers.
     * @see #isAddingLicensesForced()
     * @see #getCopyrightMessage()
     */
    public boolean isAddingLicenses() {
        return addingLicenses;
    }

    /**
     * Returns, whether RAT should add missing license headers.
     * @see #setAddingLicensesForced(boolean)
     * @see #setCopyrightMessage(String)
     */
    public void setAddingLicenses(boolean addingLicenses) {
        this.addingLicenses = addingLicenses;
    }

    
}
