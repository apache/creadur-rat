package org.apache.rat.config;

import org.apache.rat.api.MetaData;
import org.apache.rat.license.GPL1LicenseFamily;
import org.apache.rat.license.ILicenseFamily;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


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
public class ConfigurationUtilTest {
    @Test
    public void toNamesIsNullSafe() {
        assertTrue(ConfigurationUtil.toNames(null).isEmpty());
    }

    @Test
    public void toNamesIsEmptyArraySafe() {
        assertTrue(ConfigurationUtil.toNames(new ILicenseFamily[0]).isEmpty());
    }
    @Test
    public void toNamesRegular() {
        assertEquals(MetaData.RAT_LICENSE_FAMILY_NAME_VALUE_GPL_VERSION_1, ConfigurationUtil.toNames(new ILicenseFamily[]{new GPL1LicenseFamily()}).get(0));
    }
}
