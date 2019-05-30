package org.apache.rat.mp;

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
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.DefaultArtifactResolver;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.util.DirectoryScanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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
            final File[] files = pDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    remove(file);
                }
            }
            if (!pDir.delete()) {
                throw new IOException("Unable to delete directory: " + pDir);
            }
        } else if (pDir.exists()) {
            throw new IOException("Unable to delete unknown object " + pDir);
        }
    }

    /**
     * Copies the given files recursively in order to get all integration test
     * files into a target directory.
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
            scanner.setIncludes(new String[]{"*"});
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
            final FileInputStream fis = new FileInputStream(pSource);
            final FileOutputStream fos = new FileOutputStream(pTarget);
            final byte[] buffer = new byte[8192];
            for (; ; ) {
                int res = fis.read(buffer);
                if (res == -1) {
                    break;
                }
                if (res > 0) {
                    fos.write(buffer, 0, res);
                }
            }
            fos.close();
            fis.close();
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
    public static Renderer newSiteRenderer(PlexusContainer container)
            throws Exception {
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
            public Object invoke(Object pProxy, Method pMethod, Object[] pArgs)
                    throws Throwable {
                System.out.println("Invoking method " + pMethod);
                throw new IllegalStateException("Not implemented");
            }
        };
        return (ArtifactFactory) Proxy.newProxyInstance(Thread.currentThread()
                        .getContextClassLoader(),
                new Class[]{ArtifactFactory.class}, handler);
    }

    /**
     * Creates a new instance of {@link ArtifactResolver}.
     *
     * @return A configured instance of {@link DefaultArtifactResolver}.
     * @throws Exception Creating the object failed.
     */
    public static ArtifactResolver newArtifactResolver() throws Exception {
        final InvocationHandler handler = new InvocationHandler() {
            public Object invoke(Object pProxy, Method pMethod, Object[] pArgs)
                    throws Throwable {
                System.out.println("Invoking method " + pMethod);
                throw new IllegalStateException("Not implemented");
            }
        };
        return (ArtifactResolver) Proxy.newProxyInstance(Thread.currentThread()
                        .getContextClassLoader(),
                new Class[]{ArtifactResolver.class}, handler);
    }

    /**
     * Creates an instance of {@link ArtifactRepository}.
     *
     * @param container current plexus container.
     * @return A configured instance of {@link DefaultArtifactRepository}.
     * @throws Exception Creating the object failed.
     */
    public static ArtifactRepository newArtifactRepository(
            PlexusContainer container) throws Exception {
        File m2Dir = new File(System.getProperty("user.home"), ".m2");
        File settingsFile = new File(m2Dir, "settings.xml");
        String localRepo = null;
        if (settingsFile.exists()) {
            Settings settings = new SettingsXpp3Reader().read(new FileReader(
                    settingsFile));
            localRepo = settings.getLocalRepository();
        }
        if (localRepo == null) {
            localRepo = System.getProperty("user.home") + "/.m2/repository";
        }
        ArtifactRepositoryLayout repositoryLayout = (ArtifactRepositoryLayout) container
                .lookup(ArtifactRepositoryLayout.ROLE, "default");
        return new DefaultArtifactRepository("local", "file://" + localRepo,
                repositoryLayout);
    }

    public static File makeSourceDirectory(String mvnBaseDir, File pFile,
                                           String pDir, boolean pCreateCopy) throws IOException {
        if (!pCreateCopy) {
            return pFile;
        }

        final File targetDir = new File(new File(new File(mvnBaseDir),
                "target/it-source"), pDir);
        remove(targetDir);
        copy(pFile, targetDir);
        return targetDir;
    }

    public static File getSourceDirectory(String mvnBaseDir, String pDir,
                                          boolean pCreateCopy, final File baseDir) throws IOException {
        return makeSourceDirectory(mvnBaseDir, new File(new File(baseDir,
                "src/test/resources/unit"), pDir), pDir, pCreateCopy);
    }

    /**
     * Reads the created report file and verifies, whether the detected numbers
     * are matching.
     *
     * @param pRatTxtFile        The file to read.
     * @param pNumALFiles        The number of files with AL.
     * @param pNumNoLicenseFiles The number of files without license.
     * @throws IOException              An error occurred while reading the file or the file does not
     *                                  exist at all.
     * @throws IllegalArgumentException In case of mismatches in file numbers passed in as parameter.
     */
    public static void ensureRatReportIsCorrect(File pRatTxtFile,
                                                int pNumALFiles, int pNumNoLicenseFiles) throws IOException {
        if (!pRatTxtFile.exists()) {
            throw new FileNotFoundException("Could not find " + pRatTxtFile);
        }
        BufferedReader reader = null;
        try {

            reader = new BufferedReader(new FileReader(pRatTxtFile));
            Integer numALFiles = null;
            Integer numNoLicenseFiles = null;
            for (; ; ) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                int offset = line.indexOf("Apache Licensed: ");
                if (offset >= 0) {
                    numALFiles = Integer.valueOf(line.substring(
                            offset + "Apache Licensed: ".length()).trim());
                }
                offset = line.indexOf("Unknown Licenses");
                if (offset >= 0) {
                    numNoLicenseFiles = Integer.valueOf(line.substring(0, offset)
                            .trim());
                }
            }
            reader.close();

            if (!Integer.valueOf(pNumALFiles).equals(numALFiles)) {
                throw new IllegalArgumentException(
                        "Amount of licensed files does not match. Expected "
                                + pNumALFiles + ", got " + numALFiles);
            }

            if (!Integer.valueOf(pNumNoLicenseFiles).equals(numNoLicenseFiles)) {
                throw new IllegalArgumentException(
                        "Amount of licensed files does not match. Expected "
                                + pNumALFiles + ", got " + numALFiles);
            }
        } finally {
            IOUtils.closeQuietly(reader);
        }

    }

}
