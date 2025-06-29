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

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.matchers.AbstractMatcherContainer;
import org.apache.rat.config.parameters.ComponentType;
import org.apache.rat.config.parameters.Description;
import org.apache.rat.config.parameters.DescriptionBuilder;
import org.apache.rat.configuration.XMLConfig;

/**
 * The Matcher representation for documentation.
 */
public class Matcher {
    /**
     * The description for this matcher.
     */
    private final Description desc;

    /**
     * The header matcher we are wrapping.  May be {@code null}.
     */
    private final IHeaderMatcher self;

    /**
     * The description of the enclosed attribute.  May be null.
     */
    private final Enclosed enclosed;
    /**
     * The set of attributes for this matcher.
     */
    private final Set<Attribute> attributes;

    /**
     * Copy constructor.
     * @param matcher the matcher to copy.
     */
    Matcher(final Matcher matcher) {
        this.desc = matcher.desc;
        this.self = matcher.self;
        this.enclosed = matcher.enclosed;
        this.attributes = matcher.attributes;
    }

    /**
     * Creates from a system Matcher.
     * @param self the IHeaderMatcher to wrap.
     */
    Matcher(final IHeaderMatcher self) {
        this(DescriptionBuilder.buildMap(self.getClass()), self);
    }

    /**
     * Creates from a description and a system matcher.
     * @param desc the description of the matcher.
     * @param self the matcher to wrap. May be {@code null}.
     */
    Matcher(final Description desc, final IHeaderMatcher self) {
        Objects.requireNonNull(desc);
        this.desc = desc;
        this.self = self;
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

    /**
     * Get the name of the matcher type.
     * @return the name of the matcher type.
     */
    public String getName() {
        return desc.getCommonName();
    }

    /**
     * Gets the description of the matcher type.
     * @return The description of the matcher type or an empty string.
     */
    public String getDescription() {
        return StringUtils.defaultIfEmpty(desc.getDescription(), "");
    }

    /**
     * If the matcher encloses another matcher return the definition of that enclosure.
     * @return the description of the enclose matcher. May be {@code null}.
     */
    public Enclosed getEnclosed() {
        return enclosed;
    }

    /**
     * Gets the attributes of this matcher.
     * @return a collection of attributes for this matcher.  May be empty but not {@code null}.
     */
    public Collection<Attribute> getAttributes() {
        return attributes;
    }

    /**
     * Gets the direct children of this matcher.
     * @return A collection of the direct children of this matcher.  May be empty but not {@code null}.
     */
    Collection<Matcher> getChildren() {
        if (self != null && enclosed != null && IHeaderMatcher.class.equals(enclosed.desc.getChildType())) {
            if (self instanceof AbstractMatcherContainer) {
                AbstractMatcherContainer matcherContainer = (AbstractMatcherContainer) self;
                return matcherContainer.getEnclosed().stream().map(Matcher::new).collect(Collectors.toList());
            }
            try {
                IHeaderMatcher matcher = (IHeaderMatcher) enclosed.desc.getter(self.getClass()).invoke(self);
                return Collections.singleton(new Matcher(matcher));
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return Collections.emptyList();
    }

    /**
     * The description of an enclosed matcher.
     */
    public final class Enclosed {
        /** The description for the enclosed item */
        private final Description desc;

        /**
         * Constructor.
         * @param desc the description of the enclosed matcher.
         */
        Enclosed(final Description desc) {
            this.desc = desc;
        }

        /**
         * gets the required flag.
         * @return the word "required" or "optional" depending on the state of the required flag.
         */
        public String getRequired() {
            return desc.isRequired() ? "required" : "optional";
        }

        /**
         * Gets the phrase "or more " if the enclosed matcher may be multiple.
         * @return the phrase "or more " or an empty string.
         */
        public String getCollection() {
            return desc.isCollection() ? "or more " : "";
        }

        /**
         * Get the type of the enclosed matcher.
         * @return the type of the enclosed matcher.
         */
        public String getType() {
            return desc.getChildType().getSimpleName();
        }

        /**
         * Gets the value of the enclosed matcher.
         *  <ul>
         *  <li>if the Matcher does not have an {@link IHeaderMatcher} defined this method returns null.</li>
         *  <li>If the value is a string it is normalized before being returned.</li>
         *  </ul>
         * @return the value of the enclosed matcher in the provided {@link IHeaderMatcher}.
         * @see StringUtils#normalizeSpace(String)
         */
        public Object getValue() {
            if (self != null) {
                try {
                    Object value = desc.getter(self.getClass()).invoke(self);
                    return value instanceof String ? StringUtils.normalizeSpace((String) value) : value;
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        }
        Class<?> getChildType() {
            return desc.getChildType();
        }
    }

    /**
     * The definition of an attribute.
     */
    public final class Attribute {
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

        /**
         * Gets the attribute name.
         * @return the attribut name.
         */
        public String getName() {
            return desc.getCommonName();
        }

        /**
         * gets the required flag.
         * @return the word "required" or "optional" depending on the state of the required flag.
         */
        public String getRequired() {
            return desc.isRequired() ? "required" : "optional";
        }
        /**
         * Get the type of the attribute.
         * @return the type of the enclosed matcher.
         */
        public String getType() {
            return desc.getChildType().getSimpleName();
        }

        /**
         * Gets the description of the attribute or an empty string.
         * @return the description of the attribute or an empty string.
         */
        public String getDescription() {
            return StringUtils.defaultIfEmpty(desc.getDescription(), "");
        }
        /**
         * Gets the value of the enclosed matcher.
         * <ul>
         * <li>If the Matcher does not have an {@link IHeaderMatcher} defined this method returns null.</li>
         * <li>If the value of is null, this method returns null.</li>
         * <li>If the value is a string it is normalized before being returned.</li>
         * <li>If the attribute is an "id" and the value is a UUID, null is returned.</li>
         * <li>otherwise, the string value is returned.</li>
         * </ul>
         * @return the value of the enclosed matcher in the provided {@link IHeaderMatcher}.
         * @see StringUtils#normalizeSpace(String)
         */
        public String getValue() {
            if (self != null) {
                try {
                    Object value = desc.getter(self.getClass()).invoke(self);
                    if (value != null) {
                        String result = value.toString();
                        if (desc.getCommonName().equals("id")) {
                            try {
                                UUID.fromString(result);
                                return null;
                            } catch (IllegalArgumentException e) {
                                // do nothing.
                            }
                        }
                        return result;
                    }
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        }
    }
}
