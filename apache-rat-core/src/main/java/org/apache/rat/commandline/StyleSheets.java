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
package org.apache.rat.commandline;

import org.apache.commons.io.function.IOSupplier;
import org.apache.rat.Report;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * The enumeration of system defined stylesheets.
 */
public enum StyleSheets {
    /**
     * The plain style sheet.  The default.
     */
    PLAIN("plain-rat", "The default style"),
    /**
     * The missing header report style sheet
     */
    MISSING_HEADERS("missing-headers", "Produces a report of files that are missing headers"),
    /**
     * The unapproved licenses report
     */
    UNAPPROVED_LICENSES("unapproved-licenses", "Produces a report of the files with unapproved licenses"),
    /**
     * The plain style sheet.  The default.
     */
    XML("xml", "The default style");
    /**
     * The name of the style sheet.  Must map to bundled resource xslt file
     */
    private final String name;
    /**
     * The description of the style sheet
     */
    private final String desc;

    /**
     * Constructor.
     * @param name the name of the xslt file.
     * @param description What this xslt produces.
     */
    StyleSheets(final String name, final String description) {
        this.name = name;
        this.desc = description;
    }

    /**
     * Get the IOSupplier for a style sheet.
     * @param style the styles sheet to get the IOSupplier for.
     * @return an IOSupplier for the sheet.
     */
    public IOSupplier<InputStream> getStyleSheet() {
        return Objects.requireNonNull(StyleSheets.class.getClassLoader().getResource(String.format("org/apache/rat/%s.xsl", name)),
                "missing stylesheet: "+name)::openStream;
    }

    /**
     * Get the IOSupplier for a style sheet.
     * @param name the short name for or the path to a style sheet.
     * @return the IOSupplier for the style sheet.
     */
    public static IOSupplier<InputStream> getStyleSheet(final String name) {
        URL url = StyleSheets.class.getClassLoader().getResource(String.format("org/apache/rat/%s.xsl", name));
        return url == null
                ? () -> Files.newInputStream(Paths.get(name))
                : url::openStream;
    }

    /**
     * Gets the name of the xslt file.
     * @return the name of the xslt file, without the extension.
     */
    public String arg() {
        return name;
    }

    /**
     * Gets the description of the xslt file.
     * @return the description of the xslt file.
     */
    public String desc() {
        return desc;
    }
}
