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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Data about the subject.
 */
public class MetaData {

    public static final String RAT_BASE_URL = "http://org/apache/rat/meta-data";
    
    // Document Categories
    public static final String RAT_URL_DOCUMENT_CATEGORY = RAT_BASE_URL + "#FileCategory";
    public static final String RAT_DOCUMENT_CATEGORY_VALUE_GENERATED = "GEN  ";
    public static final String RAT_DOCUMENT_CATEGORY_VALUE_UNKNOWN = "?????";
    public static final String RAT_DOCUMENT_CATEGORY_VALUE_ARCHIVE = "archive";
    public static final String RAT_DOCUMENT_CATEGORY_VALUE_NOTICE = "notice";
    public static final String RAT_DOCUMENT_CATEGORY_VALUE_BINARY = "binary";
    public static final String RAT_DOCUMENT_CATEGORY_VALUE_STANDARD = "standard";
    public static final Datum RAT_DOCUMENT_CATEGORY_DATUM_GENERATED = new Datum(RAT_URL_DOCUMENT_CATEGORY, RAT_DOCUMENT_CATEGORY_VALUE_GENERATED);
    public static final Datum RAT_DOCUMENT_CATEGORY_DATUM_UNKNOWN = new Datum(RAT_URL_DOCUMENT_CATEGORY, RAT_DOCUMENT_CATEGORY_VALUE_UNKNOWN);
    public static final Datum RAT_DOCUMENT_CATEGORY_DATUM_ARCHIVE = new Datum(RAT_URL_DOCUMENT_CATEGORY, RAT_DOCUMENT_CATEGORY_VALUE_ARCHIVE);
    public static final Datum RAT_DOCUMENT_CATEGORY_DATUM_NOTICE = new Datum(RAT_URL_DOCUMENT_CATEGORY, RAT_DOCUMENT_CATEGORY_VALUE_NOTICE);
    public static final Datum RAT_DOCUMENT_CATEGORY_DATUM_BINARY = new Datum(RAT_URL_DOCUMENT_CATEGORY, RAT_DOCUMENT_CATEGORY_VALUE_BINARY);
    public static final Datum RAT_DOCUMENT_CATEGORY_DATUM_STANDARD = new Datum(RAT_URL_DOCUMENT_CATEGORY, RAT_DOCUMENT_CATEGORY_VALUE_STANDARD);
    
    // Header Categories
    public static final String RAT_URL_HEADER_CATEGORY = RAT_BASE_URL + "#HeaderCategory";
    
    // License Family Categories
    public static final String RAT_URL_LICENSE_FAMILY_CATEGORY= RAT_BASE_URL + "#LicenseFamilyCategory";
    // Shortcuts used in report output, must be exactly 5 characters
    public static final String RAT_LICENSE_FAMILY_CATEGORY_VALUE_GEN = "GEN  ";
    public static final String RAT_LICENSE_FAMILY_CATEGORY_VALUE_UNKNOWN = "?????";
    public static final String RAT_LICENSE_FAMILY_CATEGORY_VALUE_ASL = "AL   ";
    public static final String RAT_LICENSE_FAMILY_CATEGORY_VALUE_OASIS = "OASIS";
    public static final String RAT_LICENSE_FAMILY_CATEGORY_VALUE_W3CD = "W3CD ";
    public static final String RAT_LICENSE_FAMILY_CATEGORY_VALUE_W3C = "W3C  ";
    public static final String RAT_LICENSE_FAMILY_CATEGORY_VALUE_DOJO = "DOJO ";
    public static final String RAT_LICENSE_FAMILY_CATEGORY_VALUE_TMF = "TMF  ";
    public static final String RAT_LICENSE_FAMILY_CATEGORY_VALUE_GPL1 ="GPL1 ";
    public static final String RAT_LICENSE_FAMILY_CATEGORY_VALUE_GPL2 ="GPL2 ";
    public static final String RAT_LICENSE_FAMILY_CATEGORY_VALUE_GPL3 = "GPL3 ";
    public static final String RAT_LICENSE_FAMILY_CATEGORY_VALUE_MIT = "MIT  ";
    public static final String RAT_LICENSE_FAMILY_CATEGORY_VALUE_CDDL1 = "CDDL1";

