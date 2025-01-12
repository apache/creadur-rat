/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Copy a set of resource files from the parent project to target/classes/META-INF,
// so that they become a part of the generated jar file. See RAT-379 for details.

import java.nio.file.attribute.FileTime
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

final Path sourceDir = Paths.get("${sourceDir}")
final Path targetDir = Paths.get("${targetDir}")

if (!Files.isDirectory(sourceDir)) {
	final String msg = "Source directory not found: " + sourceDir.toAbsolutePath()
	System.err.println(msg)
	throw new FileNotFoundException(msg)
}

// System.out.println("copyResourcesFromParent: Using source directory " + sourceDir + ", resolved to " + sourceDir.toAbsolutePath())
// System.out.println("copyResourcesFromParent: Using target directory " + targetDir + ", resolved to " + targetDir.toAbsolutePath())
Files.createDirectories(targetDir)

for (StringTokenizer st = new StringTokenizer("${filesToCopy}",  ",");  st.hasMoreTokens(); ) {
	final String token = st.nextToken()
	final Path sourceFile = sourceDir.resolve(token)
	if (!Files.isRegularFile(sourceFile)) {
		final String msg = "Source file " + token + " not found in source directory " + sourceDir
		System.err.println("copyResourcesFromParent: " + msg)
		System.err.println("copyResourcesFromParent: A possible reason is, that you did clone only the apache-rat-core subproject from Git.")
	    throw new FileNotFoundException(msg)
	}
	final Path targetFile = targetDir.resolve(token)
	final boolean replacing = Files.isRegularFile(targetFile)
	if (replacing) {
		final FileTime sourceTime = Files.getLastModifiedTime(sourceFile)
		final FileTime targetTime = Files.getLastModifiedTime(targetFile)
		if (sourceTime != null  &&  targetTime != null  && sourceTime >= targetTime) {
			System.out.println("Skipping " + sourceFile + ", as target " + targetFile + " appears to be up-to-date already.")
			continue
	    }
	}
	System.out.println("Copying " + sourceFile
	          + " to " + targetFile)
	if (replacing) {
		Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING)
	} else {
		Files.copy(sourceFile, targetFile)
	}
}
