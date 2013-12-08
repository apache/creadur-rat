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
package org.apache.rat.analysis;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.rat.api.Document;
import org.apache.rat.api.RatException;
import org.apache.rat.document.impl.MonolithicFileDocument;
import org.apache.rat.report.claim.impl.xml.SimpleXmlClaimReporter;
import org.apache.rat.report.xml.writer.impl.base.XmlWriter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * The Class AnalyserFactoryTest.
 */
public class AnalyserFactoryTest {

    /** The out. */
    private StringWriter out;

    /** The reporter. */
    private SimpleXmlClaimReporter reporter;

    /** The matcher stub. */
    private IHeaderMatcher matcherStub;

    /**
     * Sets the up.
     * 
     * @throws Exception
     *             the exception
     */
    @Before
    public void setUp() throws Exception {
        this.out = new StringWriter();
        final XmlWriter writer = new XmlWriter(this.out);
        this.reporter = new SimpleXmlClaimReporter(writer);
        this.matcherStub = new IHeaderMatcher() {
            public boolean match(final Document subject, final String line) {
                return false;
            }

            public void reset() {
            }
        };
    }

    /**
     * Standard type analyser.
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws RatException
     *             the rat exception
     */
    @Test
    public void testStandardTypeAnalyser() throws IOException, RatException {
        final MonolithicFileDocument document =
                new MonolithicFileDocument(new File(
                        "src/test/resources/elements/Text.txt"));
        final DefaultAnalyser analyser = new DefaultAnalyser(this.matcherStub);
        analyser.analyse(document);
        this.reporter.report(document);
        assertEquals(
                "Open standard element",
                "<resource name='src/test/resources/elements/Text.txt'><header-sample>/*\n"
                        + " * Licensed to the Apache Software Foundation (ASF) under one\n"
                        + " * or more contributor license agreements.  See the NOTICE file\n"
                        + " * distributed with this work for additional information\n"
                        + " * regarding copyright ownership.  The ASF licenses this file\n"
                        + " * to you under the Apache License, Version 2.0 (the \"License\");\n"
                        + " * you may not use this file except in compliance with the License.\n"
                        + " * You may obtain a copy of the License at\n"
                        + " *\n"
                        + " *    http://www.apache.org/licenses/LICENSE-2.0\n"
                        + " *\n"
                        + " * Unless required by applicable law or agreed to in writing,\n"
                        + " * software distributed under the License is distributed on an\n"
                        + " * \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY\n"
                        + " * KIND, either express or implied.  See the License for the\n"
                        + " * specific language governing permissions and limitations\n"
                        + " * under the License.    \n"
                        + " */\n"
                        + "\n"
                        + "            \n"
                        + "</header-sample><header-type name='?????'/><license-family name='?????'/><type name='standard'/>",
                this.out.toString());
    }

    /**
     * Note type analyser.
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws RatException
     *             the rat exception
     */
    @Test
    public void testNoteTypeAnalyser() throws IOException, RatException {
        final MonolithicFileDocument document =
                new MonolithicFileDocument(
                        new File("src/test/elements/LICENSE"));
        final DefaultAnalyser analyser = new DefaultAnalyser(this.matcherStub);
        analyser.analyse(document);
        this.reporter.report(document);
        assertEquals(
                "Open note element",
                "<resource name='src/test/elements/LICENSE'><type name='notice'/>",
                this.out.toString());
    }

    /**
     * Binary type analyser.
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws RatException
     *             the rat exception
     */
    @Test
    public void testBinaryTypeAnalyser() throws IOException, RatException {
        final MonolithicFileDocument document =
                new MonolithicFileDocument(new File(
                        "src/test/elements/Image.png"));
        final DefaultAnalyser analyser = new DefaultAnalyser(this.matcherStub);
        analyser.analyse(document);
        this.reporter.report(document);
        assertEquals(
                "Open binary element",
                "<resource name='src/test/elements/Image.png'><type name='binary'/>",
                this.out.toString());
    }

    /**
     * Archive type analyser.
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws RatException
     *             the rat exception
     */
    @Test
    public void testArchiveTypeAnalyser() throws IOException, RatException {
        final MonolithicFileDocument document =
                new MonolithicFileDocument(new File(
                        "src/test/elements/Dummy.jar"));
        final DefaultAnalyser analyser = new DefaultAnalyser(this.matcherStub);
        analyser.analyse(document);
        this.reporter.report(document);
        assertEquals(
                "Open archive element",
                "<resource name='src/test/elements/Dummy.jar'><type name='archive'/>",
                this.out.toString());
    }

    /**
     * Test abstract monolithicis composite.
     */
    @Test
    public void testAbstractMonolithicisComposite() {
        final MonolithicFileDocument document =
                new MonolithicFileDocument(new File(
                        "src/test/elements/Dummy.jar"));
        Assert.assertFalse(document.isComposite());
    }

    /**
     * Test standard type analyser exception.
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws RatException
     *             the rat exception
     */
    @Test(expected = IOException.class)
    public void testStandardTypeAnalyserFileException() throws IOException,
            RatException {
        final MonolithicFileDocument document =
                new MonolithicFileDocument(new File(
                        "src/test/resources/elements/Text.txtt"));
        final DefaultAnalyser analyser = new DefaultAnalyser(this.matcherStub);
        analyser.analyse(document);
    }

    /**
     * Test note guesser file extensions.
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws RatException
     *             the rat exception
     */
    @Test
    public void testNoteGuesserFileExtensions() throws IOException,
            RatException {
        final MonolithicFileDocument document =
                new MonolithicFileDocument(new File(
                        "src/test/resources/elements/test.LICENSE"));
        final DefaultAnalyser analyser = new DefaultAnalyser(this.matcherStub);
        analyser.analyse(document);
        Assert.assertEquals(document.getMetaData().getData().size(), 1);
    }
}
