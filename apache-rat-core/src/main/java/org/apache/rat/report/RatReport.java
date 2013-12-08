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

import java.io.IOException;

import org.apache.rat.api.Document;
import org.apache.rat.api.RatException;

/**
 * The Interface RatReport.
 */
public interface RatReport {

	/**
	 * Start report.
	 * 
	 * @throws RatException
	 *             the rat exception
	 */
	void startReport() throws RatException;

	/**
	 * Report.
	 * 
	 * @param document
	 *            the document
	 * @throws RatException
	 *             the rat exception
	 */
	void report(Document document) throws IOException;

	/**
	 * End report.
	 * 
	 * @throws RatException
	 *             the rat exception
	 */
	void endReport() throws RatException;
}
