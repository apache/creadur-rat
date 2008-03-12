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
package rat.report.analyser;

import junit.framework.TestCase;
import rat.document.MockDocument;
import rat.report.claim.impl.xml.MockClaimReporter;
import rat.report.claim.impl.xml.MockClaimReporter.Claim;

public class ConstantClaimAnalyserTest extends TestCase {

    private static final String OBJECT = "OBJECT";
    private static final String PREDICATE = "PREDICATE";
    MockClaimReporter reporter;
    AbstractSingleClaimAnalyser analyser;
    
    protected void setUp() throws Exception {
        super.setUp();
        reporter = new MockClaimReporter();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAnalyseLiteral() throws Exception {
        analyser = new ConstantClaimAnalyser(reporter, PREDICATE, OBJECT, true);
        MockDocument document = new MockDocument();
        analyser.analyse(document);
        assertEquals("One claim per document", 1, reporter.claims.size());
        Claim claim = reporter.getClaim(0);
        assertEquals("Subject is name", document.name, claim.subject);
        assertEquals("Object is constant", OBJECT, claim.object);
        assertEquals("Predicate is constant", PREDICATE, claim.predicate);
        assertTrue("Constantly literal", claim.isLiteral);
        document.name = "A New Name";
        analyser.analyse(document);
        assertEquals("One claim per document", 2, reporter.claims.size());
        claim = reporter.getClaim(1);
        assertEquals("Subject is name", document.name, claim.subject);
        assertEquals("Object is constant", OBJECT, claim.object);
        assertEquals("Predicate is consant", PREDICATE, claim.predicate);
        assertTrue("Constantly literal", claim.isLiteral);
    }

    public void testAnalyseNotLiteral() throws Exception {
        analyser = new ConstantClaimAnalyser(reporter, PREDICATE, OBJECT, false);
        MockDocument document = new MockDocument();
        analyser.analyse(document);
        assertEquals("One claim per document", 1, reporter.claims.size());
        Claim claim = reporter.getClaim(0);
        assertEquals("Subject is name", document.name, claim.subject);
        assertEquals("Object is constant", OBJECT, claim.object);
        assertEquals("Predicate is constant", PREDICATE, claim.predicate);
        assertFalse("Constantly not literal", claim.isLiteral);
        document.name = "A New Name";
        analyser.analyse(document);
        assertEquals("One claim per document", 2, reporter.claims.size());
        claim = reporter.getClaim(1);
        assertEquals("Subject is name", document.name, claim.subject);
        assertEquals("Object is constant", OBJECT, claim.object);
        assertEquals("Predicate is consant", PREDICATE, claim.predicate);
        assertFalse("Constantly not literal", claim.isLiteral);
    }
}
