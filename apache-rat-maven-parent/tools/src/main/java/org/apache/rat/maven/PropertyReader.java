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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads a property file from the resource directory and returns a populated Properties object.
 */
public final class PropertyReader {
    private PropertyReader() {
        // do not instantiate
    }

    /**
     * Reads a property file from the resource directory and returns a populated Properties object.
     * @param propertyFileName the name of the property file.
     * @return the populated Property.
     * @throws IOException on IO error.
     */
    public static Properties read(final String propertyFileName) throws IOException {
        try (InputStream is = PropertyReader.class.getClassLoader()
                .getResourceAsStream(propertyFileName)) {
            Properties properties = new Properties();
            properties.load(is);
            return properties;
        }
    }
}
