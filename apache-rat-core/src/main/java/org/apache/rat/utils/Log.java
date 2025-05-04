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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * The definition of logging for the core. UIs are expected to provide an implementation of
 * Log to log data to the appropriate system within the UI.
 */
public interface Log {
    /**
     * The log levels supported by logging.
     */
    enum Level {
        // these must be listed in order of decreasing noisiness.
        /** Log debug only. */
        DEBUG,
        /** Log info only. */
        INFO,
        /** Log warn only. */
        WARN,
        /** Log error only. */
        ERROR,
        /** Log nothing. */
        OFF
    }

    static String formatLogEntry(final String message, final Throwable throwable) {
        StringWriter writer = new StringWriter();
        PrintWriter pWriter = new PrintWriter(writer);
        pWriter.print(message);
        pWriter.print(System.lineSeparator());
        throwable.printStackTrace(pWriter);
        return writer.toString();
    }

    /**
     * Gets the log level that is enabled. If encapsulated logger does not report level
     * implementations should return DEBUG.
     * @return the level that is enabled.
     */
    Level getLevel();

    /**
     * Sets the log level.
     * Implementations may elect not to set the level dynamically. However, if the option is supported
     * this method should be overridden.
     * @param level the level to set.
     */
    default void setLevel(Level level) {
        warn(String.format("This logger does not support dynamically setting the log level. Setting to %s ignored.", level));
    }

    /**
     * Determines if the log level is enabled.
     * @param level The level to check.
     * @return true if the level will produce output in the log.
     */
    default boolean isEnabled(Level level) {
        return getLevel().ordinal() <= level.ordinal();
    }

    /**
     * Writes a message at a specific log level.
     * @param level The log level to write at.
     * @param message the message to write.
     */
    void log(Level level, String message);

    /**
     * Writes a log message at the specified level.
     * @param level the level to write the message at.
     * @param message the message to write.
     */
    default void log(Level level, Object message) {
        log(level, message == null ? "NULL" : message.toString());
    }

    /**
     * Write a message at DEBUG level.
     * @param message the message to write.
     */
    default void debug(Object message) {
        log(Level.DEBUG, message);
    }

    /**
     * Write a message at INFO level.
     * @param message the message to write.
     */
    default void info(Object message) {
        log(Level.INFO, message);
    }

    /**
     * Write a message at WARN level.
     * @param message the message to write.
     */
    default void warn(Object message) {
        log(Level.WARN, message);
    }

    /**
     * Write a message at ERROR level.
     * @param message the message to write.
     */
    default void error(Object message) {
        log(Level.ERROR, message);
    }

    /**
     * Write a log message and report throwable stack trace at the specified log level.
     * @param level the level to report at
     * @param message the message for the log
     * @param throwable the throwable
     */
    default void log(Level level, String message, Throwable throwable) {
        log(level, formatLogEntry(message, throwable));
    }

    /**
     * Write a log message and report throwable stack trace at the specified log level.
     * @param level the level to report at
     * @param message the message for the log
     * @param throwable the throwable
     */
    default void log(Level level, Object message, Throwable throwable) {
        log(level, message == null ? "NULL" : message.toString(), throwable);
    }

    /**
     * Write a debug message and report throwable stack trace.
     * @param message the message for the log
     * @param throwable the throwable
     */
    default void debug(Object message, Throwable throwable) {
        log(Level.DEBUG, message, throwable);
    }

    /**
     * Write an info message and report throwable stack trace.
     * @param message the message for the log
     * @param throwable the throwable
     */
    default void info(Object message, Throwable throwable) {
        log(Level.INFO, message, throwable);
    }

    /**
     * Write a warn message and report throwable stack trace.
     * @param message the message for the log
     * @param throwable the throwable
     */
    default void warn(Object message, Throwable throwable) {
        log(Level.WARN, message, throwable);
    }

    /**
     * Write an error message and report throwable stack trace.
     * @param message the message for the log
     * @param throwable the throwable
     */
    default void error(Object message, Throwable throwable) {
        log(Level.ERROR, message, throwable);
    }

    /**
     * Returns a Writer backed by this log. All messages are logged at "INFO" level.
     * @return the Writer backed by this log.
     */
    default Writer asWriter() {
        return asWriter(Level.INFO);
    }

    /**
     * Returns a Writer backed by this log. All messages are logged at "INFO" level.
     * @return the Writer backed by this log.
     * @param level the Log level to write at.
     */
    default Writer asWriter(Level level) {
        return new Writer() {
            private StringBuilder sb = new StringBuilder();

            @Override
            public void write(final char[] cbuf, final int off, final int len) {
                String txt = String.copyValueOf(cbuf, off, len);
                int pos = txt.indexOf(System.lineSeparator());
                if (pos == -1) {
                    sb.append(txt);
                } else {
                    while (pos > -1) {
                        Log.this.log(level, sb.append(txt, 0, pos).toString());
                        sb.delete(0, sb.length());
                        txt = txt.substring(pos + 1);
                        pos = txt.indexOf(System.lineSeparator());
                    }
                    sb.append(txt);
                }
            }

            @Override
            public void flush() {
                if (sb.length() > 0) {
                    Log.this.log(level, sb.toString());
                }
                sb = new StringBuilder();
            }

            @Override
            public void close() {
                flush();
            }
        };
    }

}