    public static final Datum RAT_LICENSE_FAMILY_CATEGORY_DATUM_GEN = new Datum(RAT_URL_LICENSE_FAMILY_CATEGORY, RAT_LICENSE_FAMILY_CATEGORY_VALUE_GEN);
    public static final Datum RAT_LICENSE_FAMILY_CATEGORY_DATUM_UNKNOWN = new Datum(RAT_URL_LICENSE_FAMILY_CATEGORY, RAT_LICENSE_FAMILY_CATEGORY_VALUE_UNKNOWN);
    public static final Datum RAT_LICENSE_FAMILY_CATEGORY_DATUM_ASL = new Datum(RAT_URL_LICENSE_FAMILY_CATEGORY, RAT_LICENSE_FAMILY_CATEGORY_VALUE_ASL);
    public static final Datum RAT_LICENSE_FAMILY_CATEGORY_DATUM_OASIS = new Datum(RAT_URL_LICENSE_FAMILY_CATEGORY, RAT_LICENSE_FAMILY_CATEGORY_VALUE_OASIS);
    public static final Datum RAT_LICENSE_FAMILY_CATEGORY_DATUM_W3CD = new Datum(RAT_URL_LICENSE_FAMILY_CATEGORY, RAT_LICENSE_FAMILY_CATEGORY_VALUE_W3CD);
    public static final Datum RAT_LICENSE_FAMILY_CATEGORY_DATUM_W3C = new Datum(RAT_URL_LICENSE_FAMILY_CATEGORY, RAT_LICENSE_FAMILY_CATEGORY_VALUE_W3C);
    public static final Datum RAT_LICENSE_FAMILY_CATEGORY_DATUM_DOJO = new Datum(RAT_URL_LICENSE_FAMILY_CATEGORY, RAT_LICENSE_FAMILY_CATEGORY_VALUE_DOJO);
    public static final Datum RAT_LICENSE_FAMILY_CATEGORY_DATUM_TMF = new Datum(RAT_URL_LICENSE_FAMILY_CATEGORY, RAT_LICENSE_FAMILY_CATEGORY_VALUE_TMF);
    public static final Datum RAT_LICENSE_FAMILY_CATEGORY_DATUM_GPL1 = new Datum(RAT_URL_LICENSE_FAMILY_CATEGORY,RAT_LICENSE_FAMILY_CATEGORY_VALUE_GPL1);
    public static final Datum RAT_LICENSE_FAMILY_CATEGORY_DATUM_GPL2 = new Datum(RAT_URL_LICENSE_FAMILY_CATEGORY,RAT_LICENSE_FAMILY_CATEGORY_VALUE_GPL2);
    public static final Datum RAT_LICENSE_FAMILY_CATEGORY_DATUM_GPL3 = new Datum(RAT_URL_LICENSE_FAMILY_CATEGORY,RAT_LICENSE_FAMILY_CATEGORY_VALUE_GPL3);
    public static final Datum RAT_LICENSE_FAMILY_CATEGORY_DATUM_MIT = new Datum(RAT_URL_LICENSE_FAMILY_CATEGORY,RAT_LICENSE_FAMILY_CATEGORY_VALUE_MIT);
    public static final Datum RAT_LICENSE_FAMILY_CATEGORY_DATUM_CDLL1 = new Datum(RAT_URL_LICENSE_FAMILY_CATEGORY,RAT_LICENSE_FAMILY_CATEGORY_VALUE_CDDL1);

