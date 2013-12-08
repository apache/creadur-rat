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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Add a licence header to a document. This appender does not check for the
 * existence of an existing licence header, it is assumed that either a second
 * licence header is intentional or that there is no licence header present
 * already.
 * 
 */
public abstract class AbstractLicenceAppender {

	/** The Constant TYPE_UNKNOWN. */
	private static final int TYPE_UNKNOWN = 0;

	/** The Constant TYPE_JAVA. */
	private static final int TYPE_JAVA = 1;

	/** The Constant TYPE_XML. */
	private static final int TYPE_XML = 2;

	/** The Constant TYPE_HTML. */
	private static final int TYPE_HTML = 3;

	/** The Constant TYPE_CSS. */
	private static final int TYPE_CSS = 4;

	/** The Constant TYPE_JAVASCRIPT. */
	private static final int TYPE_JAVASCRIPT = 5;

	/** The Constant TYPE_APT. */
	private static final int TYPE_APT = 6;

	/** The Constant TYPE_PROPERTIES. */
	private static final int TYPE_PROPERTIES = 7;

	/** The Constant TYPE_PYTHON. */
	private static final int TYPE_PYTHON = 8;

	/** The Constant TYPE_C. */
	private static final int TYPE_C = 9;

	/** The Constant TYPE_H. */
	private static final int TYPE_H = 10;

	/** The Constant TYPE_SH. */
	private static final int TYPE_SH = 11;

	/** The Constant TYPE_BAT. */
	private static final int TYPE_BAT = 12;

	/** The Constant TYPE_VM. */
	private static final int TYPE_VM = 13;

	/** The Constant TYPE_SCALA. */
	private static final int TYPE_SCALA = 14;

	/** The Constant TYPE_RUBY. */
	private static final int TYPE_RUBY = 15;

	/** The Constant TYPE_PERL. */
	private static final int TYPE_PERL = 16;

	/** The Constant TYPE_TCL. */
	private static final int TYPE_TCL = 17;

	/** The Constant TYPE_CPP. */
	private static final int TYPE_CPP = 18;

	/** The Constant TYPE_CSHARP. */
	private static final int TYPE_CSHARP = 19;

	/** The Constant TYPE_PHP. */
	private static final int TYPE_PHP = 20;

	/** The Constant TYPE_GROOVY. */
	private static final int TYPE_GROOVY = 21;

	/** The Constant TYPE_VISUAL_STUDIO_SOLUTION. */
	private static final int TYPE_VISUAL_STUDIO_SOLUTION = 22;

	/** the line separator for this OS. */
	private static final String LINE_SEP = System.getProperty("line.separator");

	/** The Constant FAMILY_C. */
	private static final int[] FAMILY_C = new int[] { TYPE_JAVA,
			TYPE_JAVASCRIPT, TYPE_C, TYPE_H, TYPE_SCALA, TYPE_CSS, TYPE_CPP,
			TYPE_CSHARP, TYPE_PHP, TYPE_GROOVY, };

	/** The Constant FAMILY_SGML. */
	private static final int[] FAMILY_SGML = new int[] { TYPE_XML, TYPE_HTML, };

	/** The Constant FAMILY_SH. */
	private static final int[] FAMILY_SH = new int[] { TYPE_PROPERTIES,
			TYPE_PYTHON, TYPE_SH, TYPE_RUBY, TYPE_PERL, TYPE_TCL,
			TYPE_VISUAL_STUDIO_SOLUTION, };

	/** The Constant FAMILY_BAT. */
	private static final int[] FAMILY_BAT = new int[] { TYPE_BAT, };

	/** The Constant FAMILY_APT. */
	private static final int[] FAMILY_APT = new int[] { TYPE_APT, };

	/** The Constant FAMILY_VELOCITY. */
	private static final int[] FAMILY_VELOCITY = new int[] { TYPE_VM, };

	/** The Constant EXPECTS_HASH_PLING. */
	private static final int[] EXPECTS_HASH_PLING = new int[] { TYPE_PYTHON,
			TYPE_SH, TYPE_RUBY, TYPE_PERL, TYPE_TCL };

