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

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Add a license header to a document. This appender does not check for the
 * existence of an existing license header, it is assumed that either a second
 * license header is intentional or that there is no license header present
 * already.
 */
public abstract class AbstractLicenseAppender {
    private static final String DOT = ".";
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
    private static final int TYPE_PERL = 16;
    private static final int TYPE_TCL = 17;
    private static final int TYPE_CPP = 18;
    private static final int TYPE_CSHARP = 19;
    private static final int TYPE_PHP = 20;
    private static final int TYPE_GROOVY = 21;
    private static final int TYPE_VISUAL_STUDIO_SOLUTION = 22;
    private static final int TYPE_BEANSHELL = 23;
    private static final int TYPE_JSP = 24;
    private static final int TYPE_FML = 25;
    private static final int TYPE_GO = 26;
    private static final int TYPE_PM = 27;    

    /**
     * the line separator for this OS
     */
    private static final String LINE_SEP = System.getProperty("line.separator");

    private static final int[] FAMILY_C = new int[]{
            TYPE_JAVA, TYPE_JAVASCRIPT, TYPE_C, TYPE_H, TYPE_SCALA,
            TYPE_CSS, TYPE_CPP, TYPE_CSHARP, TYPE_PHP, TYPE_GROOVY,
            TYPE_BEANSHELL, TYPE_GO,
    };
    private static final int[] FAMILY_SGML = new int[]{
            TYPE_XML, TYPE_HTML, TYPE_JSP, TYPE_FML,
    };
    private static final int[] FAMILY_SH = new int[]{
            TYPE_PROPERTIES, TYPE_PYTHON, TYPE_SH, TYPE_RUBY, TYPE_PERL,
            TYPE_TCL, TYPE_VISUAL_STUDIO_SOLUTION, TYPE_PM,
    };
    private static final int[] FAMILY_BAT = new int[]{
            TYPE_BAT,
    };
    private static final int[] FAMILY_APT = new int[]{
            TYPE_APT,
    };
    private static final int[] FAMILY_VELOCITY = new int[]{
            TYPE_VM,
    };
    private static final int[] EXPECTS_HASH_PLING = new int[]{
            TYPE_PYTHON, TYPE_SH, TYPE_RUBY, TYPE_PERL, TYPE_TCL,
    };
    private static final int[] EXPECTS_AT_ECHO = new int[]{
            TYPE_BAT,
    };
    private static final int[] EXPECTS_PACKAGE = new int[]{
            TYPE_JAVA, TYPE_GO, TYPE_PM,
    };
    private static final int[] EXPECTS_XML_DECL = new int[]{
            TYPE_XML,
    };
    private static final int[] EXPECTS_PHP_PI = new int[]{
            TYPE_PHP,
    };
    private static final int[] EXPECTS_MSVSSF_HEADER = new int[]{
            TYPE_VISUAL_STUDIO_SOLUTION,
    };

    private static final Map<String, Integer> EXT2TYPE = new HashMap<>();

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
        Arrays.sort(EXPECTS_MSVSSF_HEADER);

