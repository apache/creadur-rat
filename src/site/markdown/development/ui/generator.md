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

# The Generator

> * > Up: [UI Implementation](../ui_implementation.html)
> <br />[Options](./options.html) | Generator | [UI Specific](./ui_specific.html)


The generator is the bit of code that ties the new UI to the RAT CLI. In this section we will explore how the RAT core module exposes the information necessary to build a valuable new UI. We will be using the Maven tooling code in `apache-rat-tools` as the basis of the examples in this section. As always, the current code base is the source of truth.

There are two implementations of the Generator concept `AntGenerator` and `MavenGenerator`.

## The MavenGenerator

The `MavenGenerator` generates the abstract class that is used by the `apache-rat-plugin` module to process Maven elements that are tied to the CLI options.  The class starts with a list map of exceptional CLI Option name to Maven option name conversions.  In the code below the legacy "addLicense" option is changed to "add-license" so that the standard conversion to camel case will produce the proper name.  There is also a list of options that are not supported by Maven.  In code below the "--dir". "--log-level", and "--help" options are all excluded.  "--dir" is excluded because Maven produces a list of files to process, "--log-level" is excluded because the Maven log level is set via a different process, and "--help" is excluded because Maven has it own help facility.

```java
    /** A mapping of external name to internal name if not standard */
    private static final Map<String, String> RENAME_MAP = new HashMap<>();

    /** List of CLI Options that are not supported by Maven. */
    private static final List<Option> MAVEN_FILTER_LIST = new ArrayList<>();

    static {
        RENAME_MAP.put("addLicense", "add-license");

        MAVEN_FILTER_LIST.addAll(Arg.DIR.group().getOptions());
        MAVEN_FILTER_LIST.addAll(Arg.LOG_LEVEL.group().getOptions());
        MAVEN_FILTER_LIST.add(OptionCollection.HELP);
    }

    /**
     * Filter to remove Options not supported by Maven.
     */
    private static final Predicate<Option> MAVEN_FILTER = option -> !(MAVEN_FILTER_LIST.contains(option) || option.getLongOpt() == null);

```
The list of MavenOptions (see [Options](./options.html)) can then be generated by

```java
List<MavenOption> options = OptionCollection.buildOptions().getOptions()
        .stream().filter(MAVEN_FILTER)
        .map(MavenOption::new).collect(Collectors.toList());
```

The `MavenGenerator` uses a template called "Maven.tpl" that looks like:

```java
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

${package}

import org.apache.commons.cli.Option;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.rat.commandline.Arg;
import org.apache.rat.utils.CasedString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/* DO NOT EDIT - GENERATED FILE */

/**
 * Generated class to provide Maven support for standard RAT command line options
 */
${class}

    private final static Map<String,String> xlateName = new HashMap<>();

    private final static List<String> unsupportedArgs = new ArrayList<>();

    static {
${static}
    }

    public static String createName(String longOpt) {
        String name = xlateName.get(longOpt);
        return name != null ? name : new CasedString(CasedString.StringCase.KEBAB, longOpt).toCase(CasedString.StringCase.CAMEL);
    }

    public static List<String> unsupportedArgs() {
        return Collections.unmodifiableList(unsupportedArgs);
    }

${commonArgs}

${constructor}


    /*  GENERATED METHODS */


${methods}
}

```

This template contains replaceable sections for :

 * package: The name of the java package that the generated class belongs to.
 * class: The line that defines the class for the Maven generated code it reads: `public abstract class BaseRatMojo extends AbstractMojo {`. Note that the Maven example extends a Maven package class.
 * static: A section to initialize the static data like the translation name table and the unsupportedArgs list.
 * commonArgs: A section where the "Args.tpl" is inserted. This section contains the methods for argument manipulation that is common among all the UIs.
 * constructor: The constructor for the class. In the Maven case this is simply `protected BaseRatMojo() {}`.
 * methods: A section where the generator will insert the methods that map from the Maven framework into the RAT framework as specified in the "Args.tpl".

## The Args template

The args template defines a number of methods to simplify the interface between an arbitrary UI and the RAT CLI command line format. The template defines a variable called `args` that is a map of a CLI based argument name to a list of strings that are the argument values.

In general classes that include this template will call:
 `setArg`, `addArg`, and/or `removeArg` to set values in the args structure. Eventually the UI class will call `getArgs` to generate a command line string to pass to the CLI command line parsing code to create a `ReportConfiguration` that may be modified before calling `Reporter.report()`

