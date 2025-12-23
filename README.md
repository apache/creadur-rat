# Apache Creadur RAT - Build status

ASF Jenkins: [![ASF Jenkins Build Status](https://ci-builds.apache.org/buildStatus/icon?job=Creadur%2FCreadur-Rat)](https://ci-builds.apache.org/job/Creadur/job/Creadur-Rat/)

GA: [![Github Action master branch status](https://github.com/apache/creadur-rat/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/apache/creadur-rat/actions)

[![Revved up by Develocity](https://img.shields.io/badge/Revved%20up%20by-Develocity-06A0CE?logo=Gradle&labelColor=02303A)](https://develocity.apache.org/scans?list.sortColumn=buildDuration&list.sortOrder=asc&search.buildToolType=maven&search.names=not:CI%20stage&search.rootProjectNames=Apache%20Creadur%20Rat&search.tasks=install&search.timeZoneId=Europe%2FBerlin&search.values=Build%20Parent-pom)

## What is RAT?

Apache RAT is developed by the Apache Creadur project of the Apache Software
Foundation. Join us at https://creadur.apache.org and read more about Apache RAT
at https://creadur.apache.org/rat.

*Release Audit Tool (RAT)* is a tool to improve accuracy and efficiency when checking
releases. It is heuristic in nature: making guesses about possible problems. It
will produce false positives and cannot find every possible issue with a release.
Its reports require interpretation.

RAT was developed in response to a need felt in the Apache Incubator to be able to
review releases for the most common faults less labour intensively. It is therefore
highly tuned to the Apache style of releases.

### RAT as binary

A good way to use RAT is to through the source. This allows the code base to be
easily patched for example to add new generated file matchers. The main jar is
runnable and self-documenting. This jar is available as a standard alone binary.

### Apache Ant integration

RAT includes a task library for Ant 1.7. This allows RAT reports to be run against
a wide variety of resources. See ant-task-examples.xml. To use the Ant tasks,
Apache Ant 1.7 is required. See https://ant.apache.org/.

### Apache Maven plugin

For Maven builds, the plugin is recommended.

### RAT as a library

In response to demands from project quality tool developers, RAT is available as a
library (rat-lib jar) suitable for inclusion in tools.

Note that binary compatibility is not guaranteed between 0.x releases. The XML output format is not yet in its
final form and so library users are recommended to either use the supplied stylesheets or keep in close touch with the code.

## License

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

## Contribution

If you want to contribute, feel free to branch from master and provide a pull request via GitHub.
You should file a contributor license agreement in order to properly handle your input.
Apart from that you can file an issue in ASF's Jira: [project RAT](https://issues.apache.org/jira/browse/RAT)

## How to build RAT

RAT is developed using the Maven build tool.  The simplest method for building RAT is to clone the 
repository and execute `mvn install`.  The `./mvnw`, or `./mvnw.cmd` scripts can be run in place of the `mvn` command.

The RAT build generates some source and copies test data, therefore it is necessary to run one of three commands to initially configure the system:
 * *package*: Will compile and create the packages for each module. For example `mvn package`.
 * *verify*: Will create the package and verify that the packaging is correct. For example `./mvnw verify`
 * *install*: Will create the packages and install them in your local repository. For example `mvn install`.

To build without running the tests add `-Dmaven.test.skip=true` to the `mvn` or `mvnw` command line.

RAT uses the previous release version during the build to verify it complies its own rules.

### Maven build profiles

#### apache-release

The "apache-release" profile creates the source release archive as part of the build process and is only relevant during release builds.

#### release-notes

The "release-notes" profile generates the `RELEASE-NOTES.txt` in the top-level directory from the `src/changes/changes.xml` file using the [maven-changes-plugin](https://maven.apache.org/plugins/maven-changes-plugin/) plugin.

Usage: `mvn changes:announcement-generate -Prelease-notes [-Dchanges.version=nnn]`

Defining changes.version creates the release notice without first removing the SNAPSHOT suffix.

Requires: file `src/changes/release-notes.vm` to format the changes.xml.

### Using Eclipse

To import RAT into Eclipse, 
 1. Clone the repository.
 2. Import the directory into Eclipse as an existing Maven project.
 3. Right-click the `creadur-rat` project and execute "Run As -> Maven install"
 
The above steps will generate the necessary source code as well as testing resources.

### Using IntelliJ

To import RAT into IntelliJ 
 1. Clone the repository.
 2. Import the directory into IntelliJ as a project using "File -> new -> Project from Existing Sources..."
 3. Select "maven" type for import.
 4. Select the maven icon from the right menu.
 5. Right click on  "Apache Creadur Rat -> Lifecycle -> verify"
 6. Select "Run Maven Build"
 
The above steps will generate the necessary source code as well as testing resources.
