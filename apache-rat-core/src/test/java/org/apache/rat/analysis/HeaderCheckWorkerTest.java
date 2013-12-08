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

package org.apache.rat.analysis;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import org.apache.rat.analysis.license.ApacheSoftwareLicense20;
import org.apache.rat.api.Document;
import org.apache.rat.document.MockLocation;
import org.junit.Test;

/**
 * The Class HeaderCheckWorkerTest.
 */
public class HeaderCheckWorkerTest {


	/**
	 * Not finished.
	 * 
	 */
    @Test
	public void notFinished() {
		final Document subject = new MockLocation("subject");
		HeaderCheckWorker worker = new HeaderCheckWorker(new StringReader(""),
				new ApacheSoftwareLicense20(), subject);
		assertFalse("Work status can´t be finished", worker.isFinished());
	}

	/**
	 * Checks if is finished.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void isFinished() throws IOException {
        final Document subject = new MockLocation("subject");
        HeaderCheckWorker worker = new HeaderCheckWorker(new StringReader(""), new ApacheSoftwareLicense20(), subject);
        worker.read();
		assertTrue("Work status must be finished", worker.isFinished());
    }
    
	/**
	 * Test header check worker constructor.
	 * 
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 */
    @Test
	public void testHeaderCheckWorkerConstructor()
			throws UnsupportedEncodingException {
		InputStream inputStream = new ByteArrayInputStream(
				"Test".getBytes("UTF-8"));
		Reader reader = new InputStreamReader(inputStream, "UTF-8");
		IHeaderMatcher matcher = null;
		int numberOfRetainedHeaderLine = 0;
		Document name = null;
		HeaderCheckWorker headerCheckWorker = new HeaderCheckWorker(reader,
				numberOfRetainedHeaderLine, matcher, name);
		assertNotNull("Not null", headerCheckWorker);
    }
}
