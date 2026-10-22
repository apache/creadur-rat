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

content = new File(basedir, 'build.log').text

// verify debug output
assert content.contains('BUILD SUCCESS')

// RAT-552: excludeSubProjects derives an "input-exclude" pattern per module. Before the fix, that call was
// silently dropped whenever the deprecated "excludes" configuration had already been set, so this line would
// never appear at all (no warning either, since the drop happens before any logging).
assert content.contains('Adding [submodule/**] to input-exclude')

// the deprecated alias must still be honoured
assert content.contains('Adding [notes.bak] to exclude')

// Report is in apache-rat-plugin/target/invoker-reports
// Make sure that report is generated completely, and that the submodule directory was actually excluded
// from the parent's own scan, rather than merely logged as excluded.
report = new File(basedir, 'target/site/rat-report.html').text
assert ! report.contains('/submodule/pom.xml')
assert ! report.contains('/submodule/README.txt')
assert report.contains('Unapproved:         0')
