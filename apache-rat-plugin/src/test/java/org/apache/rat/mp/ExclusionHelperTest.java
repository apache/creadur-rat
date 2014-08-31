package org.apache.rat.mp;

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

import static org.apache.rat.mp.ExclusionHelper.ECLIPSE_DEFAULT_EXCLUDES;
import static org.apache.rat.mp.ExclusionHelper.IDEA_DEFAULT_EXCLUDES;
import static org.apache.rat.mp.ExclusionHelper.MAVEN_DEFAULT_EXCLUDES;
import static org.apache.rat.mp.ExclusionHelper.addEclipseDefaults;
import static org.apache.rat.mp.ExclusionHelper.addIdeaDefaults;
import static org.apache.rat.mp.ExclusionHelper.addMavenDefaults;
import static org.apache.rat.mp.ExclusionHelper.addPlexusAndScmDefaults;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.logging.Log;
import org.apache.rat.config.SourceCodeManagementSystems;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExclusionHelperTest {

	@Mock
	Log log;

	@Test
	public void testNumberOfExclusions() {
		assertEquals("Did you change the number of eclipse excludes?", 3,
				ECLIPSE_DEFAULT_EXCLUDES.size());
		assertEquals("Did you change the number of idea excludes?", 4,
				IDEA_DEFAULT_EXCLUDES.size());
		assertEquals("Did you change the number of mvn excludes?", 4,
				MAVEN_DEFAULT_EXCLUDES.size());
	}

	@Test
	public void testAddingEclipseExclusions() {
		final Set<String> exclusion = new HashSet<String>();
		addEclipseDefaults(log, false, exclusion);
		assertTrue(exclusion.isEmpty());
		addEclipseDefaults(log, true, exclusion);
		assertEquals(3, exclusion.size());
		addEclipseDefaults(log, true, exclusion);
		assertEquals(3, exclusion.size());
	}

	@Test
	public void testAddingIdeaExclusions() {
		final Set<String> exclusion = new HashSet<String>();
		addIdeaDefaults(log, false, exclusion);
		assertTrue(exclusion.isEmpty());
		addIdeaDefaults(log, true, exclusion);
		assertEquals(4, exclusion.size());
		addIdeaDefaults(log, true, exclusion);
		assertEquals(4, exclusion.size());
	}

	@Test
	public void testAddingMavenExclusions() {
		final Set<String> exclusion = new HashSet<String>();
		addMavenDefaults(log, false, exclusion);
		assertTrue(exclusion.isEmpty());
		addMavenDefaults(log, true, exclusion);
		assertEquals(4, exclusion.size());
		addMavenDefaults(log, true, exclusion);
		assertEquals(4, exclusion.size());
	}

	@Test
	public void testAddingPlexusAndScmExclusion() {
		final int expectedSizeMergedFromPlexusDefaultsAndScm = (38 + SourceCodeManagementSystems.getPluginExclusions().size());

		final Set<String> exclusion = new HashSet<String>();
		addPlexusAndScmDefaults(log, false, exclusion);
		assertTrue(exclusion.isEmpty());
		addPlexusAndScmDefaults(log, true, exclusion);
		assertEquals(
				"Did you upgrade plexus to get more default excludes?",//
				expectedSizeMergedFromPlexusDefaultsAndScm,//
				exclusion.size());
		addPlexusAndScmDefaults(log, true, exclusion);
		assertEquals(
				"Did you upgrade plexus to get more default excludes?",//
				expectedSizeMergedFromPlexusDefaultsAndScm,//
				exclusion.size());
	}

}