	/** The Constant EXPECTS_AT_ECHO. */
	private static final int[] EXPECTS_AT_ECHO = new int[] { TYPE_BAT, };

	/** The Constant EXPECTS_PACKAGE. */
	private static final int[] EXPECTS_PACKAGE = new int[] { TYPE_JAVA, };

	/** The Constant EXPECTS_XML_DECL. */
	private static final int[] EXPECTS_XML_DECL = new int[] { TYPE_XML, };

	/** The Constant EXPECTS_PHP_PI. */
	private static final int[] EXPECTS_PHP_PI = new int[] { TYPE_PHP, };

	/** The Constant EXPECTS_MSVSSF_HEADER. */
	private static final int[] EXPECTS_MSVSSF_HEADER = new int[] { TYPE_VISUAL_STUDIO_SOLUTION, };

	/** The Constant EXT2TYPE. */
	private static final Map<String, Integer> EXT2TYPE = new ConcurrentHashMap<String, Integer>();

	/** The is forced. */
	private boolean isForced;

	static {
		// these arrays are used in Arrays.binarySearch so they must
		// be sorted
		Arrays.sort(FAMILY_C);
		Arrays.sort(FAMILY_SGML);
		Arrays.sort(FAMILY_SH);
		Arrays.sort(FAMILY_BAT);
		Arrays.sort(FAMILY_APT);
		Arrays.sort(FAMILY_VELOCITY);

		Arrays.sort(EXPECTS_HASH_PLING);
		Arrays.sort(EXPECTS_AT_ECHO);
		Arrays.sort(EXPECTS_PACKAGE);
		Arrays.sort(EXPECTS_XML_DECL);
		Arrays.sort(EXPECTS_MSVSSF_HEADER);

		EXT2TYPE.put("apt", Integer.valueOf(TYPE_APT));
		EXT2TYPE.put("asax", Integer.valueOf(TYPE_HTML));
		EXT2TYPE.put("ascx", Integer.valueOf(TYPE_HTML));
		EXT2TYPE.put("aspx", Integer.valueOf(TYPE_HTML));
		EXT2TYPE.put("bat", Integer.valueOf(TYPE_BAT));
		EXT2TYPE.put("c", Integer.valueOf(TYPE_C));
		EXT2TYPE.put("cc", Integer.valueOf(TYPE_CPP));
		EXT2TYPE.put("cmd", Integer.valueOf(TYPE_BAT));
		EXT2TYPE.put("config", Integer.valueOf(TYPE_XML));
		EXT2TYPE.put("cpp", Integer.valueOf(TYPE_CPP));
		EXT2TYPE.put("cs", Integer.valueOf(TYPE_CSHARP));
		EXT2TYPE.put("csdproj", Integer.valueOf(TYPE_XML));
		EXT2TYPE.put("csproj", Integer.valueOf(TYPE_XML));
		EXT2TYPE.put("css", Integer.valueOf(TYPE_CSS));
		EXT2TYPE.put("fxcop", Integer.valueOf(TYPE_XML));
		EXT2TYPE.put("groovy", Integer.valueOf(TYPE_GROOVY));
		EXT2TYPE.put("h", Integer.valueOf(TYPE_H));
		EXT2TYPE.put("hh", Integer.valueOf(TYPE_H));
		EXT2TYPE.put("hpp", Integer.valueOf(TYPE_H));
		EXT2TYPE.put("htm", Integer.valueOf(TYPE_HTML));
		EXT2TYPE.put("html", Integer.valueOf(TYPE_HTML));
		EXT2TYPE.put("java", Integer.valueOf(TYPE_JAVA));
		EXT2TYPE.put("js", Integer.valueOf(TYPE_JAVASCRIPT));
		EXT2TYPE.put("ndoc", Integer.valueOf(TYPE_XML));
		EXT2TYPE.put("nunit", Integer.valueOf(TYPE_XML));
		EXT2TYPE.put("php", Integer.valueOf(TYPE_PHP));
		EXT2TYPE.put("pl", Integer.valueOf(TYPE_PERL));
		EXT2TYPE.put("properties", Integer.valueOf(TYPE_PROPERTIES));
		EXT2TYPE.put("py", Integer.valueOf(TYPE_PYTHON));
		EXT2TYPE.put("rb", Integer.valueOf(TYPE_RUBY));
		EXT2TYPE.put("rdf", Integer.valueOf(TYPE_XML));
		EXT2TYPE.put("resx", Integer.valueOf(TYPE_XML));
		EXT2TYPE.put("scala", Integer.valueOf(TYPE_SCALA));
		EXT2TYPE.put("sh", Integer.valueOf(TYPE_SH));
		EXT2TYPE.put("shfbproj", Integer.valueOf(TYPE_XML));
		EXT2TYPE.put("sln", Integer.valueOf(TYPE_VISUAL_STUDIO_SOLUTION));
		EXT2TYPE.put("stylecop", Integer.valueOf(TYPE_XML));
		EXT2TYPE.put("svg", Integer.valueOf(TYPE_XML));
		EXT2TYPE.put("tcl", Integer.valueOf(TYPE_TCL));
		EXT2TYPE.put("vbdproj", Integer.valueOf(TYPE_XML));
		EXT2TYPE.put("vbproj", Integer.valueOf(TYPE_XML));
		EXT2TYPE.put("vcproj", Integer.valueOf(TYPE_XML));
		EXT2TYPE.put("vm", Integer.valueOf(TYPE_VM));
		EXT2TYPE.put("vsdisco", Integer.valueOf(TYPE_XML));
		EXT2TYPE.put("webinfo", Integer.valueOf(TYPE_XML));
		EXT2TYPE.put("xml", Integer.valueOf(TYPE_XML));
		EXT2TYPE.put("xsl", Integer.valueOf(TYPE_XML));
	}

