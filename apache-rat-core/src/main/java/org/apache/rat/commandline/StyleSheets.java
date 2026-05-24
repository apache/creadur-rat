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

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.apache.rat.ConfigurationException;
import org.apache.rat.ReportConfiguration;

import static java.lang.String.format;

/**
 * The enumeration of system defined stylesheets.
 */
public enum StyleSheets {
    /**
     * The plain style sheet. The current default.
     */
    PLAIN("plain-rat", "The default style."),
    /**
     * The missing header report style sheet.
     */
    MISSING_HEADERS("missing-headers", "Produces a report of files that are missing headers."),
    /**
     * The unapproved licenses report.
     */
    UNAPPROVED_LICENSES("unapproved-licenses", "Produces a report of the files with unapproved licenses."),
    /**
     * The pretty-printed XML style sheet.
     */
    XML("xml", "Produces output in pretty-printed XML."),
    /**
     * Official HTML5 stylesheet.
     */
    XHTML5("xhtml5", "Produces a HTML5 report");
    /**
     * The name of the style sheet. Must map to bundled resource XSLT file
     */
    private final String name;
    /**
     * The description of the style sheet
     */
    private final String desc;

    /**
     * Constructor.
     * @param name the name of the XSLT file.
     * @param description what this XSLT produces.
     */
    StyleSheets(final String name, final String description) {
        this.name = name;
        this.desc = description;
    }

    /**
     * Gets the IODescriptor for a style sheet.
     * @return an IODescriptor for the sheet.
     */
    public ReportConfiguration.IODescriptor<InputStream> getStyleSheet() {
        URL url = StyleSheets.class.getClassLoader().getResource(format("org/apache/rat/%s.xsl", name));
        Objects.requireNonNull(url, "missing stylesheet: " + name);
        return new ReportConfiguration.IODescriptor<>(name, url::openStream);
    }

    /**
     * Gets the IODescriptor for a style sheet.
     * @param name the short name for or the path to a style sheet.
     * @return the IODescriptor for the style sheet.
     */
    public static ReportConfiguration.IODescriptor<InputStream> getStyleSheet(final String name) {
        URL url = StyleSheets.class.getClassLoader().getResource(format("org/apache/rat/%s.xsl", name));
        if (url != null) {
            return new ReportConfiguration.IODescriptor<>(name, url::openStream);
        }
        Path p = Paths.get(name);
        if (p.toFile().exists()) {
            return new ReportConfiguration.IODescriptor<>(name, () -> Files.newInputStream(p));
        }
        throw new ConfigurationException(format("Stylesheet file '%s' not found: %s", name, xslt.getName()));
    }

    /**
     * Gets the name of the XSLT file.
     * @return the name of the XSLT file, without the extension.
     */
    public String arg() {
        return name;
    }

    /**
     * Gets the description of the XSLT file.
     * @return the description of the XSLT file.
     */
    public String desc() {
        return desc;
    }
}
