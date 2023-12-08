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
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.ConfigurationException;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.IHeaderMatcher.Builder;

/**
 * Constructs a builder that contains other builders.
 */
public abstract class ChildContainerBuilder extends AbstractBuilder {

    /**
     * The list of builders that will build the enclosed matchers.
     */
    protected final List<IHeaderMatcher.Builder> children = new ArrayList<>();

    protected ChildContainerBuilder() {
    }

    /**
     * Reads a text file. Each line becomes a text matcher in the resulting List.
     * 
     * @param resourceName the name of the resource to read.
     * @return a List of Matchers, one for each non empty line in the input file.
     */
    public AbstractBuilder setResource(String resourceName) {
          URL url = this.getClass().getResource(resourceName);
            try (final InputStream in = url.openStream()) {
                BufferedReader buffer = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                String txt;
                while (null != (txt = buffer.readLine())) {
                    txt = txt.trim();
                    if (StringUtils.isNotBlank(txt)) {
                        children.add(Builder.text().setText(txt));
                    }
                }
                return this;
            } catch (IOException e) {
                throw new ConfigurationException("Unable to read matching text file: " + resourceName, e);
            }
    }
    
    /**
     * Adds a builder to the list of builders.
     * @param child the child builder to add.
     * @return this for chaining.
     */
    public AbstractBuilder add(IHeaderMatcher.Builder child) {
        children.add(child);
        return this;
    }
    
    /**
     * Adds a collection of builders to the list of child builders.
     * @param children the children to add.
     * @return this for chaining.
     */
    public AbstractBuilder add(Collection<IHeaderMatcher.Builder> children) {
        this.children.addAll(children);
        return this;
    }

    /**
     * @return the list of child builders for this builder.
     */
    public List<IHeaderMatcher> getChildren() {
        return children.stream().map(IHeaderMatcher.Builder::build).collect(Collectors.toList());
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getSimpleName()).append( ":");
        children.stream().map(Object::toString).forEach( x -> sb.append("\n").append(x));
        return sb.toString();
    }

}
