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
package org.apache.rat.report.xml;

import org.apache.rat.report.xml.writer.IXmlWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MockXmlWriter implements IXmlWriter {

    public final List<Object> calls = new ArrayList<>();
    
    public IXmlWriter attribute(CharSequence name, CharSequence value)
            throws IOException {
        calls.add(new Attribute(name, value));
        return this;
    }

    public IXmlWriter closeDocument() throws IOException {
        calls.add(new CloseDocument());
        return this;
    }

    public IXmlWriter closeElement() throws IOException {
        calls.add(new CloseElement());
        return this;
    }

    public IXmlWriter content(CharSequence content) throws IOException {
        calls.add(new Content(content));
        return this;
    }

    public IXmlWriter openElement(CharSequence elementName) throws IOException {
        calls.add(new OpenElement(elementName));
        return this;
    }

    public IXmlWriter startDocument() throws IOException {
        calls.add(new StartDocument());
        return this;
    }
    
    public boolean isCloseElement(int index) {
        boolean result = false;
        final Object call = calls.get(index);
        result = call instanceof CloseElement;
        return result;
    }
    
    public boolean isContent(String content, int index) {
        boolean result = false;
        final Object call = calls.get(index);
        if (call instanceof Content) {
            Content contentCall = (Content) call;
            result = content.contentEquals(contentCall.content);
        }
        return result;
    }
    
    public boolean isOpenElement(String name, int index) {
        boolean result = false;
        final Object call = calls.get(index);
        if (call instanceof OpenElement) {
            OpenElement openElement = (OpenElement) call;
            result = name.contentEquals(openElement.elementName);
        }
        return result;
    }

    public boolean isAttribute(String name, String value, int index) {
        boolean result = false;
        final Object call = calls.get(index);
        if (call instanceof Attribute) {
            Attribute attribute = (Attribute) call;
            result = name.contentEquals(attribute.name) && value.contentEquals(attribute.value);
        }
        return result;
    }
    
    public class StartDocument {}
    public class CloseDocument {}
    public class CloseElement {}
    public class OpenElement {
        public final CharSequence elementName;
        private OpenElement(CharSequence elementName) {
            this.elementName = elementName;
        }
    }
    public class Content {
        public final CharSequence content;
        private Content(CharSequence content) {
            this.content = content;
        }
    }
    public class Attribute {
        public final CharSequence name;
        public final CharSequence value;
        private Attribute(CharSequence name, CharSequence value) {
            this.name = name;
            this.value = value;
        }
    }
}
