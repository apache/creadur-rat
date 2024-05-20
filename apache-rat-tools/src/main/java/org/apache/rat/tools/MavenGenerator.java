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
package org.apache.rat.tools;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.text.WordUtils;
import org.apache.rat.Report;
import org.apache.rat.tools.CasedString.StringCase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A simple tool to convert CLI options  to Maven and Ant format
 */
public class MavenGenerator {

    private MavenGenerator() {}

    private static final String INDENT="    ";

    /**
     * Creates the documentation.  Writes to the output specified by the -o or --out option.  Defaults to System.out.
     * @param args the arguments.  Try --help for help.
     * @throws IOException on error
     */
    public static void main(String[] args) throws IOException {
        String packageName = args[0];
        String className = args[1];
        String destDir = args[2];
        Options options = Report.buildOptions();

        File file = new File(new File(new File(destDir), packageName.replaceAll("\\.", File.separator)),className+".java");
        System.out.println("Creating "+file);
        file.getParentFile().mkdirs();
        try (InputStream template = MavenGenerator.class.getResourceAsStream("/Maven.tpl");
             FileWriter writer = new FileWriter(file)) {
            if (template == null) {
                throw new RuntimeException("Template /Maven.tpl not found");
            }
            LineIterator iter = IOUtils.lineIterator(new InputStreamReader(template));
            while (iter.hasNext()) {
                String line = iter.next();
                switch (line.trim()) {
                    case "${methods}":
                        writeMethods(writer, options);
                        break;
                    case "${package}":
                        writer.append("package ").append(packageName).append(";").append(System.lineSeparator());
                        break;
                    case "${class}":
                        writer.append("public abstract class ").append(className).append(" extends AbstractMojo {").append(System.lineSeparator());
                        break;
                    default:
                        writer.append(line).append(System.lineSeparator());
                        break;
                }
            }
        }
    }

    private static void writeMethods(FileWriter writer, Options options) throws IOException {
        for (Option option : options.getOptions()) {
            if (option.getLongOpt() != null) {
                CasedString name = new CasedString(StringCase.Kebab, option.getLongOpt());
                writeComment(writer, option);
                writer.append(Naming.mavenName(INDENT,option, name)).append(" {").append(System.lineSeparator());
                writeBody(writer, option, name);
                writer.append(INDENT).append("}").append(System.lineSeparator()).append(System.lineSeparator());
            }
        }
    }


    private static void writeComment(FileWriter writer, Option option) throws IOException {
        writer.append(INDENT).append("/* ").append(System.lineSeparator())
                .append(INDENT).append(" * ").append(option.getDescription()).append(System.lineSeparator());
        if (option.isDeprecated()) {
            writer.append(INDENT).append(" * ").append(option.getDeprecated().toString()).append(System.lineSeparator());
        }
        writer.append(INDENT).append(" */").append(System.lineSeparator());
    }

    private static void writeBody(FileWriter writer, Option option, CasedString name) throws IOException {
        String varName = WordUtils.uncapitalize(name.toCase(StringCase.Camel));
        String longArg = Naming.asLongArg(option);
        if (option.hasArg()) {
            writer.append(INDENT).append(INDENT).append("args.add(").append(longArg).append(");").append(System.lineSeparator())
            .append(INDENT).append(INDENT).append("args.add(").append(varName).append(");").append(System.lineSeparator());
        } else {
            writer.append(INDENT).append(INDENT).append("if (").append(varName).append(") {").append(System.lineSeparator())
            .append(INDENT).append(INDENT).append(INDENT).append("args.add(").append(longArg).append(");").append(System.lineSeparator())
                    .append(INDENT).append(INDENT).append("} else {").append(System.lineSeparator())
                    .append(INDENT).append(INDENT).append(INDENT).append("args.remove(").append(longArg).append(");").append(System.lineSeparator())
                    .append(INDENT).append(INDENT).append("}").append(System.lineSeparator());
        }
    }
}
