///*
// * Licensed to the Apache Software Foundation (ASF) under one   *
// * or more contributor license agreements.  See the NOTICE file *
// * distributed with this work for additional information        *
// * regarding copyright ownership.  The ASF licenses this file   *
// * to you under the Apache License, Version 2.0 (the            *
// * "License"); you may not use this file except in compliance   *
// * with the License.  You may obtain a copy of the License at   *
// *                                                              *
// *   http://www.apache.org/licenses/LICENSE-2.0                 *
// *                                                              *
// * Unless required by applicable law or agreed to in writing,   *
// * software distributed under the License is distributed on an  *
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
// * KIND, either express or implied.  See the License for the    *
// * specific language governing permissions and limitations      *
// * under the License.                                           *
// */
//package org.apache.rat.license;
//
//import java.util.Objects;
//import java.util.SortedSet;
//
//import org.apache.rat.analysis.IHeaderMatcher;
//
///**
// * An implementation of the ILicense Builder.
// */
//public class ILicenseBuilder  {
//
//    private IHeaderMatcher.Builder matcher;
//
//    private String notes;
//
//    private String derivedFrom;
//    
//    private String name;
//    
//    private String id;
//
//    private final ILicenseFamily.Builder licenseFamily = ILicenseFamily.builder();
//
//    @Override
//    public Builder setMatcher(IHeaderMatcher.Builder matcher) {
//        this.matcher = matcher;
//        return this;
//    }
//    
//    @Override
//    public Builder setMatcher(IHeaderMatcher matcher) {
//        this.matcher = ()->matcher;
//        return this;
//    }
//
//    @Override
//    public Builder setNotes(String notes) {
//        this.notes = notes;
//        return this;
//    }
//
//    @Override
//    public Builder setId(String id) {
//        this.id = id;
//        return this;
//    }
//    
//    @Override
//    public Builder setDerivedFrom(String derivedFrom) {
//        this.derivedFrom = derivedFrom;
//        return this;
//    }
//
//    @Override
//    public Builder setLicenseFamilyCategory(String licenseFamilyCategory) {
//        this.licenseFamily.setLicenseFamilyCategory(licenseFamilyCategory);
//        this.licenseFamily.setLicenseFamilyName("License Family for searching");
//        return this;
//    }
//
//    @Override
//    public Builder setName(String name) {
//        this.name = name;
//        return this;
//    }
//
//    @Override
//    public ILicense build(SortedSet<ILicenseFamily> licenseFamilies) {
//        ILicenseFamily family = LicenseSetFactory.search(licenseFamily.build(), licenseFamilies);
//        Objects.requireNonNull(family, "License family may not be null.  Family "+licenseFamily.getCategory()+" not found.");
//        return new SimpleLicense(family, matcher.build(), derivedFrom, notes, name, id);
//    }
//}