	/**
	 * Instantiates a new abstract licence appender.
	 */
	public AbstractLicenceAppender() {
		super();
	}

	/**
	 * Append the default licence header to the supplied document.
	 * 
	 * @param document
	 *            the document
	 * @throws IOException
	 *             if there is a problem either reading or writing the file
	 */
	public void append(final File document) throws IOException {
		int type = getType(document);
		if (type == TYPE_UNKNOWN) {
			return;
		}

		boolean expectsHashPling = expectsHashPling(type);
		boolean expectsAtEcho = expectsAtEcho(type);
		boolean expectsPackage = expectsPackage(type);
		boolean expectsXMLDecl = expectsXMLDecl(type);
		boolean expectsPhpPI = expectsPhpPI(type);
		boolean expectsMSVSSF = expectsMSVisualStudioSolutionFileHeader(type);

		File newDocument = new File(document.getAbsolutePath() + ".new");
		FileWriter writer = new FileWriter(newDocument);
		try {
			if (!attachLicense(writer, document, expectsHashPling,
					expectsAtEcho, expectsPackage, expectsXMLDecl,
					expectsPhpPI, expectsMSVSSF)) {
				// Java File without package, XML file without decl or PHP
				// file without PI
				// for Java just place the license at the front, for XML add
				// an XML decl first - don't know how to handle PHP
				if (expectsPackage || expectsXMLDecl) {
					writer = new FileWriter(newDocument);
					if (expectsXMLDecl) {
						writer.write("<?xml version='1.0'?>");
						writer.write(LINE_SEP);
					}
					attachLicense(writer, document, false, false, false, false,
							false, false);
				}
			}
		} finally {
			writer.close();
		}

		if (isForced) {
			document.delete();
			boolean renamed = newDocument.renameTo(document.getAbsoluteFile());
			if (!renamed) {
				throw new IOException(
						"Failed to rename new file, original file remains unchanged.");
			}
		}
	}

