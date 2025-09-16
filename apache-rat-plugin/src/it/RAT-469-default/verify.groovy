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

assert content.contains('BUILD SUCCESS')
assert content.contains('[INFO] Including patterns: pom.xml') // explicit inclusion worked
assert content.contains('[INFO] Processing exclude file from STANDARD_SCMS.') // default exclusions worked
assert ! content.contains('[WARNING] No resources included')

// Report is in apache-rat-plugin/target/invoker-reports
report = new File(basedir, 'target/site/rat-report.html').text
assert TextUtils.isMatching("^  /verify.groovy\\s+S ", report)
assert TextUtils.isMatching("^! /pom.xml\\s+S ", report)

assert report.contains('Unapproved:         1')
assert report.contains('GPL  : 1')
assert report.contains('    GPL      GPL3          GNU General Public License V3.0')

assert report.contains('   AL       AL            Apache License Version 2.0')
assert report.contains('Approved:           2')
assert report.contains('Standards:          3')