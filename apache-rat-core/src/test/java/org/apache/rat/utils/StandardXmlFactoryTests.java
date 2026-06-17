package org.apache.rat.utils;

import org.junit.jupiter.api.Test;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
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
