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
import static org.junit.Assert.*;

import static org.apache.rat.config.SourceCodeManagementSystems.*;

import org.junit.Test;

public class SourceCodeManagementSystemsTest {

    @Test
    public void testSubversionAndNumberOfSCMSystems() {
        assertFalse("SVN does not have any external ignore files.", SUBVERSION.hasIgnoreFile());
        
        int hasIgnore = 0;
        int hasNoIgnore = 0;
        for(SourceCodeManagementSystems scm : values()) {
            if(scm.hasIgnoreFile()) {
                hasIgnore++;
            } else {
                hasNoIgnore++;
            }
        }
        
        assertEquals("Did you change the number of SCMs?", 4, hasIgnore);
        assertEquals("Did you add a new SCM without ignoreFile?", 1, hasNoIgnore);
        assertEquals("Amount of SCM has changed.", values().length, hasIgnore+hasNoIgnore);
    }
    
    @Test
    public void testPluginExcludeLists()  {
        assertEquals(1, SUBVERSION.getExclusions().size());
        assertEquals(2, GIT.getExclusions().size());
        
        assertEquals("Did you change the number of SCM systems?", 9, getPluginExclusions().size());
        assertEquals("Did you change the number of SCM systems?", 5, values().length);
    }

}
