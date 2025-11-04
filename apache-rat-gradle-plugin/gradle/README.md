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

# No `gradle-wrapper.jar` in source tree (Gradle Wrapper in Apache projects)

Apache policies prohibit committing `gradle-wrapper.jar` into the source tree.

The `gradlew` and `gradlew.bat` scripts have been updated to call the `gradlew-include.sh`/`.ps1` script
in this directory to download and verify the Gradle Wrapper for the Gradle version defined in
`gradle/wrapper/gradle-wrapper.properties`.

The `gradlew-include.*` scripts...
1. Determine (extract) the Gradle version from the `gradle/wrapper/gradle-wrapper.properties` file.
2. If the a checksum file does not exist, delete any existing `gradle-wrapper.jar`.
3. If the `gradle-wrapper.jar` exists, verify its checksum using a the (possibly) exiting checksum file.
4. If the checksum of the jar and the checksum in the file do not match, delete both the checksum and `gradle-wrapper.jar` files.
5. If the checksum file does not exist, download it from `https://services.gradle.org/distributions/gradle-<VERSION>-wrapper.jar.sha256`
6. If the `gradle-wrapper.jar` does not exist, download it from `https://raw.githubusercontent.com/gradle/gradle/v<VERSION>/gradle/wrapper/gradle-wrapper.jar`
   and verify the checksum of the downloaded jar.