        EXT2TYPE.put("apt", TYPE_APT);
        EXT2TYPE.put("asax", TYPE_HTML);
        EXT2TYPE.put("ascx", TYPE_HTML);
        EXT2TYPE.put("aspx", TYPE_HTML);
        EXT2TYPE.put("bat", TYPE_BAT);
        EXT2TYPE.put("bsh", TYPE_BEANSHELL);
        EXT2TYPE.put("c", TYPE_C);
        EXT2TYPE.put("cc", TYPE_CPP);
        EXT2TYPE.put("cmd", TYPE_BAT);
        EXT2TYPE.put("config", TYPE_XML);
        EXT2TYPE.put("cpp", TYPE_CPP);
        EXT2TYPE.put("cs", TYPE_CSHARP);
        EXT2TYPE.put("csdproj", TYPE_XML);
        EXT2TYPE.put("csproj", TYPE_XML);
        EXT2TYPE.put("css", TYPE_CSS);
        EXT2TYPE.put("fxcop", TYPE_XML);
        EXT2TYPE.put("fml", TYPE_FML);
        EXT2TYPE.put("groovy", TYPE_GROOVY);
        EXT2TYPE.put("go", TYPE_GO);
        EXT2TYPE.put("h", TYPE_H);
        EXT2TYPE.put("hh", TYPE_H);
        EXT2TYPE.put("hpp", TYPE_H);
        EXT2TYPE.put("htm", TYPE_HTML);
        EXT2TYPE.put("html", TYPE_HTML);
        EXT2TYPE.put("java", TYPE_JAVA);
        EXT2TYPE.put("js", TYPE_JAVASCRIPT);
        EXT2TYPE.put("jsp", TYPE_JSP);
        EXT2TYPE.put("ndoc", TYPE_XML);
        EXT2TYPE.put("nunit", TYPE_XML);
        EXT2TYPE.put("php", TYPE_PHP);
        EXT2TYPE.put("pl", TYPE_PERL);
        EXT2TYPE.put("pm", TYPE_PM);
        EXT2TYPE.put("properties", TYPE_PROPERTIES);
        EXT2TYPE.put("py", TYPE_PYTHON);
        EXT2TYPE.put("rb", TYPE_RUBY);
        EXT2TYPE.put("rdf", TYPE_XML);
        EXT2TYPE.put("resx", TYPE_XML);
        EXT2TYPE.put("scala", TYPE_SCALA);
        EXT2TYPE.put("sh", TYPE_SH);
        EXT2TYPE.put("shfbproj", TYPE_XML);
        EXT2TYPE.put("sln", TYPE_VISUAL_STUDIO_SOLUTION);
        EXT2TYPE.put("stylecop", TYPE_XML);
        EXT2TYPE.put("svg", TYPE_XML);
        EXT2TYPE.put("tcl", TYPE_TCL);
        EXT2TYPE.put("vbdproj", TYPE_XML);
        EXT2TYPE.put("vbproj", TYPE_XML);
        EXT2TYPE.put("vcproj", TYPE_XML);
        EXT2TYPE.put("vm", TYPE_VM);
        EXT2TYPE.put("vsdisco", TYPE_XML);
        EXT2TYPE.put("webinfo", TYPE_XML);
        EXT2TYPE.put("xml", TYPE_XML);
        EXT2TYPE.put("xproj", TYPE_XML);
        EXT2TYPE.put("xsl", TYPE_XML);
    }

    private boolean isForced;

    public AbstractLicenseAppender() {
        super();
    }

    /**
     * Append the default license header to the supplied document.
     *
     * @param document document to append to.
     * @throws IOException if there is a problem while reading or writing the file
     */
    public void append(File document) throws IOException {
        int type = getType(document);
        if (type == TYPE_UNKNOWN) {
            return;
        }

        boolean expectsHashPling = expectsHashPling(type);
        boolean expectsAtEcho = expectsAtEcho(type);
        boolean expectsPackage = expectsPackage(type);
        boolean expectsXMLDecl = expectsXMLDecl(type);
        boolean expectsPhpPI = expectsPhpPI(type);
        boolean expectsMSVSSF = expectsMSVisualStudioSolutionFileHeader(type);

        File newDocument = new File(document.getAbsolutePath() + ".new");
        FileWriter writer = new FileWriter(newDocument);
        try {
            if (!attachLicense(writer, document,
                    expectsHashPling, expectsAtEcho, expectsPackage,
                    expectsXMLDecl, expectsPhpPI, expectsMSVSSF)) {
                // Java File without package, XML file without decl or PHP
                // file without PI
                // for Java just place the license at the front, for XML add
                // an XML decl first - don't know how to handle PHP
                if (expectsPackage || expectsXMLDecl) {
                    writer = new FileWriter(newDocument);
                    if (expectsXMLDecl) {
                        writer.write("<?xml version='1.0'?>");
                        writer.write(LINE_SEP);
                    }
                    attachLicense(writer, document,
                            false, false, false, false, false, false);
                }
            }
        } finally {
            IOUtils.closeQuietly(writer);
        }

        if (isForced) {
            boolean deleted = document.delete();
            if (!deleted) {
                System.err.println("Could not delete original file to prepare renaming.");
            }
            boolean renamed = newDocument.renameTo(document.getAbsoluteFile());
            if (!renamed) {
                System.err.println("Failed to rename new file, original file remains unchanged.");
            }
        }
    }

    /**
     * Write document's content to writer attaching the license using
     * the given flags as hints for where to put it.
     *
     * @return whether the license has actually been written
     */
    private boolean attachLicense(Writer writer, File document,
                                  boolean expectsHashPling,
                                  boolean expectsAtEcho,
                                  boolean expectsPackage,
                                  boolean expectsXMLDecl,
                                  boolean expectsPhpPI,
                                  boolean expectsMSVSSF)
            throws IOException {
        boolean written = false;
        FileInputStream fis = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(document);
            br = new BufferedReader(new InputStreamReader(new BOMInputStream(fis)));

            if (!expectsHashPling
                    && !expectsAtEcho
                    && !expectsPackage
                    && !expectsXMLDecl
                    && !expectsPhpPI
                    && !expectsMSVSSF) {
                written = true;
                writer.write(getLicenseHeader(document));
                writer.write(LINE_SEP);
            }

            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first && expectsHashPling) {
                    written = true;
                    doFirstLine(document, writer, line, "#!");
                } else if (first && expectsAtEcho) {
                    written = true;
                    doFirstLine(document, writer, line, "@echo");
                } else if (first && expectsMSVSSF) {
                    written = true;
                    if ("".equals(line)) {
                        line = passThroughReadNext(writer, line, br);
                    }
                    if (line.startsWith("Microsoft Visual Studio Solution"
                            + " File")) {
                        line = passThroughReadNext(writer, line, br);
                    }
                    doFirstLine(document, writer, line, "# Visual ");
                } else {
                    writer.write(line);
                    writer.write(LINE_SEP);
                }

                if (expectsPackage && line.startsWith("package ")) {
                    written = true;
                    writer.write(LINE_SEP);
                    writer.write(getLicenseHeader(document));
                    writer.write(LINE_SEP);
                } else if (expectsXMLDecl && line.startsWith("<?xml ")) {
                    written = true;
                    writer.write(LINE_SEP);
                    writer.write(getLicenseHeader(document));
                    writer.write(LINE_SEP);
                } else if (expectsPhpPI && line.startsWith("<?php")) {
                    written = true;
                    writer.write(LINE_SEP);
                    writer.write(getLicenseHeader(document));
                    writer.write(LINE_SEP);
                }
                first = false;
            }
        } finally {
            IOUtils.closeQuietly(br);
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(writer);
        }
        return written;
    }

    /**
     * Check first line for specified text and process.
     */
    private void doFirstLine(File document, Writer writer, String line, String lookfor) throws IOException {
        if (line.startsWith(lookfor)) {
            writer.write(line);
            writer.write(LINE_SEP);
            writer.write(getLicenseHeader(document));
        } else {
            writer.write(getLicenseHeader(document));
            writer.write(line);
            writer.write(LINE_SEP);
        }
    }

    /**
     * Detect the type of document.
     *
     * @param document to retrieve type from.
     * @return not null
     * TODO use existing mechanism to detect the type of a file and record it in the report output, thus we will not need this duplication here.
     */
    protected int getType(File document) {
        String path = document.getPath();
        int lastDot = path.lastIndexOf(DOT);
        if (lastDot >= 0 && lastDot < path.length() - 1) {
            String ext = path.substring(lastDot + 1);
            Integer type = EXT2TYPE.get(ext);
            if (type != null) {
                return type;
            }
        }
        return TYPE_UNKNOWN;
    }

    /**
     * Set the force flag on this appender. If this flag is set
     * to true then files will be modified directly, otherwise
     * new files will be created alongside the existing files.
     *
     * @param force force flag.
     */
    public void setForce(boolean force) {
        isForced = force;
    }

    /**
     * @param document document to extract from.
     * @return Get the license header of a document.
     */
    public abstract String getLicenseHeader(File document);

    /**
     * Get the first line of the license header formatted
     * for the given type of file.
     *
     * @param type the type of file, see the TYPE_* constants
     * @return not null
     */
    protected String getFirstLine(int type) {
        if (isFamilyC(type)) {
            return "/*" + LINE_SEP;
        } else if (isFamilySGML(type)) {
            return "<!--" + LINE_SEP;
        }
        return "";
    }


    /**
     * Get the last line of the license header formatted
     * for the given type of file.
     *
     * @param type the type of file, see the TYPE_* constants
     * @return not null
     */
    protected String getLastLine(int type) {
        if (isFamilyC(type)) {
            return " */" + LINE_SEP;
        } else if (isFamilySGML(type)) {
            return "-->" + LINE_SEP;
        }
        return "";
    }


    /**
     * Get a line of the license header formatted
     * for the given type of file.
     *
     * @param type    the type of file, see the TYPE_* constants
     * @param content the content for this line
     * @return not null
     */
    protected String getLine(int type, String content) {
        if (isFamilyC(type)) {
            return " * " + content + LINE_SEP;
        } else if (isFamilySGML(type)) {
            return content + LINE_SEP;
        } else if (isFamilyAPT(type)) {
            return "~~ " + content + LINE_SEP;
        } else if (isFamilySH(type)) {
            return "# " + content + LINE_SEP;
        } else if (isFamilyBAT(type)) {
            return "rem " + content + LINE_SEP;
        } else if (isFamilyVelocity(type)) {
            return "## " + content + LINE_SEP;
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

    private static boolean expectsPhpPI(int type) {
        return isIn(EXPECTS_PHP_PI, type);
    }

    private static boolean expectsMSVisualStudioSolutionFileHeader(int type) {
        return isIn(EXPECTS_MSVSSF_HEADER, type);
    }

    private static boolean isIn(int[] arr, int key) {
        return Arrays.binarySearch(arr, key) >= 0;
    }

    private String passThroughReadNext(Writer writer, String line,
                                       BufferedReader br) throws IOException {
        writer.write(line);
        writer.write(LINE_SEP);
        String l = br.readLine();
        return l == null ? "" : l;
    }
}

/**
 * Stripped down version of Commons IO 2.0's BOMInputStream.
 */
class BOMInputStream extends FilterInputStream {
    private int[] firstBytes;
    private int fbLength, fbIndex, markFbIndex;
    private boolean markedAtStart;
    private static final int[][] BOMS = {
            new int[]{0xEF, 0xBB, 0xBF}, // UTF-8
            new int[]{0xFE, 0xFF}, // UTF-16BE
            new int[]{0xFF, 0xFE}, // UTF-16LE
    };

    BOMInputStream(InputStream s) {
        super(s);
    }

    @Override
    public int read() throws IOException {
        int b = readFirstBytes();
        return (b >= 0) ? b : in.read();
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        int firstCount = 0;
        int b = 0;
        while ((len > 0) && (b >= 0)) {
            b = readFirstBytes();
            if (b >= 0) {
                buf[off++] = (byte) (b & 0xFF);
                len--;
                firstCount++;
            }
        }
        int secondCount = in.read(buf, off, len);
        return (secondCount < 0)
                ? (firstCount > 0 ? firstCount : -1) : firstCount + secondCount;
    }

    @Override
    public int read(byte[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    private int readFirstBytes() throws IOException {
        getBOM();
        return (fbIndex < fbLength) ? firstBytes[fbIndex++] : -1;
    }

    private void getBOM() throws IOException {
        if (firstBytes == null) {
            int max = 0;
            for (int[] BOM : BOMS) {
                max = Math.max(max, BOM.length);
            }
            firstBytes = new int[max];
            for (int i = 0; i < firstBytes.length; i++) {
                firstBytes[i] = in.read();
                fbLength++;
                if (firstBytes[i] < 0) {
                    break;
                }

                boolean found = find();
                if (found) {
                    fbLength = 0;
                    break;
                }
            }
        }
    }

    @Override
    public synchronized void mark(int readlimit) {
        markFbIndex = fbIndex;
        markedAtStart = (firstBytes == null);
        in.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        fbIndex = markFbIndex;
        if (markedAtStart) {
            firstBytes = null;
        }

        in.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        while ((n > 0) && (readFirstBytes() >= 0)) {
            n--;
        }
        return in.skip(n);
    }

    private boolean find() {
        for (int[] BOM : BOMS) {
            if (matches(BOM)) {
                return true;
            }
        }
        return false;
    }

    private boolean matches(int[] bom) {
        if (bom.length != fbLength) {
            return false;
        }
        for (int i = 0; i < bom.length; i++) {
            if (bom[i] != firstBytes[i]) {
                return false;
            }
        }
        return true;
    }

}
