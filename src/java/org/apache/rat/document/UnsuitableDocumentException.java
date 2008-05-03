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

/**
 * Indicates that the document was unsuitable for analysis.
 *
 */
public class UnsuitableDocumentException extends RatDocumentAnalysisException {

    private static final long serialVersionUID = 4202800209654402733L;

    public UnsuitableDocumentException() {
        super("This document is unsuitable for analysis");
    }

    public UnsuitableDocumentException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public UnsuitableDocumentException(String msg) {
        super(msg);
    }

    public UnsuitableDocumentException(Throwable cause) {
        super(cause);
    }

    
}
