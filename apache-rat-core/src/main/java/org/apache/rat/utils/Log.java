package org.apache.rat.utils;

import java.util.function.Consumer;

/**
 * The definition of logging for the core.  UIs are expected to provide an implementation of 
 * Log to log data to the appropriate system within the UI.
 */
public interface Log {
    /**
     * The log levels supported by logging.
     */
    public enum Level {DEBUG, INFO, WARN, ERROR };

    /**
     * Writes a message at a specific log level.
     * @param level The log level to write at.
     * @param msg the Message to write.
     */
    void log(Level level, String msg);
}
