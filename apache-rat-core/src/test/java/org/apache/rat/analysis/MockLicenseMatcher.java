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
package org.apache.rat.analysis;

import org.apache.rat.api.Document;

import java.util.ArrayList;
import java.util.List;

public class MockLicenseMatcher implements IHeaderMatcher {

    public final List<String> lines = new ArrayList<>();
    public int resets = 0;
    public boolean result = true;

    public boolean match(Document subject, String line) {
        lines.add(line);
        return result;
    }

    public void reset() {
        resets++;
    }

}
