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
/**
 * Classes that update files with license headers.
 * The appender owns file I/O, cleanup, replacement and header formatting;
 * format-specific editors own header placement. Detected document metadata
 * is reused when an extension does not identify a supported syntax. Generic
 * plain text is not sufficient to select a comment style.
 *
 * Internal enums describe file types, comment styles and header placement.
 * The protected integer-based typing and formatting hooks remain available
 * for subclasses; their stable IDs are explicit and independent of enum order.
 *
 * Markdown and AsciiDoc share YAML, TOML and JSON front matter handling. This
 * metadata remains before the format-specific license comment. Delimiter-based
 * blocks and JSON object boundaries are checked without parsing the metadata values.
 *
 * AsciiDoc headers retain their title, author/revision lines and attributes
 * before the license block comment. Header detection stops at the first blank
 * line, so attributes in the body do not affect license placement.
 *
 * New formats should provide a dedicated editor for
 * preserving their preamble and a matching license comment style, rather
 * than adding placement logic to the appender.
 */
package org.apache.rat.annotation;
