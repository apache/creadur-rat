package org.apache.rat.report.analyser;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.analysis.RatHeaderAnalysisException;
import org.apache.rat.document.IDocument;
import org.apache.rat.report.RatReportFailedException;
import org.apache.rat.report.claim.IClaimReporter;
import org.apache.rat.report.claim.LicenseFamilyCode;
import org.apache.rat.report.claim.LicenseFamilyName;
import org.apache.rat.report.claim.impl.LicenseFamilyClaim;

/**
 * <p>Reads from a stream to check license.</p>
 * <p><strong>Note</strong> that this class is not thread safe.</p> 
 */
class HeaderCheckWorker {

    public static final int DEFAULT_NUMBER_OF_RETAINED_HEADER_LINES = 50;
	
	private final int numberOfRetainedHeaderLines;
	private final BufferedReader reader;
	private final IHeaderMatcher matcher;
	private final IClaimReporter reporter;
    private final IDocument name;
    
	private boolean match = false;
	
	private int headerLinesToRead;
	private boolean finished = false;

	public HeaderCheckWorker(Reader reader, int numberOfRetainedHeaderLine, 
            final IHeaderMatcher matcher, final IClaimReporter reporter, final IDocument name) {
		this(new BufferedReader(reader), numberOfRetainedHeaderLine, matcher, reporter, name);
	}
	
	
	/**
	 * Convenience constructor wraps given <code>Reader</code> 
	 * in a <code>BufferedReader</code>.
	 * @param name the name of the checked content, possibly null
	 * @param reader a <code>Reader</code> for the content, not null
	 */
	public HeaderCheckWorker(Reader reader, final IHeaderMatcher matcher, final IClaimReporter reporter, final IDocument name) {
		this(new BufferedReader(reader), matcher, reporter, name);
	}
	
	public HeaderCheckWorker(BufferedReader reader, final IHeaderMatcher matcher,
            final IClaimReporter reporter, final IDocument name) {
		this(reader, DEFAULT_NUMBER_OF_RETAINED_HEADER_LINES, matcher, reporter, name);
	}
	
	public HeaderCheckWorker(BufferedReader reader, int numberOfRetainedHeaderLine, final IHeaderMatcher matcher,
            final IClaimReporter reporter, final IDocument name) {
		this.reader = reader;
		this.numberOfRetainedHeaderLines = numberOfRetainedHeaderLine;
		this.matcher = matcher;
        this.reporter = reporter;
        this.name = name;
	}

	public boolean isFinished() {
		return finished;
	}

	public void read() throws RatHeaderAnalysisException {
		if (!finished) {
			final StringBuffer headers = new StringBuffer();
			headerLinesToRead = numberOfRetainedHeaderLines;
			try {
				while(readLine(headers));
				if (!match) {
					final String notes = headers.toString();
					reporter.claim(new LicenseFamilyClaim(name, LicenseFamilyName.UNKNOWN_LICENSE_FAMILY, LicenseFamilyCode.UNKNOWN, notes));
				}
			} catch (IOException e) {
                throw new RatHeaderAnalysisException("Cannot read header for " + name, e);
			} catch (RatReportFailedException e) {
                throw new RatHeaderAnalysisException("Cannot write claim for " + name, e);
            }
			try {
				reader.close();
			} catch (IOException e) {
				// swallow
			}
            matcher.reset();
		}
		finished = true;
	}
	
	boolean readLine(StringBuffer headers) throws IOException, RatHeaderAnalysisException {
		String line = reader.readLine();
		boolean result = line != null;
		if (result) {
			if (headerLinesToRead-- > 0) {
				headers.append(line);
				headers.append('\n');
			}
            match = matcher.match(name, line, reporter);
			result = !match;
		}
		return result;
	}
}
