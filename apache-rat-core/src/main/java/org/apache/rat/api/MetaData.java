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


/**
 * Data about the subject.
 */
public class MetaData {

    private ContentType contentType;
    
    public MetaData() {
        this(null);
    }
    
    public MetaData(final ContentType contentType) {
        this.contentType = contentType;
    }
    
    /**
     * Gets the content type for the subject.
     * @return or null when the type is unknown
     */
    public ContentType getContentType() {
        return contentType;
    }
    
    /**
     * Sets the content type for this subject.
     * @param contentType <code>ContentType</code>,
     * or null when the content type is unknown
     */
    public void setContentType(final ContentType contentType) {
        this.contentType = contentType;
    }
}
