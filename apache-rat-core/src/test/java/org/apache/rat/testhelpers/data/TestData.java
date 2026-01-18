/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */
package org.apache.rat.testhelpers.data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.apache.commons.cli.Option;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.WordUtils;
import org.apache.rat.ui.ArgumentTracker;
import org.apache.rat.utils.CasedString;

/**
 * The definition of a test.
 */
public final class TestData implements Comparable<TestData> {
    /** if set, the expected exception from the test. */
    private Exception expectedException;
    /** The sub name of the test */
    private final String name;
    /** The command line for the test */
    private final List<ImmutablePair<Option, String[]>> commandLine;
    /** A function to setup the test in a specific path */
    private final Consumer<Path> setupFiles;
    /** A function to test the results of the test. */
    private final Consumer<ValidatorData> validator;

    /**
     * Constructs the Test data
     * @param name the sub name of the test.  May not be {@code null} but may be an empty string.  Should
     * be specified in Camel case for multiple words.
     * @param commandLine The command line for the test.  May not be {@code null} but may consist of a single {@link ImmutablePair#nullPair()}.
     * @param setupFiles the method to set up the files for the test. May not be {@code null}.
     * @param validator the validator for the results of the test. May not be {@code null}.
     */
    public TestData(String name, List<ImmutablePair<Option, String[]>> commandLine,
             Consumer<Path> setupFiles,
             Consumer<ValidatorData> validator) {
        Objects.requireNonNull(name, " name cannot be null");
        Objects.requireNonNull(commandLine, "commandLine cannot be null");
        Objects.requireNonNull(setupFiles, "setupFiles cannot be null");
        Objects.requireNonNull(validator, "validator cannot be null");
        if (name.contains("/")) {
            throw new IllegalArgumentException("name may not contain '/', use camel case instead");
        }
        if (commandLine.isEmpty()) {
            throw new IllegalArgumentException("commandLine may not be empty but contain an ImmutablePair.nullPair()");
        }
        this.name = name;
        this.commandLine = commandLine;
        this.setupFiles = setupFiles;
        this.validator = validator;
        this.expectedException = null;
    }

    /**
     * Sets the exception expected from this test.
     * @param expectedException the expected exception.
     */
    void setException(Exception expectedException) {
        this.expectedException = expectedException;
    }

    /**
     * The option for the test.  This is the first option specified in the command line.
     * If the command line is empty this returns {@code null}.
     * @return the first option in the command line or {@code null} if there is no option.
     */
    public Option getOption() {
        return commandLine.get(0).getLeft();
    }

    /**
     * Get the sub name for this test.
     * @return the sub name for this test.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the expected excepton or {@code null} if not exception is expected.
     * @return the expected excepton or {@code null} if not exception is expected.
     */
    public Exception getExpectedException() {
        return expectedException;
    }

    /**
     * Determines if the test is expecting an exception.
     * @return {@code true} if the test is expecting to throw an exception.
     */
    public boolean expectingException() {
        return expectedException != null;
    }

    /**
     * Gets the command line as the string objects that are normally parsed by the
     * command line parser.
     * @return the command line strings.
     */
    public String[] getCommandLine() {
        return getCommandLine(null);
    }

    /**
     * Gets the command line as the string objects that are normally parsed by the
     * command line parser. The result will include "--" to terminate a trailing multi
     * argument option.
     * @param workingDir the directory to add to the command line.  May be {@code null}.
     * @return the command line strings.
     */
    public String[] getCommandLine(String workingDir) {
        List<String> args = new ArrayList<>(commandLine.size());
        final boolean[] lastWasMultiArg = {false};
        commandLine.forEach(pair -> {
            if (pair.getKey() != null) {
                if (pair.getKey().hasLongOpt()) {
                    args.add("--" + pair.getKey().getLongOpt());
                } else {
                    args.add("-" + pair.getKey().getOpt());
                }
                if (pair.getValue() != null) {
                    args.addAll(Arrays.asList(pair.getValue()));
                }
                lastWasMultiArg[0] = pair.getKey().hasArgs();
            } else {
                lastWasMultiArg[0] = false;
            }
        });
        if (lastWasMultiArg[0]) {
            args.add("--");
        }
        if (workingDir != null) {
            args.add(workingDir);
        }
        return args.toArray(new String[0]);
    }

    /**
     * Get the arguments for the command line.
     * @return the argumentsfor the command line as Option, String[] pairs.
     */
    public List<? extends Pair<Option, String[]>> getArgs() {
        return commandLine;
    }

    /**
     * Gets the validator for the test.
     * @return the validator for the test.
     */
    public Consumer<ValidatorData> getValidator() {
        return validator;
    }

    /**
     * Sets up the files for the test.
     * @param path the path to use as the base directory.  Subdirectories and files may be added
     * to this path.
     */
    public void setupFiles(Path path) {
        setupFiles.accept(path);
    }

    /**
     * Gets the test name.  This is the option concatenated with the name.
     * @return the unique test name
     */
    public String getTestName() {
         new ArrayList<>();
        String result = null;
        if (getOption() == null) {
            result = name + "_DefaultTest";
        } else {
            result = new CasedString(CasedString.StringCase.KEBAB, ArgumentTracker.extractKey(getOption())).toString();
            if (!name.isEmpty()) {
                result += "/" + name;
            }
        }
        return result;
    }

    /**
     * Gets the test name as a class name.  This is based on the option concatenated with the name.
     * @return the unique Java class name
     */
    public String getClassName() {
        if (getOption() == null) {
            return WordUtils.capitalize(name) + "_DefaultTest";
        } else {
            List<String> parts = new ArrayList<>(Arrays.asList(new CasedString(CasedString.StringCase.KEBAB, ArgumentTracker.extractKey(getOption())).getSegments()));
            if (!name.isEmpty()) {
                parts.addAll(Arrays.asList(new CasedString(CasedString.StringCase.CAMEL, name).getSegments()));
            }
            return CasedString.StringCase.PASCAL.assemble(parts.toArray(new String[0]));
        }
    }

    @Override
    public int compareTo(TestData other) {
        return getTestName().compareTo(other.getTestName());
    }
}
