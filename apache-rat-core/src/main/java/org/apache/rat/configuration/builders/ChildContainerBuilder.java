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
package org.apache.rat.configuration.builders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.ConfigurationException;
import org.apache.rat.analysis.IHeaderMatcher;

/**
 * Constructs a builder that contains other builders.
 */
public abstract class ChildContainerBuilder extends AbstractBuilder {

    /** The list of builders that will build the enclosed matchers. */
    protected final List<IHeaderMatcher.Builder> children = new ArrayList<>();

    /** The resource the builders came from if it was read from a resource */
    protected String resource;

    /**
     * Empty default constructor.
     */
    protected ChildContainerBuilder() {
    }

    /**
     * Reads a text file. Each line becomes a text matcher in the resulting list.
     *
     * @param resourceName the name of the resource to read.
     * @return a List of Matchers, one for each non-empty line in the input file.
     */
    public AbstractBuilder setResource(final String resourceName) {
        // this method is called by reflection
        URL url = this.getClass().getResource(resourceName);
        if (url == null) {
            throw new ConfigurationException("Unable to read matching text file: " + resourceName);
        }

        try (InputStream in = url.openStream();
             BufferedReader buffer = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String txt;
            while (null != (txt = buffer.readLine())) {
                txt = txt.trim();
                if (StringUtils.isNotBlank(txt)) {
                    children.add(new TextBuilder().setSimpleText(txt));
                }
            }
            this.resource = resourceName;
            return this;
        } catch (IOException e) {
            throw new ConfigurationException("Unable to read matching text file: " + resourceName, e);
        }
    }

    /**
     * Adds a builder to the list of builders.
     *
     * @param child the child builder to add.
     * @return this for chaining.
     */
    public AbstractBuilder addEnclosed(final IHeaderMatcher.Builder child) {
        children.add(child);
        return this;
    }

    /**
     * Adds a collection of builders to the list of child builders.
     *
     * @param children the children to add.
     * @return this for chaining.
     */
    public AbstractBuilder addEnclosed(final Collection<IHeaderMatcher.Builder> children) {
        // this method is called by reflection
        this.children.addAll(children);
        return this;
    }

    public List<IHeaderMatcher.Builder> getEnclosedBuilders() {
        // this method is called by reflection
        return Collections.unmodifiableList(children);
    }
    /**
     * @return the list of child builders for this builder.
     */
    public List<IHeaderMatcher> getEnclosed() {
        return children.stream().map(IHeaderMatcher.Builder::build).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getSimpleName()).append(":");
        children.stream().map(Object::toString).forEach(x -> sb.append(System.lineSeparator()).append(x));
        return sb.toString();
    }
}
