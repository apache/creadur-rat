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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.input.BOMInputStream;
import org.apache.rat.api.MetaData;
import org.apache.rat.utils.DefaultLog;

/**
 * Add a license header to a document. This appender does not check for the
 * existence of an existing license header, it is assumed that either a second
 * license header is intentional or that there is no license header present
 * already.
 */
public abstract class AbstractLicenseAppender {
    /** The dot '.' character */
    private static final String DOT = ".";
    /** unknown files */
    private static final int TYPE_UNKNOWN = 0;
    /** Java files */
    private static final int TYPE_JAVA = 1;
    /** XML files */
    private static final int TYPE_XML = 2;
    /** HTML files */
    private static final int TYPE_HTML = 3;
    /** CSS files */
    private static final int TYPE_CSS = 4;
    /** javascript files */
    private static final int TYPE_JAVASCRIPT = 5;
    /** Almost plain text files */
    private static final int TYPE_APT = 6;
    /** Properties files */
    private static final int TYPE_PROPERTIES = 7;
    /** Python files */
    private static final int TYPE_PYTHON = 8;
    /** C files */
    private static final int TYPE_C = 9;
    /** C Header files */
    private static final int TYPE_H = 10;
    /** Shell script files */
    private static final int TYPE_SH = 11;
    /** Batch files */
    private static final int TYPE_BAT = 12;
    /** VM files */
    private static final int TYPE_VM = 13;
    /** scala files */
    private static final int TYPE_SCALA = 14;
    /** Ruby files */
    private static final int TYPE_RUBY = 15;
    /** PERL files */
    private static final int TYPE_PERL = 16;
    /** TCL files */
    private static final int TYPE_TCL = 17;
    /** C++ files */
    private static final int TYPE_CPP = 18;
    /** C# files */
    private static final int TYPE_CSHARP = 19;
    /** PHP files */
    private static final int TYPE_PHP = 20;
    /** Groovy files */
    private static final int TYPE_GROOVY = 21;
    /** Visual studio solution files */
    private static final int TYPE_VISUAL_STUDIO_SOLUTION = 22;
    /** BeanShell files */
    private static final int TYPE_BEANSHELL = 23;
    /** JSP files */
    private static final int TYPE_JSP = 24;
    /** FML files */
    private static final int TYPE_FML = 25;
    /** GO files */
    private static final int TYPE_GO = 26;
    /** PM files */
    private static final int TYPE_PM = 27;
    /** Markdown files */
    private static final int TYPE_MD = 28;
    /** YAML files */
    private static final int TYPE_YAML = 29;
    /** AsciiDoc files */
    private static final int TYPE_ASCIIDOC = 30;

    /**
     * The line separator for this OS.
     */
    private static final String LINE_SEP = System.lineSeparator();
    /**
     * Files that are in the C family.
     */
    private static final int[] FAMILY_C = new int[]{
            TYPE_JAVA, TYPE_JAVASCRIPT, TYPE_C, TYPE_H, TYPE_SCALA,
            TYPE_CSS, TYPE_CPP, TYPE_CSHARP, TYPE_PHP, TYPE_GROOVY,
            TYPE_BEANSHELL, TYPE_GO,
    };
    /**
     * Files that are in the SGML family.
     */
    private static final int[] FAMILY_SGML = new int[]{
            TYPE_XML, TYPE_HTML, TYPE_JSP, TYPE_FML, TYPE_MD,
    };
    /**
     * Files that are in the Shell family.
     */
    private static final int[] FAMILY_SH = new int[]{
            TYPE_PROPERTIES, TYPE_PYTHON, TYPE_SH, TYPE_RUBY, TYPE_PERL,
            TYPE_TCL, TYPE_VISUAL_STUDIO_SOLUTION, TYPE_PM, TYPE_YAML,
    };
    /**
     * Files that are in the batch family.
     */
    private static final int[] FAMILY_BAT = new int[] {
            TYPE_BAT,
    };
    /**
     * Files that are in the Almost Plain Text family.
     */
    private static final int[] FAMILY_APT = new int[] {
            TYPE_APT,
    };
    /**
     * Files in the velocity family.
     */
    private static final int[] FAMILY_VELOCITY = new int[] {
            TYPE_VM,
    };
    /**
     * Files that expect "#/some/path".
     */
    private static final int[] EXPECTS_HASH_PLING = new int[] {
            TYPE_PYTHON, TYPE_SH, TYPE_RUBY, TYPE_PERL, TYPE_TCL,
    };
    /**
     * Files that expect "@Echo".
     */
    private static final int[] EXPECTS_AT_ECHO = new int[]{
            TYPE_BAT,
    };
    /**
     * Files that expect package names.
     */
    private static final int[] EXPECTS_PACKAGE = new int[]{
            TYPE_JAVA, TYPE_GO, TYPE_PM,
    };
    /**
     * Files that expect the XML Declaration.
     */
    private static final int[] EXPECTS_XML_DECL = new int[]{
            TYPE_XML,
    };
    /**
     * Files that expect the PHP header.
     */
    private static final int[] EXPECTS_PHP_PI = new int[] {
            TYPE_PHP,
    };
    /**
     * Files that expect the Microsoft Visual Source Safe header.
     */
    private static final int[] EXPECTS_MSVSSF_HEADER = new int[] {
            TYPE_VISUAL_STUDIO_SOLUTION,
    };

