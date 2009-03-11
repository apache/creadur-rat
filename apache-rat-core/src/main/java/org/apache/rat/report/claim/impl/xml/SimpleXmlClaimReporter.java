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

import java.io.IOException;

import org.apache.rat.report.RatReportFailedException;
import org.apache.rat.report.claim.IClaimReporter;
import org.apache.rat.report.claim.IObject;
import org.apache.rat.report.claim.IPredicate;
import org.apache.rat.report.claim.ISubject;
import org.apache.rat.report.xml.writer.IXmlWriter;

public class SimpleXmlClaimReporter implements IClaimReporter {

    private static final String NAME = "name";
    private final IXmlWriter writer;
    private ISubject lastSubject;
    
    public SimpleXmlClaimReporter(final IXmlWriter writer) {
        this.writer = writer;
    }
    
    public void claim(ISubject subject, IPredicate predicate,
            IObject object, boolean isLiteral) throws RatReportFailedException {
        try {
            if (!(subject.equals(lastSubject))) {
                if (lastSubject != null) {
                    writer.closeElement();
                }
                writer.openElement("resource").attribute(NAME, subject.getName());
            }
            if (isLiteral) {
                writer.openElement(predicate.getName()).content(object.getValue()).closeElement();
            } else {
                writer.openElement(predicate.getName()).attribute(NAME, object.getValue()).closeElement();
            }
            lastSubject = subject;
        } catch (IOException e) {
            throw new RatReportFailedException("XML writing failure: " + e.getMessage() + " subject: " + subject + " predicate: " + predicate, e);
        }
    }

}
