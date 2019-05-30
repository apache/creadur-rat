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
package org.apache.rat.document.impl.util;

import org.apache.rat.document.IDocumentAnalyser;
import org.apache.rat.document.MockDocument;
import org.apache.rat.document.MockDocumentAnalyser;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DocumentAnalyserMultiplexerTest {

    private DocumentAnalyserMultiplexer multiplexer;
    private IDocumentAnalyser[] analysers;
    private MockDocument document;
    
    @Before
    public void setUp() {
        IDocumentAnalyser[] analysers = {
                new MockDocumentAnalyser(), 
                new MockDocumentAnalyser(),
                new MockDocumentAnalyser()
        };
        this.analysers = analysers;
        document = new MockDocument();
        multiplexer = new DocumentAnalyserMultiplexer(analysers);
    }

    @Test
    public void testAnalyse() throws Exception {
        multiplexer.analyse(document);
        MockDocumentAnalyser analyser =  (MockDocumentAnalyser) (analysers[0]);
        assertEquals("Call made to analyser", 1, analyser.matches.size());
        assertEquals("Call made to analyser", document, analyser.matches.get(0));
        analyser =  (MockDocumentAnalyser) (analysers[1]);
        assertEquals("Call made to analyser", 1, analyser.matches.size());
        assertEquals("Call made to analyser", document, analyser.matches.get(0));
        analyser =  (MockDocumentAnalyser) (analysers[2]);
        assertEquals("Call made to analyser", 1, analyser.matches.size());
        assertEquals("Call made to analyser", document, analyser.matches.get(0));
    }

}
