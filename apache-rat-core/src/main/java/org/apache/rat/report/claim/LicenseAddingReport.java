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
package org.apache.rat.report.claim;

import java.io.File;
import java.io.IOException;

import org.apache.rat.analysis.UnknownLicense;
import org.apache.rat.annotation.AbstractLicenseAppender;
import org.apache.rat.annotation.ApacheV2LicenseAppender;
import org.apache.rat.api.Document;
import org.apache.rat.api.RatException;
import org.apache.rat.report.RatReport;

/**
 * A Report that adds licenses text to files.
 */
public class LicenseAddingReport implements RatReport {
    /** The license appender used to update files */
    private final AbstractLicenseAppender appender;

    /**
     * Creates a LicenseAddingReport that inserts the copyright message and may overwrite the files.
     * @param copyrightMsg The message to insert into the files. May be {@code null}.
     * @param overwrite if {@code true} will overwrite the files rather than create {@code .new} files.
     */
    public LicenseAddingReport(final String copyrightMsg, final boolean overwrite) {
        appender = copyrightMsg == null ? new ApacheV2LicenseAppender()
                : new ApacheV2LicenseAppender(copyrightMsg);
        appender.setOverwrite(overwrite);
    }

    @Override
    public void report(final Document document) throws RatException {
        if (document.getMetaData().licenses().anyMatch(lic -> lic.equals(UnknownLicense.INSTANCE))) {
            final File file = new File(document.getName().getName());
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
