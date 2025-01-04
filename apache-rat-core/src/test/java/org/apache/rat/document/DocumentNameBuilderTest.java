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
package org.apache.rat.document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.apache.rat.document.FSInfoTest.WINDOWS;


public class DocumentNameBuilderTest {

    @ParameterizedTest(name="{0}")
    @MethodSource("buildTestData")
    void buildTest(String testName, DocumentName documentName, String name, String shortName, String baseName, String root,
                   String directorySeparator, Boolean isCaseSensitive, String localized, String localizedArg) {
        assertThat(documentName.getName()).as("Invalid name").isEqualTo(name);
        assertThat(documentName.getShortName()).as("Invalid short name").isEqualTo(shortName);
        assertThat(documentName.getBaseName()).as("Invalid base name").isEqualTo(baseName);
        assertThat(documentName.getRoot()).as("Invalid root").isEqualTo(root);
        assertThat(documentName.getDirectorySeparator()).as("Invalid directory separator").isEqualTo(directorySeparator);
        if (isCaseSensitive) {
            assertThat(documentName.isCaseSensitive()).as("Invalid case sensitivity").isTrue();
        } else {
            assertThat(documentName.isCaseSensitive()).as("Invalid case sensitivity").isFalse();
        }
        assertThat(documentName.localized()).as("Invalid localized ").isEqualTo(localized);
        final String sep = documentName.getDirectorySeparator().equals("/") ? "\\" : "/";
        assertThat(documentName.localized(sep)).as(() -> String.format("Invalid localized('%s')", sep)).isEqualTo(localizedArg);
    }


