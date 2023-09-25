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
package org.apache.rat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

import org.apache.rat.ReportConfiguration.LicenseFilter;
import org.apache.rat.config.AddLicenseHeaders;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.report.IReportable;
import org.junit.Before;
import org.junit.Test;

public class ReportConfigurationTest {

    private ReportConfiguration underTest;

    @Before
    public void setup() {
        underTest = new ReportConfiguration();
    }

    @Test
    public void testAddAndRemoveApproveLicenseCategories() {
        List<String> expected = new ArrayList<>();
        assertTrue(underTest.getApprovedLicenseCategories().isEmpty());

        underTest.addApprovedLicenseCategory(
                ILicenseFamily.builder().setLicenseFamilyCategory("TheCat").setLicenseFamilyName("TheName").build());
        expected.add("TheCa");
        SortedSet<String> result = underTest.getApprovedLicenseCategories();
        assertEquals(expected.size(), result.size());
        assertTrue(result.containsAll(expected));

        underTest.addApprovedLicenseCategory("ACat");
        expected.add("ACat ");
        result = underTest.getApprovedLicenseCategories();
        assertEquals(expected.size(), result.size());
        assertTrue(result.containsAll(expected));

        String[] cats = { "Spot ", "Felix" };
        underTest.addApprovedLicenseCategories(Arrays.asList(cats));
        expected.addAll(Arrays.asList(cats));
        result = underTest.getApprovedLicenseCategories();
        assertEquals(expected.size(), result.size());
        assertTrue(result.containsAll(expected));

        underTest.removeApprovedLicenseCategory("Spot ");
        expected.remove("Spot ");
        result = underTest.getApprovedLicenseCategories();
        assertEquals(expected.size(), result.size());
        assertTrue(result.containsAll(expected));

        cats[0] = "TheCa";
        underTest.removeApprovedLicenseCategories(Arrays.asList(cats));
        expected.removeAll(Arrays.asList(cats));
        result = underTest.getApprovedLicenseCategories();
        assertEquals(expected.size(), result.size());
        assertTrue(result.containsAll(expected));
    }

    @Test
    public void testRemoveBeforeAddApproveLicenseCategories() {
        assertTrue(underTest.getApprovedLicenseCategories().isEmpty());

        underTest.removeApprovedLicenseCategory("TheCat");
        assertTrue(underTest.getApprovedLicenseCategories().isEmpty());

        underTest.addApprovedLicenseCategory("TheCat");
        assertTrue(underTest.getApprovedLicenseCategories().isEmpty());
    }

    private ILicense mockLicense(String category, String name) {
        ILicenseFamily family = ILicenseFamily.builder().setLicenseFamilyCategory(category).setLicenseFamilyName(name)
                .build();
        ILicense result = mock(ILicense.class);
        when(result.getLicenseFamily()).thenReturn(family);
        return result;
    }

    @Test
    public void testAddLicense() {

        List<ILicense> expected = new ArrayList<>();
        assertTrue(underTest.getLicenses(LicenseFilter.all).isEmpty());

        ILicense lic1 = mockLicense("TheCat", "TheName");
        expected.add(lic1);
        underTest.addLicense(lic1);
        SortedSet<ILicense> result = underTest.getLicenses(LicenseFilter.all);
        assertEquals(expected.size(), result.size());
        assertTrue(result.containsAll(expected));

        ILicense[] lics = { mockLicense("Spot", "Data's cat"), mockLicense("Felix", "Cartoon cat") };
        expected.addAll(Arrays.asList(lics));
        underTest.addLicenses(Arrays.asList(lics));
        result = underTest.getLicenses(LicenseFilter.all);
        assertEquals(expected.size(), result.size());
        assertTrue(result.containsAll(expected));
    }

    @Test
    public void copyrightMessageTest() {
        assertNull(underTest.getCopyrightMessage());
        underTest.setCopyrightMessage("This is the message");
        assertEquals("This is the message", underTest.getCopyrightMessage());
    }

    @Test
    public void inputFileFilterTest() {
        FilenameFilter filter = mock(FilenameFilter.class);
        assertNull(underTest.getInputFileFilter());
        underTest.setInputFileFilter(filter);
        assertEquals(filter, underTest.getInputFileFilter());
    }

