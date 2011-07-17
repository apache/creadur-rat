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
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Locale;

import org.apache.rat.api.Document;

/**
 * TODO: factor into MIME guesser and MIME->binary guesser
 */
public class BinaryGuesser {

    private static boolean isBinaryDocument(Document document) {
        boolean result = false;
        InputStream stream = null;
        try
        {
            stream = document.inputStream();
            result = isBinary(stream);
        }
        catch (IOException e)
        {
            result = false;
        }
        finally
        {
            try
            {
                if (stream != null)
                {
                    stream.close();
                }
            }
            catch (IOException e)
            {
                // SWALLOW
            }   
        }
        return result;
    }
    
    private static boolean isBinary(CharSequence taste) {
        int highBytes = 0;
        final int length = taste.length();
        for (int i = 0; i < length; i++) {
            char c = taste.charAt(i);
            if (c > BinaryGuesser.NON_ASCII_THREASHOLD
                || c <= BinaryGuesser.ASCII_CHAR_THREASHOLD) {
                highBytes++;
            }
        }
        return highBytes * BinaryGuesser.HIGH_BYTES_RATIO
            > length * BinaryGuesser.TOTAL_READ_RATIO;
    }

    /**
     * Do the first few bytes of the stream hint at a binary file?
     *
     * <p>Any IOException is swallowed internally and the test returns
     * false.</p>
     *
     * <p>This method may lead to false negatives if the reader throws
     * an exception because it can't read characters according to the
     * reader's encoding from the underlying stream.</p>
     */
    public static boolean isBinary(Reader in) {
        char[] taste = new char[100];
        try {
            int bytesRead = in.read(taste);
            if (bytesRead > 0) {
                return isBinary(new String(taste, 0, bytesRead));
            }
        } catch (IOException e) {
            // SWALLOW 
        }
        return false;
    }

    /**
     * Do the first few bytes of the stream hint at a binary file?
     *
     * <p>Any IOException is swallowed internally and the test returns
     * false.</p>
     *
     * <p>This method will try to read bytes from the stream and
     * translate them to characters according to the platform's
     * default encoding.  If any bytes can not be translated to
     * characters it will assume the original data must be binary and
     * return true.</p>
     */
    public static boolean isBinary(InputStream in) {
        try {
            byte[] taste = new byte[200];
            int bytesRead = in.read(taste);
            if (bytesRead > 0) {
                ByteBuffer bytes = ByteBuffer.wrap(taste, 0, bytesRead);
                CharBuffer chars = CharBuffer.allocate(2 * bytesRead);
                Charset cs = Charset.forName(System.getProperty("file.encoding"));
                CharsetDecoder cd = cs.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
                while (bytes.remaining() > 0) {
                    CoderResult res = cd.decode(bytes, chars, true);
                    if (res.isMalformed() || res.isUnmappable()) {
                        return true;
                    } else if (res.isOverflow()) {
                        chars.limit(chars.position());
                        chars.rewind();
                        int c = chars.capacity() * 2;
                        CharBuffer on = CharBuffer.allocate(c);
                        on.put(chars);
                        chars = on;
                    }
                }
                chars.limit(chars.position());
                chars.rewind();
                return isBinary(chars);
            }
        } catch (IOException e) {
            // SWALLOW 
        }
        return false;
    }

    public static final boolean isBinaryData(final String name) {
        return extensionMatches(name, DATA_EXTENSIONS);
    }

    /**
     * Is a file by that name a known non-binary file?
     */
    public static final boolean isNonBinary(final String name) {
        if (name == null) {return false;}
        return extensionMatches(name.toUpperCase(Locale.US),
                                BinaryGuesser.NON_BINARY_EXTENSIONS);
    }

    public static final boolean isExecutable(final String name) {
        return name.equals(BinaryGuesser.JAVA) || extensionMatches(name, EXE_EXTENSIONS)
            || containsExtension(name, EXE_EXTENSIONS);
    }

    public static boolean containsExtension(final String name,
                                             final String[] exts) {
        for (int i = 0; i < exts.length; i++) {
            if (name.indexOf("." + exts[i] + ".") >= 0) {
                return true;
            }
        }
        return false;
    }

