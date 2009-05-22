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

    public static final String RAT_DOCUMENT_CATEGORY_VALUE_GENERATED = "GEN  ";
    public static final String RAT_DOCUMENT_CATEGORY_VALUE_UNKNOWN = "?????";
    public static final String RAT_DOCUMENT_CATEGORY_VALUE_ARCHIVE = "archive";
    public static final String RAT_DOCUMENT_CATEGORY_VALUE_NOTICE = "notice";
    public static final String RAT_DOCUMENT_CATEGORY_VALUE_BINARY = "binary";
    public static final String RAT_DOCUMENT_CATEGORY_VALUE_STANDARD = "standard";
    public static final String RAT_DOCUMENT_CATEGORY_URL ="http://org/apache/rat/meta-data#FileCategory";
    
    private ContentType contentType;
    /** 
     * Only likely to be a small quantity of data 
     * so trade some performance for simplicity.
     */
    private final List/*<Datum>*/ data;
    public static final String RAT_LICENSE_FAMILY_VALUE_ASL = "AL   ";
    public static final String RAT_LICENSE_FAMILY_VALUE_OASIS = "OASIS";
    public static final String RAT_LICENSE_FAMILY_VALUE_W3CD = "W3CD ";
    public static final String RAT_LICENSE_FAMILY_VALUE_W3C = "W3C  ";
    public static final String RAT_LICENSE_FAMILY_VALUE_DOJO = "DOJO ";
    public static final String RAT_LICENSE_FAMILY_VALUE_TMF = "TMF  ";
    
    public MetaData() {
        this(null);
    }
    
    public MetaData(final ContentType contentType) {
        this.contentType = contentType;
        this.data = new ArrayList/*<Datum>*/(16);
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
    public Collection getData() {
        return Collections.unmodifiableCollection(data);
    }
    
    /**
     * Adds a new datum.
     * Existing data with the same name are not replaced.
     * @param datum
     * @see #put(org.apache.rat.api.MetaData.Datum)
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
    public void put(final Datum datum) {
       clear(datum.getName()); 
    }
    
    /**
     * Removes all data matching the given name.
     * @param name not null
     * @return true if any data match, false otherwise
     */
    public boolean clear(final String name) {
        boolean dataRemoved = false;
        for (final Iterator it=data.iterator();it.hasNext();) {
            final Datum datum = (Datum) it.next();
            if (datum.getName().equals(name)) {
                it.remove();
                dataRemoved = true;
            }
        }
        return dataRemoved;
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
        public String toString()
        {
            return "Datum [ "
                + "name ='" + this.name + "',"
                + "value ='" + this.value + " "
                + "']";
        }
    }
}
