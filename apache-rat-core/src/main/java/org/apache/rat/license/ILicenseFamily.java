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

import java.util.SortedSet;

import org.apache.commons.lang3.builder.EqualsBuilder;

public interface ILicenseFamily extends Comparable<ILicenseFamily> {
    String getFamilyName();
    
    String getFamilyCategory();

    static String makeCategory(String cat) {
        return cat == null ? "     " : cat.concat("     ").substring(0, 5);
    }
    
    @Override
    default int compareTo(ILicenseFamily other) {
        return getFamilyCategory().compareTo(other.getFamilyCategory());
    }
    
//    does not work -- revisit if needed
//    static ILicenseFamily search(String licenseCategory, SortedSet<ILicenseFamily> licenses) {
//        ILicenseFamily target = new ILicenseFamily() {
//            @Override
//            public String getFamilyName() {
//                return licenseCategory;
//            }
//
//            @Override
//            public String getFamilyCategory() {
//                return "Searching family";
//            }
//        };
//        return search(target, licenses);
//    }
    
    static ILicenseFamily search(ILicenseFamily target, SortedSet<ILicenseFamily> licenses) {
        SortedSet<ILicenseFamily> part = licenses.tailSet(target);
        return (!part.isEmpty() && part.first().compareTo(target) == 0) ? part.first() : null;
    }
}
