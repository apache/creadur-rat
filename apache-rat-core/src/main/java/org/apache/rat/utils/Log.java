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
    public enum Level {
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
    
    default void log(Level level, Object message) {
        log(level, message == null ? "NULL" : message.toString());
    }
    
    default void debug(Object message) {
        log(Level.DEBUG, message);
    }

    default void info(Object message) {
        log(Level.INFO, message);
    }
    
    default void warn(Object message) {
        log(Level.WARN, message);
    }
    
    default void error(Object message) {
        log(Level.ERROR, message);
    }
    default void log(Level level, String message, Throwable t) {       
        StringWriter writer = new StringWriter(500);
        PrintWriter pWriter = new PrintWriter(writer);
        pWriter.print(message);
        pWriter.print(System.lineSeparator());
        t.printStackTrace(pWriter);
        log(level, writer.toString());
    }
    
    default void log(Level level, Object message, Throwable t){
        log(level, message == null ? "NULL" : message.toString(), t);
    }
    
    default void debug(Object message, Throwable t) {
        log(Level.DEBUG, message, t);
    }

    default void info(Object message, Throwable t) {
        log(Level.INFO, message, t);
    }
    
    default void warn(Object message, Throwable t) {
        log(Level.WARN, message, t);
    }
    
    default void error(Object message, Throwable t) {
        log(Level.ERROR, message, t);
    }
}
