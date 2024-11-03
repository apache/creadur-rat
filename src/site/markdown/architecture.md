<!---
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
-->

# Overview

The RAT architecture is build around a single engine that parses input files to build an XML based report.  User interfaces (UIs) create and configure a `ReportConfiguration` which is passed to a `Reporter` instance which performs all the processing.

```
/------\    /------\    /------\
| CLI  |    |Maven |    | Ant  |
|  UI  |    |  UI  |    |  UI  |
\--+---/    \--+---/    \--+---/
   |           |           |
   \-----------+-----------/     ReportConfiguration
               |                   input to Reporter
          /----+----\
          | Reporter| <---  Multiple files and directories
          \----+----/
               |
               V
            Report

```

# Configuration

The `ReportConfiguration` contains all the options for the Reporter class. User interfaces create a `ReportConfiguration` and pass it to the `Reporter` instance for execution. The configuration specifies the configuration for:

* License Families -- An identifier for families of licenses that share the same constraints.  The default list is defined in the `o.a.r.default.xml` file in the families section.
* Licenses -- the definition of a license that defines the matchers that comprise the test for the license.  The default licenses are defined in the  `o.a.r.default.xml` file in the licenses section.
* Approved License Families -- A list of license families that are approved. Licenses of families not in this are unapproved.  The default approved license families are defined in the  `o.a.r.default.xml` file in the approved section.

## License Families

A license family comprises an ID and a name. New families may be added to the `o.a.r.default.xml` file or programattically from the UI.  License family IDs must be unique.  Later ID definitions override earlier definitions.

## License

A license definition comprises a family (identified by its ID), and a matcher. Licenses adopt the ID of their family which may be overridden with an id attribute.  Licenses also adopt the name of their family unless it is overridden by a name attribute.

## Approved Licenses

The approved licenses are enumerated in a list. Licenses are selected by family and the family ID is specified in the list.

## Matchers

Matchers are classes that implement specific tests.  They are created by builders.  The default builders are specified in the  `o.a.r.default.xml` file in the matchers section.

### Matcher.Builder

New matcher builders may be defined within the `o.a.r.default.xml` file and then used in the license definitions within the file.

Matcher builder class names must be of the form `<name>Builder` where `<name>` becomes the name of the matcher and is used in identifying the matcher to use (e.g. as a tag in the License section of the `o.a.r.default.xml` file).  They must also implement the `o.a.r.analysis.IHeaderMatcher.Builder` interface.

## flags to add licenses and copyrights to files

The configuration has a set of options to add licenses and optionally copyright notices to files that do not have them.
The `addLicenses` option adds the Apache2 license to files that do not have any license specified.
If the `copyright` text is provided it is also added to the files.
The `addLicensesForced` modifies the way the licenses are added, by default the licenses are added to new files with name of the old file but a `.new` extension added.
If `addLicensesForced` is specified then the files are overwritten.

## file name filter

The file name filter specifies a filter to be used to filter out any files that should not be evaluated.

## out

The `out` parameter specifies an `OutputStream` IOSupplier that is used to create the output stream to write text to.  By default, this is the `System.out` stream.

## stylesheet

the `styleSheet` parameter specifies the `InputStream` IOSupplier that is used to read the stylesheet that styles the XML output.

## IReportable

The `IReportable` parameter identifies the objects that the report should run against.
Implementations of `IReportable` generally do things like walk directory trees, or archives.

# Reporter

The reporter uses the `ReporterConfiguration` to configure the report.  It then begins to read the files.
Each file is a `Document` the reporter determines the Document type and if appropriate reads the header information (50 lines) from the document.

The header information is passed to the licenses.  The processing of the file stops when a license is triggered (found) or the end of the header is reached.

The information retrieved is used to create an XML document that may be styled by the defined stylesheet.

The reporter utilizes the `RatReport` interface to determine what actions to take with the documents.
Currently the `RatReport` interface is used to define license adding capabilities, report claim aggregation, and multiplexed reports.
Additional capabilities can be implemented by implementing a `RatReport` and inserting it into the processing chain.
