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
package org.apache.rat.report.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.rat.report.xml.writer.IXmlWriter;

/**
 * The Class MockXmlWriter.
 */
public class MockXmlWriter implements IXmlWriter {

	/** The calls. */
	public final List<Object> calls;

	/**
	 * Instantiates a new mock xml writer.
	 */
	public MockXmlWriter() {
		calls = new ArrayList<Object>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.rat.report.xml.writer.IXmlWriter#attribute(java.lang.CharSequence
	 * , java.lang.CharSequence)
	 */
	public IXmlWriter attribute(final CharSequence name,
			final CharSequence value)
			throws IOException {
		calls.add(new Attribute(name, value));
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.rat.report.xml.writer.IXmlWriter#closeDocument()
	 */
	public IXmlWriter closeDocument() throws IOException {
		calls.add(new CloseDocument());
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.rat.report.xml.writer.IXmlWriter#closeElement()
	 */
	public IXmlWriter closeElement() throws IOException {
		calls.add(new CloseElement());
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.rat.report.xml.writer.IXmlWriter#content(java.lang.CharSequence
	 * )
	 */
	public IXmlWriter content(final CharSequence content) throws IOException {
		calls.add(new Content(content));
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.rat.report.xml.writer.IXmlWriter#openElement(java.lang.
	 * CharSequence)
	 */
	public IXmlWriter openElement(final CharSequence elementName)
			throws IOException {
		calls.add(new OpenElement(elementName));
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.rat.report.xml.writer.IXmlWriter#startDocument()
	 */
	public IXmlWriter startDocument() throws IOException {
		calls.add(new StartDocument());
		return this;
	}

	/**
	 * Checks if is close element.
	 * 
	 * @param index
	 *            the index
	 * @return true, if is close element
	 */
	public boolean isCloseElement(final int index) {
		boolean result = false;
		final Object call = calls.get(index);
		result = call instanceof CloseElement;
		return result;
	}

	/**
	 * Checks if is content.
	 * 
	 * @param content
	 *            the content
	 * @param index
	 *            the index
	 * @return true, if is content
	 */
	public boolean isContent(final String content, final int index) {
		boolean result = false;
		final Object call = calls.get(index);
		if (call instanceof Content) {
			Content contentCall = (Content) call;
			result = content.equals(contentCall.content);
		}
		return result;
	}

	/**
	 * Checks if is open element.
	 * 
	 * @param name
	 *            the name
	 * @param index
	 *            the index
	 * @return true, if is open element
	 */
	public boolean isOpenElement(final String name, final int index) {
		boolean result = false;
		final Object call = calls.get(index);
		if (call instanceof OpenElement) {
			OpenElement openElement = (OpenElement) call;
			result = name.equals(openElement.elementName);
		}
		return result;
	}

	/**
	 * Checks if is attribute.
	 * 
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 * @param index
	 *            the index
	 * @return true, if is attribute
	 */
	public boolean isAttribute(final String name, final String value,
			final int index) {
		boolean result = false;
		final Object call = calls.get(index);
		if (call instanceof Attribute) {
			Attribute attribute = (Attribute) call;
			result = name.equals(attribute.name)
					&& value.equals(attribute.value);
		}
		return result;
	}

	/**
	 * The Class StartDocument.
	 */
	public class StartDocument {
	}

	/**
	 * The Class CloseDocument.
	 */
	public class CloseDocument {
	}

	/**
	 * The Class CloseElement.
	 */
	public class CloseElement {
	}

	/**
	 * The Class OpenElement.
	 */
	public class OpenElement {

		/** The element name. */
		public final CharSequence elementName;

		/**
		 * Instantiates a new open element.
		 * 
		 * @param elementName
		 *            the element name
		 */
		private OpenElement(final CharSequence elementName) {
			this.elementName = elementName;
		}
	}

	/**
	 * The Class Content.
	 */
	public class Content {

		/** The content. */
		public final CharSequence content;

		/**
		 * Instantiates a new content.
		 * 
		 * @param content
		 *            the content
		 */
		private Content(final CharSequence content) {
			this.content = content;
		}
	}

	/**
	 * The Class Attribute.
	 */
	public class Attribute {

		/** The name. */
		public final CharSequence name;

		/** The value. */
		public final CharSequence value;

		/**
		 * Instantiates a new attribute.
		 * 
		 * @param name
		 *            the name
		 * @param value
		 *            the value
		 */
		private Attribute(final CharSequence name, final CharSequence value) {
			this.name = name;
			this.value = value;
		}
	}
}
