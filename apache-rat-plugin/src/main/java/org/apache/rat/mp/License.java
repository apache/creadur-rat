/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.mp;

import org.apache.maven.plugins.annotations.Parameter;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.SimpleLicenseFamily;

public class License extends EnclosingMatcher implements ILicense {
    private ILicenseFamily family;

    private IHeaderMatcher matcher;

    @Parameter(required = false)
    private String notes;

    @Parameter(required = false)
    private String derivedFrom;

    @Parameter(required = true)
    private String licenseFamilyCategory;

    @Parameter(required = true)
    private String licenseFamilyName;

    public License() {
    }

    @Parameter(required = false)
    public void setNot(Not not) {
        setHolder(not);
    }

    @Override
    public ILicenseFamily getLicenseFamily() {
        if (family == null) {
            family = new SimpleLicenseFamily(licenseFamilyCategory, licenseFamilyName);
        }
        return family;
    }

    @Override
    public int compareTo(ILicense arg0) {
        return ILicense.getComparator().compare(this, arg0);
    }

    @Override
    public String getNotes() {
        return notes;
    }

    @Override
    public boolean matches(String line) {
        return matcher.matches(line);
    }

    @Override
    public String derivedFrom() {
        return derivedFrom;
    }

    @Override
    public String getId() {
        return matcher.getId();
    }

    @Override
    public void reset() {
        matcher.reset();
    }

    @Override
    protected void setHolder(Holder holder) {
        this.matcher = holder.getMatcher();
    }
    
    @Override
    public String toString() {
        return getLicenseFamily().toString();
    }
}
