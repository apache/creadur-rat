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
package org.apache.rat.document.impl.guesser;

import org.apache.rat.document.MockDocument;
import org.apache.rat.document.impl.FileDocument;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BinaryGuesserTest {

    @Test
    public void testMatches() {
        assertTrue(BinaryGuesser.isBinary(new MockDocument("image.png")));
        assertTrue(BinaryGuesser.isBinary(new MockDocument("image.pdf")));
        assertTrue(BinaryGuesser.isBinary(new MockDocument("image.gif")));
        assertTrue(BinaryGuesser.isBinary(new MockDocument("image.giff")));
        assertTrue(BinaryGuesser.isBinary(new MockDocument("image.tif")));
        assertTrue(BinaryGuesser.isBinary(new MockDocument("image.tiff")));
        assertTrue(BinaryGuesser.isBinary(new MockDocument("image.jpg")));
        assertTrue(BinaryGuesser.isBinary(new MockDocument("image.jpeg")));
        assertTrue(BinaryGuesser.isBinary(new MockDocument("image.exe")));
        assertTrue(BinaryGuesser.isBinary(new MockDocument("Whatever.class")));
        assertTrue(BinaryGuesser.isBinary(new MockDocument("data.dat")));
        assertTrue(BinaryGuesser.isBinary(new MockDocument("libicudata.so.34.")));
    }

    public void testIsBinary() {
        assertTrue(BinaryGuesser.isBinary("image.png"));
        assertTrue(BinaryGuesser.isBinary("image.pdf"));
        assertTrue(BinaryGuesser.isBinary("image.gif"));
        assertTrue(BinaryGuesser.isBinary("image.giff"));
        assertTrue(BinaryGuesser.isBinary("image.tif"));
        assertTrue(BinaryGuesser.isBinary("image.tiff"));
        assertTrue(BinaryGuesser.isBinary("image.jpg"));
        assertTrue(BinaryGuesser.isBinary("image.jpeg"));
        assertTrue(BinaryGuesser.isBinary("image.exe"));
        assertTrue(BinaryGuesser.isBinary("Whatever.class"));
        assertTrue(BinaryGuesser.isBinary("data.dat"));
        assertTrue(BinaryGuesser.isBinary("libicudata.so.34."));
    }

    /**
     * Used to swallow a MalformedInputException and return false
     * because the encoding of the stream was different from the
     * platform's default encoding.
     *
     * @see "RAT-81"
     */
    @Test
    public void binaryWithMalformedInputRAT81() throws Throwable {
        FileDocument doc = new FileDocument(new File("src/test/resources/binaries/UTF16_with_signature.xml"));
        Reader r = null;
        try {
            char[] dummy = new char[100];
            r = doc.reader();
            r.read(dummy);
            // if we get here, the UTF-16 encoded file didn't throw
            // any exception, try the UTF-8 encoded one
            r.close();
            doc = new FileDocument(new File("src/test/resources/binaries/UTF8_with_signature.xml"));
            r = doc.reader();
            r.read(dummy);
            // still here?  can't test on this platform
            System.err.println("Skipping testBinaryWithMalformedInput");
        } catch (IOException e) {
            if (r!= null) {
                r.close();
            }
            r = null;
            assertTrue(BinaryGuesser.isBinary(doc));
        } finally {
            if (r != null) {
                r.close();
            }
        }
    }

    @Test
    public void realBinaryContent() {
        // This test is not accurate on all platforms
        if (System.getProperty("file.encoding").startsWith("ANSI")) {
            assertTrue(BinaryGuesser.isBinary(new FileDocument(new File("src/test/resources/binaries/Image-png.not"))));
        }
    }

    @Test
    public void textualContent() {
        assertFalse(BinaryGuesser.isBinary(new FileDocument(new File("src/test/resources/elements/Text.txt"))));
    }

    @Test
    public void emptyFile() {
        assertFalse(BinaryGuesser.isBinary(new FileDocument(new File("src/test/resources/elements/sub/Empty.txt"))));
    }
}
