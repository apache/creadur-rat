/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
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
package org.apache.rat.api.domain;

/**
 * Licenses are grouped into families. Each family has similar legal semantics,
 * though some small details may differ.
 * <p>
 * For example, the <a href='https://opensource.org/licenses/BSD-3-Clause'>3
 * clause BSD license</a> is in a family where members differ by &lt;OWNER&gt;,
 * &lt;ORGANIZATION&gt; and &lt;YEAR&gt; parameters.
 * 
 */
public final class LicenseFamily {

    /**
     * Further information associated with the license family. Human readable.
     * Possibly null.
     */
    private final String notes;
    /**
     * Names of the category containing this license family. Choosing a suitable
     * URI is recommended. Possibly null;
     */
    private final String category;
    /**
     * Uniquely identifies this family. Choosing a suitable URI is recommended.
     * Not null.
     */
    private final String name;

    /**
     * Constructs an immutable license family.
     * 
     * @param name
     *            the name uniquely identifying this family. Recommended that
     *            this be an URI. Not null.
     * @param category
     *            the name of the category containing this license family.
     *            Recommended that this be an URI. Possibly null.
     * @param notes
     *            further information associated with the license family. Human
     *            readable. Possibly null.
     */
    public LicenseFamily(final String name, final String category,
            final String notes) {
        super();
        this.notes = notes;
        this.category = category;
        this.name = name;
    }

    /**
     * Gets further information associated with the license family. Human
     * readable.
     * 
     * @return possibly null
     */
    public String getNotes() {
        return this.notes;
    }

    /**
     * Gets the name of the category containing this license family. Recommended
     * that this be an URI.
     * 
     * @return possibly null
     */
    public String getCategory() {
        return this.category;
    }

    /**
     * Gets the name uniquely identifying this family. Recommended that this be
     * an URI.
     * 
     * @return not null
     */
    public String getName() {
        return this.name;
    }

}
