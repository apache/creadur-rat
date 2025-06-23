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
package org.apache.rat.documentation.velocity;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.license.ILicense;

/**
 * The License representation for documentation.
 */
public final class License {
    /** the RAT internal license we are wrapping */
    private final ILicense license;

    /** constructor */
    License(final ILicense license) {
        this.license = license;
    }

    /**
     * Gets the name of this license.
     * @return the name of this license.
     */
    public String name() {
        return license.getName();
    }

    /**
     * Gets the family name of this license.
     * @return the family name of this license.
     */
    public String family() {
        return license.getFamilyName();
    }

    /**
     * Gets the space normalized note for the license.
     * @return the space normalized note for the license. May be {@code null}.
     * @see StringUtils
     */
    public String note() {
        return StringUtils.normalizeSpace(license.getNote());
    }

    /**
     * Gets the ID for this license if it is not a system generated one.
     * @return the id for this license. May be {@code null}.
     */
    public String id() {
        String result = license.getId();
            try {
                UUID.fromString(result);
                return null;
            } catch (IllegalArgumentException e) {
                // do nothing.
            }
        return result;
    }

    /**
     * Gets the matcher associated with this license.
     * @return the matcher associated with this license.
     */
    public Matcher getMatcher() {
        return new Matcher(license.getMatcher());
    }

    /**
     * Gets the matcher tree associated with this license.
     * @return the matcher tree associated with this license.
     */
    public MatcherTree getMatcherTree() {
        return new MatcherTree(license.getMatcher());
    }
}
