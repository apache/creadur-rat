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

import org.apache.rat.api.EnvVar;

/**
 * A default implementation of Log that writes to {@code System.out} and {@code System.err}.
 * <p>
 * The singleton instance is stored in a {@link ThreadLocal} so that each thread
 * (e.g. parallel Maven reactor threads) gets its own logger and does not
 * interfere with loggers set by other threads.
 * </p>
 */
public final class DefaultLog implements Log {
    /**
     * The per-thread instance of the default log.
     */
    private static final ThreadLocal<Log> INSTANCE = ThreadLocal.withInitial(DefaultLog::new);

    /**
     * Retrieves the DefaultLog instance for the current thread.
     * @return the Default log instance.
     */
    public static Log getInstance() {
        return INSTANCE.get();
    }

    /**
     * Sets the default log instance for the current thread.
     * If not set an instance of DefaultLog will be returned.
     * @param newInstance a Log to use as the default.
     * @return the old instance.
     */
    public static Log setInstance(final Log newInstance) {
        Log result = INSTANCE.get();
        INSTANCE.set(newInstance == null ? new DefaultLog() : newInstance);
        return result;
    }

    /**
     * Removes the log instance for the current thread, allowing the
     * {@link ThreadLocal} entry to be garbage-collected. Subsequent calls
     * to {@link #getInstance()} on this thread will return a fresh
     * {@code DefaultLog}.
     */
    public static void removeInstance() {
        INSTANCE.remove();
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
            level = EnvVar.RAT_DEFAULT_LOG_LEVEL.isSet() ?
                    Level.valueOf(EnvVar.RAT_DEFAULT_LOG_LEVEL.getValue().toUpperCase()) : Level.INFO;
        } catch (IllegalArgumentException e) {
            level = Level.INFO;
            log(Level.WARN, "Invalid log level set in environment: " + EnvVar.RAT_DEFAULT_LOG_LEVEL.getValue().toUpperCase(), e);
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
