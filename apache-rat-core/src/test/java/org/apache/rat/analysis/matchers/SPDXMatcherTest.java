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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.IHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SPDXMatcherTest {

    IHeaderMatcher target1 = SPDXMatcherFactory.INSTANCE.create("hello");
    IHeaderMatcher target2 = SPDXMatcherFactory.INSTANCE.create("world");
    IHeaderMatcher target3 = SPDXMatcherFactory.INSTANCE.create("goodbye");

    @BeforeEach
    public void setup() {
        target1.reset();
    }

    @Test
    public void testMatch() {
        StringBuilder sb = new StringBuilder()
                .append("SPDX-License-Identifier: world").append(System.lineSeparator())
                .append("SPDX-License-Identifier: hello").append(System.lineSeparator());

        IHeaders headers =  AbstractMatcherTest.makeHeaders(sb.toString(),null);

        assertTrue(target1.matches(headers));
        assertTrue(target2.matches(headers));
        assertFalse(target3.matches(headers));
        target1.reset();
    }
}
