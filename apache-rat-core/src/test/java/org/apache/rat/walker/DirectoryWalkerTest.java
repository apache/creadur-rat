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
package org.apache.rat.walker;

import org.apache.rat.api.Document;
import org.apache.rat.api.RatException;
import org.apache.rat.report.RatReport;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DirectoryWalkerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void walk() throws IOException, RatException {
        File toWalk = folder.newFolder("test");
        File regular = new File(toWalk, "regular");
        regular.mkdir();
        File regularFile = new File(regular, "test");
        FileWriter writer = new FileWriter(regularFile);
        writer.write("test");
        writer.flush();
        writer.close();

        File hidden = new File(toWalk, ".hidden");
        hidden.mkdir();
        File hiddenFile = new File(hidden, "test");
        writer = new FileWriter(hiddenFile);
        writer.write("test");
        writer.flush();
        writer.close();

        DirectoryWalker walker = new DirectoryWalker(toWalk, false);
        List<String> scanned = new ArrayList<>();
        walker.run(new TestRatReport(scanned));

        Assert.assertEquals(1, scanned.size());

        walker = new DirectoryWalker(toWalk, true);
        scanned = new ArrayList<>();
        walker.run(new TestRatReport(scanned));

        Assert.assertEquals(2, scanned.size());
    }

    class TestRatReport implements RatReport {

        private List<String> scanned;

        public TestRatReport(List<String> scanned) {
            this.scanned = scanned;
        }

        @Override
        public void startReport() throws RatException {
            System.out.println("Start");
        }

        @Override
        public void report(Document document) throws RatException {
            scanned.add(document.getName());
        }

        @Override
        public void endReport() throws RatException {
            System.out.println("End");
        }
    }

}
