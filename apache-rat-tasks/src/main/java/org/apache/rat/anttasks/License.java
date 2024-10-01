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
package org.apache.rat.anttasks;

import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;

import java.util.SortedSet;

import org.apache.rat.analysis.IHeaderMatcher;

/**
 * Creates a License definition.
 * @deprecated use configuration file.
 */
@Deprecated // since 0.17
public class License {

    private final ILicense.Builder builder = ILicense.builder();
    
    ILicense.Builder asBuilder() {
        return builder;
    }

    public ILicense build(SortedSet<ILicenseFamily> context) {
        return builder.setLicenseFamilies(context).build();
    }

    public void setNotes(String notes) {
        builder.setNote(notes);
    }

    public void addNotes(String notes) {
        builder.setNote(notes);
    }
    

    public void setFamily(String licenseFamilyCategory) {
        builder.setFamily(licenseFamilyCategory);
    }
    
    public void setId(String id) {
        builder.setId(id);
    }

    public void setName(String name) {
        builder.setName(name);
    }

    public void add(IHeaderMatcher.Builder builder) {
        this.builder.setMatcher(builder);
    }
    
    public void add(IHeaderMatcher matcher) {
        this.builder.setMatcher(matcher);
    }
}
