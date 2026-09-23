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
import javax.xml.namespace.QName;
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
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.rat.report.xml.writer.XmlWriter;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.StandardXmlFactory;
import org.opentest4j.AssertionFailedError;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Utilities to help test XML doucments/
 */
public final class XmlUtils {
    /**
     * Private constructor, to prevent accidental instantiation.
     */
    private XmlUtils() {
        // Does nothing
    }

    /**
     * Construct a safe XML reader.
     * @return an XML reader.
     * @throws SAXException on sax exception
     * @throws ParserConfigurationException on parser configuration exception
     */
    public static XMLReader newXMLReader() throws SAXException, ParserConfigurationException {
        final SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(false);
        spf.setNamespaceAware(true);
        return spf.newSAXParser().getXMLReader();
    }

    /**
     * Determines if the document string is wellformed.
     * @param string the document string to check.
     * @return {@code true} if the document is wellformed, {@code false} otherwise.
     */
    public static boolean isWellFormedXml(final String string) {
        return isWellFormedXml(new InputSource(new StringReader(string)));
    }

    /**
     * Determines if the document in the input stream is wellformed.
     * @param in the input stream containing the document.
     * @return {@code true} if the document is wellformed, {@code false} otherwise.
     */
    public static boolean isWellFormedXml(final InputStream in) {
        return isWellFormedXml(new InputSource(in));
    }

    /**
     * Determines if the document in the input source is wellformed.
     * @param isource the input source containing the document.
     * @return {@code true} if the document is wellformed, {@code false} otherwise.
     */
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

    /**
     * Gets a Nodelist from an xpath string.
     * @param source the context for the xpath statement to be evaluated in.  See {@link XPathExpression#evaluate(Object, QName)}.
     * @param xPath The XPath object to compile the statement with.
     * @param xpath the Xpath statement to compile.
     * @return the NodeList of nodes that match the xpath.
     * @throws XPathExpressionException on error.
     */
    public static NodeList getNodeList(Object source, XPath xPath, String xpath) throws XPathExpressionException {
        return (NodeList) xPath.compile(xpath).evaluate(source, XPathConstants.NODESET);
    }

    /**
     * Determines if an xpath identified an node in the source
     * @param source the context for the xpath statement to be evaluated in.  See {@link XPathExpression#evaluate(Object, QName)}.
     * @param xPath The XPath object to compile the statement with.
     * @param xpath the Xpath statement to compile.
     * @return {@code true} if the document is contains the node identified by the xpath statement, {@code false} otherwise.
     * @throws XPathExpressionException on error.
     */
    public static boolean isPresent(Object source, XPath xPath, String xpath) throws XPathExpressionException {
        Object node = xPath.compile(xpath).evaluate(source, XPathConstants.NODE);
        return node != null;
    }