    @Test
    public void licenseFamiliesTest() {
        assertTrue(underTest.getLicenseFamilies(LicenseFilter.all).isEmpty());
        assertTrue(underTest.getLicenseFamilies(LicenseFilter.approved).isEmpty());
        assertTrue(underTest.getLicenseFamilies(LicenseFilter.none).isEmpty());

        ILicense[] lics = { mockLicense("TheCat", "TheName"), mockLicense("Spot", "Data's cat"),
                mockLicense("Felix", "Cartoon cat") };
        underTest.addLicenses(Arrays.asList(lics));

        assertEquals(lics.length, underTest.getLicenseFamilies(LicenseFilter.all).size());
        assertTrue(underTest.getLicenseFamilies(LicenseFilter.approved).isEmpty());
        assertTrue(underTest.getLicenseFamilies(LicenseFilter.none).isEmpty());

        underTest.addApprovedLicenseCategory(lics[1].getLicenseFamily());
        assertEquals(lics.length, underTest.getLicenseFamilies(LicenseFilter.all).size());
        SortedSet<ILicenseFamily> result = underTest.getLicenseFamilies(LicenseFilter.approved);
        assertEquals(1, result.size());
        assertEquals(lics[1].getLicenseFamily(), result.first());
        assertTrue(underTest.getLicenseFamilies(LicenseFilter.none).isEmpty());
    }

    @Test
    public void testLicenseFilter() {
        assertEquals(LicenseFilter.approved, underTest.getLicenseFilter());
        underTest.setLicenseFilter(LicenseFilter.all);
        assertEquals(LicenseFilter.all, underTest.getLicenseFilter());
        underTest.setLicenseFilter(LicenseFilter.none);
        assertEquals(LicenseFilter.none, underTest.getLicenseFilter());
        underTest.setLicenseFilter(LicenseFilter.approved);
        assertEquals(LicenseFilter.approved, underTest.getLicenseFilter());
    }

    @Test
    public void licensesTest() {
        assertTrue(underTest.getLicenses(LicenseFilter.all).isEmpty());
        assertTrue(underTest.getLicenses(LicenseFilter.approved).isEmpty());
        assertTrue(underTest.getLicenses(LicenseFilter.none).isEmpty());

        ILicense[] lics = { mockLicense("TheCat", "TheName"), mockLicense("Spot", "Data's cat"),
                mockLicense("Felix", "Cartoon cat") };
        underTest.addLicenses(Arrays.asList(lics));

        assertEquals(lics.length, underTest.getLicenses(LicenseFilter.all).size());
        assertTrue(underTest.getLicenses(LicenseFilter.approved).isEmpty());
        assertTrue(underTest.getLicenses(LicenseFilter.none).isEmpty());

        underTest.addApprovedLicenseCategory(lics[1].getLicenseFamily());
        assertEquals(lics.length, underTest.getLicenses(LicenseFilter.all).size());
        SortedSet<ILicense> result = underTest.getLicenses(LicenseFilter.approved);
        assertEquals(1, result.size());
        assertEquals(lics[1], result.first());
        assertTrue(underTest.getLicenseFamilies(LicenseFilter.none).isEmpty());
    }

    @Test
    public void outputTest() {
        // defaults to system.out
        assertEquals(System.out, underTest.getOutput());
        assertNotNull(underTest.getWriter());

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        underTest.setOut(stream);
        assertEquals(stream, underTest.getOutput());
        PrintWriter writer = underTest.getWriter();
        assertNotNull(writer);
        writer.write('a');
        writer.flush();
        assertEquals('a', stream.toByteArray()[0]);
    }

    @Test
    public void reportableTest() {
        assertNull(underTest.getReportable());
        IReportable reportable = mock(IReportable.class);
        underTest.setReportable(reportable);
        assertEquals(reportable, underTest.getReportable());
        underTest.setReportable(null);
        assertNull(underTest.getReportable());
    }

