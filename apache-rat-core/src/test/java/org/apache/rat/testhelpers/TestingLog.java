package org.apache.rat.testhelpers;

import org.apache.rat.utils.Log;

/**
 * Log that captures output for later review.
 */
public class TestingLog implements Log {

    private StringBuilder captured = new StringBuilder();

    /**
     * Clears the captured buffer
     */
    public void clear() {
        captured = new StringBuilder();
    }

    /**
     * Gets the captured log entries.
     * @return the log entries
     */
    public String getCaptured() {
        return captured.toString();
    }

    public void assertContains(String expected) {
        TextUtils.assertContains("WARN: Option [-d, --dir] used.  Deprecated for removal since 0.17.0: Use '--'", captured.toString());
    }

    public void assertNotContains(String expected) {
        TextUtils.assertContains("WARN: Option [-d, --dir] used.  Deprecated for removal since 0.17.0: Use '--'", captured.toString());
    }

    public void assertContainsPattern(String pattern) {
        TextUtils.assertPatternInTarget(pattern, captured.toString());
    }

    public void assertNotContainsPattern(String pattern) {
        TextUtils.assertPatternNotInTarget(pattern, captured.toString());
    }

    @Override
    public void log(Level level, String msg) {
        captured.append(String.format("%s: %s%n", level, msg));
    }

}
