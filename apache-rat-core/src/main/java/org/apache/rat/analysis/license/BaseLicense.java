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

import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.api.Document;
import org.apache.rat.api.Reporter;
import org.apache.rat.api.MetaData;

public class BaseLicense {
	private final MetaData.Datum licenseFamilyCategory;
	private final MetaData.Datum licenseFamilyName;
	private final String notes;
	
	public BaseLicense(final MetaData.Datum licenseFamilyCategory, final MetaData.Datum licenseFamilyName, final String notes)
	{
		this.licenseFamilyCategory = licenseFamilyCategory;
		this.licenseFamilyName = licenseFamilyName;
		this.notes = notes;
	}
    
    public final void reportOnLicense(Document subject, Reporter reporter) throws RatHeaderAnalysisException {
        final MetaData metaData = subject.getMetaData();
        metaData.set(new MetaData.Datum(MetaData.RAT_URL_HEADER_SAMPLE, notes));
        metaData.set(new MetaData.Datum(MetaData.RAT_URL_HEADER_CATEGORY,licenseFamilyCategory.getValue()));
        metaData.set(licenseFamilyCategory);
        metaData.set(licenseFamilyName);
    }
}
