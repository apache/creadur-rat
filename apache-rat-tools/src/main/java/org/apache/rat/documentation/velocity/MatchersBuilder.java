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

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.config.parameters.ComponentType;
import org.apache.rat.config.parameters.Description;
import org.apache.rat.config.parameters.DescriptionBuilder;
import org.apache.rat.configuration.MatcherBuilderTracker;
import org.apache.rat.configuration.XMLConfig;

public final class MatchersBuilder {

    private MatchersBuilder() {
        // do not instantiate.
    }

    /**
     * Creates a set of matchers.
     * @return the set of matchers.
     */
    public static Set<Matcher> build() {
        MatcherBuilderTracker tracker = MatcherBuilderTracker.instance();
        Set<Matcher> documentationSet = new TreeSet<>((x, y) -> x.getName().compareTo(y.getName()));
        for (Class<?> clazz : tracker.getClasses()) {
            Description desc = DescriptionBuilder.buildMap(clazz);
            documentationSet.add(new Matcher(desc));
        }
        return documentationSet;
    }

    /**
     * Describes a matcher.
     */
    public static final class Matcher {
        /**
         * The description for this matcher.
         */
        private final Description desc;
        /**
         * The description of the enclosed attribute.  May be null.
         */
        private final Enclosed enclosed;
        /**
         * The set of attributes for this matcher.
         */
        private final Set<Attribute> attributes;

        Matcher(final Description desc) {
            this.desc = desc;
            Enclosed enclosed = null;
            attributes = new TreeSet<>((x, y) -> x.getName().compareTo(y.getName()));
            for (Description child : desc.childrenOfType(ComponentType.PARAMETER)) {
                if (XMLConfig.isInlineNode(desc.getCommonName(), child.getCommonName())) {
                    enclosed = new Enclosed(child);
                } else {
                    attributes.add(new Attribute(child));
                }
            }
            this.enclosed = enclosed;
        }
        public String getName() {
            return desc.getCommonName();
        }
        public String getDescription() {
            return StringUtils.defaultIfEmpty(desc.getDescription(), "");
        }
        public Enclosed getEnclosed() {
            return enclosed;
        }
        public Collection<Attribute> getAttributes() {
            return attributes;
        }

        public static final class Enclosed {
            /** The description for the enclosed item */
            private final Description desc;
            Enclosed(final Description desc) {
                this.desc = desc;
            }
            public String getRequired() {
                return desc.isRequired() ? "required" : "optional";
            }
            public String getCollection() {
                return desc.isCollection() ? "or more " : "";
            }
            public String getType() {
                return desc.getChildType().getSimpleName();
            }
        }

        /**
         * The definition of an attribute.
         */
        public static final class Attribute {
            /**
             * The description for the attribute.
             */
            private final Description desc;

            /**
             * Constructor
             * @param desc the description for this attribute.
             */
            Attribute(final Description desc) {
                this.desc = desc;
            }
            public String getName() {
                return desc.getCommonName();
            }
            public String getRequired() {
                return desc.isRequired() ? "required" : "optional";
            }
            public String getType() {
                return desc.getChildType().getSimpleName();
            }
            public String getDescription() {
                return StringUtils.defaultIfEmpty(desc.getDescription(), "");
            }
        }
    }
}
