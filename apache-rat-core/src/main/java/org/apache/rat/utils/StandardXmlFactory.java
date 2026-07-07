/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

/**
 * Factory to create standard XML objects. The intention of this class is to resolve in a consistent manner the
 * XXE errors and similar XML IO errors.
 */
public final class StandardXmlFactory {

    /**
     This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all
     XML entity attacks are prevented.
     */
     //Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
    private static final String FEATURE  = "http://apache.org/xml/features/disallow-doctype-decl";

    private StandardXmlFactory() {
        // do not instantiate.
    }

    /**
     * Create a transformer with no stylesheet.
     * @return the transformer.
     * @throws TransformerConfigurationException on error.
     */
    public static Transformer createTransformer() throws TransformerConfigurationException {
        return createTransformer(null);
    }

    /**
     * Create a transformer with the specified stylesheet.
     * @param styleIn the stylesheet to use.
     * @return the transformer.
     * @throws TransformerConfigurationException on error.
     */
    public static Transformer createTransformer(final InputStream styleIn) throws TransformerConfigurationException {
        TransformerFactory factory = TransformerFactory.newInstance(); // NOSONAR
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        Transformer transformer = styleIn == null ? factory.newTransformer() : factory.newTransformer(new StreamSource(styleIn));
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        return transformer;
    }

    /**
     * Creates a DocumentBuilder with reasonable security settings.
     * @return a DocumentBuilder.
     */
    public static DocumentBuilder documentBuilder() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(FEATURE, true);

            // and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
            factory.setXIncludeAware(false);

            // remaining parser logic
            return factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("No XML parser defined", e);
        }
    }

    /**
     * Write an XML document to a file.
     * @param document the document to write
     * @param file the file to write to.
     */
    public static void writeDocument(final Document document, final File file) throws IOException, TransformerException {
        DOMSource source = new DOMSource(document);
        FileWriter writer = new FileWriter(file);
        StreamResult result = new StreamResult(writer);
        createTransformer().transform(source, result);
    }

    /**
     * Write an XML document to a file.
     * @param document the document to write.
     */
    public static String serializeDocument(final Document document) throws TransformerException {
        DOMSource source = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult sink = new StreamResult(writer);
        createTransformer().transform(source, sink);
        String result = writer.toString();
        return StringUtils.defaultIfBlank(result, "");
    }
}
