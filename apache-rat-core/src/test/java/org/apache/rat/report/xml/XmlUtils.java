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
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.UndeclaredThrowableException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * The Class XmlUtils.
 */
public final class XmlUtils {
	/**
	 * Private constructor, to prevent accidental instantiation.
	 */
	private XmlUtils() {
		// Does nothing
	}

	/**
	 * Checks if is well formed xml.
	 * 
	 * @param string
	 *            the string
	 * @return true, if is well formed xml
	 */
	public static boolean isWellFormedXml(final String string) {
		return isWellFormedXml(new InputSource(new StringReader(string)));
	}

	/**
	 * New xml reader.
	 * 
	 * @return the xML reader
	 * @throws SAXException
	 *             the sAX exception
	 * @throws ParserConfigurationException
	 *             the parser configuration exception
	 */
	public static XMLReader newXMLReader() throws SAXException,
			ParserConfigurationException {
		final SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setValidating(false);
		spf.setNamespaceAware(true);
		return spf.newSAXParser().getXMLReader();
	}

	/**
	 * Checks if is well formed xml.
	 * 
	 * @param isource
	 *            the isource
	 * @return true, if is well formed xml
	 */
	public static boolean isWellFormedXml(final InputSource isource) {
		boolean result = false;
		try {
			newXMLReader().parse(isource);
			result = true;
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			throw new UndeclaredThrowableException(e);
		} catch (ParserConfigurationException e) {
			throw new UndeclaredThrowableException(e);
		}
		return result;
	}

	/**
	 * Checks if is well formed xml.
	 * 
	 * @param inputStream
	 *            the input stream
	 * @return true, if is well formed xml
	 */
	public static boolean isWellFormedXml(final InputStream inputStream) {
		return isWellFormedXml(new InputSource(inputStream));
	}

	/**
	 * To dom.
	 * 
	 * @param inputStream
	 *            the input stream
	 * @return the document
	 * @throws SAXException
	 *             the sAX exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws ParserConfigurationException
	 *             the parser configuration exception
	 * @throws FactoryConfigurationError
	 *             the factory configuration error
	 */
	public static Document toDom(final InputStream inputStream)
			throws SAXException, IOException, ParserConfigurationException,
			FactoryConfigurationError {
		final DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document result;
		result = builder.parse(inputStream);
		return result;
	}
}
