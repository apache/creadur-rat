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
import java.util.Random;

import junit.framework.TestCase;

public class TestLicenceAppender extends TestCase {
  /** Used to ensure that temporary files have unq */
  private Random random = new Random();
    
  public void testAddLicenceToUnknownFile() throws IOException {
    String filename = "tmp" + random.nextLong() + ".unkownTyoe";
    File file = new File(System.getProperty("java.io.tmpdir") + File.separator + filename);
    FileWriter writer = new FileWriter(file);
    writer.append("Unkown file type\n");
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
    writer.append(firstLine + "\n");
    writer.append("\n");
    writer.append("public class test {\n");
    writer.append("}\n");
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
    writer.append(firstLine + "\n");
    writer.append("\n");
    writer.append("<xml>\n");
    writer.append("</xml>\n");
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
    writer.append("<html>\n");
    writer.append("\n");
    writer.append("</html>\n");
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
    writer.append(".class {\n");
    writer.append(" background-color: red;");
    writer.append("}\n");
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
    writer.append("if (a ==b) {>\n");
    writer.append(" alert(\"how useful!\");");
    writer.append("}\n");
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
    writer.append("A Simple APT file");
    writer.append(" This file contains nothing\n");
    writer.append(" of any importance\n");
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
    writer.append("property = value\n");
    writer.append("fun = true\n");
    writer.append("cool = true\n");
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
}
