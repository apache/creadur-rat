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
package org.apache.rat.report.xml.writer;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

/**
 * <p>
 * Lightweight {@link IXmlWriter} implementation.
 * </p>
 * <p>
 * Requires a wrapper to be used safely in a multithreaded environment.
 * </p>
 * <p>
 * Not intended to be subclassed. Please copy and hack!
 * </p>
 */
@SuppressWarnings({"checkstyle:MagicNumber", "checkstyle:JavadocVariable"})
public final class XmlWriter implements IXmlWriter {

    private final Appendable appendable;
    private final ArrayDeque<CharSequence> elementNames;
    private final Set<CharSequence> currentAttributes = new HashSet<>();

    private boolean elementsWritten;
    private boolean inElement;
    private boolean prologWritten;

    /**
     * Constructs an XmlWriter with the specified writer for output.
     * @param writer the writer to write to.
     */
    public XmlWriter(final Appendable writer) {
        this.appendable = writer;
        this.elementNames = new ArrayDeque<>();
    }

    private void validateRootOpen() throws IOException {
        if (elementsWritten && elementNames.isEmpty()) {
            throw new OperationNotAllowedException("Root element already closed. Cannot open new element.");
        }
    }

    private void maybeCloseElement() throws IOException {
        if (inElement) {
            appendable.append('>');
            inElement = false;
        }
    }

    /**
     * Starts a document by writing a prolog. Calling this method is optional. When
     * writing a document fragment, it should <em>not</em> be called.
     *
     * @return this object
     * @throws OperationNotAllowedException if called after the first element has
     * been written or once a prolog has already been written
     */
    @Override
    public IXmlWriter startDocument() throws IOException {
        if (elementsWritten) {
            throw new OperationNotAllowedException("Document already started");
        }
        if (prologWritten) {
            throw new OperationNotAllowedException("Only one prolog allowed");
        }
        appendable.append("<?xml version='1.0'?>");
        prologWritten = true;
        return this;
    }

    /**
     * Writes the start of an element.
     *
     * @param elementName the name of the element, not null
     * @return this object
     * @throws InvalidXmlException if the name is not valid for an xml element
     * @throws OperationNotAllowedException if called after the first element has
     * been closed
     */
    @Override
    public IXmlWriter openElement(final CharSequence elementName) throws IOException {
        validateRootOpen();
        if (!XMLChar.isValidName(elementName.toString())) {
            throw new InvalidXmlException("'" + elementName + "' is not a valid element name");
        }
        elementsWritten = true;
        maybeCloseElement();
        appendable.append('<');
        appendable.append(elementName);
        inElement = true;
        elementNames.push(elementName);
        currentAttributes.clear();
        return this;
    }

    @Override
    public IXmlWriter comment(final CharSequence text) throws IOException {
        maybeCloseElement();
        appendable.append("<!-- ");
        writeEscaped(text, false);
         appendable.append(" -->");
         return this;
    }

    /**
     * Writes an attribute of an element. Note that this is only allowed directly
     * after {@link #openElement(CharSequence)} or a previous {@code attribute} call.
     *
     * @param name the attribute name, not null
     * @param value the attribute value, not null
     * @return this object
     * @throws InvalidXmlException if the name is not valid for an xml attribute or
     * if a value for the attribute has already been written
     * @throws OperationNotAllowedException if called after
     * {@link #content(CharSequence)} or {@link #closeElement()} or before any call
     * to {@link #openElement(CharSequence)}
     */
    @Override
    public IXmlWriter attribute(final CharSequence name, final CharSequence value) throws IOException {
        if (elementNames.isEmpty()) {
            validateRootOpen();
            throw new OperationNotAllowedException("Close called before an element has been opened.");
        }
        if (!XMLChar.isValidName(name.toString())) {
            throw new InvalidXmlException("'" + name + "' is not a valid attribute name.");
        }
        if (!inElement) {
            throw new InvalidXmlException("Attributes can only be written in elements");
        }
        if (currentAttributes.contains(name)) {
            throw new InvalidXmlException("Each attribute can only be written once");
        }
        appendable.append(' ');
        appendable.append(name);
        appendable.append("='");
        writeAttributeContent(value);
        appendable.append("'");
        currentAttributes.add(name);
        return this;
    }

    private void writeAttributeContent(final CharSequence content) throws IOException {
        writeEscaped(content, true);
    }

