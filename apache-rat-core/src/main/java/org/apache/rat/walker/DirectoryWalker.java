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

package org.apache.rat.walker;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.rat.api.Document;
import org.apache.rat.document.impl.FileDocument;
import org.apache.rat.report.IReportable;
import org.apache.rat.report.RatReport;

/**
 * Walks directories.
 */
public class DirectoryWalker extends Walker implements IReportable {

	/** The Constant COMPARATOR. */
	protected static final FileNameComparator COMPARATOR = new FileNameComparator();

	/**
	 * Instantiates a new directory walker.
	 * 
	 * @param file
	 *            the file
	 */
	public DirectoryWalker(final File file) {
		this(file, (FilenameFilter) null);
	}

	/**
	 * Constructs a walker.
	 * 
	 * @param file
	 *            not null
	 * @param filter
	 *            filters input files (optional), or null when no filtering
	 *            should be performed
	 */
	public DirectoryWalker(final File file, final FilenameFilter filter) {
		super(file.getPath(), file, filter);
	}

	/**
	 * Instantiates a new directory walker.
	 * 
	 * @param file
	 *            the file
	 * @param ignoreNameRegex
	 *            the ignore name regex
	 */
	public DirectoryWalker(final File file, final Pattern ignoreNameRegex) {
		super(file.getPath(), file, regexFilter(ignoreNameRegex));
	}

	/**
	 * Checks if is restricted.
	 * 
	 * @return true, if is restricted
	 */
	public boolean isRestricted() {
		return false;
	}

	/**
	 * Process a directory, restricted directories will be ignored.
	 * 
	 * @param report
	 *            The report to process the directory with
	 * @param file
	 *            the directory to process
	 * @throws RatException
	 *             the rat exception
	 */
	private void processDirectory(final RatReport report, final File file)
			throws IOException {
		if (!isRestricted(file)) {
			process(report, file);
		}
	}

	/**
	 * Run a report over all files and directories in this DirectoryWalker,
	 * ignoring any files/directories set to be ignored.
	 * 
	 * @param report
	 *            the defined RatReport to run on this Directory walker.
	 * @throws RatException
	 *             the rat exception
	 */
	public void run(final RatReport report) throws IOException {
		process(report, file);
	}

	/**
	 * Process a directory, ignoring any files/directories set to be ignored.
	 * 
	 * @param report
	 *            the report to use in processing
	 * @param file
	 *            the run the report against
	 * @throws RatException
	 *             the rat exception
	 */
	private void process(final RatReport report, final File file)
			throws IOException {
		final File[] files = file.listFiles();
		Arrays.sort(files, COMPARATOR);
		if (files != null) {
			// breadth first traversal
			processNonDirectories(report, files);
			processDirectories(report, files);
		}
	}

	/**
	 * Process all directories in a set of file objects, ignoring any
	 * directories set to be ignored.
	 * 
	 * @param report
	 *            the report to use in processing
	 * @param files
	 *            the files to process (only directories will be processed)
	 * @throws RatException
	 *             the rat exception
	 */
	private void processDirectories(final RatReport report, final File... files)
			throws IOException {
		for (File file : files) {
			if (!ignored(file) && file.isDirectory()) {
				processDirectory(report, file);
			}
		}
	}

	/**
	 * Process all files in a set of file objects, ignoring any files set to be
	 * ignored.
	 * 
	 * @param report
	 *            the report to use in processing
	 * @param files
	 *            the files to process (only files will be processed)
	 * @throws RatException
	 *             the rat exception
	 */
	private void processNonDirectories(final RatReport report,
			final File... files) throws IOException {
		for (File file : files) {
			if (!ignored(file) && !file.isDirectory()) {
				report(report, file);
			}
		}
	}

	/**
	 * Report on the given file.
	 * 
	 * @param report
	 *            the report to process the file with
	 * @param file
	 *            the file to be reported on
	 * @throws RatException
	 *             the rat exception
	 */
	private void report(final RatReport report, final File file)
			throws IOException {

		Document document = new FileDocument(file);
		report.report(document);

	}
}
