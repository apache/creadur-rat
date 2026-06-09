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

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.apache.rat.document.FSInfoTest.WINDOWS;

/**
 * Tests the DcoumentName.Builder class
 */
public class DocumentNameBuilderTest {

    private static final DocumentName.FSInfo[] TEST_SUITE = FSInfoTest.TEST_SUITE;

    /**
     * Validates the data in a document name matches expected data.
     * @param documentName the document name to check
     * @param name the expected fully qualified name.
     * @param shortName the name for the last segment of the name.
     * @param baseName the name of the base document.
     * @param root the root the document is in.
     * @param directorySeparator the expected directory separator.
     * @param isCaseSensitive the expected case sensitivity.
     * @param localized the default localized name (e.g. path and file name from base name).
     * @param localizedArg the localized name with directory separator set to '+'
     */
    void assertDocumentName(DocumentName documentName, String name, String shortName, String baseName, String root,
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
        assertThat(documentName.localized("+")).as("Invalid localized('+')").isEqualTo(localizedArg);
    }

    /**
     * Verifies tha the baseName is not modified when used in the builder.
     * Base name is default root + OS name.  For example C:\windows, or /unix
     * @param fsInfo the file system info for the test.
     */
    @ParameterizedTest
    @FieldSource("TEST_SUITE")
    void baseNamePreserved(DocumentName.FSInfo fsInfo) {
        final String root = fsInfo.roots()[0];
        final String baseNameStr = root + fsInfo;
        // create a document {os name}/bar.  Used to establish basename in builder.
        final DocumentName siblingName = DocumentName.builder(fsInfo).setName("bar").setBaseName(fsInfo.toString()).build();

        // check a relative name does not change base name.
        String nameStr = fsInfo.mkPath("foo", "baz");
        DocumentName documentName = DocumentName.builder(siblingName).setName(nameStr).build();
        String expected = root + fsInfo.mkPath(fsInfo.toString(), "foo", "baz");
        assertThat(documentName.getName()).as("relative value").isEqualTo(expected);
        assertDocumentName(documentName, expected, "baz", baseNameStr, root, fsInfo.dirSeparator(), fsInfo.isCaseSensitive(),
                fsInfo.dirSeparator() + nameStr, "+foo+baz");

        // check a FQName results in the base name not being changed.
        documentName = DocumentName.builder(siblingName).setName(expected).build();
        assertThat(documentName.getName()).as("absolute value").isEqualTo(expected);
        assertDocumentName(documentName, expected, "baz", baseNameStr, root, fsInfo.dirSeparator(), fsInfo.isCaseSensitive(),
                fsInfo.dirSeparator() + nameStr, "+foo+baz");

    }

    @ParameterizedTest
    @FieldSource("TEST_SUITE")
    void documentNameFromFQNameWithBaseName(DocumentName.FSInfo fsInfo) {
        final String root = fsInfo.roots()[0];
        final String baseNameStr = root + fsInfo;
        String fqName = root + fsInfo.mkPath(fsInfo.toString(), "foo");
        DocumentName documentName = DocumentName.builder(fsInfo).setName(fqName).setBaseName(baseNameStr).build();
        assertDocumentName(documentName, fqName, "foo", baseNameStr, root, fsInfo.dirSeparator(), fsInfo.isCaseSensitive(),
                fsInfo.dirSeparator() + "foo", "+foo");
    }

    @ParameterizedTest
    @FieldSource("TEST_SUITE")
    void noBaseNameThowsException(DocumentName.FSInfo fsInfo) {
        final String root = fsInfo.roots()[0];
        String fqName = root + fsInfo.mkPath(fsInfo.toString(), "foo");
        assertThatThrownBy(() -> DocumentName.builder(fsInfo).setName(fqName).build())
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Basename must not be null");
    }

    @ParameterizedTest
    @FieldSource("TEST_SUITE")
    void noNameThowsException(DocumentName.FSInfo fsInfo) {
        final String root = fsInfo.roots()[0];
        String fqName = root + fsInfo.mkPath(fsInfo.toString(), "foo");
        assertThatThrownBy(() -> DocumentName.builder(fsInfo).setBaseName(fqName).build())
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Name must not be null");
    }


    @ParameterizedTest
    @FieldSource("TEST_SUITE")
    void DocumentNameFromDocumentName(DocumentName.FSInfo fsInfo) {
        final String root = fsInfo.roots()[0];
        final String baseNameStr = root + fsInfo;
        String fqName = root + fsInfo.mkPath(fsInfo.toString(), "foo");
        DocumentName expected = DocumentName.builder(fsInfo).setName(fqName).setBaseName(baseNameStr).build();

        DocumentName actual = DocumentName.builder(expected).build();
        assertThat(actual).isEqualTo(expected);

        assertDocumentName(actual, fqName, "foo", baseNameStr, root, fsInfo.dirSeparator(), fsInfo.isCaseSensitive(),
                fsInfo.dirSeparator() + "foo", "+foo");

    }

