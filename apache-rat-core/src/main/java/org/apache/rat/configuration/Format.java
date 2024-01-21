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
package org.apache.rat.configuration;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.apache.rat.ConfigurationException;

/**
 * An enumeration of the types of files that can contain the configuration
 * information.
 */
public enum Format {
    /** an XML file */
    XML(XMLConfigurationReader.class, "xml"),
    /** A plain text file */
    TXT(null, "txt", "text");

    private final String[] suffix;

    private Constructor<MatcherReader> matcherReader;
    private Constructor<LicenseReader> licenseReader;

    @SuppressWarnings("unchecked")
    Format(Class<?> reader, String... suffix) {
        if (reader != null) {
            try {
                matcherReader = MatcherReader.class.isAssignableFrom(reader)
                        ? (Constructor<MatcherReader>) reader.getConstructor()
                        : null;
                licenseReader = LicenseReader.class.isAssignableFrom(reader)
                        ? (Constructor<LicenseReader>) reader.getConstructor()
                        : null;
            } catch (NoSuchMethodException | SecurityException e) {
                throw new ConfigurationException("Error retrieving no argument constructor for " + reader.getName(), e);
            }
        }
        this.suffix = suffix;
    }

    /**
     * @return a new instance of MatcherReader for this format.
     */
    public MatcherReader matcherReader() {
        try {
            return matcherReader == null ? null : matcherReader.newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new ConfigurationException("Can not instantiate MatcherReader for " + this.name(), e);
        }
    }

    /**
     * @return a new instance of the LicenseReader for this format.
     */
    public LicenseReader licenseReader() {
        try {
            return licenseReader == null ? null : licenseReader.newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new ConfigurationException("Can not instantiate LicenseReader for " + this.name(), e);
        }
    }

    /**
     * Determine the {@code Format} from the file name.
     * @param name the file name to check.
     * @return the Format
     */
    public static Format fromName(String name) {
        String[] parts = name.split("\\.");
        String suffix = parts[parts.length - 1];
        for (Format f : Format.values()) {
            if (Arrays.asList(f.suffix).contains(suffix)) {
                return f;
            }
        }
        throw new IllegalArgumentException(String.format("No such suffix: %s", suffix));
    }

   /**
    * Determine the {@code Format} from a URL.
    * @param url the URL to check.
    * @return the Format
    */
   public static Format fromURL(URL url) {
        return Format.fromName(url.getFile());
    }

   /**
    * Determine the {@code Format} from a File.
    * @param file the File to check.
    * @return the Format
    * @throws MalformedURLException in case the file cannot be found.
    */
   public static Format fromFile(File file) throws MalformedURLException {
        return Format.fromURL(file.toURI().toURL());
    }
}
