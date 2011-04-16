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
package org.apache.rat.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 * Add a licence header to a document. This appender does not check for the
 * existence of an existing licence header, it is assumed that either a second
 * licence header is intentional or that there is no licence header present
 * already.
 * 
 */
public abstract class AbstractLicenceAppender {
    private static final int TYPE_UNKNOWN = 0;
    private static final int TYPE_JAVA = 1;
    private static final int TYPE_XML = 2;
    private static final int TYPE_HTML = 3;
    private static final int TYPE_CSS = 4;
    private static final int TYPE_JAVASCRIPT = 5;
    private static final int TYPE_APT = 6;
    private static final int TYPE_PROPERTIES = 7;
    private static final int TYPE_PYTHON = 8;
    private static final int TYPE_C = 9;
    private static final int TYPE_H = 10;
    private static final int TYPE_SH = 11;
    private static final int TYPE_BAT = 12;
    private static final int TYPE_VM = 13;
    private static final int TYPE_SCALA = 14;
    private static final int TYPE_RUBY = 15;

    private static final int[] FAMILY_C = new int[] {
        TYPE_JAVA, TYPE_JAVASCRIPT, TYPE_C, TYPE_H, TYPE_SCALA,
        TYPE_CSS,
    };
    private static final int[] FAMILY_SGML = new int[] {
        TYPE_XML, TYPE_HTML,
    };
    private static final int[] FAMILY_SH = new int[] {
        TYPE_PROPERTIES, TYPE_PYTHON, TYPE_SH, TYPE_RUBY,
    };
    private static final int[] FAMILY_BAT = new int[] {
        TYPE_BAT,
    };
    private static final int[] FAMILY_APT = new int[] {
        TYPE_APT,
    };
    private static final int[] FAMILY_VELOCITY = new int[] {
        TYPE_VM,
    };
    private static final int[] EXPECTS_HASH_PLING = new int[] {
        TYPE_PYTHON, TYPE_SH, TYPE_RUBY,
    };
    private static final int[] EXPECTS_AT_ECHO = new int[] {
        TYPE_BAT,
    };
    private static final int[] EXPECTS_PACKAGE = new int[] {
        TYPE_JAVA,
    };
    private static final int[] EXPECTS_XML_DECL = new int[] {
        TYPE_XML,
    };

    static {
        // these arrays are used in Arrays.binarySearch so they must
        // be sorted
        Arrays.sort(FAMILY_C);
        Arrays.sort(FAMILY_SGML);
        Arrays.sort(FAMILY_SH);
        Arrays.sort(FAMILY_BAT);
        Arrays.sort(FAMILY_APT);
        Arrays.sort(FAMILY_VELOCITY);

        Arrays.sort(EXPECTS_HASH_PLING);
        Arrays.sort(EXPECTS_AT_ECHO);
        Arrays.sort(EXPECTS_PACKAGE);
        Arrays.sort(EXPECTS_XML_DECL);
    }

    private boolean isForced;

    public AbstractLicenceAppender() {
        super();
    }

    /**
     * Append the default licence header to the supplied document.
     * 
     * @param document
     * @throws IOException
     *           if there is a problem either reading or writing the file
     */
    public void append(File document) throws IOException {
        int type = getType(document);
        if (type == TYPE_UNKNOWN) {
            return;
        }
        File newDocument = new File(document.getAbsolutePath() + ".new");
        FileWriter writer = new FileWriter(newDocument);

        FileReader fr = new FileReader(document);
        BufferedReader br = new BufferedReader(fr);

        boolean expectsHashPling = expectsHashPling(type);
        boolean expectsAtEcho = expectsAtEcho(type);
        boolean expectsPackage = expectsPackage(type);
        boolean expectsXMLDecl = expectsXMLDecl(type);

        if (!expectsHashPling
            && !expectsAtEcho
            && !expectsPackage
            && !expectsXMLDecl) {
            writer.write(getLicenceHeader(document));
            writer.write('\n');
        }

        String line;
        boolean first = true;
        while ((line = br.readLine()) != null) {
            if (first && expectsHashPling) {
                doFirstLine(document, writer, line, "#!");
            } else if (first && expectsAtEcho) {
                doFirstLine(document, writer, line, "@echo");
            } else {
                writer.write(line);
                writer.write('\n');
            }

            if (expectsPackage && line.startsWith("package ")) {
                writer.write(getLicenceHeader(document));
                writer.write('\n');
            }
            if (expectsXMLDecl && line.startsWith("<?xml ")) {
                writer.write(getLicenceHeader(document));
                writer.write('\n');
            }
            first = false;
        }
        br.close();
        writer.close();

        if (isForced) {
            document.delete();
            boolean renamed = newDocument.renameTo(document.getAbsoluteFile());
            if (!renamed) {
                System.err.println("Failed to rename new file, original file remains unchanged.");
            }
        }
    }

    /**
     * Check first line for specified text and process.
     */
    private void doFirstLine(File document, FileWriter writer, String line, String lookfor) throws IOException  {
        if (line.startsWith(lookfor)) {
            writer.write(line);
            writer.write('\n');
            writer.write(getLicenceHeader(document));
        } else {
            writer.write(getLicenceHeader(document));
            writer.write(line);
            writer.write('\n');
        }
    }

