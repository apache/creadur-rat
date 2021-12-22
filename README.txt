
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

                            Apache Rat
                            ==========

Apache Rat is developed by the Apache Creadur project of the Apache Software
Foundation. Join us at https://creadur.apache.org and read more about Apache Rat
at https://creadur.apache.org/rat.

Release Audit Tool (Rat) is a tool to improve accuracy and efficiency when checking
releases. It is heuristic in nature: making guesses about possible problems. It
will produce false positives and cannot find every possible issue with a release.
It's reports require interpretation.

Rat was developed in response to a need felt in the Apache Incubator to be able to
review releases for the most common faults less labour intensively. It is therefore
highly tuned to the Apache style of releases.

Rat is intended to be self documenting: reports should include introductory material
describing their function. Building Rat describes how to run Rat. Running Rat
describes the options available. These release notes describe the current state of
Rat.

A good way to use Rat is to through the source. This allows the code base to be
easily patched for example to add new generated file matchers. The main jar is
runnable and self-documenting. This jar is available as a standard alone binary.

Rat includes a task library for Ant 1.10. This allows Rat reports to be run against
a wide variety of resources. See ant-task-examples.xml. To use the Ant tasks,
Apache Ant 1.10 is required. See https://ant.apache.org/.

For Maven builds, the plugin is recommended.

In response to demands from project quality tool developers, Rat is available as a
library (rat-lib jar) suitable for inclusion in tools. Note that binary compatibility
is not guaranteed between 0.x releases. The XML output format is not yet in it's
final form and so library users are recommended to either use the supplied
stylesheets or keep in close touch with the code.

