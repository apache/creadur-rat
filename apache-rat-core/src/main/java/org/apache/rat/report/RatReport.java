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

import org.apache.rat.api.Document;
import org.apache.rat.api.RatException;

/**
 * Interface that defines a RatReport.
 * A RatReport examines a document and may report issues or modify the underlying file.
 */
public interface RatReport {

    /**
     * Signals the start of execution for the report. Will be called before the {@code report()} method
     * to ensure proper setup.
     * Default implementation does nothing.
     * @throws RatException on error.
     * @see #report(Document)
     */
    default void startReport() throws RatException {
    }

    /**
     * Performs the actual reporting on the given document.
     * Default implementation does nothing.
     * @param document write any reporting results into this document upon analysis.
     * @throws RatException on error.
     */
    default void report(Document document) throws RatException {
    };

    /**
     * Signals the end of execution for the report. Will be called after the {@code report()} method
     * to ensure proper cleanup.
     * Default implementation does nothing.
     * @throws RatException on error.
     * @see #report(Document)
     */
    default void endReport() throws RatException {
    }
}