    /**
     * Detect the type of document.
     * 
     * @param document
     * @return not null
     * @TODO use existing mechanism to detect the type of a file and record it in the report output, thus we will not need this duplication here.
     */
    protected int getType(File document) {
        if (document.getPath().endsWith(".java")) {
            return TYPE_JAVA;
        } else if (document.getPath().endsWith(".xml") || document.getPath().endsWith(".xsl")) {
            return TYPE_XML;
        } else if (document.getPath().endsWith(".html") || document.getPath().endsWith(".htm")) {
            return TYPE_HTML;
        } else if (document.getPath().endsWith(".rdf") || document.getPath().endsWith(".xsl")) {
            return TYPE_XML;
        } else if (document.getPath().endsWith(".css")) {
            return TYPE_CSS;
        } else if (document.getPath().endsWith(".js")) {
            return TYPE_JAVASCRIPT;
        } else if (document.getPath().endsWith(".apt")) {
            return TYPE_APT;
        } else if (document.getPath().endsWith(".properties")) {
            return TYPE_PROPERTIES;
        } else if (document.getPath().endsWith(".py")) {
            return TYPE_PYTHON;
        } else if (document.getPath().endsWith(".c")) {
            return TYPE_C;
        } else if (document.getPath().endsWith(".h")) {
            return TYPE_H;
        } else if (document.getPath().endsWith(".sh")) {
            return TYPE_SH;
        } else if (document.getPath().endsWith(".bat")) {
            return TYPE_BAT;
        } else if (document.getPath().endsWith(".vm")) {
            return TYPE_VM;
        } else if (document.getPath().endsWith(".scala")) {
            return TYPE_SCALA;
        } else if (document.getPath().endsWith(".rb")) {
            return TYPE_RUBY;
        }
        return TYPE_UNKNOWN;
    }

    /**
     * Set the force flag on this appender. If this flag is set
     * to true then files will be modified directly, otherwise
     * new files will be created alongside the existing files.
     * 
     * @param b
     */
    public void setForce(boolean b) {
        isForced = b;
    }

    /**
     * Get the licence header for a document.
     */
    public abstract String getLicenceHeader(File document);

    /**
     * Get the first line of the licence header formatted
     * for the given type of file.
     * 
     * @param type the type of file, see the TYPE_* constants
     * @return not null
     */
    protected String getFirstLine(int type) {
        if (isFamilyC(type)) {
            return "/*\n";
        } else if (isFamilySGML(type)) {
            return "<!--\n";
        } else if (isFamilyAPT(type)) {
            return "~~\n";
        } else if (isFamilySH(type)) {
            return "#\n";
        } else if (isFamilyBAT(type)) {
            return "rem\n";
        }
        return "";
    }


    /**
     * Get the last line of the licence header formatted
     * for the given type of file.
     * 
     * @param type the type of file, see the TYPE_* constants
     * @return not null
     */
    protected String getLastLine(int type) {
        if (isFamilyC(type)) {
            return "*/\n";
        } else if (isFamilySGML(type)) {
            return "-->\n";
        } else if (isFamilyAPT(type)) {
            return "~~\n";
        } else if (isFamilySH(type)) {
            return "#\n";
        } else if (isFamilyBAT(type)) {
            return "rem\n";
        }
        return "";
    }


    /**
     * Get a line of the licence header formatted
     * for the given type of file.
     * 
     * @param type the type of file, see the TYPE_* constants
     * @param content the content for this line
     * @return not null
     */
    protected String getLine(int type, String content) {
        if (content != null && content.length() > 0) {
            content = " " + content;
        }
        if (isFamilyC(type)) {
            return " *" + content + "\n";
        } else if (isFamilySGML(type)) {
            return content + "\n";
        } else if (isFamilyAPT(type)) {
            return "~~" + content + "\n";
        } else if (isFamilySH(type)) {
            return "#" + content + "\n";
        } else if (isFamilyBAT(type)) {
            return "rem" + content + "\n";
        } else if (isFamilyVelocity(type)) {
            return "##" + content + "\n";
        }
        return "";
    }

    private static boolean isFamilyC(int type) {
        return isIn(FAMILY_C, type);
    }
    private static boolean isFamilySGML(int type) {
        return isIn(FAMILY_SGML, type);
    }
    private static boolean isFamilySH(int type) {
        return isIn(FAMILY_SH, type);
    }
    private static boolean isFamilyAPT(int type) {
        return isIn(FAMILY_APT, type);
    }
    private static boolean isFamilyBAT(int type) {
        return isIn(FAMILY_BAT, type);
    }
    private static boolean isFamilyVelocity(int type) {
        return isIn(FAMILY_VELOCITY, type);
    }
    private static boolean expectsHashPling(int type) {
        return isIn(EXPECTS_HASH_PLING, type);
    }
    private static boolean expectsAtEcho(int type) {
        return isIn(EXPECTS_AT_ECHO, type);
    }
    private static boolean expectsPackage(int type) {
        return isIn(EXPECTS_PACKAGE, type);
    }
    private static boolean expectsXMLDecl(int type) {
        return isIn(EXPECTS_XML_DECL, type);
    }
    private static boolean isIn(int[] arr, int key) {
        return Arrays.binarySearch(arr, key) >= 0;
    }
}