    @Test
    public void stylesheetTest() throws IOException, URISyntaxException {
        URL url = this.getClass().getResource("ReportConfigurationTestFile");

        assertNull(underTest.getStyleSheet());
        InputStream stream = mock(InputStream.class);
        underTest.setStyleSheet(stream);
        assertEquals(stream, underTest.getStyleSheet());
        underTest.setStyleSheet((InputStream) null);
        assertNull(underTest.getStyleSheet());
        File file = mock(File.class);

        when(file.toURI()).thenReturn(url.toURI());
        underTest.setStyleSheet(file);
        BufferedReader d = new BufferedReader(new InputStreamReader(underTest.getStyleSheet()));
        assertEquals("/*", d.readLine());
        assertEquals(" * Licensed to the Apache Software Foundation (ASF) under one   *", d.readLine());
    }

    @Test
    public void testFlags() {
        assertFalse(underTest.isAddingLicenses());
        assertFalse(underTest.isAddingLicensesForced());
        assertTrue(underTest.isStyleReport());

        underTest.setAddLicenseHeaders(AddLicenseHeaders.TRUE);
        assertTrue(underTest.isAddingLicenses());
        assertFalse(underTest.isAddingLicensesForced());
        assertTrue(underTest.isStyleReport());

        underTest.setAddLicenseHeaders(AddLicenseHeaders.FALSE);
        assertFalse(underTest.isAddingLicenses());
        assertFalse(underTest.isAddingLicensesForced());
        assertTrue(underTest.isStyleReport());

        underTest.setAddLicenseHeaders(AddLicenseHeaders.FORCED);
        assertTrue(underTest.isAddingLicenses());
        assertTrue(underTest.isAddingLicensesForced());
        assertTrue(underTest.isStyleReport());

        underTest.setAddLicenseHeaders(AddLicenseHeaders.FALSE);
        underTest.setStyleReport(false);
        assertFalse(underTest.isAddingLicenses());
        assertFalse(underTest.isAddingLicensesForced());
        assertFalse(underTest.isStyleReport());

        underTest.setStyleReport(true);
        assertFalse(underTest.isAddingLicenses());
        assertFalse(underTest.isAddingLicensesForced());
        assertTrue(underTest.isStyleReport());
    }

    @Test
    public void testValidate() {

//        public void validate(Consumer<String> logger) {
//            if (reportable == null) {
//                throw new ConfigurationException("Reportable may not be null");
//            }
//            if (licenses.size() == 0) {
//                throw new ConfigurationException("You must specify at least one license");
//            }
//            if (styleSheet != null && !isStyleReport()) {
//                logger.accept("Ignoring stylesheet because styling is not selected");
//            }
//            if (styleSheet == null && isStyleReport()) {
//                throw new ConfigurationException("Stylesheet must be specified if report styling is selected");
//            }
//        }

        final StringBuilder sb = new StringBuilder();
        try {
            underTest.validate(s -> sb.append(s));
            fail("should have thrown ConfigurationException");
        } catch (ConfigurationException e) {
            assertEquals("Reportable may not be null", e.getMessage());
            assertEquals(0, sb.length());
        }

        underTest.setReportable(mock(IReportable.class));
        try {
            underTest.validate(s -> sb.append(s));
            fail("should have thrown ConfigurationException");
        } catch (ConfigurationException e) {
            assertEquals("You must specify at least one license", e.getMessage());
            assertEquals(0, sb.length());
        }

        underTest.addLicense(mockLicense("valid", "Validation testing license"));
        try {
            underTest.validate(s -> sb.append(s));
            fail("should have thrown ConfigurationException");
        } catch (ConfigurationException e) {
            assertEquals("Stylesheet must be specified if report styling is selected", e.getMessage());
            assertEquals(0, sb.length());
        }

        underTest.setStyleSheet(mock(InputStream.class));
        underTest.setStyleReport(false);
        underTest.validate(s -> sb.append(s));
        assertEquals("Ignoring stylesheet because styling is not selected", sb.toString());

        final StringBuilder sb2 = new StringBuilder();
        underTest.setStyleReport(true);
        underTest.validate(s -> sb2.append(s));
        assertEquals(0, sb2.length());
    }
}
