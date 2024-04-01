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
package org.apache.rat.analysis.license;

import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

/**
 * Apache Software License detection tests.
 *
 */
public class ApacheSoftwareLicenseTest extends AbstractLicenseTest {

    public static final String id = "AL";
    public static final String name = "Apache License Version 2.0";
    private static String[][] targets = {
            { "short", "Licensed under the Apache License, Version 2.0 (the \"License\")" },
            { "short2",
                    "Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0." },
            { "http", "http://www.apache.org/licenses/LICENSE-2.0" },
            { "https", "https://www.apache.org/licenses/LICENSE-2.0" },
            { "html", "http://www.apache.org/licenses/LICENSE-2.0.html" },
            { "htmls", "https://www.apache.org/licenses/LICENSE-2.0.html" },
            { "txt", "http://www.apache.org/licenses/LICENSE-2.0.txt" },
            { "txts", "https://www.apache.org/licenses/LICENSE-2.0.txt" },
            { "fullTxt",
                    "Licensed under the Apache License, Version 2.0 (the \"License\")\nyou may not use this file except "
                            + "in compliance with the License.\nYou may obtain a copy of the License at\n"
                            + "http://www.apache.org/licenses/LICENSE-2.0\nUnless required by applicable "
                            + "law or agreed to in writing, software\ndistributed under the License is "
                            + "distributed on an \"AS IS\" BASIS,\nWITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either "
                            + "express or implied.\nSee the License for the specific language governing permissions and\n"
                            + "limitations under the License.\n" },
            { "spdx-tab", "SPDX-License-Identifier:\tApache-2.0" },
            { "spdx-space", "SPDX-License-Identifier: Apache-2.0" },
            { "long text",
                    "/*\n" + " *  Licensed under the Apache License, Version 2.0 (the \"License\");\n"
                            + " *  you may not use this file except in compliance with the License.\n" + " *\n"
                            + " *  You may obtain a copy of the License at\n"
                            + " *       http://www.apache.org/licenses/LICENSE-2.0\n" + " *\n"
                            + " *  Unless required by applicable law or agreed to in writing, software\n"
                            + " *  distributed under the License is distributed on an \"AS IS\" BASIS,\n"
                            + " *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"
                            + " *  See the License for the specific language governing permissions and\n"
                            + " *  limitations under the License.\n" + " */\n" }

    };

    public static Stream<Arguments> parameterProvider() {
        return Stream.of(Arguments.of(id, id, name, null, targets));
    }
}
