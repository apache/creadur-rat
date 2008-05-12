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
package org.apache.rat.document.impl.guesser;

import java.util.Locale;

import org.apache.rat.document.IDocument;
import org.apache.rat.document.IDocumentMatcher;

public class ArchiveGuesser implements IDocumentMatcher {
    
    public static final String[] ARCHIVE_EXTENSIONS = {
        "jar", "gz",
        "zip", "tar",
        "bz", "bz2",
        "rar", "war",
    };

    public boolean matches(final IDocument document) {
        
        final String name = document.getName();
        final boolean result = isArchive(name);
        return result;
    }

    /**
     * Is a file by that name an archive?
     */
    public static final boolean isArchive(final String name) {
        if (name == null) {return false;}
        String nameToLower = name.toLowerCase(Locale.US);
        boolean result = false;
        for (int i = 0; !result && i < ArchiveGuesser.ARCHIVE_EXTENSIONS.length; i++) {
            result = nameToLower.endsWith("." + ArchiveGuesser.ARCHIVE_EXTENSIONS[i]);
        }
        return result;
    }

}
