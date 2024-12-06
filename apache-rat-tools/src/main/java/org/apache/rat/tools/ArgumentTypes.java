/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.tools;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.function.Consumer;

import org.apache.rat.help.Help;

/**
 * A simple tool to convert CLI options to Maven and Ant format and produce a CSV file.
 * <br>
 * Options
 * <ul>
 *     <li>--ant   Produces Ant options in result</li>
 *     <li>--maven Produces Maven options in result</li>
 *     <li>--csv   Produces CSV output text is produced</li>
 * </ul>
 * Note: if neither --ant nor --maven are included both will be listed.
 */
public final class ArgumentTypes {

    private ArgumentTypes() { }

    public static void main(final String[] args) throws IOException {
        final Consumer<Writer> writerConsumer = writer -> new Help(writer).printArgumentTypes();
        if (args.length > 0) {
            try (OutputStream os = new FileOutputStream(args[0]);
                 Writer writer = new OutputStreamWriter(os, Charset.defaultCharset())) {
                writerConsumer.accept(writer);
            }
        } else {
            // Do *not* use try-with-resources, because we don't want to close System.out.
            Writer writer = new OutputStreamWriter(System.out, Charset.defaultCharset());
            writerConsumer.accept(writer);
        }
    }
}
