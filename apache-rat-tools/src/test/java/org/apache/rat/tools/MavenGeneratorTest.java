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

import org.apache.rat.documentation.options.MavenOption;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MavenGeneratorTest {

    @Test
    void testGenerateMavenProject() throws IOException {
        MavenGenerator.main(new String[]{"com.example", "MavenExample", "target"});
        File f = new File("target/com/example/MavenExample.java");
        assertThat(f).exists();
    }

    @Test
    void testGenerationWithoutParameters() {
        assertDoesNotThrow(() -> MavenGenerator.main(null));
        assertDoesNotThrow(() -> MavenGenerator.main(new String[]{}));
        assertDoesNotThrow(() -> MavenGenerator.main(new String[]{"one"}));
        assertDoesNotThrow(() -> MavenGenerator.main(new String[]{"one", "two"}));
    }

    @Test
    void testGetArgumentDescriptionWithoutDescription() {
        MavenOption option = mock(MavenOption.class);
        when(option.getDescription()).thenReturn(null);
        when(option.getName()).thenReturn("testGetArgumentDescriptionWithoutDescription");

        var exception = assertThrows(IllegalStateException.class, () -> MavenGenerator.getComment(option));
        assertThat(exception.getMessage()).isEqualTo("Description for testGetArgumentDescriptionWithoutDescription must not be null");
    }

    @Test
    void shouldReturnTheStateWhenOptionHasNoArgument() {
        MavenOption option = mock(MavenOption.class);
        when(option.hasArg()).thenReturn(false);

        assertThat(MavenGenerator.getArgumentDescription(option, "some description.")).isEqualTo("The state");
    }

    @Test
    void shouldReturnCapitalizedArgumentDescription() {
        MavenOption option = mock(MavenOption.class);
        when(option.hasArg()).thenReturn(true);

        assertThat(MavenGenerator.getArgumentDescription(option, "set value.")).isEqualTo("Value.");
    }

}