    @ParameterizedTest
    @FieldSource("TEST_SUITE")
    void builderOnDocumentNameWithNameSharesBaseName(DocumentName.FSInfo fsInfo) {
        final String root = fsInfo.roots()[0];
        final String baseNameStr = root + fsInfo;

        String fqName = root + fsInfo.mkPath(fsInfo.toString(), "foo");
        DocumentName firstName = DocumentName.builder(fsInfo).setName(fqName).setBaseName(baseNameStr).build();

        fqName = root + fsInfo.mkPath(fsInfo.toString(), "bar");
        DocumentName actual = DocumentName.builder(firstName).setName(fqName).setBaseName(baseNameStr).build();

        assertDocumentName(actual, fqName, "bar", baseNameStr, root, fsInfo.dirSeparator(), fsInfo.isCaseSensitive(),
                fsInfo.dirSeparator() + "bar", "+bar");
    }

    @ParameterizedTest
    @FieldSource("TEST_SUITE")
    void builderOnFile(DocumentName.FSInfo fsInfo) {
        final String root = fsInfo.roots()[0];
        final String baseNameStr = root + fsInfo.mkPath(fsInfo.toString(), "foo");
        final String fqName = root + fsInfo.mkPath(fsInfo.toString(), "foo", "bar");
        File file = mock(File.class);
        File parent = mock(File.class);
        when(file.getAbsolutePath()).thenReturn(fqName);
        when(file.getParentFile()).thenReturn(parent);
        when(file.isDirectory()).thenReturn(false);
        when(parent.getAbsolutePath()).thenReturn(baseNameStr);
        when(parent.isDirectory()).thenReturn(true);

        DocumentName actual = new DocumentName.Builder(fsInfo, file).build();

        assertDocumentName(actual, fqName, "bar", baseNameStr, root, fsInfo.dirSeparator(), fsInfo.isCaseSensitive(),
                fsInfo.dirSeparator() + "bar", "+bar");

    }

    @Test
    void windowRootDifference() {
        // verify that setting the root results in the entire DocumentName being re-rooted e.g. including the basename(s).
        DocumentName.FSInfo fsInfo = WINDOWS;
        String root = fsInfo.roots()[0];
        String baseNameStr = root + fsInfo.mkPath(fsInfo.toString(), "foo");
        String fqName = root + fsInfo.mkPath(fsInfo.toString(), "foo", "bar");
        DocumentName firstName = DocumentName.builder(fsInfo).setName(fqName).setBaseName(baseNameStr).build();

        root = "D:\\";
        DocumentName actual = DocumentName.builder(firstName).setRoot(root).build();

        baseNameStr = root + fsInfo.mkPath(fsInfo.toString(), "foo");
        fqName = root + fsInfo.mkPath(fsInfo.toString(), "foo", "bar");

        assertDocumentName(actual, fqName, "bar", baseNameStr, root, fsInfo.dirSeparator(), fsInfo.isCaseSensitive(),
                fsInfo.dirSeparator() + "bar", "+bar");

    }

    @ParameterizedTest
    @FieldSource("TEST_SUITE")
    void setRootNull(DocumentName.FSInfo fsInfo) {
        String root = fsInfo.roots()[0];
        String baseNameStr = root + fsInfo.mkPath(fsInfo.toString(), "foo");
        String fqName = root + fsInfo.mkPath(fsInfo.toString(), "foo", "bar");
        DocumentName firstName = DocumentName.builder(fsInfo).setName(fqName).setBaseName(baseNameStr).build();

        // verify setting the root to null results in relative names with a blank root set
        root = null;
        DocumentName actual = DocumentName.builder(firstName).setRoot(root).build();

        baseNameStr = fsInfo.mkPath(fsInfo.toString(), "foo");
        fqName = fsInfo.mkPath(fsInfo.toString(), "foo", "bar");

        assertDocumentName(actual, fqName, "bar", baseNameStr, "", fsInfo.dirSeparator(), fsInfo.isCaseSensitive(),
                fsInfo.dirSeparator() + "bar", "+bar");

    }

    @ParameterizedTest
    @FieldSource("TEST_SUITE")
    void setRootEmpty(DocumentName.FSInfo fsInfo) {
        String root = fsInfo.roots()[0];
        String baseNameStr = root + fsInfo.mkPath(fsInfo.toString(), "foo");
        String fqName = root + fsInfo.mkPath(fsInfo.toString(), "foo", "bar");
        DocumentName firstName = DocumentName.builder(fsInfo).setName(fqName).setBaseName(baseNameStr).build();

        root = "";
        DocumentName actual = DocumentName.builder(firstName).setRoot(root).build();

        baseNameStr = root + fsInfo.mkPath(fsInfo.toString(), "foo");
        fqName = root + fsInfo.mkPath(fsInfo.toString(), "foo", "bar");

        assertDocumentName(actual, fqName, "bar", baseNameStr, root, fsInfo.dirSeparator(), fsInfo.isCaseSensitive(),
                fsInfo.dirSeparator() + "bar", "+bar");

    }

    @ParameterizedTest
    @FieldSource("TEST_SUITE")
    void splitRootsTest(DocumentName.FSInfo fsInfo) {
        String root = fsInfo.roots()[0];
        String path = fsInfo.mkPath("My", "path", "to", "a", "file.txt");
        Pair<String, String> result = DocumentName.builder(fsInfo).splitRoot(root + path);
        assertThat(result.getLeft()).isEqualTo(root);
        assertThat(result.getRight()).isEqualTo(path);
    }
}
