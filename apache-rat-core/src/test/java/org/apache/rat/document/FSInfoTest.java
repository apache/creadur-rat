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

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

public class FSInfoTest {
    public static final DocumentName.FSInfo DEFAULT;
    public static final DocumentName.FSInfo OSX;
    public static final DocumentName.FSInfo UNIX;
    public static final DocumentName.FSInfo WINDOWS;

    static {
        try (FileSystem osx = Jimfs.newFileSystem(Configuration.osX());
             FileSystem unix = Jimfs.newFileSystem(Configuration.unix());
             FileSystem windows = Jimfs.newFileSystem(Configuration.windows())) {
            OSX = new DocumentName.FSInfo(osx);
            UNIX = new DocumentName.FSInfo(unix);
            WINDOWS = new DocumentName.FSInfo(windows);
            DEFAULT = new DocumentName.FSInfo(FileSystems.getDefault());
        } catch (IOException e) {
            throw new RuntimeException("Unable to creat FSInfo objects: " + e.getMessage(), e);
        }
    }
}
