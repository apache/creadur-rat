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

import groovy.util.Node
import groovy.util.NodeList
import groovy.xml.XmlParser

rootProject.name = "apache-rat-gradle-plugin"

// Extract groupId + version from RAT root pom
val rootPom = XmlParser().parse(file("../pom.xml"))

fun xmlFirstChildText(node: Node, element: String): String =
  (node.get(element) as NodeList).text().trim()

val rootPomVersion = xmlFirstChildText(rootPom, "version")
val rootPomGroup = xmlFirstChildText(rootPom, "groupId")

val ideaActive = System.getProperty("idea.active").toBoolean()

pluginManagement {
  repositories {
    mavenCentral() // prefer Maven Central, in case Gradle's repo has issues
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
  repositories {
    // TODO move mavenLocal() under mavenCentral() once the 0.18 development branch is there
    mavenLocal()
    mavenCentral()
  }
}

gradle.beforeProject {
  version = rootPomVersion
  group = rootPomGroup
}
