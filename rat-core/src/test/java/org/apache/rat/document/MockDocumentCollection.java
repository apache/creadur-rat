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
package org.apache.rat.document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class MockDocumentCollection implements IDocumentCollection {

    public Collection documents;
    public Collection subdirectories;
    public String name;
    public String url;

    public MockDocumentCollection() {
        this(new ArrayList(), new ArrayList(), "name", "url");
    }
    
    public MockDocumentCollection(Collection documents, Collection subdirectories, String name, String url) {
        super();
        this.documents = documents;
        this.subdirectories = subdirectories;
        this.name = name;
        this.url = url;
    }

    public Iterator documentIterator() {
        return documents.iterator();
    }

    public Iterator subcollectionIterator() {
        return subdirectories.iterator();
    }

    public String getName() {
        return name;
    }

    public String getURL() {
        return url;
    }
}
