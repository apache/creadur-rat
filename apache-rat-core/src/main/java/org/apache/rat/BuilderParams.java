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
package org.apache.rat;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.SortedSet;

import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.license.ILicenseFamily;

/**
 * Parameters that can be set by the BUILDER_PARAM ComponentType. The name of the method listed here
 * should be the same as the name specified in the ConfigComponent.
 */
public interface BuilderParams {
    /**
     * Gets one of the contained methods by name.
     *
     * @param name the name of the method to get.
     * @return the Method.
     */
    default Method get(String name) {
        try {
            return this.getClass().getMethod(name);
        } catch (NoSuchMethodException e) {
            throw new ImplementationException(String.format("method '%s' is not found in %s", name, this.getClass()));
        } catch (IllegalArgumentException e) {
            throw new ImplementationException(
                    String.format("method '%s' in %s can not be retrieved", name, this.getClass()), e);
        }
    }

    /**
     * Gets a mapping of matcher names to matchers.
     *
     * @return the mapping of matcher names to matchers.
     */
    Map<String, IHeaderMatcher> matcherMap();
    // the above method is called by reflection.

    /**
     * Gets a sorted set of registered license families.
     *
     * @return the sorted set of license families.
     */
    SortedSet<ILicenseFamily> licenseFamilies();
    // the above method is called  by reflection
}
