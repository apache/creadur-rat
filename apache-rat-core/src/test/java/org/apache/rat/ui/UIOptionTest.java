/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.ui;

import org.apache.commons.cli.Option;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UIOptionTest {
    private UIOption underTest;
    private UIOptionCollectionTest.TestingUIOptionCollection optionCollection;

    @Test
    void cleanup() {
        optionCollection = new UIOptionCollectionTest.TestingUIOptionCollection();
        underTest = new UIOptionCollectionTest.TestingUIOption(optionCollection, new Option("a", false, "An option"));
        String s = underTest.cleanup("The name is --output-licenses because I said so");
        assertThat(s).isEqualTo("The name is output.licenses because I said so");

        s = underTest.cleanup("The name is -A because I said so");
        assertThat(s).isEqualTo("The name is addLicense because I said so");
    }

}
