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
package org.apache.rat.document.impl.guesser;

import static org.junit.Assert.assertTrue;

import org.apache.rat.document.MockDocument;
import org.junit.Test;

public class NoteGuesserTest {

    @Test
    public void testMatches() {
        assertThatGuesserMatches("LICENSE");
        assertThatGuesserMatches("LICENSE.txt");
        assertThatGuesserMatches("NOTICE");
        assertThatGuesserMatches("NOTICE.txt");
        assertThatGuesserMatches("README");
        assertThatGuesserMatches("README.txt");
        assertThatGuesserMatches("src/test/LICENSE");
        assertThatGuesserMatches("src/test/LICENSE.txt");
        assertThatGuesserMatches("src/test/NOTICE");
        assertThatGuesserMatches("src/test/NOTICE.txt");
        assertThatGuesserMatches("src/test/README");
        assertThatGuesserMatches("src/test/README.txt");
        assertThatGuesserMatches("src\\test\\LICENSE");
        assertThatGuesserMatches("src\\test\\LICENSE.txt");
        assertThatGuesserMatches("src\\test\\NOTICE");
        assertThatGuesserMatches("src\\test\\NOTICE.txt");
        assertThatGuesserMatches("src\\test\\README");
        assertThatGuesserMatches("src\\test\\README.txt");
    }

    private void assertThatGuesserMatches(final String name) {
        assertTrue(new NoteGuesser().matches(new MockDocument(name)));
    }
}
