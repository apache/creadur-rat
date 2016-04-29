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

import org.apache.rat.api.MetaData;

/**
 * License matches GPL2 or later.
 */
public class GPL2License extends FullTextMatchingLicense {
    public static final String FIRST_LICENSE_LINE = "This program is free software; you can redistribute it and/or\n" +
            " modify it under the terms of the GNU General Public License\n" +
            " as published by the Free Software Foundation; either version 2\n" +
            " of the License, or (at your option) any later version.";

    public GPL2License() {
        super(MetaData.RAT_LICENSE_FAMILY_CATEGORY_DATUM_GPL2,
                MetaData.RAT_LICENSE_FAMILY_NAME_DATUM_GPL_VERSION_2,
                "", FIRST_LICENSE_LINE);
    }
}
