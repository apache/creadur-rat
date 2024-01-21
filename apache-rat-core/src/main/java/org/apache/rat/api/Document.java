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
package org.apache.rat.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.rat.document.CompositeDocumentException;

public interface Document {

    /**
     * @return the name of the current document.
     */
	String getName();
    
    /**
     * Reads the contents of this document.
     * @return <code>Reader</code> not null
     * @throws IOException if this document cannot be read
     * @throws CompositeDocumentException if this document can only be read as
     * a composite archive
     */
    Reader reader() throws IOException;
    
    /**
     * Streams the document's contents.
     * @return not null
     * @throws IOException when stream could not be opened
     */
    InputStream inputStream() throws IOException;

    /**
     * Gets data describing this resource.
     * @return not null
     */
    MetaData getMetaData();
    
    /**
     * Is this a composite document?
     * @return true if composite, false otherwise
     */
    boolean isComposite();
}
