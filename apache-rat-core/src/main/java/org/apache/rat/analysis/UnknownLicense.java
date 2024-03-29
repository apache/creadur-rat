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

import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.ILicenseFamilyBuilder;

/**
 * An ILicense implementation that represents an unknown license.
 * <p>
 * The UnknownLicense is used during processing to report that a document license can not be determined.
 * </p>
 */
public class UnknownLicense implements ILicense {
    
    /**
     * The single instance of this class.
     */
    public static final UnknownLicense INSTANCE = new UnknownLicense();
    
    private final ILicenseFamily family ;
    
    /**
     * Do not allow other constructions.
     */
    private UnknownLicense() {
        family = new ILicenseFamilyBuilder().setLicenseFamilyCategory(ILicenseFamily.UNKNOWN_CATEGORY)
                .setLicenseFamilyName("Unknown license").build();
    }
    
    @Override
    public String getId() {
        return ILicenseFamily.UNKNOWN_CATEGORY;
    }

    @Override
    public void reset() {
        // do nothing
    }

    @Override
    public boolean matches(IHeaders headers) {
        return false;
    }

    @Override
    public int compareTo(ILicense arg0) {
        return getLicenseFamily().compareTo(arg0.getLicenseFamily());
    }

    @Override
    public ILicenseFamily getLicenseFamily() {
        return family;
    }

    @Override
    public String getNotes() {
        return null;
    }

    @Override
    public String getName() {
        return family.getFamilyName();
    }

    @Override
    public String derivedFrom() {
        return null;
    }
}
