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
# UI Development

## Overview

The Rat architecture supports multiple UIs.  By default, Rat provides a command line implementation (CLI) as well as implementations for the Ant and Maven build system. The source code for those implementation provide a good roadmap for implementing any new UI.

### CLI first

Rat is designed as a CLI first architecture.  This means that every new functionality is introduced as a CLI option first.  All tests cases are built and the implementation verified before it is released.  Additional native UIs are build on top of the CLI implementation. 

For example the CLI uses kabob format options (e.g. --this-is-an-option) Ant and Maven use an XML format configuration utilizing camel case names.  For Maven all options are elements in the Maven `pom.xml` file, for Ant it is a bit more complicated because Ant allows for attributes for elements while maven does not.

In the Maven example Rat is defined as a plugin.  We expect the new `--this-is-an-option` option to be reflected in that section as `<thisIsAnOption>`.  So expect to see something like 

```xml
<plugin>
  <groupId>org.apache.rat</groupId>
  <artifactId>apache-rat-plugin</artifactId>
  <configuration>
    <thisIsAnOption>option value</thisIsAnOption>
    <!-- more options here -->
  </configuration>
</plugin>
```

For Ant the new option may be a child element of the `<rat:report>` element, if the option only has a single argument it may be an attribute of the `<rat:report>` element.  Therefor, we expect to see either

```xml
<rat:report thisIsAnOption='option value'>
    <!-- more options here -->
</rat:report>
```
or 

```xml
<rat:report>
    <rat:thisIsAnOption>option value</rat:thisIsAnOption>
    <!-- more options here -->
</rat:report>
```

Note that if `--this-is-an-option` is a flag then the elements would be closed without text and the And attribute would be `thisIsAnOption='true'`.

### UI adapter architecture

The UIs are actually implemented as adapters.  They plug into an existing system and provide a mechanism to call the CLI code.  They utilize classes in the `apache-rat-tools` module that map from the CLI options into the UI options.

The basic build operation for a UI adapter is:
1. Execute a "Generator" to produce a base class for the UI based on the definitions found in the Rat core classes.
2. Compile the UI specific code that extends the generated base class.
3. Package the UI specific code in a manner expected by the UI being extended.

#### [The Options](ui/options.html)

Each UI has specific requirements to display and accept input for the CLI options.  In most cases it makes sense to create a UI Specific Option that takes the CLI option as a constructor argument and creates an Option that is used by the Generator and other associated code.

#### [The Generator](ui/generator.html)

The Generator class literally writes the source code for the base of the UI adapter.  The adapter accepts input from the new UI and converts the UI option into the kebab format for the CLI and places the values associated with that option into a Map indexed by the CLI option name.  In our example above both the Ant and the Maven UI would take the value for `thisIsAnOption` and place the values into the map as `this-is-an-option`.

The Options in the CLI have sufficient information for the Generator to provide user documentation for the methods in a form acceptable to the new UI ecosystem.

The class created by the Generator should have methods that represent all the options in the CLI.  In most cases there is a one-to-one correspondence between a method in the base class and the options in the CLI.

#### [UI specific](ui/ui_specific.html) code

The UI specific code uses extends the class created by the Generator and adds additional UI specific options and generally performs the necessary options to hook the new UI into the system being supported.  Examples of this type of code can be found in the `apache-rat-tasks` (Ant) and `apache-rat-plugin` (Maven) modules.
