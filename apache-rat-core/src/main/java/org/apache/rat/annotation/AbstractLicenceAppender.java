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
    private static final int TYPE_C      = 9;
    private static final int TYPE_H      = 10;
    private static final int TYPE_SH     = 11;
    private static final int TYPE_BAT    = 12;
    private static final int TYPE_VM     = 13;
    private static final int TYPE_SCALA = 14;

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

        if (type == TYPE_CSS 
            || type == TYPE_JAVASCRIPT 
            || type == TYPE_APT 
            || type == TYPE_PROPERTIES
            || type == TYPE_C
            || type == TYPE_H
            || type == TYPE_HTML
            || type == TYPE_VM
            || type == TYPE_SCALA) {
            writer.write(getLicenceHeader(document));
            writer.write('\n');
        }

        String line;
        boolean first = true;
        while ((line = br.readLine()) != null) {
            if (first && type == TYPE_PYTHON) {
                doFirstLine(document, writer, line, "#!/usr/bin");
            } else if (first && type == TYPE_BAT) {
                doFirstLine(document, writer, line, "@echo off");
            } else if (first && type == TYPE_SH) {
                doFirstLine(document, writer, line, "#!/bin");
            } else {
                writer.write(line);
                writer.write('\n');
            }

            if (type == TYPE_JAVA && line.startsWith("package ")) {
                writer.write(getLicenceHeader(document));
                writer.write('\n');
            }
            if (type == TYPE_XML && line.startsWith("<?xml ")) {
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
        switch (type) {
        case TYPE_JAVA: return "/*\n";
        case TYPE_C:    return "/*\n";
        case TYPE_H:    return "/*\n";
        case TYPE_XML: return "<!--\n";
        case TYPE_HTML: return "<!--\n";
        case TYPE_CSS: return "/*\n";
        case TYPE_JAVASCRIPT: return "/*\n";
        case TYPE_APT: return "~~\n";
        case TYPE_PROPERTIES: return "#\n";
        case TYPE_PYTHON:     return "#\n";
        case TYPE_SH:         return "#\n";
        case TYPE_BAT:        return "rem\n";
        case TYPE_SCALA: return "/*\n";
        default: return "";
        }
    }


    /**
     * Get the last line of the licence header formatted
     * for the given type of file.
     * 
     * @param type the type of file, see the TYPE_* constants
     * @return not null
     */
    protected String getLastLine(int type) {
        switch (type) {
        case TYPE_JAVA: return " */\n";
        case TYPE_C:    return " */\n";
        case TYPE_H:    return " */\n";
        case TYPE_XML: return "-->\n";
        case TYPE_HTML: return "-->\n";
        case TYPE_CSS: return " */\n";
        case TYPE_JAVASCRIPT: return " */\n";
        case TYPE_APT: return "~~\n";
        case TYPE_PROPERTIES: return "#\n";
        case TYPE_PYTHON:     return "#\n";
        case TYPE_SH:         return "#\n";
        case TYPE_BAT:        return "rem\n";
        case TYPE_SCALA: return " */\n";
        default: return "";
        }
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
        switch (type) {
        case TYPE_JAVA: return " *" + content + "\n";
        case TYPE_C:    return " *" + content + "\n";
        case TYPE_H:    return " *" + content + "\n";
        case TYPE_XML: return         content + "\n";
        case TYPE_HTML: return        content + "\n";
        case TYPE_CSS: return " *"  + content + "\n";
        case TYPE_JAVASCRIPT: return " *" + content + "\n";
        case TYPE_APT: return "~~" + content + "\n";
        case TYPE_PROPERTIES: return "#" + content + "\n";
        case TYPE_PYTHON:     return "#" + content + "\n";
        case TYPE_SH:         return "#" + content + "\n";
        case TYPE_BAT:        return "rem" + content + "\n";
        case TYPE_VM:         return "##" + content + "\n";
        case TYPE_SCALA: return " *" + content + "\n";
        default: return "";
        }
    }
}
