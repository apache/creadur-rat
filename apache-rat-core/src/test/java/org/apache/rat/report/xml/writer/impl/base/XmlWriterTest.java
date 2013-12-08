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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringWriter;

import org.apache.rat.report.xml.writer.InvalidXmlException;
import org.apache.rat.report.xml.writer.OperationNotAllowedException;
import org.junit.Before;
import org.junit.Test;

/**
 * The Class XmlWriterTest.
 */
public class XmlWriterTest {

	/** The Constant ZERO_CHAR. */
	private static final char[] ZERO_CHAR = { (char) 0 };

	/** The writer. */
	XmlWriter writer;

	/** The out. */
	StringWriter out;

	/**
	 * Sets the up.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Before
	public void setUp() throws Exception {
		out = new StringWriter();
		writer = new XmlWriter(out);
	}

	/**
	 * Return values.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void returnValues() throws Exception {
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("alpha"));
		assertEquals("XmlWriters should always return themselves", writer,
				writer.attribute("beta", "b"));
		assertEquals("XmlWriters should always return themselves", writer,
				writer.content("gamma"));
		assertEquals("XmlWriters should always return themselves", writer,
				writer.closeElement());
	}

	/**
	 * Open element.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void openElement() throws Exception {
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("alpha"));
		assertEquals("Alpha element started", "<alpha", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("beta"));
		assertEquals("Alpha element tag closed and beta started",
				"<alpha><beta", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.closeElement());
		assertEquals("Beta tag ended", "<alpha><beta/>", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("gamma"));
		assertEquals("Gamma tag started", "<alpha><beta/><gamma",
				out.toString());
	}

	/**
	 * Invalid element name.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void invalidElementName() throws Exception {
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

	/**
	 * Checks if is valid element name.
	 * 
	 * @param elementName
	 *            the element name
	 * @return true, if is valid element name
	 * @throws Exception
	 *             the exception
	 */
	private boolean isValidElementName(String elementName) throws Exception {
		boolean result = true;
		try {
			writer.openElement(elementName);
		} catch (InvalidXmlException e) {
			result = false;
		}
		return result;
	}

