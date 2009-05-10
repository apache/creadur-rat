package org.apache.rat.report.claim.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.rat.report.RatReportFailedException;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.report.claim.IClaim;
import org.apache.rat.report.claim.IClaimReporter;
import org.apache.rat.report.claim.LicenseFamilyCode;
import org.apache.rat.report.claim.impl.xml.CustomClaim;


/**
 * The aggregator is used to create a numerical statistic
 * of claims.
 */
public class ClaimAggregator extends AbstractClaimReporter {
    private final IClaimReporter reporter;
    private final Map numsByLicenseFamilyName = new HashMap();
    private final Map numsByLicenseFamilyCode = new HashMap();
    private final Map numsByFileType = new HashMap();
    private int numApproved, numUnApproved, numGenerated, numCustom, numUnknown;

    public ClaimAggregator(IClaimReporter pReporter) {
        reporter = pReporter;
    }
    
    protected void handleClaim(CustomClaim pClaim) {
        ++numCustom;
    }

    private void incMapValue(Map pMap, Object pKey) {
        final Integer num = (Integer) pMap.get(pKey);
        final int newNum;
        if (num == null) {
            newNum = 1;
        } else {
            newNum = num.intValue() + 1;
        }
        pMap.put(pKey, new Integer(newNum));
    }
    
    protected void handleClaim(FileTypeClaim pClaim) {
        incMapValue(numsByFileType, pClaim.getType());
    }

    protected void handleClaim(LicenseApprovalClaim pClaim) {
        if (pClaim.isApproved()) {
            numApproved++;
        } else {
            numUnApproved++;
        }
    }

    protected void handleClaim(LicenseFamilyClaim pClaim) {
        super.handleClaim(pClaim);
        incMapValue(numsByLicenseFamilyName, pClaim.getLicenseFamilyName());
    }

    protected void handleClaim(LicenseHeaderClaim pClaim) {
        incMapValue(numsByLicenseFamilyCode, pClaim.getLicenseFamilyCode());
        if (pClaim.getLicenseFamilyCode().equals(LicenseFamilyCode.GENERATED)) {
            numGenerated++;
        } else if (pClaim.getLicenseFamilyCode().equals(LicenseFamilyCode.UNKNOWN)) {
            numUnknown++;
        }
    }

    public void fillClaimStatistic(ClaimStatistic pStatistic) {
        pStatistic.setFileTypeMap(numsByFileType);
        pStatistic.setLicenseFileCodeMap(numsByLicenseFamilyCode);
        pStatistic.setLicenseFileNameMap(numsByLicenseFamilyName);
        pStatistic.setNumApproved(numApproved);
        pStatistic.setNumCustom(numCustom);
        pStatistic.setNumGenerated(numGenerated);
        pStatistic.setNumUnApproved(numUnApproved);
        pStatistic.setNumUnknown(numUnknown);
    }

    public void claim(IClaim pClaim) throws RatReportFailedException {
        super.claim(pClaim);
        if (reporter != null) {
            reporter.claim(pClaim);
        }
    }
}
