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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.IHeaders;
import org.apache.rat.testhelpers.TestingMatcher;
import org.junit.jupiter.api.Test;

public class AndMatcherTest extends AbstractMatcherTest {

    @Test
    public void trueTest() {

        IHeaderMatcher one = new TestingMatcher("one", true, true, false, false);
        // only need 2 entries because when one is false two test does not get called.
        IHeaderMatcher two = new TestingMatcher("two", true, false);
        AndMatcher target = new AndMatcher("Testing", Arrays.asList(one,  two),null);
        assertValues(target, true, false, false, false);
        target.reset();
    }
}
