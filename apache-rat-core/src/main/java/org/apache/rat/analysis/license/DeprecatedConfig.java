package org.apache.rat.analysis.license;

import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;

/**
 * A temoprary interface for deprecated configuration options.
 */
@Deprecated // remove in v1.0
public interface DeprecatedConfig {
    ILicense.Builder getLicense();
    ILicenseFamily getLicenseFamily();
}
