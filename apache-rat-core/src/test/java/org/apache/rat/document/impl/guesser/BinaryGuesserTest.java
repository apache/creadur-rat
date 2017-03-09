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

import org.apache.commons.io.IOUtils;
import org.apache.rat.document.MockDocument;
import org.apache.rat.document.impl.FileDocument;
import org.apache.rat.test.utils.Resources;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BinaryGuesserTest {

    private static final List<String> BINARY_FILES = Arrays.asList(//
            "image.png",//
            "image.pdf",//
            "image.psd",//
            "image.gif",//
            "image.giff",//
            "image.jpg",//
            "image.jpeg",//
            "image.exe",//
            "Whatever.class",//
            "data.dat",//
            "libicuda.so.34",//
            "my.truststore",//
            //"foo.Java", //
            //"manifest.Mf",//
            "deprecatedtechnology.swf",
            "xyz.aif",
            "abc.iff",
            // Audio Files
            "test.m3u", "test.m4a",
            "test-audio.mid", "test-audio.mp3",
            "test-audio.mpa", "test-audio.wav",
            "test-audio.wma"
    );

    @Test
    public void testMatches() {
        for (String name : BINARY_FILES) {
            assertTrue("'" + name + "' should be detected as a binary", BinaryGuesser.isBinary(new MockDocument(name)));
        }

    }

    @Test
    public void testIsBinary() {
        for (String name : BINARY_FILES) {
            assertTrue("'" + name + "' should be detected as a binary", BinaryGuesser.isBinary(name));
        }
    }

    /**
     * Used to swallow a MalformedInputException and return false
     * because the encoding of the stream was different from the
     * platform's default encoding.
     *
     * @throws Exception
     * @see "RAT-81"
     */
    @Test
    public void binaryWithMalformedInputRAT81() throws Exception {
        FileDocument doc = new FileDocument(Resources.getResourceFile("/binaries/UTF16_with_signature.xml"));
        Reader r = doc.reader(); // this will fail test if file is not readable
        try {
            char[] dummy = new char[100];
            r.read(dummy);
            // if we get here, the UTF-16 encoded file didn't throw
            // any exception, try the UTF-8 encoded one
            r.close();
            r = null; // ensure we detect failure to read second file
            doc = new FileDocument(Resources.getResourceFile("/binaries/UTF8_with_signature.xml"));
            r = doc.reader();
            r.read(dummy);
            // still here?  can't test on this platform
            System.err.println("Skipping testBinaryWithMalformedInput");
        } catch (IOException e) {
            if (r != null) {
                IOUtils.closeQuietly(r);
            } else {
                throw e; // could not open the second file
            }
            r = null;
            assertTrue("Expected binary for " + doc.getName(), BinaryGuesser.isBinary(doc));
        } finally {
            IOUtils.closeQuietly(r);
        }
    }

    @Test
    public void realBinaryContent() throws IOException {
        // This test is not accurate on all platforms
        final String encoding = System.getProperty("file.encoding");
        final boolean isBinary = BinaryGuesser.isBinary(new FileDocument(Resources.getResourceFile("/binaries/Image-png.not")));
        if (encoding.startsWith("ANSI")) {
            assertTrue(isBinary);
        } else {
            if (isBinary) {
                System.out.println("BinaryGuesserTest.realBinaryContent() succeeded when using encoding " + encoding);
            } else {
                System.err.println("BinaryGuesserTest.realBinaryContent() failed when using encoding " + encoding);
            }
        }
    }

    @Test
    public void textualContent() throws IOException {
        assertFalse(BinaryGuesser.isBinary(new FileDocument(Resources.getResourceFile("/elements/Text.txt"))));
    }

    @Test
    public void emptyFile() throws IOException {
        assertFalse(BinaryGuesser.isBinary(new FileDocument(Resources.getResourceFile("/elements/sub/Empty.txt"))));
    }

    @Test
    public void testFileEncodingCanBeSetAndHasFallbackInCaseOfErrors() {
        System.setProperty(BinaryGuesser.FILE_ENCODING, "shouldThrowAnExceptionBecauseNotFound");
        assertEquals("UTF-8", BinaryGuesser.getFileEncodingOrUTF8AsFallback().displayName());

        final String usAscii = "US-ASCII";
        System.setProperty(BinaryGuesser.FILE_ENCODING, usAscii);
        assertEquals(usAscii, BinaryGuesser.getFileEncodingOrUTF8AsFallback().displayName());
    }
}