    private void prepareForData() throws IOException {
        if (elementNames.isEmpty()) {
            validateRootOpen();
            throw new OperationNotAllowedException("An element must be opened before content can be written.");
        }
        maybeCloseElement();
    }

    @Override
    public IXmlWriter content(final CharSequence content) throws IOException {
        prepareForData();
        writeEscaped(content, false);
        return this;
    }

    @Override
    public IXmlWriter cdata(final CharSequence content) throws IOException {
        prepareForData();
        StringBuilder sb = new StringBuilder(content);
        int found;
        while (-1 != (found = sb.indexOf("]]>"))) {
            sb.replace(found, found + 3, "{rat:CDATA close}");
        }

        appendable.append("<![CDATA[ ");
        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if (!XMLChar.isContent(c)) {
                appendable.append(String.format("\\u%X", (int) c));
            } else {
                appendable.append(c);
            }
        }
        appendable.append(" ]]>");

        inElement = false;
        return this;
    }

    private void writeEscaped(final CharSequence content, final boolean isAttributeContent) throws IOException {
        final int length = content.length();
        for (int i = 0; i < length; i++) {
            char character = content.charAt(i);
            if (character == '&') {
                appendable.append("&amp;");
            } else if (character == '<') {
                appendable.append("&lt;");
            } else if (character == '>') {
                appendable.append("&gt;");
            } else if (isAttributeContent && character == '\'') {
                appendable.append("&apos;");
            } else if (isAttributeContent && character == '\"') {
                appendable.append("&quot;");
            } else if (!(XMLChar.isContent(character) || XMLChar.isSpace(character))) {
                appendable.append(String.format("\\u%X", (int) character));
            } else {
                appendable.append(character);
            }
        }
    }

    /**
     * Closes the last element written.
     *
     * @return this object
     * @throws OperationNotAllowedException if called before any call to
     * {@link #openElement} or after the first element has been closed
     */
    @Override
    public IXmlWriter closeElement() throws IOException {
        if (elementNames.isEmpty()) {
            validateRootOpen();
            throw new OperationNotAllowedException("Close called before an element has been opened.");
        }
        final CharSequence elementName = elementNames.pop();
        if (inElement) {
            appendable.append("/>");
        } else {
            appendable.append("</");
            appendable.append(elementName);
            appendable.append('>');
        }
        inElement = false;
        return this;
    }

    /**
     * Closes back to and including the last instance of the specified element name.
     * @param name The name of the element to close.  Must not be {@code null}.
     * @return this object
     * @throws OperationNotAllowedException if called before any call to
     * {@link #openElement} or after the first element has been closed
     */
    @Override
    public IXmlWriter closeElement(final CharSequence name) throws IOException {
        Objects.requireNonNull(name);
        if (elementNames.isEmpty()) {
            validateRootOpen();
            throw new OperationNotAllowedException("Close called before an element has been opened.");
        }
        CharSequence elementName = null;
        while (!name.equals(elementName)) {
            elementName = elementNames.pop();
            if (inElement) {
                appendable.append("/>");
            } else {
                appendable.append("</");
                appendable.append(elementName);
                appendable.append('>');
            }
            inElement = false;
        }
        return this;
    }

    /**
     * Closes all pending elements. When appropriate, resources are also flushed and
     * closed. No exception is raised when called upon a document whose root element
     * has already been closed.
     *
     * @return this object
     * @throws OperationNotAllowedException if called before any call to
     * {@link #openElement}
     */
    @Override
    public IXmlWriter closeDocument() throws IOException {
        if (elementNames.isEmpty() && !elementsWritten) {
            throw new OperationNotAllowedException("Close called before an element has been opened.");
        }
        while (!elementNames.isEmpty()) {
            closeElement();
        }
        return this;
    }

    @Override
    public void close() throws IOException {
        closeDocument();
        if (appendable instanceof Closeable) {
            ((Closeable) appendable).close();
        }
    }

    public IXmlWriter append(final Document document) throws IOException {
        validateRootOpen();
        elementsWritten = true;
        maybeCloseElement();
        appendable.append(System.lineSeparator());
        currentAttributes.clear();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(document),
                    new StreamResult(baos));
            appendable.append(baos.toString());
        } catch (TransformerException e) {
            throw new IOException(e);
        }
        return this;
    }
}
