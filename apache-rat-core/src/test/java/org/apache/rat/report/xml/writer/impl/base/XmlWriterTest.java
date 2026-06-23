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
package org.apache.rat.report.xml.writer.impl.base;

import org.apache.rat.report.xml.writer.InvalidXmlException;
import org.apache.rat.report.xml.writer.OperationNotAllowedException;
import org.apache.rat.report.xml.writer.XmlWriter;
import org.apache.rat.testhelpers.XmlUtils;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XmlWriterTest {

    @Test
    void returnValues() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
        assertThat(writer.startElement("alpha")).as("XmlWriters should always return themselves").isEqualTo(writer);
            assertThat(writer.attribute("beta", "b")).as("XmlWriters should always return themselves").isEqualTo(writer);
            assertThat(writer.content("gamma")).as("XmlWriters should always return themselves").isEqualTo(writer);
            assertThat(writer.closeElement()).as("XmlWriters should always return themselves").isEqualTo(writer);
        }
    }

    @Test
    void startElement() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startElement("alpha");
            assertThat(out.toString()).isEqualTo("<alpha");
            writer.startElement("beta");
            assertThat(out.toString()).isEqualTo("<alpha><beta");
            writer.closeElement();
            assertThat(out.toString()).isEqualTo("<alpha><beta/>");
            writer.startElement("gamma");
            assertThat(out.toString()).isEqualTo("<alpha><beta/><gamma");
        }
    }
    
    @Test
    void invalidElementName() throws Exception {
        assertThat(isValidElementName("alpha")).as("All strings ok").isTrue();
        assertThat(isValidElementName("alpha77")).as("Strings and digits ok").isTrue();
        assertThat(isValidElementName("5alpha77")).as("Must no start with digit").isFalse();
        assertThat(isValidElementName("alph<a77")).as("Greater than not ok").isFalse();
        assertThat(isValidElementName("alph<a77")).as("Less than not ok").isFalse();
        assertThat(isValidElementName("alph'a77")).as("Quote not ok").isFalse();
        assertThat(isValidElementName("alph-a77")).as("Dash ok").isTrue();
        assertThat(isValidElementName("alph_a77")).as("Underscore ok").isTrue();
        assertThat(isValidElementName("alph.a77")).as("Dot ok").isTrue();
        assertThat(isValidElementName("alpha:77")).as("Colon ok").isTrue();
        assertThat(isValidElementName("-a77")).as("Start with dash not ok").isFalse();
        assertThat(isValidElementName("_a77")).as("Start with underscore ok").isTrue();
        assertThat(isValidElementName(".a77")).as("Start with dot not ok").isFalse();
        assertThat(isValidElementName(":a77")).as("Start with colon ok").isTrue();
    }
    
    private boolean isValidElementName(String elementName) throws Exception {
        boolean result = true;
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startElement(elementName);
        } catch (InvalidXmlException e) {
            result = false;
        }
        return result;
    }

    @Test
    void callStartElementAfterLastElementClosed() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startElement("alpha");
            assertThat(out.toString()).isEqualTo("<alpha");
            writer.closeElement();
            assertThat(out.toString()).isEqualTo("<alpha/>");
            assertThatThrownBy(() -> writer.startElement("delta"))
                    .hasMessageContaining("Root element already closed. Cannot open new element.")
                            .isInstanceOf(OperationNotAllowedException.class);
        }
    }    

    @Test
    void callCloseElementAfterLastElementClosed() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startElement("alpha");
            assertThat(out.toString()).isEqualTo("<alpha");
            writer.closeElement();
            assertThat(out.toString()).isEqualTo("<alpha/>");
            assertThatThrownBy(writer::closeElement)
                    .hasMessageContaining("Root element already closed. Cannot open new element.")
                    .isInstanceOf(OperationNotAllowedException.class);
        }
    }

    @Test
    void closeFirstElement() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startElement("alpha");
            assertThat(out.toString()).isEqualTo("<alpha");
            writer.closeElement();
            assertThat(out.toString()).isEqualTo("<alpha/>");
        }
    }
    
    @Test
    void closeElementWithContent() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startElement("alpha").startElement("beta");
            assertThat(out.toString()).isEqualTo("<alpha><beta");

                   writer.closeElement();
            assertThat(out.toString()).isEqualTo("<alpha><beta/>");
            writer.closeElement();
            assertThat(out.toString()).isEqualTo("<alpha><beta/></alpha>");
            assertThatThrownBy(writer::closeElement)
                    .hasMessageContaining("Root element already closed. Cannot open new element.")
                    .isInstanceOf(OperationNotAllowedException.class);
        }
    }
    
    @Test
    void closeElementBeforeFirstElement() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            assertThatThrownBy(writer::closeElement)
                    .hasMessageContaining("Close called before an element has been opened.")
                    .isInstanceOf(OperationNotAllowedException.class);
        } catch (OperationNotAllowedException expected) {
            assertThat(expected).hasMessageContaining("Close called before an element has been opened.");
        }

    }
    
    @Test
    void contentAfterElement() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startElement("alpha");
            assertThat(out.toString()).isEqualTo("<alpha");
            writer.content("foo bar");
            assertThat(out.toString()).isEqualTo("<alpha>foo bar");
            writer.content(" and more foo bar");
            assertThat(out.toString()).isEqualTo("<alpha>foo bar and more foo bar");
            writer.startElement("beta");
            assertThat(out.toString()).isEqualTo("<alpha>foo bar and more foo bar<beta");
            writer.closeElement();
            assertThat(out.toString()).isEqualTo("<alpha>foo bar and more foo bar<beta/>");
            writer.closeElement();
            assertThat(out.toString()).isEqualTo("<alpha>foo bar and more foo bar<beta/></alpha>");
            assertThatThrownBy(() -> writer.content("A Sentence Too far"))
                    .hasMessageContaining("Root element already closed. Cannot open new element.")
                            .isInstanceOf(OperationNotAllowedException.class);
        }
    }

    @Test
    void contentAfterLastElement() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startElement("alpha");
            assertThat(out.toString()).isEqualTo("<alpha");
            writer.closeElement();
            assertThat(out.toString()).isEqualTo("<alpha/>");
            assertThatThrownBy(() -> writer.content("A Sentence Too far"))
                    .hasMessageContaining("Root element already closed. Cannot open new element.")
                            .isInstanceOf(OperationNotAllowedException.class);
        }
    }
    
    @Test
    void writeContentBeforeFirstElement() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            assertThatThrownBy(() -> writer.content("Too early"))
                    .hasMessageContaining("An element must be opened before content can be written.")
                            .isInstanceOf(OperationNotAllowedException.class);
        } catch (OperationNotAllowedException expected) {
            assertThat(expected).hasMessageContaining("Close called before an element has been opened.");
        }
    }
    
    @Test
    void contentEscaping() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startElement("alpha");
            assertThat(out.toString()).isEqualTo("<alpha");
            writer.content("this&that");
            assertThat(out.toString()).isEqualTo("<alpha>this&amp;that");
            writer.content("small<large");
            assertThat(out.toString()).isEqualTo("<alpha>this&amp;thatsmall&lt;large");
            writer.content("12>1");
            assertThat(out.toString()).isEqualTo("<alpha>this&amp;thatsmall&lt;large12&gt;1");
        }
    }

    @Test
    void attributeAfterLastElement() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startElement("alpha").closeElement();
            assertThat(out.toString()).isEqualTo("<alpha/>");
            assertThatThrownBy(() -> writer.attribute("foo", "bar"))
                    .hasMessageContaining("Root element already closed. Cannot open new element.")
                            .isInstanceOf(OperationNotAllowedException.class);
        }
    }
    
    @Test
    void attributeContentBeforeFirstElement() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            assertThatThrownBy(() -> writer.attribute("foo", "bar"))
                    .hasMessageContaining("Close called before an element has been opened.")
                    .isInstanceOf(OperationNotAllowedException.class);
        } catch (OperationNotAllowedException expected) {
            assertThat(expected).hasMessageContaining("Close called before an element has been opened.");
        }
    }
    
    @Test
    void invalidAttributeName() throws Exception {
        assertThat(isValidAttributeName("alpha")).as("All string ok").isTrue();
        assertThat(isValidAttributeName("alpha77")).as("Strings and digits ok").isTrue();
        assertThat(isValidAttributeName("5alpha77")).as("Must not start with digit").isFalse();
        assertThat(isValidAttributeName("alpha:77")).as("Colon ok").isTrue();
        assertThat(isValidAttributeName("alph<a77")).as("Greater than not ok").isFalse();
        assertThat(isValidAttributeName("alph<a77")).as("Less than not ok").isFalse();
        assertThat(isValidAttributeName("alph'a77")).as("Quote not ok").isFalse();
    }
    
    private boolean isValidAttributeName(String name) throws Exception {
        boolean result = true;
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startElement("alpha");
            writer.attribute(name, "");
        } catch (InvalidXmlException e) {
            result = false;
        }
        return result;
    }
    
    @Test
    void escapeAttributeContent() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startElement("alpha").attribute("one", "this&that").attribute("two", "small<large")
                    .attribute("three", "12>1").attribute("four", "'quote'").attribute("five", "\"quote\"");
            assertThat(out.toString()).isEqualTo("<alpha one='this&amp;that' two='small&lt;large' three='12&gt;1' four='&apos;quote&apos;' five='&quot;quote&quot;'");
        }
    }
    
    @Test
    void attributeInContent() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startElement("alpha").content("foo bar");
            assertThatThrownBy(() -> writer.attribute("name", "value"))
                    .hasMessageContaining("Attributes can only be written in elements")
                            .isInstanceOf(InvalidXmlException.class);
        }
    }
  
    @Test
    void outOfRangeCharacter() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            CharSequence cs = new CharSequence() {
                @Override
                public int length() {
                    return 1;
                }

                @Override
                public char charAt(int index) {
                    return Character.highSurrogate(0x110000);
                }

                @Override
                public CharSequence subSequence(int start, int end) {
                    return null;
                }
            };
            writer.startElement("alpha").content(cs);
            assertThat(out.toString()).isEqualTo("<alpha>\\uDC00");
        }
    }
    
    @Test
    void attributeAfterElementClosed() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startElement("alpha").startElement("beta").closeElement();
            assertThat(out.toString()).isEqualTo("<alpha><beta/>");
            assertThatThrownBy(() -> writer.attribute("name", "value"))
                    .hasMessageContaining("Attributes can only be written in elements")
                            .isInstanceOf(InvalidXmlException.class);
        }
    }
    
    @Test
    void closeDocumentBeforeOpen() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            assertThatThrownBy(writer::closeDocument)
                    .hasMessageContaining("Close called before an element has been opened.")
                    .isInstanceOf(OperationNotAllowedException.class);
        } catch (OperationNotAllowedException expected) {
            assertThat(expected).hasMessageContaining("Close called before an element has been opened.");
        }

    }
    
    @Test
    void closeDocumentAfterRootElementClosed() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startElement("alpha").closeElement().closeDocument();
        }
    }   
    
    @Test
    void closeSimpleDocument() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startElement("alpha").startElement("beta").closeDocument();
            assertThat(out.toString()).isEqualTo("<alpha><beta/></alpha>");
        }
    }
    
    @Test
    void closeComplexDocument() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startElement("alpha").startElement("beta").attribute("name", "value").closeElement()
                    .startElement("beta")
            .attribute("name", "value")
                    .startElement("gamma").closeDocument();
            assertThat(out.toString()).isEqualTo("<alpha><beta name='value'/><beta name='value'><gamma/></beta></alpha>");
        }
    }
    
    @Test
    void writeProlog() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startDocument();
            assertThat(out.toString()).isEqualTo("<?xml version='1.0'?>");
        } catch (OperationNotAllowedException expected) {
            assertThat(expected).hasMessageContaining("Close called before an element has been opened.");
        }
    }
    
    @Test
    void writeAfterElement() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startElement("alpha");
            assertThatThrownBy(writer::startDocument).hasMessageContaining("Document already started")
                    .isInstanceOf(OperationNotAllowedException.class);
        }
    }
    
    @Test
    void writePrologTwice() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startDocument();
            assertThat(out.toString()).isEqualTo("<?xml version='1.0'?>");
            assertThatThrownBy(writer::startDocument).hasMessageContaining("Only one prolog allowed")
                    .isInstanceOf(OperationNotAllowedException.class);
        } catch (OperationNotAllowedException expected) {
            assertThat(expected).hasMessageContaining("Close called before an element has been opened.");
        }
    }
    
    @Test
    void duplicateAttributes() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startElement("alpha").attribute("one", "1").startElement("beta").attribute("one", "1");
            assertThat(out.toString()).isEqualTo("<alpha one='1'><beta one='1'");
            assertThatThrownBy(() -> writer.attribute("one", "2"))
                    .hasMessageContaining("Each attribute can only be written once")
                    .isInstanceOf(InvalidXmlException.class);
        }
    }

    @Test
    void writeCDataBeforeElement() throws IOException {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startDocument();
            assertThatThrownBy(() -> writer.cdata("Just cdata"))
                    .hasMessageContaining("An element must be opened before content can be written.")
                    .isInstanceOf(OperationNotAllowedException.class);
        } catch (OperationNotAllowedException expected) {
            assertThat(expected).hasMessageContaining("Close called before an element has been opened.");
        }
    }

    @Test
    void writeCData() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startDocument().startElement("test").cdata("Just cdata").closeDocument();
            assertThat(out.toString()).isEqualTo("<?xml version='1.0'?><test><![CDATA[ Just cdata ]]></test>");
        }
    }

    @Test
    void writeCDataEmbeddedCData() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startDocument().startElement("test").cdata("Some <![CDATA[ cdata ]]> text").closeDocument();
            assertThat(out.toString()).isEqualTo("<?xml version='1.0'?><test><![CDATA[ Some \\u3C![CDATA[ cdata {rat:CDATA close} text ]]></test>");
        }
    }

    @Test
    void closeElementBeforeOpened() throws IOException {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            XmlWriter underTest = writer.startDocument().startElement("test");
            assertThatThrownBy(() -> underTest.closeElement("missing"))
                            .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Test
    void closeElement() throws Exception {
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startDocument().startElement("root").startElement("hello").startElement("world").content("hello world").closeElement("hello").startElement("test").closeDocument();
            assertThat(out.toString()).isEqualTo("<?xml version='1.0'?><root><hello><world>hello world</world></hello><test/></root>");
        }
    }

    @Test
    void append() throws Exception {
        // ensure proper line endings
        String expected = String.format("<?xml version='1.0'?><base>%n" +
                "<root>%n" +
                "    <hello>%n" +
                "        <world>hello world</world>%n" +
                "    </hello>%n" +
                "    <test/>%n" +
                "</root>%n" +
                "</base>");
        byte[] rawDocument = "<?xml version='1.0'?><root><hello><world>hello world</world></hello><test/></root>".getBytes(StandardCharsets.UTF_8);
        Document document = XmlUtils.toDom(new ByteArrayInputStream(rawDocument));
        StringWriter out = new StringWriter();
        try (XmlWriter writer = new XmlWriter(out)) {
            writer.startDocument().startElement("base").append(document).closeDocument();
        }
        assertThat(out.toString()).isEqualTo(expected);
    }
}
