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
package org.apache.rat.annotation;

import java.io.File;

/**
 * Add an Apache Licence V2 licence header to a document. This appender does not
 * check for the existence of an existing licence header, it is assumed that
 * either a second licence header is intentional or that there is no licence
 * header present already.
 * 
 */
public class ApacheV2LicenceAppender extends AbstractLicenceAppender {

	/** The copyright. */
	private String copyright;

	private static final String EMPTY = "";

	/**
	 * Create a licence appender with the standard ASF licence header.
	 */
	public ApacheV2LicenceAppender() {
		super();
	}

	/**
	 * Create a licence appender with the given copyright line. This should be
	 * of the form "Copyright 2008 Foo"
	 * 
	 * @param copyright
	 *            the copyright
	 */
	public ApacheV2LicenceAppender(final String copyright) {
		super();
		this.copyright = copyright;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.rat.annotation.AbstractLicenceAppender#getLicenceHeader(java
	 * .io.File)
	 */
	@Override
	public String getLicenceHeader(final File document) {
		int type = getType(document);
		StringBuilder stringBuilder = new StringBuilder();
		if (copyright == null) {
			stringBuilder.append(getFirstLine(type));
			stringBuilder.append(getLine(type, EMPTY));
			stringBuilder
					.append(getLine(type,
					"Licensed to the Apache Software Foundation (ASF) under one"));
			stringBuilder
					.append(getLine(type,
					"or more contributor license agreements.  See the NOTICE file"));
			stringBuilder.append(getLine(type,
					"distributed with this work for additional information"));
			stringBuilder
					.append(getLine(type,
					"regarding copyright ownership.  The ASF licenses this file"));
			stringBuilder.append(getLine(type,
					"to you under the Apache License, Version 2.0 (the"));
			stringBuilder
					.append(getLine(type,
					"\"License\"); you may not use this file except in compliance"));
			stringBuilder
					.append(getLine(type,
					"with the License.  You may obtain a copy of the License at"));
			stringBuilder.append(getLine(type, EMPTY));
			stringBuilder.append(getLine(type,
					"  http://www.apache.org/licenses/LICENSE-2.0"));
			stringBuilder.append(getLine(type, EMPTY));
			stringBuilder
					.append(getLine(type,
					"Unless required by applicable law or agreed to in writing,"));
			stringBuilder
					.append(getLine(type,
					"software distributed under the License is distributed on an"));
			stringBuilder
					.append(getLine(type,
					"\"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY"));
			stringBuilder
					.append(getLine(type,
					"KIND, either express or implied.  See the License for the"));
			stringBuilder.append(getLine(type,
					"specific language governing permissions and limitations"));
			stringBuilder.append(getLine(type, "under the License."));
			stringBuilder.append(getLine(type, EMPTY));
			stringBuilder.append(getLastLine(type));
		} else {
			stringBuilder.append(getFirstLine(type));
			stringBuilder.append(getLine(type, EMPTY));
			stringBuilder.append(getLine(type, copyright));
			stringBuilder.append(getLine(type, EMPTY));
			stringBuilder
					.append(getLine(type,
					"Licensed under the Apache License, Version 2.0 (the \"License\");"));
			stringBuilder
					.append(getLine(type,
					"you may not use this file except in compliance with the License."));
			stringBuilder.append(getLine(type,
					"You may obtain a copy of the License at"));
			stringBuilder.append(getLine(type, EMPTY));
			stringBuilder.append(getLine(type,
					"  http://www.apache.org/licenses/LICENSE-2.0"));
			stringBuilder.append(getLine(type, EMPTY));
			stringBuilder
					.append(getLine(type,
					"Unless required by applicable law or agreed to in writing,"));
			stringBuilder
					.append(getLine(type,
					"software distributed under the License is distributed on an"));
			stringBuilder
					.append(getLine(type,
					"\"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY"));
			stringBuilder
					.append(getLine(type,
					"KIND, either express or implied.  See the License for the"));
			stringBuilder.append(getLine(type,
					"specific language governing permissions and limitations"));
			stringBuilder.append(getLine(type, "under the License."));
			stringBuilder.append(getLine(type, EMPTY));
			stringBuilder.append(getLastLine(type));
		}
		return stringBuilder.toString();
	}

}
