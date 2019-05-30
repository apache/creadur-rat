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

import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.api.RatException;
import org.apache.rat.document.impl.DocumentImplUtils;
import org.apache.rat.report.IReportable;
import org.apache.rat.report.RatReport;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;

/**
 * Implementation of IReportable that traverses over a resource
 * collection internally.
 */
class ResourceCollectionContainer implements IReportable {
    private final ResourceCollection rc;

    ResourceCollectionContainer(ResourceCollection rc) {
        this.rc = rc;
    }

    public void run(RatReport report) throws RatException {
        ResourceDocument document = new ResourceDocument();
        for (Resource r : rc) {
            if (!r.isDirectory()) {
                document.setResource(r);
                document.getMetaData().clear();
                report.report(document);
            }
        }
    }
    private class ResourceDocument implements Document {

        private Resource resource;
        private final MetaData metaData = new MetaData();

        private void setResource(Resource resource) {
            this.resource = resource;
        }
        
        public Reader reader() throws IOException {
            final InputStream in = resource.getInputStream();
            return new InputStreamReader(in);
        }

        public String getName() {
            // TODO: reconsider names
            String result = null;
            if (resource instanceof FileResource) {
                final FileResource fileResource = (FileResource) resource;
                final File file = fileResource.getFile();
                result = DocumentImplUtils.toName(file);
            } else {
                result = resource.getName();
            }
            return result;
        }

        public boolean isComposite() {
            if (resource instanceof FileResource) {
                final FileResource fileResource = (FileResource) resource;
                final File file = fileResource.getFile();
                return DocumentImplUtils.isZip(file);
            }
            return false;
        }
        
        public MetaData getMetaData() {
            return metaData;
        }
        
        public InputStream inputStream() throws IOException {
            return resource.getInputStream();
        }
    }
}
