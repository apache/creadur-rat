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
package org.apache.rat.report.xml.writer.impl.base;

import java.io.StringWriter;

import junit.framework.TestCase;
import org.apache.rat.report.xml.writer.InvalidXmlException;
import org.apache.rat.report.xml.writer.OperationNotAllowedException;

public class XmlWriterTest extends TestCase {

    private static final char[] ZERO_CHAR = {(char)0};
    
    XmlWriter writer;
    StringWriter out;
    
    protected void setUp() throws Exception {
        super.setUp();
        out = new StringWriter();
        writer = new XmlWriter(out);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testReturnValues() throws Exception {
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("alpha"));
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.attribute("beta", "b"));
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.content("gamma"));
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.closeElement());
    }

    public void testOpenElement() throws Exception {
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("alpha"));
        assertEquals("Alpha element started", "<alpha", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("beta"));
        assertEquals("Alpha element tag closed and beta started", "<alpha><beta", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.closeElement());
        assertEquals("Beta tag ended", "<alpha><beta/>", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("gamma"));
        assertEquals("Gamma tag started", "<alpha><beta/><gamma", out.toString());
    }
    
    public void testInvalidElementName() throws Exception {
        assertTrue("All strings ok", isValidElementName("alpha"));
        assertTrue("Strings and digits ok", isValidElementName("alpha77"));
        assertFalse("Must no start with digit", isValidElementName("5alpha77"));
        assertFalse("Greater than not ok", isValidElementName("alph<a77"));
        assertFalse("Less than not ok", isValidElementName("alph<a77"));
        assertFalse("Quote not ok", isValidElementName("alph'a77"));
        assertTrue("Dash ok", isValidElementName("alph-a77"));
        assertTrue("Underscore ok", isValidElementName("alph_a77"));
        assertTrue("Dot ok", isValidElementName("alph.a77"));
        assertTrue("Colon ok", isValidElementName("alpha:77"));
        assertFalse("Start with dash not ok", isValidElementName("-a77"));
        assertTrue("Start with underscore ok", isValidElementName("_a77"));
        assertFalse("Start with dot not ok", isValidElementName(".a77"));
        assertTrue("Start with colon ok", isValidElementName(":a77"));
    }
    
    private boolean isValidElementName(String elementName) throws Exception {
        boolean result = true;
        try {
            writer.openElement(elementName);
        } catch (InvalidXmlException e) {
            result = false;
        }
        return result;
    }
    
    public void testCallOpenElementAfterLastElementClosed() throws Exception {
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("alpha"));
        assertEquals("Alpha element started", "<alpha", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.closeElement());
        assertEquals("Element alpha is closed", "<alpha/>", out.toString());
        try {
            writer.openElement("delta");
            fail("Cannot open new elements once the first element has been closed");
        } catch (OperationNotAllowedException e) {
            // Cannot open new elements once the first element has been closed
        }
    }    
    
    public void testCallCloseElementAfterLastElementClosed() throws Exception {
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("alpha"));
        assertEquals("Alpha element started", "<alpha", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.closeElement());
        assertEquals("Element alpha is closed", "<alpha/>", out.toString());
        try {
            writer.closeElement();
            fail("Cannot close elements once the first element has been closed");
        } catch (OperationNotAllowedException e) {
            // Cannot open new elements once the first element has been closed
        }
    }
    
    
    public void testCloseFirstElement() throws Exception {
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("alpha"));
        assertEquals("Alpha element started", "<alpha", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.closeElement());
        assertEquals("Element alpha is closed", "<alpha/>", out.toString());
    }
    
    public void testCloseElementWithContent() throws Exception {
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("alpha"));
        assertEquals("Alpha element started", "<alpha", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("beta"));
        assertEquals("Beta element started", "<alpha><beta", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.closeElement());
        assertEquals("Element beta is closed", "<alpha><beta/>", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.closeElement());
        assertEquals("Element beta is closed", "<alpha><beta/></alpha>", out.toString());
        try {
            writer.closeElement();
            fail("Cannot close elements once the first element has been closed");
        } catch (OperationNotAllowedException e) {
            // Cannot open new elements once the first element has been closed
        }
    }
    
    public void testCloseElementBeforeFirstElement() throws Exception {
        try {
            writer.closeElement();
            fail("Cannot close elements before the first element has been closed");
        } catch (OperationNotAllowedException e) {
            // Cannot open new elements before the first element has been closed
        }
    }
    
    public void testContentAfterElement() throws Exception {
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("alpha"));
        assertEquals("Alpha element started", "<alpha", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.content("foo bar"));
        assertEquals("Alpha tag closed. Content written", "<alpha>foo bar", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.content(" and more foo bar"));
        assertEquals("Alpha tag closed. Content written", "<alpha>foo bar and more foo bar", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("beta"));
        assertEquals("Beta element started", "<alpha>foo bar and more foo bar<beta", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.closeElement());
        assertEquals("Element beta is closed", "<alpha>foo bar and more foo bar<beta/>", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.closeElement());
        assertEquals("Element beta is closed", "<alpha>foo bar and more foo bar<beta/></alpha>", out.toString());
        try {
            writer.content("A Sentence Too far");
            fail("Cannot write content once the first element has been closed");
        } catch (OperationNotAllowedException e) {
            // Cannot open new elements once the first element has been closed
        }
    }
    

    public void testContentAfterLastElement() throws Exception {
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("alpha"));
        assertEquals("Alpha element started", "<alpha", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.closeElement());
        assertEquals("Element beta is closed", "<alpha/>", out.toString());
        try {
            writer.content("A Sentence Too far");
            fail("Cannot write content once the first element has been closed");
        } catch (OperationNotAllowedException e) {
            // Cannot open new elements once the first element has been closed
        }
    }
    
    public void testWriteContentBeforeFirstElement() throws Exception {
        try {
            writer.content("Too early");
            fail("Cannot close elements before the first element has been closed");
        } catch (OperationNotAllowedException e) {
            // Cannot open new elements before the first element has been closed
        }
    }
    
    public void testContentEscaping() throws Exception {
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("alpha"));
        assertEquals("Alpha element started", "<alpha", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.content("this&that"));
        assertEquals("Amphersands must be escaped", "<alpha>this&amp;that", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.content("small<large"));
        assertEquals("Left angle brackets must be escaped", "<alpha>this&amp;thatsmall&lt;large", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.content("12>1"));
        assertEquals("Choose to escape right angle brackets", "<alpha>this&amp;thatsmall&lt;large12&gt;1", out.toString());

    }

    public void testAttributeAfterLastElement() throws Exception {
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("alpha"));
        assertEquals("Alpha element started", "<alpha", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.closeElement());
        assertEquals("Element beta is closed", "<alpha/>", out.toString());
        try {
            writer.attribute("foo", "bar");
            fail("Cannot write content once the first element has been closed");
        } catch (OperationNotAllowedException e) {
            // Cannot open new elements once the first element has been closed
        }
    }
    
    public void testAttributeContentBeforeFirstElement() throws Exception {
        try {
            writer.attribute("foo", "bar");
            fail("Cannot close elements before the first element has been closed");
        } catch (OperationNotAllowedException e) {
            // Cannot open new elements before the first element has been closed
        }
    }
    
    public void testInvalidAttributeName() throws Exception {
        writer.openElement("alpha");
        assertTrue("All strings ok", isValidAttributeName("alpha"));
        assertTrue("Strings and digits ok", isValidAttributeName("alpha77"));
        assertFalse("Must not start with digit", isValidAttributeName("5alpha77"));
        assertTrue("Colon ok", isValidAttributeName("alpha:77"));
        assertFalse("Greater than not ok", isValidAttributeName("alph<a77"));
        assertFalse("Less than not ok", isValidAttributeName("alph<a77"));
        assertFalse("Quote not ok", isValidAttributeName("alph'a77"));
    }
    
    private boolean isValidAttributeName(String name) throws Exception {
        boolean result = true;
        try {
            writer.attribute(name, "");
        } catch (InvalidXmlException e) {
            result = false;
        }
        return result;
    }
    
    public void testEscapeAttributeContent() throws Exception {
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("alpha"));
        assertEquals("Alpha element started", "<alpha", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.attribute("one", "this&that"));
        assertEquals("Amphersands must be escaped", "<alpha one='this&amp;that'", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.attribute("two", "small<large"));
        assertEquals("Left angle brackets must be escaped", "<alpha one='this&amp;that' two='small&lt;large'", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.attribute("three", "12>1"));
        assertEquals("Choose to escape right angle brackets", "<alpha one='this&amp;that' two='small&lt;large' three='12&gt;1'", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.attribute("four", "'quote'"));
        assertEquals("Apostrophes must be escape", "<alpha one='this&amp;that' two='small&lt;large' three='12&gt;1' four='&apos;quote&apos;'", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.attribute("five", "\"quote\""));
        assertEquals("Double quotes must be escape", "<alpha one='this&amp;that' two='small&lt;large' three='12&gt;1' four='&apos;quote&apos;' five='&quot;quote&quot;'", out.toString());

    }
    
    public void testAttributeInContent() throws Exception {
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("alpha"));
        assertEquals("Alpha element started", "<alpha", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.content("foo bar"));
        try {
            writer.attribute("name", "value");
            fail("attributes after body content are not allowed");
        } catch (InvalidXmlException e) {
            // attributes after body content are not allowed
        }
    }
  
    public void testOutOfRangeCharacter() throws Exception {
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("alpha"));
        assertEquals("Alpha element started", "<alpha", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.content(new String(ZERO_CHAR)));
        String out = this.out.toString();
        assertEquals("Replace illegal characters with question marks", "<alpha>?", out);
    }
    
    public void testAttributeAfterElementClosed() throws Exception {
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("alpha"));
        assertEquals("Alpha element started", "<alpha", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("beta"));
        assertEquals("Beta element started", "<alpha><beta", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.closeElement());
        assertEquals("Beta element closed", "<alpha><beta/>", out.toString());
        try {
            writer.attribute("name", "value");
            fail("attributes after closed element are not allowed");
        } catch (InvalidXmlException e) {
            // attributes after body content are not allowed
        }
    }
    
    public void testCloseDocumentBeforeOpen() throws Exception {
        try {
            writer.closeDocument();
            fail("Cannot close document before the first element has been opened");
        } catch (OperationNotAllowedException e) {
            // Cannot open new elements before the first element has been opened
        }
    }
    
    public void testCloseDocumentAfterRootElementClosed() throws Exception {
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("alpha"));
        assertEquals("Alpha element started", "<alpha", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.closeElement());
        assertEquals("Beta element started", "<alpha/>", out.toString());
        try {
            writer.closeDocument();
        } catch (OperationNotAllowedException e) {
            fail("No exception should be thrown when called after the root element is closed.");
        }
    }   
    
    public void testCloseSimpleDocument() throws Exception {
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("alpha"));
        assertEquals("Alpha element started", "<alpha", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("beta"));
        assertEquals("Beta element started", "<alpha><beta", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.closeDocument());
        assertEquals("Beta element started", "<alpha><beta/></alpha>", out.toString());
    }
    
    public void testCloseComplexDocument() throws Exception {
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("alpha"));
        assertEquals("Alpha element started", "<alpha", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("beta"));
        assertEquals("Beta element started", "<alpha><beta", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.attribute("name", "value"));
        assertEquals("Beta element started", "<alpha><beta name='value'", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.closeElement());
        assertEquals("Beta element started", "<alpha><beta name='value'/>", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("beta"));
        assertEquals("Beta element started", "<alpha><beta name='value'/><beta", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.attribute("name", "value"));
        assertEquals("Beta element started", "<alpha><beta name='value'/><beta name='value'", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("gamma"));
        assertEquals("Beta element started", "<alpha><beta name='value'/><beta name='value'><gamma", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.closeDocument());
        assertEquals("Beta element started", "<alpha><beta name='value'/><beta name='value'><gamma/></beta></alpha>", out.toString());
    }
    
    public void testWriteProlog() throws Exception {
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.startDocument());
        assertEquals("Prolog written", "<?xml version='1.0'?>", out.toString());
    }
    
    public void testWriteAfterElement() throws Exception {
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("alpha"));
        assertEquals("Alpha element started", "<alpha", out.toString());
        try {
            writer.startDocument();
            fail("Operation not allowed once an element has been written");
        } catch (OperationNotAllowedException e) {
            // Operation not allowed once an element has been written
        }
    }
    
    public void testWritePrologTwo() throws Exception {
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.startDocument());
        assertEquals("Prolog written", "<?xml version='1.0'?>", out.toString());
        try {
            writer.startDocument();
            fail("Operation not allow once a prolog has been written");
        } catch (OperationNotAllowedException e) {
            // Operation not allowed once an prolog has been written
        }
    }
    
    public void testDuplicateAttributes() throws Exception {
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("alpha"));
        assertEquals("Alpha element started", "<alpha", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.attribute("one", "1"));
        assertEquals("Attribute written", "<alpha one='1'", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.openElement("beta"));
        assertEquals("Beta element started", "<alpha one='1'><beta", out.toString());
        assertEquals("XmlWriters should always return themselves", 
                writer, writer.attribute("one", "1"));
        assertEquals("Beta element started", "<alpha one='1'><beta one='1'", out.toString());
        try {
            writer.attribute("one", "2");
            fail("Each attribute may only be written once");
        } catch (InvalidXmlException e) {
            // Each attribute may only be written once
        }
    }
}
