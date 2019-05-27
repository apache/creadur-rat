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

import org.apache.rat.api.MetaData;

/**
 * Matches Apache License, Version 2.0
 *
 */
public final class ApacheSoftwareLicense20 extends SimplePatternBasedLicense {
    public static final String FIRST_LICENSE_LINE = "Licensed under the Apache License, Version 2.0 (the \"License\")";
    public static final String FIRST_LICENSE_LINE_SHORT = "Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.";
    public static final String LICENSE_REFERENCE_LINE = "http://www.apache.org/licenses/LICENSE-2.0";

    // These are all allowed variants as mentioned in https://issues.apache.org/jira/browse/LEGAL-265
    public static final String LICENSE_URL_HTTP       = LICENSE_REFERENCE_LINE;
    public static final String LICENSE_URL_HTTPS      = "https://www.apache.org/licenses/LICENSE-2.0";
    public static final String LICENSE_URL_HTTP_HTML  = "http://www.apache.org/licenses/LICENSE-2.0.html";
    public static final String LICENSE_URL_HTTPS_HTML = "https://www.apache.org/licenses/LICENSE-2.0.html";
    public static final String LICENSE_URL_HTTP_TXT   = "http://www.apache.org/licenses/LICENSE-2.0.txt";
    public static final String LICENSE_URL_HTTPS_TXT  = "https://www.apache.org/licenses/LICENSE-2.0.txt";
    public ApacheSoftwareLicense20() {
        super(MetaData.RAT_LICENSE_FAMILY_CATEGORY_DATUM_ASL, MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_APACHE_LICENSE_VERSION_2_0,
                "", new String[]{FIRST_LICENSE_LINE, FIRST_LICENSE_LINE_SHORT,
                        LICENSE_URL_HTTP,       LICENSE_URL_HTTPS,
                        LICENSE_URL_HTTP_HTML,  LICENSE_URL_HTTPS_HTML,
                        LICENSE_URL_HTTP_TXT,   LICENSE_URL_HTTPS_TXT,});
    }
}

