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
package org.apache.rat.documentation.options;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.cli.Option;
import org.apache.rat.ui.UIOption;
import org.apache.rat.ui.UIOptionCollection;

import static java.lang.String.format;

/**
 * A class that wraps the CLI option and provides Ant specific values.
 */
public class AntOption extends UIOption<AntOption> {

    /**
     * Constructor.
     *
     * @param option the option to wrap.
     */
    public AntOption(final UIOptionCollection<AntOption> collection, final Option option) {
        super(collection, option, AntOptionCollection.createName(option));
    }

    /**
     * Returns {@code true} if the option should be an attribute of the &lt;rat:report&gt; element.
     *
     * @return {@code true} if the option should be an attribute of the &lt;rat:report&gt; element.
     */
    public boolean isAttribute() {
        return getAntCollection().isAttribute(this);
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Returns {@code true} if the option should be a child element of the &lt;rat:report&gt; element.
     *
     * @return {@code true} if the option should be a child element of the &lt;rat:report&gt; element.
     */
    public boolean isElement() {
        return !isAttribute();
    }

    /**
     * If this option is converted to another option return that option otherwise
     * return this option.
     * @return the converted option.
     */
    public AntOption getActualAntOption() {
        return getAntCollection().getActualAntOption(this);
    }

    /**
     * Gets the set of options that are mapped to this option.
     * @return the set of options that are mapped to this option.
     */
    public Set<AntOption> convertedFrom() {
        return getAntCollection().convertedFrom(this);
    }

    @Override
    public String getText() {
        return cleanupName(option);
    }

    @Override
    protected String cleanupName(final Option option) {
        AntOption antOption;
        if (getOption().equals(option)) {
            antOption = this;
        } else {
            Optional<AntOption> optAntOption = getOptionCollection().getMappedOption(option);
            if (optAntOption.isPresent()) {
                antOption = optAntOption.get();
            } else {
                return "";
            }
        }
        return antOption.cleanupName();
    }

    public String cleanupName() {
        String fmt = isAttribute() ? "%s attribute" : "<%s>";
        return format(fmt, name);
    }

    AntOptionCollection getAntCollection() {
        return getOptionCollection();
    }

    public AntOptionCollection.BuildType buildType() {
        return getAntCollection().buildType(this.getArgType());
    }

    @Override
    public String getExample() {
        return new ExampleGenerator().getExample();
    }

    /**
     * An example code generator for this AntOption.
     */
    public class ExampleGenerator {

        /**
         * The constructor.
         */
        public ExampleGenerator() {
        }

        /**
         * Gets an example Ant XML report call using ant option.
         * @return the example of this ant option.
         */
        String getExample() {
                return getExample("data", getAntCollection().getRequiredAttributes(getName()), null);
        }

        /**
         * Gets an example Ant XML report call using ant option with the specified attributes and child elements.
         * @param data The data value for this option.
         * @param attributes A map of attribute keys and values.
         * @param childElements a list of child elements for the example
         * @return example Ant XML report call using ant option with the specified attributes and child elements.
         */
        public String getExample(final String data, final Map<String, String> attributes, final List<String> childElements) {
            return "<rat:report" +
                    getExampleAttributes(data, attributes) +
                    "> \n" +
                    getChildElements(data, childElements) +
                    "</rat:report>\n";
        }

        /**
         * Creates a string comprising the attributes for the Ant XML report call.
         * @param data The data value for this option.
         * @param attributes A map of attribute keys and values.
         * @return a string comprising all the attribute keys and values for the Ant XML report element.
         */
        public String getExampleAttributes(final String data, final Map<String, String> attributes) {
            AntOption actualOption = getActualAntOption();
            StringBuilder result = new StringBuilder();
            if (attributes != null) {
                attributes.forEach((k, v) -> result.append(format(" %s=\"%s\"", k, v)));
            }
            if (actualOption.isAttribute()) {
                result.append(format(" %s=\"%s\"", actualOption.getName(), actualOption.hasArg() ? data : "true"));
            }
            return result.toString();
        }

        /**
         * Creates a string comprising the child elements for the Ant XML report call.
         * @param data the data for this option.
         * @param childElements additional child elements.
         * @return A string comprising the child elements for the Ant XML report call.
         */
        public String getChildElements(final String data, final List<String> childElements) {
            AntOption baseOption = AntOption.this;
            AntOption actualOption = getActualAntOption();
            StringBuilder result = new StringBuilder();
            if (!actualOption.isAttribute()) {
                String inner = getAntCollection().buildType(getArgType()).getXml(actualOption, baseOption, data);
                result.append(inner);
            }
            if (childElements != null) {
                childElements.forEach(x -> result.append(x).append("\n"));
            }
            return result.toString();
        }
    }
}
