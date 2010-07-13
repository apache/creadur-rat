package org.apache.rat.report.claim.util;

import java.io.File;
import java.io.IOException;

import org.apache.rat.annotation.AbstractLicenceAppender;
import org.apache.rat.annotation.ApacheV2LicenceAppender;
import org.apache.rat.api.MetaData;
import org.apache.rat.api.MetaData.Datum;
import org.apache.rat.api.RatException;
import org.apache.rat.report.AbstractReport;


public class LicenseAddingReport extends AbstractReport {
    private final AbstractLicenceAppender appender;

    public LicenseAddingReport(String pCopyrightMsg, boolean pForced) {
        appender = pCopyrightMsg == null ? new ApacheV2LicenceAppender() : new ApacheV2LicenceAppender(pCopyrightMsg);
        appender.setForce(pForced);
    }

    public void report(org.apache.rat.api.Document document) throws RatException {
        final MetaData metaData = document.getMetaData();
        final Datum licenseHeader = metaData.get(MetaData.RAT_URL_HEADER_CATEGORY);
        if (licenseHeader == null
                ||  MetaData.RAT_LICENSE_FAMILY_CATEGORY_DATUM_UNKNOWN.getValue().equals(licenseHeader.getValue())) {
            final File file = new File(document.getName());
            if (file.isFile()) {
                try {
                    appender.append(file);
                } catch (IOException e) {
                    throw new RatException(e.getMessage(), e);
                }
            }
        }
        metaData.getData();
    }
}
