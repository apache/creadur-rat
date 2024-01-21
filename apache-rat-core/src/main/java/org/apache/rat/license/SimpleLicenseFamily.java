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
package org.apache.rat.license;

/**
 * An implementation of the ILicenseFamily.
 */
@Deprecated // remove in v1.0
public class SimpleLicenseFamily  {
    private String familyName;
    private String familyCategory;

    public SimpleLicenseFamily() {}

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }
    
    public void setFamilyCategory(String familyCategory) {
        this.familyCategory = familyCategory;
    }
    
    @Override
    public String toString() {
        return String.format("%s %s", getFamilyCategory(), getFamilyName());
    }


    public final String getFamilyName() {
        return familyName;
    }


    public String getFamilyCategory() {
        return familyCategory;
    }
}
