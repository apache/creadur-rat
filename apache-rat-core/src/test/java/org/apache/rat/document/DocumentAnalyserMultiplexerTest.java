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

import org.apache.rat.testhelpers.TestingDocument;
import org.apache.rat.testhelpers.TestingDocumentAnalyser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DocumentAnalyserMultiplexerTest {

    private DocumentAnalyserMultiplexer multiplexer;
    private IDocumentAnalyser[] analysers;
    private TestingDocument document;
    
    @BeforeEach
    public void setUp() {
        IDocumentAnalyser[] analysers = {
                new TestingDocumentAnalyser(),
                new TestingDocumentAnalyser(),
                new TestingDocumentAnalyser()
        };
        this.analysers = analysers;
        document = new TestingDocument();
        multiplexer = new DocumentAnalyserMultiplexer(analysers);
    }

    @Test
    public void testAnalyse() throws Exception {
        multiplexer.analyse(document);
        TestingDocumentAnalyser analyser =  (TestingDocumentAnalyser) (analysers[0]);
        assertEquals(1, analyser.matches.size(),"Call made to analyser");
        assertEquals( document, analyser.matches.get(0), "Call made to analyser");
        analyser =  (TestingDocumentAnalyser) (analysers[1]);
        assertEquals(1, analyser.matches.size(), "Call made to analyser");
        assertEquals(document, analyser.matches.get(0), "Call made to analyser");
        analyser =  (TestingDocumentAnalyser) (analysers[2]);
        assertEquals( 1, analyser.matches.size());
        assertEquals( document, analyser.matches.get(0),"Call made to analyser");
    }

}
