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
        }
    }
}