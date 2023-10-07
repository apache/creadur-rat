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

public enum Format {
    XML( XMLConfigurationReader.class, "xml"),
    TXT ( null, "txt","text");
    
    private String[] suffix;
    
    private Constructor<MatcherReader> matcherReader;
    private Constructor<LicenseReader> licenseReader;
    
    @SuppressWarnings("unchecked")
    Format(Class<?> reader, String... suffix) {
        if (reader != null)
        {
        try {
            matcherReader =  MatcherReader.class.isAssignableFrom(reader) ? (Constructor<MatcherReader>) reader.getConstructor() : null;
            licenseReader = LicenseReader.class.isAssignableFrom(reader) ? (Constructor<LicenseReader>) reader.getConstructor() : null;
        } catch (NoSuchMethodException | SecurityException e) {
            throw new ConfigurationException( "Error retrieving no argument constructor for "+reader.getName(), e);
        }
        }
        this.suffix = suffix;
    }
    
    public MatcherReader matcherReader() {
        try {
            return matcherReader == null ? null : matcherReader.newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new ConfigurationException( "Can not instantiate MatcherReader for "+this.name(), e);
        }
    }

    public LicenseReader licenseReader() {
        try {
            return licenseReader == null ? null : licenseReader.newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new ConfigurationException( "Can not instantiate LicenseReader for "+this.name(), e);
        }
    }
    
    public static Format fromName(String name) {
        String[] parts = name.split("\\.");
        String suffix = parts[parts.length-1];
        for (Format f: Format.values()) {
            if (Arrays.stream(f.suffix).anyMatch( suffix::equals )) {
                return f;
            }
        }
        throw new IllegalArgumentException(String.format("No such suffix: %s", suffix));
    }
    
    public static Format fromURL(URL url) {
        return Format.fromName(url.getFile());
    }

    public static Format fromFile(File file) throws MalformedURLException {
        return Format.fromURL(file.toURI().toURL());
    }

}
