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

/** Replacement for {@link java.io.LineNumberReader}. This class
 * provides a workaround for an incompatibility in the
 * {@link java.io.LineNumberReader}: If the last line in a file
 * isn't terminated with LF, or CR, or CRLF, then that line
 * is counted in Java 16, and beyond, but wasn't counted before.
 * This implementation is compatible with the latter variant,
 * thus providing upwards compatibility for RAT.
 */
@Deprecated // since 0.17
public class LineNumberReader {
	private final Reader parent;
	private boolean previousCharWasCR = false;
	private int lineNumber = 0;

	public LineNumberReader(Reader pReader) {
		parent = pReader;
	}

	public int read() throws IOException {
		final int c = parent.read();
		switch(c) {
		case 13:
			previousCharWasCR = true;
			++lineNumber;
			break;
		case 10:
			if (!previousCharWasCR) {
				++lineNumber;
			}
			previousCharWasCR = false;
			break;
		default:
			previousCharWasCR = false;
			break;
		}
		return c;
	}

	public int getLineNumber() {
		return lineNumber;
	}
}