    // License Family Standard Names
    public static final String RAT_URL_LICENSE_FAMILY_NAME= RAT_BASE_URL + "#LicenseFamilyName";
    public static final String RAT_LICENSE_FAMILY_NAME_VALUE_W3C_SOFTWARE_COPYRIGHT = "W3C Software Copyright";
    public static final String RAT_LICENSE_FAMILY_NAME_VALUE_W3C_DOCUMENT_COPYRIGHT = "W3C Document Copyright";
    public static final String RAT_LICENSE_FAMILY_NAME_VALUE_OASIS_OPEN_LICENSE = "OASIS Open License";
    public static final String RAT_LICENSE_FAMILY_NAME_VALUE_MODIFIED_BSD_LICENSE = "Modified BSD License";
    public static final String RAT_LICENSE_FAMILY_NAME_VALUE_APACHE_LICENSE_VERSION_2_0 = "Apache License Version 2.0";
    public static final String RAT_LICENSE_FAMILY_NAME_VALUE_GPL_VERSION_1 =
            "GNU General Public License, version 1";
    public static final String RAT_LICENSE_FAMILY_NAME_VALUE_GPL_VERSION_2 =
            "GNU General Public License, version 2";
    public static final String RAT_LICENSE_FAMILY_NAME_VALUE_GPL_VERSION_3 =
            "GNU General Public License, version 3";
    public static final String RAT_LICENSE_FAMILY_NAME_VALUE_MIT =
            "The MIT License";
    public static final String RAT_LICENSE_FAMILY_NAME_VALUE_CDDL1 =
            "COMMON DEVELOPMENT AND DISTRIBUTION LICENSE Version 1.0";
    public static final String RAT_LICENSE_FAMILY_NAME_VALUE_ACADEMIC_FREE_LICENSE_VERSION_2_1 = "Academic Free License, Version 2.1";
    public static final String RAT_LICENSE_FAMILY_NAME_VALUE_UNKNOWN = "?????";
    public static final Datum RAT_LICENSE_FAMILY_NAME_DATUM_W3C_SOFTWARE_COPYRIGHT 
        = new Datum(RAT_URL_LICENSE_FAMILY_NAME, RAT_LICENSE_FAMILY_NAME_VALUE_W3C_SOFTWARE_COPYRIGHT);
    public static final Datum RAT_LICENSE_FAMILY_NAME_DATUM_W3C_DOCUMENT_COPYRIGHT 
        = new Datum(RAT_URL_LICENSE_FAMILY_NAME, RAT_LICENSE_FAMILY_NAME_VALUE_W3C_DOCUMENT_COPYRIGHT);
    public static final Datum RAT_LICENSE_FAMILY_NAME_DATUM_OASIS_OPEN_LICENSE 
        = new Datum(RAT_URL_LICENSE_FAMILY_NAME, RAT_LICENSE_FAMILY_NAME_VALUE_OASIS_OPEN_LICENSE);
    public static final Datum RAT_LICENSE_FAMILY_NAME_DATUM_MODIFIED_BSD_LICENSE 
        = new Datum(RAT_URL_LICENSE_FAMILY_NAME, RAT_LICENSE_FAMILY_NAME_VALUE_MODIFIED_BSD_LICENSE);
    public static final Datum RAT_LICENSE_FAMILY_NAME_DATUM_APACHE_LICENSE_VERSION_2_0
        = new Datum(RAT_URL_LICENSE_FAMILY_NAME, RAT_LICENSE_FAMILY_NAME_VALUE_APACHE_LICENSE_VERSION_2_0);
    public static final Datum
            RAT_LICENSE_FAMILY_NAME_DATUM_GPL_VERSION_1 = new Datum(RAT_URL_LICENSE_FAMILY_NAME, RAT_LICENSE_FAMILY_CATEGORY_VALUE_GPL1);
    public static final Datum
            RAT_LICENSE_FAMILY_NAME_DATUM_GPL_VERSION_2 = new Datum(RAT_URL_LICENSE_FAMILY_NAME, RAT_LICENSE_FAMILY_CATEGORY_VALUE_GPL2);
    public static final Datum
            RAT_LICENSE_FAMILY_NAME_DATUM_GPL_VERSION_3 = new Datum(RAT_URL_LICENSE_FAMILY_NAME, RAT_LICENSE_FAMILY_NAME_VALUE_GPL_VERSION_3);
    public static final Datum
            RAT_LICENSE_FAMILY_NAME_DATUM_MIT = new Datum(RAT_URL_LICENSE_FAMILY_NAME, RAT_LICENSE_FAMILY_NAME_VALUE_MIT);
    public static final Datum
            RAT_LICENSE_FAMILY_NAME_DATUM_CDDL1 = new Datum(RAT_URL_LICENSE_FAMILY_NAME, RAT_LICENSE_FAMILY_NAME_VALUE_CDDL1);
    public static final Datum RAT_LICENSE_FAMILY_NAME_DATUM_ACADEMIC_FREE_LICENSE_VERSION_2_1
        = new Datum(RAT_URL_LICENSE_FAMILY_NAME, RAT_LICENSE_FAMILY_NAME_VALUE_ACADEMIC_FREE_LICENSE_VERSION_2_1);
    public static final Datum RAT_LICENSE_FAMILY_NAME_DATUM_UNKNOWN
        = new Datum(RAT_URL_LICENSE_FAMILY_NAME, RAT_LICENSE_FAMILY_NAME_VALUE_UNKNOWN); 
    
    // Header sample
    public static final String RAT_URL_HEADER_SAMPLE = RAT_BASE_URL + "#HeaderSample";
    
