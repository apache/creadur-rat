#!/usr/bin/env bash
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

# Script to verify that the Gradle plugin works from a local Maven install

set -e

cd "${0%/*}"

temp_dir="$(mktemp --tmpdir --directory)"
function purge_temp_dir {
  echo "Purging ${temp_dir}..."
  rm -rf "${temp_dir}"
}
trap purge_temp_dir EXIT

rat_version="$(cat ../pom.xml | grep '^  <version>' | head -1 | sed -e 's/.*<version>\([^<]*\)<\/version>.*/\1/')"

cat <<! > "${temp_dir}/settings.gradle"
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
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}
!

cat <<! > "${temp_dir}/build.gradle"
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

import org.apache.rat.config.exclusion.StandardCollection

plugins {
    id 'org.apache.rat' version '${rat_version}'
}

rat {
  // excludes standard file patterns (like .git)
  inputExcludeStds.addAll(
      StandardCollection.MAC)

  inputExcludes.addAll(
      "gradle/wrapper/gradle-wrapper.jar",
      "gradle/wrapper/gradle-wrapper-*.sha256"
  )
}
!

cp -r gradle "${temp_dir}"
cp gradlew "${temp_dir}"

(cd "$temp_dir" ; ./gradlew :rat)
