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
package rat.document.impl.zip;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.zip.ZipEntry;

import junit.framework.TestCase;
import rat.document.MockDocument;
import rat.document.MockDocumentCollection;

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

    public void testDocumentIterator() {
        Iterator iterator = directory.documentIterator();
        assertNotNull("Iterator should be not null", iterator);
        assertTrue("Iteration has two elements", iterator.hasNext());
        assertEquals("Iteration returned in order", documentOne, iterator.next());
        assertTrue("Iteration has two elements", iterator.hasNext());
        assertEquals("Iteration returned in order", documentTwo, iterator.next()); 
        assertFalse("Iteration has two elements", iterator.hasNext());
    }

    public void testSubcollectionIterator() {
        Iterator iterator = directory.subcollectionIterator();
        assertNotNull("Iterator should be not null", iterator);
        assertTrue("Iteration has two elements", iterator.hasNext());
        assertEquals("Iteration returned in order", subcollectionOne, iterator.next());
        assertTrue("Iteration has two elements", iterator.hasNext());
        assertEquals("Iteration returned in order", subcollectionTwo, iterator.next()); 
        assertFalse("Iteration has two elements", iterator.hasNext());
    }

    public void testGetName() {
        assertEquals(NAME, directory.getName());
    }

    public void testGetURL() {
        assertEquals("zip:" + NAME, directory.getURL());
    }

}
