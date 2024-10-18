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

/** 
 * Types of components 
 */
public enum ComponentType {
    /** A License, the top level component. May not be used as a child of any component type. */
    LICENSE,
    /** A Matcher */
    MATCHER,
    /** A Parameter for example the "id" parameter found in every component */
    PARAMETER,
    /** A parameter that is supplied by the environment.
     * Currently systems using builders have to handle setting this.
     * For example the list of matchers for the "MatcherRefBuilder" */
    BUILD_PARAMETER
}