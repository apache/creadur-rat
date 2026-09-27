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
package org.apache.rat.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileUtilsTest {
    @Mock
    private File mockedFile;

    @Test
    void deleteSwallowsExceptions() {
        when(mockedFile.exists()).thenReturn(true);
        when(mockedFile.isDirectory()).thenReturn(false);

        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.delete(any(Path.class)))
                    .thenThrow(new IOException("Mocked exception"));

            assertDoesNotThrow(()->FileUtils.delete(mockedFile));
        }
    }

    @Test
    void writeFileHandlesNullFile() {
        assertThrows(IllegalArgumentException.class, () ->
                FileUtils.writeFile(null, "just a Test", Arrays.asList("a", "b", "c")));
    }
}