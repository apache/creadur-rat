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

import org.apache.rat.api.Document;
import org.apache.rat.api.MetaData;
import org.apache.rat.document.MockLocation;
import org.apache.rat.report.claim.impl.xml.MockClaimReporter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CopyrightHeaderTest {

    private static final String[] MATCHING_HEADERS =
            { "/*  Copyright 2012 FooBar.*/"
            , "/*  copyright 2012 foobar.*/"
            , "/*  Copyright 2012-2013 FooBar.*/" };
    private static final String[] NON_MATCHING_HEADERS =
            { "/*  Copyright*/"
            , "/*  Copyright FooBar.*/"
            , "/*  Copyright 2013*/"
            , "/*  Copyright 123a*/"
            , "/*  Copyright 123f oobar*/"
            , "/*  Copyright 2013FooBar*/"
            , "/*  Copyright 2012 2013 FooBar.*/" };

    private CopyrightHeader header;
    private MockClaimReporter reporter;
    private Document subject = new MockLocation("subject");

    @Before
    public void setUp() throws Exception {
        header = new CopyrightHeader(MetaData.RAT_LICENSE_FAMILY_CATEGORY_DATUM_ASL,MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_APACHE_LICENSE_VERSION_2_0,"","FooBar");
        reporter = new MockClaimReporter();
        subject = new MockLocation("subject");
    }

    @Test
    public void match() throws Exception {
        for (String line : MATCHING_HEADERS) {
            assertTrue("Copyright Header should be matched", header.match(subject, line));
            header.reset();
            assertFalse("After reset, content should build up again", header.match(subject, "New line"));
            header.reset();
        }
    }

    @Test
    public void noMatch() throws Exception {
        for (String line : NON_MATCHING_HEADERS) {
            assertFalse("Copyright Header shouldn't be matched", header.match(subject, line));
            header.reset();
            assertTrue("After reset, content should build up again", header.match(subject, MATCHING_HEADERS[0]));
            header.reset();
        }
    }
}
