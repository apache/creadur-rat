package org.apache.rat.mp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.apache.commons.io.FileUtils;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.factory.DefaultArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.apache.rat.api.Document.Type;
import org.apache.rat.testhelpers.TextUtils;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.util.DirectoryScanner;

import com.google.common.base.Charsets;

/**
 * Test helpers used when verifying mojo interaction in RAT integration tests.
 */
public final class RatTestHelpers {

    /**
     * @param pDir Removes the given directory recursively.
     * @throws IOException in case of errors.
     */
    public static void remove(File pDir) throws IOException {
        if (pDir.isFile()) {
            if (!pDir.delete()) {
                throw new IOException("Unable to delete file: " + pDir);
            }
        } else if (pDir.isDirectory()) {
            FileUtils.deleteDirectory(pDir);
        } else if (pDir.exists()) {
            throw new IOException("Unable to delete unknown object " + pDir);
        }
    }

    /**
     * Copies the given files recursively in order to get all integration test files
     * into a target directory.
     *
     * @param pSource source files.
     * @param pTarget target directory
     * @throws IOException in case of errors.
     */
    public static void copy(File pSource, File pTarget) throws IOException {
        if (pSource.isDirectory()) {
            if (!pTarget.isDirectory() && !pTarget.mkdirs()) {
                throw new IOException("Unable to create directory: " + pTarget);
            }
            final DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(pSource);
            scanner.addDefaultExcludes();
            scanner.setIncludes(new String[] { "*" });
            scanner.scan();
            final String[] dirs = scanner.getIncludedDirectories();

            for (final String dir : dirs) {
                if (!"".equals(dir)) {
                    copy(new File(pSource, dir), new File(pTarget, dir));
                }
            }
            final String[] files = scanner.getIncludedFiles();
            for (String file : files) {
                copy(new File(pSource, file), new File(pTarget, file));
            }
        } else if (pSource.isFile()) {
            try (final FileInputStream fis = new FileInputStream(pSource);
                    final FileOutputStream fos = new FileOutputStream(pTarget);) {
                final byte[] buffer = new byte[8192];
                for (;;) {
                    int res = fis.read(buffer);
                    if (res == -1) {
                        break;
                    }
                    if (res > 0) {
                        fos.write(buffer, 0, res);
                    }
                }
            }
        } else {
            throw new IOException("Unable to copy unknown object " + pSource);
        }
    }

    /**
     * Creates a new instance of {@link Renderer}.
     *
     * @param container current plexus container.
     * @return A configured instance of a Default renderer.
     * @throws Exception Creating the object failed.
     */
    public static Renderer newSiteRenderer(PlexusContainer container) throws Exception {
        return (Renderer) container.lookup(Renderer.ROLE, "default");
    }

    /**
     * Creates a new instance of {@link ArtifactFactory}.
     *
     * @return A configured instance of {@link DefaultArtifactFactory}.
     * @throws Exception Creating the object failed.
     */
    public static ArtifactFactory newArtifactFactory() throws Exception {
        final InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object pProxy, Method pMethod, Object[] pArgs) throws Throwable {
                System.out.println("Invoking method " + pMethod);
                throw new IllegalStateException("Not implemented");
            }
        };
        return (ArtifactFactory) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[] { ArtifactFactory.class }, handler);
    }

    /**
     * Creates an instance of {@link ArtifactRepository}.
     *
     * @param container current plexus container.
     * @return A configured instance of {@link MavenArtifactRepository}.
     * @throws Exception Creating the object failed.
     */
    public static ArtifactRepository newArtifactRepository(PlexusContainer container) throws Exception {
        File m2Dir = new File(System.getProperty("user.home"), ".m2");
        File settingsFile = new File(m2Dir, "settings.xml");
        String localRepo = null;
        if (settingsFile.exists()) {
            Settings settings = new SettingsXpp3Reader().read(new FileReader(settingsFile));
            localRepo = settings.getLocalRepository();
        }
        if (localRepo == null) {
            localRepo = System.getProperty("user.home") + "/.m2/repository";
        }
        ArtifactRepositoryLayout repositoryLayout = (ArtifactRepositoryLayout) container
                .lookup(ArtifactRepositoryLayout.ROLE, "default");
        return new MavenArtifactRepository("local", "file://" + localRepo, repositoryLayout,
                new ArtifactRepositoryPolicy(), new ArtifactRepositoryPolicy());
    }

    public static File makeSourceDirectory(String mvnBaseDir, File pFile, String pDir, boolean pCreateCopy)
            throws IOException {
        if (!pCreateCopy) {
            return pFile;
        }

        final File targetDir = new File(new File(new File(mvnBaseDir), "target/it-source"), pDir);
        remove(targetDir);
        copy(pFile, targetDir);
        return targetDir;
    }

    public static File getSourceDirectory(String mvnBaseDir, String pDir, boolean pCreateCopy, final File baseDir)
            throws IOException {
        return makeSourceDirectory(mvnBaseDir, new File(new File(baseDir, "src/test/resources/unit"), pDir), pDir,
                pCreateCopy);
    }

    /**
     * Reads the created report file and verifies, whether the detected numbers are
     * matching.
     *
     * @param pRatTxtFile The file to read.
     * @param in An array of regex expressions that must be in the file.
     * @param notIn An array of regex expressions that must NOT be in the file.
     * @throws IOException An error occurred while reading the file or the file does
     * not exist at all.
     * @throws IllegalArgumentException In case of mismatches in file numbers passed
     * in as parameter.
     */
    public static void ensureRatReportIsCorrect(File pRatTxtFile, String[] in, String[] notIn) throws IOException {
        List<String> lines = IOUtils.readLines(new FileInputStream(pRatTxtFile), Charsets.UTF_8);
        String document = String.join("\n", lines);
        for (String pattern : in) {
            TextUtils.assertPatternInTarget(pattern, document);
        }

        for (String pattern : notIn) {
            TextUtils.assertPatternInTarget(pattern, document);
        }
    }

}