    // License Approval
    public static final String RAT_URL_APPROVED_LICENSE = RAT_BASE_URL + "#ApprovedLicense";
    public static final String RAT_APPROVED_LICENSE_VALUE_TRUE = Boolean.TRUE.toString();
    public static final String RAT_APPROVED_LICENSE_VALUE_FALSE = Boolean.FALSE.toString();
    public static final Datum RAT_APPROVED_LICENSE_DATIM_TRUE = new Datum(RAT_URL_APPROVED_LICENSE, RAT_APPROVED_LICENSE_VALUE_TRUE);
    public static final Datum RAT_APPROVED_LICENSE_DATIM_FALSE = new Datum(RAT_URL_APPROVED_LICENSE, RAT_APPROVED_LICENSE_VALUE_FALSE);
    
    private ContentType contentType;
    /** 
     * Only likely to be a small quantity of data 
     * so trade some performance for simplicity.
     */
    private final List<Datum> data;

    public MetaData() {
        this(null);
    }
    
    public MetaData(final ContentType contentType) {
        this.contentType = contentType;
        this.data = new ArrayList<>(16);
    }
    
    /**
     * Gets the content type for the subject.
     * @return or null when the type is unknown
     */
    public ContentType getContentType() {
        return contentType;
    }
    
    /**
     * Sets the content type for this subject.
     * @param contentType <code>ContentType</code>,
     * or null when the content type is unknown
     */
    public void setContentType(final ContentType contentType) {
        this.contentType = contentType;
    }
    
    /**
     * Gets all data.
     * @return unmodifiable view of the meta data.
     */
    public Collection<Datum> getData() {
        return Collections.unmodifiableCollection(data);
    }
    
    /**
     * Adds a new datum.
     * Existing data with the same name are not replaced.
     * @param datum datum to add.
     * @see #set(org.apache.rat.api.MetaData.Datum)
     */
    public void add(final Datum datum) {
       data.add(datum); 
    }
    
    /**
     * Puts in a new datum replacing any existing data.
     * Any current data matching the name are removed.
     * @param datum not null
     * @see #add(org.apache.rat.api.MetaData.Datum)
     */
    public void set(final Datum datum) {
       clear(datum.getName()); 
       add(datum);
    }
    
    /**
     * Gets the first datum matching the given name.
     * @param name not null
     * @return the matching datum first added when there is any matching data,
     * null otherwise
     */
    public Datum get(final String name) {
        Datum result = null;
        for (Datum next : data) {
            if (name.equals(next.getName())) {
                result = next;
                break;
            }
        }
        return result;
    }
    
    /**
     * Gets the value of the first datum matching the given name.
     * @param name not null
     * @return the value of the matchin datum first added when there is any matching data,
     * null otherwise
     */
    public String value(final String name) {
        final Datum datum = get(name);
        final String result;
        if (datum == null) {
            result = null;
        } else {
            result = datum.getValue();
        }
        return result;
    }
    
    /**
     * Removes all data matching the given name.
     * @param name not null
     * @return true if any data match, false otherwise
     */
    public boolean clear(final String name) {
        boolean dataRemoved = false;
        for (final Iterator<Datum> it = data.iterator();it.hasNext();) {
            final Datum datum = it.next();
            if (datum.getName().equals(name)) {
                it.remove();
                dataRemoved = true;
            }
        }
        return dataRemoved;
    }
    
    /**
     * Clears all data.
     */
    public void clear() {
        data.clear();
        this.contentType = null;
    }
    
    /**
     * A datum.
     */
    public static final class Datum {
        private final String name;
        private final String value;
        
        /**
         * Constructs a datum.
         * @param name not null
         * @param value not null
         */
        public Datum(final String name, final String value) {
            super();
            this.name = name;
            this.value = value;
        }
        
        /**
         * Gets the name of the data type.
         * To avoid collisions, it is recommended that URLs are used.
         * @return not null
         */
        public String getName() {
            return name;
        }
     
        /**
         * Data type value.
         * @return not null
         */
        public String getValue() {
            return value;
        }

        /**
         * Constructs a <code>String</code> with all attributes
         * in name = value format.
         *
         * @return a <code>String</code> representation 
         * of this object.
         */
        @Override
        public String toString()
        {
            return "Datum [ "
                + "name ='" + this.name + "',"
                + "value ='" + this.value + " "
                + "']";
        }
    }
}
 