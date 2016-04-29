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

import java.util.regex.Pattern;

import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;

/**
 * Looks for documents contain the OASIS copyright claim plus derivative work clause.
 * Perhaps need to match more.
 */
public class OASISLicense extends FullTextMatchingLicense {

    private static final String COPYRIGHT_PATTERN_DEFN = ".*Copyright\\s.*OASIS Open.*";
    private static final String CLAUSE_DEFN
    = "This document and translations of it may be copied and furnished to others and derivative works" +
            "that comment on or otherwise explain it or assist in its implementation may be prepared" +
            "copied published and distributed";
    
    private static final Pattern COPYRIGHT_PATTERN = Pattern.compile(COPYRIGHT_PATTERN_DEFN);

    boolean copyrightMatch = false;
    
    public OASISLicense() {
        super(MetaData.RAT_LICENSE_FAMILY_CATEGORY_DATUM_OASIS,
              MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_OASIS_OPEN_LICENSE,
              "No modifications allowed",
              CLAUSE_DEFN);
    }

    @Override
    public boolean match(Document subject, String line) throws RatHeaderAnalysisException {
        boolean result = false;
        if (copyrightMatch) {
            result = super.match(subject, line);
        } else {
            copyrightMatch = COPYRIGHT_PATTERN.matcher(line).matches();
        }
        return result;
    }

    @Override
    public void reset() {
        copyrightMatch = false;
        super.reset();
    }
}