    public static boolean extensionMatches(final String name,
                                            final String[] exts) {
        for (int i = 0; i < exts.length; i++) {
            if (name.endsWith("." + exts[i])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBytecode(final String name) {
        return BinaryGuesser.extensionMatches(name, BYTECODE_EXTENSIONS);
    }

    public static final boolean isImage(final String name) {
        return BinaryGuesser.extensionMatches(name, IMAGE_EXTENSIONS);
    }

    public static final boolean isKeystore(final String name) {
        return BinaryGuesser.extensionMatches(name, KEYSTORE_EXTENSIONS);
    }
    
    /**
     * Is a file by that name a known binary file?
     */
    public static final boolean isBinary(final String name) {
        if (name == null) {return false;}
        String normalisedName = GuessUtils.normalise(name);
        return BinaryGuesser.JAR_MANIFEST.equals(name) || BinaryGuesser.isImage(normalisedName)
            || BinaryGuesser.isKeystore(normalisedName) || BinaryGuesser.isBytecode(normalisedName)
            || BinaryGuesser.isBinaryData(normalisedName) || BinaryGuesser.isExecutable(normalisedName);
    }

    public static final String[] DATA_EXTENSIONS = {
        "DAT", "DOC",
        "NCB", "IDB",
        "SUO", "XCF",
        "RAJ", "CERT",
        "KS", "TS",
        "ODP",
    };
    public static final String[] EXE_EXTENSIONS = {
        "EXE", "DLL",
        "LIB", "SO",
        "A", "EXP",
    };
    public static final String[] KEYSTORE_EXTENSIONS = {
        "JKS", "KEYSTORE", "PEM", "CRL"
    };
    public static final String[] IMAGE_EXTENSIONS = {
        "PNG", "PDF",
        "GIF", "GIFF",
        "TIF", "TIFF",
        "JPG", "JPEG",
        "ICO", "ICNS",
    };
    public static final String[] BYTECODE_EXTENSIONS = {
        "CLASS", "PYD",
        "OBJ", "PYC",
    };
    
    /**
     * Based on http://www.apache.org/dev/svn-eol-style.txt
     */
    public static final String[] NON_BINARY_EXTENSIONS = {
        "AART",
        "AC",
        "AM",
        "BAT",
        "C",
        "CAT",
        "CGI",
        "CLASSPATH",
        "CMD",
        "CONFIG",
        "CPP",
        "CSS",
        "CWIKI",
        "DATA",
        "DCL",
        "DTD",
        "EGRM",
        "ENT",
        "FT", 
        "FN",
        "FV", 
        "GRM",
        "G",
        "H",
        "HTACCESS",
        "HTML",
        "IHTML",
        "IN",
        "JAVA",
        "JMX", 
        "JSP",
        "JS",
        "JUNIT",
        "JX", 
        "MANIFEST",
        "M4",
        "MF",
        "MF",
        "META",
        "MOD",
        "N3",
        "PEN",
        "PL",
        "PM",
        "POD",
        "POM",
        "PROJECT",
        "PROPERTIES",
        "PY",
        "RB",
        "RDF",
        "RNC",
        "RNG",
        "RNX",
        "ROLES",
        "RSS",
        "SH",
        "SQL",
        "SVG",
        "TLD",
        "TXT",
        "TYPES",
        "VM",
        "VSL",
        "WSDD",
        "WSDL",
        "XARGS",
        "XCAT",
        "XCONF",
        "XEGRM",
        "XGRM",
        "XLEX",
        "XLOG",
        "XMAP",
        "XML",
        "XROLES",
        "XSAMPLES",
        "XSD",
        "XSL",
        "XSLT",
        "XSP",
        "XUL",
        "XWEB",
        "XWELCOME",
    };
    public static final String JAR_MANIFEST = "MANIFEST.MF";
    public static final String JAVA = "JAVA";
    public static final int HIGH_BYTES_RATIO = 100;
    public static final int TOTAL_READ_RATIO = 30;
    public static final int NON_ASCII_THREASHOLD = 256;
    public static final int ASCII_CHAR_THREASHOLD = 8;

    public static final boolean isBinary(final Document document) {
        // TODO: reimplement the binary test algorithm?
        // TODO: more efficient to move into standard analysis
        // TODO: then use binary as default
        return isBinary(document.getName())
            ||
            // try a taste
            isBinaryDocument(document);
    }



}
