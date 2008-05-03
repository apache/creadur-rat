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

import rat.document.MockDocument;
import junit.framework.TestCase;

public class ArchiveGuesserTest extends TestCase {

    ArchiveGuesser guesser;
    
    protected void setUp() throws Exception {
        super.setUp();
        this.guesser = new ArchiveGuesser();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    

    public void testMatches() {
        assertTrue(guesser.matches(new MockDocument("42.jar")));
        assertTrue(guesser.matches(new MockDocument("42.tar.gz")));
        assertTrue(guesser.matches(new MockDocument("42.zip")));
        assertTrue(guesser.matches(new MockDocument("42.tar")));
        assertTrue(guesser.matches(new MockDocument("42.bz")));
        assertTrue(guesser.matches(new MockDocument("42.bz2")));
    }
    
    public void testIsArchive() {
        assertTrue(ArchiveGuesser.isArchive("42.jar"));
        assertTrue(ArchiveGuesser.isArchive("42.tar.gz"));
        assertTrue(ArchiveGuesser.isArchive("42.zip"));
        assertTrue(ArchiveGuesser.isArchive("42.tar"));
        assertTrue(ArchiveGuesser.isArchive("42.bz"));
        assertTrue(ArchiveGuesser.isArchive("42.bz2"));
    }
    

}
