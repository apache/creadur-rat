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
package org.apache.rat.report.analyser;

import rat.document.MockArchiveDocument;
import rat.document.MockDocument;
import rat.document.MockDocumentCollection;
import rat.report.claim.impl.xml.MockClaimReporter;
import junit.framework.TestCase;

public class ReadableArchiveAnalyserTest extends TestCase {

    MockClaimReporter reporter;
    ReadableArchiveAnalyser analyser;
    
    protected void setUp() throws Exception {
        super.setUp();
        reporter = new MockClaimReporter();
        analyser = new ReadableArchiveAnalyser(reporter);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testUnreadableArchiveToObject() throws Exception {
        MockDocument document = new MockDocument();
        assertEquals("Document is unreadable", ReadableArchiveAnalyser.UNREADABLE_ARCHIVE_VALUE, 
                analyser.toObject(document));
    }

    public void testReadableArchiveToObject() throws Exception {
        MockArchiveDocument document = new MockArchiveDocument("whatever", new MockDocumentCollection());
        assertEquals("Document is readable", ReadableArchiveAnalyser.READABLE_ARCHIVE_VALUE, 
                analyser.toObject(document));
    }
}
