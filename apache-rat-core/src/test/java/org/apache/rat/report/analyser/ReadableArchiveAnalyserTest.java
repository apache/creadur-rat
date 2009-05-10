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

import junit.framework.TestCase;

import org.apache.rat.document.MockArchiveDocument;
import org.apache.rat.document.MockDocument;
import org.apache.rat.document.MockDocumentCollection;
import org.apache.rat.report.claim.IClaim;
import org.apache.rat.report.claim.impl.ArchiveFileTypeClaim;
import org.apache.rat.report.claim.impl.xml.MockClaimReporter;

public class ReadableArchiveAnalyserTest extends TestCase {
    private final AbstractSingleClaimAnalyser newAnalyser() {
        final MockClaimReporter reporter = new MockClaimReporter();
        return (AbstractSingleClaimAnalyser) DefaultAnalyserFactory.createArchiveTypeAnalyser(reporter);
    }

    public void testUnreadableArchiveToObject() throws Exception {
        MockDocument document = new MockDocument();
        final IClaim claim = newAnalyser().toClaim(document);
        assertTrue("Expected ArchiveFileTypeClaim", claim instanceof ArchiveFileTypeClaim);
        assertFalse("Expected unreadable archive", ((ArchiveFileTypeClaim) claim).isReadable());
    }

    public void testReadableArchiveToObject() throws Exception {
        MockArchiveDocument document = new MockArchiveDocument("whatever", new MockDocumentCollection());
        final IClaim claim = newAnalyser().toClaim(document);
        assertTrue("Expected ArchiveFileTypeClaim", claim instanceof ArchiveFileTypeClaim);
        assertTrue("Expected readable archive", ((ArchiveFileTypeClaim) claim).isReadable());
    }
}
