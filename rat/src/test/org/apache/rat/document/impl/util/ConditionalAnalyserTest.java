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
package org.apache.rat.document.impl.util;

import org.apache.rat.document.MockDocument;
import org.apache.rat.document.MockDocumentAnalyser;
import org.apache.rat.document.MockDocumentMatcher;
import junit.framework.TestCase;

public class ConditionalAnalyserTest extends TestCase {

    MockDocumentAnalyser analyser;
    MockDocument document;
    
    protected void setUp() throws Exception {
        super.setUp();
        analyser = new MockDocumentAnalyser();
        document = new MockDocument();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMatch() throws Exception {
        MockDocumentMatcher matcher = new MockDocumentMatcher(true);
        ConditionalAnalyser conditionalAnalyser = new ConditionalAnalyser(matcher, analyser);
        assertTrue("Returns match value", conditionalAnalyser.matches(document));
        assertEquals("Document analysed", 1, analyser.matches.size());
        assertEquals("Document analysed", document, analyser.matches.get(0));
        assertEquals("Document matched", 1, matcher.matches.size());
        assertEquals("Document matched", document, matcher.matches.get(0));
    }

    public void testNoMatch() throws Exception {
        MockDocumentMatcher matcher = new MockDocumentMatcher(false);
        ConditionalAnalyser conditionalAnalyser = new ConditionalAnalyser(matcher, analyser);
        assertFalse("Returns match value", conditionalAnalyser.matches(document));
        assertEquals("Not Documents analysed", 0, analyser.matches.size());
        assertEquals("Document matched", 1, matcher.matches.size());
        assertEquals("Document matched", document, matcher.matches.get(0));
    }

}
