package org.apache.rat.config;
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


import static org.apache.rat.config.SourceCodeManagementSystems.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

public class SourceCodeManagementSystemsTest {

    @Test
    public void testSubversionAndNumberOfSCMSystems() {
        assertFalse(SUBVERSION.hasIgnoreFile(), "SVN does not have any external ignore files.");
        
        int hasIgnore = 0;
        int hasNoIgnore = 0;
        for(SourceCodeManagementSystems scm : values()) {
            if(scm.hasIgnoreFile()) {
                hasIgnore++;
            } else {
                hasNoIgnore++;
            }
        }
        
        assertEquals(4, hasIgnore, "Did you change the number of SCMs?");
        assertEquals(1, hasNoIgnore, "Did you add a new SCM without ignoreFile?");
        assertEquals(values().length, hasIgnore+hasNoIgnore, "Amount of SCM has changed.");
    }
    
    @Test
    public void testPluginExcludeLists()  {
        assertEquals(1, SUBVERSION.getExclusions().size());
        assertEquals(2, GIT.getExclusions().size());
        
        assertEquals(9, getPluginExclusions().size(), "Did you change the number of SCM systems?");
        assertEquals(5, values().length, "Did you change the number of SCM systems?");
    }

}
