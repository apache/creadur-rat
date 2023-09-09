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

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.api.Document;
import org.apache.rat.document.MockLocation;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CDDL1LicenseTest extends AbstractMatcherTest {
    private static String category = "CDDL1";
    private static String name = "COMMON DEVELOPMENT AND DISTRIBUTION LICENSE Version 1.0";
    private static String[][] targets = {
            { "fullTxt", "", "The contents of this file are subject to the terms of the Common Development\n"
                    + "and Distribution License(\"CDDL\") (the \"License\"). You may not use this file\n"
                    + "except in compliance with the License.\n\n"},
            { "longerTxt", "",
                " DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.\n\n"
                        + "Copyright 2011-2013 Tirasa. All rights reserved.\n\n"
                        + "The contents of this file are subject to the terms of the Common Development\n"
                        + "and Distribution License(\"CDDL\") (the \"License\"). You may not use this file\n"
                        + "except in compliance with the License.\n\n"
                        + "You can obtain a copy of the License at https://oss.oracle.com/licenses/CDDL\n"
                        + "See the License for the specific language governing permissions and limitations\n"
                        + "under the License." },
            { "spdx-tab", "", "SPDX-License-Identifier:\tCDDL-1.0" },
            { "spdx-space", "", "SPDX-License-Identifier: CDDL-1.0" },
            { "illumos", "", "The contents of this file are subject to the terms of the\n"
                    + "Common Development and Distribution License (the \"License\")\n"
                    + "You may not use this file except in compliance with the License.\n"}
    };
    
    public CDDL1LicenseTest() {
        super(category, name, targets);
    }

}
