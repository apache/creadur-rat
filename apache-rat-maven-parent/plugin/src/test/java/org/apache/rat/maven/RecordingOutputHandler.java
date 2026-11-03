/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.maven;

import org.apache.maven.shared.invoker.InvocationOutputHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * An output handler that records the output lines so they can be checked after the test run.
 */
public class RecordingOutputHandler implements InvocationOutputHandler {
    private final InvocationOutputHandler delegate;
    private final List<String> lines = new ArrayList<>();

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
