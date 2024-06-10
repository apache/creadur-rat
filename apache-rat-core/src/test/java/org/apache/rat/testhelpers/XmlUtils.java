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
package org.apache.rat.testhelpers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.rat.report.xml.writer.IXmlWriter;
import org.apache.rat.utils.DefaultLog;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public final class XmlUtils {
    /**
     * Private constructor, to prevent accidental instantiation.
     */
    private XmlUtils() {
        // Does nothing
    }

    public static XMLReader newXMLReader() throws SAXException, ParserConfigurationException {
        final SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(false);
        spf.setNamespaceAware(true);
        return spf.newSAXParser().getXMLReader();
    }

    public static boolean isWellFormedXml(final String string) throws Exception {
        return isWellFormedXml(new InputSource(new StringReader(string)));
    }

    public static boolean isWellFormedXml(final InputStream in) throws Exception {
        return isWellFormedXml(new InputSource(in));
    }

    public static boolean isWellFormedXml(final InputSource isource) {
        try {
            newXMLReader().parse(isource);
            return true;
        } catch (SAXException e) {
            DefaultLog.getInstance().error(e.getMessage(), e);
            return false;
        } catch (IOException | ParserConfigurationException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    public static NodeList getNodeList(Object source, XPath xPath, String xpath) throws XPathExpressionException {
        return (NodeList) xPath.compile(xpath).evaluate(source, XPathConstants.NODESET);
    }

    public static boolean isPresent(Object source, XPath xPath, String xpath) throws XPathExpressionException {
        return !xPath.compile(xpath).evaluate(source).equals("null");
    }

    public static List<Node> getNodes(Object source, XPath xPath, String xpath) throws XPathExpressionException {
        NodeList nodeList = (NodeList) xPath.compile(xpath).evaluate(source, XPathConstants.NODESET);
        List<Node> result = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            result.add(nodeList.item(i));
        }
        return result;
    }

    public static Node getNode(Object source, XPath xPath, String xpath) throws XPathExpressionException {
        NodeList nodeList = getNodeList(source, xPath, xpath);
        assertEquals(1, nodeList.getLength(), "Could not find " + xpath);
        return nodeList.item(0);
    }

    public static String printNodeList(NodeList nodeList) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node n = nodeList.item(0);
            sb.append(n.getNodeName()).append(" ");
            NamedNodeMap nnm = n.getAttributes();
            for (int j = 0; j < nnm.getLength(); j++) {
                Node a = nnm.item(j);
                sb.append(a.getNodeName()).append("=").append(a.getNodeValue()).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public static Document toDom(final InputStream in)
            throws SAXException, IOException, ParserConfigurationException, FactoryConfigurationError {
        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document result;
        result = builder.parse(in);
        return result;
    }

    public static void writeAttribute(final IXmlWriter writer, final String name, final boolean booleanValue)
            throws IOException {
        final String value = Boolean.toString(booleanValue);
        writer.attribute(name, value);
    }

    /**
     * Print the and XML document to the output stream
     * 
     * @param out the OutputStream to print the document to.
     * @param document The XML DOM document to print
     */
    public static void printDocument(OutputStream out, Document document) {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();

            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            transformer.transform(new DOMSource(document), new StreamResult(new OutputStreamWriter(out, "UTF-8")));
        } catch (TransformerException | UnsupportedEncodingException e) {
            e.printStackTrace(new PrintStream(out));
        }
    }

}
