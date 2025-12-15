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
package org.apache.rat.testhelpers.data;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import org.apache.commons.cli.Option;
import org.apache.rat.report.xml.writer.XmlWriter;
import org.apache.rat.utils.CasedString;

public class DataUtils {
    /**
     * The text for the current ASF license.
     */
    public static final String[] ASF_TEXT_LINES = {
            "Licensed to the Apache Software Foundation (ASF) under one or more",
            "contributor license agreements.  See the NOTICE file distributed with",
            "this work for additional information regarding copyright ownership.",
            "The ASF licenses this file to You under the Apache License, Version 2.0",
            "(the \"License\"); you may not use this file except in compliance with",
            "the License.  You may obtain a copy of the License at",
            "  ",
            "    https://www.apache.org/licenses/LICENSE-2.0",
            "  ",
            "Unless required by applicable law or agreed to in writing, software",
            "distributed under the License is distributed on an \"AS IS\" BASIS,",
            "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.",
            "See the License for the specific language governing permissions and",
            "limitations under the License."
    };

    /**
     * The text for the current ASF license as a block of text with new lines.
     */
    public static final String ASF_TEXT = String.join("\n" ,ASF_TEXT_LINES);

    /**
     * A setup that does nothing.
     */
    static final Consumer<Path> NO_SETUP = basePath -> {};

    static final Consumer<ValidatorData> NO_VALIDATOR = validatorData -> {};

    private DataUtils() {
        // do not instantiate
    }

    /**
     * Create a directory name from the option.  Directory names are camel case with a lower case
     * first letter.
     * @param option the option to create a directory name from.
     * @return the directory name for the option.
     */
    static String asDirName(Option option) {
        return CasedString.StringCase.CAMEL.assemble(CasedString.StringCase.KEBAB.getSegments(option.getLongOpt()));
    }

    /**
     * Generate a simple configuration file that defines one license based on the name of the file and
     * containing as single text matcher that mateches the {@code text} parameter.
     * @param fileName The name of the file to create.
     * @param id the ID for the family.
     * @param text the for the matcher.
     */
    static void generateSimpleConfig(Path fileName, String id, String text) {
        try (XmlWriter writer = new XmlWriter(new OutputStreamWriter(Files.newOutputStream(fileName.toFile().toPath()),
                        StandardCharsets.UTF_8))) {
            writer.startDocument()
                    .comment(ASF_TEXT)
                    .openElement("rat-config")
                    .openElement("families")
                    .openElement("family")
                    .attribute("id", id)
                    .attribute("name", "from " + fileName.getFileName())
                    .closeElement() // family
                    .closeElement() // families
                    .openElement("licenses")
                    .openElement("license")
                    .attribute("family", id)
                    .openElement("text")
                    .content(text)
                    .closeDocument(); // close all open elements
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
