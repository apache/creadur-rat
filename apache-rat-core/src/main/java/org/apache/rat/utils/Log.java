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
package org.apache.rat.utils;

/**
 * The definition of logging for the core.  UIs are expected to provide an implementation of 
 * Log to log data to the appropriate system within the UI.
 */
public interface Log {
    /**
     * The log levels supported by logging.
     */
    public enum Level {OFF, DEBUG, INFO, WARN, ERROR };

    /**
     * Writes a message at a specific log level.
     * @param level The log level to write at.
     * @param msg the Message to write.
     */
    void log(Level level, String msg);
}
