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
package org.apache.rat.config.exclusion;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


import java.util.ArrayList;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StandardCollectionTest {

    @ParameterizedTest
    @MethodSource("collectionData")
    public void testState(StandardCollection scm, boolean hasFileProcessor, boolean hasPathMatchSupplier, boolean hasPatterns) {
        assertEquals(hasFileProcessor, scm.hasFileProcessor(), () -> scm.name()+" FileProcessor state wrong.");
        assertEquals(hasPathMatchSupplier, scm.hasPathMatchSupplier(), () -> scm.name()+" PathMatcherSupplier state wrong.");
        assertEquals(hasPatterns, !scm.patterns().isEmpty(), () -> scm.name()+" patterns state wrong.");
    }

    @Test
    public void verifyAllStandardCollectionObjectsAreTested() {
        Collection<Arguments> tests = collectionData();
        assertEquals(StandardCollection.values().length, tests.size(), "Some StandardCollections are not listed in 'collectionData'");
    }

    public static Collection<Arguments> collectionData() {
        List<Arguments> lst = new ArrayList<Arguments>();
        lst.add(Arguments.of(StandardCollection.ALL, true, true, true));
        lst.add(Arguments.of(StandardCollection.ARCH, false, false, true));
        lst.add(Arguments.of(StandardCollection.BAZAAR, true, false, true));
        lst.add(Arguments.of(StandardCollection.BITKEEPER, false, false, true));
        lst.add(Arguments.of(StandardCollection.CVS, true, false, true));
        lst.add(Arguments.of(StandardCollection.DARCS, false, false, true));
        lst.add(Arguments.of(StandardCollection.ECLIPSE, false, false, true));
        lst.add(Arguments.of(StandardCollection.GIT, true, false, true));
        lst.add(Arguments.of(StandardCollection.HIDDEN_DIR, false, true, false));
        lst.add(Arguments.of(StandardCollection.HIDDEN_FILE, false, true, false));
        lst.add(Arguments.of(StandardCollection.IDEA, false, false, true));
        lst.add(Arguments.of(StandardCollection.MAC, false, false, true));
        lst.add(Arguments.of(StandardCollection.MAVEN, false, false, true));
        lst.add(Arguments.of(StandardCollection.MERCURIAL, true, false, true));
        lst.add(Arguments.of(StandardCollection.MISC, false, false, true));
        lst.add(Arguments.of(StandardCollection.MKS, false, false, true));
        lst.add(Arguments.of(StandardCollection.RCS, false, false, true));
        lst.add(Arguments.of(StandardCollection.SCCS, false, false, true));
        lst.add(Arguments.of(StandardCollection.SERENA_DIMENSIONS_10, false, false, true));
        lst.add(Arguments.of(StandardCollection.STANDARD_PATTERNS, true, false, true));
        lst.add(Arguments.of(StandardCollection.STANDARD_SCMS, true, false, true));
        lst.add(Arguments.of(StandardCollection.SUBVERSION, false, false, true));
        lst.add(Arguments.of(StandardCollection.SURROUND_SCM, false, false, true));
        lst.add(Arguments.of(StandardCollection.VSS, false, false, true));
        return lst;
    }
}