    static Stream<Arguments> buildTestData() throws IOException {
        List<Arguments> lst = new ArrayList<>();

            //
            String testName = "windows\\foo direct";
            DocumentName documentName = DocumentName.builder(WINDOWS).setName("C:\\\\windows\\foo").setBaseName("C:\\\\windows").build();
            lst.add(Arguments.of( testName, documentName, "C:\\\\windows\\foo", "foo", "C:\\\\windows", "C:\\", "\\", false,
                    "\\foo", "/foo"));
            DocumentName baseName = documentName;

            //
            testName = "builder(docName)";
            documentName = DocumentName.builder(baseName).build();
            lst.add(Arguments.of( testName, documentName, "C:\\\\windows\\foo", "foo", "C:\\\\windows", "C:\\", "\\", false,
                    "\\foo", "/foo"));

            //
            testName = "windows\\foo\\bar by resolve";
            documentName = baseName.resolve("bar");
            lst.add(Arguments.of(testName, documentName, "C:\\\\windows\\foo\\bar", "bar", "C:\\\\windows", "C:\\", "\\", false,
                    "\\foo\\bar", "/foo/bar"));

            //
            testName = "windows\\foo\\direct by basename";
            documentName = DocumentName.builder(baseName).setName("windows\\foo\\direct").build();
            lst.add(Arguments.of(testName, documentName, "C:\\\\windows\\foo\\direct", "direct", "C:\\\\windows", "C:\\", "\\", false,
                    "\\foo\\direct", "/foo/direct"));

            //
            testName = "windows\\foo\\bar by file";
            File file = mock(File.class);
            File parent = mock(File.class);
            when(file.getAbsolutePath()).thenReturn("C:\\\\windows\\foo\\bar");
            when(file.getParentFile()).thenReturn(parent);
            when(file.isDirectory()).thenReturn(false);
            when(parent.getAbsolutePath()).thenReturn("C:\\\\windows\\foo");
            when(parent.isDirectory()).thenReturn(true);
            documentName = new DocumentName.Builder(WINDOWS, file).build();
            lst.add(Arguments.of(testName, documentName, "C:\\\\windows\\foo\\bar", "bar", "C:\\\\windows\\foo", "C:\\", "\\", false,
                    "\\bar", "/bar"));

        //
        testName = "windows\\foo\\bar by directory";
        file = mock(File.class);
        parent = mock(File.class);
        when(file.getAbsolutePath()).thenReturn("C:\\\\windows\\foo\\bar");
        when(file.getParentFile()).thenReturn(parent);
        when(file.isDirectory()).thenReturn(true);
        when(parent.getAbsolutePath()).thenReturn("C:\\\\windows\\foo");
        when(parent.isDirectory()).thenReturn(true);
        documentName = new DocumentName.Builder(WINDOWS, file).build();
        lst.add(Arguments.of(testName, documentName, "C:\\\\windows\\foo\\bar", "bar", "C:\\\\windows\\foo\\bar", "C:\\", "\\", false,
                "\\", "/"));

            //
            testName = "windows setRoot";
            documentName = DocumentName.builder(baseName).setRoot("D:\\").build();
            lst.add(Arguments.of(testName, documentName, "D:\\\\windows\\foo", "foo", "C:\\\\windows", "D:\\", "\\", false,
                    "D:\\\\windows\\foo", "D://windows/foo"));

            testName = "windows setRoot(null)";
            documentName = DocumentName.builder(baseName).setRoot(null).build();
            lst.add(Arguments.of(testName, documentName, "\\windows\\foo", "foo", "C:\\\\windows", "", "\\", false,
                    "\\windows\\foo", "/windows/foo"));

            testName = "windows setRoot('')";
            documentName = DocumentName.builder(baseName).setRoot("").build();
            lst.add(Arguments.of(testName, documentName, "\\windows\\foo", "foo", "C:\\\\windows", "", "\\", false,
                    "\\windows\\foo", "/windows/foo"));

            //
            testName = "windows setName('baz')";
            documentName = DocumentName.builder(baseName).setName("baz").build();
            lst.add(Arguments.of(testName, documentName, "C:\\\\windows\\baz", "baz", "C:\\\\windows", "C:\\", "\\", false,
                    "\\baz", "/baz"));

            testName = "windows setName((String)null)";
            documentName = DocumentName.builder(baseName).setName((String)null).build();
            lst.add(Arguments.of(testName, documentName, "C:\\\\windows", "windows", "C:\\\\windows", "C:\\", "\\", false,
                    "\\", "/"));

            testName = "windows setName('')";
            documentName = DocumentName.builder(baseName).setName("").build();
            lst.add(Arguments.of(testName, documentName, "C:\\\\windows", "windows", "C:\\\\windows", "C:\\", "\\", false,
                    "\\", "/"));

        file = mock(File.class);
        parent = mock(File.class);
        when(file.getAbsolutePath()).thenReturn("C:\\\\windows\\foo\\bar");
        when(file.getParentFile()).thenReturn(parent);
        when(file.isDirectory()).thenReturn(false);
        when(parent.getAbsolutePath()).thenReturn("C:\\\\windows\\foo");
        when(parent.isDirectory()).thenReturn(true);
            testName = "windows setName(file)";
            documentName = DocumentName.builder(baseName).setName(file).build();
            lst.add(Arguments.of(testName, documentName, "C:\\\\windows\\foo\\bar", "bar", "C:\\\\windows\\foo", "C:\\", "\\", false,
                    "\\bar", "/bar"));

        file = mock(File.class);
        parent = mock(File.class);
        when(file.getAbsolutePath()).thenReturn("C:\\\\windows\\foo\\bar");
        when(file.getParentFile()).thenReturn(parent);
        when(file.isDirectory()).thenReturn(true);
        when(parent.getAbsolutePath()).thenReturn("C:\\\\windows\\foo");
        when(parent.isDirectory()).thenReturn(true);
        testName = "windows setName(directory)";
        documentName = DocumentName.builder(baseName).setName(file).build();
        lst.add(Arguments.of(testName, documentName, "C:\\\\windows\\foo\\bar", "bar", "C:\\\\windows\\foo\\bar", "C:\\", "\\", false,
                "\\", "/"));

        return lst.stream();


//
//        /**
//         * Sets the baseName.
//         * Will set the root if it is not set.
//         * <p>
//         *     To correctly parse the string it must use the directory separator specified by this builder.
//         * </p>
//         * @param baseName the basename to use.
//         * @return this.
//         */
//        public DocumentName.Builder setBaseName(final String baseName) {
//            DocumentName.Builder builder = DocumentName.builder(fsInfo).setName(baseName);
//            builder.sameNameFlag = true;
//            setBaseName(builder);
//            return this;
//        }
//
//        /**
//         * Sets the basename from the {@link #name} of the specified DocumentName.
//         * Will set the root the baseName has the root set.
//         * @param baseName the DocumentName to set the basename from.
//         * @return this.
//         */
//        public DocumentName.Builder setBaseName(final DocumentName baseName) {
//            this.baseName = baseName;
//            if (!baseName.getRoot().isEmpty()) {
//                this.root = baseName.getRoot();
//            }
//            return this;
//        }
//
//        /**
//         * Executes the builder, sets the base name and clears the sameName flag.
//         * @param builder the builder for the base name.
//         */
//        private void setBaseName(DocumentName.Builder builder) {
//            this.baseName = builder.build();
//            this.sameNameFlag = false;
//        }
//
//        /**
//         * Sets the basename from a File. Sets {@link #root} and the {@link #baseName}
//         * Will set the root.
//         * @param file the file to set the base name from.
//         * @return this.
//         */
//        public DocumentName.Builder setBaseName(final File file) {
//            DocumentName.Builder builder = DocumentName.builder(fsInfo).setName(file);
//            builder.sameNameFlag = true;
//            setBaseName(builder);
//            return this;
//        }
//
//        /**
//         * Build a DocumentName from this builder.
//         * @return A new DocumentName.
//         */
//        public DocumentName build() {
//            verify();
//            return new DocumentName(this);
//        }

//
//            assertThat(documentName.getName()).isEqualTo("C:\\\\windows\\foo")
//
//
//            assertThat(documentName.getRoot()).isEqualTo("C:\\");
//            assertThat(documentName.isCaseSensitive()).isFalse();
//            assertThat(documentName.getShortName()).isEqualTo("foo");
//            assertThat(documentName.localized("/")).isEqualTo("/windows/foo");
//            assertThat(documentName.getBaseName()).isEqualTo("C:\\\\windows");
//            documentName.getBaseDocumentName();
//            documentName.getDirectorySeparator();
//
//
//            public String getName()
//
//
//            public String getBaseName()
//
//
//            public String getRoot()
//
//
//            public DocumentName getBaseDocumentName
//
//            public String getDirectorySeparator(
//
//
//            public String localized()
//
//            public String localized(final String dirSeparator)
//
//
//            public String[] tokenize(final String source) {
//               // return source.split("\\Q" + fsInfo.dirSeparator() + "\\E");
//
//
//
//            public String getShortName()
//
//            public boolean isCaseSensitive()


/*
            @Override
            public int compareTo(final DocumentName other) {
                return CompareToBuilder.reflectionCompare(this, other);
            }

            @Override
            public boolean equals(final Object other) {
                return EqualsBuilder.reflectionEquals(this, other);
            }

            @Override
            public int hashCode() {
                return HashCodeBuilder.reflectionHashCode(this);
            }

*/

    }


}