	/**
	 * Write document's content to writer attaching the license using the given
	 * flags as hints for where to put it.
	 * 
	 * @param writer
	 *            the writer
	 * @param document
	 *            the document
	 * @param expectsHashPling
	 *            the expects hash pling
	 * @param expectsAtEcho
	 *            the expects at echo
	 * @param expectsPackage
	 *            the expects package
	 * @param expectsXMLDecl
	 *            the expects xml decl
	 * @param expectsPhpPI
	 *            the expects php pi
	 * @param expectsMSVSSF
	 *            the expects msvssf
	 * @return whether the license has actually been written
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private boolean attachLicense(final Writer writer, final File document,
			final boolean expectsHashPling, final boolean expectsAtEcho,
			final boolean expectsPackage, final boolean expectsXMLDecl,
			final boolean expectsPhpPI, final boolean expectsMSVSSF)
			throws IOException {
		boolean written = false;
		try {
			FileInputStream fis = new FileInputStream(document);
			BufferedReader bufferedReader = null;
			try {
				bufferedReader = new BufferedReader(new InputStreamReader(
						new BOMInputStream(fis)));

				if (!expectsHashPling && !expectsAtEcho && !expectsPackage
						&& !expectsXMLDecl && !expectsPhpPI && !expectsMSVSSF) {
					written = true;
					writer.write(getLicenceHeader(document));
					writer.write(LINE_SEP);
				}

				String line;
				boolean first = true;
				while ((line = bufferedReader.readLine()) != null) {
					if (first && expectsHashPling) {
						written = true;
						doFirstLine(document, writer, line, "#!");
					} else if (first && expectsAtEcho) {
						written = true;
						doFirstLine(document, writer, line, "@echo");
					} else if (first && expectsMSVSSF) {
						written = true;
						if ("".equals(line)) {
							line = passThroughReadNext(writer, line,
									bufferedReader);
						}
						if (line.startsWith("Microsoft Visual Studio Solution"
								+ " File")) {
							line = passThroughReadNext(writer, line,
									bufferedReader);
						}
						doFirstLine(document, writer, line, "# Visual ");
					} else {
						writer.write(line);
						writer.write(LINE_SEP);
					}

					if (expectsPackage && line.startsWith("package ")) {
						written = true;
						writer.write(getLicenceHeader(document));
						writer.write(LINE_SEP);
					} else if (expectsXMLDecl && line.startsWith("<?xml ")) {
						written = true;
						writer.write(getLicenceHeader(document));
						writer.write(LINE_SEP);
					} else if (expectsPhpPI && line.startsWith("<?php")) {
						written = true;
						writer.write(getLicenceHeader(document));
						writer.write(LINE_SEP);
					}
					first = false;
				}
			} finally {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
				fis.close();
			}
		} finally {
			writer.close();
		}
		return written;
	}

	/**
	 * Check first line for specified text and process.
	 * 
	 * @param document
	 *            the document
	 * @param writer
	 *            the writer
	 * @param line
	 *            the line
	 * @param lookfor
	 *            the lookfor
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void doFirstLine(final File document, final Writer writer,
			final String line, final String lookfor) throws IOException {
		if (line.startsWith(lookfor)) {
			writer.write(line);
			writer.write(LINE_SEP);
			writer.write(getLicenceHeader(document));
		} else {
			writer.write(getLicenceHeader(document));
			writer.write(line);
			writer.write(LINE_SEP);
		}
	}

	/**
	 * Detect the type of document.
	 * 
	 * @param document
	 *            the document
	 * @return not null
	 * @TODO use existing mechanism to detect the type of a file and record it
	 *       in the report output, thus we will not need this duplication here.
	 */
	protected int getType(final File document) {
		int result = TYPE_UNKNOWN;
		String path = document.getPath();
		int lastDot = path.lastIndexOf('.');
		if (lastDot >= 0 && lastDot < path.length() - 1) {
			String ext = path.substring(lastDot + 1);
			Integer type = EXT2TYPE.get(ext);
			if (type != null) {
				result = type.intValue();
			}
		}
		return result;
	}

	/**
	 * Set the force flag on this appender. If this flag is set to true then
	 * files will be modified directly, otherwise new files will be created
	 * alongside the existing files.
	 * 
	 * @param forced
	 *            the new force
	 */
	public void setForce(final boolean forced) {
		isForced = forced;
	}

	/**
	 * Get the licence header for a document.
	 * 
	 * @param document
	 *            the document
	 * @return the licence header
	 */
	public abstract String getLicenceHeader(File document);

