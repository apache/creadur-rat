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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Random;

import junit.framework.TestCase;

public class TestLicenceAppender extends TestCase {
  /** Used to ensure that temporary files have unq */
  private Random random = new Random();
    
    private interface FileCreator {
        void createFile(Writer w) throws IOException;
    }

    private interface NewFileReader {
        void readFile(BufferedReader r) throws IOException;
    }

    private static String qualify(String fileName) {
        return new File(new File(System.getProperty("java.io.tmpdir")),
                        fileName)
            .getAbsolutePath();
    }

    private static void createTestFile(String fileName,
                                       FileCreator creator)
        throws IOException {
        FileWriter w = null;
        try {
            creator.createFile(w = new FileWriter(fileName));
        } finally {
            if (w != null) {
                w.close();
            }
        }
    }

    private static void commonTestTemplate(String relativeName,
                                           FileCreator creator,
                                           NewFileReader reader)
        throws IOException {
        String name = qualify(relativeName);
        try {
            createTestFile(name, creator);

            ApacheV2LicenceAppender appender =
                new ApacheV2LicenceAppender();
            appender.append(new File(name));

            BufferedReader r = null;
            try {
                r = new BufferedReader(new FileReader(name + ".new"));
                reader.readFile(r);
            } finally {
                if (r != null) {
                    r.close();
                }
            }
        } finally {
            File f = new File(name);
            if (f.exists() && !f.delete()) {
                f.deleteOnExit();
            }
            f = new File(name + ".new");
            if (f.exists() && !f.delete()) {
                f.deleteOnExit();
            }
        }
    }

    private static NewFileReader checkLines(final String firstLine,
                                            final String secondLine) {
        return new NewFileReader() {
            public void readFile(BufferedReader r) throws IOException {
                String line = r.readLine();
                assertEquals("First line is incorrect",
                             firstLine, line);
                if (secondLine != null) {
                    line = r.readLine();
                    assertEquals("Second line is incorrect",
                                 secondLine, line);
                }
            }
        };
    }

  public void testAddLicenceToUnknownFile() throws IOException {
    String filename = "tmp" + random.nextLong() + ".unknownType";
    File file = new File(System.getProperty("java.io.tmpdir") + File.separator + filename);
    FileWriter writer = new FileWriter(file);
    writer.write("Unkown file type\n");
    writer.close();
    
    ApacheV2LicenceAppender appender = new ApacheV2LicenceAppender();
    appender.append(file);
    
    File newFile = new File(System.getProperty("java.io.tmpdir") + File.separator + filename + ".new");
    assertFalse("No new file should have been written", newFile.exists());
  }
  
  public void testAddLicenceToJava() throws IOException {
    String filename = "tmp.java";
    String firstLine = "package foo;";
    String secondLine = "/*";
    
    File file = new File(System.getProperty("java.io.tmpdir") + File.separator + filename);
    FileWriter writer = new FileWriter(file);
    writer.write(firstLine + "\n");
    writer.write("\n");
    writer.write("public class test {\n");
    writer.write("}\n");
    writer.close();
    
    ApacheV2LicenceAppender appender = new ApacheV2LicenceAppender();
    appender.append(file);
    
    File newFile = new File(System.getProperty("java.io.tmpdir") + File.separator + filename + ".new");
    BufferedReader reader = new BufferedReader(new FileReader(newFile));
    String line = reader.readLine();
    assertEquals("First line is incorrect", firstLine, line);
    line = reader.readLine();
    assertEquals("Second line is incorrect", secondLine, line);
    
    file.delete();
    newFile.delete();
  }
  
  public void testAddLicenceToXML() throws IOException {
    String filename = "tmp.xml";
    String firstLine = "<?xml version='1.0'?>";
    String secondLine = "<!--";
    
    File file = new File(System.getProperty("java.io.tmpdir") + File.separator + filename);
    FileWriter writer = new FileWriter(file);
    writer.write(firstLine + "\n");
    writer.write("\n");
    writer.write("<xml>\n");
    writer.write("</xml>\n");
    writer.close();
    
    ApacheV2LicenceAppender appender = new ApacheV2LicenceAppender();
    appender.append(file);
    
    File newFile = new File(System.getProperty("java.io.tmpdir") + File.separator + filename + ".new");
    
    BufferedReader reader = new BufferedReader(new FileReader(newFile));
    String line = reader.readLine();
    assertEquals("First line is incorrect", firstLine, line);
    line = reader.readLine();
    assertEquals("Second line is incorrect", secondLine, line);
    
    file.delete();
    newFile.delete();
  }
  public void testAddLicenceToHTML() throws IOException {
    String filename = "tmp.html";
    String commentLine = "<!--";
    
    File file = new File(System.getProperty("java.io.tmpdir") + File.separator + filename);
    FileWriter writer = new FileWriter(file);
    writer.write("<html>\n");
    writer.write("\n");
    writer.write("</html>\n");
    writer.close();
    
    ApacheV2LicenceAppender appender = new ApacheV2LicenceAppender();
    appender.append(file);
    
    File newFile = new File(System.getProperty("java.io.tmpdir") + File.separator + filename + ".new");
    
    BufferedReader reader = new BufferedReader(new FileReader(newFile));
    String line = reader.readLine();
    assertEquals("First line is incorrect", commentLine, line);
    
    file.delete();
    newFile.delete();
  }
  
