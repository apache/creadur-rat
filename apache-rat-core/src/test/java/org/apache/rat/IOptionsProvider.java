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
package org.apache.rat;

/**
 * A list of methods that an OptionsProvider in a test case must support.
 * Use of this interface can ensures consistent testing across the UIs.  Each method
 * tests an Option from OptionCollection that must be implemented in the UI.
 * Each method in this interface tests an Option in OptionCollection.
 */
public interface IOptionsProvider {
    void addLicenseTest();

    void archiveTest();

    void standardTest();

    void copyrightTest();

    void dryRunTest();

    void excludeCliTest();

    void excludeCliFileTest();

    void forceTest();

    void licensesTest();

    void listLicensesTest();

    void listFamiliesTest();

    void noDefaultsTest();

    void outTest();

    void scanHiddenDirectoriesTest();

    void styleSheetTest();

    void xmlTest();
}
