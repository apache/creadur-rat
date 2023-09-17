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
package org.apache.rat.configuration;

import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;

public class ILicenseFamilyProxy implements ILicenseFamily {
    private final ILicense wrapped;

    static public ILicenseFamily create(ILicense license) {
        return (license instanceof ILicenseProxy) ? new ILicenseFamilyProxy(license) : license.getLicenseFamily();
    }

    private ILicenseFamilyProxy(ILicense license) {
        this.wrapped = license;
    }

    @Override
    public String getFamilyName() {
        return wrapped.getLicenseFamily().getFamilyName();
    }

    @Override
    public String getFamilyCategory() {
        return wrapped.getLicenseFamily().getFamilyCategory();
    }

}
