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

# The UI specific code

> Up: [UI Implementation](../ui_implementation.html)
> <br />[Options](./options.html)  | [Generator](./generator.html) | UI Specific

In most cases the UI provides values for some of the CLI options, or have additional options that are not supported by CLI.  In the case of Maven there is a "skip" option that causes the Maven plugin to not execute.  This is not implemented in the CLI as it is assumed if you don't want to execute the CLI you won't call it.

The `apache-rat-plugin` contains the code for the Maven UI extension.  In this particular case there are three components that extend the generated abstract class.
1. `AbstractRatMojo` - extends the generated class and add standard functionality.
2. `RatCheckMojo` - extends `AbstractRatMojo` and adds the functionality to execute the Rat core `Reporter` class.
3. `RatReportMojo` - extends `AbstractRatMojo` and adds the functionality to provide reports into the Maven reporting system.

## Added / Modified functionality

### Logging

Most UIs have some process for logging.  Rat defines a [Log](https://github.com/apache/creadur-rat/blob/master/apache-rat-core/src/main/java/org/apache/rat/utils/Log.java) interface and uses the [DefaultLog](https://github.com/apache/creadur-rat/blob/master/apache-rat-core/src/main/java/org/apache/rat/utils/DefaultLog.java) class to track the instance.

The `AbstractRatMojo` class has a `makeLog` method that wraps the Maven log to create an instance of the Rat Log interface.  In the `getConfiguration` method the DefaultLog default is set to return the wrapped Maven log.


### Processing Arguments

the `AbstractRatMojo.getConfiguration` performs some changes to the args defined in generated base class, generates the configuration and then calls:

```java
ReportConfiguration config = OptionCollection.parseCommands(args().toArray(new String[0]),
                    o -> getLog().warn("Help option not supported"),
                    true);
```

The above line creates the configuration from the args and if the user somehow manages to call help will log a warning that help is not supported.

After the `ReportConfiguration` is created it is modified based on specific Maven command line options before it is returned to the calling method.

## Executing the Rat scan****

The Maven code executes the Rat scan in the `RatCheckMojo.execute()` method.  This method processes the Maven "skip" option, checks is the output is overridden in the Args structure and if not sets the Maven default, retrieves the configuration and executes:

```java
try {
   this.reporter = new Reporter(config);
   reporter.output();
   check();
} catch (MojoFailureException e) {
   throw e;
} catch (Exception e) {
   throw new MojoExecutionException(e.getMessage(), e);
}
```

The `check()` method performs custom checks and logging for Maven specific issues. 

## Ant processing

The Ant processing is performed by the `Report` class in the `apache-rat-tasks` module.  It follows the same basic processing path as the Maven processing except that it handle Ant specific requirements and options.  The class has an `execute()` method that looks very similar to the Maven version:

```java
try {
   Reporter r = new Reporter(validate(getConfiguration()));
   r.output(StyleSheets.PLAIN.getStyleSheet(), () -> new ReportConfiguration.NoCloseOutputStream(System.out));
   r.output();
} catch (BuildException e) {
   throw e;
} catch (Exception ioex) {
   throw new BuildException(ioex);
}
```

> Up: [UI Implementation](../ui_implementation.html)
> <br />[Options](./options.html)  | [Generator](./generator.html) | UI Specific
