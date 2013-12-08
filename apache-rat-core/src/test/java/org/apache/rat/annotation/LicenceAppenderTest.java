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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Random;

import org.apache.rat.test.utils.Resources;
import org.junit.Test;

/**
 * The Class TestLicenceAppender.
 */
public class LicenceAppenderTest {

	/** The Constant ZERO. */
	private static final int ZERO = 0;

	/** The Constant END_BRACKET. */
	private static final String END_BRACKET = "}\n";

	/** The Constant TMP_SLN. */
	private static final String TMP_SLN = "tmp.sln";

	/** The Constant SECOND_LINE_INCORRECT. */
	private static final String SECOND_LINE_INCORRECT = "Second line is incorrect";

	/** The Constant FIRST_LINE_INCORRECT. */
	private static final String FIRST_LINE_INCORRECT = "First line is incorrect";

	/** The Constant END_PROJECT. */
	private static final String END_PROJECT = "EndProject\n";

	/** The Constant END_GLOBAL_SELECTION. */
	private static final String END_GLOBAL_SELECTION = "EndGlobalSection\n";

	/** The Constant DOT_NEW. */
	private static final String DOT_NEW = ".new";
	/** Used to ensure that temporary files have unq. */
	private final Random random = new Random();

	/**
	 * The Interface FileCreator.
	 */
	private interface FileCreator {

		/**
		 * Creates the file.
		 * 
		 * @param writer
		 *            the writer
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		void createFile(Writer writer) throws IOException;
	}

	/**
	 * The Interface NewFileReader.
	 */
	private interface NewFileReader {

		/**
		 * Read file.
		 * 
		 * @param bufferedReader
		 *            the buffered reader
		 * @throws IOException
		 *             Signals that an I/O exception has occurred.
		 */
		void readFile(BufferedReader bufferedReader) throws IOException;
	}

	/**
	 * Qualify.
	 * 
	 * @param fileName
	 *            the file name
	 * @return the string
	 */
	private static String qualify(final String fileName) {
		return new File(new File(System.getProperty("java.io.tmpdir")),
				fileName).getAbsolutePath();
	}

	/**
	 * Creates the test file.
	 * 
	 * @param fileName
	 *            the file name
	 * @param creator
	 *            the creator
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private static void createTestFile(final String fileName,
			final FileCreator creator)
			throws IOException {
		FileWriter fileWriter = null;
		try {
			creator.createFile(fileWriter = new FileWriter(fileName));
		} finally {
			if (fileWriter != null) {
				fileWriter.close();
			}
		}
	}

	/**
	 * Try to delete.
	 * 
	 * @param file
	 *            the file
	 */
	private static void tryToDelete(final File file) {
		if (file != null && file.exists() && !file.delete()) {
			file.deleteOnExit();
		}
	}

	/**
	 * Common test template.
	 * 
	 * @param relativeName
	 *            the relative name
	 * @param creator
	 *            the creator
	 * @param reader
	 *            the reader
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private static void commonTestTemplate(final String relativeName,
			final FileCreator creator, final NewFileReader reader)
			throws IOException {
		String name = qualify(relativeName);
		try {
			createTestFile(name, creator);

			ApacheV2LicenceAppender appender = new ApacheV2LicenceAppender();
			appender.append(new File(name));

			BufferedReader bufferedReader = null;
			try {
				bufferedReader = new BufferedReader(new FileReader(name
						+ DOT_NEW));
				reader.readFile(bufferedReader);
			} finally {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			}
		} finally {
			tryToDelete(new File(name));
			tryToDelete(new File(name + DOT_NEW));
		}
	}

	/**
	 * Check lines.
	 * 
	 * @param firstLine
	 *            the first line
	 * @param secondLine
	 *            the second line
	 * @return the new file reader
	 */
	private static NewFileReader checkLines(final String firstLine,
			final String secondLine) {
		return new NewFileReader() {
			public void readFile(final BufferedReader bufferedReader)
					throws IOException {
				String line = bufferedReader.readLine();
				assertEquals(FIRST_LINE_INCORRECT, firstLine, line);
				if (secondLine != null) {
					line = bufferedReader.readLine();
					assertEquals(SECOND_LINE_INCORRECT, secondLine, line);
				}
			}
		};
	}

