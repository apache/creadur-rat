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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.zip.ZipEntry;

import junit.framework.TestCase;
import org.apache.rat.document.MockDocument;
import org.apache.rat.document.MockDocumentCollection;

public class ZipDirectoryTest extends TestCase {
    
    private static final String NAME = "directory";
    ZipEntry entry;
    ZipDirectory directory;
    MockDocument documentOne;
    MockDocument documentTwo;
    MockDocumentCollection subcollectionOne;
    MockDocumentCollection subcollectionTwo;
    Collection documents;
    Collection subdirectories;
    
    protected void setUp() throws Exception {
        super.setUp();
        entry = new ZipEntry(NAME);
        documents = new ArrayList();
        documentOne = new MockDocument();
        documentTwo = new MockDocument();
        documents.add(documentOne);
        documents.add(documentTwo);
        subdirectories = new ArrayList();
        subcollectionOne = new MockDocumentCollection();
        subcollectionTwo = new MockDocumentCollection();
        subdirectories.add(subcollectionOne);
        subdirectories.add(subcollectionTwo);
        directory = new ZipDirectory(entry, subdirectories, documents);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetName() {
        assertEquals(NAME, directory.getName());
    }

    public void testGetURL() {
        assertEquals("zip:" + NAME, directory.getURL());
    }

}
