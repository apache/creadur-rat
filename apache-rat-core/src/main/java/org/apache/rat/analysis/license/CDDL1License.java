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

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;

/**
 * Base CDDL 1.0 license.
 */
public class CDDL1License extends BaseLicense implements IHeaderMatcher {

    public static final String CDDL1_LICENSE_DEFN
            = "The contents of this file are subject to the terms of the Common Development\n"
            + "and Distribution License(\"CDDL\") (the \"License\"). You may not use this file\n"
            + "except in compliance with the License.\n\n";

    public static final String CDDL1_LICENSE_DEFN_ILLUMOS_STYLE
            = "The contents of this file are subject to the terms of the\n"
            + "Common Development and Distribution License (the \"License\")\n"
            + "You may not use this file except in compliance with the License.\n";

    private final FullTextMatchingLicense textMatcherBase;
    private final FullTextMatchingLicense textMatcherIllumosStyle;


    public CDDL1License() {
        super(MetaData.RAT_LICENSE_FAMILY_CATEGORY_DATUM_CDLL1,
                MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_CDDL1,
                "");
        textMatcherBase = new FullTextMatchingLicense(MetaData.RAT_LICENSE_FAMILY_CATEGORY_DATUM_CDLL1,
                MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_CDDL1,
                "", CDDL1_LICENSE_DEFN);
        textMatcherIllumosStyle = new FullTextMatchingLicense(MetaData.RAT_LICENSE_FAMILY_CATEGORY_DATUM_CDLL1,
                MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_CDDL1,
                "", CDDL1_LICENSE_DEFN_ILLUMOS_STYLE);
    }

    public boolean match(Document subject, String s) throws RatHeaderAnalysisException {
        return textMatcherBase.match(subject, s) ||
                textMatcherIllumosStyle.match(subject, s);
    }

    public void reset() {
        textMatcherBase.reset();
        textMatcherIllumosStyle.reset();
    }

}
