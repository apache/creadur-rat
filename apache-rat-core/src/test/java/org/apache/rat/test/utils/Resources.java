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
package org.apache.rat.test.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Utility class, which provides static methods for creating test cases.
 */
public class Resources {
    /**
     * Private constructor, to prevent accidental instantiation.
     */
    private Resources() {
        // Does nothing
    }

    public static final String SRC_TEST_RESOURCES = "src/test/resources";
    public static final String SRC_MAIN_RESOURCES = "src/main/resources";
    private static final File TEST_RESOURCE_BASE_PATH = new File(SRC_TEST_RESOURCES);
    private static final File RESOURCE_BASE_PATH = new File(SRC_MAIN_RESOURCES);

    /**
     * Locates a test resource file in the classpath.
     */
    public static File getResourceFile(String pResource) throws IOException {
        File f = getResourceFromBase(TEST_RESOURCE_BASE_PATH, pResource);
        if (!f.isFile()) {
            throw new FileNotFoundException("Unable to locate resource file: " + pResource);
        }
        return f;
    }

    /**
     * Locates a test resource directory in the classpath.
     */
    public static File getResourceDirectory(String pResource) throws IOException {
        File f = getResourceFromBase(TEST_RESOURCE_BASE_PATH, pResource);
        if (!f.isDirectory()) {
            throw new FileNotFoundException("Unable to locate resource directory: " + pResource);
        }
        return f;
    }

    /**
     * Locates a file in the unpacked example data archive.
     * @param pResource the name of the resource to find.
     * @return the File for the resource.
     * @throws URISyntaxException in case of path I/O errors with the given resource.
     */
    public static File getExampleResource(String pResource) throws URISyntaxException {
        URL url = Resources.class.getResource("/examples/" + pResource);
        Objects.requireNonNull(url, "/examples/" + pResource + " not found");
        return Paths.get(url.toURI()).toFile();
    }

    /**
     * Try to load the given file from baseDir. In case of errors try to add
     * module names to fix behaviour from within IntelliJ.
     */
    private static File getResourceFromBase(File baseDir, String pResource) throws IOException {
        File f = new File(baseDir, pResource);
        return f.getCanonicalFile();
    }

    /**
     * Locates a set of resource files in the class path. In case of errors try to
     * add module names to fix behaviour from within IntelliJ.
     */
    public static File[] getResourceFiles(String pResource) throws IOException {
        File f = new File(TEST_RESOURCE_BASE_PATH, pResource).getCanonicalFile();
        if (!f.isDirectory()) {
            throw new FileNotFoundException("Unable to locate resource directory: " + pResource);
        }

        return f.listFiles(File::isFile);
    }

    /**
     * Locates a resource file in the classpath and returns an {@link InputStream}.
     */
    public static InputStream getResourceStream(String pResource) throws IOException {
        return Files.newInputStream(getResourceFile(pResource).toPath());
    }

    /**
     * Locates a resource file in the classpath and returns a {@link Reader}.
     */
    public static Reader getResourceReader(String pResource) throws IOException {
        return new InputStreamReader(getResourceStream(pResource), StandardCharsets.UTF_8);
    }

    /**
     * Locates a resource file in the classpath and returns a
     * {@link BufferedReader}.
     */
    public static BufferedReader getBufferedReader(File file) throws IOException {
        return new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8));
    }
}
