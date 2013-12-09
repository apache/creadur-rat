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

import static org.apache.rat.api.domain.RatLicenseFamily.GPL3;

/**
 * Licence matches GPL3 or later.
 */
public class GPL3License extends FullTextMatchingLicense {

	/** The Constant FIRST_LICENSE_LINE. */
	public static final String FIRST_LICENSE_LINE = "This program is free software: you can redistribute it and/or modify\n"
			+ " it under the terms of the GNU General Public License as published by\n"
			+ " the Free Software Foundation, either version 3 of the License, or\n"
			+ " (at your option) any later version.";

	/**
	 * Instantiates a new gP l3 license.
	 */
	public GPL3License() {
		super(GPL3.licenseFamily(),
				FIRST_LICENSE_LINE);
	}
}
