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

import static org.apache.rat.api.domain.RatLicenseFamily.DOJO;

/**
 * The Class DojoLicenseHeader.
 */
public class DojoLicenseHeader extends SimplePatternBasedLicense {

	/** The Constant LICENSE_URL. */
    private static final String LICENSE_URL  = "http://dojotoolkit.org/community/licensing.shtml";
    
    //  Copyright (c) 2004-2006, The Dojo Foundation
    // All Rights Reserved.
    //
    // Licensed under the Academic Free License version 2.1 or above OR the
    // modified BSD license. For more information on Dojo licensing, see:
    //
    //    http://dojotoolkit.org/community/licensing.shtml

	/**
	 * Instantiates a new dojo license header.
	 */
    public DojoLicenseHeader() {
		super(DOJO.licenseFamily(),
                new String[]{LICENSE_URL});
    }
}
