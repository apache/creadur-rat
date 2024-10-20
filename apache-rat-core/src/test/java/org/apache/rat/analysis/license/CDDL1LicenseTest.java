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

public class CDDL1LicenseTest extends AbstractLicenseTest {
    public static final String id = "CDDL1";
    public static final String name = "COMMON DEVELOPMENT AND DISTRIBUTION LICENSE Version 1.0";
    private static final String[][] targets = {
            { "fullTxt",
                    "The contents of this file are subject to the terms of the Common Development "
                            + "and Distribution License(\"CDDL\") (the \"License\"). You may not use this file "
                            + "except in compliance with the License.  " },
            { "longerTxt",
                    " DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.  "
                            + "Copyright 2011-2013 Tirasa. All rights reserved.  "
                            + "The contents of this file are subject to the terms of the Common Development "
                            + "and Distribution License(\"CDDL\") (the \"License\"). You may not use this file "
                            + "except in compliance with the License.  "
                            + "You can obtain a copy of the License at https://oss.oracle.com/licenses/CDDL "
                            + "See the License for the specific language governing permissions and limitations "
                            + "under the License." },
            { "spdx-tab", "SPDX-License-Identifier:\tCDDL-1.0" },
            { "spdx-space", "SPDX-License-Identifier: CDDL-1.0" } };

    public static Stream<Arguments> parameterProvider() {
        return Stream.of(Arguments.of(id, id, name, null, targets));
    }

}
