/*
 * Copyright 2006 Robert Burrell Donkin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package rat.header;

import java.io.StringReader;

import junit.framework.TestCase;

public class FilteringSequenceFactoryTest extends TestCase {

    int capacity;
    FilteringSequenceFactory factory;
    SimpleCharFilter filter;
    
    protected void setUp() throws Exception {
        super.setUp();
        capacity = 50;
        filter = new SimpleCharFilter();
        factory = new FilteringSequenceFactory(capacity, filter);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testNoFiltering() throws Exception {
        final String INPUT = "Whatever";
        StringReader reader = new StringReader(INPUT);
        CharSequence result = factory.filter(reader);
        assertNotNull(result);
        String output = new StringBuffer().append(result).toString();
        assertEquals("No filtering so input equals output.", INPUT, output);
        reader = new StringReader(INPUT);
        result = factory.filter(reader);
        assertNotNull(result);
        output = new StringBuffer().append(result).toString();
        assertEquals("No filtering so input equals output. Independent of previous input", INPUT, output);
    }

    public void testFiltering() throws Exception {
        final String INPUT = "Whatever";
        StringReader reader = new StringReader(INPUT);
        CharSequence result = factory.filter(reader);
        assertNotNull(result);
        String output = new StringBuffer().append(result).toString();
        assertEquals("No filtering so input equals output.", INPUT, output);
        filter.filterOut = true;
        reader = new StringReader(INPUT);
        result = factory.filter(reader);
        assertNotNull(result);
        assertEquals("All filtered output is empty. Independent of previous input", 0, result.length());
    }
    
    public void testOverCapacity() throws Exception {
        final String INPUT = "WhateverWhateverWhateverWhateverWhateverWhateverWhateverWhateverWhateverWhatever";
        StringReader reader = new StringReader(INPUT);
        CharSequence result = factory.filter(reader);
        assertNotNull(result);
        String output = new StringBuffer().append(result).toString();
        assertEquals("No filtering so input equals output.", INPUT.substring(0, capacity), output);
    }
}
