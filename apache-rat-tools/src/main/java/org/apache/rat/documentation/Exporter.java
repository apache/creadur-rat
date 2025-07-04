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
package org.apache.rat.documentation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.rat.api.Document;
import org.apache.rat.api.RatException;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.document.FileDocument;
import org.apache.rat.documentation.velocity.RatTool;
import org.apache.rat.report.RatReport;
import org.apache.rat.walker.DirectoryWalker;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.apache.velocity.tools.generic.EscapeTool;

/**
 * Uses Apache Velocity to write a document containing RAT configuration information..
 *
 * @see <a href="https://velocity.apache.org/">Apache Velocity</a>
 */
public class Exporter {

    @Override
    public String toString() {
        return "Documentation exporter";
    }

    /**
     * Executes the generation of documentation from a configuration definition.
     * Arguments are
     * <ol>
     * <li>Template directory - the top level directory in a tree of  Velocity templates.  The process scans the directory
     * tree looking for files ending in {@code .vm} and processes them.</li>
     * <li>Output directory - The top level directory to writhe the processed files to.  The process removes the {@code .vm}
     * from the input file name and writes the resulting file to an equivalent directory entry in the output directory.</li>
     * </ol>
     *
     * @param args the arguments
     * @throws IOException on IO error.
     * @throws ClassNotFoundException if the configuration is not found.
     * @throws NoSuchMethodException if the method name is not found.
     * @throws InvocationTargetException if the method can not be invoked.
     * @throws InstantiationException if the class can not be instantiated.
     * @throws IllegalAccessException if there are access restrictions on the class.
     * @throws RatException on rat processing error.
     */
    public static void main(final String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException, RatException {
        String templateDir = args[0];
        String outputDir = args[1];

        // crate a DirectoryWalker to walk the template tree.
        File sourceDir = new File(templateDir);
        DocumentName sourceName = DocumentName.builder(sourceDir).build();
        FileFilter fileFilter = new OrFileFilter(new SuffixFileFilter(".vm"), DirectoryFileFilter.INSTANCE);
        Document document = new FileDocument(sourceName, sourceDir, new DocumentNameMatcher(fileFilter));
        DirectoryWalker walker = new DirectoryWalker(document);

        // create a rewriter that writes to the target directory.
        DocumentName targetDir = DocumentName.builder(new File(outputDir)).build();

        // have the walker process clean and then the rewrite.
        walker.run(new Cleaner(targetDir));
        walker.run(new Rewriter(targetDir));
    }

    /**
     * A RatReport implementation that processes the {@code .vm} files in the tempalte tree and writes the
     * results to the output tree.
     */
    private static class Rewriter implements RatReport {
        /**
         * The base directory we are targeting for output
         */
        private final DocumentName targetDir;
        /**
         * The name of the root dir we are reading from
         */
        private final DocumentName rootDir;
        /**
         * The configured velocity engine
         */
        private final VelocityEngine velocityEngine;
        /**
         * The context for Velocity
         */
        private final VelocityContext context;

        /**
         * Create a rewriter.
         *
         * @param targetDir the root of the output directory tree.
         */
        Rewriter(final DocumentName targetDir) {
            this.targetDir = targetDir;
            this.rootDir = DocumentName.builder(new File(".")).build();
            velocityEngine = new VelocityEngine();
            velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "file");
            velocityEngine.setProperty("file.resource.loader.class", FileResourceLoader.class.getName());
            velocityEngine.init();
            context = new VelocityContext();
            context.put("esc", new EscapeTool());
            context.put("rat", new RatTool());
        }

        /**
         * Processes the input document and creates an output document at an equivalent place in the output tree.
         *
         * @param document the input document.
         */
        @Override
        public void report(final Document document) {
            String localized = document.getName().localized();
            DocumentName outputFile = targetDir.resolve(localized.substring(0, localized.length() - 3));
            DocumentName relativeFile = DocumentName.builder(document.getName()).setBaseName(rootDir).build();
            try {
                final File file = outputFile.asFile();
                if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                    throw new IOException("Unable to create directory: " + file.getParentFile());
                }
                final Template template = velocityEngine.getTemplate(relativeFile.localized());

                try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
                    template.merge(context, writer);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * A RatReport implementation that processes the {@code .vm} files in the tempalte tree and writes the
     * results to the output tree.
     */
    private static class Cleaner implements RatReport {
        /**
         * The base directory we are targeting for output
         */
        private final DocumentName targetDir;


        /**
         * Create a rewriter.
         *
         * @param targetDir the root of the output directory tree.
         */
        Cleaner(final DocumentName targetDir) {
            this.targetDir = targetDir;
        }

        /**
         * Processes the input document and creates an output document at an equivalent place in the output tree.
         *
         * @param document the input document.
         */
        @Override
        public void report(final Document document) {
            String localized = document.getName().localized();
            DocumentName outputFile = targetDir.resolve(localized.substring(0, localized.length() - 3));
            try {
                final File file = outputFile.asFile();
                if (file.exists()) {
                    Files.delete(file.toPath());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
