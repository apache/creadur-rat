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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.collections.EnumerationUtils;

import rat.document.IDocumentCollection;

public class ZipDocumentFactory {

    public static final IDocumentCollection load(File file) throws IOException {
        ZipFile zip = null;
        try {
            zip = new ZipFile(file);
            final Collection documents = new ArrayList();
            final Collection subdirectories = new ArrayList();

            final Collection directories = new ArrayList();
            final List entries = EnumerationUtils.toList(zip.entries());
            for (Iterator it=entries.iterator();it.hasNext();) {
                final ZipEntry entry = (ZipEntry) it.next();
                final boolean isDirectory = entry.isDirectory();
                final boolean topLevel = ZipUtils.isTopLevel(entry);
                if (isDirectory) {
                    ZipDirectory directory = createDirectory(entry, entries, directories, zip);
                    if (topLevel) {
                        subdirectories.add(directory);
                    }
                    directories.add(directory);
                } else if (topLevel){
                    documents.add(new ZipDocument(entry, zip));
                }
            }
            for (Iterator it=directories.iterator();it.hasNext();) {
                ZipDirectory directory = (ZipDirectory) it.next();
                directory.addSubDirectories(directories);
            }

            final IDocumentCollection result = new ZipRootDirectory(zip, subdirectories, documents);
            return result;
        } finally {
            if (zip != null) {
                zip.close();
            }
        }
    }

    private static ZipDirectory createDirectory(ZipEntry entry, 
            List entries, Collection directories, ZipFile zip) {
        final String stem = ZipUtils.getStem(entry);
        final Collection subdirectories = new ArrayList();
        final Collection documents = new ArrayList();
        
        for(Iterator it=entries.iterator();it.hasNext();) {
            ZipEntry otherEntry = (ZipEntry) it.next();
            if (!otherEntry.equals(entry))
            {
                final String otherStem = ZipUtils.getStem(otherEntry);
                if (stem.equals(otherStem)) {
                    if (otherEntry.isDirectory()) {
                        ZipDirectory subDirectory 
                            = createDirectory(otherEntry, entries,directories, zip);
                        subdirectories.add(subDirectory);
                        directories.add(subDirectory);
                    } else {
                        ZipDocument document = new ZipDocument(otherEntry, zip);
                        documents.add(document);
                    }
                }
            }
        }
        
        final ZipDirectory result = new ZipDirectory(entry, subdirectories, documents);
        return result;
    }
}
