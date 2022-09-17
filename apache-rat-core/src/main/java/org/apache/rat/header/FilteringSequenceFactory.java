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
import java.nio.CharBuffer;

class FilteringSequenceFactory {
    
    private static final int BUFFER_CAPACITY = 5000;
    
    private final CharBuffer buffer;
    private final CharFilter filter;
    
    public FilteringSequenceFactory(final CharFilter filter) {
        this(BUFFER_CAPACITY, filter);
    }
    
    public FilteringSequenceFactory(final int capacity, final CharFilter filter) {
        this.buffer = CharBuffer.allocate(capacity);
        this.filter = filter;
    }

    public CharSequence filter(Reader reader) throws IOException {
    	return filter(new LineNumberReader(reader));
    }

    public CharSequence filter(LineNumberReader reader) throws IOException {
        buffer.clear();
        boolean eof = false;
        while(!eof) {
            final int next = reader.read();
            if (next == -1 || !buffer.hasRemaining()) {
                eof = true;
            } else {
                final char character = (char) next;
                if (!filter.isFilteredOut(character))
                {
                    buffer.put(character); 
                }
            }
        }
        buffer.limit(buffer.position()).rewind();
        return buffer;
    }
}
