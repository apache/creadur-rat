/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rat.anttasks;

import java.io.File;
import org.apache.rat.document.DocumentName;
import org.apache.tools.ant.MagicNames;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HelpTest extends AbstractRatAntTaskTest {
    private final String baseNameStr = String.join(File.separator, new String[]{"src","test","resources","helpTest"});
    private final File antFile = new File(new File(baseNameStr), "build.xml").getAbsoluteFile();

    @BeforeEach
    public void setUp()  {
        File baseFile = antFile.getParentFile();
        for (int i = 0; i < 4; i++) {
            baseFile = baseFile.getParentFile();
        }
        DocumentName documentName = DocumentName.builder(antFile).setBaseName(baseFile).build();
        System.setProperty(MagicNames.PROJECT_BASEDIR, documentName.getBaseName());
        super.setUp();
    }
    @Override
    protected File getAntFile() {
        return antFile;
    }

    @Test
    public void testExecHelp() {
        buildRule.executeTarget("execHelp");
        System.out.println(buildRule.getOutput());
        assertThat(buildRule.getOutput()).contains("<rat:report addLicense='value'> ");
        assertThat(buildRule.getOutput()).contains("<inputSource>File</inputSource>");
        assertThat(buildRule.getOutput()).contains("Deprecated for removal since 0.17: Use outputFamilies attribute instead.");
    }
}
