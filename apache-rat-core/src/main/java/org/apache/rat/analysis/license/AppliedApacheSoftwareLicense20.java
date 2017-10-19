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

import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;

/**
 * Matches an applied AL 2.0 License header, including a <em>required</em>
 * initial copyright header line, conforming the <a href="https://apache.org/licenses/LICENSE-2.0.html#apply">template</a>
 * from the AL 2.0 license itself.
 *
 * @since Rat 0.9
 */
public class AppliedApacheSoftwareLicense20 extends CopyrightHeader {

    public static final String ASL20_LICENSE_DEFN
            = "Licensed under the Apache License, Version 2.0 (the \"License\");\n"
            + "you may not use this file except in compliance with the License.\n"
            + "You may obtain a copy of the License at\n"
            + "http://www.apache.org/licenses/LICENSE-2.0\n"
            + "Unless required by applicable law or agreed to in writing, software\n"
            + "distributed under the License is distributed on an \"AS IS\" BASIS,\n"
            + "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"
            + "See the License for the specific language governing permissions and\n"
            + "limitations under the License.\n";

    private final FullTextMatchingLicense textMatcher;

    public AppliedApacheSoftwareLicense20() {
        super(MetaData.RAT_LICENSE_FAMILY_CATEGORY_DATUM_ASL, MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_APACHE_LICENSE_VERSION_2_0,"");
        textMatcher = new FullTextMatchingLicense(MetaData.RAT_LICENSE_FAMILY_CATEGORY_DATUM_ASL, MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_APACHE_LICENSE_VERSION_2_0,"",ASL20_LICENSE_DEFN);
    }

    public AppliedApacheSoftwareLicense20(String copyrightOwner) {
        this();
        setCopyrightOwner(copyrightOwner);
    }

    @Override
    public boolean match(Document subject, String s) throws RatHeaderAnalysisException {
        if (isCopyrightMatch()) {
            return textMatcher.match(subject, s); // will report the match if it has occurred
        }
        else {
            matchCopyright(s);
        }
        return false;
    }

    @Override
    public void reset() {
        super.reset();
        textMatcher.reset();
    }
}
