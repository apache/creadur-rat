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
package org.apache.rat.report.claim.util;

import junit.framework.TestCase;

import org.apache.rat.report.claim.BaseObject;
import org.apache.rat.report.claim.BasePredicate;
import org.apache.rat.report.claim.IClaimReporter;
import org.apache.rat.report.claim.IObject;
import org.apache.rat.report.claim.IPredicate;
import org.apache.rat.report.claim.ISubject;
import org.apache.rat.report.claim.MockSubject;
import org.apache.rat.report.claim.impl.xml.MockClaimReporter;

public class ClaimReporterMultiplexerTest extends TestCase {
    
    MockClaimReporter reporterOne;
    MockClaimReporter reporterTwo;
    MockClaimReporter reporterThree;
    ClaimReporterMultiplexer multiplexer;
    
    protected void setUp() throws Exception {
        super.setUp();
        reporterOne = new MockClaimReporter();
        reporterTwo = new MockClaimReporter();
        reporterThree = new MockClaimReporter();
        IClaimReporter[] reporters = {reporterOne, reporterTwo, reporterThree};
        multiplexer = new ClaimReporterMultiplexer(reporters);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testClaim() throws Exception {
        final ISubject subject = new MockSubject("subject");
        final IPredicate predicate = new BasePredicate("predicate");
        final ISubject otherSubject = new MockSubject("another subject");
        final IPredicate otherPredicate = new BasePredicate("another predicate");
        final IObject object = new BaseObject("object");
        final IObject otherObject = new BaseObject("another object");
        multiplexer.claim(subject, predicate, object, true);
        MockClaimReporter.Claim claimOne = new MockClaimReporter.Claim(subject, predicate, object, true);
        assertEquals("Claim reported", 1 , reporterOne.claims.size());
        assertEquals("Claim reported", claimOne, reporterOne.claims.get(0));
        assertEquals("Claim reported", 1 , reporterTwo.claims.size());
        assertEquals("Claim reported", claimOne, reporterTwo.claims.get(0));
        assertEquals("Claim reported", 1 , reporterThree.claims.size());
        assertEquals("Claim reported", claimOne, reporterThree.claims.get(0));
        multiplexer.claim(otherSubject, otherPredicate, otherObject, false);
        MockClaimReporter.Claim claimTwo = new MockClaimReporter.Claim(otherSubject, otherPredicate, otherObject, false);
        assertEquals("Claim reported", 2, reporterOne.claims.size());
        assertEquals("Claim reported", claimTwo, reporterOne.claims.get(1));
        assertEquals("Claim reported", 2, reporterTwo.claims.size());
        assertEquals("Claim reported", claimTwo, reporterTwo.claims.get(1));
        assertEquals("Claim reported", 2, reporterThree.claims.size());
        assertEquals("Claim reported", claimTwo, reporterThree.claims.get(1));
    }
}
