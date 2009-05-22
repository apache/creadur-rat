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
package org.apache.rat.report.claim.impl.xml;

import junit.framework.TestCase;

import org.apache.rat.document.IDocument;
import org.apache.rat.document.MockLocation;
import org.apache.rat.report.xml.MockXmlWriter;

public class SimpleXmlClaimReporterTest extends TestCase {

    MockXmlWriter mockWriter;
    SimpleXmlClaimReporter reporter;
    
    protected void setUp() throws Exception {
        super.setUp();
        mockWriter = new MockXmlWriter();
        reporter = new SimpleXmlClaimReporter(mockWriter);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testClaimsAboutOneResource() throws Exception {
        final IDocument subject = new MockLocation("subject");
        final String predicate = "predicate";
        final String object = "object";
        reporter.report(subject);
        reporter.claim(new CustomClaim(subject, predicate, object, false));
        assertEquals("Five calls made", 5, mockWriter.calls.size());
        assertTrue("First call is open element 'resource'", mockWriter.isOpenElement("resource", 0));
        assertTrue("Second call is name attribute", mockWriter.isAttribute("name", subject.getName(), 1));
        assertTrue("Third call is predicate element", mockWriter.isOpenElement(predicate, 2));
        assertTrue("Forth call is object attribute", mockWriter.isAttribute("name", object, 3));    
        assertTrue("Fifth call is close element", mockWriter.isCloseElement(4));    
        final String predicateTwo = "another-predicate";
        final String objectTwo = "another-object";
        reporter.claim(new CustomClaim(subject, predicateTwo, objectTwo, false));
        assertEquals("Another three calls made", 8, mockWriter.calls.size());
        assertTrue("Sixth call is predicate element", mockWriter.isOpenElement(predicateTwo, 5));
        assertTrue("Seventh call is object attribute", mockWriter.isAttribute("name", objectTwo, 6));    
        assertTrue("Eighth call is close element", mockWriter.isCloseElement(7));    
    }

    public void testClaimsAboutTwoResource() throws Exception {
        final IDocument subject = new MockLocation("subject");
        final String predicate = "predicate";
        final String object = "object";
        reporter.report(subject);
        reporter.claim(new CustomClaim(subject, predicate, object, false));
        assertEquals("Five calls made", 5, mockWriter.calls.size());
        assertTrue("First call is open element 'resource'", mockWriter.isOpenElement("resource", 0));
        assertTrue("Second call is name attribute", mockWriter.isAttribute("name", subject.getName(), 1));
        assertTrue("Third call is predicate element", mockWriter.isOpenElement(predicate, 2));
        assertTrue("Forth call is object attribute", mockWriter.isAttribute("name", object, 3));    
        assertTrue("Fifth call is close element", mockWriter.isCloseElement(4));    
        final IDocument subjectTwo = new MockLocation("another-subject");
        reporter.report(subjectTwo);
        reporter.claim(new CustomClaim(subjectTwo, predicate, object, false));
        assertEquals("Another found calls made", 11, mockWriter.calls.size());
        assertTrue("Sixth call is close element", mockWriter.isCloseElement(5));  
        assertTrue("Seventh call is open element 'resource'", mockWriter.isOpenElement("resource", 6));
        assertTrue("Eighth call is name attribute", mockWriter.isAttribute("name", subjectTwo.getName(), 7));
        assertTrue("Nineth call is predicate element", mockWriter.isOpenElement(predicate, 8));
        assertTrue("Tenth call is object attribute", mockWriter.isAttribute("name", object, 9));    
        assertTrue("Eleventh call is close element", mockWriter.isCloseElement(10));
    }

    public void testLiteralClaim() throws Exception {
        final IDocument subject = new MockLocation("subject");
        final String predicate = "predicate";
        final String object = "object";
        reporter.report(subject);
        reporter.claim(new CustomClaim(subject, predicate, object, true));
        assertEquals("Five calls made", 5, mockWriter.calls.size());
        assertTrue("First call is open element 'resource'", mockWriter.isOpenElement("resource", 0));
        assertTrue("Second call is name attribute", mockWriter.isAttribute("name", subject.getName(), 1));
        assertTrue("Third call is predicate element", mockWriter.isOpenElement(predicate, 2));
        assertTrue("Forth call is object content", mockWriter.isContent(object, 3));    
        assertTrue("Fifth call is close element", mockWriter.isCloseElement(4));  
    }
}
