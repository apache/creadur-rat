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

import org.apache.rat.DeprecationReporter;
import org.apache.rat.license.ILicenseFamily;

/**
 * Creates a License Family
 * @deprecated use configuration file.
 */
@Deprecated
@DeprecationReporter.Info(since = "0.17", forRemoval = true, use = "Configuration file and <config> element")// since 0.17
public class Family {

    private final ILicenseFamily.Builder builder = ILicenseFamily.builder();

    public ILicenseFamily build() {
        return builder.build();
    }

    public void setId(String id) {
        builder.setLicenseFamilyCategory(id);
    }

    public void setName(String name) {
        builder.setLicenseFamilyName(name);
    }
}
