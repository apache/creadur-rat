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
package org.apache.rat.api;

/**
 * The Class RatException.
 */
public class RatException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4940711222435919034L;

	/**
	 * Instantiates a new rat exception.
	 */
	public RatException() {
		super();
	}

	/**
	 * Instantiates a new rat exception.
	 * 
	 * @param message
	 *            the message
	 * @param cause
	 *            the cause
	 */
	public RatException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Instantiates a new rat exception.
	 * 
	 * @param message
	 *            the message
	 */
	public RatException(String message) {
		super(message);
	}

	/**
	 * Instantiates a new rat exception.
	 * 
	 * @param cause
	 *            the cause
	 */
	public RatException(Throwable cause) {
		super(cause);
	}
}
