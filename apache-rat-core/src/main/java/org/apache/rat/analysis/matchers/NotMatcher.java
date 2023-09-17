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
package org.apache.rat.analysis.matchers;

import org.apache.rat.analysis.IHeaderMatcher;

public class NotMatcher extends AbstractHeaderMatcher {

    private final IHeaderMatcher enclosed;

    public NotMatcher(IHeaderMatcher enclosed) {
        this(null, enclosed);
    }

    public NotMatcher(String id, IHeaderMatcher enclosed) {
        super(id);
        this.enclosed = enclosed;
    }

    @Override
    public boolean matches(String line) {
        return !enclosed.matches(line);
    }

    @Override
    public void reset() {
        enclosed.reset();
    }
}
