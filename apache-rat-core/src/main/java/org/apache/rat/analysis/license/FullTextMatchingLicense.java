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
package org.apache.rat.analysis.license;

import org.apache.rat.configuration.builders.TextBuilder;
import org.apache.rat.license.ILicense;

/**
 * Accumulates all letters and numbers contained inside the header and
 * compares it to the full text of a given license (after reducing it
 * to letters and numbers as well).
 *
 * <p>The text comparison is case insensitive but assumes only
 * characters in the US-ASCII charset are being matched.</p>
 *
 * @deprecated Use new configuration options
 */
@Deprecated // Since 0.16
public class FullTextMatchingLicense extends BaseLicense {

    private String text;

    /** constructor */
    public FullTextMatchingLicense() {
    }

    /**
     * Set the text to match
     * @param text the text to match
     */
    public final void setFullText(String text) {
        this.text = text;
    }

    @Override
    public ILicense.Builder getLicense() {
        return ILicense.builder()
        .setLicenseFamilyCategory(getLicenseFamilyCategory())
        .setName(getLicenseFamilyName())
        .setMatcher( new TextBuilder().setText(text) )
        .setNotes(getNotes());
    }
}