    /**
     * Gets a List of Nodes from an xpath string.
     * @param source the context for the xpath statement to be evaluated in.  See {@link XPathExpression#evaluate(Object, QName)}.
     * @param xPath The XPath object to compile the statement with.
     * @param xpath the Xpath statement to compile.
     * @return the list of nodes that match the xpath.
     * @throws XPathExpressionException on error.
     */
    public static List<Node> getNodes(Object source, XPath xPath, String xpath) throws XPathExpressionException {
        NodeList nodeList = (NodeList) xPath.compile(xpath).evaluate(source, XPathConstants.NODESET);
        List<Node> result = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            result.add(nodeList.item(i));
        }
        return result;
    }

    /**
     * Gets a node identified by an xpath.
     * @param source the context for the xpath statement to be evaluated in.  See {@link XPathExpression#evaluate(Object, QName)}.
     * @param xPath The XPath object to compile the statement with.
     * @param xpath the Xpath statement to compile.
     * @return the identified Node.
     * @throws XPathExpressionException on error.
     * @throws AssertionFailedError if more than one node is found.
     */
    public static Node getNode(Object source, XPath xPath, String xpath) throws XPathExpressionException {
        NodeList nodeList = getNodeList(source, xPath, xpath);
        assertEquals(1, nodeList.getLength(), "Could not find exactly one" + xpath);
        return nodeList.item(0);
    }

    /**
     * Prints the specifide NodeList as a string representation of its contents.
     * @param nodeList the Nodelist to pring.
     * @return the String that contains the textual representation of the NodeList nodes.
     */
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

    /**
     * Reads an input stream into a document.
     * @param inputStream the input stream to read.
     * @return the Document
     * @throws SAXException on sax Error
     * @throws IOException on IO Error
     */
    public static Document toDom(final InputStream inputStream)
            throws SAXException, IOException {
        return StandardXmlFactory.documentBuilder().parse(inputStream);
    }

    /**
     * Write a boolean attribute ot an XML writer.
     * @param writer the writer to write to.
     * @param name the name of the attribute
     * @param booleanValue the boolean value.
     * @throws IOException on write error.
     */
    public static void writeAttribute(final XmlWriter writer, final String name, final boolean booleanValue)
            throws IOException {
        final String value = Boolean.toString(booleanValue);
        writer.attribute(name, value);
    }

    /**
     * Print the XML document to the output stream
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

    /**
     * Get an attribute from an Xpath statement
     * @param source the context for the xpath statement to be evaluated in.  See {@link XPathExpression#evaluate(Object, QName)}.
     * @param xPath The XPath object to compile the statement with.
     * @param xpath the Xpath statement to compile.
     * @param attribute attribute to retrieve from the node specified by the xpath statement.
     * @return the string value of the attribute.
     * @throws XPathExpressionException on error
     */
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

    /**
     * Use {@link #assertAttributes(Object, XPath, String, Map)}.
     * @param source the context for the xpath statement to be evaluated in.  See {@link XPathExpression#evaluate(Object, QName)}.
     * @param xPath The XPath object to compile the statement with.
     * @param xpath the Xpath statement to compile.
     * @param values a decomposed map of values.
     * @throws XPathExpressionException
     */
    @Deprecated
    public static void assertAttributes(Object source, XPath xPath, String xpath, String... values) throws XPathExpressionException {
        assertAttributes(source, xPath, xpath, mapOf(values));
    }

    /**
     * Assert that attributes are set on a node.
     * @param source the context for the xpath statement to be evaluated in.  See {@link XPathExpression#evaluate(Object, QName)}.
     * @param xPath The XPath object to compile the statement with.
     * @param xpath the Xpath statement to compile.
     * @param attributes a map of attribute names to values.
     * @throws XPathExpressionException on error.
     */
    public static void assertAttributes(Object source, XPath xPath, String xpath, Map<String, String> attributes) throws XPathExpressionException {
        Node node = XmlUtils.getNode(source, xPath, xpath);
        NamedNodeMap attr = node.getAttributes();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            node = attr.getNamedItem(entry.getKey());
            assertThat(node).as(() -> entry.getKey() + " was not found on " + xpath).isNotNull();
            assertThat(node.getNodeValue()).as(() -> entry.getKey() + " on " + xpath).isEqualTo(entry.getValue());
        }
    }

    /**
     * Assert that an xpath is present in the document.
     * @param source the context for the xpath statement to be evaluated in.  See {@link XPathExpression#evaluate(Object, QName)}.
     * @param xPath The XPath object to compile the statement with.
     * @param xpath the Xpath statement to compile.
    */
    public static void assertIsPresent(Object source, XPath xPath, String xpath) throws XPathExpressionException {
        assertThat(isPresent(source, xPath, xpath)).as("Presence of " + xpath).isTrue();
    }

    /**
     * Assert that an xpath is not present in the document.
     * @param source the context for the xpath statement to be evaluated in.  See {@link XPathExpression#evaluate(Object, QName)}.
     * @param xPath The XPath object to compile the statement with.
     * @param xpath the Xpath statement to compile.
     */
    public static void assertIsNotPresent(Object source, XPath xPath, String xpath) throws XPathExpressionException {
        assertThat(isPresent(source, xPath, xpath)).as("Non-presence of " + xpath).isFalse();
    }

    /**
     * Assert that a named xpath is present in the document.
     * @param identifier the name of the object.
     * @param source the context for the xpath statement to be evaluated in.  See {@link XPathExpression#evaluate(Object, QName)}.
     * @param xPath The XPath object to compile the statement with.
     * @param xpath the Xpath statement to compile.
     */
    public static void assertIsPresent(String identifier, Object source, XPath xPath, String xpath) throws XPathExpressionException {
        assertThat(isPresent(source, xPath, xpath)).as(identifier + ": Presence of " + xpath).isTrue();
    }

    /**
     * Assert that a named xpath is not present in the document.
     * @param identifier the name of the object.
     * @param source the context for the xpath statement to be evaluated in.  See {@link XPathExpression#evaluate(Object, QName)}.
     * @param xPath The XPath object to compile the statement with.
     * @param xpath the Xpath statement to compile.
     */
    public static void assertIsNotPresent(String identifier, Object source, XPath xPath, String xpath) throws XPathExpressionException {
        assertThat(isPresent(source, xPath, xpath)).as(identifier + ": Non-presence of " + xpath).isFalse();
    }
}
