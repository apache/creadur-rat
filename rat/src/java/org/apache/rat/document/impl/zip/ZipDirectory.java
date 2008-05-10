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
package org.apache.rat.document.impl.zip;

import java.util.Collection;
import java.util.Iterator;
import java.util.zip.ZipEntry;

class ZipDirectory extends ZipDocumentCollection {

    private final ZipEntry entry;
    public ZipDirectory(final ZipEntry entry, final Collection subdirectorires, final Collection documents) {
        super(subdirectorires, documents);
        this.entry = entry;
    }

    public String getName() {
        return ZipUtils.getName(entry);
    }

    public String getURL() {
        return ZipUtils.getUrl(entry);
    }

    void addSubDirectories(Collection directories) {
        final String stem = getStem();
        for (Iterator it=directories.iterator();it.hasNext();) {
            ZipDirectory directory = (ZipDirectory) it.next();
            if (!directory.equals(this)) {
                final String otherStem = directory.getStem();
                if (stem.equals(otherStem)) {
                    add(directory);
                }
            }
        } 
    }

    public String getStem() {
        return ZipUtils.getStem(entry);
    }
}
