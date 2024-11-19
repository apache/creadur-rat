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
package org.apache.rat.document.impl.guesser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.guesser.NoteGuesser;
import org.apache.rat.testhelpers.TestingDocument;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NoteGuesserTest {

    @ParameterizedTest
    @MethodSource("nameData")
    public void testMatches(DocumentName testingName, boolean expected) {
        boolean actual = NoteGuesser.isNote(new TestingDocument(testingName));
        assertEquals( expected, NoteGuesser.isNote(new TestingDocument(testingName)), testingName::getName);
    }

    private static Stream<Arguments> nameData() {
        List<Arguments> lst = new ArrayList<>();

        final DocumentName linuxBaseName = DocumentName.builder().setName("/").setBaseName("/").setDirSeparator("/")
                .setCaseSensitive(true).build();
        final DocumentName windowsBaseName = DocumentName.builder().setName("\\").setBaseName("\\")
                .setDirSeparator("\\").setCaseSensitive(false).build();

        lst.add(Arguments.of(linuxBaseName.resolve("DEPENDENCIES"), true));
        lst.add(Arguments.of(linuxBaseName.resolve("LICENSE"), true));
        lst.add(Arguments.of(linuxBaseName.resolve("LICENSE.txt"), true));
        lst.add(Arguments.of(linuxBaseName.resolve("NOTICE"), true));
        lst.add(Arguments.of(linuxBaseName.resolve("NOTICE.txt"), true));
        lst.add(Arguments.of(linuxBaseName.resolve("README"), true));
        lst.add(Arguments.of(linuxBaseName.resolve("README.txt"), true));
        lst.add(Arguments.of(linuxBaseName.resolve("src/test/DEPENDENCIES"), true));
        lst.add(Arguments.of(linuxBaseName.resolve("src/test/LICENSE"), true));
        lst.add(Arguments.of(linuxBaseName.resolve("src/test/LICENSE.txt"), true));
        lst.add(Arguments.of(linuxBaseName.resolve("src/test/NOTICE"), true));
        lst.add(Arguments.of(linuxBaseName.resolve("src/test/NOTICE.txt"), true));
        lst.add(Arguments.of(linuxBaseName.resolve("src/test/README"), true));
        lst.add(Arguments.of(linuxBaseName.resolve("src/test/README.txt"), true));
        lst.add(Arguments.of(linuxBaseName.resolve("src/test/README.shouldFail"), false));

        lst.add(Arguments.of(windowsBaseName.resolve("DEPENDENCIES"), true));
        lst.add(Arguments.of(windowsBaseName.resolve("LICENSE"), true));
        lst.add(Arguments.of(windowsBaseName.resolve("LICENSE.txt"), true));
        lst.add(Arguments.of(windowsBaseName.resolve("NOTICE"), true));
        lst.add(Arguments.of(windowsBaseName.resolve("NOTICE.txt"), true));
        lst.add(Arguments.of(windowsBaseName.resolve("README"), true));
        lst.add(Arguments.of(windowsBaseName.resolve("README.txt"), true));
        lst.add(Arguments.of(windowsBaseName.resolve("src\\test\\DEPENDENCIES"), true));
        lst.add(Arguments.of(windowsBaseName.resolve("src\\test\\LICENSE"), true));
        lst.add(Arguments.of(windowsBaseName.resolve("src\\test\\LICENSE.txt"), true));
        lst.add(Arguments.of(windowsBaseName.resolve("src\\test\\NOTICE"), true));
        lst.add(Arguments.of(windowsBaseName.resolve("src\\test\\NOTICE.txt"), true));
        lst.add(Arguments.of(windowsBaseName.resolve("src\\test\\README"), true));
        lst.add(Arguments.of(windowsBaseName.resolve("src\\test\\README.txt"), true));
        lst.add(Arguments.of(windowsBaseName.resolve("src\\test\\README.shouldFail"), false));

        return lst.stream();
    }
}
