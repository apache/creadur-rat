/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.example;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.analysis.license.BaseLicense;
import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;


public class Matcher extends BaseLicense implements IHeaderMatcher {
    public Matcher() {
        super(new MetaData.Datum(MetaData.RAT_URL_LICENSE_FAMILY_CATEGORY, "EXMPL"),
                new MetaData.Datum(MetaData.RAT_URL_LICENSE_FAMILY_NAME, "Example License"), "");
    }
    public void reset() {}
    
    public boolean match(Document subject, String line) throws RatHeaderAnalysisException {
        reportOnLicense(subject);
        return true;
    }
}
