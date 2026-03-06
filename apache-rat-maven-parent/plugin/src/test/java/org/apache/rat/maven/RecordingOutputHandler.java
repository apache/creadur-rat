package org.apache.rat.maven;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.maven.shared.invoker.InvocationOutputHandler;

public class RecordingOutputHandler implements InvocationOutputHandler {
    private final InvocationOutputHandler delegate;
    private final List<String> lines = new ArrayList<String>();

    RecordingOutputHandler(final InvocationOutputHandler delegate) {
        this.delegate = delegate;
    }

    @Override
    public void consumeLine(String line) throws IOException {
        lines.add(line);
        delegate.consumeLine(line);
    }

    public Stream<String> getLines() {
        return lines.stream();
    }
}
