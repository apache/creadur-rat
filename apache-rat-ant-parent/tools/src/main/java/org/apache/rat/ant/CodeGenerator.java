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
package org.apache.rat.ant;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.text.WordUtils;
import org.apache.rat.testhelpers.FileUtils;
import org.apache.rat.ui.AbstractCodeGenerator;
import org.apache.rat.ui.OptionFactory;
import org.apache.rat.utils.CasedString;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

/**
 * A simple tool to convert CLI options into an Ant report base class.
 */
public final class CodeGenerator extends AbstractCodeGenerator<AntOption> {
    /** The xml version of the license text */
    private static final String XML_LICENSE_TEXT = """
 <!--
 Licensed to the Apache Software Foundation (ASF) under one   *
 or more contributor license agreements.  See the NOTICE file *
 distributed with this work for additional information        *
 regarding copyright ownership.  The ASF licenses this file   *
 to you under the Apache License, Version 2.0 (the            *
 "License"); you may not use this file except in compliance   *
 with the License.  You may obtain a copy of the License at   *
                                                              *
   http://www.apache.org/licenses/LICENSE-2.0                 *
                                                              *
 Unless required by applicable law or agreed to in writing,   *
 software distributed under the License is distributed on an  *
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 KIND, either express or implied.  See the License for the    *
 specific language governing permissions and limitations      *
 under the License.                                           *
-->
""";
    /** The Syntax for this command */
    private static final String SYNTAX = String.format("java -cp ... %s [options]", CodeGenerator.class.getName());
    /** The package name for this AbstractMaven file */
    private final CasedString packageName = new CasedString(CasedString.StringCase.DOT, "org.apache.rat.ant");

    /** The template for the method */
    private final Template elementTemplate;
    /** The template for the class */
    private final Template javaTemplate;
    /** The template for attribute types */
    private final Template attributeTemplate;
    /** The template for element classes */
    private final Template elementClassTemplate;
    /** The argument template mapping */
    private final ArgMethodMapping argMethodMapping;

    private CodeGenerator(final String baseDirectory) {
        super(baseDirectory);

        // retrieve the templates
        final String[] nameParts = CasedString.StringCase.DOT.getSegments(CodeGenerator.class.getName());
        final String templatePath = CasedString.StringCase.SLASH.assemble(nameParts);
        elementTemplate = velocityEngine.getTemplate(templatePath + "/ElementMethod.vm");
        javaTemplate = velocityEngine.getTemplate(templatePath + "/AbstractAnt.vm");
        elementClassTemplate = velocityEngine.getTemplate(templatePath + "/AntElementClass.vm");
        attributeTemplate = velocityEngine.getTemplate(templatePath + "/AttributeMethod.vm");
        argMethodMapping = new ArgMethodMapping(templatePath, velocityEngine);
    }


    /**
     * Executable entry point.
     * @param args the arguments for the executable
     * @throws IOException on IO error.
     */
    public static void main(final String[] args) throws IOException, NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException {
        AbstractCodeGenerator.processArgs(SYNTAX, CodeGenerator::new, args);
    }

    private String gatherTypes() {
        return "// types go here";
    }
    /**
     * Executes the code generation.
     * @throws IOException on IO error
     */
    protected void execute() throws IOException {
        final String taskdefLine = "  <taskdef name='%s' classname='%s'/>";
        final VelocityContext context = new VelocityContext();
        context.put("methods", gatherMethods());

        File javaFile = Path.of(baseDirectory).resolve(packageName.toCase(CasedString.StringCase.SLASH))
                .resolve("AntReport.java").toFile();
        FileUtils.mkDir(javaFile.getParentFile());
        try (FileWriter fileWriter = new FileWriter(javaFile, StandardCharsets.UTF_8)) {
            javaTemplate.merge(context, fileWriter);
        }

        File antlibFile = Path.of(baseDirectory).resolve("../resources")
                .resolve(packageName.toCase(CasedString.StringCase.SLASH))
                .resolve("antlib.xml").toFile();
        FileUtils.mkDir(antlibFile.getParentFile());
        try (FileWriter writer = new FileWriter(antlibFile, StandardCharsets.UTF_8)) {
             writer.append("<?xml version='1.0'?>\n")
            .append(XML_LICENSE_TEXT).append("<antlib>\n")
                     .append(String.format(taskdefLine, "report", packageName.toString()))
                     .append("\n");
            for (String subclass : new String[]{"Std", "Expr", "Cntr"}) {
                writer.append(String.format(taskdefLine, subclass.toLowerCase(Locale.ROOT),
                        packageName.toString() + "AbstractAndReport$" + subclass))
                        .append("\n");
            }
            writer.append("</antlib>\n");
        }
    }

    @Override
    public String createMethodName(final AntOption antOption) {
        String name = WordUtils.capitalize(antOption.getName());
        return (antOption.isAttribute() ? "void set" : name + " create") + name;
    }


    /**
     * Gathers all method definitions into a single string.
     * @return the definition of all the methods.
     */
    protected String gatherMethods() {
        final VelocityContext context = new VelocityContext();
        final StringWriter methodWriter = new StringWriter();
        List<AntOption> optionsToProcess = OptionFactory.getOptions(AntOption.FACTORY_CONFIG)
                .filter(AntOption.UNMAPPED_ANTOPTION_FILTER).toList();
        for (AntOption antOption : optionsToProcess) {
            context.put("option", antOption);
            String desc = createDesc(antOption);
            context.put("desc", desc);
            context.put("argDesc", createArgDesc(antOption, desc));
            String methodName = WordUtils.capitalize(antOption.getName());
            context.put("methodName", methodName);
            String argumentType = antOption.isAttribute() ?
                    (antOption.hasArg() ? "String" : "boolean") :
                    (antOption.hasArg() ? antOption.getArgName() : "");
            context.put("argType", argumentType);
            if (antOption.isAttribute()) {
                attributeTemplate.merge(context, methodWriter);
            } else {
                List<String> elementClasses = new ArrayList<>();
                elementClasses.add(getElementClass(antOption, context));
                for (AntOption childOption : antOption.convertedFrom()) {
                    elementClasses.add(getElementClass(childOption, context));
                }
                context.put("elementClass", String.join("\n", elementClasses));
                elementTemplate.merge(context, methodWriter);
            }
        }
        return methodWriter.toString();
    }

    private String getElementClass(final AntOption option, final VelocityContext innerContext) {
        final VelocityContext context = new VelocityContext(innerContext);
        context.put("className", WordUtils.capitalize(option.getName()));
        StringWriter stringWriter = new StringWriter();
        elementClassTemplate.merge(context, stringWriter);
        Set<AntOption> options = option.convertedFrom();
        options.add(option);
        for (AntOption childOption : options) {
            context.put("argName", WordUtils.uncapitalize(childOption.getArgName()));
            context.put("argType", childOption.getArgName());
            final Template innerTemplate = argMethodMapping.getTemplate(childOption, context);
            if (innerTemplate != null) {
                innerTemplate.merge(context, stringWriter);
            }
        }
        stringWriter.append("    }");
        return stringWriter.toString();
    }
}
