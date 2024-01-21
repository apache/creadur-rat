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

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.rat.ConfigurationException;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.matchers.FullTextMatcher;
import org.apache.rat.analysis.matchers.SimpleTextMatcher;

/**
 * Builds text based matcher based on the complexity of the text to match.
 */
public class TextBuilder extends AbstractBuilder implements TextCaptureBuilder {

    private String text;

    @SuppressWarnings("unchecked")
    @Override
    public TextBuilder setText(String text) {
        Objects.requireNonNull(text, "text may not be null");
        this.text = text;
        return this;
    }

    @Override
    public IHeaderMatcher build() {
        if (StringUtils.isBlank(text)) {
            throw new ConfigurationException("text value is required");
        }
        boolean complex = text.contains(" ") | text.contains("\\t") | text.contains("\\n") | text.contains("\\r")
                | text.contains("\\f") | text.contains("\\v");

        return complex ? new FullTextMatcher(getId(), text) : new SimpleTextMatcher(getId(), text);
    }

    @Override
    public String toString() {
        if (text.length() > 20) {
            return "TextBuilder: " + text.substring(0, 20) + "...";
        }
        return "TextBuilder: " + text;
    }
}
