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

import org.apache.rat.api.Document;
import org.apache.rat.report.claim.ClaimStatistic;

/**
 * Utilities to help analyze report test output.
 */
public final class ReporterTestUtils {

    /** The default license output for an unknown license */
    public static final String UNKNOWN_LICENSE = licenseOut("?????", "Unknown license (Unapproved)");
    /** The default license output for an Apache 2 license */
    public static final String APACHE_LICENSE = licenseOut("AL", "AL", "Apache License Version 2.0");

    private ReporterTestUtils() {
    }
    /**
     * Generates the regex string for a document output line. Suitable for regex query.
     * @param approved {@code True} if this license is approved
     * @param type The document type.
     * @param name The name of the document.
     * @return the regular expression string representing the document.
     */
    public static String documentOut(final boolean approved, final Document.Type type, final String name) {
        return String.format("^%s \\Q%s\\E\\s+  %s [^$]+$", approved ? " " : "!", name, type.name().charAt(0) );
    }

    /**
     * Generates the regex string for a license output line. Suitable for regex query.
     * @param family The license family category.
     * @param name The license name.
     * @return the regular expression string representing the license.
     */
    public static String licenseOut(final String family, final String name) {
        return licenseOut(family, family, name);
    }

    /**
     * Generates the regex string for a license output line. Suitable for regex query.
     * @param family The license family category.
     * @param id The license id if different from family.
     * @param name The license name.
     * @return the regular expression string representing the license.
     */
    public static String licenseOut(final String family, final String id, final String name) {
        return String.format("\\s+\\Q%s\\E\\s+\\Q%s\\E\\s+\\Q%s\\E$", family, id, name);
    }

    /**
     * Renders report contents of a given counter value, including handling of negation.
     * @param counter The expected type of counter in the report.
     * @param count The expected number of occurrences of the given counter.
     * @param error {@code True} if the counter does exceed a minimum or maximum value.
     * @return the report contents of the given counter.
     */
    public static String counterText(ClaimStatistic.Counter counter, int count, boolean error) {
        return String.format("^%s %s:\\s*%s ", error ? "!" : " ", counter.displayName(), count);
    }

    /**
     * Generates the report contents for Apache 2.0 licenses.
     * @param count The expected number of occurrences of the license.
     * @return Report contents for Apache 2.0 licenses in the report.
     */
    public static String apacheLicenseVersion2(int count) {
        return String.format("^Apache License Version 2.0: %s ", count);
    }

    /**
     * Generates the report contents for unknown licenses.
     * @param count The expected number of occurrences of the license.
     * @return Report contents for unknown licenses in the report.
     */
    public static String unknownLicense(int count) {
        return String.format("^Unknown license: %s ", count);
    }
}
