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
package org.apache.rat.config.parameters;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.apache.rat.BuilderParams;
import org.apache.rat.analysis.matchers.AndMatcher;
import org.apache.rat.analysis.matchers.CopyrightMatcher;
import org.apache.rat.api.Document;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.document.FileDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class DescriptionBuilderTest {

    @Test
    public void matcherMapBuildTest() {
        Description underTest = DescriptionBuilder.buildMap(CopyrightMatcher.class);
        assertEquals(ComponentType.MATCHER, underTest.getType());
        assertEquals("copyright", underTest.getCommonName());
        assertNotNull(underTest.getDescription());
        assertEquals(4, underTest.getChildren().size());
        assertTrue(underTest.getChildren().containsKey("id"));
        assertTrue(underTest.getChildren().containsKey("start"));
        assertTrue(underTest.getChildren().containsKey("end"));
        assertTrue(underTest.getChildren().containsKey("owner"));

        underTest = DescriptionBuilder.buildMap(AndMatcher.class);
        assertEquals(ComponentType.MATCHER, underTest.getType());
        assertEquals("all", underTest.getCommonName());
        assertNotNull(underTest.getDescription());
        assertEquals(3, underTest.getChildren().size());
        assertTrue(underTest.getChildren().containsKey("id"));
        assertTrue(underTest.getChildren().containsKey("resource"));
        assertTrue(underTest.getChildren().containsKey("enclosed"));
        Description desc = underTest.getChildren().get("enclosed");
    }

    /**
     * Test to verify that all Description based inspection invoked methods exist.
     * @param clazz The class to check
     * @param desc The description of an element in that class.
     */
    @ParameterizedTest
    @MethodSource("descriptionSource")
    public void textConfigParameters(Class<?> clazz, Description desc) {
        if (desc.getType() == ComponentType.BUILD_PARAMETER) {
            try {
                Method m = BuilderParams.class.getMethod(desc.getCommonName());
                assertNotNull(m);
            } catch (NoSuchMethodException e) {
                fail(format("BuilderParams does not have a %s method", desc.getCommonName()), e);
            }
        } else {
            try {
                // verify that the class has the getter for the description.
                assertNotNull(desc.getter(clazz));
            } catch (NoSuchMethodException e) {
                fail(format("Missing method: %s.%s", clazz, desc.methodName("get")), e);
            }
        }
    }

    /** Class to build a list of documents that are class files. */
    private static class DocumentProcessor implements Consumer<Document> {
        SortedSet<Document> documents = new TreeSet<>();
        @Override
        public void accept(Document document) {
            if (document.isDirectory()) {
                document.listChildren().forEach(this);
            } else {
                if (document.getName().getName().endsWith(".class")) {
                    documents.add(document);
                }
            }
        }
    }

    /**
     * Scans the build classes directory to locate all class files that have Description annotations and ensure its validity.
     * @return A stream of class and descriptions to validate.
     * @throws URISyntaxException if URI is incorrect.
     * @throws ClassNotFoundException if class can not be found.
     */
    public static Stream<Arguments> descriptionSource() throws URISyntaxException, ClassNotFoundException {
        List<Arguments> arguments = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource("");
        File baseDir = new File(url.toURI());
        baseDir = new File(baseDir.getParent(), "classes");
        DocumentName documentName = DocumentName.builder(baseDir).build();
        FileDocument fileDocument =  new FileDocument(documentName, baseDir, DocumentNameMatcher.MATCHES_ALL);
        DocumentProcessor processor = new DocumentProcessor();
        processor.accept(fileDocument);

        for (Document document : processor.documents) {
            String className = document.getName().localized(".");
            className = className.substring(1, className.length() - ".class".length());
            Class<?> clazz = Class.forName(className);
            List<Description> descriptionList = DescriptionBuilder.getConfigComponents(clazz);
            if (descriptionList != null) {
                descriptionList.forEach( desc -> arguments.add(Arguments.of(clazz, desc)));
            }
        }

        return arguments.stream();
    }
}
