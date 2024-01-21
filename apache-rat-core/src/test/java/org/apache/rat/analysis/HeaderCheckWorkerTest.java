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

package org.apache.rat.analysis;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.StringReader;

import org.apache.rat.api.Document;
import org.apache.rat.license.ILicense;
import org.apache.rat.testhelpers.TestingLicense;
import org.apache.rat.testhelpers.TestingLocation;
import org.junit.jupiter.api.Test;


public class HeaderCheckWorkerTest {

    @Test
    public void isFinished() throws Exception {
        final Document subject = new TestingLocation("subject");
        ILicense matcher = new TestingLicense();
        HeaderCheckWorker worker = new HeaderCheckWorker(new StringReader(""), matcher, subject);
        assertFalse(worker.isFinished());
        worker.read();
        assertTrue(worker.isFinished());
    }
}
