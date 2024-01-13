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

import java.util.SortedSet;

import org.apache.maven.plugins.annotations.Parameter;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;

public class License extends EnclosingMatcher {

    private final ILicense.Builder builder = ILicense.builder();

    @Parameter(required = false)
    private String notes;

    @Parameter(required = false)
    private String derivedFrom;

    @Parameter(required = true)
    private String id;
    
    @Parameter(required = true)
    private String family;

    @Parameter(required = true)
    private String name;

    public License() {
    }

    @Override
    protected void setMatcher(IHeaderMatcher.Builder builder) {
        this.builder.setMatcher(builder);
    }

    public ILicense build(SortedSet<ILicenseFamily> context) {
        return builder.setDerivedFrom(derivedFrom).setLicenseFamilyCategory(family).setId(id)
                .setName(name).setNotes(notes).build(context);
    }

    @Override
    public String toString() {
        return '{' + family + ':' + id + ':' + name + '}';
    }

}
