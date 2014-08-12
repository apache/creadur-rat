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

import static org.junit.Assert.*;

import org.junit.Test;

public class AbstractRatMojoTest {

    @Test
    public void testAmountOfExclusions() {
        assertEquals("Did you change the amount of eclipse excludes?", 3, AbstractRatMojo.ECLIPSE_DEFAULT_EXCLUDES.size());
        assertEquals("Did you change the amount of idea excludes?", 4, AbstractRatMojo.IDEA_DEFAULT_EXCLUDES.size());
        assertEquals("Did you change the amount of mvn excludes?", 4, AbstractRatMojo.MAVEN_DEFAULT_EXCLUDES.size());
    }

}
