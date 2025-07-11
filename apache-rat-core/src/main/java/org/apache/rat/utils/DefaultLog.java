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

import org.apache.commons.lang3.StringUtils;

/**
 * A default implementation of Log that writes to {@code System.out} and {@code System.err}.
 */
public final class DefaultLog implements Log {
    /**
     * The environment variable to set the default log level that RAT should log at.
     * If not specified INFO is used.
     * @see #setLevel(Level)
     */
    public static final String ENV_VAR = DefaultLog.class.getName();

    /**
     * The instance of the default log.
     */
    private static Log instance = new DefaultLog();

    /**
     * Retrieves teh DefaultLog instance.
     * @return the Default log instance.
     */
    public static Log getInstance() {
        return instance;
    }

    /**
     * Sets the default log instance.
     * If not set an instance of DefaultLog will be returned
     * @param newInstance a Log to use as the default.
     * @return the old instance.
     */
    public static Log setInstance(final Log newInstance) {
        Log result = instance;
        instance = newInstance == null ? new DefaultLog() : newInstance;
        return result;
    }

    /**
     * Creates a new instance of the default log.
     * @return A new instance of the default log.
     */
    public static Log createDefault() {
        return new DefaultLog();
    }

    /** The level at which we will write messages */
    private Level level;

    private DefaultLog() {
        try {
            level = StringUtils.isNotEmpty(System.getenv(ENV_VAR)) ?
                    Level.valueOf(System.getenv(ENV_VAR).toUpperCase()) : Level.INFO;
        } catch (IllegalArgumentException e) {
            level = Level.INFO;
            log(Level.WARN, "Invalid log level set in environment", e);
        }
    }

    /**
     * Sets the level. Log messages below the specified level will
     * not be written to the log.
     * @param level the level to use when writing messages.
     */
    public void setLevel(final Level level) {
        this.level = level;
    }

    /**
     * Gets the level we are writing at.
     * @return the level we are writing at.
     */
    public Level getLevel() {
        return level;
    }

    @Override
    public void log(final Level level, final String msg) {
        if (isEnabled(level)) {
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
                default:
                    break;
            }
        }
    }
}
