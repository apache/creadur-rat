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
package rat.document.impl.util;

import junit.framework.TestCase;
import rat.document.IDocumentMatcher;
import rat.document.MockDocument;
import rat.document.MockDocumentMatcher;

public class DocumentMatcherMultiplexerTest extends TestCase {

    DocumentMatcherMultiplexer multiplexer;
    MockDocumentMatcher documentOne;
    MockDocumentMatcher documentTwo;
    MockDocumentMatcher documentThree;
    MockDocumentMatcher documentFour;
    MockDocument document;
    
    protected void setUp() throws Exception {
        super.setUp();
        documentOne = new MockDocumentMatcher();
        documentTwo = new MockDocumentMatcher();
        documentThree = new MockDocumentMatcher();
        documentFour = new MockDocumentMatcher();
        IDocumentMatcher[] documents = {documentOne, documentTwo, documentThree, documentFour};
        multiplexer = new DocumentMatcherMultiplexer(documents);
        document = new MockDocument();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMatchesAll() throws Exception {
        documentOne.returnValue = false;
        documentTwo.returnValue = false;
        documentThree.returnValue = false;
        documentFour.returnValue = true;
        assertTrue("Matches", multiplexer.matches(document));
        assertEquals("Matcher called with document", 1, documentOne.matches.size());
        assertEquals("Matcher called with document", document, documentOne.matches.get(0));
        assertEquals("Matcher called with document", 1, documentTwo.matches.size());
        assertEquals("Matcher called with document", document, documentTwo.matches.get(0));
        assertEquals("Matcher called with document", 1, documentThree.matches.size());
        assertEquals("Matcher called with document", document, documentThree.matches.get(0));
        assertEquals("Matcher called with document", 1, documentFour.matches.size());
        assertEquals("Matcher called with document", document, documentFour.matches.get(0));
    }
    
    public void testIgnoreAfterMatch() throws Exception {
        documentOne.returnValue = false;
        documentTwo.returnValue = false;
        documentThree.returnValue = true;
        documentFour.returnValue = false;
        assertTrue("Matches", multiplexer.matches(document));
        assertEquals("Matcher called with document", 1, documentOne.matches.size());
        assertEquals("Matcher called with document", document, documentOne.matches.get(0));
        assertEquals("Matcher called with document", 1, documentTwo.matches.size());
        assertEquals("Matcher called with document", document, documentTwo.matches.get(0));
        assertEquals("Matcher called with document", 1, documentThree.matches.size());
        assertEquals("Matcher called with document", document, documentThree.matches.get(0));
        assertEquals("Matcher not called", 0, documentFour.matches.size());
    }
    
    public void testNoMatch() throws Exception {
        documentOne.returnValue = false;
        documentTwo.returnValue = false;
        documentThree.returnValue = false;
        documentFour.returnValue = false;
        assertFalse("Matches", multiplexer.matches(document));
    }
    
}
