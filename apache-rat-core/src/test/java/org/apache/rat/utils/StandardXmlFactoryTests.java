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

import org.junit.jupiter.api.Test;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for StandardXmlFactory.
 */
public class StandardXmlFactoryTests {

    @Test
    void noArg() throws TransformerConfigurationException {
        assertThat(StandardXmlFactory.create()).isNotNull();
    }

    @Test
    void noEmptyInput() {
        assertThatThrownBy(() -> StandardXmlFactory.create(InputStream.nullInputStream()))
                .isInstanceOf(TransformerConfigurationException.class)
                .hasMessageContaining("Premature end of file.");
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
