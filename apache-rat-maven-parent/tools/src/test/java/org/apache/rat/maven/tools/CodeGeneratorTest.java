/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.maven.tools;

import org.apache.rat.commandline.Arg;
import org.apache.rat.maven.MavenOption;
import org.apache.rat.maven.MavenOptionCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class CodeGeneratorTest {
    @TempDir
    private Path testPath;
    private CodeGenerator codeGenerator;

    CodeGeneratorTest() {}

    @BeforeEach
    void setup() {
        codeGenerator = new CodeGenerator(testPath.toString());
    }

    @Test
    public void testGenerateMethods() {
        final MavenOptionCollection mavenOptionCollection = codeGenerator.getMavenOptionCollection();
        Map<MavenOption, String> methods = codeGenerator.gatherMethods();
        Optional<MavenOption> mavenOption = mavenOptionCollection.getMappedOption(Arg.EXCLUDE.option());
        assertThat(mavenOption.isPresent());
        String methodText = methods.get(mavenOption.get());
        assertThat(methodText).contains("public void setInputExcludes(final String[] inputExcludes) {");

        mavenOption = mavenOptionCollection.getMappedOption(Arg.FAMILIES_APPROVED.option());
        assertThat(mavenOption.isPresent());
        methodText = methods.get(mavenOption.get());
        assertThat(methodText).contains("public void setLicenseFamiliesApproved(final String licenseFamiliesApproved) {")
                        .containsOnlyOnce("public void set");
        chckDeprecated(methods);
    }

    private void chckDeprecated(Map<MavenOption, String> methods) {
        for (Map.Entry<MavenOption, String> entry : methods.entrySet()) {
            if (entry.getKey().isDeprecated()) {
                assertThat(entry.getValue()).as(entry.getKey().toString()).doesNotContain("<", ">");
            }
        }
    }
}
