/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.apache.rat.testhelpers.TextUtils

content = new File(basedir, 'build.log').text

// verify debug output
assert content.contains('BUILD SUCCESS')
assert ! content.contains('[WARNING] No resources included')
assert content.contains('Adding [MAVEN] to input-exclude-std')
assert content.contains('Excluding MAVEN collection.')

// Report is in apache-rat-plugin/target/invoker-reports
// Make sure that report is generated completely
report = new File(basedir, 'target/site/rat-report.html').text
assert TextUtils.isMatching("^  /verify.groovy\\s+S ", report)
assert TextUtils.isMatching("^  /pom.xml\\s+S ", report)
assert TextUtils.isMatching("^  /target\\s+I\\s+application/octet-stream\\s+\\(directory\\)", report)

assert report.contains('Unapproved:         0')
assert report.contains('Apache License 2.0: 3')
assert report.contains('IGNORED: 3') // MAVEN excludes
assert report.contains('STANDARD: 3')
