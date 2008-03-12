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
package rat.header;

import java.io.StringReader;
import java.util.regex.Pattern;

import junit.framework.TestCase;

public class HeaderMatcherTest extends TestCase {

    int capacity;
    HeaderMatcher matcher;
    SimpleCharFilter filter;
    
    protected void setUp() throws Exception {
        super.setUp();
        capacity = 20;
        filter = new SimpleCharFilter();
        matcher = new HeaderMatcher(filter, 20);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testSimpleMatches() throws Exception {
        Pattern hatPattern = Pattern.compile("(.*)hat(.*)");
        Pattern headPattern = Pattern.compile("head....");
        StringReader reader = new StringReader("The mad hatter");
        matcher.read(reader);
        assertTrue(matcher.matches(hatPattern));
        assertFalse(matcher.matches(headPattern));
        reader = new StringReader("headache");
        matcher.read(reader);
        assertFalse(matcher.matches(hatPattern));
        assertTrue(matcher.matches(headPattern));   
    }
    
    public void testFilteredMatches() throws Exception {
        Pattern capPattern = Pattern.compile("cap(.*)");
        StringReader reader = new StringReader("capped");
        matcher.read(reader);
        assertTrue(matcher.matches(capPattern));
        filter.filterOut = true;
        reader = new StringReader("capped");
        matcher.read(reader);
        assertFalse(matcher.matches(capPattern));
    }

    public void testNoLines() throws Exception {
        StringReader reader = new StringReader("None");
        matcher.read(reader);
        assertEquals("No lines read", 0, matcher.lines());
    }
    
    public void testLines() throws Exception {
        StringReader reader = new StringReader("One\n");
        matcher.read(reader);
        assertEquals("One line read", 1, matcher.lines());
        reader = new StringReader("One\nTwo");
        matcher.read(reader);
        assertEquals("One line read", 1, matcher.lines());
        reader = new StringReader("One\nTwo\nThree");
        matcher.read(reader);
        assertEquals("Two lines read", 2, matcher.lines());
        reader = new StringReader("One\nTwo\nThree\n");
        matcher.read(reader);
        assertEquals("Three lines read", 3, matcher.lines());
    }
    
    public void testTooManyLines() throws Exception {
        StringReader reader = new StringReader("WhateverWhateverWhateverWhateverWhateverWhateverWhateverWhatever");
        matcher.read(reader);
        assertEquals("Too many lines read", -1, matcher.lines());
    }
}