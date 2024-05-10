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

package org.apache.rat.report.claim.impl;

import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.api.RatException;
import org.apache.rat.license.ILicense;
import org.apache.rat.report.AbstractReport;


/**
 * Abstract base implementation of {@link AbstractReport}.
 * It is strongly suggested, that implementations derive from
 * this class.
 */
public abstract class AbstractClaimReporter extends AbstractReport {
    /**
     * Increment the document type counter.
     * The default implementations does nothing.
     * @param type The document type counter to increment.
     */
    protected void handleDocumentCategoryClaim(Document.Type type) {
        // Does nothing
    }

    /**
     * Increment the approved license claim.
     * The default implementation does nothing.
     * @param metadata The metadata for the document
     */
    protected void handleApprovedLicenseClaim(MetaData metadata) {
        // Does nothing
    }

    /**
     * Increment the counts associated with the license
     * The default implementation does nothing.
     * @param license the license to record the category for.
     */
    protected void handleLicenseClaim(ILicense license) {
        // Does nothing
    }

    @Override
    public void report(Document subject) throws RatException {
        final MetaData metaData = subject.getMetaData();
        metaData.licenses().forEach(this::handleLicenseClaim);
        handleDocumentCategoryClaim(metaData.getDocumentType());
        handleApprovedLicenseClaim(metaData);
    }
}
