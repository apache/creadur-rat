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

import org.apache.rat.document.MockDocument;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class NoteGuesserTest {

    @Test
    public void testMatches() {
        assertTrue(NoteGuesser.isNote(new MockDocument("DEPENDENCIES")));
        assertTrue(NoteGuesser.isNote(new MockDocument("LICENSE")));
        assertTrue(NoteGuesser.isNote(new MockDocument("LICENSE.txt")));
        assertTrue(NoteGuesser.isNote(new MockDocument("NOTICE")));
        assertTrue(NoteGuesser.isNote(new MockDocument("NOTICE.txt")));
        assertTrue(NoteGuesser.isNote(new MockDocument("README")));
        assertTrue(NoteGuesser.isNote(new MockDocument("README.txt")));
    }

    @Test
    public void isNote() {
        assertTrue(NoteGuesser.isNote("DEPENDENCIES"));
        assertTrue(NoteGuesser.isNote("LICENSE"));
        assertTrue(NoteGuesser.isNote("LICENSE.txt"));
        assertTrue(NoteGuesser.isNote("NOTICE"));
        assertTrue(NoteGuesser.isNote("NOTICE.txt"));
        assertTrue(NoteGuesser.isNote("README"));
        assertTrue(NoteGuesser.isNote("README.txt"));
    }
    
    @Test
    public void isNoteWithPath() {
        assertTrue(NoteGuesser.isNote("src/test/DEPENDENCIES"));
        assertTrue(NoteGuesser.isNote("src/test/LICENSE"));
        assertTrue(NoteGuesser.isNote("src/test/LICENSE.txt"));
        assertTrue(NoteGuesser.isNote("src/test/NOTICE"));
        assertTrue(NoteGuesser.isNote("src/test/NOTICE.txt"));
        assertTrue(NoteGuesser.isNote("src/test/README"));
        assertTrue(NoteGuesser.isNote("src/test/README.txt"));
        assertTrue(NoteGuesser.isNote("src\\test\\DEPENDENCIES"));
        assertTrue(NoteGuesser.isNote("src\\test\\LICENSE"));
        assertTrue(NoteGuesser.isNote("src\\test\\LICENSE.txt"));
        assertTrue(NoteGuesser.isNote("src\\test\\NOTICE"));
        assertTrue(NoteGuesser.isNote("src\\test\\NOTICE.txt"));
        assertTrue(NoteGuesser.isNote("src\\test\\README"));
        assertTrue(NoteGuesser.isNote("src\\test\\README.txt"));
    }
}
