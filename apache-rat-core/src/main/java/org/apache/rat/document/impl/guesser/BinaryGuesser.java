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
import java.io.Reader;
import java.util.Locale;

import org.apache.rat.api.Document;

/**
 * TODO: factor into MIME guesser and MIME->binary guesser
 */
public class BinaryGuesser {

    private static boolean isBinaryDocument(Document document) {
        boolean result = false;
        Reader reader = null;
        try
        {
            reader = document.reader();
            result = isBinary(reader);
        }
        catch (IOException e)
        {
            result = false;
        }
        finally
        {
            try
            {
                if (reader != null)
                {
                    reader.close();
                }
            }
            catch (IOException e)
            {
                // SWALLOW
            }   
        }
        return result;
    }
    
    /**
     * Do the first few bytes of the stream hint at a binary file?
     */
    public static boolean isBinary(Reader in) {
        boolean result = false;
        char[] taste = new char[100];
        try {
            int bytesRead = in.read(taste);
            if (bytesRead > 0) {
                int highBytes = 0;
                for (int i=0;i<bytesRead;i++) {
                    if (taste[i] > BinaryGuesser.NON_ASCII_THREASHOLD
                        || taste[i] <= BinaryGuesser.ASCII_CHAR_THREASHOLD) {
                        highBytes++;
                    }
                }
                if (highBytes * BinaryGuesser.HIGH_BYTES_RATIO
                    > bytesRead * BinaryGuesser.TOTAL_READ_RATIO) {
                    result = true;
                }
            }
        } catch (IOException e) {
            // SWALLOW 
        }
        return result;
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
        boolean result = false;
        for (int i = 0; !result && i < exts.length; i++) {
            result = name.indexOf("." + exts[i] + ".") >= 0;
        }
        return result;
    }

    public static boolean extensionMatches(final String name,
                                            final String[] exts) {
        boolean result = false;
        for (int i = 0; !result && i < exts.length; i++) {
            result = name.endsWith("." + exts[i]);
        }
        return result;
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
        final String name = document.getName();
        boolean result = isBinary(name);
        if (!result)
        {
            // try a taste
            result = isBinaryDocument(document);
        }
        return result;
    }



}
