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
package org.apache.rat.document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

public final class DocumentUtils {

    private static final Transformer TO_NAME_TRANSFORMER = new ToNameTransformer();
    
    public static final Transformer toNameTransformer() {
        return TO_NAME_TRANSFORMER;
    }
    
    /**
     * Collects all the documents contained in the collection tree.
     * @param documentCollection <code>IDocumentCollection</code>, not null
     * @return <code>Collection</code> of <code>IDocument</code>
     */
    public static final Collection documentsContained(IDocumentCollection documentCollection) {
        final Collection results = new ArrayList();
        addContents(documentCollection, results);
        return results;
    }
    
    /**
     * Depth first traversal.
     * @param documentCollection <code>IDocumentCollection</code>, not null
     * @param contents <code>Collection</code> of <code>IDocument</code>'s, not null
     */
    private static final void addContents(IDocumentCollection documentCollection, Collection contents) {
        for (Iterator it=documentCollection.subcollectionIterator();it.hasNext();) {
            IDocumentCollection subCollection = (IDocumentCollection) it.next();
            addContents(subCollection, contents);
        }
        CollectionUtils.addAll(contents, documentCollection.documentIterator());
    }
}
