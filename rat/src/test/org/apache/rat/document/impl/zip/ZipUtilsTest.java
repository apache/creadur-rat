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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.TestCase;

public class ZipUtilsTest extends TestCase {

    private static final String NAME = "Text.txt";
    private static final String SUBDIRECTORY = "sub";
    private static final String SUBDIRECTORY_ELEMENT = "Empty.txt";
    
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSimpleGetName() throws Exception {
        ZipFile zip = new ZipFile(new File("src/test/elements/dummy.jar"));
        ZipEntry entry = zip.getEntry(NAME);
        assertNotNull(entry);
        assertEquals(NAME, ZipUtils.getName(entry));
    }
    
    public void testGetSubDirectoryName() throws Exception {
        ZipFile zip = new ZipFile(new File("src/test/elements/dummy.jar"));
        ZipEntry entry = zip.getEntry(SUBDIRECTORY);
        assertNotNull(entry);
        assertEquals(SUBDIRECTORY, ZipUtils.getName(entry));
    }
    
    public void testGetSubDirectoryNameWithSlash() throws Exception {
        ZipFile zip = new ZipFile(new File("src/test/elements/dummy.jar"));
        ZipEntry entry = zip.getEntry(SUBDIRECTORY + "/");
        assertNotNull(entry);
        assertEquals(SUBDIRECTORY, ZipUtils.getName(entry));
    }
    
    public void testGetSubDirectoryDocumentName() throws Exception {
        ZipFile zip = new ZipFile(new File("src/test/elements/dummy.jar"));
        ZipEntry entry = zip.getEntry(SUBDIRECTORY + "/" + SUBDIRECTORY_ELEMENT);
        assertNotNull(entry);
        assertEquals(SUBDIRECTORY_ELEMENT, ZipUtils.getName(entry));
    }
    
    public void testSimpleGetStem() throws Exception {
        ZipFile zip = new ZipFile(new File("src/test/elements/dummy.jar"));
        ZipEntry entry = zip.getEntry(NAME);
        assertNotNull(entry);
        assertEquals("", ZipUtils.getStem(entry));
    }
     
    public void testGetSubDirectoryStem() throws Exception {
        ZipFile zip = new ZipFile(new File("src/test/elements/dummy.jar"));
        ZipEntry entry = zip.getEntry(SUBDIRECTORY);
        assertNotNull(entry);
        assertEquals("", ZipUtils.getStem(entry));
    }
    
    public void testGetSubDirectoryDocumentStem() throws Exception {
        ZipFile zip = new ZipFile(new File("src/test/elements/dummy.jar"));
        ZipEntry entry = zip.getEntry(SUBDIRECTORY + "/" + SUBDIRECTORY_ELEMENT);
        assertNotNull(entry);
        assertEquals(SUBDIRECTORY, ZipUtils.getStem(entry));
    }
    
    public void testIsTopLevelDirectory() throws Exception {
        String[] topLevelNames = {"Image.png", "LICENSE", "NOTICE", "Source.java", 
                "Text.txt", "Xml.xml", "META-INF", "META-INF/", "META-INF\\", "sub", "sub/", "sub\\"};
        Collection topLevel = Arrays.asList(topLevelNames);
        ZipFile zip = new ZipFile(new File("src/test/elements/dummy.jar"));
        for (Enumeration en=zip.entries();en.hasMoreElements();) {
            ZipEntry entry = (ZipEntry) en.nextElement();
            assertEquals("Top level element", topLevel.contains(entry.getName()), 
                    ZipUtils.isTopLevel(entry));
        }
    }
}
