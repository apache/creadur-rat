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
package org.apache.rat.api;

import java.io.Serial;

/**
 * This exception is used to indicate error conditions during RAT runs.
 */
public class RatException extends Exception {
    /**
     * Creates a {@code RatException} out of any other type of exception.
     * If the exception is already a {@code RatException}, just return it.
     * @param exception the exception to convert.
     * @return a {@code RatException}.
     */
    public static RatException makeRatException(final Exception exception) {
        return exception instanceof RatException ? (RatException) exception : new RatException(exception);
    }

    @Serial
    private static final long serialVersionUID = 4940711222435919034L;

    /**
     * Constructor.
     */
    public RatException() {
        super();
    }

    /**
     * Constructor with message and cause.
     * @param message an error message to give more context.
     * @param cause a cause.
     */
    public RatException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with message only.
     * @param message an error message to give more context.
     */
    public RatException(final String message) {
        super(message);
    }

    /**
     * Constructor with cause only.
     * @param cause a cause.
     */
    public RatException(final Throwable cause) {
        super(cause);
    }
}
