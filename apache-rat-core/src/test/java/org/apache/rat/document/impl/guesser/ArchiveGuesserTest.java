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

public class ArchiveGuesserTest {

    @Test
    public void matches() {
        assertTrue(ArchiveGuesser.isArchive(new MockDocument("42.jar")));
        assertTrue(ArchiveGuesser.isArchive(new MockDocument("42.tar.gz")));
        assertTrue(ArchiveGuesser.isArchive(new MockDocument("42.zip")));
        assertTrue(ArchiveGuesser.isArchive(new MockDocument("42.tar")));
        assertTrue(ArchiveGuesser.isArchive(new MockDocument("42.bz")));
        assertTrue(ArchiveGuesser.isArchive(new MockDocument("42.bz2")));
    }
    
    @Test
    public void isArchive() {
        assertTrue(ArchiveGuesser.isArchive("42.jar"));
        assertTrue(ArchiveGuesser.isArchive("42.tar.gz"));
        assertTrue(ArchiveGuesser.isArchive("42.zip"));
        assertTrue(ArchiveGuesser.isArchive("42.tar"));
        assertTrue(ArchiveGuesser.isArchive("42.bz"));
        assertTrue(ArchiveGuesser.isArchive("42.bz2"));
    }
    

}