	/**
	 * Adds the licence to unknown file.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAddLicenceToUnknownFile() throws IOException {
		String filename = qualify("tmp" + random.nextLong() + ".unknownType");
		File file = null;
		File newFile = null;
		try {
			createTestFile(filename, new FileCreator() {
				public void createFile(final Writer writer) throws IOException {
					writer.write("Unknown file type\n");
				}
			});

			file = new File(filename);
			ApacheV2LicenceAppender appender = new ApacheV2LicenceAppender();
			appender.append(file);

			newFile = new File(filename + DOT_NEW);
			assertFalse("No new file should have been written",
					newFile.exists());
		} finally {
			tryToDelete(file);
			tryToDelete(newFile);
		}
	}

	/**
	 * Adds the licence to java.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAddLicenceToJava() throws IOException {
		String filename = "tmp.java";
		final String firstLine = "package foo;";
		String secondLine = "/*";
		commonTestTemplate(filename, new FileCreator() {
			public void createFile(final Writer writer) throws IOException {
				writer.write(firstLine + "\n");
				writer.write("\n");
				writer.write("public class test {\n");
				writer.write(END_BRACKET);
			}
		}, checkLines(firstLine, secondLine));
	}

	/**
	 * Adds the licence to java without package.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAddLicenceToJavaWithoutPackage() throws IOException {
		String filename = "tmp.java";
		String commentLine = "/*";
		commonTestTemplate(filename, new FileCreator() {
			public void createFile(final Writer writer) throws IOException {
				writer.write("public class test {\n");
				writer.write(END_BRACKET);
			}
		}, checkLines(commentLine, null));
	}

	/**
	 * Adds the licence to xml.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAddLicenceToXML() throws IOException {
		String filename = "tmp.xml";
		final String firstLine = "<?xml version='1.0'?>";
		String secondLine = "<!--";

		commonTestTemplate(filename, new FileCreator() {
			public void createFile(final Writer writer) throws IOException {
				writer.write(firstLine + "\n");
				writer.write("\n");
				writer.write("<xml>\n");
				writer.write("</xml>\n");
			}
		}, checkLines(firstLine, secondLine));
	}

	/**
	 * Adds the licence to xml without decl.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAddLicenceToXMLWithoutDecl() throws IOException {
		String filename = "tmp.xml";
		String firstLine = "<?xml version='1.0'?>";
		String secondLine = "<!--";

		commonTestTemplate(filename, new FileCreator() {
			public void createFile(final Writer writer) throws IOException {
				writer.write("<xml>\n");
				writer.write("</xml>\n");
			}
		}, checkLines(firstLine, secondLine));
	}

	/**
	 * Adds the licence to html.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAddLicenceToHTML() throws IOException {
		String filename = "tmp.html";
		String commentLine = "<!--";

		commonTestTemplate(filename, new FileCreator() {
			public void createFile(final Writer writer) throws IOException {
				writer.write("<html>\n");
				writer.write("\n");
				writer.write("</html>\n");
			}
		}, checkLines(commentLine, null));
	}

	/**
	 * Adds the licence to css.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAddLicenceToCSS() throws IOException {
		String filename = "tmp.css";
		String firstLine = "/*";

		commonTestTemplate(filename, new FileCreator() {
			public void createFile(final Writer writer) throws IOException {
				writer.write(".class {\n");
				writer.write(" background-color: red;");
				writer.write(END_BRACKET);
			}
		}, checkLines(firstLine, null));
	}

	/**
	 * Adds the licence to javascript.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAddLicenceToJavascript() throws IOException {
		String filename = "tmp.js";
		String firstLine = "/*";

		commonTestTemplate(filename, new FileCreator() {
			public void createFile(final Writer writer) throws IOException {
				writer.write("if (a ==b) {>\n");
				writer.write(" alert(\"how useful!\");");
				writer.write(END_BRACKET);
			}
		}, checkLines(firstLine, null));
	}

	/**
	 * Adds the licence to apt.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAddLicenceToAPT() throws IOException {
		String filename = "tmp.apt";
		String firstLine = "~~";

		commonTestTemplate(filename, new FileCreator() {
			public void createFile(final Writer writer) throws IOException {
				writer.write("A Simple APT file");
				writer.write(" This file contains nothing\n");
				writer.write(" of any importance\n");
			}
		}, checkLines(firstLine, null));
	}

	/**
	 * Adds the licence to properties.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAddLicenceToProperties() throws IOException {
		String filename = "tmp.properties";
		String firstLine = "#";

		commonTestTemplate(filename, new FileCreator() {
			public void createFile(final Writer writer) throws IOException {
				writer.write("property = value\n");
				writer.write("fun = true\n");
				writer.write("cool = true\n");
			}
		}, checkLines(firstLine, null));
	}

	/**
	 * Adds the licence to scala.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAddLicenceToScala() throws IOException {
		String filename = "tmp.scala";
		final String firstLine = "package foo {";
		final String newFirstLine = "/*";

		commonTestTemplate(filename, new FileCreator() {
			public void createFile(final Writer writer) throws IOException {
				writer.write(firstLine + "\n");
				writer.write("\n");
				writer.write("    object X { val x = 1; }\n");
				writer.write(END_BRACKET);
			}
		}, new NewFileReader() {
			public void readFile(final BufferedReader reader)
					throws IOException {
				String line = reader.readLine();
				String lineOne = line;
				// assertEquals(FIRST_LINE_INCORRECT, newFirstLine, line);
				while (line != null) {
					if (line.length() == ZERO) {
						line = reader.readLine();
						break;
					}
					line = reader.readLine();
				}
				assertEquals(
						"First line is incorrect Package line is incorrect",
						newFirstLine + firstLine, lineOne + line);
			}
		});
	}

	/**
	 * Adds the license to ruby without hash bang.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAddLicenseToRubyWithoutHashBang() throws IOException {
		String filename = "tmp.rb";
		String firstLine = "#";

		commonTestTemplate(filename, new FileCreator() {
			public void createFile(final Writer writer) throws IOException {
				writer.write("class Foo\n");
				writer.write("end\n");
			}
		}, checkLines(firstLine, null));
	}

	/**
	 * Adds the license to ruby with hash bang.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAddLicenseToRubyWithHashBang() throws IOException {
		String filename = "tmp.rb";
		final String firstLine = "#!/usr/bin/env ruby";
		String secondLine = "#";

		commonTestTemplate(filename, new FileCreator() {
			public void createFile(final Writer writer) throws IOException {
				writer.write(firstLine + "\n");
				writer.write("class Foo\n");
				writer.write("end\n");
			}
		}, checkLines(firstLine, secondLine));
	}

	/**
	 * Adds the license to perl without hash bang.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAddLicenseToPerlWithoutHashBang() throws IOException {
		String filename = "tmp.pl";
		String firstLine = "#";

		commonTestTemplate(filename, new FileCreator() {
			public void createFile(final Writer writer) throws IOException {
				writer.write("print \"Hello world\"\n");
			}
		}, checkLines(firstLine, null));
	}

	/**
	 * Adds the license to perl with hash bang.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAddLicenseToPerlWithHashBang() throws IOException {
		String filename = "tmp.pl";
		final String firstLine = "#!/usr/bin/env perl";
		String secondLine = "#";

		commonTestTemplate(filename, new FileCreator() {
			public void createFile(final Writer writer) throws IOException {
				writer.write(firstLine + "\n");
				writer.write("print \"Hello world\"\n");
			}
		}, checkLines(firstLine, secondLine));
	}

	/**
	 * Adds the license to tcl without hash bang.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAddLicenseToTclWithoutHashBang() throws IOException {
		String filename = "tmp.tcl";
		String firstLine = "#";

		commonTestTemplate(filename, new FileCreator() {
			public void createFile(final Writer writer) throws IOException {
				writer.write("puts \"Hello world\"\n");
			}
		}, checkLines(firstLine, null));
	}

	/**
	 * Adds the license to tcl with hash bang.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAddLicenseToTclWithHashBang() throws IOException {
		String filename = "tmp.tcl";
		final String firstLine = "#!/usr/bin/env tcl";
		String secondLine = "#";

		commonTestTemplate(filename, new FileCreator() {
			public void createFile(final Writer writer) throws IOException {
				writer.write(firstLine + "\n");
				writer.write("puts \"Hello world\"\n");
			}
		}, checkLines(firstLine, secondLine));
	}

	/**
	 * Adds the licence to php.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAddLicenceToPHP() throws IOException {
		String filename = "tmp.php";
		final String firstLine = "<?php";
		String secondLine = "/*";

		commonTestTemplate(filename, new FileCreator() {
			public void createFile(final Writer writer) throws IOException {
				writer.write(firstLine + "\n");
				writer.write("echo 'Hello World'\n");
				writer.write("?>\n");
			}
		}, checkLines(firstLine, secondLine));
	}

	/**
	 * Adds the licence to c sharp.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAddLicenceToCSharp() throws IOException {
		String filename = "tmp.cs";
		String firstLine = "/*";

		commonTestTemplate(filename, new FileCreator() {
			public void createFile(final Writer writer) throws IOException {
				writer.write("namespace org.example {\n");
				writer.write("    public class Foo {\n");
				writer.write("    }\n");
				writer.write(END_BRACKET);
			}
		}, checkLines(firstLine, null));
	}

	/**
	 * Adds the licence to groovy.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAddLicenceToGroovy() throws IOException {
		String filename = "tmp.groovy";
		String firstLine = "/*";

		commonTestTemplate(filename, new FileCreator() {
			public void createFile(final Writer writer) throws IOException {
				writer.write("package org.example \n");
				writer.write("    class Foo {\n");
				writer.write("    }\n");
			}
		}, checkLines(firstLine, null));
	}

	/**
	 * Adds the licence to c plus plus.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAddLicenceToCPlusPlus() throws IOException {
		String filename = "tmp.cpp";
		String firstLine = "/*";

		commonTestTemplate(filename, new FileCreator() {
			public void createFile(final Writer writer) throws IOException {
				writer.write("namespace org.example {\n");
				writer.write("    public class Foo {\n");
				writer.write("    }\n");
				writer.write(END_BRACKET);
			}
		}, checkLines(firstLine, null));
	}

	/**
	 * File with bom.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void fileWithBOM() throws IOException {
		File file = Resources.getResourceFile("violations/FilterTest.cs");
		try {
			ApacheV2LicenceAppender appender = new ApacheV2LicenceAppender();
			appender.append(file);

			BufferedReader bufferedReader = null;
			try {
				bufferedReader = new BufferedReader(new FileReader(
						file.getAbsolutePath()
						+ DOT_NEW));
				String begining = bufferedReader.readLine();
				String line = bufferedReader.readLine();
				while (line != null) {
					if (line.trim().length() == ZERO) {
						break;
					}
					line = bufferedReader.readLine();
				}
				String ending = bufferedReader.readLine();
				assertEquals(
						"The string must be /* The string must be #if NET_2_0",
						"/*#if NET_2_0", begining + ending);
			} finally {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			}
		} finally {
			tryToDelete(new File(file.getAbsolutePath() + DOT_NEW));
		}
	}

	/**
	 * Adds the licence to v s2003solution.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAddLicenceToVS2003solution() throws IOException {
		String filename = TMP_SLN;
		final String firstLine = "Microsoft Visual Studio Solution File,"
				+ " Format Version 8.0";
		String secondLine = "#";

		commonTestTemplate(filename, new FileCreator() {
			public void createFile(final Writer writer) throws IOException {
				writer.write(firstLine + "\n");
				writer.write("Project(\"{FAE04EC0-301F-11D3-BF4B-00C04F79EFBC}\") = \"ConsoleApp\", \"Tutorials\\ConsoleApp\\cs\\src\\ConsoleApp.csproj\", \"{933969DF-2BC5-44E6-8B1A-400FC276A23F}\"\n");
				writer.write("	ProjectSection(WebsiteProperties) = preProject\n");
				writer.write("		Debug.AspNetCompiler.Debug = \"True\"\n");
				writer.write("		Release.AspNetCompiler.Debug = \"False\"\n");
				writer.write("	EndProjectSection\n");
				writer.write(END_PROJECT);
			}
		}, checkLines(firstLine, secondLine));
	}

	/**
	 * Adds the licence to v s2005solution.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAddLicenceToVS2005solution() throws IOException {
		String filename = TMP_SLN;
		final String firstLine = "Microsoft Visual Studio Solution File,"
				+ " Format Version 9.0";
		final String secondLine = "# Visual Studio 2005";
		final String thirdLine = "#";

		commonTestTemplate(filename, new FileCreator() {
			public void createFile(final Writer writer) throws IOException {
				writer.write(firstLine + "\n");
				writer.write(secondLine + "\n");
				writer.write("Project(\"{FAE04EC0-301F-11D3-BF4B-00C04F79EFBC}\") = \"ConsoleApp\", \"Tutorials\\ConsoleApp\\cs\\src\\ConsoleApp.csproj\", \"{933969DF-2BC5-44E6-8B1A-400FC276A23F}\"\n");
				writer.write("	ProjectSection(WebsiteProperties) = preProject\n");
				writer.write("		Debug.AspNetCompiler.Debug = \"True\"\n");
				writer.write("		Release.AspNetCompiler.Debug = \"False\"\n");
				writer.write("	EndProjectSection\n");
				writer.write(END_PROJECT);
			}
		}, new NewFileReader() {
			public void readFile(final BufferedReader bufferedReader)
					throws IOException {
				String lineOne = bufferedReader.readLine();
				String lineTwo = bufferedReader.readLine();
				String lineThree = bufferedReader.readLine();
				assertEquals(
						"First line is incorrect Second line is incorrect Third line is incorrect",
						firstLine + secondLine + thirdLine, lineOne + lineTwo
								+ lineThree);
			}
		});
	}

	/**
	 * Adds the licence to v s2010 express solution.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAddLicenceToVS2010ExpressSolution() throws IOException {
		String filename = TMP_SLN;
		final String firstLine = "Microsoft Visual Studio Solution File, "
				+ "Format Version 11.00";
		final String secondLine = "# Visual C# Express 2010";
		final String thirdLine = "#";

		commonTestTemplate(filename, new FileCreator() {
			public void createFile(final Writer writer) throws IOException {
				writer.write(firstLine + "\n");
				writer.write(secondLine + "\n");
				writer.write("Project(\"{FAE04EC0-301F-11D3-BF4B-00C04F79EFBC}\") = \"Lucene.Net\", \"..\\..\\..\\src\\core\\Lucene.Net.csproj\", \"{5D4AD9BE-1FFB-41AB-9943-25737971BF57}\"\n");
				writer.write(END_PROJECT);
				writer.write("Project(\"{FAE04EC0-301F-11D3-BF4B-00C04F79EFBC}\") = \"Contrib.Highlighter\", \"..\\..\\..\\src\\contrib\\Highlighter\\Contrib.Highlighter.csproj\", \"{901D5415-383C-4AA6-A256-879558841BEA}\"\n");
				writer.write(END_PROJECT);
				writer.write("Global\n");
				writer.write("GlobalSection(SolutionConfigurationPlatforms) = preSolution\n");
				writer.write("Debug|Any CPU = Debug|Any CPU\n");
				writer.write("Release|Any CPU = Release|Any CPU\n");
				writer.write(END_GLOBAL_SELECTION);
				writer.write("GlobalSection(ProjectConfigurationPlatforms) = postSolution\n");
				writer.write("{5D4AD9BE-1FFB-41AB-9943-25737971BF57}.Debug|Any CPU.ActiveCfg = Debug|Any CPU\n");
				writer.write("{5D4AD9BE-1FFB-41AB-9943-25737971BF57}.Debug|Any CPU.Build.0 = Debug|Any CPU\n");
				writer.write("{5D4AD9BE-1FFB-41AB-9943-25737971BF57}.Release|Any CPU.ActiveCfg = Release|Any CPU\n");
				writer.write("{5D4AD9BE-1FFB-41AB-9943-25737971BF57}.Release|Any CPU.Build.0 = Release|Any CPU\n");
				writer.write("{901D5415-383C-4AA6-A256-879558841BEA}.Debug|Any CPU.ActiveCfg = Debug|Any CPU\n");
				writer.write("{901D5415-383C-4AA6-A256-879558841BEA}.Debug|Any CPU.Build.0 = Debug|Any CPU\n");
				writer.write("{901D5415-383C-4AA6-A256-879558841BEA}.Release|Any CPU.ActiveCfg = Release|Any CPU\n");
				writer.write("{901D5415-383C-4AA6-A256-879558841BEA}.Release|Any CPU.Build.0 = Release|Any CPU\n");
				writer.write(END_GLOBAL_SELECTION);
				writer.write("GlobalSection(SolutionProperties) = preSolution\n");
				writer.write("HideSolutionNode = FALSE\n");
				writer.write(END_GLOBAL_SELECTION);
				writer.write("EndGlobal \n");
			}
		}, new NewFileReader() {
			public void readFile(final BufferedReader bufferedReader)
					throws IOException {
				String lineOne = bufferedReader.readLine();
				String lineTwo = bufferedReader.readLine();
				String lineThree = bufferedReader.readLine();
				assertEquals(
						"First line is incorrect Second line is incorrect Third line is incorrect",
						firstLine + secondLine + thirdLine, lineOne + lineTwo
								+ lineThree);
			}
		});
	}

	/**
	 * Adds the licence to v s2010 solution with blank line.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAddLicenceToVS2010SolutionWithBlankLine()
			throws IOException {
		String filename = TMP_SLN;
		final String firstLine = "";
		final String secondLine = "Microsoft Visual Studio Solution File, "
				+ "Format Version 11.00";
		final String thirdLine = "# Visual C# Express 2010";
		final String forthLine = "#";

		commonTestTemplate(filename, new FileCreator() {
			public void createFile(final Writer writer) throws IOException {
				writer.write(firstLine + "\n");
				writer.write(secondLine + "\n");
				writer.write(thirdLine + "\n");
				writer.write("Project(\"{FAE04EC0-301F-11D3-BF4B-00C04F79EFBC}\") = \"Lucene.Net\", \"..\\..\\..\\src\\core\\Lucene.Net.csproj\", \"{5D4AD9BE-1FFB-41AB-9943-25737971BF57}\"\n");
				writer.write(END_PROJECT);
				writer.write("Project(\"{FAE04EC0-301F-11D3-BF4B-00C04F79EFBC}\") = \"Contrib.Highlighter\", \"..\\..\\..\\src\\contrib\\Highlighter\\Contrib.Highlighter.csproj\", \"{901D5415-383C-4AA6-A256-879558841BEA}\"\n");
				writer.write(END_PROJECT);
				writer.write("Global\n");
				writer.write("GlobalSection(SolutionConfigurationPlatforms) = preSolution\n");
				writer.write("Debug|Any CPU = Debug|Any CPU\n");
				writer.write("Release|Any CPU = Release|Any CPU\n");
				writer.write(END_GLOBAL_SELECTION);
				writer.write("GlobalSection(ProjectConfigurationPlatforms) = postSolution\n");
				writer.write("{5D4AD9BE-1FFB-41AB-9943-25737971BF57}.Debug|Any CPU.ActiveCfg = Debug|Any CPU\n");
				writer.write("{5D4AD9BE-1FFB-41AB-9943-25737971BF57}.Debug|Any CPU.Build.0 = Debug|Any CPU\n");
				writer.write("{5D4AD9BE-1FFB-41AB-9943-25737971BF57}.Release|Any CPU.ActiveCfg = Release|Any CPU\n");
				writer.write("{5D4AD9BE-1FFB-41AB-9943-25737971BF57}.Release|Any CPU.Build.0 = Release|Any CPU\n");
				writer.write("{901D5415-383C-4AA6-A256-879558841BEA}.Debug|Any CPU.ActiveCfg = Debug|Any CPU\n");
				writer.write("{901D5415-383C-4AA6-A256-879558841BEA}.Debug|Any CPU.Build.0 = Debug|Any CPU\n");
				writer.write("{901D5415-383C-4AA6-A256-879558841BEA}.Release|Any CPU.ActiveCfg = Release|Any CPU\n");
				writer.write("{901D5415-383C-4AA6-A256-879558841BEA}.Release|Any CPU.Build.0 = Release|Any CPU\n");
				writer.write(END_GLOBAL_SELECTION);
				writer.write("GlobalSection(SolutionProperties) = preSolution\n");
				writer.write("HideSolutionNode = FALSE\n");
				writer.write(END_GLOBAL_SELECTION);
				writer.write("EndGlobal \n");
			}
		}, new NewFileReader() {
			public void readFile(final BufferedReader bufferedReader)
					throws IOException {
				String lineOne = bufferedReader.readLine();
				String lineTwo = bufferedReader.readLine();
				String lineThree = bufferedReader.readLine();
				String lineFour = bufferedReader.readLine();
				assertEquals(
						"the string must be First line is incorrect Second line is incorrect Third line is incorrect Forth line is incorrect",
						firstLine + secondLine + thirdLine + forthLine, lineOne
								+ lineTwo + lineThree + lineFour);
			}
		});
	}
}
