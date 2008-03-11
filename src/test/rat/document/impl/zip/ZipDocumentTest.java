/*
 * Copyright 2006 Robert Burrell Donkin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package rat.document.impl.zip;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.TestCase;

public class ZipDocumentTest extends TestCase {

    private static final String NAME = "Text.txt";
    ZipDocument document;
    
    protected void setUp() throws Exception {
        super.setUp();
        ZipFile zip = new ZipFile(new File("src/test/elements/dummy.jar"));
        ZipEntry entry = zip.getEntry(NAME);
        assertNotNull(entry);
        document = new ZipDocument(entry, zip);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testReader() throws Exception {
        Reader reader = document.reader();
        assertNotNull("Reader for entry returned", reader);
        BufferedReader bufferedReader = new BufferedReader(reader);
        assertEquals("Expected content", "/*", bufferedReader.readLine().trim());
        assertEquals("Expected content", "*     Copyright 2006 Robert Burrell Donkin", bufferedReader.readLine().trim());
        bufferedReader.close();
    }

    public void testGetName() {
        assertEquals(NAME, document.getName());
    }

    public void testGetURL() {
        assertEquals("zip:" + NAME, document.getURL());
    }

}
