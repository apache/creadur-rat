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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

import org.apache.rat.api.Document;

/**
 * TODO: factor into MIME guesser and MIME->binary guesser
 */
public class BinaryGuesser {

    private static final String[] DATA_EXTENSIONS = { "DAT", "DOC", "NCB",
            "IDB", "SUO", "XCF", "RAJ", "CERT", "KS", "TS", "ODP", };
    private static final String[] EXE_EXTENSIONS = { "EXE", "DLL", "LIB", "SO",
            "A", "EXP", };
    private static final String[] KEYSTORE_EXTENSIONS = { "JKS", "KEYSTORE",
            "PEM", "CRL" };
    private static final String[] IMAGE_EXTENSIONS = { "PNG", "PDF", "GIF",
            "GIFF", "TIF", "TIFF", "JPG", "JPEG", "ICO", "ICNS", };
    private static final String[] BYTECODE_EXTENSIONS = { "CLASS", "PYD",
            "OBJ", "PYC", };

    private static final String JAR_MANIFEST = "MANIFEST.MF";
    private static final String JAVA = "JAVA";
    private static final int HIGH_BYTES_RATIO = 100;
    private static final int TOTAL_READ_RATIO = 30;
    private static final int NON_ASCII_THREASHOLD = 256;
    private static final int ASCII_CHAR_THREASHOLD = 8;

    public static final boolean isBinary(final Document document) {
        return new BinaryGuesser().matches(document);
    }

    public BinaryGuesser() {
    }

    private boolean matches(final Document document) {
        // TODO: reimplement the binary test algorithm?
        // TODO: more efficient to move into standard analysis
        // TODO: then use binary as default
        return isBinary(document.getName()) ||
        // try a taste
                isBinaryDocument(document);
    }

    private boolean isBinaryDocument(final Document document) {
        boolean result = false;
        InputStream stream = null;
        try {
            stream = document.inputStream();
            result = isBinary(stream);
        } catch (final IOException e) {
            result = false;
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (final IOException e) {
                // SWALLOW
            }
        }
        return result;
    }

    private boolean isBinary(final CharSequence taste) {
        int highBytes = 0;
        final int length = taste.length();
        for (int i = 0; i < length; i++) {
            final char c = taste.charAt(i);
            if (c > BinaryGuesser.NON_ASCII_THREASHOLD
                    || c <= BinaryGuesser.ASCII_CHAR_THREASHOLD) {
                highBytes++;
            }
        }
        return highBytes * BinaryGuesser.HIGH_BYTES_RATIO > length
                * BinaryGuesser.TOTAL_READ_RATIO;
    }

    /**
     * Do the first few bytes of the stream hint at a binary file?
     * 
     * <p>
     * Any IOException is swallowed internally and the test returns false.
     * </p>
     * 
     * <p>
     * This method will try to read bytes from the stream and translate them to
     * characters according to the platform's default encoding. If any bytes can
     * not be translated to characters it will assume the original data must be
     * binary and return true.
     * </p>
     */
    private boolean isBinary(final InputStream in) {
        try {
            final byte[] taste = new byte[200];
            final int bytesRead = in.read(taste);
            if (bytesRead > 0) {
                final ByteBuffer bytes = ByteBuffer.wrap(taste, 0, bytesRead);
                CharBuffer chars = CharBuffer.allocate(2 * bytesRead);
                final Charset cs =
                        Charset.forName(System.getProperty("file.encoding"));
                final CharsetDecoder cd =
                        cs.newDecoder()
                                .onMalformedInput(CodingErrorAction.REPORT)
                                .onUnmappableCharacter(CodingErrorAction.REPORT);
                while (bytes.remaining() > 0) {
                    final CoderResult res = cd.decode(bytes, chars, true);
                    if (res.isMalformed() || res.isUnmappable()) {
                        return true;
                    } else if (res.isOverflow()) {
                        chars.limit(chars.position());
                        chars.rewind();
                        final int c = chars.capacity() * 2;
                        final CharBuffer on = CharBuffer.allocate(c);
                        on.put(chars);
                        chars = on;
                    }
                }
                chars.limit(chars.position());
                chars.rewind();
                return isBinary(chars);
            }
        } catch (final IOException e) {
            // SWALLOW
        }
        return false;
    }

    private boolean isBinaryData(final String name) {
        return extensionMatches(name, DATA_EXTENSIONS);
    }

    private boolean isExecutable(final String name) {
        return name.equals(BinaryGuesser.JAVA)
                || extensionMatches(name, EXE_EXTENSIONS)
                || containsExtension(name, EXE_EXTENSIONS);
    }

    private boolean containsExtension(final String name, final String[] exts) {
        for (final String ext : exts) {
            if (name.indexOf("." + ext + ".") >= 0) {
                return true;
            }
        }
        return false;
    }

    private boolean extensionMatches(final String name, final String[] exts) {
        for (final String ext : exts) {
            if (name.endsWith("." + ext)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBytecode(final String name) {
        return extensionMatches(name, BYTECODE_EXTENSIONS);
    }

    private boolean isImage(final String name) {
        return extensionMatches(name, IMAGE_EXTENSIONS);
    }

    private boolean isKeystore(final String name) {
        return extensionMatches(name, KEYSTORE_EXTENSIONS);
    }

    /**
     * Is a file by that name a known binary file?
     */
    private boolean isBinary(final String name) {
        if (name == null) {
            return false;
        }
        final String normalisedName = GuessUtils.normalise(name);
        return BinaryGuesser.JAR_MANIFEST.equals(name)
                || isImage(normalisedName) || isKeystore(normalisedName)
                || isBytecode(normalisedName) || isBinaryData(normalisedName)
                || isExecutable(normalisedName);
    }

}
