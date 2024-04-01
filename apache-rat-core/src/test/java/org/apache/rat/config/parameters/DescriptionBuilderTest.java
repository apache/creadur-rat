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
package org.apache.rat.config.parameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.rat.analysis.matchers.AndMatcher;
import org.apache.rat.analysis.matchers.CopyrightMatcher;
import org.junit.jupiter.api.Test;

public class DescriptionBuilderTest {

    @Test
    public void matcherMapBuildTest() {

        Description underTest = DescriptionBuilder.buildMap(CopyrightMatcher.class);
        assertEquals(Component.Type.Matcher, underTest.getType());
        assertEquals("copyright", underTest.getCommonName());
        assertNotNull(underTest.getDescription());
        assertEquals(4, underTest.getChildren().size());
        assertTrue(underTest.getChildren().containsKey("id"));
        assertTrue(underTest.getChildren().containsKey("start"));
        assertTrue(underTest.getChildren().containsKey("stop"));
        assertTrue(underTest.getChildren().containsKey("owner"));

        underTest = DescriptionBuilder.buildMap(AndMatcher.class);
        assertEquals(Component.Type.Matcher, underTest.getType());
        assertEquals("all", underTest.getCommonName());
        assertNotNull(underTest.getDescription());
        assertEquals(3, underTest.getChildren().size());
        assertTrue(underTest.getChildren().containsKey("id"));
        assertTrue(underTest.getChildren().containsKey("resource"));
        assertTrue(underTest.getChildren().containsKey("enclosed"));
        Description desc = underTest.getChildren().get("enclosed");
        assertEquals(Component.Type.Unlabled, desc.getType());
    }

}