	/**
	 * Get the first line of the licence header formatted for the given type of
	 * file.
	 * 
	 * @param type
	 *            the type of file, see the TYPE_* constants
	 * @return not null
	 */
	protected String getFirstLine(final int type) {
		String result = "";
		if (isFamilyC(type)) {
			result = "/*" + LINE_SEP;
		} else if (isFamilySGML(type)) {
			result = "<!--" + LINE_SEP;
		} else if (isFamilyAPT(type)) {
			result = "~~" + LINE_SEP;
		} else if (isFamilySH(type)) {
			result = "#" + LINE_SEP;
		} else if (isFamilyBAT(type)) {
			result = "rem" + LINE_SEP;
		}
		return result;
	}

	/**
	 * Get the last line of the licence header formatted for the given type of
	 * file.
	 * 
	 * @param type
	 *            the type of file, see the TYPE_* constants
	 * @return not null
	 */
	protected String getLastLine(final int type) {
		String result = "";
		if (isFamilyC(type)) {
			result = "*/" + LINE_SEP;
		} else if (isFamilySGML(type)) {
			result = "-->" + LINE_SEP;
		} else if (isFamilyAPT(type)) {
			result = "~~" + LINE_SEP;
		} else if (isFamilySH(type)) {
			result = "#" + LINE_SEP;
		} else if (isFamilyBAT(type)) {
			result = "rem" + LINE_SEP;
		}
		return result;
	}

	/**
	 * Get a line of the licence header formatted for the given type of file.
	 * 
	 * @param type
	 *            the type of file, see the TYPE_* constants
	 * @param content
	 *            the content for this line
	 * @return not null
	 */
	protected String getLine(final int type, String content) {
		String result = "";
		if (content != null && content.length() > 0) {
			content = " " + content;
		}
		if (isFamilyC(type)) {
			result = " *" + content + LINE_SEP;
		} else if (isFamilySGML(type)) {
			result = content + LINE_SEP;
		} else if (isFamilyAPT(type)) {
			result = "~~" + content + LINE_SEP;
		} else if (isFamilySH(type)) {
			result = "#" + content + LINE_SEP;
		} else if (isFamilyBAT(type)) {
			result = "rem" + content + LINE_SEP;
		} else if (isFamilyVelocity(type)) {
			result = "##" + content + LINE_SEP;
		}
		return result;
	}

	/**
	 * Checks if is family c.
	 * 
	 * @param type
	 *            the type
	 * @return true, if is family c
	 */
	private static boolean isFamilyC(final int type) {
		return isIn(FAMILY_C, type);
	}

	/**
	 * Checks if is family sgml.
	 * 
	 * @param type
	 *            the type
	 * @return true, if is family sgml
	 */
	private static boolean isFamilySGML(final int type) {
		return isIn(FAMILY_SGML, type);
	}

	/**
	 * Checks if is family sh.
	 * 
	 * @param type
	 *            the type
	 * @return true, if is family sh
	 */
	private static boolean isFamilySH(final int type) {
		return isIn(FAMILY_SH, type);
	}

	/**
	 * Checks if is family apt.
	 * 
	 * @param type
	 *            the type
	 * @return true, if is family apt
	 */
	private static boolean isFamilyAPT(final int type) {
		return isIn(FAMILY_APT, type);
	}

	/**
	 * Checks if is family bat.
	 * 
	 * @param type
	 *            the type
	 * @return true, if is family bat
	 */
	private static boolean isFamilyBAT(final int type) {
		return isIn(FAMILY_BAT, type);
	}

	/**
	 * Checks if is family velocity.
	 * 
	 * @param type
	 *            the type
	 * @return true, if is family velocity
	 */
	private static boolean isFamilyVelocity(final int type) {
		return isIn(FAMILY_VELOCITY, type);
	}

	/**
	 * Expects hash pling.
	 * 
	 * @param type
	 *            the type
	 * @return true, if successful
	 */
	private static boolean expectsHashPling(final int type) {
		return isIn(EXPECTS_HASH_PLING, type);
	}

	/**
	 * Expects at echo.
	 * 
	 * @param type
	 *            the type
	 * @return true, if successful
	 */
	private static boolean expectsAtEcho(final int type) {
		return isIn(EXPECTS_AT_ECHO, type);
	}

	/**
	 * Expects package.
	 * 
	 * @param type
	 *            the type
	 * @return true, if successful
	 */
	private static boolean expectsPackage(final int type) {
		return isIn(EXPECTS_PACKAGE, type);
	}

