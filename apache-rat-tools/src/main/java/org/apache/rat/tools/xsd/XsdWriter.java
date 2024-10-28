/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.tools.xsd;

import java.io.IOException;
import java.io.Writer;

import org.apache.rat.report.xml.writer.impl.base.XmlWriter;

/**
 * A writer that writes XSD nodes.
 */
public class XsdWriter {
    /** The XML Writer that this writer uses */
    private final XmlWriter writer;

    /** Types of elements in the XSD */
    public enum Type {
        /** An element */
        ELEMENT("xs:element"),
        /** An attribute */
        ATTRIBUTE("xs:attribute"),
        /** A complex type */
        COMPLEX("xs:complexType"),
        /** A sequence */
        SEQUENCE("xs:sequence"),
        /** A simple type */
        SIMPLE("xs:simpleContent"),
        /** An extension */
        EXTENSION("xs:extension"),
        /** A choice */
        CHOICE("xs:choice"),
        /** A complex type */
        COMPLEX_CONTENT("xs:complexContent");
        /** the element name associated with the type */
        private final String elementName;

        /**
         * Type constructor
         *
         * @param name The element name associated with the type
         */
        Type(final String name) {
            elementName = name;
        }
    }

    /**
     * Creates an XSD writer that wraps a standard Writer.
     * @param writer the writer to wrap.
     */
    public XsdWriter(final Writer writer) {
        this.writer = new XmlWriter(writer);
    }

    /**
     * Initializes the writer.  Writes the initial "xs:schema tag" .
     * @return the Writer.
     * @throws IOException on error.
     */
    public XsdWriter init() throws IOException {
        writer.startDocument()
        .openElement("xs:schema")
        .attribute("attributeFormDefault", "unqualified")
                .attribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema");
        return this;
    }

    /**
     * Finishes the process.  Closes the document.
     * @throws IOException on error.
     */
    public void finish() throws IOException {
        writer.closeDocument();
    }

    /**
     * Writes an attribute map, each pair of items in the string list are consider attribue name and value.
     * @param attributeMap the array of attribute names and  values.
     * @throws IOException on error.
     */
    private void writeAttributes(final String[] attributeMap) throws IOException {
        if (attributeMap != null) {
            for (int i = 0; i < attributeMap.length; i += 2) {
                writer.attribute(attributeMap[i], attributeMap[i + 1]);
            }
        }
    }

    /**
     * Opens (Starts) an element of the specified type along with its attributes.
     * @param type the Type to start.
     * @param attributeMap the attributes for the element
     * @return this
     * @throws IOException on error.
     */
    public XsdWriter open(final Type type, final String... attributeMap) throws IOException {
        writer.openElement(type.elementName);
        writeAttributes(attributeMap);
        return this;
    }

    /**
     * Writes the attributes
     * @param name The name of the attribute
     * @param attributeMap the attributes of the attribute.
     * @return this
     * @throws IOException on error.
     */
    public XsdWriter attribute(final String name, final String... attributeMap) throws IOException {
        writer.openElement("xs:attribute").attribute("name", name);
        writeAttributes(attributeMap);
        writer.closeElement();
        return this;
    }

    /**
     * Closes (Ends) the element for the type.
     * @param type The type to close
     * @return this.
     * @throws IOException on error
     */
    public XsdWriter close(final Type type) throws IOException {
        writer.closeElement(type.elementName);
        return this;
    }

}