    /**
     * Mapping of extension to family type.
     */
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

        EXT2TYPE.put("adoc", TYPE_ASCIIDOC);
        EXT2TYPE.put("asc", TYPE_ASCIIDOC);
        EXT2TYPE.put("asciidoc", TYPE_ASCIIDOC);
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
        EXT2TYPE.put("md", TYPE_MD);
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
        EXT2TYPE.put("yaml", TYPE_YAML);
        EXT2TYPE.put("yml", TYPE_YAML);
    }

    /**
     * if {@code true} overwrite the existing files.
     */
    private boolean isOverwrite;

    /**
     * Constructor
     */
    public AbstractLicenseAppender() {
        super();
    }

    /**
     * Append the default license header to the supplied document.
     *
     * @param document document to append to.
     * @throws IOException if there is a problem while reading or writing the file.
     */
    public void append(final File document) throws IOException {
        append(document, null);
    }

    /**
     * Appends a header using already detected content metadata when available.
     * @param document the file to edit.
     * @param metaData metadata from document analysis, or {@code null}.
     * @throws IOException if reading or writing fails.
     */
    public void append(final File document, final MetaData metaData) throws IOException {
        int type = getType(document, metaData);
        if (type == TYPE_UNKNOWN) {
            return;
        }

        LicenseEditor editor = getEditor(type);
        String header = getLicenseHeader(document, metaData);

        File newDocument = new File(document.getAbsolutePath() + ".new");
        try {
            boolean written;
            try (FileWriter writer = new FileWriter(newDocument, StandardCharsets.UTF_8)) {
                written = attachLicense(writer, document, editor, header);
            }
            if (!written) {
                // Java File without package, XML file without decl or PHP
                // file without PI
                // for Java just place the license at the front, for XML add
                // an XML decl first - don't know how to handle PHP
                if (expectsPackage(type) || expectsXMLDecl(type)) {
                    try (FileWriter writer2  = new FileWriter(newDocument, StandardCharsets.UTF_8)) {
                        if (expectsXMLDecl(type)) {
                            writer2.write("<?xml version='1.0'?>");
                            writer2.write(LINE_SEP);
                        }
                        attachLicense(writer2, document, new PlainLicenseEditor(), header);
                    }
                }
            }
        } catch (IOException e) {
            try {
                Files.deleteIfExists(newDocument.toPath());
            } catch (IOException cleanupException) {
                e.addSuppressed(cleanupException);
            }
            throw e;
        }

        if (isOverwrite) {
            replaceOriginalDocument(document, newDocument);
        }
    }

    /**
     * Replace the original document while preserving its executable flag.
     */
    private void replaceOriginalDocument(final File document, final File newDocument) {
        try {
            Path docPath = document.toPath();
            boolean isExecutable = Files.isExecutable(docPath);
            Files.move(newDocument.toPath(), docPath, StandardCopyOption.REPLACE_EXISTING);
            if (isExecutable && !document.setExecutable(true)) {
                DefaultLog.getInstance().warn(String.format("Could not set %s as executable.", document));
            }
        } catch (InvalidPathException | IOException e) {
            DefaultLog.getInstance().error(String.format("Failed to rename new file to %s, original file is unchanged.", document), e);
        }
    }

    private boolean attachLicense(final Writer writer, final File document, final LicenseEditor editor,
                                  final String header) throws IOException {
        try (FileInputStream stream = new FileInputStream(document);
             BOMInputStream bom = BOMInputStream.builder().setInputStream(stream).get();
             BufferedReader reader = new BufferedReader(new InputStreamReader(bom, StandardCharsets.UTF_8))) {
            return editor.edit(document, reader, writer, header);
        }
    }

    private LicenseEditor getEditor(final int type) {
        if (type == TYPE_MD) {
            return new MarkdownLicenseEditor();
        } else if (type == TYPE_ASCIIDOC) {
            return new AsciiDocLicenseEditor();
        } else if (expectsHashPling(type)) {
            return new FirstLineLicenseEditor("#!");
        } else if (expectsAtEcho(type)) {
            return new FirstLineLicenseEditor("@echo");
        } else if (expectsMSVisualStudioSolutionFileHeader(type)) {
            return new VisualStudioLicenseEditor();
        } else if (expectsPackage(type)) {
            return new DeclarationLicenseEditor("package ");
        } else if (expectsXMLDecl(type)) {
            return new DeclarationLicenseEditor("<?xml ");
        } else if (expectsPhpPI(type)) {
            return new DeclarationLicenseEditor("<?php");
        }
        return new PlainLicenseEditor();
    }

    /**
     * Resolves supported types using extensions first, then specific detected MIME types.
     * Generic text/plain cannot determine the required comment syntax.
     * @param document the source file.
     * @param metaData detected metadata, or {@code null}.
     * @return the supported type, or the unknown type.
     */
    protected int getType(final File document, final MetaData metaData) {
        int type = getType(document);
        if (type != TYPE_UNKNOWN || metaData == null || metaData.getMediaType() == null) {
            return type;
        }
        switch (metaData.getMediaType().getBaseType().toString()) {
        case "text/asciidoc":
        case "text/x-asciidoc":
            return TYPE_ASCIIDOC;
        case "text/markdown":
        case "text/x-web-markdown":
            return TYPE_MD;
        case "application/xml":
        case "text/xml":
            return TYPE_XML;
        case "text/html":
            return TYPE_HTML;
        default:
            return TYPE_UNKNOWN;
        }
    }

    /**
     * Detect the type of document.
     *
     * @param document to retrieve type from.
     * @return not null
     */
    protected int getType(final File document) {
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
     * @param overwrite force flag.
     */
    public void setOverwrite(final boolean overwrite) {
        isOverwrite = overwrite;
    }

    /**
     * Gets the header text to insert into the file.
     * @param document document to extract from.
     * @return Get the license header of a document.
     */
    public abstract String getLicenseHeader(File document);

    /**
     * Gets a header using detected metadata. Defaults to the legacy formatting hook.
     * @param document the source file.
     * @param metaData detected metadata, or {@code null}.
     * @return the formatted header.
     */
    protected String getLicenseHeader(final File document, final MetaData metaData) {
        return getLicenseHeader(document);
    }

    /**
     * Get the first line of the license header formatted
     * for the given type of file.
     *
     * @param type the type of file, see the TYPE_* constants
     * @return not null
     */
    protected String getFirstLine(final int type) {
        if (isFamilyC(type)) {
            return "/*" + LINE_SEP;
        } else if (isFamilySGML(type)) {
            return "<!--" + LINE_SEP;
        } else if (type == TYPE_ASCIIDOC) {
            return "////" + LINE_SEP;
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
    protected String getLastLine(final int type) {
        if (isFamilyC(type)) {
            return " */" + LINE_SEP;
        } else if (isFamilySGML(type)) {
            return "-->" + LINE_SEP;
        } else if (type == TYPE_ASCIIDOC) {
            return "////" + LINE_SEP;
        }
        return "";
    }

    /**
     * Get a line of the license header formatted
     * for the given type of file.
     *
     * @param type    the type of file, see the TYPE_* constants.
     * @param content the content for this line.
     * @return not null
     */
    protected String getLine(final int type, final String content) {
        if (isFamilyC(type)) {
            return " * " + content + LINE_SEP;
        } else if (isFamilySGML(type) || type == TYPE_ASCIIDOC) {
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

    private static boolean isFamilyC(final int type) {
        return isIn(FAMILY_C, type);
    }

    private static boolean isFamilySGML(final int type) {
        return isIn(FAMILY_SGML, type);
    }

    private static boolean isFamilySH(final int type) {
        return isIn(FAMILY_SH, type);
    }

    private static boolean isFamilyAPT(final int type) {
        return isIn(FAMILY_APT, type);
    }

    private static boolean isFamilyBAT(final int type) {
        return isIn(FAMILY_BAT, type);
    }

    private static boolean isFamilyVelocity(final int type) {
        return isIn(FAMILY_VELOCITY, type);
    }

    private static boolean expectsHashPling(final int type) {
        return isIn(EXPECTS_HASH_PLING, type);
    }

    private static boolean expectsAtEcho(final int type) {
        return isIn(EXPECTS_AT_ECHO, type);
    }

    private static boolean expectsPackage(final int type) {
        return isIn(EXPECTS_PACKAGE, type);
    }

    private static boolean expectsXMLDecl(final int type) {
        return isIn(EXPECTS_XML_DECL, type);
    }

    private static boolean expectsPhpPI(final int type) {
        return isIn(EXPECTS_PHP_PI, type);
    }

    private static boolean expectsMSVisualStudioSolutionFileHeader(final int type) {
        return isIn(EXPECTS_MSVSSF_HEADER, type);
    }

    private static boolean isIn(final int[] arr, final int key) {
        return Arrays.binarySearch(arr, key) >= 0;
    }

}
