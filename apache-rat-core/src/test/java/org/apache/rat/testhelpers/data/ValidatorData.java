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

import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.rat.ReportConfiguration;
import org.apache.rat.Reporter;
import org.apache.rat.document.DocumentName;

/**
 * The validator for test data.
 */
public class ValidatorData {
    /** The report output */
    public final Reporter.Output output;
    /** the report configuration */
    public final ReportConfiguration config;
    /** the base directory where the test setup was created */
    public final Path baseDir;

    /**
     * Constructor.
     * @param output The report output.
     * @param config the report configuration.
     * @param baseDir the directory where the test setup was created.
     */
    public ValidatorData(final Reporter.Output output, final ReportConfiguration config, final String baseDir) {
        this.output = output;
        this.config = config;
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
}
