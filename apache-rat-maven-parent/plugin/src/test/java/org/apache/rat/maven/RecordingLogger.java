package org.apache.rat.maven;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.maven.shared.invoker.InvokerLogger;
import org.apache.rat.utils.Log;

public class RecordingLogger implements InvokerLogger {
    public record LogEntry(Log.Level level, String message, Throwable exception) {}

    private final List<LogEntry> entries = new ArrayList<>();
    private final InvokerLogger delegate;

    public RecordingLogger(InvokerLogger delegate) {
        this.delegate = delegate;
    }

    @Override
    public void debug(String message) {
        entries.add(new LogEntry(Log.Level.DEBUG, message, null));
        delegate.debug(message);
    }

    @Override
    public void debug(String message, Throwable throwable) {
        entries.add(new LogEntry(Log.Level.DEBUG, message, throwable));
        delegate.debug(message, throwable);
    }

    @Override
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    @Override
    public void info(String message) {
        entries.add(new LogEntry(Log.Level.INFO, message, null));
        delegate.info(message);
    }

    @Override
    public void info(String message, Throwable throwable) {
        entries.add(new LogEntry(Log.Level.INFO, message, throwable));
        delegate.info(message, throwable);
    }

    @Override
    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    @Override
    public void warn(String message) {
        entries.add(new LogEntry(Log.Level.WARN, message, null));
        delegate.warn(message);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        entries.add(new LogEntry(Log.Level.WARN, message, throwable));
        delegate.warn(message, throwable);
    }

    @Override
    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }

    @Override
    public void error(String message) {
        entries.add(new LogEntry(Log.Level.ERROR, message, null));
        delegate.error(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        entries.add(new LogEntry(Log.Level.ERROR, message, throwable));
        delegate.error(message, throwable);
    }

    @Override
    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }

    @Override
    public void fatalError(String message) {
        entries.add(new LogEntry(Log.Level.ERROR, message, null));
        delegate.fatalError(message);
    }

    @Override
    public void fatalError(String message, Throwable throwable) {
        entries.add(new LogEntry(Log.Level.ERROR, message, throwable));
        delegate.fatalError(message);
    }

    @Override
    public boolean isFatalErrorEnabled() {
        return delegate.isFatalErrorEnabled();
    }

    @Override
    public void setThreshold(int threshold) {
        delegate.setThreshold(threshold);
    }

    @Override
    public int getThreshold() {
        return delegate.getThreshold();
    }

    public Stream<RecordingLogger.LogEntry> getEntries() {
        return entries.stream();
    }
}
