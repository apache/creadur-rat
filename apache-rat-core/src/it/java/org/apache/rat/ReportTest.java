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
package org.apache.rat;

import groovy.lang.GroovyShell;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.rat.api.Document;
import org.apache.rat.api.RatException;
import org.apache.rat.commandline.Arg;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.document.FileDocument;
import org.apache.rat.document.RatDocumentAnalysisException;
import org.apache.rat.report.RatReport;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log;
import org.apache.rat.walker.DirectoryWalker;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration tests for the {@link Report} class.
 * Integration tests must have a specific structure.
 * <ul>
 *     <li>They are in directories under {@code src/it/resources/ReportText/X} where {@code X} is the name of the test. Usually
 *     a reference to a reported bug.</li>
 *     <li>Within the directory is a file named {@code commandLine.txt}. Each line in the file is a single command line
 *     token. For example "--output-style XML" would be 2 lines in the file "--output-style" and "XML"</li>
 *     <li>Within the directory there is a subdirectory named {@code src}. This is the root of the file system for RAT to scan.</li>
 *     <li>There may be a {@code notes.md} describing the test</li>
 *     <li>There is a {@code verify.groovy} script. When executed:
 *     <ul>
 *         <li>The first parameter will be then name of the file that captured the output.</li>
 *         <li>The second parameter will be the name of the file that captured the log.</li>
 *         <li>Any assert that fails within the Groovy script will fail the test.</li>
 *         <li>Any value returned from the script execution will fail the test and the returned value will be
 *         used as the failure message.</li>
 *      </ul>
 *      </li>
 *      <li>If an exception is expected when Report is run with the command line a file named {@code expected-message.txt}
 *      must be present in the directory. It must contain text that is expected to be found within the message
 *      associated with the exception.</li>
 * </ul>
 */
public class ReportTest {

    private String[] asArgs(final List<String> argsList) {
        return argsList.toArray(new String[argsList.size()]);
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("args")
    public void integrationTest(String testName, Document commandLineDoc) throws Exception {
        DefaultLog.getInstance().log(Log.Level.INFO, "Running test for " + testName);
        File baseDir = new File(commandLineDoc.getName().getName()).getParentFile();

        // get the arguments
        List<String> argsList = IOUtils.readLines(commandLineDoc.reader());

        CommandLine commandLine = DefaultParser.builder().setDeprecatedHandler(DeprecationReporter.getLogReporter())
                .setAllowPartialMatching(true).build().parse(Arg.getOptions(), asArgs(argsList));

        File outputFile = new File(baseDir,"output.txt");
        if (!commandLine.hasOption(Arg.OUTPUT_FILE.option())) {
            argsList.add(0, "--" + Arg.OUTPUT_FILE.option().getLongOpt());
            argsList.add(1, outputFile.getAbsolutePath());
        } else {
            outputFile = new File(baseDir, commandLine.getOptionValue(Arg.OUTPUT_FILE.option()));
        }

        File logFile = new File(baseDir,"log.txt");
        FileLog fileLog = new FileLog(logFile);
        Log oldLog = null;
        try {
            oldLog = DefaultLog.setInstance(fileLog);

            File src = new File(baseDir, "src");
            if (src.isDirectory()) {
                argsList.add(src.getAbsolutePath());
            }

            File expectedMsg = new File(baseDir, "expected-message.txt");
            if (expectedMsg.exists()) {
                String msg = IOUtils.readLines(new FileReader(expectedMsg)).get(0).trim();
                assertThrows(RatDocumentAnalysisException.class, () -> Report.main(asArgs(argsList)),
                        msg);
            } else {
                Report.main(asArgs(argsList));
            }
        } finally {
            DefaultLog.setInstance(oldLog);
            fileLog.close();
        }

        File groovyScript = new File(baseDir, "verify.groovy");
        if (groovyScript.exists()) {
            // call groovy expressions from Java code
            CompilerConfiguration compilerConfiguration = new CompilerConfiguration();

            GroovyShell shell = new GroovyShell(compilerConfiguration);
            for (String classPath : System.getProperty("java.class.path").split(File.pathSeparator)) {
                shell.getClassLoader().addClasspath(classPath);
            }
            Object value = shell.run(groovyScript, new String[]{outputFile.getAbsolutePath(), logFile.getAbsolutePath()});
            if (value != null) {
                fail(String.format("%s",value));
            }
        }
    }

    public static Stream<Arguments> args() throws RatException {
        List<Arguments> results = new ArrayList<>();
        URL url = ReportTest.class.getResource("/ReportTest");
        String urlAsFile = url.getFile();
        if(StringUtils.isEmpty(urlAsFile)) {
            throw new RatException("Could not find root directory for " + url);
        }

        File baseDir = new File(url.getFile());
        DocumentName docName = DocumentName.builder(baseDir).build();
        AbstractFileFilter fileFilter = new NameFileFilter("commandLine.txt", docName.isCaseSensitive() ? IOCase.SENSITIVE : IOCase.INSENSITIVE);
        fileFilter = new OrFileFilter(fileFilter, DirectoryFileFilter.INSTANCE);
        Document document = new FileDocument(docName, baseDir, new DocumentNameMatcher(fileFilter));
        DirectoryWalker walker = new DirectoryWalker(document);
        RatReport report = new RatReport() {
            @Override
            public void report(Document document)  {
                if (!document.isIgnored()) {
                    String[] tokens = DocumentName.FSInfo.getDefault().tokenize(document.getName().localized());
                    results.add(Arguments.of(tokens[1], document));
                }
            }
        };
        walker.run(report);
        return results.stream();
    }

    /**
     * Log that captures output for later review.
     */
    public static class FileLog implements Log {

        private final PrintStream logFile;

        /**
         * The level at which we will write messages
         */
        private Level level;


        FileLog(File logFile) throws IOException {
            this.logFile = new PrintStream(logFile);
            level = Level.INFO;
        }

        /**
         * Sets the level.Log messages below the specified level will
         * not be written to the log.
         *
         * @param level the level to use when writing messages.
         */
        public void setLevel(final Level level) {
            this.level = level;
        }

        @Override
        public Level getLevel() {
            return level;
        }

        @Override
        public void log(Level level, String msg) {
            if (isEnabled(level)) {
                logFile.printf("%s: %s%n", level, msg);
            }
        }

        public void close() throws IOException {
            logFile.close();
        }
    }
}
