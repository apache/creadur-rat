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
package org.apache.rat.analysis.license;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.api.Document;
import org.apache.rat.document.MockLocation;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.report.claim.impl.xml.MockClaimReporter;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MultiplexLicenseTest {

    private static final String LINE_ONE = "Line One";
    private static final String LINE_TWO = "Line Two";

    private IHeaderMatcher matcherOne;
    private IHeaderMatcher matcherTwo;

    private MultiplexLicense multiplexer;

    @Before
    public void setUp() {
        matcherOne = mock(IHeaderMatcher.class);
        matcherTwo = mock(IHeaderMatcher.class);
        multiplexer = new MultiplexLicense("multiplexMatcher Test", Arrays.<IHeaderMatcher>asList(matcherOne, matcherTwo));
    }

    @Test
    public void testMatcherLine() throws Exception {
        final Document document = mock(Document.class);
        
        multiplexer.match(document, LINE_ONE);
        verify(matcherOne, times(1)).match(eq(document), eq(LINE_ONE));
        verify(matcherTwo, times(1)).match(eq(document), eq(LINE_ONE));

        multiplexer.match(document, LINE_TWO);
        verify(matcherOne, times(1)).match(eq(document), eq(LINE_TWO));
        verify(matcherTwo, times(1)).match(eq(document), eq(LINE_TWO));
        verify(matcherOne, times(2)).match(eq(document), any());
        verify(matcherTwo, times(2)).match(eq(document), any());
    }


    @Test
    public void testReset() {
        multiplexer.reset();
        verify(matcherOne, times(1)).reset();
        verify(matcherTwo, times(1)).reset();
        
        multiplexer.reset();
        verify(matcherOne, times(2)).reset();
        verify(matcherTwo, times(2)).reset();
    }
    
    @Test
    public void testReportFamily() {
        Consumer<ILicenseFamily> consumer = mock(Consumer.class);
        multiplexer.reportFamily(consumer);
        verify(matcherOne, times(1)).reportFamily(eq(consumer));
        verify(matcherTwo, times(1)).reportFamily(eq(consumer));
    }
    
    @Test
    public void testExtractMatcher() {
        Predicate<ILicenseFamily> comparator = mock(Predicate.class);
        Consumer<IHeaderMatcher> consumer = mock(Consumer.class);

        multiplexer.extractMatcher(consumer, comparator);
        verify(matcherOne, times(1)).extractMatcher(eq(consumer),eq(comparator));
        verify(matcherTwo, times(1)).extractMatcher(eq(consumer),eq(comparator));
    }
}
