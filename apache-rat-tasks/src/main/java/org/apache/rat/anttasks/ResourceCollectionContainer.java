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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.SortedSet;

import org.apache.rat.api.Document;
import org.apache.rat.api.RatException;
import org.apache.rat.document.impl.FileDocument;
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
    private final ResourceCollection rc;

    ResourceCollectionContainer(ResourceCollection rc) {
        this.rc = rc;
    }

    @Override
    public void run(RatReport report) throws RatException {
        ResourceDocument document = null;
        for (Resource r : rc) {
            if (!r.isDirectory()) {
                document = new ResourceDocument(r);
                report.report(document);
            }
        }
    }

    private static class ResourceDocument extends Document {

        private final Resource resource;

        private static String asName(Resource resource) {
            return resource instanceof FileResource ?
                    FileDocument.normalizeFileName(((FileResource) resource).getFile())
                    : resource.getName();
        }


        private ResourceDocument(Resource resource) {
            super(asName(resource));
            this.resource = resource;
        }

        @Override
        public Reader reader() throws IOException {
            final InputStream in = resource.getInputStream();
            return new InputStreamReader(in, StandardCharsets.UTF_8);
        }

        @Override
        public boolean isDirectory() {
            if (resource instanceof FileResource) {
                final FileResource fileResource = (FileResource) resource;
                final File file = fileResource.getFile();
                return file.isDirectory();
            }
            return false;
        }

        @Override
        public SortedSet<Document> listChildren() {
            if (resource instanceof FileResource) {
                final FileResource fileResource = (FileResource) resource;
                return new FileDocument(fileResource.getFile()).listChildren();
            }
            return Collections.emptySortedSet();
        }

        @Override
        public InputStream inputStream() throws IOException {
            return resource.getInputStream();
        }
    }
}
