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

import org.apache.commons.collections4.Transformer;
import org.apache.rat.api.Document;

public class ToNameTransformer implements Transformer {

    private static final Transformer TO_NAME_TRANSFORMER = new ToNameTransformer();
    
    public static final Transformer toNameTransformer() {
        return TO_NAME_TRANSFORMER;
    }
    
    public Object transform(Object subject) {
        Object result = null;
        if (subject instanceof Document) {
            Document location = (Document) subject;
            result = location.getName();
        }
        return result;
    }
}
