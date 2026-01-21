<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at
 
   http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
-->

# WIP instructions

## Using the Gradle plugin (once RAT 1.0.0 is released)

1. Add the following to your `settings.gradle.kts` or `settings.gradle` file.
   ```kotlin
   pluginManagement {
     repositories {
       mavenCentral()
       gradlePluginPortal()
     }
   }
   ```
2. Add the following to your `build.gradle[.kts]` file. Adopt the version to the latest release
   ```kotlin
   plugins {
     id("org.apache.rat") version "1.0.0"
   }
   ``` 
   
   For Groovy (`build.gradle`) use:
   ```groovy
   plugins {
     id "org.apache.rat" version "1.0.0"
   }
   ``` 

## IDEs

### IntelliJ IDEA

There's no special setup needed to develop the RAT Gradle plugin, except having to install the Maven projects
to your local Maven repository using `./mvnw install -DskipTests -T1C` before.

IntelliJ asks you to "link" the Gradle project when you open the `build.gradle.kts` or `settings.gradle.kts` file.

## Integration in a Gradle build (locally built / for development)

1. `./mvnw install -DskipTests -T1C`
2. Alternative 1: Use the plugin via
   `settings.gradle.kts`:
   ```kotlin
   pluginManagement {
     repositories {
       mavenCentral()
       gradlePluginPortal()
       mavenLocal()
     }
   }
   ``` 
   `build.gradle.kts`:
   ```kotlin
   plugins {
     id("org.apache.rat") version "1.0.0-SNAPSHOT"
   }
   ``` 
3. Or include the plugin as an included Gradle build:
   `settings.gradle.kts`:
   ```kotlin
   includeBuild("<path-to-RAT-clone>/apache-rat-gradle-plugin") {
       name = "apache-rat-gradle-plugin"
   }
   pluginManagement {
     repositories {
       mavenCentral()
       gradlePluginPortal()
       mavenLocal()
     }
   }
   ``` 
   `build.gradle.kts`:
   ```kotlin
   plugins {
     id("org.apache.rat")
   }
   ``` 

## TODO

* Documentation
  * On website
* Release to Maven Central or Gradle plugin portal or both?
* Split task into rat check + 1 or more report tasks? (likely overengineering)
  * Default output is XML, text, HTML
* Unify code of `GradleOption` + `MavenOption`, if and where necessary?
* Apache release process
  * Are Gradle Plugin sources included in release source tarball?
  * Is the RAT release process automated??

## Done

* Add Gradle configurations to consume RAT reports in other Gradle tasks.
* Align code formatting with Creadur RAT.
* LICENSE/NOTICE file work needed?
  * No changes needed
* Gradle plugin specific unit and integration tests 
* Integrate into CI workflows
* Cleanup plugin's build file(s)
* Verify all artifacts ("special" Gradle plugin artifact) are built and deployed by Maven
* Integrate into Maven build
* Logging:
  * Implement a `Log` instance that delegates to Gradle's SLF4J compatible logging and install via
    `org.apache.rat.utils.DefaultLog.setInstance`.
* Use Gradle's worker approach to isolate parallel RAT runs.
* Allow usage of Rat native types (enums) in config options.
  * Alternatively or in addition to `String`s?
* Use `RegularFileProperty` for options that reference files.
* Added `GRADLE` standard exclude collection
* `GradleGenerator`, `Gradle.tpl`
  * Generation of `GradleOptions` in Gradle plugin build
* Gradle plugin extension + task
* Smoke-tested via Apache Polaris build
  * `./mvnw install -DskipTests -T1C` in creadur-rat
  * Checkout `rat-new-plugin` from `https://github.com/snazy/polaris/`
  * Adopt file path to `apache-rat-gradle-plugin`in `settings.gradle.kts` (only for this WIP work!)
  * Run `./gradlew :rat`

## Later

* Make Gradle task fully cachable by Gradle
  * Requires Gradle task to know all files that are considered
  * Caching support for properties + generated files is alrady implemented though
