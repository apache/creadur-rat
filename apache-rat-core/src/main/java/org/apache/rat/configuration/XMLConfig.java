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

import java.util.Arrays;

/**
 * Configuration definitions for XMLConfiguration reader and writer.
 */
public class XMLConfig {

    /** id attribute name */
    public final static String ATT_ID = "id";
    /** name attribute name */
    public final static String ATT_NAME = "name";
    /** license reference attribute name */
    public final static String ATT_LICENSE_REF = "license_ref";
    /** class name attribute name */
    public final static String ATT_CLASS_NAME = "class";
    /** root of the configuration file */
    public final static String ROOT = "rat-config";
    /** families element name */
    public final static String FAMILIES = "families";
    /** licenses element name */
    public final static String LICENSES = "licenses";
    /** license element name */
    public final static String LICENSE = "license";
    /** approved element name */
    public final static String APPROVED = "approved";
    /** family element name */
    public final static String FAMILY = "family";
    /** note element name */
    public final static String NOTE = "note";
    /** matchers element name */
    public final static String MATCHERS = "matchers";
    /** matcher element name */
    public final static String MATCHER = "matcher";

    /** License property names that should be children */
    static final String[] LICENSE_CHILDREN = { "note", "matcher" };
    /**
     * License property names that should not be displayed contents should be placed
     * inline
     */
    static final String[] LICENSE_INLINE = { "matcher" };

    /**
     * Matcher properties that should be directly inlined Entries are matcher node
     * name / property name pairs. A matcher may only have one inline node and then
     * only if there is no other non-property node.
     */
    static final String[][] INLINE_NODES = { { "any", "enclosed" }, { "all", "enclosed" }, { "not", "enclosed" },
            { "text", "simpleText" } };

    private XMLConfig() {
        // do not instantiate
    }

    /**
     * Returns true if the specified child node should be placed inline in the XML
     * document.
     * 
     * @param parent the parent node name.
     * @param child the child node name.
     * @return true if the child should be inlined.
     */
    public static boolean isInlineNode(String parent, String child) {
        return Arrays.stream(INLINE_NODES).filter(s -> s[0].equals(parent) && s[1].equals(child)).findAny().isPresent();
    }

    /**
     * Returns true if the child should be a child node of a license node, as
     * opposed to a attribute of the license.
     * 
     * @param child the name of the child node.
     * @return true if the child should be a child node.
     */
    public static boolean isLicenseChild(String child) {
        return Arrays.stream(LICENSE_CHILDREN).filter(s -> s.equals(child)).findAny().isPresent();
    }

    /**
     * Return true if the child should be inlined in the parent node.
     * 
     * @param child the name of the child node.
     * @return true if the child should be inlined.
     */
    public static boolean isLicenseInline(String child) {
        return Arrays.stream(LICENSE_INLINE).filter(s -> s.equals(child)).findAny().isPresent();
    }
}