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
package org.apache.rat.header;

import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HeaderMatcherTest {

    private HeaderMatcher matcher;
    private SimpleCharFilter filter;

    @Before
    public void setUp() throws Exception {
        filter = new SimpleCharFilter();
        matcher = new HeaderMatcher(filter, 20);
    }

    @Test
    public void simpleMatches() throws Exception {
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

    @Test
    public void filteredMatches() throws Exception {
        Pattern capPattern = Pattern.compile("cap(.*)");
        StringReader reader = new StringReader("capped");
        matcher.read(reader);
        assertTrue(matcher.matches(capPattern));
        filter.filterOut = true;
        reader = new StringReader("capped");
        matcher.read(reader);
        assertFalse(matcher.matches(capPattern));
    }

    @Test
    public void noLines() throws Exception {
        StringReader reader = new StringReader("None");
        matcher.read(reader);
        assertEquals("No lines read", 0, matcher.lines());
    }
    
    @Test
    public void lines() throws Exception {
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
    
    @Test
    public void tooManyLines() throws Exception {
        StringReader reader = new StringReader("WhateverWhateverWhateverWhateverWhateverWhateverWhateverWhatever");
        matcher.read(reader);
        assertEquals("Too many lines read", -1, matcher.lines());
    }
}
