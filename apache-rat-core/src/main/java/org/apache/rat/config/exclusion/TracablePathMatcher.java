/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.config.exclusion;

import org.apache.rat.utils.DefaultLog;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.function.Supplier;

import static java.lang.String.format;

public class TracablePathMatcher implements PathMatcher {
    private static boolean traceEnabled;

    private final PathMatcher delegate;
    protected final Supplier<String> name;

    public final static TracablePathMatcher TRUE = TracablePathMatcher.make(() -> "True", pth -> true );
    public final static TracablePathMatcher FALSE = TracablePathMatcher.make(() -> "False", pth -> false );

    public static void setTraceEnabled(boolean traceEnabled) {
        TracablePathMatcher.traceEnabled = traceEnabled;
    }

    static TracablePathMatcher make(Supplier<String> name, PathMatcher delegate) {
        if (delegate instanceof TracablePathMatcher) {
            return (TracablePathMatcher) delegate;
        }
        return new TracablePathMatcher(name, delegate);
    }

    TracablePathMatcher(Supplier<String> name, PathMatcher delegate) {
        this.name = name;
        this.delegate = delegate;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(name.get());
        if (delegate instanceof TracablePathMatcher) {
            sb.append(" ").append(delegate.toString());
        }
        return sb.toString();
    }

    @Override
    public boolean matches(Path path) {
        boolean result = delegate.matches(path);
        if (traceEnabled) {
            DefaultLog.getInstance().debug(format("{%s} %s %s", path, result, toString()));
        }
        return result;
    }
}