	/**
	 * Expects xml decl.
	 * 
	 * @param type
	 *            the type
	 * @return true, if successful
	 */
	private static boolean expectsXMLDecl(final int type) {
		return isIn(EXPECTS_XML_DECL, type);
	}

	/**
	 * Expects php pi.
	 * 
	 * @param type
	 *            the type
	 * @return true, if successful
	 */
	private static boolean expectsPhpPI(final int type) {
		return isIn(EXPECTS_PHP_PI, type);
	}

	/**
	 * Expects ms visual studio solution file header.
	 * 
	 * @param type
	 *            the type
	 * @return true, if successful
	 */
	private static boolean expectsMSVisualStudioSolutionFileHeader(
			final int type) {
		return isIn(EXPECTS_MSVSSF_HEADER, type);
	}

	/**
	 * Checks if is in.
	 * 
	 * @param arr
	 *            the arr
	 * @param key
	 *            the key
	 * @return true, if is in
	 */
	private static boolean isIn(final int[] arr, final int key) {
		return Arrays.binarySearch(arr, key) >= 0;
	}

	/**
	 * Pass through read next.
	 * 
	 * @param writer
	 *            the writer
	 * @param line
	 *            the line
	 * @param bufferedReader
	 *            the buffered reader
	 * @return the string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private String passThroughReadNext(final Writer writer, final String line,
			final BufferedReader bufferedReader) throws IOException {
		writer.write(line);
		writer.write(LINE_SEP);
		String readLine = bufferedReader.readLine();
		return readLine == null ? "" : readLine;
	}
}

/**
 * Stripped down version of Commons IO 2.0's BOMInputStream.
 */
class BOMInputStream extends FilterInputStream {
	private int[] firstBytes;
	private int fbLength, fbIndex;
	private static final int[][] BOMS = { new int[] { 0xEF, 0xBB, 0xBF }, // UTF-8
			new int[] { 0xFE, 0xFF }, // UTF-16BE
			new int[] { 0xFF, 0xFE }, // UTF-16LE
	};
	private static final int ZERO = 0;

	BOMInputStream(final InputStream inputStream) {
		super(inputStream);
	}

	@Override
	public int read() throws IOException {
		int value = readFirstBytes();
		return value >= 0 ? value : in.read();
	}

	@Override
	public int read(byte[] buf, int off, int len) throws IOException {
		int firstCount = 0;
		int value = 0;
		while (len > 0 && value >= 0) {
			value = readFirstBytes();
			if (value >= ZERO) {
				buf[off++] = (byte) (value & 0xFF);
				len--;
				firstCount++;
			}
		}
		int secondCount = in.read(buf, off, len);
		return secondCount < 0 ? firstCount > 0 ? firstCount : -1
				: firstCount + secondCount;
	}

	@Override
	public int read(final byte[] buf) throws IOException {
		return read(buf, 0, buf.length);
	}

	private int readFirstBytes() throws IOException {
		getBOM();
		return fbIndex < fbLength ? firstBytes[fbIndex++] : -1;
	}

	private void getBOM() throws IOException {
		if (firstBytes == null) {
			int max = 0;
			for (int[] bom : BOMS) {
				max = Math.max(max, bom.length);
			}
			firstBytes = new int[max];
			for (int i = 0; i < firstBytes.length; i++) {
				firstBytes[i] = in.read();
				fbLength++;
				if (firstBytes[i] < ZERO) {
					break;
				}

				boolean found = find();
				if (found) {
					fbLength = 0;
					break;
				}
			}
		}
	}

	@Override
	public long skip(long value) throws IOException {
		while (value > 0 && readFirstBytes() >= 0) {
			value--;
		}
		return in.skip(value);
	}

	private boolean find() {
		boolean result = false;
		for (int[] bom : BOMS) {
			if (matches(bom)) {
				result = true;
				break;
			}
		}
		return result;
	}

	private boolean matches(final int... bom) {
		boolean result = true;
		if (bom.length == fbLength) {
			for (int i = 0; i < bom.length; i++) {
				if (bom[i] != firstBytes[i]) {
					result = false;
					break;
				}
			}
		} else {
			result = false;
		}
		return result;
	}

}
