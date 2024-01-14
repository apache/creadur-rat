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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.function.Function;

import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.function.IOSupplier;
import org.apache.rat.ReportConfiguration.NoCloseOutputStream;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.config.AddLicenseHeaders;
import org.apache.rat.configuration.ConfigurationReaderTest;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.report.IReportable;
import org.apache.rat.testhelpers.TestingLicense;
import org.apache.rat.utils.Log;
import org.apache.rat.utils.Log.Level;
import org.apache.rat.utils.ReportingSet.Options;
import org.apache.rat.walker.NameBasedHiddenFileFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ReportConfigurationTest {

    private ReportConfiguration underTest;
    private LoggingCapture log;

    @BeforeEach
    public void setup() {
        log = new LoggingCapture();
        underTest = new ReportConfiguration(log);
    }

    @Test
    public void testAddAndRemoveApproveLicenseCategories() {
        List<String> expected = new ArrayList<>();
        underTest.addLicense( new TestingLicense("Unapproved"));

        assertThat(underTest.getApprovedLicenseCategories()).isEmpty();

        TestingLicense license = new TestingLicense("TheCat");
        underTest.addLicense(license);
        underTest.addApprovedLicenseCategory(license.getFamily());
        expected.add("TheCa");
        SortedSet<String> result = underTest.getApprovedLicenseCategories();
        assertThat(expected).hasSize(result.size()).containsAll(result);
        SortedSet<ILicenseFamily> families = underTest.getLicenseFamilies(LicenseFilter.approved);
        assertThat(expected).hasSize(families.size());
        SortedSet<ILicense> licenses = underTest.getLicenses(LicenseFilter.approved);
        assertThat(expected).hasSize(licenses.size());

        underTest.addLicense(new TestingLicense("ACat"));
        underTest.addApprovedLicenseCategory("ACat");
        expected.add("ACat ");
        result = underTest.getApprovedLicenseCategories();
        assertThat(expected).hasSize(result.size()).containsAll(result);
        families = underTest.getLicenseFamilies(LicenseFilter.approved);
        assertThat(expected).hasSize(families.size());
        licenses = underTest.getLicenses(LicenseFilter.approved);
        assertThat(expected).hasSize(licenses.size());

        String[] cats = { "Spot ", "Felix" };
        underTest.addLicense(new TestingLicense("Spot"));
        underTest.addLicense(new TestingLicense("Felix"));
        underTest.addApprovedLicenseCategories(Arrays.asList(cats));
        expected.addAll(Arrays.asList(cats));
        result = underTest.getApprovedLicenseCategories();
        assertThat(expected).hasSize(result.size()).containsAll(result);
        families = underTest.getLicenseFamilies(LicenseFilter.approved);
        assertThat(expected).hasSize(families.size());
        licenses = underTest.getLicenses(LicenseFilter.approved);
        assertThat(expected).hasSize(licenses.size());
        
        underTest.removeApprovedLicenseCategory("Spot ");
        expected.remove("Spot ");
        result = underTest.getApprovedLicenseCategories();
        assertThat(expected).hasSize(result.size()).containsAll(result);
        families = underTest.getLicenseFamilies(LicenseFilter.approved);
        assertThat(expected).hasSize(families.size());
        licenses = underTest.getLicenses(LicenseFilter.approved);
        assertThat(expected).hasSize(licenses.size());

        cats[0] = "TheCa";
        underTest.removeApprovedLicenseCategories(Arrays.asList(cats));
        expected.removeAll(Arrays.asList(cats));
        result = underTest.getApprovedLicenseCategories();
        assertThat(expected).hasSize(result.size()).containsAll(result);
        families = underTest.getLicenseFamilies(LicenseFilter.approved);
        assertThat(expected).hasSize(families.size());
        licenses = underTest.getLicenses(LicenseFilter.approved);
        assertThat(expected).hasSize(licenses.size());    }

    @Test
    public void testRemoveBeforeAddApproveLicenseCategories() {
        underTest.addLicense( new TestingLicense("TheCat"));
        assertThat(underTest.getApprovedLicenseCategories()).isEmpty();
        assertThat(underTest.getLicenseFamilies(LicenseFilter.approved)).isEmpty();
        assertThat(underTest.getLicenses(LicenseFilter.approved)).isEmpty();
        
        underTest.removeApprovedLicenseCategory("TheCat");
        assertThat(underTest.getApprovedLicenseCategories()).isEmpty();
        assertThat(underTest.getLicenseFamilies(LicenseFilter.approved)).isEmpty();
        assertThat(underTest.getLicenses(LicenseFilter.approved)).isEmpty();

        underTest.addApprovedLicenseCategory("TheCat");
        assertThat(underTest.getApprovedLicenseCategories()).isEmpty();
        assertThat(underTest.getLicenseFamilies(LicenseFilter.approved)).isEmpty();
        assertThat(underTest.getLicenses(LicenseFilter.approved)).isEmpty();
    }

    private ILicense testingLicense(String category, String name) {
        ILicenseFamily family = ILicenseFamily.builder().setLicenseFamilyCategory(category).setLicenseFamilyName(name)
                .build();
        return new TestingLicense( family );
    }

    @Test
    public void testAddLicense() {

        List<ILicense> expected = new ArrayList<>();
        assertThat(underTest.getLicenses(LicenseFilter.all)).isEmpty();

        ILicense lic1 = testingLicense("TheCat", "TheName");
        expected.add(lic1);
        underTest.addLicense(lic1);
        SortedSet<ILicense> result = underTest.getLicenses(LicenseFilter.all);
        assertThat(expected).hasSize(result.size()).containsAll(result);

        ILicense[] lics = { testingLicense("Spot", "Data's cat"), testingLicense("Felix", "Cartoon cat") };
        expected.addAll(Arrays.asList(lics));
        underTest.addLicenses(Arrays.asList(lics));
        result = underTest.getLicenses(LicenseFilter.all);
        assertThat(expected).hasSize(result.size()).containsAll(result);
    }

    @Test
    public void copyrightMessageTest() {
        assertThat(underTest.getCopyrightMessage()).isNull();
        underTest.setCopyrightMessage("This is the message");
        assertThat(underTest.getCopyrightMessage()).isEqualTo("This is the message");
    }

    @Test
    public void inputFileFilterTest() {
        FilenameFilter filter = mock(FilenameFilter.class);
        assertThat(underTest.getInputFileFilter()).isNull();
        underTest.setInputFileFilter(filter);
        assertThat(underTest.getInputFileFilter()).isEqualTo(filter);
    }

    @Test
    public void directoryFilterTest() {
        assertThat(underTest.getDirectoryFilter()).isNotNull();
        assertThat(underTest.getDirectoryFilter()).isExactlyInstanceOf(NameBasedHiddenFileFilter.class);

        underTest.setDirectoryFilter(DirectoryFileFilter.DIRECTORY);
        underTest.addDirectoryFilter(NameBasedHiddenFileFilter.HIDDEN);
        assertThat(underTest.getDirectoryFilter()).isExactlyInstanceOf(AndFileFilter.class);

        underTest.setDirectoryFilter(null);
        assertThat(underTest.getDirectoryFilter()).isExactlyInstanceOf(FalseFileFilter.class);
    }

    @Test
    public void licenseFamiliesTest() {
        assertThat(underTest.getLicenseFamilies(LicenseFilter.all)).isEmpty();
        assertThat(underTest.getLicenseFamilies(LicenseFilter.approved)).isEmpty();
        assertThat(underTest.getLicenseFamilies(LicenseFilter.none)).isEmpty();

        ILicense[] lics = { testingLicense("TheCat", "TheName"), testingLicense("Spot", "Data's cat"),
                testingLicense("Felix", "Cartoon cat") };
        underTest.addLicenses(Arrays.asList(lics));

        assertThat(underTest.getLicenseFamilies(LicenseFilter.all)).hasSize(lics.length);
        assertThat(underTest.getLicenseFamilies(LicenseFilter.approved)).isEmpty();
        assertThat(underTest.getLicenseFamilies(LicenseFilter.none)).isEmpty();

        underTest.addApprovedLicenseCategory(lics[1].getLicenseFamily());
        assertThat(underTest.getLicenseFamilies(LicenseFilter.all)).hasSize(lics.length);
        SortedSet<ILicenseFamily> result = underTest.getLicenseFamilies(LicenseFilter.approved);
        assertThat(result).hasSize(1);
        assertThat(result.first()).isEqualTo(lics[1].getLicenseFamily());
        assertThat(underTest.getLicenseFamilies(LicenseFilter.none)).isEmpty();
    }

    @Test
    public void licensesTest() {
        assertThat(underTest.getLicenses(LicenseFilter.all)).isEmpty();
        assertThat(underTest.getLicenses(LicenseFilter.approved)).isEmpty();
        assertThat(underTest.getLicenses(LicenseFilter.none)).isEmpty();

        ILicense[] lics = { testingLicense("TheCat", "TheName"), testingLicense("Spot", "Data's cat"),
                testingLicense("Felix", "Cartoon cat") };
        underTest.addLicenses(Arrays.asList(lics));

        assertThat(underTest.getLicenses(LicenseFilter.all)).hasSize(lics.length);
        assertThat(underTest.getLicenses(LicenseFilter.approved)).isEmpty();
        assertThat(underTest.getLicenses(LicenseFilter.none)).isEmpty();

        underTest.addApprovedLicenseCategory(lics[1].getLicenseFamily());
        assertThat(underTest.getLicenses(LicenseFilter.all)).hasSize(lics.length);
        SortedSet<ILicense> result = underTest.getLicenses(LicenseFilter.approved);
        assertThat(result).hasSize(1);
        assertThat(result.first()).isEqualTo(lics[1]);
        assertThat(underTest.getLicenseFamilies(LicenseFilter.none)).isEmpty();
    }

    @Test
    public void outputTest() throws IOException {
        assertThat(underTest.getOutput().get()).isExactlyInstanceOf(NoCloseOutputStream.class);
        assertThat(underTest.getWriter()).isNotNull();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        underTest.setOut(() -> stream);
        assertThat(underTest.getOutput().get()).isEqualTo(stream);
        PrintWriter writer = underTest.getWriter().get();
        assertThat(writer).isNotNull();
        writer.write('a');
        writer.flush();
        assertThat(stream.toByteArray()[0]).isEqualTo((byte) 'a');
    }

    @Test
    public void reportableTest() {
        assertThat(underTest.getReportable()).isNull();
        IReportable reportable = mock(IReportable.class);
        underTest.setReportable(reportable);
        assertThat(underTest.getReportable()).isEqualTo(reportable);
        underTest.setReportable(null);
        assertThat(underTest.getReportable()).isNull();
    }

    @Test
    public void stylesheetTest() throws IOException, URISyntaxException {
        URL url = this.getClass().getResource("ReportConfigurationTestFile");

        assertThat(underTest.getStyleSheet()).isNull();
        InputStream stream = mock(InputStream.class);
        underTest.setStyleSheet(() -> stream);
        assertThat(underTest.getStyleSheet().get()).isEqualTo(stream);
        IOSupplier<InputStream> sup = null;
        underTest.setStyleSheet(sup);
        assertThat(underTest.getStyleSheet()).isNull();
        
        File file = mock(File.class);
        when(file.toURI()).thenReturn(url.toURI());
        underTest.setStyleSheet(file);
        BufferedReader d = new BufferedReader(new InputStreamReader(underTest.getStyleSheet().get()));
        assertThat(d.readLine()).isEqualTo("/*");
        assertThat(d.readLine()).isEqualTo(" * Licensed to the Apache Software Foundation (ASF) under one   *");
    }

    @Test
    public void testFlags() {
        assertThat(underTest.isAddingLicenses()).isFalse();
        assertThat(underTest.isAddingLicensesForced()).isFalse();
        assertThat(underTest.isStyleReport()).isTrue();

        underTest.setAddLicenseHeaders(AddLicenseHeaders.TRUE);
        assertThat(underTest.isAddingLicenses()).isTrue();
        assertThat(underTest.isAddingLicensesForced()).isFalse();
        assertThat(underTest.isStyleReport()).isTrue();

        underTest.setAddLicenseHeaders(AddLicenseHeaders.FALSE);
        assertThat(underTest.isAddingLicenses()).isFalse();
        assertThat(underTest.isAddingLicensesForced()).isFalse();
        assertThat(underTest.isStyleReport()).isTrue();

        underTest.setAddLicenseHeaders(AddLicenseHeaders.FORCED);
        assertThat(underTest.isAddingLicenses()).isTrue();
        assertThat(underTest.isAddingLicensesForced()).isTrue();
        assertThat(underTest.isStyleReport()).isTrue();

        underTest.setAddLicenseHeaders(AddLicenseHeaders.FALSE);
        underTest.setStyleReport(false);
        assertThat(underTest.isAddingLicenses()).isFalse();
        assertThat(underTest.isAddingLicensesForced()).isFalse();
        assertThat(underTest.isStyleReport()).isFalse();

        underTest.setStyleReport(true);
        assertThat(underTest.isAddingLicenses()).isFalse();
        assertThat(underTest.isAddingLicensesForced()).isFalse();
        assertThat(underTest.isStyleReport()).isTrue();
    }

    @Test
    public void testValidate() {
        final StringBuilder sb = new StringBuilder();
        try {
            underTest.validate(s -> sb.append(s));
            fail("should have thrown ConfigurationException");
        } catch (ConfigurationException e) {
            assertThat(e.getMessage()).isEqualTo("Reportable may not be null");
            assertThat(sb.length()).isEqualTo(0);
        }

        underTest.setReportable(mock(IReportable.class));
        try {
            underTest.validate(s -> sb.append(s));
            fail("should have thrown ConfigurationException");
        } catch (ConfigurationException e) {
            assertThat(e.getMessage()).isEqualTo("You must specify at least one license");
            assertThat(sb.length()).isEqualTo(0);
        }

        underTest.addLicense(testingLicense("valid", "Validation testing license"));
        try {
            underTest.validate(s -> sb.append(s));
            fail("should have thrown ConfigurationException");
        } catch (ConfigurationException e) {
            assertThat(e.getMessage()).isEqualTo("Stylesheet must be specified if report styling is selected");
            assertThat(sb.length()).isEqualTo(0);
        }

        underTest.setStyleSheet(()->mock(InputStream.class));
        underTest.setStyleReport(false);
        underTest.validate(s -> sb.append(s));
        assertThat(sb.toString()).isEqualTo("Ignoring stylesheet because styling is not selected");

        final StringBuilder sb2 = new StringBuilder();
        underTest.setStyleReport(true);
        underTest.validate(s -> sb2.append(s));
        assertThat(sb2.length()).isEqualTo(0);
    }
    
    @Test
    public void testSetOut() throws IOException {
        ReportConfiguration config = new ReportConfiguration(log);
        try (OutputStreamIntercepter osi = new OutputStreamIntercepter()) {
			config.setOut(() -> osi);
			assertThat(osi.closeCount).isEqualTo(0);
			try (OutputStream os = config.getOutput().get()) {
			    assertThat(osi.closeCount).isEqualTo(0);
			}
			assertThat(osi.closeCount).isEqualTo(1);
			try (OutputStream os = config.getOutput().get()) {
			    assertThat(osi.closeCount).isEqualTo(1);
			}
			assertThat(osi.closeCount).isEqualTo(2);
		}
    }
    
    @Test
    public void logFamilyCollisionTest() {
        // setup
        underTest.addFamily(ILicenseFamily.builder().setLicenseFamilyCategory("CAT").setLicenseFamilyName("name"));
        assertFalse(log.captured.toString().contains("CAT"));
       
        // verify default collision logs WARNING
        underTest.addFamily(ILicenseFamily.builder().setLicenseFamilyCategory("CAT").setLicenseFamilyName("name2"));
        assertTrue(log.captured.toString().contains("WARN"), ()->"default value not WARN");
        assertTrue(log.captured.toString().contains("CAT"), ()->"'CAT' not found");
        
        // verify level setting works.
        for (Level l : Level.values()) {
        log.clear();
        underTest.logFamilyCollisions(l);
        underTest.addFamily(ILicenseFamily.builder().setLicenseFamilyCategory("CAT").setLicenseFamilyName("name2"));
        assertTrue(log.captured.toString().contains("CAT"), ()->"'CAT' not found");
        assertTrue(log.captured.toString().contains(l.name()), ()->"logging not set to "+l);
        }

    }
    
    @Test
    public void familyDuplicateOptionsTest() {
        underTest.addFamily(ILicenseFamily.builder().setLicenseFamilyCategory("CAT").setLicenseFamilyName("name"));
        assertFalse(log.captured.toString().contains("CAT"));
        
        // verify default second setting ignores change
        underTest.addFamily(ILicenseFamily.builder().setLicenseFamilyCategory("CAT").setLicenseFamilyName("name2"));
        assertTrue(log.captured.toString().contains("CAT"));
        assertEquals("name", underTest.getLicenseFamilies(LicenseFilter.all).stream()
                .filter(s -> s.getFamilyCategory().equals("CAT  ")).map(s -> s.getFamilyName()).findFirst().get());
        
        underTest.familyDuplicateOption(Options.OVERWRITE);
        // verify second setting ignores change
        underTest.addFamily(ILicenseFamily.builder().setLicenseFamilyCategory("CAT").setLicenseFamilyName("name2"));
        assertTrue(log.captured.toString().contains("CAT"));
        assertEquals("name2", underTest.getLicenseFamilies(LicenseFilter.all).stream()
                .filter(s -> s.getFamilyCategory().equals("CAT  ")).map(s -> s.getFamilyName()).findFirst().get());

        // verify fail throws exception
        underTest.familyDuplicateOption(Options.FAIL);
        assertThrows( IllegalArgumentException.class, ()->underTest.addFamily(ILicenseFamily.builder().setLicenseFamilyCategory("CAT").setLicenseFamilyName("name2")));
  
        underTest.familyDuplicateOption(Options.IGNORE);
    }
    
    
    @Test
    public void logLicenseCollisionTest() {
        // setup
        ILicenseFamily family = ILicenseFamily.builder().setLicenseFamilyCategory("CAT").setLicenseFamilyName("family name").build();
        IHeaderMatcher matcher = Mockito.mock(IHeaderMatcher.class);
        when(matcher.getId()).thenReturn("Macher ID");
        underTest.addFamily(family);
        underTest.addLicense(ILicense.builder().setId("ID").setName("license name").setLicenseFamilyCategory(family.getFamilyCategory())
                .setMatcher( matcher ).build(underTest.getLicenseFamilies(LicenseFilter.all)));
        
        // verify default collistion logs WARN
        underTest.addLicense(ILicense.builder().setId("ID").setName("license name2").setLicenseFamilyCategory(family.getFamilyCategory())
                .setMatcher( matcher ).build(underTest.getLicenseFamilies(LicenseFilter.all)));
        assertTrue(log.captured.toString().contains("WARN"));
        
        log.clear();
        underTest.logLicenseCollisions(Level.ERROR);
        
        // verify second setting changes logs issue
        underTest.addLicense(ILicense.builder().setId("ID").setName("license name2").setLicenseFamilyCategory(family.getFamilyCategory())
                .setMatcher( matcher ).build(underTest.getLicenseFamilies(LicenseFilter.all)));
        assertTrue(log.captured.toString().contains("ERROR"));

    }
    
    @Test
    public void licenseDuplicateOptionsTest() {
        // setup
        ILicenseFamily family = ILicenseFamily.builder().setLicenseFamilyCategory("CAT").setLicenseFamilyName("family name").build();
        IHeaderMatcher matcher = Mockito.mock(IHeaderMatcher.class);
        when(matcher.getId()).thenReturn("Macher ID");
        underTest.addFamily(family);
        Function<String,ILicense> makeLicense = s -> ILicense.builder().setId("ID").setName(s).setLicenseFamilyCategory(family.getFamilyCategory())
                .setMatcher( matcher ).build(underTest.getLicenseFamilies(LicenseFilter.all));
                
        underTest.addLicense(makeLicense.apply("license name"));
        
        // verify default second setting ignores change
        underTest.addLicense(makeLicense.apply("license name2"));
        assertTrue(log.captured.toString().contains("WARN"));
        assertEquals("license name",
                underTest.getLicenses(LicenseFilter.all).stream().map(ILicense::getName).findFirst().get());
        
        underTest.licenseDuplicateOption(Options.OVERWRITE);
        underTest.addLicense(makeLicense.apply("license name2"));
        assertEquals("license name2",
                underTest.getLicenses(LicenseFilter.all).stream().map(ILicense::getName).findFirst().get());
        
         
        // verify fail throws exception
        underTest.licenseDuplicateOption(Options.FAIL);
        assertThrows( IllegalArgumentException.class, ()-> underTest.addLicense(makeLicense.apply("another name")));

    }
    
    /**
     * Validates that the configuration contains the default approved licenses.
     * @param config The configuration to test.
     */
    public static void validateDefaultApprovedLicenses(ReportConfiguration config) {
        validateDefaultApprovedLicenses(config, 0);
    }
    
    /**
     * Validates that the configuration contains the default approved licenses.
     * @param config The configuration to test.
     */
    public static void validateDefaultApprovedLicenses(ReportConfiguration config, int additionalIdCount) {
        assertThat(config.getApprovedLicenseCategories()).hasSize(ConfigurationReaderTest.EXPECTED_IDS.length + additionalIdCount);
        for (String s : ConfigurationReaderTest.EXPECTED_IDS) {
            assertThat(config.getApprovedLicenseCategories()).contains(ILicenseFamily.makeCategory(s));
        }
    }
    

    /**
     * Validates that the configruation contains the default license families.
     * @param config the configuration to test.
     */
    public static void validateDefaultLicenseFamilies(ReportConfiguration config, String...additionalIds) {
        assertThat(config.getLicenseFamilies(LicenseFilter.all)).hasSize(ConfigurationReaderTest.EXPECTED_IDS.length + additionalIds.length);
        List<String> expected = new ArrayList<>();
        expected.addAll(Arrays.asList(ConfigurationReaderTest.EXPECTED_IDS));
        expected.addAll(Arrays.asList(additionalIds));
        for (ILicenseFamily family : config.getLicenseFamilies(LicenseFilter.all)) {
            assertThat(expected).contains(family.getFamilyCategory().trim());
        }
    }

    /**
     * Validates that the configuration contains the default licenses.
     * @param config the configuration to test.
     */
    public static void validateDefaultLicenses(ReportConfiguration config, String...additionalLicenses) {
        assertThat(config.getLicenses(LicenseFilter.all)).hasSize(ConfigurationReaderTest.EXPECTED_LICENSES.length + additionalLicenses.length);
        List<String> expected = new ArrayList<>();
        expected.addAll(Arrays.asList(ConfigurationReaderTest.EXPECTED_LICENSES));
        expected.addAll(Arrays.asList(additionalLicenses));
        for (ILicense license : config.getLicenses(LicenseFilter.all)) {
            assertThat(expected).contains(license.getId());
        }
    }
    
    /**
     * Validates that the configuration matches the default.
     * @param config The configuration to test.
     */
    public static void validateDefault(ReportConfiguration config) {
        //config.validate( System.err::println );
        assertThat(config.isAddingLicenses()).isFalse();
        assertThat(config.isAddingLicensesForced()).isFalse();
        assertThat(config.getCopyrightMessage()).isNull();
        assertThat(config.getInputFileFilter()).isNull();
        assertThat(config.isStyleReport()).isTrue();
        assertThat(config.getStyleSheet()).isNotNull().withFailMessage("Stylesheet should not be null");
        assertThat(config.getDirectoryFilter()).isNotNull().withFailMessage("Directory filter should not be null");
        assertThat(config.getDirectoryFilter()).isExactlyInstanceOf(NameBasedHiddenFileFilter.class);
        
        validateDefaultApprovedLicenses(config);
        validateDefaultLicenseFamilies(config);
        validateDefaultLicenses(config);
    }
    
    private class LoggingCapture implements Log {

        StringBuilder captured = new StringBuilder();
        
        public void clear() {
            captured = new StringBuilder();
        }
        
        @Override
        public void log(Level level, String msg) {
            captured.append( String.format("%s: %s%n", level, msg));
        }
        
    }
    
    static class OutputStreamIntercepter extends OutputStream {
        
        int closeCount = 0;

        @Override
        public void write(int arg0) throws IOException {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void close() {
            ++closeCount;
        }
    }
}
