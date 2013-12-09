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
package org.apache.rat;

/**
 * Indicates that a report has failed in a fatal manner.
 */
class ReportFailedRuntimeException extends RuntimeException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7605175211996815712L;

	/**
	 * Instantiates a new report failed runtime exception.
	 */
	public ReportFailedRuntimeException() {
		super();
	}

	/**
	 * Instantiates a new report failed runtime exception.
	 * 
	 * @param message
	 *            the message
	 * @param t
	 *            the t
	 */
	public ReportFailedRuntimeException(String message, Throwable t) {
		super(message, t);
	}

	/**
	 * Instantiates a new report failed runtime exception.
	 * 
	 * @param message
	 *            the message
	 */
	public ReportFailedRuntimeException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new report failed runtime exception.
	 * 
	 * @param t
	 *            the t
	 */
	public ReportFailedRuntimeException(Throwable t) {
		super(t);
	}
}
