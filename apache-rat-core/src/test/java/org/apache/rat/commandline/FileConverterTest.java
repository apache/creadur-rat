/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   https://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */
package org.apache.rat.commandline;

import org.apache.rat.document.DocumentName;
import org.apache.rat.document.FSInfoTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FileConverterTest {

    @ParameterizedTest
    @MethodSource("applyTestData")
    void applyTest(DocumentName.FSInfo fsInfo, String fileName, Converters.FileConverter underTest, String expected) {
        assertThat(underTest.apply(fileName).getName()).isEqualTo(expected);
    }

    /**
     * Record returned from createDocumentData
     * @param documentName the document name for the working directory.
     * @param defaultRoot the default root for the file system
     * @param workingBaseName the working base name.
     * @param baseName the base name.
     */
    private record DocumentData(DocumentName documentName, String defaultRoot, String workingBaseName, String baseName) {
    }

    /**
     * Creates a DocumentData from a FSInfo.
     * @param fsInfo the FSInfo to work with.
     * @return the DocumentData for the working directory.
     */
    private static DocumentData createDocumentData(final DocumentName.FSInfo fsInfo) {
        String defaultRoot = fsInfo.roots()[0];
        String workingBaseName = defaultRoot + "working";
        String baseName = workingBaseName + fsInfo.dirSeparator() + "base";
        return new DocumentData(DocumentName.builder(fsInfo).setRoot(defaultRoot).setBaseName(workingBaseName).setName("base").build(),
                defaultRoot, workingBaseName, baseName);
    }

    static List<Arguments> applyTestData() {
        List<Arguments> lst = new ArrayList<>();

        for (DocumentName.FSInfo fsInfo : FSInfoTest.TEST_SUITE) {
            final DocumentData workingDirectory = createDocumentData(fsInfo);
            final Converters.FileConverter underTest = new Converters.FileConverter();
            underTest.setWorkingDirectory(workingDirectory.documentName);

            lst.add(Arguments.of(fsInfo, "/foo/bar.txt", underTest, workingDirectory.defaultRoot + String.join(fsInfo.dirSeparator(), "foo", "bar.txt")));
            lst.add(Arguments.of(fsInfo, "\\foo\\bar.txt", underTest, workingDirectory.defaultRoot + String.join(fsInfo.dirSeparator(), "foo", "bar.txt")));

            lst.add(Arguments.of(fsInfo, "foo/bar.txt", underTest, workingDirectory.baseName + String.join(fsInfo.dirSeparator(), "", "foo", "bar.txt")));
            lst.add(Arguments.of(fsInfo, "foo\\bar.txt", underTest, workingDirectory.baseName + String.join(fsInfo.dirSeparator(), "", "foo", "bar.txt")));

            if (fsInfo.equals(FSInfoTest.WINDOWS)) {
                String root = fsInfo.roots()[1];
                lst.add(Arguments.of(fsInfo, root + "foo/bar.txt", underTest, root + String.join(fsInfo.dirSeparator(), "foo", "bar.txt")));
                lst.add(Arguments.of(fsInfo, root + "foo\\bar.txt", underTest, root + String.join(fsInfo.dirSeparator(), "foo", "bar.txt")));
            }
        }
        return lst;
    }
}
