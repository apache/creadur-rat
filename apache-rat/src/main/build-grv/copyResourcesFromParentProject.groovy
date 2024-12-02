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
// so that they become a part of the generated jar file. See RAT-379.

import java.io.FileNotFoundException;
import java.nio.file.attribute.FileTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

final Path sourceDir = Paths.get("${sourceDir}");
final Path targetDir = Paths.get("${targetDir}");
if (!Files.isDirectory(sourceDir)) {
	final String msg = "Source directory not found: " + sourceDir.toAbsolutePath();
	log.error(msg);
	throw new FileNotFoundException(msg);
}
log.debug("copyResourcesFromParent: Using source directory " + sourceDir + ", resolved to " + sourceDir.toAbsolutePath());
log.debug("copyResourcesFromParent: Using target directory " + targetDir + ", resolved to " + targetDir.toAbsolutePath());
Files.createDirectories(targetDir);
for (StringTokenizer st = new StringTokenizer("${filesToCopy}",  ",");  st.hasMoreTokens();  ) {
	final String token = st.nextToken();
	final Path sourceFile = sourceDir.resolve(token);
	if (!Files.isRegularFile(sourceFile)) {
		final String msg = "Source file " + token + " not found in source directory " + sourceDir;
		log.error("copyResourcesFromParent: " + msg);
		log.error("copyResourcesFromParent: A possible reason is, that you did clone only the apache-rat-core subproject from Git.");
	    throw new FileNotFoundException(msg);
	}
	final Path targetFile = targetDir.resolve(token);
	if (Files.isRegularFile(targetFile)) {
		final FileTime sourceTime = Files.getLastModifiedTime(sourceFile);
		final FileTime targetTime = Files.getLastModifiedTime(targetFile);
		if (sourceTime != null  &&  targetTime != null  &&  sourceTime.compareTo(targetTime) >= 0) {
			log.debug("copyResourcesFromParent: Skipping source file "
			          + sourceFile + ", because target file " + targetFile + " appears to be uptodate.");
			continue;
	    }
	}
	log.debug("copyResourcesFromParent: Copying source file " + sourceFile
	          + " to target file " + targetFile);
	Files.copy(sourceFile, targetFile);
}
