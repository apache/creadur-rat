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

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.function.Supplier;

import org.apache.rat.utils.DefaultLog;

import static java.lang.String.format;

/**
 * A PatternMatcher that will emit trace messages during execution if the environment variable is set.
 * Also provides a printable name for general logging
 */
public class TracablePathMatcher implements PathMatcher {
    /** The envrionment variable to set to enable tracing for the PatternMatcher execution */
    public static final String ENV_VAR = "TracablePathMatcher";
    /** The delegate that will do all the work */
    private final PathMatcher delegate;
    /** The {@code true} if the environment variable has been set to "true" */
    private static final boolean TRACE_ENABLED = Boolean.valueOf(System.getenv(ENV_VAR));
    /** The supplier for the name */
    protected final Supplier<String> name;
    /** A TracablePathMatcher that alwasys return {@code true} */
    public static final TracablePathMatcher TRUE = TracablePathMatcher.make(() -> "True", pth -> true);
    /** A TracablePathMatcher that alwasys return {@code false} */
    public static final TracablePathMatcher FALSE = TracablePathMatcher.make(() -> "False", pth -> false);

    /**
     * Creates a TracablePathMatcher from a PathMatcher.
     * If the {@code delegate} is already a TracablePathMatcher it will be returned un modified.
     * @param name the name of the tracable path matcher.
     * @param delegate the PathMatcher that will actually do the work.
     * @return the new TracablePathMatcher
     */
    static TracablePathMatcher make(final Supplier<String> name, final PathMatcher delegate) {
        if (delegate instanceof TracablePathMatcher) {
            return (TracablePathMatcher) delegate;
        }
        return new TracablePathMatcher(name, delegate);
    }

    /**
     * Creates a tracable patter nmatcher with the specified name.
     * @param name the nmae of the pattern matcher.
     * @param delegate the delegate that actually does the work.
     */
    TracablePathMatcher(final Supplier<String> name, final PathMatcher delegate) {
        this.name = name;
        this.delegate = delegate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name.get());
        if (delegate instanceof TracablePathMatcher) {
            sb.append(" ").append(delegate.toString());
        }
        return sb.toString();
    }

    @Override
    public boolean matches(final Path path) {
        boolean result = delegate.matches(path);
        if (TRACE_ENABLED) {
            DefaultLog.getInstance().debug(format("{%s} %s %s", path, result, toString()));
        }
        return result;
    }
}
