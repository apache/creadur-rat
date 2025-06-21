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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;


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
 * Uses Apache Velocity to write a configuration document.
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
     * <li>Template - The file name of the Velocity template</li>
     * <li>Output - THe file name for the generated document</li>
     * <li>Providers - zero or more providers to add to the context</li>
     * </ol>
     *
     * @param args
     *            the arguments
     * @throws IOException
     *             on IO error.
     * @throws ClassNotFoundException
     *             if the configuration is not found.
     * @throws NoSuchMethodException
     *             if the method name is not found.
     * @throws InvocationTargetException
     *             if the method can not be invoked.
     * @throws InstantiationException
     *             if the class can not be instantiated.
     * @throws IllegalAccessException
     *             if there are access restrictions on the class.
     */
    public static void main(final String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException, RatException {
        final String templateDir = args[0];
        final String outputDir = args[1];
        File sourceDir = new File(templateDir);
        DocumentName sourceName = DocumentName.builder(sourceDir).build();
        Document document = new FileDocument(sourceName, sourceDir, DocumentNameMatcher.MATCHES_ALL);
        DirectoryWalker walker = new DirectoryWalker(document);
        DocumentName targetDir = DocumentName.builder(new File(outputDir)).build();
        Rewriter rewriter = new Rewriter(targetDir);
        walker.run(rewriter);
    }

    private static class Rewriter implements RatReport {
       private DocumentName targetDir;
       private final DocumentName rootDir;
       private final VelocityEngine velocityEngine;
       private final VelocityContext context;
        public Rewriter(DocumentName targetDir) {
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

        @Override
        public void report(Document document) {
            DocumentName outputFile = targetDir.resolve(document.getName().localized());
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
                //Exporter.execute(relativeFile, outputFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
