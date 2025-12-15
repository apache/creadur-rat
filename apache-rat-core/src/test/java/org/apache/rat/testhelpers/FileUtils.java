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
package org.apache.rat.testhelpers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Fail.fail;

public class FileUtils {

    /**
     * Creates a directory if it does not exist.
     * @param dir the directory to make.
     */
    public static void mkDir(File dir) {
        boolean ignored = dir.mkdirs();
    }

    /**
     * Deletes a file if it exists.
     * @param file the file to delete.
     */
    public static void delete(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                try {
                    org.apache.commons.io.FileUtils.deleteDirectory(file);
                } catch (IOException ignore) {
                    //
                }
            } else {
                boolean ignored = file.delete();
            }
        }
    }

    /**
     * Writes a test file.
     * @param dir The directory to write the file in.
     * @param name the name of the file.
     * @param lines the lines to write into the file.
     * @return the new File.
     */
    static public File writeFile(File dir, final String name, final Iterable<String> lines) {
        if (dir == null) {
            fail("base directory not specified");
        }
        mkDir(dir);
        File file = new File(dir, name);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            lines.forEach(writer::println);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return file;
    }

    /**
     * Writes a test file.
     * @param dir The directory to write the file in.
     * @param name the name of the file.
     * @param lines the lines to write into the file.
     * @return the new File.
     */
    static public File writeFile(File dir, final String name, final String... lines) {
        return writeFile(dir, name, Arrays.asList(lines));
    }

    /**
     * Writes a text file containing the name of the file.
     * @param dir The directory to write the file into.
     * @param name the name of the file.
     * @return the new file.
     */
    public static File writeFile(File dir, String name) {
        return writeFile(dir, name, Collections.singletonList(name));
    }
}
