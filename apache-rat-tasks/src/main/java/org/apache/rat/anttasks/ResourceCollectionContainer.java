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
package org.apache.rat.anttasks;

import org.apache.rat.ReportConfiguration;
import org.apache.rat.api.RatException;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.FileDocument;
import org.apache.rat.report.IReportable;
import org.apache.rat.report.RatReport;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;

/**
 * Implementation of IReportable that traverses over a resource collection
 * internally.
 */
class ResourceCollectionContainer implements IReportable {
    /** The resources as collected by Ant */
    private final ResourceCollection resources;
    /** The report configuration being used for the report */
    private final ReportConfiguration configuration;
    /** The document name */
    private final DocumentName name;

    ResourceCollectionContainer(final DocumentName name, final ReportConfiguration configuration, final ResourceCollection resources) {
        this.resources = resources;
        this.configuration = configuration;
        this.name = name;
    }

    @Override
    public void run(final RatReport report) throws RatException {
        for (Resource r : resources) {
            if (r.isFilesystemOnly()) {
                FileResource fr = (FileResource) r;
                DocumentName dirName = new DocumentName(fr.getFile(), new DocumentName(fr.getProject().getBaseDir()));
                FileDocument document = new FileDocument(dirName, fr.getFile(), configuration.getNameMatcher(dirName));
                report.report(document);
            }
        }
    }

    @Override
    public DocumentName getName() {
        return name;
    }
}
