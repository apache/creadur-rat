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
package org.apache.rat.testhelpers.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.rat.ConfigurationException;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.Reporter;
import org.apache.rat.api.RatException;
import org.apache.rat.configuration.XMLConfig;
import org.apache.rat.configuration.XMLConfigurationWriter;
import org.apache.rat.document.DocumentName;
import org.apache.rat.report.claim.ClaimStatistic;
import org.apache.rat.report.xml.writer.XmlWriter;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static org.apache.rat.configuration.XMLConfigurationReader.nodeListConsumer;

/**
 * The validator for test data.
 */
public final class ValidatorData {
    /** The report output */
    private final Reporter.Output output;
    /** the base directory where the test setup was created */
    private final Path baseDir;

    /**
     * Constructor.
     * @param output The report output.
     * @param baseDir the directory where the test setup was created.
     */
    public ValidatorData(final Reporter.Output output, final String baseDir) {
        this.output = output;
        this.baseDir = Paths.get(baseDir);
    }

    /**
     * Gets the directory where the test setup was created as a DocumentName.
     * @return the DocumentName for the baseDir directory.
     */
    public DocumentName getBaseName() {
        return DocumentName.builder(baseDir.toFile()).build();
    }

    /**
     * Creates a DocumentName for a file name.  The root of the document Name will be the baseDir.
     * @param fileName the file name to create a document name for.
     * @return the Document name for the file.
     */
    public DocumentName mkDocName(String fileName) {
        return DocumentName.builder().setBaseName(baseDir.toFile()).setName(fileName).build();
    }

    public Reporter.Output getOutput() {
        return output;
    }
    /**
     * Gets the document that was generated during execution.
     * @return the document that was generated during execution.
     */
    public Document getDocument() {
        return output.getDocument();
    }

    /**
     * Gets the claim statistics from the run
     * @return the ClaimStatistic from the run.
     */
    public ClaimStatistic getStatistic() {
        return output.getStatistic();
    }

    /**
     * Gets the configuration from the run.
     * @return the configuration from the run.
     */
    public ReportConfiguration getConfiguration() {
        return output.getConfiguration();
    }

    public Path getBaseDir() {
        return baseDir;
    }

}
