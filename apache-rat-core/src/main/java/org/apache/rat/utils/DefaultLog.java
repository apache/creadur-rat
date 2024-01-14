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
 * A default implementation of Log that writes to System.out and System.err
 */
public class DefaultLog implements Log {

    /**
     * The instance of the default log.
     */
    public static final DefaultLog INSTANCE = new DefaultLog();

    private DefaultLog() {
    }

    @Override
    public void log(Level level, String msg) {
        switch (level) {
        case DEBUG:
        case INFO:
        case WARN:
            System.out.format("%s: %s%n", level, msg);
            break;
        case ERROR:
            System.err.format("%s: %s%n", level, msg);
            break;
		case OFF:
			break;
		default:
			break;
        }
    }
}