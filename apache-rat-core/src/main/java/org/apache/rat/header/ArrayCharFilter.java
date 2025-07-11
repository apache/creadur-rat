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
package org.apache.rat.header;

import org.apache.rat.DeprecationReporter;

@Deprecated // since 0.17
@DeprecationReporter.Info(since = "0.17", forRemoval = true)
class ArrayCharFilter implements CharFilter {

    private final char[] filtered;
    private final int length;
    
    protected ArrayCharFilter(final char[] filtered) {
        super();
        DeprecationReporter.logDeprecated(this.getClass());
        this.filtered = filtered;
        length = filtered.length;
    }

    public boolean isFilteredOut(char character) {
        boolean result = false;
        for(int i = 0; i < length; i++) {
            if (character == filtered[i]) {
                result = true;
                break;
            }
        }
        return result;
    }

}
