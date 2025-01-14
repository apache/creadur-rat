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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringReader;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
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

    public static boolean isWellFormedXml(final String string) {
        return isWellFormedXml(new InputSource(new StringReader(string)));
    }

    public static boolean isWellFormedXml(final InputStream in) {
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
        Object node = xPath.compile(xpath).evaluate(source, XPathConstants.NODE);
        return node != null;
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
        assertEquals(1, nodeList.getLength(), "Could not find exactly one" + xpath);
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
     * Print the properties and XML document to the output stream
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

            transformer.transform(new DOMSource(document), new StreamResult(new OutputStreamWriter(out, StandardCharsets.UTF_8)));
        } catch (TransformerException e) {
            e.printStackTrace(new PrintStream(out));
        }
    }

    public static String getAttribute(Object source, XPath xPath, String xpath, String attribute) throws XPathExpressionException {
        Node node = XmlUtils.getNode(source, xPath, xpath);
        NamedNodeMap attr = node.getAttributes();
        node = attr.getNamedItem(attribute);
        assertThat(node).as(attribute + " was not found").isNotNull();
        return node.getNodeValue();
    }

    public static Map<String, String> mapOf(String... parts) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < parts.length; i += 2) {
            map.put(parts[i], parts[i+1]);
        }
        return map;
    }

    public static void assertAttributes(Object source, XPath xPath, String xpath, String... mapValues) throws XPathExpressionException {
        assertAttributes(source, xPath, xpath, mapOf(mapValues));
    }

    public static void assertAttributes(Object source, XPath xPath, String xpath, Map<String, String> attributes) throws XPathExpressionException {
        Node node = XmlUtils.getNode(source, xPath, xpath);
        NamedNodeMap attr = node.getAttributes();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            node = attr.getNamedItem(entry.getKey());
            assertThat(node).as(() -> entry.getKey() + " was not found on " + xpath).isNotNull();
            assertThat(node.getNodeValue()).as(() -> entry.getKey() + " on " + xpath).isEqualTo(entry.getValue());
        }
    }

    public static void assertIsPresent(Object source, XPath xPath, String xpath) throws XPathExpressionException {
        assertThat(isPresent(source, xPath, xpath)).as("Presence of " + xpath).isTrue();
    }

    public static void assertIsNotPresent(Object source, XPath xPath, String xpath) throws XPathExpressionException {
        assertThat(isPresent(source, xPath, xpath)).as("Non-presence of " + xpath).isFalse();
    }
}