	/**
	 * Call open element after last element closed.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void callOpenElementAfterLastElementClosed() throws Exception {
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("alpha"));
		assertEquals("Alpha element started", "<alpha", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.closeElement());
		assertEquals("Element alpha is closed", "<alpha/>", out.toString());
		try {
			writer.openElement("delta");
			fail("Cannot open new elements once the first element has been closed");
		} catch (OperationNotAllowedException e) {
			// Cannot open new elements once the first element has been closed
		}
	}

	/**
	 * Call close element after last element closed.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void callCloseElementAfterLastElementClosed() throws Exception {
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("alpha"));
		assertEquals("Alpha element started", "<alpha", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.closeElement());
		assertEquals("Element alpha is closed", "<alpha/>", out.toString());
		try {
			writer.closeElement();
			fail("Cannot close elements once the first element has been closed");
		} catch (OperationNotAllowedException e) {
			// Cannot open new elements once the first element has been closed
		}
	}

	/**
	 * Close first element.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void closeFirstElement() throws Exception {
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("alpha"));
		assertEquals("Alpha element started", "<alpha", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.closeElement());
		assertEquals("Element alpha is closed", "<alpha/>", out.toString());
	}

	/**
	 * Close element with content.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void closeElementWithContent() throws Exception {
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("alpha"));
		assertEquals("Alpha element started", "<alpha", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("beta"));
		assertEquals("Beta element started", "<alpha><beta", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.closeElement());
		assertEquals("Element beta is closed", "<alpha><beta/>", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.closeElement());
		assertEquals("Element beta is closed", "<alpha><beta/></alpha>",
				out.toString());
		try {
			writer.closeElement();
			fail("Cannot close elements once the first element has been closed");
		} catch (OperationNotAllowedException e) {
			// Cannot open new elements once the first element has been closed
		}
	}

	/**
	 * Close element before first element.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void closeElementBeforeFirstElement() throws Exception {
		try {
			writer.closeElement();
			fail("Cannot close elements before the first element has been closed");
		} catch (OperationNotAllowedException e) {
			// Cannot open new elements before the first element has been closed
		}
	}

	/**
	 * Content after element.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void contentAfterElement() throws Exception {
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("alpha"));
		assertEquals("Alpha element started", "<alpha", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.content("foo bar"));
		assertEquals("Alpha tag closed. Content written", "<alpha>foo bar",
				out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.content(" and more foo bar"));
		assertEquals("Alpha tag closed. Content written",
				"<alpha>foo bar and more foo bar", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("beta"));
		assertEquals("Beta element started",
				"<alpha>foo bar and more foo bar<beta", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.closeElement());
		assertEquals("Element beta is closed",
				"<alpha>foo bar and more foo bar<beta/>", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.closeElement());
		assertEquals("Element beta is closed",
				"<alpha>foo bar and more foo bar<beta/></alpha>",
				out.toString());
		try {
			writer.content("A Sentence Too far");
			fail("Cannot write content once the first element has been closed");
		} catch (OperationNotAllowedException e) {
			// Cannot open new elements once the first element has been closed
		}
	}

	/**
	 * Content after last element.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void contentAfterLastElement() throws Exception {
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("alpha"));
		assertEquals("Alpha element started", "<alpha", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.closeElement());
		assertEquals("Element beta is closed", "<alpha/>", out.toString());
		try {
			writer.content("A Sentence Too far");
			fail("Cannot write content once the first element has been closed");
		} catch (OperationNotAllowedException e) {
			// Cannot open new elements once the first element has been closed
		}
	}

	/**
	 * Write content before first element.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void writeContentBeforeFirstElement() throws Exception {
		try {
			writer.content("Too early");
			fail("Cannot close elements before the first element has been closed");
		} catch (OperationNotAllowedException e) {
			// Cannot open new elements before the first element has been closed
		}
	}

	/**
	 * Content escaping.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void contentEscaping() throws Exception {
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("alpha"));
		assertEquals("Alpha element started", "<alpha", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.content("this&that"));
		assertEquals("Amphersands must be escaped", "<alpha>this&amp;that",
				out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.content("small<large"));
		assertEquals("Left angle brackets must be escaped",
				"<alpha>this&amp;thatsmall&lt;large", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.content("12>1"));
		assertEquals("Choose to escape right angle brackets",
				"<alpha>this&amp;thatsmall&lt;large12&gt;1", out.toString());

	}

	/**
	 * Attribute after last element.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void attributeAfterLastElement() throws Exception {
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("alpha"));
		assertEquals("Alpha element started", "<alpha", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.closeElement());
		assertEquals("Element beta is closed", "<alpha/>", out.toString());
		try {
			writer.attribute("foo", "bar");
			fail("Cannot write content once the first element has been closed");
		} catch (OperationNotAllowedException e) {
			// Cannot open new elements once the first element has been closed
		}
	}

	/**
	 * Attribute content before first element.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void attributeContentBeforeFirstElement() throws Exception {
		try {
			writer.attribute("foo", "bar");
			fail("Cannot close elements before the first element has been closed");
		} catch (OperationNotAllowedException e) {
			// Cannot open new elements before the first element has been closed
		}
	}

	/**
	 * Invalid attribute name.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void invalidAttributeName() throws Exception {
		writer.openElement("alpha");
		assertTrue("All strings ok", isValidAttributeName("alpha"));
		assertTrue("Strings and digits ok", isValidAttributeName("alpha77"));
		assertFalse("Must not start with digit",
				isValidAttributeName("5alpha77"));
		assertTrue("Colon ok", isValidAttributeName("alpha:77"));
		assertFalse("Greater than not ok", isValidAttributeName("alph<a77"));
		assertFalse("Less than not ok", isValidAttributeName("alph<a77"));
		assertFalse("Quote not ok", isValidAttributeName("alph'a77"));
	}

	/**
	 * Checks if is valid attribute name.
	 * 
	 * @param name
	 *            the name
	 * @return true, if is valid attribute name
	 * @throws Exception
	 *             the exception
	 */
	private boolean isValidAttributeName(String name) throws Exception {
		boolean result = true;
		try {
			writer.attribute(name, "");
		} catch (InvalidXmlException e) {
			result = false;
		}
		return result;
	}

