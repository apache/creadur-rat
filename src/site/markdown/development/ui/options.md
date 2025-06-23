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

# Options

> Up: [UI Implementation](../ui_implementation.html)
> <br />Options  | [Generator](./generator.html) | [UI Specific](./ui_specific.html)


In this section we will explore how the Rat core module exposes the information necessary to build a UI specific Option implementation.  We will be using the Maven tooling code in `apache-rat-tools` as the basis of the examples in this section.  As always, the current code base is the source of truth.

Rat core uses the Apache Commons CLI library to process command line options.  All the options are recorded in the `org.apache.rat.commandline.Arg` enumeration.  Internally the Arg enumeration contains logical option groups that represent a single option.  The use of option groups provides us a way to deprecate options and create replacements.  Only one option in an option group may be used in a single Rat invocation.

Each UI will have a representation of the individual Options specified within the Arg enumeration.  To simplify the code each UI implementation should have an implementation of the `org.apache.rat.documentation.options.AbstractOption` class.  This class will map the information contained in the `org.apache.commons.cli.Option` into methods that make sense with respect to the UI being implemented.

As an example we will look at the `org.apache.rat.documentation.options.MavenOption`.

## MavenOption

### Name conversion

First to deal with the mapping from the kebab style to the camel case  style, the `MavenOption` utilizes a static method in the `MavenGenerator` class to perform the conversion.

```java
    static String createName(final Option option) {
        String name = StringUtils.defaultIfEmpty(option.getLongOpt(), option.getOpt());
        name = StringUtils.defaultIfEmpty(RENAME_MAP.get(name), name).toLowerCase(Locale.ROOT);
        return new CasedString(StringCase.KEBAB, name).toCase(StringCase.CAMEL);
    }
```

This method ensures that long options are selected over short options and then allows those options to be mapped to a different name.  The remapping is an historical case where there was a camel case option in the CLI that had to be converted to kebab case first.  In addition, it may be that a future CLI option will generate a camel case name that conflicts with some other UI based method.  The mapping allows that to be easily overcome.  Finally, the `createName` method uses the `org.apache.rat.utils.CasedString` and `org.apache.rat.utils.CasedString.StringCase` classes to convert the kebab case into the camel case.  This method will always yield a camel case that starts with a lower case letter.

### Overridden methods

The AbstractOption has several methods that should be overridden.

#### cleanupName

The cleanup name method is called by the AbstractOption to convert the `org.apache.commons.cli.Option` name (kabob style) into the name expected in the configuration of the UI.  In the Maven case it returns the camel case name inside of angle brackets so that it appears as an XML element name.

#### getDefaultValue

The default value method will return the default value specified in the `org.apache.commons.cli.Option` unless it is overridden.

Maven allows the definition of default values for its options.  The class `org.apache.rat.documentation.options.MavenOption` defines the default values for the CLI options that have default values in Maven.  For example, the Maven environment expects the output of the tool to be written to a file and not displayed on standard out so this value is set as the default.

### Generating the method signature

The MavenGenerator needs to generate method signatures based on the `org.apache.commons.cli.Option` state so the MavenOption provides the `getMethodSignature` method.  This method checks the following conditions.

1. Is the option deprecated? If so add the `@Deprecated` annotation.
2. Does the option accept one or more arguments (i.e. is not a flag option)? If it does the argument should be a `String` otherwise it should be a `boolean`.
3. Does the method accept multiple arguments?  If so modify the name to indicate plural as per the Maven standard.
4. Create the Maven `@Property` annotation with optional Maven command line property and defaults for the method.
5. Write all the information into a string and prefix the method name with "set" as per the Maven standard.

## AntOption

The `AntOption` class is very similar to the `MavenOption` except that, as noted earlier, it creates XML Elements and Attributes depending on whether the option accepts more than one argument.

> Up: [UI Implementation](../ui_implementation.html)
> <br />Options | [Generator](./generator.html) | [UI Specific](./ui_specific.html)
