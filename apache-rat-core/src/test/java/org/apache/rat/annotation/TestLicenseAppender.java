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
package org.apache.rat.annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.rat.test.utils.Resources;
import org.apache.rat.utils.DefaultLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class TestLicenseAppender {

    @TempDir
    private File baseTempFolder;

    private static final String FIRST_LICENSE_LINE = " Licensed to the Apache Software Foundation (ASF) under one";

    @FunctionalInterface
    private interface FileCreator {
        void createFile(Writer w) throws IOException;
    }

    private interface NewFileReader {
        void readFile(BufferedReader r) throws IOException;
    }

    private String getTemporaryFileWithName(String fileName) throws IOException {
        if (fileName != null) {
            return new File(baseTempFolder, fileName).getAbsolutePath();
        }
        return File.createTempFile("tla", null, baseTempFolder).getAbsolutePath();
    }

    private static void createTestFile(String fileName, FileCreator creator) throws IOException {
        try (FileWriter w = new FileWriter(fileName)) {
            creator.createFile(w);
        }
    }

    private void commonTestTemplate(String relativeName, FileCreator creator, NewFileReader reader) throws IOException {
        String name = getTemporaryFileWithName(relativeName);

        createTestFile(name, creator);

        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender();
        appender.append(new File(name));

        try (BufferedReader r = new BufferedReader(new FileReader(name + ".new"))) {
            reader.readFile(r);
        }
        FileUtils.delete(new File(name + ".new"));
        FileUtils.delete(new File(name));
    }

    private static NewFileReader checkLines(final String... lines) {
        return new NewFileReader() {
            @Override
            public void readFile(BufferedReader r) throws IOException {
                for (int i = 0; i < lines.length; i++) {
                    String line = r.readLine();
                    assertEquals(lines[i], line, String.format("Line %s is incorrect", i));
                }
            }
        };
    }

    @Test
    public void addLicenseToUnknownFile() throws IOException {
        String filename = getTemporaryFileWithName(null);
        createTestFile(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write("Unknown file type\n");
            }
        });

        File file = new File(filename);
        file.deleteOnExit();
        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender();
        appender.append(file);

        File newFile = new File(filename + ".new");
        newFile.deleteOnExit();
        assertFalse(newFile.exists(), "No new file should have been written");
    }

    @Test
    public void addLicenseToJava() throws IOException {
        String filename = "tmp.java";
        final String firstLine = "package foo;";
        final String secondLine = "";
        final String thirdLine = "/*";
        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write(firstLine + "\n");
                writer.write("\n");
                writer.write("public class test {\n");
                writer.write("}\n");
            }
        }, checkLines(firstLine, secondLine, thirdLine));
    }

    @Test
    public void addLicenseToJavaWithoutPackage() throws IOException {
        String filename = "tmp.java";
        String commentLine = "/*";
        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write("public class test {\n");
                writer.write("}\n");
            }
        }, checkLines(commentLine));
    }

    @Test
    public void addLicenseToXML() throws IOException {
        String filename = "tmp.xml";
        final String firstLine = "<?xml version='1.0'?>";
        final String secondLine = "";
        final String thirdLine = "<!--";

        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write(firstLine + "\n");
                writer.write("\n");
                writer.write("<xml>\n");
                writer.write("</xml>\n");
            }
        }, checkLines(firstLine, secondLine, thirdLine));
    }

    @Test
    public void addLicenseToXMLWithoutDecl() throws IOException {
        String filename = "tmp.xml";
        final String firstLine = "<?xml version='1.0'?>";
        final String secondLine = "<!--";

        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write("<xml>\n");
                writer.write("</xml>\n");
            }
        }, checkLines(firstLine, secondLine));
    }

    @Test
    public void addLicenseToHTML() throws IOException {
        String filename = "tmp.html";
        String commentLine = "<!--";

        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write("<html>\n");
                writer.write("\n");
                writer.write("</html>\n");
            }
        }, checkLines(commentLine));
    }

    @Test
    public void addLicenseToCSS() throws IOException {
        String filename = "tmp.css";
        String firstLine = "/*";

        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write(".class {\n");
                writer.write(" background-color: red;");
                writer.write("}\n");
            }
        }, checkLines(firstLine));
    }

    @Test
    public void addLicenseToJavascript() throws IOException {
        String filename = "tmp.js";
        String firstLine = "/*";

        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write("if (a ==b) {>\n");
                writer.write(" alert(\"how useful!\");");
                writer.write("}\n");
            }
        }, checkLines(firstLine));
    }

    @Test
    public void addLicenseToAPT() throws IOException {
        String filename = "tmp.apt";
        String firstLine = "~~" + FIRST_LICENSE_LINE;

        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write("A Simple APT file");
                writer.write(" This file contains nothing\n");
                writer.write(" of any importance\n");
            }
        }, checkLines(firstLine));
    }

    @Test
    public void addLicenseToProperties() throws IOException {
        String filename = "tmp.properties";
        String firstLine = "#" + FIRST_LICENSE_LINE;

        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write("property = value\n");
                writer.write("fun = true\n");
                writer.write("cool = true\n");
            }
        }, checkLines(firstLine));
    }

    @Test
    public void addLicenseToScala() throws IOException {
        String filename = "tmp.scala";
        final String firstLine = "package foo {";
        final String newFirstLine = "/*";

        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write(firstLine + "\n");
                writer.write("\n");
                writer.write("    object X { val x = 1; }\n");
                writer.write("}\n");
            }
        }, new NewFileReader() {
            @Override
            public void readFile(BufferedReader reader) throws IOException {
                String line = reader.readLine();
                assertEquals(newFirstLine, line, "First line is incorrect");
                while ((line = reader.readLine()) != null) {
                    if (line.length() == 0) {
                        line = reader.readLine();
                        break;
                    }
                }
                assertEquals(firstLine, line, "Package line is incorrect");
            }
        });
    }

    @Test
    public void addLicenseToRubyWithoutHashBang() throws IOException {
        String filename = "tmp.rb";
        String firstLine = "#" + FIRST_LICENSE_LINE;

        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write("class Foo\n");
                writer.write("end\n");
            }
        }, checkLines(firstLine));
    }

    @Test
    public void addLicenseToRubyWithHashBang() throws IOException {
        String filename = "tmp.rb";
        final String firstLine = "#!/usr/bin/env ruby";
        String secondLine = "#" + FIRST_LICENSE_LINE;

        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write(firstLine + "\n");
                writer.write("class Foo\n");
                writer.write("end\n");
            }
        }, checkLines(firstLine, secondLine));
    }

    @Test
    public void addLicenseToPerlWithoutHashBang() throws IOException {
        String filename = "tmp.pl";
        String firstLine = "#" + FIRST_LICENSE_LINE;

        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write("print \"Hello world\"\n");
            }
        }, checkLines(firstLine));
    }

    @Test
    public void addLicenseToPerlWithHashBang() throws IOException {
        String filename = "tmp.pl";
        final String firstLine = "#!/usr/bin/env perl";
        String secondLine = "#" + FIRST_LICENSE_LINE;

        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write(firstLine + "\n");
                writer.write("print \"Hello world\"\n");
            }
        }, checkLines(firstLine, secondLine));
    }

    @Test
    public void addLicenseToPerlModule() throws IOException {
        String filename = "tmp.pm";
        final String firstLine = "package API::TestAPI;";
        final String secondLine = "";
        final String thirdLine = "#" + FIRST_LICENSE_LINE;

        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write(firstLine + "\n");
                writer.write("print \"Hello world\"\n");
            }
        }, checkLines(firstLine, secondLine, thirdLine));
    }

    @Test
    public void addLicenseToTclWithoutHashBang() throws IOException {
        String filename = "tmp.tcl";
        String firstLine = "#" + FIRST_LICENSE_LINE;

        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write("puts \"Hello world\"\n");
            }
        }, checkLines(firstLine));
    }

    @Test
    public void addLicenseToTclWithHashBang() throws IOException {
        String filename = "tmp.tcl";
        final String firstLine = "#!/usr/bin/env tcl";
        String secondLine = "#" + FIRST_LICENSE_LINE;

        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write(firstLine + "\n");
                writer.write("puts \"Hello world\"\n");
            }
        }, checkLines(firstLine, secondLine));
    }

    @Test
    public void addLicenseToPHP() throws IOException {
        String filename = "tmp.php";
        final String firstLine = "<?php";
        final String secondLine = "";
        final String thirdLine = "/*";

        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write(firstLine + "\n");
                writer.write("echo 'Hello World'\n");
                writer.write("?>\n");
            }
        }, checkLines(firstLine, secondLine, thirdLine));
    }

    @Test
    public void addLicenseToCSharp() throws IOException {
        String filename = "tmp.cs";
        String firstLine = "/*";

        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write("namespace org.example {\n");
                writer.write("    public class Foo {\n");
                writer.write("    }\n");
                writer.write("}\n");
            }
        }, checkLines(firstLine));
    }

    @Test
    public void addLicenseToGroovy() throws IOException {
        String filename = "tmp.groovy";
        String firstLine = "/*";

        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write("package org.example \n");
                writer.write("    class Foo {\n");
                writer.write("    }\n");
            }
        }, checkLines(firstLine));
    }

    @Test
    public void addLicenseToCPlusPlus() throws IOException {
        String filename = "tmp.cpp";
        String firstLine = "/*";

        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write("namespace org.example {\n");
                writer.write("    public class Foo {\n");
                writer.write("    }\n");
                writer.write("}\n");
            }
        }, checkLines(firstLine));
    }

    @Test
    public void addLicenseToGo() throws IOException {
        String filename = "tmp.go";
        final String firstLine = "package main";
        String secondLine = "";
        String thirdLine = "/*";

        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write(firstLine + "\n");
                writer.write("import (\n");
                writer.write("    log\n");
                writer.write(")\n");
            }
        }, checkLines(firstLine, secondLine, thirdLine));
    }

    @Test
    public void fileWithBOM() throws IOException {
        File f = Resources.getResourceFile("violations/FilterTest.cs");

        ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender();
        appender.append(f);

        try (BufferedReader r = new BufferedReader(new FileReader(f.getAbsolutePath() + ".new"))) {
            assertEquals("/*", r.readLine());
            String line = null;
            while ((line = r.readLine()) != null) {
                if (line.trim().length() == 0) {
                    break;
                }
            }
            assertEquals("#if NET_2_0", r.readLine());
        }
    }

    @Test
    public void addLicenseToVS2003solution() throws IOException {
        String filename = "tmp.sln";
        final String firstLine = "Microsoft Visual Studio Solution File," + " Format Version 8.0";
        String secondLine = "#" + FIRST_LICENSE_LINE;

        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write(firstLine + "\n");
                writer.write(
                        "Project(\"{FAE04EC0-301F-11D3-BF4B-00C04F79EFBC}\") = \"ConsoleApp\", \"Tutorials\\ConsoleApp\\cs\\src\\ConsoleApp.csproj\", \"{933969DF-2BC5-44E6-8B1A-400FC276A23F}\"\n");
                writer.write("\tProjectSection(WebsiteProperties) = preProject\n");
                writer.write("\t\tDebug.AspNetCompiler.Debug = \"True\"\n");
                writer.write("\t\tRelease.AspNetCompiler.Debug = \"False\"\n");
                writer.write("\tEndProjectSection\n");
                writer.write("EndProject\n");
            }
        }, checkLines(firstLine, secondLine));
    }

    @Test
    public void addLicenseToVS2005solution() throws IOException {
        String filename = "tmp.sln";
        final String firstLine = "Microsoft Visual Studio Solution File," + " Format Version 9.0";
        final String secondLine = "# Visual Studio 2005";
        final String thirdLine = "#" + FIRST_LICENSE_LINE;

        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write(firstLine + "\n");
                writer.write(secondLine + "\n");
                writer.write(
                        "Project(\"{FAE04EC0-301F-11D3-BF4B-00C04F79EFBC}\") = \"ConsoleApp\", \"Tutorials\\ConsoleApp\\cs\\src\\ConsoleApp.csproj\", \"{933969DF-2BC5-44E6-8B1A-400FC276A23F}\"\n");
                writer.write("\tProjectSection(WebsiteProperties) = preProject\n");
                writer.write("\t\tDebug.AspNetCompiler.Debug = \"True\"\n");
                writer.write("\t\tRelease.AspNetCompiler.Debug = \"False\"\n");
                writer.write("\tEndProjectSection\n");
                writer.write("EndProject\n");
            }
        }, checkLines(firstLine, secondLine, thirdLine));
    }

    @Test
    public void addLicenseToVS2010ExpressSolution() throws IOException {
        String filename = "tmp.sln";
        final String firstLine = "Microsoft Visual Studio Solution File, " + "Format Version 11.00";
        final String secondLine = "# Visual C# Express 2010";
        final String thirdLine = "#" + FIRST_LICENSE_LINE;

        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write(firstLine + "\n");
                writer.write(secondLine + "\n");
                writer.write(
                        "Project(\"{FAE04EC0-301F-11D3-BF4B-00C04F79EFBC}\") = \"Lucene.Net\", \"..\\..\\..\\src\\core\\Lucene.Net.csproj\", \"{5D4AD9BE-1FFB-41AB-9943-25737971BF57}\"\n");
                writer.write("EndProject\n");
                writer.write(
                        "Project(\"{FAE04EC0-301F-11D3-BF4B-00C04F79EFBC}\") = \"Contrib.Highlighter\", \"..\\..\\..\\src\\contrib\\Highlighter\\Contrib.Highlighter.csproj\", \"{901D5415-383C-4AA6-A256-879558841BEA}\"\n");
                writer.write("EndProject\n");
                writer.write("Global\n");
                writer.write("GlobalSection(SolutionConfigurationPlatforms) = preSolution\n");
                writer.write("Debug|Any CPU = Debug|Any CPU\n");
                writer.write("Release|Any CPU = Release|Any CPU\n");
                writer.write("EndGlobalSection\n");
                writer.write("GlobalSection(ProjectConfigurationPlatforms) = postSolution\n");
                writer.write("{5D4AD9BE-1FFB-41AB-9943-25737971BF57}.Debug|Any CPU.ActiveCfg = Debug|Any CPU\n");
                writer.write("{5D4AD9BE-1FFB-41AB-9943-25737971BF57}.Debug|Any CPU.Build.0 = Debug|Any CPU\n");
                writer.write("{5D4AD9BE-1FFB-41AB-9943-25737971BF57}.Release|Any CPU.ActiveCfg = Release|Any CPU\n");
                writer.write("{5D4AD9BE-1FFB-41AB-9943-25737971BF57}.Release|Any CPU.Build.0 = Release|Any CPU\n");
                writer.write("{901D5415-383C-4AA6-A256-879558841BEA}.Debug|Any CPU.ActiveCfg = Debug|Any CPU\n");
                writer.write("{901D5415-383C-4AA6-A256-879558841BEA}.Debug|Any CPU.Build.0 = Debug|Any CPU\n");
                writer.write("{901D5415-383C-4AA6-A256-879558841BEA}.Release|Any CPU.ActiveCfg = Release|Any CPU\n");
                writer.write("{901D5415-383C-4AA6-A256-879558841BEA}.Release|Any CPU.Build.0 = Release|Any CPU\n");
                writer.write("EndGlobalSection\n");
                writer.write("GlobalSection(SolutionProperties) = preSolution\n");
                writer.write("HideSolutionNode = FALSE\n");
                writer.write("EndGlobalSection\n");
                writer.write("EndGlobal \n");
            }
        }, checkLines(firstLine, secondLine, thirdLine));
    }

    @Test
    public void addLicenseToVS2010SolutionWithBlankLine() throws IOException {
        String filename = "tmp.sln";
        final String firstLine = "";
        final String secondLine = "Microsoft Visual Studio Solution File, " + "Format Version 11.00";
        final String thirdLine = "# Visual C# Express 2010";
        final String forthLine = "#" + FIRST_LICENSE_LINE;

        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write(firstLine + "\n");
                writer.write(secondLine + "\n");
                writer.write(thirdLine + "\n");
                writer.write(
                        "Project(\"{FAE04EC0-301F-11D3-BF4B-00C04F79EFBC}\") = \"Lucene.Net\", \"..\\..\\..\\src\\core\\Lucene.Net.csproj\", \"{5D4AD9BE-1FFB-41AB-9943-25737971BF57}\"\n");
                writer.write("EndProject\n");
                writer.write(
                        "Project(\"{FAE04EC0-301F-11D3-BF4B-00C04F79EFBC}\") = \"Contrib.Highlighter\", \"..\\..\\..\\src\\contrib\\Highlighter\\Contrib.Highlighter.csproj\", \"{901D5415-383C-4AA6-A256-879558841BEA}\"\n");
                writer.write("EndProject\n");
                writer.write("Global\n");
                writer.write("GlobalSection(SolutionConfigurationPlatforms) = preSolution\n");
                writer.write("Debug|Any CPU = Debug|Any CPU\n");
                writer.write("Release|Any CPU = Release|Any CPU\n");
                writer.write("EndGlobalSection\n");
                writer.write("GlobalSection(ProjectConfigurationPlatforms) = postSolution\n");
                writer.write("{5D4AD9BE-1FFB-41AB-9943-25737971BF57}.Debug|Any CPU.ActiveCfg = Debug|Any CPU\n");
                writer.write("{5D4AD9BE-1FFB-41AB-9943-25737971BF57}.Debug|Any CPU.Build.0 = Debug|Any CPU\n");
                writer.write("{5D4AD9BE-1FFB-41AB-9943-25737971BF57}.Release|Any CPU.ActiveCfg = Release|Any CPU\n");
                writer.write("{5D4AD9BE-1FFB-41AB-9943-25737971BF57}.Release|Any CPU.Build.0 = Release|Any CPU\n");
                writer.write("{901D5415-383C-4AA6-A256-879558841BEA}.Debug|Any CPU.ActiveCfg = Debug|Any CPU\n");
                writer.write("{901D5415-383C-4AA6-A256-879558841BEA}.Debug|Any CPU.Build.0 = Debug|Any CPU\n");
                writer.write("{901D5415-383C-4AA6-A256-879558841BEA}.Release|Any CPU.ActiveCfg = Release|Any CPU\n");
                writer.write("{901D5415-383C-4AA6-A256-879558841BEA}.Release|Any CPU.Build.0 = Release|Any CPU\n");
                writer.write("EndGlobalSection\n");
                writer.write("GlobalSection(SolutionProperties) = preSolution\n");
                writer.write("HideSolutionNode = FALSE\n");
                writer.write("EndGlobalSection\n");
                writer.write("EndGlobal \n");
            }
        }, checkLines(firstLine, secondLine, thirdLine, forthLine));
    }

    @Test
    public void addLicenseMarkdown() throws IOException {
        String filename = "tmp.md";

        commonTestTemplate(filename, new FileCreator() {
            @Override
            public void createFile(Writer writer) throws IOException {
                writer.write("## This is the first header\n");
                writer.write(" * this is a list entry\n");
                writer.write(" * this is another list entry\n");
                writer.write(" <!-- this is a comment line -->");
                writer.write("## This is the second header\n");
            }
        }, new NewFileReader() {
            @Override
            public void readFile(BufferedReader r) throws IOException {
                String line = r.readLine();
                assertEquals("<!--", line, "First line is incorrect");
                line = r.readLine();
            }
        });
    }

    @Test
    public void testForced() throws IOException {
        String filename = "tmp.php";
        final String firstLine = "<?php";

        FileCreator phpCreator = (writer) -> {
            writer.write(firstLine + "\n");
            writer.write("echo 'Hello World'\n");
            writer.write("?>\n");
        };

        String name = getTemporaryFileWithName(filename);
        try {
            createTestFile(name, phpCreator);

            ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender();
            appender.setForce(true);
            appender.append(new File(name));

            assertFalse(new File(name + ".new").exists());
            assertTrue(new File(name).exists());

            try (FileInputStream inputStream = new FileInputStream(name)) {
                String everything = IOUtils.toString(inputStream, Charset.defaultCharset());
                assertTrue(everything.contains("Licensed to the Apache Software Foundation "));
            }
        } finally {
            new File(name + ".new").delete();
            FileUtils.delete(new File(name));
        }
    }

    @Test
    public void testExecutePermsPreserved() throws IOException {
        String filename = "tmp.php";
        final String firstLine = "<?php";

        FileCreator phpCreator = (writer) -> {
            writer.write(firstLine + "\n");
            writer.write("echo 'Hello World'\n");
            writer.write("?>\n");
        };

        String name = getTemporaryFileWithName(filename);
        try {
            createTestFile(name, phpCreator);
            assertTrue(new File(name).setExecutable(true), "Unable to set executable flag");

            ApacheV2LicenseAppender appender = new ApacheV2LicenseAppender();
            appender.setForce(true);
            appender.append(new File(name));

            assertFalse(new File(name + ".new").exists());
            assertTrue(new File(name).exists());

            try (FileInputStream inputStream = new FileInputStream(name)) {
                String everything = IOUtils.toString(inputStream, Charset.defaultCharset());
                assertTrue(everything.contains("Licensed to the Apache Software Foundation "));
            }
            assertTrue(new File(name).canExecute());
        } finally {
            new File(name + ".new").delete();
            FileUtils.delete(new File(name));
        }
    }
}
