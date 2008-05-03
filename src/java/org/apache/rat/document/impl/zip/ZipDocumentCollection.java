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
package org.apache.rat.document.impl.zip;

import java.util.Collection;
import java.util.Iterator;

import rat.document.IDocumentCollection;

abstract class ZipDocumentCollection  implements IDocumentCollection {

    private final Collection subdirectories;
    private final Collection documents;

    public ZipDocumentCollection(final Collection subdirectorires, final Collection documents) {
        super();
        this.subdirectories = subdirectorires;
        this.documents = documents;
    }

    public Iterator documentIterator() {
        return documents.iterator();
    }

    public Iterator subcollectionIterator() {
        return subdirectories.iterator();
    }

    void add(ZipDirectory directory) {
        subdirectories.add(directory);
    }
    
    public String toString() {
        final StringBuffer buffer = new StringBuffer("[ZipDocumentCollection '"); 
        buffer.append(getName());
        buffer.append("' documents: ");
        buffer.append(documents);
        buffer.append(", directories: ");
        buffer.append(subdirectories);
        buffer.append("]");
        return buffer.toString();
    }
}