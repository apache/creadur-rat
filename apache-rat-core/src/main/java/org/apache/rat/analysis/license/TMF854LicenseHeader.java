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

import static org.apache.rat.api.domain.RatLicenseFamily.TMF854;


/**
 * The Class TMF854LicenseHeader.
 */
public class TMF854LicenseHeader extends SimplePatternBasedLicense {
    
	/** The Constant COPYRIGHT_HEADER. */
    private static final String COPYRIGHT_HEADER = "TMF854 Version 1.0 - Copyright TeleManagement Forum";
    
    //  TMF854 Version 1.0 - Copyright TeleManagement Forum 

	/**
	 * Instantiates a new tM f854 license header.
	 */
    public TMF854LicenseHeader() {
		super(TMF854.licenseFamily(),
                new String[]{COPYRIGHT_HEADER});
    }
}
