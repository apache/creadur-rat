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
package rat.report.claim;

import rat.report.RatReportFailedException;

public interface IClaimReporter {

    /**
     * States a claim.
     * @param subject subject of this claim, not null
     * @param predicate predicate claimed, not null
     * @param object object of this claim, not null
     * @param isLiteral <code>true</code> is the object of this claim is a literal,
     * <code>false</code> if the object of this claim is an identifier
     */
    public void claim(CharSequence subject, CharSequence predicate, CharSequence object, boolean isLiteral)
        throws RatReportFailedException;
}
