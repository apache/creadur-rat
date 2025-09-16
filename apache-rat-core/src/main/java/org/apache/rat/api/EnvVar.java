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

package org.apache.rat.api;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.rat.utils.Log;

public enum EnvVar {
    /**
     * Ignore Git Global ignore file.
     */
    RAT_NO_GIT_GLOBAL_IGNORE("Ignore global ignore file when processing GIT ignore files."),
    /**
     * Log matcher decomposition when used.
     */
    RAT_DECOMPOSE_MATCHER_ON_USE("Log the decomposition of matchers on use. Messages will be logged at the minimum log level currently enabled."),
    /**
     * Defines the XDG home.
     */
    XDG_CONFIG_HOME("Where user-specific configurations are written. Generally defaults to $HOME/.config. " +
            "Only needs to be set if it has been changed on the system."),
    /**
     * Defines the user HOME.
     */
    HOME("The home directory"),
    /**
     * Sets the log level for the DefaultLog when it is constructed.
     */
    RAT_DEFAULT_LOG_LEVEL("The default log level for the command line logger. Should be one of: " +
            Arrays.stream(Log.Level.values()).map(Log.Level::name).collect(Collectors.joining(", ")));
    /**
     * The documentation for this EnvVar.
     */
    private final String documentation;

    /**
     * Constructor.
     * @param documentation the documentation for the EnvVar.
     */
    EnvVar(final String documentation) {
        this.documentation = documentation;
    }

    /**
     * Gets the documentation for this EnvVar.
     * @return the documentation for this EnvVar.
     */
    public String documentation() {
        return documentation;
    }

    /**
     * Determines if this EnvVar is set.
     * @return {@code true} if the EnvVar is set.
     */
    public boolean isSet() {
        return getValue() != null;
    }

    /**
     * Gets the value for the EnvVar.
     * @return the value for this EnvVar or {@code null} if not set.
     */
    public String getValue() {
        return System.getenv(name());
    }
}
