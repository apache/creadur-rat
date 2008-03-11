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
package rat.document;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

import org.apache.commons.collections.CollectionUtils;

import rat.document.impl.zip.ZipDocumentFactory;

public class DocumentUtilsTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDocumentsContained() throws Exception {
        IDocumentCollection documents 
        = ZipDocumentFactory.load(new File("src/test/elements/dummy.jar"));
        Collection contents = DocumentUtils.documentsContained(documents);
        assertEquals("8 documents in jar", 8, contents.size());
        CollectionUtils.transform(contents, DocumentUtils.toNameTransformer());
        String[] names = {"Image.png", "LICENSE", "NOTICE", "Source.java", "Text.txt", "Xml.xml", 
                "MANIFEST.MF", "Empty.txt"};
        assertTrue(CollectionUtils.isEqualCollection(Arrays.asList(names), contents));
    }
}
