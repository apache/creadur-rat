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
package org.apache.rat.test;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.cli.Option;
import org.apache.rat.OptionCollection;

public final class OptionsList {
    private final static Map<String, Option> OPTIONS_MAP = new HashMap<String, Option>();

    static {
        for (Option option : OptionCollection.buildOptions().getOptions()) {
            if (option.getLongOpt() != null) {
                OPTIONS_MAP.put(option.getLongOpt(), option);
            }
        }
    }

    private OptionsList() {
        // do not instantiate.
    }

    public static Option getOption(String longOpt) {
        return OPTIONS_MAP.get(longOpt);
    }

    public static Set<String> getKeys() {
        return OPTIONS_MAP.keySet();
    }
}
