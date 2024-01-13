package org.apache.rat.mp.util;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.logging.Log;
import org.apache.rat.config.SourceCodeManagementSystems;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Set;

import static org.apache.rat.mp.util.ExclusionHelper.ECLIPSE_DEFAULT_EXCLUDES;
import static org.apache.rat.mp.util.ExclusionHelper.IDEA_DEFAULT_EXCLUDES;
import static org.apache.rat.mp.util.ExclusionHelper.MAVEN_DEFAULT_EXCLUDES;
import static org.apache.rat.mp.util.ExclusionHelper.addEclipseDefaults;
import static org.apache.rat.mp.util.ExclusionHelper.addIdeaDefaults;
import static org.apache.rat.mp.util.ExclusionHelper.addMavenDefaults;
import static org.apache.rat.mp.util.ExclusionHelper.addPlexusAndScmDefaults;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ExclusionHelperTest {

    
    private Log log = Mockito.mock(Log.class);

    @Test
    public void testNumberOfExclusions() {
        assertEquals(5,
                ECLIPSE_DEFAULT_EXCLUDES.size(),"Did you change the number of eclipse excludes?");
        assertEquals(4,
                IDEA_DEFAULT_EXCLUDES.size(), "Did you change the number of idea excludes?");
        assertEquals(8,
                MAVEN_DEFAULT_EXCLUDES.size(), "Did you change the number of mvn excludes?");
    }

    @Test
    public void testAddingEclipseExclusions() {
        final Set<String> exclusion = new HashSet<>();
        exclusion.addAll(addEclipseDefaults(log, false));
        assertTrue(exclusion.isEmpty());
        exclusion.addAll(addEclipseDefaults(log, true));
        assertEquals(5, exclusion.size());
        exclusion.addAll(addEclipseDefaults(log, true));
        assertEquals(5, exclusion.size());
    }

    @Test
    public void testAddingIdeaExclusions() {
        final Set<String> exclusion = new HashSet<>();
        exclusion.addAll(addIdeaDefaults(log, false));
        assertTrue(exclusion.isEmpty());
        exclusion.addAll(addIdeaDefaults(log, true));
        assertEquals(4, exclusion.size());
        exclusion.addAll(addIdeaDefaults(log, true));
        assertEquals(4, exclusion.size());
    }

    @Test
    public void testAddingMavenExclusions() {
        final Set<String> exclusion = new HashSet<>();
        exclusion.addAll(addMavenDefaults(log, false));
        assertTrue(exclusion.isEmpty());
        exclusion.addAll(addMavenDefaults(log, true));
        assertEquals(8, exclusion.size());
        exclusion.addAll(addMavenDefaults(log, true));
        assertEquals(8, exclusion.size());
    }

    @Test
    public void testAddingPlexusAndScmExclusion() {
        final int expectedSizeMergedFromPlexusDefaultsAndScm = (37 + SourceCodeManagementSystems.getPluginExclusions().size());

        final Set<String> exclusion = new HashSet<>();
        exclusion.addAll(addPlexusAndScmDefaults(log, false));
        assertTrue(exclusion.isEmpty());
        exclusion.addAll(addPlexusAndScmDefaults(log, true));
        assertEquals(
                expectedSizeMergedFromPlexusDefaultsAndScm,//
                exclusion.size(), //
                "Did you upgrade plexus to get more default excludes?");
        exclusion.addAll(addPlexusAndScmDefaults(log, true));
        assertEquals(
                expectedSizeMergedFromPlexusDefaultsAndScm,//
                exclusion.size(), //
                "Did you upgrade plexus to get more default excludes?"
                );
    }

}