  public void testAddLicenceToCSS() throws IOException {
    String filename = "tmp.css";
    String firstLine = "/*";
    
    File file = new File(System.getProperty("java.io.tmpdir") + File.separator + filename);
    FileWriter writer = new FileWriter(file);
    writer.write(".class {\n");
    writer.write(" background-color: red;");
    writer.write("}\n");
    writer.close();
    
    ApacheV2LicenceAppender appender = new ApacheV2LicenceAppender();
    appender.append(file);
    
    File newFile = new File(System.getProperty("java.io.tmpdir") + File.separator + filename + ".new");
    
    BufferedReader reader = new BufferedReader(new FileReader(newFile));
    String line = reader.readLine();
    assertEquals("First line is incorrect", firstLine, line);
    
    file.delete();
    newFile.delete();
  }
  
  public void testAddLicenceToJavascript() throws IOException {
    String filename = "tmp.js";
    String firstLine = "/*";
    
    File file = new File(System.getProperty("java.io.tmpdir") + File.separator + filename);
    FileWriter writer = new FileWriter(file);
    writer.write("if (a ==b) {>\n");
    writer.write(" alert(\"how useful!\");");
    writer.write("}\n");
    writer.close();
    
    ApacheV2LicenceAppender appender = new ApacheV2LicenceAppender();
    appender.append(file);
    
    File newFile = new File(System.getProperty("java.io.tmpdir") + File.separator + filename + ".new");
    
    BufferedReader reader = new BufferedReader(new FileReader(newFile));
    String line = reader.readLine();
    assertEquals("First line is incorrect", firstLine, line);
    
    file.delete();
    newFile.delete();
  }
  
  public void testAddLicenceToAPT() throws IOException {
    String filename = "tmp.apt";
    String firstLine = "~~";
    
    File file = new File(System.getProperty("java.io.tmpdir") + File.separator + filename);
    FileWriter writer = new FileWriter(file);
    writer.write("A Simple APT file");
    writer.write(" This file contains nothing\n");
    writer.write(" of any importance\n");
    writer.close();
    
    ApacheV2LicenceAppender appender = new ApacheV2LicenceAppender();
    appender.append(file);
    
    File newFile = new File(System.getProperty("java.io.tmpdir") + File.separator + filename + ".new");
    
    BufferedReader reader = new BufferedReader(new FileReader(newFile));
    String line = reader.readLine();
    assertEquals("First line is incorrect", firstLine, line);
    
    file.delete();
    newFile.delete();
  }
  
  public void testAddLicenceToProperties() throws IOException {
    String filename = "tmp.properties";
    String firstLine = "#";
    
    File file = new File(System.getProperty("java.io.tmpdir") + File.separator + filename);
    FileWriter writer = new FileWriter(file);
    writer.write("property = value\n");
    writer.write("fun = true\n");
    writer.write("cool = true\n");
    writer.close();
    
    ApacheV2LicenceAppender appender = new ApacheV2LicenceAppender();
    appender.append(file);
    
    File newFile = new File(System.getProperty("java.io.tmpdir") + File.separator + filename + ".new");
    
    BufferedReader reader = new BufferedReader(new FileReader(newFile));
    String line = reader.readLine();
    assertEquals("First line is incorrect", firstLine, line);
    
    file.delete();
    newFile.delete();
  }

  public void testAddLicenceToScala() throws IOException {
    String filename = "tmp.scala";
    String firstLine = "package foo {";
    String newFirstLine = "/*";
    
    File file = new File(System.getProperty("java.io.tmpdir") + File.separator + filename);
    FileWriter writer = new FileWriter(file);
    writer.write(firstLine + "\n");
    writer.write("\n");
    writer.write("    object X { val x = 1; }\n");
    writer.write("}\n");
    writer.close();
    
    ApacheV2LicenceAppender appender = new ApacheV2LicenceAppender();
    appender.append(file);
    
    File newFile = new File(System.getProperty("java.io.tmpdir") + File.separator + filename + ".new");
    BufferedReader reader = new BufferedReader(new FileReader(newFile));
    String line = reader.readLine();
    assertEquals("First line is incorrect", newFirstLine, line);
    while ((line = reader.readLine()) != null) {
        if (line.length() == 0) {
            line = reader.readLine();
            break;
        }
    }
    assertEquals("Package line is incorrect", firstLine, line);
    
    file.delete();
    newFile.delete();
  }
  
  public void testAddLicenseToRubyWithoutHashBang() throws IOException {
    String filename = "tmp.rb";
    String firstLine = "#";
    
    File file = new File(System.getProperty("java.io.tmpdir") + File.separator + filename);
    FileWriter writer = new FileWriter(file);
    writer.write("class Foo\n");
    writer.write("end\n");
    writer.close();
    
    ApacheV2LicenceAppender appender = new ApacheV2LicenceAppender();
    appender.append(file);
    
    File newFile = new File(System.getProperty("java.io.tmpdir") + File.separator + filename + ".new");
    
    BufferedReader reader = new BufferedReader(new FileReader(newFile));
    String line = reader.readLine();
    assertEquals("First line is incorrect", firstLine, line);
    
    file.delete();
    newFile.delete();
  }

  public void testAddLicenseToRubyWithHashBang() throws IOException {
    String filename = "tmp.rb";
    String firstLine = "#!/usr/bin/env ruby";
    String secondLine = "#";
    
    File file = new File(System.getProperty("java.io.tmpdir") + File.separator + filename);
    FileWriter writer = new FileWriter(file);
    writer.write(firstLine + "\n");
    writer.write("class Foo\n");
    writer.write("end\n");
    writer.close();
    
    ApacheV2LicenceAppender appender = new ApacheV2LicenceAppender();
    appender.append(file);
    
    File newFile = new File(System.getProperty("java.io.tmpdir") + File.separator + filename + ".new");
    
    BufferedReader reader = new BufferedReader(new FileReader(newFile));
    String line = reader.readLine();
    assertEquals("First line is incorrect", firstLine, line);
    line = reader.readLine();
    assertEquals("Second line is incorrect", secondLine, line);
    
    file.delete();
    newFile.delete();
  }
}
