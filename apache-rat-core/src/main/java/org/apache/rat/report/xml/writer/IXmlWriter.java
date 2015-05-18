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

import java.io.IOException;

/**
 * Simple interface for creating basic xml documents.
 * Performs basic validation and escaping.
 * Not namespace aware (may reconsider this later).
 */
public interface IXmlWriter {

    /**
     * Starts a document by writing a prolog.
     * Calling this method is optional.
     * When writing a document fragment, it should <em>not</em> be called.
     * @return this object
     * @throws OperationNotAllowedException 
     * if called after the first element has been written
     * or once a prolog has already been written
     */
    IXmlWriter startDocument() throws IOException;
    
    /**
     * Writes the start of an element.
     * 
     * @param elementName the name of the element, not null
     * @return this object 
     * @throws InvalidXmlException if the name is not valid for an xml element
     * @throws OperationNotAllowedException 
     * if called after the first element has been closed
     */
    IXmlWriter openElement(CharSequence elementName) throws IOException;
    
    /**
     * Writes an attribute of an element.
     * Note that this is only allowed directly after {@link #openElement(CharSequence)}
     * or {@link #attribute}.
     * 
     * @param name the attribute name, not null
     * @param value the attribute value, not null
     * @return this object
     * @throws InvalidXmlException if the name is not valid for an xml attribute 
     * or if a value for the attribute has already been written
     * @throws OperationNotAllowedException if called after {@link #content(CharSequence)} 
     * or {@link #closeElement()} or before any call to {@link #openElement(CharSequence)}
     */
    IXmlWriter attribute(CharSequence name, CharSequence value) throws IOException;
    
    /**
     * Writes content.
     * Calling this method will automatically 
     * Note that this method does not use CDATA.
     * 
     * @param content the content to write
     * @return this object
     * @throws OperationNotAllowedException 
     * if called before any call to {@link #openElement} 
     * or after the first element has been closed
     */
    IXmlWriter content(CharSequence content) throws IOException;
    
    /**
     * Closes the last element written.
     * 
     * @return this object
     * @throws OperationNotAllowedException 
     * if called before any call to {@link #openElement} 
     * or after the first element has been closed
     */
    IXmlWriter closeElement() throws IOException;
    
    /**
     * Closes all pending elements.
     * When appropriate, resources are also flushed and closed.
     * No exception is raised when called upon a document whose
     * root element has already been closed.
     * 
     * @return this object
     * @throws OperationNotAllowedException 
     * if called before any call to {@link #openElement} 
     */
    IXmlWriter closeDocument() throws IOException;
}
