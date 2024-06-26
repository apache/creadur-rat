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

/**
 * The definition of logging for the core.  UIs are expected to provide an implementation of 
 * Log to log data to the appropriate system within the UI.
 */
public interface Log {
    /**
     * The log levels supported by logging.
     */
    enum Level {
        // these must be listed in order of decreasing noisiness.
        /**
         * Log debug only.
         */
        DEBUG,
        /**
         * Log info only.
         */
        INFO,
        /**
         * Log warn only.
         */
        WARN,
        /**
         * Log error only.
         */
        ERROR,
       /**
        * Log nothing.
        */
       OFF};

    /**
     * Writes a message at a specific log level.
     * @param level The log level to write at.
     * @param message the Message to write.
     */
    void log(Level level, String message);
    
    /**
     * Write a log message at the specified level.
     * @param level the level to write the message at.
     * @param message the mesage to write.
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
        StringWriter writer = new StringWriter(500);
        PrintWriter pWriter = new PrintWriter(writer);
        pWriter.print(message);
        pWriter.print(System.lineSeparator());
        throwable.printStackTrace(pWriter);
        log(level, writer.toString());
    }
    
    /**
     * Write a log message and report throwable stack trace at the specified log level.
     * @param level the level to report at
     * @param message the message for the log
     * @param throwable the throwable
     */
    default void log(Level level, Object message, Throwable throwable){
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
}
