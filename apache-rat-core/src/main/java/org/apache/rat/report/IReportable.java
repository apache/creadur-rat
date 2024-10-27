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
package org.apache.rat.report;

import org.apache.rat.api.RatException;
import org.apache.rat.document.DocumentName;

public interface IReportable {
    /**
     * Adds the reportable to the RatReport.
     * @param report the report to add the results to.
     * @throws RatException on error.
     */
    void run(RatReport report) throws RatException;

    /**
     * Returns the DocumentName for the reportable.
     * @return the DocumentName for the reportable.
     */
    DocumentName getName();
}
