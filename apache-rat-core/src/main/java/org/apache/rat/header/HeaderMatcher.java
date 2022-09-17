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
package org.apache.rat.header;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Matches headers.</p>
 * <p><strong>Usage:</strong></p>
 * <ol>
 * <li>{@link #read(Reader)} content</li>
 * <li>{@link #matches(Pattern)} against filtered content</li>
 * </ol>
 * <p><strong>Note:</strong> use only from a single thread.</p>
 *
 */
public class HeaderMatcher {

    private final FilteringSequenceFactory factory;
    private final HeaderBean[] headers;
    private CharSequence read;
    private int lines;
    
    public HeaderMatcher(final CharFilter filter, final int capacity) {
        this(filter, capacity, null);
    }
    
    public HeaderMatcher(final CharFilter filter, final int capacity, final HeaderBean[] headers) {
        factory = new FilteringSequenceFactory(capacity, filter);
        read = null;
        this.headers = headers;
    }
    
    public void read(Reader reader) throws IOException {
        final LineNumberReader lineNumberReader = new LineNumberReader(reader);
        read = factory.filter(lineNumberReader);
        if (lineNumberReader.read() == -1) {
            lines = lineNumberReader.getLineNumber();
        } else {
            lines = -1;
        }
        if (headers != null) {
            for (final HeaderBean headerBean : headers) {
                if (headerBean != null) {
                    final Pattern headerPattern = headerBean.getHeaderPattern();
                    if (headerPattern != null) {
                        final boolean matches = matches(headerPattern);
                        headerBean.setMatch(matches);
                    }
                }
            }
        }
    }
    
    /**
     * <p>Seeks a match in the last headers read.</p>
     * <p><strong>Note</strong> that this pattern
     * must not contain filtered characters.
     * </p>
     * @param pattern <code>Pattern</code> to match
     * @return true if the pattern matches,
     * false otherwise or if {@link #read(Reader)} has not been
     * called
     */
    public boolean matches(Pattern pattern) {
        boolean result = false;
        if (read != null) {
            final Matcher matcher = pattern.matcher(read);
            result = matcher.matches();
        }
        return result;
    }
    
    /**
     * Number of lines read.
     * @return the number of lines in the file
     * or -1 if the file has more lines than were read
     */
    public int lines() {
        return lines;
    }
}
