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

public class BinaryGuesserTest extends TestCase {

    BinaryGuesser guesser;
    
    protected void setUp() throws Exception {
        super.setUp();
        guesser = new BinaryGuesser();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testMatches() {
        assertTrue(guesser.matches(new MockDocument("image.png")));
        assertTrue(guesser.matches(new MockDocument("image.pdf")));
        assertTrue(guesser.matches(new MockDocument("image.gif")));
        assertTrue(guesser.matches(new MockDocument("image.giff")));
        assertTrue(guesser.matches(new MockDocument("image.tif")));
        assertTrue(guesser.matches(new MockDocument("image.tiff")));
        assertTrue(guesser.matches(new MockDocument("image.jpg")));
        assertTrue(guesser.matches(new MockDocument("image.jpeg")));
        assertTrue(guesser.matches(new MockDocument("image.exe")));
        assertTrue(guesser.matches(new MockDocument("Whatever.class")));
        assertTrue(guesser.matches(new MockDocument("data.dat")));
        assertTrue(guesser.matches(new MockDocument("libicudata.so.34.")));
    }

    public void testIsBinary() {
        assertTrue(BinaryGuesser.isBinary("image.png"));
        assertTrue(BinaryGuesser.isBinary("image.pdf"));
        assertTrue(BinaryGuesser.isBinary("image.gif"));
        assertTrue(BinaryGuesser.isBinary("image.giff"));
        assertTrue(BinaryGuesser.isBinary("image.tif"));
        assertTrue(BinaryGuesser.isBinary("image.tiff"));
        assertTrue(BinaryGuesser.isBinary("image.jpg"));
        assertTrue(BinaryGuesser.isBinary("image.jpeg"));
        assertTrue(BinaryGuesser.isBinary("image.exe"));
        assertTrue(BinaryGuesser.isBinary("Whatever.class"));
        assertTrue(BinaryGuesser.isBinary("data.dat"));
        assertTrue(BinaryGuesser.isBinary("libicudata.so.34."));
    }
}
