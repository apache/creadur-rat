package org.apache.rat.config;

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

import org.apache.rat.license.ILicenseFamily;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationUtil {
    /**
     * Extract license names from the given license families.
     *
     * @param approvedLicenses list of license family implementations.
     * @return extracted names of given license families.
     */
    public static final List<String> toNames(final ILicenseFamily[] approvedLicenses) {
        List<String> names = new ArrayList<>();
        if (approvedLicenses != null && approvedLicenses.length > 0) {
            for (ILicenseFamily approvedFamily : approvedLicenses) {
                names.add(approvedFamily.getFamilyName());
            }
        }
        return names;
    }

}
