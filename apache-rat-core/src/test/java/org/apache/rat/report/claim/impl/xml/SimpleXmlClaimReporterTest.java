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

import org.apache.rat.report.claim.BaseObject;
import org.apache.rat.report.claim.BasePredicate;
import org.apache.rat.report.claim.IObject;
import org.apache.rat.report.claim.IPredicate;
import org.apache.rat.report.claim.ISubject;
import org.apache.rat.report.claim.MockSubject;
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
        final ISubject subject = new MockSubject("subject");
        final IPredicate predicate = new BasePredicate("predicate");
        final IObject object = new BaseObject("object");
        reporter.claim(subject, predicate, object, false);
        assertEquals("Five calls made", 5, mockWriter.calls.size());
        assertTrue("First call is open element 'resource'", mockWriter.isOpenElement("resource", 0));
        assertTrue("Second call is name attribute", mockWriter.isAttribute("name", subject.getName(), 1));
        assertTrue("Third call is predicate element", mockWriter.isOpenElement(predicate.getName(), 2));
        assertTrue("Forth call is object attribute", mockWriter.isAttribute("name", object.getValue(), 3));    
        assertTrue("Fifth call is close element", mockWriter.isCloseElement(4));    
        final IPredicate predicateTwo = new BasePredicate("another-predicate");
        final IObject objectTwo = new BaseObject("another-object");
        reporter.claim(subject, predicateTwo, objectTwo, false);
        assertEquals("Another three calls made", 8, mockWriter.calls.size());
        assertTrue("Sixth call is predicate element", mockWriter.isOpenElement(predicateTwo.getName(), 5));
        assertTrue("Seventh call is object attribute", mockWriter.isAttribute("name", objectTwo.getValue(), 6));    
        assertTrue("Eighth call is close element", mockWriter.isCloseElement(7));    
    }

    public void testClaimsAboutTwoResource() throws Exception {
        final ISubject subject = new MockSubject("subject");
        final IPredicate predicate = new BasePredicate("predicate");
        final IObject object = new BaseObject("object");
        reporter.claim(subject, predicate, object, false);
        assertEquals("Five calls made", 5, mockWriter.calls.size());
        assertTrue("First call is open element 'resource'", mockWriter.isOpenElement("resource", 0));
        assertTrue("Second call is name attribute", mockWriter.isAttribute("name", subject.getName(), 1));
        assertTrue("Third call is predicate element", mockWriter.isOpenElement(predicate.getName(), 2));
        assertTrue("Forth call is object attribute", mockWriter.isAttribute("name", object.getValue(), 3));    
        assertTrue("Fifth call is close element", mockWriter.isCloseElement(4));    
        final ISubject subjectTwo = new MockSubject("another-subject");
        reporter.claim(subjectTwo, predicate, object, false);
        assertEquals("Another found calls made", 11, mockWriter.calls.size());
        assertTrue("Sixth call is close element", mockWriter.isCloseElement(5));  
        assertTrue("Seventh call is open element 'resource'", mockWriter.isOpenElement("resource", 6));
        assertTrue("Eighth call is name attribute", mockWriter.isAttribute("name", subjectTwo.getName(), 7));
        assertTrue("Nineth call is predicate element", mockWriter.isOpenElement(predicate.getName(), 8));
        assertTrue("Tenth call is object attribute", mockWriter.isAttribute("name", object.getValue(), 9));    
        assertTrue("Eleventh call is close element", mockWriter.isCloseElement(10));
    }

    public void testLiteralClaim() throws Exception {
        final ISubject subject = new MockSubject("subject");
        final IPredicate predicate = new BasePredicate("predicate");
        final IObject object = new BaseObject("object");
        reporter.claim(subject, predicate, object, true);
        assertEquals("Five calls made", 5, mockWriter.calls.size());
        assertTrue("First call is open element 'resource'", mockWriter.isOpenElement("resource", 0));
        assertTrue("Second call is name attribute", mockWriter.isAttribute("name", subject.getName(), 1));
        assertTrue("Third call is predicate element", mockWriter.isOpenElement(predicate.getName(), 2));
        assertTrue("Forth call is object content", mockWriter.isContent(object.getValue(), 3));    
        assertTrue("Fifth call is close element", mockWriter.isCloseElement(4));  
    }
}
