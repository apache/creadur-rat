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

import org.apache.rat.report.xml.writer.InvalidXmlException;
import org.apache.rat.report.xml.writer.OperationNotAllowedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class XmlWriterTest {

    private static final char[] ZERO_CHAR = {(char)0};
    
    private XmlWriter writer;
    private StringWriter out;
    
    @BeforeEach
    public void setUp() {
        out = new StringWriter();
        writer = new XmlWriter(out);
    }

    @Test
    public void returnValues() throws Exception {
        assertEquals( 
                writer, writer.openElement("alpha"), "XmlWriters should always return themselves");
        assertEquals(
                writer, writer.attribute("beta", "b"), "XmlWriters should always return themselves");
        assertEquals(
                writer, writer.content("gamma"), "XmlWriters should always return themselves");
        assertEquals(
                writer, writer.closeElement(), "XmlWriters should always return themselves");
    }

    @Test
    public void openElement() throws Exception {
        assertEquals(
                writer, writer.openElement("alpha"), "XmlWriters should always return themselves");
         assertEquals("<alpha", out.toString(), "Alpha element started");;
        assertEquals(
                writer, writer.openElement("beta"), "XmlWriters should always return themselves");
        assertEquals("<alpha><beta", out.toString(), "Alpha element tag closed and beta started");
        assertEquals(
                writer, writer.closeElement(), "XmlWriters should always return themselves");
        assertEquals("<alpha><beta/>", out.toString(), "Beta tag ended");
        assertEquals(
                writer, writer.openElement("gamma"), "XmlWriters should always return themselves");
        assertEquals("<alpha><beta/><gamma", out.toString(), "Gamma tag started");
    }
    
    @Test
    public void invalidElementName() throws Exception {
        assertTrue( isValidElementName("alpha"), "All strings ok");
        assertTrue(isValidElementName("alpha77"), "Strings and digits ok");
        assertFalse(isValidElementName("5alpha77"), "Must no start with digit");
        assertFalse(isValidElementName("alph<a77"), "Greater than not ok");
        assertFalse(isValidElementName("alph<a77"), "Less than not ok");
        assertFalse(isValidElementName("alph'a77"), "Quote not ok");
        assertTrue(isValidElementName("alph-a77"), "Dash ok");
        assertTrue(isValidElementName("alph_a77"), "Underscore ok");
        assertTrue(isValidElementName("alph.a77"), "Dot ok");
        assertTrue(isValidElementName("alpha:77"), "Colon ok");
        assertFalse(isValidElementName("-a77"), "Start with dash not ok");
        assertTrue(isValidElementName("_a77"), "Start with underscore ok");
        assertFalse(isValidElementName(".a77"), "Start with dot not ok");
        assertTrue(isValidElementName(":a77"), "Start with colon ok");
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

    @Test
    public void callOpenElementAfterLastElementClosed() throws Exception {
        assertEquals(
                writer, writer.openElement("alpha"), "XmlWriters should always return themselves");
         assertEquals("<alpha", out.toString(), "Alpha element started");;
        assertEquals(
                writer, writer.closeElement(), "XmlWriters should always return themselves");
        assertEquals("<alpha/>", out.toString(), "Element alpha is closed");
        try {
            writer.openElement("delta");
            fail("Cannot open new elements once the first element has been closed");
        } catch (OperationNotAllowedException e) {
            // Cannot open new elements once the first element has been closed
        }
    }    

    @Test
    public void callCloseElementAfterLastElementClosed() throws Exception {
        assertEquals(
                writer, writer.openElement("alpha"), "XmlWriters should always return themselves");
         assertEquals("<alpha", out.toString(), "Alpha element started");;
        assertEquals(
                writer, writer.closeElement(), "XmlWriters should always return themselves");
        assertEquals("<alpha/>", out.toString(), "Element alpha is closed");
        try {
            writer.closeElement();
            fail("Cannot close elements once the first element has been closed");
        } catch (OperationNotAllowedException e) {
            // Cannot open new elements once the first element has been closed
        }
    }

    @Test
    public void closeFirstElement() throws Exception {
        assertEquals(
                writer, writer.openElement("alpha"), "XmlWriters should always return themselves");
        assertEquals("<alpha", out.toString(), "Alpha element started");
        assertEquals(
                writer, writer.closeElement(), "XmlWriters should always return themselves");
        assertEquals("<alpha/>", out.toString(), "Element alpha is closed");
    }
    
    @Test
    public void closeElementWithContent() throws Exception {
        assertEquals(
                writer, writer.openElement("alpha"), "XmlWriters should always return themselves");
        assertEquals("<alpha", out.toString(), "Alpha element started");
        assertEquals(
                writer, writer.openElement("beta"), "XmlWriters should always return themselves");
        assertEquals("<alpha><beta", out.toString(), "Beta element started");
        assertEquals(
                writer, writer.closeElement(), "XmlWriters should always return themselves");
        assertEquals("<alpha><beta/>", out.toString(), "Element beta is closed");
        assertEquals(
                writer, writer.closeElement(), "XmlWriters should always return themselves");
        assertEquals("<alpha><beta/></alpha>", out.toString(), "Element beta is closed");
        try {
            writer.closeElement();
            fail("Cannot close elements once the first element has been closed");
        } catch (OperationNotAllowedException e) {
            // Cannot open new elements once the first element has been closed
        }
    }
    
    @Test
    public void closeElementBeforeFirstElement() throws Exception {
        try {
            writer.closeElement();
            fail("Cannot close elements before the first element has been closed");
        } catch (OperationNotAllowedException e) {
            // Cannot open new elements before the first element has been closed
        }
    }
    
    @Test
    public void contentAfterElement() throws Exception {
        assertEquals(
                writer, writer.openElement("alpha"), "XmlWriters should always return themselves");
         assertEquals("<alpha", out.toString(), "Alpha element started");;
        assertEquals(
                writer, writer.content("foo bar"), "XmlWriters should always return themselves");
        assertEquals("<alpha>foo bar", out.toString(), "Alpha tag closed. Content written");
        assertEquals(
                writer, writer.content(" and more foo bar"), "XmlWriters should always return themselves");
        assertEquals("<alpha>foo bar and more foo bar", out.toString(), "Alpha tag closed. Content written");
        assertEquals(
                writer, writer.openElement("beta"), "XmlWriters should always return themselves");
        assertEquals("<alpha>foo bar and more foo bar<beta", out.toString());
        assertEquals(
                writer, writer.closeElement(), "XmlWriters should always return themselves");
        assertEquals("<alpha>foo bar and more foo bar<beta/>", out.toString(), "Element beta is closed");
        assertEquals(
                writer, writer.closeElement(), "XmlWriters should always return themselves");
        assertEquals("<alpha>foo bar and more foo bar<beta/></alpha>", out.toString(), "Element beta is closed");
        try {
            writer.content("A Sentence Too far");
            fail("Cannot write content once the first element has been closed");
        } catch (OperationNotAllowedException e) {
            // Cannot open new elements once the first element has been closed
        }
    }

    @Test
    public void contentAfterLastElement() throws Exception {
        assertEquals(
                writer, writer.openElement("alpha"), "XmlWriters should always return themselves");
         assertEquals("<alpha", out.toString(), "Alpha element started");;
        assertEquals(
                writer, writer.closeElement(), "XmlWriters should always return themselves");
        assertEquals("<alpha/>", out.toString(), "Element alpha is closed");
        try {
            writer.content("A Sentence Too far");
            fail("Cannot write content once the first element has been closed");
        } catch (OperationNotAllowedException e) {
            // Cannot open new elements once the first element has been closed
        }
    }
    
    @Test
    public void writeContentBeforeFirstElement() throws Exception {
        try {
            writer.content("Too early");
            fail("Cannot close elements before the first element has been closed");
        } catch (OperationNotAllowedException e) {
            // Cannot open new elements before the first element has been closed
        }
    }
    
    @Test
    public void contentEscaping() throws Exception {
        assertEquals(
                writer, writer.openElement("alpha"), "XmlWriters should always return themselves");
         assertEquals("<alpha", out.toString(), "Alpha element started");;
        assertEquals(
                writer, writer.content("this&that"), "XmlWriters should always return themselves");
        assertEquals("<alpha>this&amp;that", out.toString(), "Amphersands must be escaped");
        assertEquals(
                writer, writer.content("small<large"), "XmlWriters should always return themselves");
        assertEquals("<alpha>this&amp;thatsmall&lt;large", out.toString(), "Left angle brackets must be escaped");
        assertEquals(
                writer, writer.content("12>1"), "XmlWriters should always return themselves");
        assertEquals("<alpha>this&amp;thatsmall&lt;large12&gt;1", out.toString(), "Choose to escape right angle brackets");

    }

    @Test
    public void attributeAfterLastElement() throws Exception {
        assertEquals(
                writer, writer.openElement("alpha"), "XmlWriters should always return themselves");
         assertEquals("<alpha", out.toString(), "Alpha element started");;
        assertEquals(
                writer, writer.closeElement(), "XmlWriters should always return themselves");
        assertEquals("<alpha/>", out.toString(), "Element alpha is closed");
        try {
            writer.attribute("foo", "bar");
            fail("Cannot write content once the first element has been closed");
        } catch (OperationNotAllowedException e) {
            // Cannot open new elements once the first element has been closed
        }
    }
    
    @Test
    public void attributeContentBeforeFirstElement() throws Exception {
        try {
            writer.attribute("foo", "bar");
            fail("Cannot close elements before the first element has been closed");
        } catch (OperationNotAllowedException e) {
            // Cannot open new elements before the first element has been closed
        }
    }
    
    @Test
    public void invalidAttributeName() throws Exception {
        writer.openElement("alpha");
        assertTrue(isValidAttributeName("alpha"), "All string ok");
        assertTrue(isValidAttributeName("alpha77"), "Strings and digits ok");
        assertFalse(isValidAttributeName("5alpha77"), "Must not start with digit");
        assertTrue(isValidAttributeName("alpha:77"), "Colon ok");
        assertFalse(isValidAttributeName("alph<a77"),"Greater than not ok");
        assertFalse(isValidAttributeName("alph<a77"), "Less than not ok");
        assertFalse(isValidAttributeName("alph'a77"), "Quote not ok");
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
    
    @Test
    public void escapeAttributeContent() throws Exception {
        assertEquals(
                writer, writer.openElement("alpha"), "XmlWriters should always return themselves");
         assertEquals("<alpha", out.toString(), "Alpha element started");;
        assertEquals(
                writer, writer.attribute("one", "this&that"), "XmlWriters should always return themselves");
        assertEquals("<alpha one='this&amp;that'", out.toString(), "Amphersands must be escaped");
        assertEquals(
                writer, writer.attribute("two", "small<large"), "XmlWriters should always return themselves");
        assertEquals("<alpha one='this&amp;that' two='small&lt;large'", out.toString(), "Left angle brackets must be escaped");
        assertEquals(
                writer, writer.attribute("three", "12>1"), "XmlWriters should always return themselves");
        assertEquals("<alpha one='this&amp;that' two='small&lt;large' three='12&gt;1'", out.toString(), "Choose to escape right angle brackets");
        assertEquals(
                writer, writer.attribute("four", "'quote'"), "XmlWriters should always return themselves");
        assertEquals("<alpha one='this&amp;that' two='small&lt;large' three='12&gt;1' four='&apos;quote&apos;'", out.toString(), "Apostrophes must be escape");
        assertEquals(
                writer, writer.attribute("five", "\"quote\""), "XmlWriters should always return themselves");
        assertEquals("<alpha one='this&amp;that' two='small&lt;large' three='12&gt;1' four='&apos;quote&apos;' five='&quot;quote&quot;'", out.toString(), "Double quotes must be escape");

    }
    
    @Test
    public void attributeInContent() throws Exception {
        assertEquals(
                writer, writer.openElement("alpha"), "XmlWriters should always return themselves");
         assertEquals("<alpha", out.toString(), "Alpha element started");;
        assertEquals(
                writer, writer.content("foo bar"), "XmlWriters should always return themselves");
        try {
            writer.attribute("name", "value");
            fail("attributes after body content are not allowed");
        } catch (InvalidXmlException e) {
            // attributes after body content are not allowed
        }
    }
  
    @Test
    public void outOfRangeCharacter() throws Exception {
        assertEquals(
                writer, writer.openElement("alpha"), "XmlWriters should always return themselves");
         assertEquals("<alpha", out.toString(), "Alpha element started");;
        assertEquals(
                writer, writer.content(new String(ZERO_CHAR)), "XmlWriters should always return themselves");
        String out = this.out.toString();
        assertEquals("<alpha>?", out, "Replace illegal characters with question marks");
    }
    
    @Test
    public void attributeAfterElementClosed() throws Exception {
        assertEquals(
                writer, writer.openElement("alpha"), "XmlWriters should always return themselves");
         assertEquals("<alpha", out.toString(), "Alpha element started");;
        assertEquals(
                writer, writer.openElement("beta"), "XmlWriters should always return themselves");
        assertEquals("<alpha><beta", out.toString(), "Beta element started");
        assertEquals(
                writer, writer.closeElement(), "XmlWriters should always return themselves");
        assertEquals("<alpha><beta/>", out.toString(), "Beta element closed");
        try {
            writer.attribute("name", "value");
            fail("attributes after closed element are not allowed");
        } catch (InvalidXmlException e) {
            // attributes after body content are not allowed
        }
    }
    
    @Test
    public void closeDocumentBeforeOpen() throws Exception {
        try {
            writer.closeDocument();
            fail("Cannot close document before the first element has been opened");
        } catch (OperationNotAllowedException e) {
            // Cannot open new elements before the first element has been opened
        }
    }
    
    @Test
    public void closeDocumentAfterRootElementClosed() throws Exception {
        assertEquals(
                writer, writer.openElement("alpha"), "XmlWriters should always return themselves");
         assertEquals("<alpha", out.toString(), "Alpha element started");;
        assertEquals(
                writer, writer.closeElement(), "XmlWriters should always return themselves");
        assertEquals("<alpha/>", out.toString());
        try {
            writer.closeDocument();
        } catch (OperationNotAllowedException e) {
            fail("No exception should be thrown when called after the root element is closed.");
        }
    }   
    
    @Test
    public void closeSimpleDocument() throws Exception {
        assertEquals(
                writer, writer.openElement("alpha"), "XmlWriters should always return themselves");
         assertEquals("<alpha", out.toString(), "Alpha element started");;
        assertEquals(
                writer, writer.openElement("beta"), "XmlWriters should always return themselves");
        assertEquals("<alpha><beta", out.toString(), "Beta element started");
        assertEquals(
                writer, writer.closeDocument(), "XmlWriters should always return themselves");
        assertEquals("<alpha><beta/></alpha>", out.toString(), "Beta element started");
    }
    
    @Test
    public void closeComplexDocument() throws Exception {
        assertEquals(
                writer, writer.openElement("alpha"), "XmlWriters should always return themselves");
         assertEquals("<alpha", out.toString(), "Alpha element started");;
        assertEquals(
                writer, writer.openElement("beta"), "XmlWriters should always return themselves");
        assertEquals("<alpha><beta", out.toString(), "Beta element started");
        assertEquals(
                writer, writer.attribute("name", "value"), "XmlWriters should always return themselves");
        assertEquals("<alpha><beta name='value'", out.toString(), "Beta element started");
        assertEquals(
                writer, writer.closeElement(), "XmlWriters should always return themselves");
        assertEquals("<alpha><beta name='value'/>", out.toString(), "Beta element started");
        assertEquals(
                writer, writer.openElement("beta"), "XmlWriters should always return themselves");
        assertEquals("<alpha><beta name='value'/><beta", out.toString(), "Beta element started");
        assertEquals(
                writer, writer.attribute("name", "value"), "XmlWriters should always return themselves");
        assertEquals("<alpha><beta name='value'/><beta name='value'", out.toString(), "Beta element started");
        assertEquals(
                writer, writer.openElement("gamma"), "XmlWriters should always return themselves");
        assertEquals("<alpha><beta name='value'/><beta name='value'><gamma", out.toString(), "Beta element started");
        assertEquals(
                writer, writer.closeDocument(), "XmlWriters should always return themselves");
        assertEquals("<alpha><beta name='value'/><beta name='value'><gamma/></beta></alpha>", out.toString(), "Beta element started");
    }
    
    @Test
    public void writeProlog() throws Exception {
        assertEquals(
                writer, writer.startDocument(), "XmlWriters should always return themselves");
        assertEquals("<?xml version='1.0'?>", out.toString(), "Prolog written");
    }
    
    @Test
    public void writeAfterElement() throws Exception {
        assertEquals(
                writer, writer.openElement("alpha"), "XmlWriters should always return themselves");
         assertEquals("<alpha", out.toString(), "Alpha element started");;
        try {
            writer.startDocument();
            fail("Operation not allowed once an element has been written");
        } catch (OperationNotAllowedException e) {
            // Operation not allowed once an element has been written
        }
    }
    
    @Test
    public void writePrologTwo() throws Exception {
        assertEquals(
                writer, writer.startDocument(), "XmlWriters should always return themselves");
        assertEquals("<?xml version='1.0'?>", out.toString(), "Prolog written");
        try {
            writer.startDocument();
            fail("Operation not allow once a prolog has been written");
        } catch (OperationNotAllowedException e) {
            // Operation not allowed once an prolog has been written
        }
    }
    
    @Test
    public void duplicateAttributes() throws Exception {
        assertEquals(
                writer, writer.openElement("alpha"), "XmlWriters should always return themselves");
         assertEquals("<alpha", out.toString(), "Alpha element started");;
        assertEquals(
                writer, writer.attribute("one", "1"), "XmlWriters should always return themselves");
        assertEquals("<alpha one='1'", out.toString(), "Attribute written");
        assertEquals(
                writer, writer.openElement("beta"), "XmlWriters should always return themselves");
        assertEquals("<alpha one='1'><beta", out.toString(), "Beta element started");
        assertEquals(
                writer, writer.attribute("one", "1"), "XmlWriters should always return themselves");
        assertEquals("<alpha one='1'><beta one='1'", out.toString(), "Beta element started");
        try {
            writer.attribute("one", "2");
            fail("Each attribute may only be written once");
        } catch (InvalidXmlException e) {
            // Each attribute may only be written once
        }
    }
}
