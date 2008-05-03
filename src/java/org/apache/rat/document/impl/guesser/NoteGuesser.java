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

import java.util.Arrays;
import java.util.List;

import rat.document.IDocument;
import rat.document.IDocumentMatcher;

public class NoteGuesser implements IDocumentMatcher {


    public static final String[] NOTE_FILE_NAMES = {
    "NOTICE", "LICENSE",
        "LICENSE.TXT", "NOTICE.TXT", 
        "INSTALL", "INSTALL.TXT",
        "README", "README.TXT", 
        "NEWS", "NEWS.TXT",
        "AUTHOR", "AUTHOR.TXT",
        "AUTHORS", "AUTHORS.txt",
        "CHANGELOG", "CHANGELOG.TXT",
        "DISCLAIMER", "DISCLAIMER.TXT",
        "KEYS", "KEYS.TXT",
        "RELEASE-NOTES", "RELEASE-NOTES.TXT",
        "RELEASE_NOTES", "RELEASE_NOTES.TXT",
        "UPGRADE", "UPGRADE.TXT",
        "STATUS", "STATUS.TXT",
        "THIRD_PARTY_NOTICES", "THIRD_PARTY_NOTICES.TXT",
        "COPYRIGHT", "COPYRIGHT.TXT",
        "BUILDING", "BUILDING.TXT",
    };
    public static final String[] NOTE_FILE_EXTENSIONS = {
        "LICENSE", "LICENSE.TXT",
        "NOTICE", "NOTICE.TXT",
    };
    
    /**
     * Is a file by that name a note file?
     */
    public static final boolean isNote(final String name) {
        if (name == null) {return false;}
    
        List l = Arrays.asList(NoteGuesser.NOTE_FILE_NAMES);
        String normalisedName = GuessUtils.normalise(name);
        
        boolean result = l.contains(name) || l.contains(normalisedName);
        for (int i = 0; !result && i < NoteGuesser.NOTE_FILE_EXTENSIONS.length; i++) {
            result = normalisedName.endsWith("." + NoteGuesser.NOTE_FILE_EXTENSIONS[i]);
        }
        return result;
    }

    public boolean matches(final IDocument document) {
        final String name = document.getName();
        final boolean result = isNote(name);
        return result;
    }

}
