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
package org.apache.rat;

/**
 * An exception thrown when there is an issue with the configuration.
 */
public class ConfigurationException extends RuntimeException {

    private static final long serialVersionUID = 7257245932787579431L;

    public static ConfigurationException from(final Exception e) {
        if (e instanceof ConfigurationException) {
            return (ConfigurationException) e;
        }
        return new ConfigurationException(e);
    }

    public ConfigurationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ConfigurationException(final String message) {
        super(message);
    }

    public ConfigurationException(final Throwable cause) {
        super(cause);
    }

}
