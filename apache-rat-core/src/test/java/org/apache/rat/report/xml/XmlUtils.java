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

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.UndeclaredThrowableException;

public final class XmlUtils {
    /**
     * Private constructor, to prevent accidental instantiation.
     */
    private XmlUtils() {
        // Does nothing
    }

    public static final boolean isWellFormedXml(final String string) throws Exception {
        return isWellFormedXml(new InputSource(new StringReader(string)));
    }

    public static final XMLReader newXMLReader() throws SAXException, ParserConfigurationException {
        final SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(false);
        spf.setNamespaceAware(true);
        return spf.newSAXParser().getXMLReader();
    }

    public static final boolean isWellFormedXml(final InputSource isource) {
        try {
            newXMLReader().parse(isource);
            return true;
        } catch (SAXException e) {
            e.printStackTrace();
            return false;
        } catch (IOException | ParserConfigurationException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    public static final boolean isWellFormedXml(final InputStream in) throws Exception {
        return isWellFormedXml(new InputSource(in));
    }
    
    public static final Document toDom(final InputStream in) throws SAXException, IOException, ParserConfigurationException, FactoryConfigurationError {
        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document result;
        result = builder.parse(in);
        return result;
    }
}
