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

import java.net.URI;
import java.util.SortedSet;

import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.utils.Log;

/**
 * An interface describing the methods of a LicenseReader.
 */
public interface LicenseReader {
    /**
     * Adds a URL to the set of files to be read.
     * @param uri the URI to read.
     */
    void addLicenses(URI uri);

    /**
     * Reads the configuration and extracts instances of ILicense.
     * @return A collection of ILicense.
     */
    SortedSet<ILicense> readLicenses();

    /**
     * Reads the configuration and extracts instances of ILicenseFamily.
     * @return A collection of ILicenseFamily.
     */
    SortedSet<ILicenseFamily> readFamilies();

    /**
     * Reads the configuration and extracts the list of approved licenses.
     * @return The list of approved licenses specified in the configuration or an
     * empty list if none specified.
     */
    SortedSet<String> approvedLicenseId();

    /**
     * Sets the logger to use during parsing.
     * @param log the log to use.
     */
    void setLog(Log log);
}