	/**
	 * Escape attribute content.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void escapeAttributeContent() throws Exception {
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("alpha"));
		assertEquals("Alpha element started", "<alpha", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.attribute("one", "this&that"));
		assertEquals("Amphersands must be escaped",
				"<alpha one='this&amp;that'", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.attribute("two", "small<large"));
		assertEquals("Left angle brackets must be escaped",
				"<alpha one='this&amp;that' two='small&lt;large'",
				out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.attribute("three", "12>1"));
		assertEquals(
				"Choose to escape right angle brackets",
				"<alpha one='this&amp;that' two='small&lt;large' three='12&gt;1'",
				out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.attribute("four", "'quote'"));
		assertEquals(
				"Apostrophes must be escape",
				"<alpha one='this&amp;that' two='small&lt;large' three='12&gt;1' four='&apos;quote&apos;'",
				out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.attribute("five", "\"quote\""));
		assertEquals(
				"Double quotes must be escape",
				"<alpha one='this&amp;that' two='small&lt;large' three='12&gt;1' four='&apos;quote&apos;' five='&quot;quote&quot;'",
				out.toString());

	}

	/**
	 * Attribute in content.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void attributeInContent() throws Exception {
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("alpha"));
		assertEquals("Alpha element started", "<alpha", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.content("foo bar"));
		try {
			writer.attribute("name", "value");
			fail("attributes after body content are not allowed");
		} catch (InvalidXmlException e) {
			// attributes after body content are not allowed
		}
	}

	/**
	 * Out of range character.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void outOfRangeCharacter() throws Exception {
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("alpha"));
		assertEquals("Alpha element started", "<alpha", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.content(new String(ZERO_CHAR)));
		String out = this.out.toString();
		assertEquals("Replace illegal characters with question marks",
				"<alpha>?", out);
	}

	/**
	 * Attribute after element closed.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void attributeAfterElementClosed() throws Exception {
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("alpha"));
		assertEquals("Alpha element started", "<alpha", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("beta"));
		assertEquals("Beta element started", "<alpha><beta", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.closeElement());
		assertEquals("Beta element closed", "<alpha><beta/>", out.toString());
		try {
			writer.attribute("name", "value");
			fail("attributes after closed element are not allowed");
		} catch (InvalidXmlException e) {
			// attributes after body content are not allowed
		}
	}

	/**
	 * Close document before open.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void closeDocumentBeforeOpen() throws Exception {
		try {
			writer.closeDocument();
			fail("Cannot close document before the first element has been opened");
		} catch (OperationNotAllowedException e) {
			// Cannot open new elements before the first element has been opened
		}
	}

	/**
	 * Close document after root element closed.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void closeDocumentAfterRootElementClosed() throws Exception {
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("alpha"));
		assertEquals("Alpha element started", "<alpha", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.closeElement());
		assertEquals("Beta element started", "<alpha/>", out.toString());
		try {
			writer.closeDocument();
		} catch (OperationNotAllowedException e) {
			fail("No exception should be thrown when called after the root element is closed.");
		}
	}

	/**
	 * Close simple document.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void closeSimpleDocument() throws Exception {
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("alpha"));
		assertEquals("Alpha element started", "<alpha", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("beta"));
		assertEquals("Beta element started", "<alpha><beta", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.closeDocument());
		assertEquals("Beta element started", "<alpha><beta/></alpha>",
				out.toString());
	}

	/**
	 * Close complex document.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void closeComplexDocument() throws Exception {
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("alpha"));
		assertEquals("Alpha element started", "<alpha", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("beta"));
		assertEquals("Beta element started", "<alpha><beta", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.attribute("name", "value"));
		assertEquals("Beta element started", "<alpha><beta name='value'",
				out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.closeElement());
		assertEquals("Beta element started", "<alpha><beta name='value'/>",
				out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("beta"));
		assertEquals("Beta element started",
				"<alpha><beta name='value'/><beta", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.attribute("name", "value"));
		assertEquals("Beta element started",
				"<alpha><beta name='value'/><beta name='value'", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("gamma"));
		assertEquals("Beta element started",
				"<alpha><beta name='value'/><beta name='value'><gamma",
				out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.closeDocument());
		assertEquals(
				"Beta element started",
				"<alpha><beta name='value'/><beta name='value'><gamma/></beta></alpha>",
				out.toString());
	}

	/**
	 * Write prolog.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void writeProlog() throws Exception {
		assertEquals("XmlWriters should always return themselves", writer,
				writer.startDocument());
		assertEquals("Prolog written", "<?xml version='1.0'?>", out.toString());
	}

	/**
	 * Write after element.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void writeAfterElement() throws Exception {
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("alpha"));
		assertEquals("Alpha element started", "<alpha", out.toString());
		try {
			writer.startDocument();
			fail("Operation not allowed once an element has been written");
		} catch (OperationNotAllowedException e) {
			// Operation not allowed once an element has been written
		}
	}

	/**
	 * Write prolog two.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void writePrologTwo() throws Exception {
		assertEquals("XmlWriters should always return themselves", writer,
				writer.startDocument());
		assertEquals("Prolog written", "<?xml version='1.0'?>", out.toString());
		try {
			writer.startDocument();
			fail("Operation not allow once a prolog has been written");
		} catch (OperationNotAllowedException e) {
			// Operation not allowed once an prolog has been written
		}
	}

	/**
	 * Duplicate attributes.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void duplicateAttributes() throws Exception {
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("alpha"));
		assertEquals("Alpha element started", "<alpha", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.attribute("one", "1"));
		assertEquals("Attribute written", "<alpha one='1'", out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.openElement("beta"));
		assertEquals("Beta element started", "<alpha one='1'><beta",
				out.toString());
		assertEquals("XmlWriters should always return themselves", writer,
				writer.attribute("one", "1"));
		assertEquals("Beta element started", "<alpha one='1'><beta one='1'",
				out.toString());
		try {
			writer.attribute("one", "2");
			fail("Each attribute may only be written once");
		} catch (InvalidXmlException e) {
			// Each attribute may only be written once
		}
	}
}
