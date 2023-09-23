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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.apache.rat.analysis.IHeaderMatcher;
import org.junit.Test;

public class AndMatcherTest {

    @Test
    public void standardTest() {
        IHeaderMatcher one = mock(IHeaderMatcher.class);
        IHeaderMatcher two = mock(IHeaderMatcher.class);
        when(one.matches(any())).thenReturn( true ).thenReturn(false);
        when(two.matches(any())).thenReturn( false ).thenReturn(true);
        
        AndMatcher target = new AndMatcher( "Testing", Arrays.asList( one, two ));
        assertFalse( target.matches("hello"));
        assertTrue( target.matches("world"));
    }
}
