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

import org.apache.rat.report.xml.writer.XmlWriter;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for StandardXmlFactory.
 */
public class StandardXmlFactoryTests {

    public static final String SIMPLE_DOCUMENT_TEXT =  """
                <first>
                    <second attr1="one"/>
                </first>
                """;

    public static Document simpleDocument() throws IOException, SAXException {
        StringWriter writer = new StringWriter();
        try (XmlWriter xmlWriter = new XmlWriter(writer)) {
            xmlWriter.startDocument().startElement("first")
                    .startElement("second")
                    .attribute("attr1", "one")
                    .closeDocument();
        }
        return StandardXmlFactory.documentBuilder()
                .parse(new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void noArg() throws TransformerConfigurationException {
        assertThat(StandardXmlFactory.createTransformer()).isNotNull();
    }

    @Test
    void noEmptyInput() {
        assertThatThrownBy(() -> StandardXmlFactory.createTransformer(InputStream.nullInputStream()))
                .isInstanceOf(TransformerConfigurationException.class);
    }

    @Test
    void badDocumentBuilderTest() {
        try {
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory", DummyDocumentBuilderFactory.class.getName());
            assertThatThrownBy(StandardXmlFactory::documentBuilder)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No XML parser defined");
        } finally {
            System.clearProperty("javax.xml.parsers.DocumentBuilderFactory");
        }
    }

    @Test
    void goodDocumentBuilderTest() {
        assertThat(StandardXmlFactory.documentBuilder())
                .isNotNull();
    }

    @Test
    void serializeDocumentTest() throws IOException, SAXException, TransformerException {
        String result = StandardXmlFactory.serializeDocument(simpleDocument());
        assertThat(result).isEqualTo(SIMPLE_DOCUMENT_TEXT);
    }

    @Test
    void serializeEmptyDocumentYieldsEmtpyString() throws TransformerException {
        Document document = StandardXmlFactory.documentBuilder().newDocument();
        assertThat(StandardXmlFactory.serializeDocument(document)).isEmpty();
    }

    /**
     * Class to test failing document builder.
     */
    public static class DummyDocumentBuilderFactory extends DocumentBuilderFactory {
        @Override
        public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
            throw new ParserConfigurationException();
        }

        @Override
        public void setAttribute(String name, Object value) throws IllegalArgumentException {
            throw new IllegalArgumentException();
        }

        @Override
        public Object getAttribute(String name) throws IllegalArgumentException {
            throw new IllegalArgumentException();
        }

        @Override
        public void setFeature(String name, boolean value) throws ParserConfigurationException {
            throw new ParserConfigurationException();
        }

        @Override
        public boolean getFeature(String name) throws ParserConfigurationException {
            throw new ParserConfigurationException();
        }
    }
}
