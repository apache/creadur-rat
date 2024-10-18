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

import org.apache.rat.report.xml.writer.impl.base.XmlWriter;

import java.io.IOException;
import java.io.Writer;

public class XsdWriter {
    private final XmlWriter writer;

    public enum Type {
        ELEMENT("xs:element"), ATTRIBUTE("xs:attribute"),
        COMPLEX("xs:complexType"), SEQUENCE("xs:sequence"), SIMPLE("xs:simpleContent"),
        EXTENSION("xs:extension"), CHOICE("xs:choice"), COMPLEX_CONTENT("xs:complexContent");
        private final String elementName;

        Type(String name) {
            elementName = name;
        }
    }

    public XsdWriter(Writer writer) {
        this.writer = new XmlWriter(writer);
    }

    public XsdWriter init() throws IOException {
        writer.startDocument()
        .openElement("xs:schema")
        .attribute("attributeFormDefault", "unqualified")
                .attribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema");
        return this;
    }

    public void finish() throws IOException {
        writer.closeDocument();
    }

    private void writeAttributes(String[] attributeMap) throws IOException {
        if (attributeMap != null) {
            for (int i = 0; i < attributeMap.length; i += 2) {
                writer.attribute(attributeMap[i], attributeMap[i + 1]);
            }
        }
    }

    public XsdWriter open(Type type, String... attributeMap) throws IOException {
        writer.openElement(type.elementName);
        writeAttributes(attributeMap);
        return this;
    }

    public XsdWriter attribute(String name, String... attributeMap) throws IOException {
        writer.openElement("xs:attribute").attribute("name", name);
        writeAttributes(attributeMap);
        writer.closeElement();
        return this;
    }

    public XsdWriter close(Type type) throws IOException {
        writer.closeElement(type.elementName);
        return this;
    }

}