```java
    ///////////////////////// Start common Arg manipulation code

    /**
     * A map of CLI based arguments to values.
     */
    protected final Map<String, List<String>> args = new HashMap<>();

    /**
     * Gets the list of arguments prepared for the CLI code to parse.
     * @return the List of arguments for the CLI command line.
     */
    protected List<String> args() {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : args.entrySet()) {
            result.add("--" + entry.getKey());
            result.addAll(entry.getValue().stream().filter(Objects::nonNull).collect(Collectors.toList()));
        }
        return result;
    }

    private String argsKey(Option opt) {
        return StringUtils.defaultIfEmpty(opt.getLongOpt(), opt.getKey());
    }

    private boolean validateSet(String key) {
        Arg arg = Arg.findArg(key);
        if (arg != null) {
            Option opt = arg.find(key);
            Option main = arg.option();
            if (opt.isDeprecated()) {
                args.remove(argsKey(main));
                // deprecated options must be explicitly set so let it go.
                return true;
            }
            // non-deprecated options may have default so ignore it if another option has already been set.
            for (Option o : arg.group().getOptions()) {
                if (!o.equals(main)) {
                    if (args.containsKey(argsKey(o))) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Set a key and value into the argument list.
     * Replaces any existing value.
     * @param key the key for the map.
     * @param value the value to set.
     */
    protected void setArg(String key, String value) {
        if (validateSet(key)) {
            List<String> values = new ArrayList<>();
            values.add(value);
            args.put(key, values);
        }
    }

    /**
     * Get the list of values for a key.
     * @param key the key for the map.
     * @return the list of values for the key or {@code null} if not set.
     */
    public List<String> getArg(String key) {
        return args.get(key);
    }

    /**
     * Add values to the key in the argument list.
     * If the key does not exist, adds it.
     * @param key the key for the map.
     * @param value the value to set.
     */
    protected void addArg(String key, String[] value) {
        if (validateSet(key)) {
            List<String> values = args.get(key);
            if (values == null) {
                values = new ArrayList<>();
                args.put(key, values);
            }
            values.addAll(Arrays.asList(value));
        }
    }

    /**
     * Add a value to the key in the argument list.
     * If the key does not exist, adds it.
     * @param key the key for the map.
     * @param value the value to set.
     */
    protected void addArg(String key, String value) {
        if (validateSet(key)) {
            List<String> values = args.get(key);
            if (values == null) {
                values = new ArrayList<>();
                args.put(key, values);
            }
            values.add(value);
        }
    }

    /**
     * Remove a key from the argument list.
     * @param key the key to remove from the map.
     */
    protected void removeArg(String key) {
        args.remove(key);
    }

 ///////////////////////// End common Arg manipulation code

```

## Generating Methods

For every `MavenOption` at least one method is created.  

### Generating the method signature

The `MavenGenerator` needs to generate method signatures based on the `org.apache.commons.cli.Option` state so the `MavenOption` provides the `getMethodSignature` method. This method checks the following conditions:

1. Is the option deprecated? If so add the `@Deprecated` annotation.
2. Does the option accept one or more arguments (i.e. is not a flag option)? If it does the argument should be a `String` otherwise it should be a `boolean`.
3. Does the method accept multiple arguments? If so modify the name to indicate plural as per the Maven standard.
4. Create the Maven `@Property` annotation with optional Maven command line property and defaults for the method.
5. Write all the information into a string and prefix the method name with "set" as per the Maven standard.

With the result being that a fully formed, Maven compliant, Java method signature is developed. Complete with documentation from the Option description. For example "--copyright", which is a deprecated option, produces the following Maven mojo method signature:

```java
/**
     * The copyright message to use in the license headers.
     * @param copyright copyright message to use in the license headers.
     * @deprecated Deprecated for removal since 0.17: Use &lt;editCopyright&gt; instead.
     */
    @Deprecated
    @Parameter(property = "rat.Copyright")
    public void setCopyright(String copyright) {
```

### Generating the method body

When executing the generated class should take the value passed in from the new UI convert it to a `String` (or `boolean` if a flag option) and call the `setArg` (or possibly the `removeArg`) method defined in `Args.tpl`. The key for the arg method is the CLI option name (without leading dashes). In the case of the `setCopyright` method above this looks like:

```java
public void setCopyright(String copyright) {
        setArg("copyright", copyright);
    }
```

Maven also supports the case where multiple values are accepted for one Property. To support this the `MavenGenerator` will generate methods that accept multiple string arguments and have a name that pluralizes the standard "set" based name. Details are available in the `MavenGenerator` code.

## AntGenerator

The AntGenerator follows the same design as the MavenGenerator. The main difference is that Ant allows attributes in the XML elements.

> * > Up: [UI Implementation](../ui_implementation.html)
    > <br />[Options](./options.html) | Generator | [UI Specific](./ui_specific.html)
