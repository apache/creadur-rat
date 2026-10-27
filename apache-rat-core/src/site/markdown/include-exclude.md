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

# Include and Exclude Handling

> > Where do I find the exclude-include handling?

There are several places that could be called "exclude-include" handling.  So let's start at the lowest level.

## Command line execution
```


[ExclusionProcessor] --builds--> [DocumentNameMatcher]
     |                                    ^
     | contained in                       | used by 
     V                                    |   
[ReportConfiguration]  --configures--> [Report]
     ^
     | created by
     |
[OptionCollection.parseCommands()] --uses------------------> [ArgumentContext]
                                                               |  /  |
[UI Implementation] --provides--> [UIOptionCollection] <-uses--/ /   | uses
         |                                                      /    V
         | uses                                       contains /   [Arg]
         V                                                    /      |
[ArgumentTracker] --creates------------> [command line] <----/       | defines
                                                                     V
                                                                  options  
 
 ```


[`ExclusionProcessor`](https://github.com/apache/creadur-rat/blob/master/apache-rat-core/src/main/java/org/apache/rat/config/exclusion/ExclusionProcessor.java) builds the final `DocumentNameMatcher` that is used by the `Report` to filer out files.  The `ExclusionProcessor` also has the methods to add patterns and fileMatchers to include and exclude. 

The `ReportConfiguration` contains an `ExclusionProcessor` to do all the heavy include/exclude calculations and has methods that delegate to the `ExclusionProcessor` to add and remove includes and excludes.

The `ReportConfiguration` is created by the `OptionCollection.parseCommands()` method. The `parseCommands()` uses an `ArgumentContext` to set the options.

`ArgumentContext` contains the working directory, the command line as parsed by the Apache Commons CLI library, and the `ReportConfiguration`.  It uses the `Arg` class as well as a `UIOptionsCollection` to call the `Arg.processArgs()` to configure the `ReportConfiguration`. 

The UI implementation generally uses an `ArgumentTracker` to build the command line arguments to use in the `ArgumentContext` constructor.  The UI also provides a collection of UIOptions.  UIOptions are standard Apache Commons CLI Option objects that have been decroated with methods to display information in a format usable by the UI being supported.Generally these are the options defined by the `Arg` enumerations, but may include UI specific options and may filter out soem standard options that are not supported by the UI.

For example the Maven UI and the Ant UI do not support specifying files and directories to process on the command line. They are defined by other means within the UI, so tokens on the command line that are not associated with options are not supported.

In a second example Maven has an option to ignore child modules.  This is a concept specific to Maven. To support this the Maven UI accepts and `<ignoreChildModules>` configuration parameter that is processed by the Maven code to add directories containing the modules to the exclude list.

## UI Implementations

In general UI implementations should support all the `Arg` defined options that it can. Any additional features that the UI enforces or offers should be processed within the UI to set or alter configuration options before the `ReportConfiguration` is passed to the `Report` class for execution. 
