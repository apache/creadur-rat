/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */
package org.apache.rat.report.claim.util;

import java.io.File;
import java.io.IOException;

import org.apache.rat.analysis.UnknownLicense;
import org.apache.rat.annotation.AbstractLicenseAppender;
import org.apache.rat.annotation.ApacheV2LicenseAppender;
import org.apache.rat.api.RatException;
import org.apache.rat.report.AbstractReport;
import org.apache.rat.utils.Log;

public class LicenseAddingReport extends AbstractReport {
    private final AbstractLicenseAppender appender;

    public LicenseAddingReport(final Log log, String pCopyrightMsg, boolean pForced) {
        appender = pCopyrightMsg == null ? new ApacheV2LicenseAppender(log)
                : new ApacheV2LicenseAppender(log, pCopyrightMsg);
        appender.setForce(pForced);
    }

    @Override
    public void report(org.apache.rat.api.Document document) throws RatException {
        if (document.getMetaData().licenses().anyMatch(lic -> lic.equals(UnknownLicense.INSTANCE))) {
            final File file = new File(document.getName());
            if (file.isFile()) {
                try {
                    appender.append(file);
                } catch (IOException e) {
                    throw new RatException(e.getMessage(), e);
                }
            }
        }
    }
}
