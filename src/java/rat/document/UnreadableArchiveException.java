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
package rat.document;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.apache.commons.lang.exception.Nestable;
import org.apache.commons.lang.exception.NestableDelegate;

/**
 * Indicates that an archive is unreadable.
 *
 */
public class UnreadableArchiveException extends IOException implements Nestable {

    private static final String MESSAGE = "Archive is unreadable";

    private static final long serialVersionUID = -1556313036805276658L;

    private final NestableDelegate delegate = new NestableDelegate(this);
    private final Throwable cause;
    
    public UnreadableArchiveException() {
        this(MESSAGE, null);
    }

    public UnreadableArchiveException(String s) {
        this(s, null);
    }
    
    public UnreadableArchiveException(Throwable cause) {
        this(MESSAGE, cause);
    }
    
    public UnreadableArchiveException(String s, Throwable cause) {
        super(s);
        this.cause = cause;
    }
    
    public Throwable getCause() {
        return cause;
    }
    
    public String getMessage() {
        final String result = super.getMessage();
        return result;
    }
    
    public String getMessage(int index) {
        String result = null;
        if (index == 0) {
            result = getMessage();
        } else {
            result = delegate.getMessage(index);
        }
        return result;
    }

    public String[] getMessages() {
        final String[] messages = delegate.getMessages();
        return messages;
    }

    public Throwable getThrowable(int index) {
        final Throwable result = delegate.getThrowable(index);
        return result;
    }

    public int getThrowableCount() {
        final int throwableCount = delegate.getThrowableCount();
        return throwableCount;
    }

    public Throwable[] getThrowables() {
        final Throwable[] throwables = delegate.getThrowables();
        return throwables;
    }

    public int indexOfThrowable(Class type) {
        final int result = delegate.indexOfThrowable(type, 0);
        return result;
    }

    public int indexOfThrowable(Class type, int fromIndex) {
        final int result = delegate.indexOfThrowable(type, fromIndex);
        return result;
    }

    public void printStackTrace() {
        delegate.printStackTrace();
    }

    public void printStackTrace(PrintStream s) {
        delegate.printStackTrace(s);
    }

    public void printStackTrace(PrintWriter s) {
        delegate.printStackTrace(s);
    }

    public void printPartialStackTrace(PrintWriter out) {
        super.printStackTrace(out);
    }
}    
