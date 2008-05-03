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
package org.apache.rat.test.utils;

import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;

import rat.document.DocumentUtils;
import rat.document.IDocument;
import rat.document.IDocumentCollection;

public abstract class RATCase extends TestCase {

    protected void checkDummyJar(IDocumentCollection collection) {
        assertNotNull(collection);
        Collection documents = IteratorUtils.toList(collection.documentIterator());
        CollectionUtils.transform(documents, DocumentUtils.toNameTransformer());
        assertEquals("Six documents in the jar", 6, documents.size());
        assertTrue("Document names", documents.contains("Image.png"));
        assertTrue("Document names", documents.contains("LICENSE"));
        assertTrue("Document names", documents.contains("NOTICE"));
        assertTrue("Document names", documents.contains("Source.java"));
        assertTrue("Document names", documents.contains("Text.txt"));
        assertTrue("Document names", documents.contains("Xml.xml"));
        Iterator iterator = collection.subcollectionIterator();
        assertTrue("Two subdirectories", iterator.hasNext());
        Object subdirectory = iterator.next();
        assertNotNull("Collection contains document collections", subdirectory);
        assertTrue("Collection contains document collections", subdirectory instanceof IDocumentCollection);
        IDocumentCollection subcollection = (IDocumentCollection) subdirectory;
        checkSubDirectory(collection, subcollection);
        assertTrue("Two subdirectories", iterator.hasNext());
        subdirectory = iterator.next();
        assertNotNull("Collection contains document collections", subdirectory);
        assertTrue("Collection contains document collections", subdirectory instanceof IDocumentCollection);
        subcollection = (IDocumentCollection) subdirectory;
        checkSubDirectory(collection, subcollection);
        assertFalse("Two subdirectories", iterator.hasNext());
    }

    private void checkSubDirectory(IDocumentCollection collection, IDocumentCollection subcollection) {
        final String name = subcollection.getName();
        if ("sub".equals(name)) {
            checkSub(subcollection);
        } else if ("META-INF".equals(name)) {
            checkMetaInf(subcollection);
        } else {
            fail("Unexception subdirectory named " + name);
        }
    }

    private void checkMetaInf(IDocumentCollection subcollection) {
        Iterator documentIterator = subcollection.documentIterator();
        assertNotNull("Document iterator has one element", documentIterator);
        assertTrue("Document iterator has one element", documentIterator.hasNext());
        Object next = documentIterator.next();
        assertNotNull("Document iterator has one element", next);
        assertTrue("Document iterator has one element", next instanceof IDocument);
        IDocument document = (IDocument) next;
        assertEquals("Document named", "MANIFEST.MF", document.getName());
        Iterator subsubcollectionIterator = subcollection.subcollectionIterator();
        assertNotNull("No sub collections", subsubcollectionIterator);
        assertFalse("No sub collections", subsubcollectionIterator.hasNext()); 
    }

    private void checkSub(IDocumentCollection subcollection) {
        Iterator documentIterator = subcollection.documentIterator();
        assertNotNull("Document iterator has one element", documentIterator);
        assertTrue("Document iterator has one element", documentIterator.hasNext());
        Object next = documentIterator.next();
        assertNotNull("Document iterator has one element", next);
        assertTrue("Document iterator has one element", next instanceof IDocument);
        IDocument document = (IDocument) next;
        assertEquals("Document named", "Empty.txt", document.getName());
        Iterator subsubcollectionIterator = subcollection.subcollectionIterator();
        assertNotNull("No sub collections", subsubcollectionIterator);
        assertFalse("No sub collections", subsubcollectionIterator.hasNext());
    }

}
