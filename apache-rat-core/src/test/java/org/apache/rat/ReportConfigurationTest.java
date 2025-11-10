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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.rat.ReportConfiguration.NoCloseOutputStream;
import org.apache.rat.analysis.IHeaderMatcher;
import org.apache.rat.config.AddLicenseHeaders;
import org.apache.rat.config.exclusion.StandardCollection;
import org.apache.rat.configuration.XMLConfigurationReaderTest;
import org.apache.rat.document.DocumentName;
import org.apache.rat.document.DocumentNameMatcher;
import org.apache.rat.license.ILicense;
import org.apache.rat.license.ILicenseFamily;
import org.apache.rat.license.LicenseSetFactory.LicenseFilter;
import org.apache.rat.report.IReportable;
import org.apache.rat.testhelpers.TestingLog;
import org.apache.rat.testhelpers.TestingLicense;
import org.apache.rat.testhelpers.TestingMatcher;
import org.apache.rat.utils.DefaultLog;
import org.apache.rat.utils.Log.Level;
import org.apache.rat.utils.ReportingSet.Options;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

public class ReportConfigurationTest {

    private ReportConfiguration underTest;
    private TestingLog log;

    @TempDir
    private File tempDir;

    @BeforeEach
    public void setup() {
        log = new TestingLog();
        DefaultLog.setInstance(log);
        underTest = new ReportConfiguration();
    }

    @AfterEach
    public void cleanup() {
        DefaultLog.setInstance(null);
    }

    @Test
    public void testAddIncludedFilter() {
        DocumentName dirName = DocumentName.builder(tempDir).build();
        underTest.addExcludedFilter(DirectoryFileFilter.INSTANCE);
        DocumentNameMatcher excluder = underTest.getDocumentExcluder(dirName);

        assertThat(excluder.toString()).isEqualTo("not(DirectoryFileFilter)");
        assertThat(excluder.matches(DocumentName.builder(tempDir).build())).isFalse();

        File f = new File(tempDir, "foo.txt");
        assertThat(excluder.matches(DocumentName.builder(f).build())).isTrue();
    }

    @Test
    public void testAddFamilies() {
        ILicenseFamily fam1 = ILicenseFamily.builder().setLicenseFamilyCategory("FOO").setLicenseFamilyName("found on overview").build();
        ILicenseFamily fam2 = ILicenseFamily.builder().setLicenseFamilyCategory("BAR").setLicenseFamilyName("big and round").build();
        underTest.addFamilies(Arrays.asList(fam1, fam2));
        SortedSet<String> result = underTest.getLicenseIds(LicenseFilter.ALL);
        assertThat(result).contains(ILicenseFamily.makeCategory("FOO"));
        assertThat(result).contains(ILicenseFamily.makeCategory("BAR"));
        assertThat(result).hasSize(2);
    }

    @Test
    public void testAddApprovedLicenseId() {
        underTest.addApprovedLicenseId("FOO");
        SortedSet<String> result = underTest.getLicenseIds(LicenseFilter.APPROVED);
        assertThat(result).hasSize(1).contains("FOO");
    }
    @Test
    public void testAddAndRemoveApproveLicenseCategories() {
        List<String> expected = new ArrayList<>();
        underTest.addLicense(new TestingLicense("Unapproved"));

        assertThat(underTest.getLicenseCategories(LicenseFilter.APPROVED)).isEmpty();

        TestingLicense license = new TestingLicense("TheCat");
        underTest.addLicense(license);
        underTest.addApprovedLicenseCategory(license.getFamily());
        expected.add("TheCa");
        SortedSet<String> result = underTest.getLicenseCategories(LicenseFilter.APPROVED);
        assertThat(expected).hasSize(result.size()).containsAll(result);
        SortedSet<ILicenseFamily> families = underTest.getLicenseFamilies(LicenseFilter.APPROVED);
        assertThat(expected).hasSize(families.size());
        SortedSet<ILicense> licenses = underTest.getLicenses(LicenseFilter.APPROVED);
        assertThat(expected).hasSize(licenses.size());

        underTest.addLicense(new TestingLicense("ACat"));
        underTest.addApprovedLicenseCategory("ACat");
        expected.add("ACat ");
        result = underTest.getLicenseCategories(LicenseFilter.APPROVED);
        assertThat(expected).hasSize(result.size()).containsAll(result);
        families = underTest.getLicenseFamilies(LicenseFilter.APPROVED);
        assertThat(expected).hasSize(families.size());
        licenses = underTest.getLicenses(LicenseFilter.APPROVED);
        assertThat(expected).hasSize(licenses.size());

        String[] cats = {"Spot ", "Felix"};
        underTest.addLicense(new TestingLicense("Spot"));
        underTest.addLicense(new TestingLicense("Felix"));
        underTest.addApprovedLicenseCategories(Arrays.asList(cats));
        expected.addAll(Arrays.asList(cats));
        result = underTest.getLicenseCategories(LicenseFilter.APPROVED);
        assertThat(expected).hasSize(result.size()).containsAll(result);
        families = underTest.getLicenseFamilies(LicenseFilter.APPROVED);
        assertThat(expected).hasSize(families.size());
        licenses = underTest.getLicenses(LicenseFilter.APPROVED);
        assertThat(expected).hasSize(licenses.size());

        underTest.removeApprovedLicenseCategory("Spot ");
        expected.remove("Spot ");
        result = underTest.getLicenseCategories(LicenseFilter.APPROVED);
        assertThat(expected).hasSize(result.size()).containsAll(result);
        families = underTest.getLicenseFamilies(LicenseFilter.APPROVED);
        assertThat(expected).hasSize(families.size());
        licenses = underTest.getLicenses(LicenseFilter.APPROVED);
        assertThat(expected).hasSize(licenses.size());

        cats[0] = "TheCa";
        underTest.removeApprovedLicenseCategories(Arrays.asList(cats));
        expected.removeAll(Arrays.asList(cats));
        result = underTest.getLicenseCategories(LicenseFilter.APPROVED);
        assertThat(expected).hasSize(result.size()).containsAll(result);
        families = underTest.getLicenseFamilies(LicenseFilter.APPROVED);
        assertThat(expected).hasSize(families.size());
        licenses = underTest.getLicenses(LicenseFilter.APPROVED);
        assertThat(expected).hasSize(licenses.size());
    }

    @Test
    public void testRemoveBeforeAddApproveLicenseCategories() {
        underTest.addLicense( new TestingLicense("TheCat"));
        assertThat(underTest.getLicenseCategories(LicenseFilter.APPROVED)).isEmpty();
        assertThat(underTest.getLicenseFamilies(LicenseFilter.APPROVED)).isEmpty();
        assertThat(underTest.getLicenses(LicenseFilter.APPROVED)).isEmpty();
        
        underTest.removeApprovedLicenseCategory("TheCat");
        assertThat(underTest.getLicenseCategories(LicenseFilter.APPROVED)).isEmpty();
        assertThat(underTest.getLicenseFamilies(LicenseFilter.APPROVED)).isEmpty();
        assertThat(underTest.getLicenses(LicenseFilter.APPROVED)).isEmpty();

        underTest.addApprovedLicenseCategory("TheCat");
        assertThat(underTest.getLicenseCategories(LicenseFilter.APPROVED)).isEmpty();
        assertThat(underTest.getLicenseFamilies(LicenseFilter.APPROVED)).isEmpty();
        assertThat(underTest.getLicenses(LicenseFilter.APPROVED)).isEmpty();
    }

    @Test
    public void testAddAndRemoveApproveLicenseIds() {
        List<String> expected = new ArrayList<>();
        underTest.addLicense(new TestingLicense("Unapproved"));

        assertThat(underTest.getLicenseIds(LicenseFilter.APPROVED)).isEmpty();

        TestingLicense license = new TestingLicense("TheCat");
        underTest.addLicense(license);
        underTest.addApprovedLicenseId(license.getId());
        expected.add("TheCat");
        SortedSet<String> result = underTest.getLicenseIds(LicenseFilter.APPROVED);
        assertThat(result).hasSize(expected.size()).containsAll(expected);
        SortedSet<ILicense> licenses = underTest.getLicenses(LicenseFilter.APPROVED);
        assertThat(licenses).hasSize(expected.size());

        underTest.addLicense(new TestingLicense("ACat"));
        underTest.addApprovedLicenseId("ACat");
        expected.add("ACat");
        result = underTest.getLicenseIds(LicenseFilter.APPROVED);
        assertThat(result).hasSize(expected.size()).containsAll(expected);
        licenses = underTest.getLicenses(LicenseFilter.APPROVED);
        assertThat(licenses).hasSize(expected.size());

        String[] cats = {"Spot", "Felix"};
        underTest.addLicense(new TestingLicense("Spot"));
        underTest.addLicense(new TestingLicense("Felix"));
        underTest.addApprovedLicenseIds(Arrays.asList(cats));
        expected.addAll(Arrays.asList(cats));
        result = underTest.getLicenseIds(LicenseFilter.APPROVED);
        assertThat(result).hasSize(expected.size()).containsAll(expected);
        licenses = underTest.getLicenses(LicenseFilter.APPROVED);
        assertThat(licenses).hasSize(expected.size());

        underTest.removeApprovedLicenseId("Spot");
        expected.remove("Spot");
        result = underTest.getLicenseIds(LicenseFilter.APPROVED);
        assertThat(result).hasSize(expected.size()).containsAll(expected);
        licenses = underTest.getLicenses(LicenseFilter.APPROVED);
        assertThat(licenses).hasSize(expected.size());

        cats[0] = "TheCat";
        underTest.removeApprovedLicenseIds(Arrays.asList(cats));
        expected.removeAll(Arrays.asList(cats));
        result = underTest.getLicenseIds(LicenseFilter.APPROVED);
        assertThat(result).hasSize(expected.size()).containsAll(expected);
        licenses = underTest.getLicenses(LicenseFilter.APPROVED);
        assertThat(licenses).hasSize(expected.size());
    }

    /**
     * Sets up underTest to have a set of licenses named after
     * <a href="https://en.wikipedia.org/wiki/List_of_fictional_cats_in_comics"></a>cartoon cats</a>
     * all in the license family 'catz'
     */
    private void addCatz() {
        underTest.addLicense(new TestingLicense("catz", "Garfield"));
        underTest.addLicense(new TestingLicense("catz", "Felix"));
        underTest.addLicense(new TestingLicense("catz", "Arlene"));
        underTest.addLicense(new TestingLicense("catz", "Nermal"));
        underTest.addLicense(new TestingLicense("catz", "Hobbes"));
        underTest.addLicense(new TestingLicense("catz", "Heathcliff"));
        underTest.addLicense(new TestingLicense("catz", "Catbert"));
    }

    /**
     * Sets up underTest to have a set of licenses named after
     * <a href="https://en.wikipedia.org/wiki/List_of_fictional_dogs_in_comics"></a>cartoon cats</a>cartoon dogs</a>
     * all in the license family 'dogz'
     */
    private void addDogz() {
        underTest.addLicense(new TestingLicense("dogz", "Odie"));
        underTest.addLicense(new TestingLicense("dogz", "Snoopy"));
        underTest.addLicense(new TestingLicense("dogz", "Scamp"));
        underTest.addLicense(new TestingLicense("dogz", "Marmaduke"));
        underTest.addLicense(new TestingLicense("dogz", "Rosebud"));
        underTest.addLicense(new TestingLicense("dogz", "Spike"));
        underTest.addLicense(new TestingLicense("dogz", "Dogbert"));
    }

    @Test
    public void removeFamilyAddLicense() {
        addCatz();
        underTest.addApprovedLicenseCategory("catz");
        underTest.removeApprovedLicenseCategory("catz");
        assertThat(underTest.getLicenses(LicenseFilter.APPROVED)).isEmpty();
        underTest.addApprovedLicenseId("Garfield");
        assertThat(underTest.getLicenses(LicenseFilter.APPROVED).size()).isEqualTo(1);
    }

    @Test
    public void addFamilyRemoveLicense() {
        addCatz();
        underTest.addApprovedLicenseCategory("catz");
        assertThat(underTest.getLicenses(LicenseFilter.APPROVED).size()).isEqualTo(7);
        underTest.removeApprovedLicenseId("Catbert");
        assertThat(underTest.getLicenses(LicenseFilter.APPROVED).size()).isEqualTo(6);
    }

    @Test
    public void removeFamilyRemoveLicense() {
        addCatz();
        addDogz();
        underTest.addApprovedLicenseCategory("catz");
        underTest.addApprovedLicenseCategory("dogz");
        assertThat(underTest.getLicenses(LicenseFilter.APPROVED).size()).isEqualTo(14);
        underTest.removeApprovedLicenseCategory("dogz");
        underTest.removeApprovedLicenseId("Catbert");
        assertThat(underTest.getLicenses(LicenseFilter.APPROVED).size()).isEqualTo(6);
    }

    @Test
    public void addFamilyAddLicense() {
        addCatz();
        addDogz();
        underTest.addApprovedLicenseCategory("catz");
        assertThat(underTest.getLicenses(LicenseFilter.APPROVED).size()).isEqualTo(7);
        underTest.addApprovedLicenseId("Dogbert");
        assertThat(underTest.getLicenses(LicenseFilter.APPROVED).size()).isEqualTo(8);
    }

    @Test
    public void testRemoveBeforeAddApproveLicenseIds() {
        underTest.addLicense( new TestingLicense("TheCat"));
        assertThat(underTest.getLicenseIds(LicenseFilter.APPROVED)).isEmpty();
        assertThat(underTest.getLicenses(LicenseFilter.APPROVED)).isEmpty();

        underTest.removeApprovedLicenseId("TheCat");
        assertThat(underTest.getLicenseIds(LicenseFilter.APPROVED)).isEmpty();
        assertThat(underTest.getLicenses(LicenseFilter.APPROVED)).isEmpty();

        underTest.addApprovedLicenseId("TheCat");
        assertThat(underTest.getLicenseIds(LicenseFilter.APPROVED)).isEmpty();
        assertThat(underTest.getLicenses(LicenseFilter.APPROVED)).isEmpty();
    }

    private ILicense testingLicense(String category, String name) {
        ILicenseFamily family = ILicenseFamily.builder().setLicenseFamilyCategory(category).setLicenseFamilyName(name)
                .build();
        return new TestingLicense( category, new TestingMatcher(), family );
    }

    @Test
    public void testAddLicense() {
        List<ILicense> expected = new ArrayList<>();
        assertThat(underTest.getLicenses(LicenseFilter.ALL)).isEmpty();

        ILicense lic1 = testingLicense("TheCat", "TheName");
        expected.add(lic1);
        underTest.addLicense(lic1);
        SortedSet<ILicense> result = underTest.getLicenses(LicenseFilter.ALL);
        assertThat(expected).hasSize(result.size()).containsAll(result);

        ILicense[] lics = { testingLicense("Spot", "Data's cat"), testingLicense("Felix", "Cartoon cat") };
        expected.addAll(Arrays.asList(lics));
        underTest.addLicenses(Arrays.asList(lics));
        result = underTest.getLicenses(LicenseFilter.ALL);
        assertThat(expected).hasSize(result.size()).containsAll(result);
    }

    @Test
    public void copyrightMessageTest() {
        assertThat(underTest.getCopyrightMessage()).isNull();
        underTest.setCopyrightMessage("This is the message");
        assertThat(underTest.getCopyrightMessage()).isEqualTo("This is the message");
    }

    DocumentName mkDocumentName(File f) {
        return DocumentName.builder(f).setBaseName(tempDir).build();
    }
    @Test
    public void exclusionTest() {
        DocumentName baseDir = DocumentName.builder(tempDir).build();
        DocumentName foo = mkDocumentName(new File(tempDir,"foo"));
        assertThat(underTest.getDocumentExcluder(baseDir).matches(foo)).isTrue();

        underTest.setFrom(Defaults.builder().build());

        File f = new File(tempDir, ".hiddenDir");
        assertThat(f.mkdir()).as(() -> "Could not create directory " + f).isTrue();
        DocumentName hiddenDir = mkDocumentName(new File(tempDir, ".hiddenDir"));
        DocumentNameMatcher excluder = underTest.getDocumentExcluder(baseDir);
        assertThat(excluder.matches(hiddenDir)).isFalse();

        underTest.addIncludedCollection(StandardCollection.HIDDEN_DIR);
        assertThat(underTest.getDocumentExcluder(baseDir).matches(hiddenDir)).isTrue();

        underTest.addExcludedCollection(StandardCollection.HIDDEN_DIR);
        assertThat(underTest.getDocumentExcluder(baseDir).matches(hiddenDir)).isTrue();

        underTest.addExcludedFilter(DirectoryFileFilter.DIRECTORY);

        File file = new File(tempDir, "newDir");
        assertThat(file.mkdirs()).as(() -> "Could not create directory " + file).isTrue();
        assertThat(underTest.getDocumentExcluder(baseDir).matches(mkDocumentName(file))).isFalse();
    }

    @Test
    public void archiveProcessingTest() {
        assertThat(underTest.getArchiveProcessing()).isEqualTo(ReportConfiguration.Processing.NOTIFICATION);

        underTest.setFrom(Defaults.builder().build());
        assertThat(underTest.getArchiveProcessing()).isEqualTo(ReportConfiguration.Processing.NOTIFICATION);

        underTest.setArchiveProcessing(ReportConfiguration.Processing.ABSENCE);
        assertThat(underTest.getArchiveProcessing()).isEqualTo(ReportConfiguration.Processing.ABSENCE);

        underTest.setArchiveProcessing(ReportConfiguration.Processing.PRESENCE);
        assertThat(underTest.getArchiveProcessing()).isEqualTo(ReportConfiguration.Processing.PRESENCE);

        underTest.setArchiveProcessing(null);
        assertThat(underTest.getArchiveProcessing()).isEqualTo(ReportConfiguration.Processing.NOTIFICATION);
    }

    @Test
    public void licenseFamiliesTest() {
        assertThat(underTest.getLicenseFamilies(LicenseFilter.ALL)).isEmpty();
        assertThat(underTest.getLicenseFamilies(LicenseFilter.APPROVED)).isEmpty();
        assertThat(underTest.getLicenseFamilies(LicenseFilter.NONE)).isEmpty();

        ILicense[] lics = { testingLicense("TheCat", "TheName"), testingLicense("Spot", "Data's cat"),
                testingLicense("Felix", "Cartoon cat") };
        underTest.addLicenses(Arrays.asList(lics));

        assertThat(underTest.getLicenseFamilies(LicenseFilter.ALL)).hasSize(lics.length);
        assertThat(underTest.getLicenseFamilies(LicenseFilter.APPROVED)).isEmpty();
        assertThat(underTest.getLicenseFamilies(LicenseFilter.NONE)).isEmpty();

        underTest.addApprovedLicenseCategory(lics[1].getLicenseFamily());
        assertThat(underTest.getLicenseFamilies(LicenseFilter.ALL)).hasSize(lics.length);
        SortedSet<ILicenseFamily> result = underTest.getLicenseFamilies(LicenseFilter.APPROVED);
        assertThat(result).hasSize(1);
        assertThat(result.first()).isEqualTo(lics[1].getLicenseFamily());
        assertThat(underTest.getLicenseFamilies(LicenseFilter.NONE)).isEmpty();
    }

    @Test
    public void licensesTest() {
        assertThat(underTest.getLicenses(LicenseFilter.ALL)).isEmpty();
        assertThat(underTest.getLicenses(LicenseFilter.APPROVED)).isEmpty();
        assertThat(underTest.getLicenses(LicenseFilter.NONE)).isEmpty();

        ILicense[] lics = { testingLicense("TheCat", "TheName"), testingLicense("Spot", "Data's cat"),
                testingLicense("Felix", "Cartoon cat") };
        underTest.addLicenses(Arrays.asList(lics));

        assertThat(underTest.getLicenses(LicenseFilter.ALL)).hasSize(lics.length);
        assertThat(underTest.getLicenses(LicenseFilter.APPROVED)).isEmpty();
        assertThat(underTest.getLicenses(LicenseFilter.NONE)).isEmpty();

        underTest.addApprovedLicenseCategory(lics[1].getLicenseFamily());
        assertThat(underTest.getLicenses(LicenseFilter.ALL)).hasSize(lics.length);
        SortedSet<ILicense> result = underTest.getLicenses(LicenseFilter.APPROVED);
        assertThat(result).hasSize(1);
        assertThat(result.first()).isEqualTo(lics[1]);
        assertThat(underTest.getLicenseFamilies(LicenseFilter.NONE)).isEmpty();
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
        assertThat(underTest.hasSource()).isFalse();
        IReportable reportable = mock(IReportable.class);
        underTest.addSource(reportable);
        assertThat(underTest.hasSource()).isTrue();
        assertThatThrownBy(() -> underTest.addSource((IReportable)null)).isExactlyInstanceOf(ConfigurationException.class)
                .hasMessageContaining("Reportable may not be null.");
    }

    @Test
    public void stylesheetTest() throws IOException, URISyntaxException {
        URL url = this.getClass().getResource("ReportConfigurationTestFile");
        assertThat(url).isNotNull();

        assertThat(underTest.getStyleSheet()).isNull();
        InputStream stream = mock(InputStream.class);
        underTest.setStyleSheet(() -> stream);
        assertThat(underTest.getStyleSheet().get()).isEqualTo(stream);

        File file = mock(File.class);
        URI asUri = url.toURI();
        assertThat(asUri).isNotNull();
        when(file.toURI()).thenReturn(asUri);
        underTest.setStyleSheet(file);
        BufferedReader d = new BufferedReader(new InputStreamReader(underTest.getStyleSheet().get()));
        assertThat(d.readLine()).isEqualTo("/*");
        assertThat(d.readLine()).isEqualTo(" * Licensed to the Apache Software Foundation (ASF) under one   *");
    }

    @Test
    public void testFlags() {
        assertThat(underTest.isAddingLicenses()).isFalse();
        assertThat(underTest.isAddingLicensesForced()).isFalse();

        underTest.setAddLicenseHeaders(AddLicenseHeaders.TRUE);
        assertThat(underTest.isAddingLicenses()).isTrue();
        assertThat(underTest.isAddingLicensesForced()).isFalse();

        underTest.setAddLicenseHeaders(AddLicenseHeaders.FALSE);
        assertThat(underTest.isAddingLicenses()).isFalse();
        assertThat(underTest.isAddingLicensesForced()).isFalse();

        underTest.setAddLicenseHeaders(AddLicenseHeaders.FORCED);
        assertThat(underTest.isAddingLicenses()).isTrue();
        assertThat(underTest.isAddingLicensesForced()).isTrue();
    }

    @Test
    public void testValidate() {
        final StringBuilder sb = new StringBuilder();
        String msg = "At least one source must be specified";
        assertThatThrownBy(() -> underTest.validate(sb::append)).isExactlyInstanceOf(ConfigurationException.class)
                .hasMessageContaining(msg);
        assertThat(sb.toString()).isEqualTo(msg);


        sb.setLength(0);
        msg = "You must specify at least one license";
        underTest.addSource(mock(IReportable.class));

        assertThatThrownBy(() -> underTest.validate(sb::append)).isExactlyInstanceOf(ConfigurationException.class)
                .hasMessageContaining(msg);
        assertThat(sb.toString()).isEqualTo(msg);

        sb.setLength(0);
        underTest.addLicense(testingLicense("valid", "Validation testing license"));
        underTest.validate(sb::append);
        assertThat(sb.length()).isEqualTo(0);
    }
    
    @Test
    public void testSetOut() throws IOException {
        ReportConfiguration config = new ReportConfiguration();
        try (OutputStreamInterceptor osi = new OutputStreamInterceptor()) {
            config.setOut(() -> osi);
            assertThat(osi.closeCount).isEqualTo(0);
            try (OutputStream os = config.getOutput().get()) {
                assertThat(os).isNotNull();
                assertThat(osi.closeCount).isEqualTo(0);
            }
            assertThat(osi.closeCount).isEqualTo(1);
            try (OutputStream os = config.getOutput().get()) {
                assertThat(os).isNotNull();
                assertThat(osi.closeCount).isEqualTo(1);
            }
            assertThat(osi.closeCount).isEqualTo(2);
        }
    }
    
    @Test
    public void logFamilyCollisionTest() {
        // setup
        underTest.addFamily(ILicenseFamily.builder().setLicenseFamilyCategory("CAT").setLicenseFamilyName("name"));
        assertThat(log.getCaptured()).doesNotContain("CAT");
       
        // verify default collision logs WARNING
        underTest.addFamily(ILicenseFamily.builder().setLicenseFamilyCategory("CAT").setLicenseFamilyName("name2"));
        assertThat(log.getCaptured().contains("WARN")).as("default value not WARN").isTrue();
        assertThat(log.getCaptured().contains("CAT")).as("'CAT' not found").isTrue();
        
        // verify level setting works.
        for (Level l : Level.values()) {
          log.clear();
          underTest.logFamilyCollisions(l);
          underTest.addFamily(ILicenseFamily.builder().setLicenseFamilyCategory("CAT").setLicenseFamilyName("name2"));
          assertThat(log.getCaptured().contains("CAT")).as("'CAT' not found").isTrue();
          assertThat(log.getCaptured().contains(l.name())).as("logging not set to "+l).isTrue();
        }
    }
    
    @Test
    public void familyDuplicateOptionsTest() {
        underTest.addFamily(ILicenseFamily.builder().setLicenseFamilyCategory("CAT").setLicenseFamilyName("name"));
        assertThat(log.getCaptured()).doesNotContain("CAT");
        
        // verify default second setting ignores change
        underTest.addFamily(ILicenseFamily.builder().setLicenseFamilyCategory("CAT").setLicenseFamilyName("name2"));
        assertThat(log.getCaptured()).contains("CAT");
        assertThat(underTest.getLicenseFamilies(LicenseFilter.ALL).stream()
                .filter(s -> s.getFamilyCategory().equals("CAT  ")).map(ILicenseFamily::getFamilyName).findFirst())
                .contains("name");
        
        underTest.familyDuplicateOption(Options.OVERWRITE);
        // verify second setting ignores change
        underTest.addFamily(ILicenseFamily.builder().setLicenseFamilyCategory("CAT").setLicenseFamilyName("name2"));
        assertThat(log.getCaptured()).contains("CAT");
        assertThat(underTest.getLicenseFamilies(LicenseFilter.ALL).stream()
                .filter(s -> s.getFamilyCategory().equals("CAT  ")).map(ILicenseFamily::getFamilyName).findFirst())
                .contains("name2");

        // verify fail throws exception
        underTest.familyDuplicateOption(Options.FAIL);
        assertThatThrownBy(()->underTest.addFamily(ILicenseFamily.builder().setLicenseFamilyCategory("CAT").setLicenseFamilyName("name2")))
                .isExactlyInstanceOf(IllegalArgumentException.class);

        underTest.familyDuplicateOption(Options.IGNORE);
    }

    @Test
    public void logLicenseCollisionTest() {
        // setup
        ILicenseFamily family = ILicenseFamily.builder().setLicenseFamilyCategory("CAT").setLicenseFamilyName("family name").build();
        IHeaderMatcher matcher = Mockito.mock(IHeaderMatcher.class);
        when(matcher.getId()).thenReturn("Macher ID");
        underTest.addFamily(family);
        underTest.addLicense(ILicense.builder().setId("ID").setName("license name").setFamily(family.getFamilyCategory())
                .setMatcher( matcher )
                .setLicenseFamilies(underTest.getLicenseFamilies(LicenseFilter.ALL))
                .build());
        
        // verify default collision logs WARN
        underTest.addLicense(ILicense.builder().setId("ID").setName("license name2").setFamily(family.getFamilyCategory())
                .setMatcher( matcher ).setLicenseFamilies(underTest.getLicenseFamilies(LicenseFilter.ALL))
                .build());
        assertThat(log.getCaptured()).contains("WARN");
        
        log.clear();
        underTest.logLicenseCollisions(Level.ERROR);
        
        // verify second setting changes logs issue
        underTest.addLicense(ILicense.builder().setId("ID").setName("license name2").setFamily(family.getFamilyCategory())
                .setMatcher( matcher ).setLicenseFamilies(underTest.getLicenseFamilies(LicenseFilter.ALL))
                .build());
        assertThat(log.getCaptured()).contains("ERROR");
    }
    
    @Test
    public void licenseDuplicateOptionsTest() {
        // setup
        ILicenseFamily family = ILicenseFamily.builder().setLicenseFamilyCategory("CAT").setLicenseFamilyName("family name").build();
        IHeaderMatcher matcher = Mockito.mock(IHeaderMatcher.class);
        when(matcher.getId()).thenReturn("Macher ID");
        underTest.addFamily(family);
        Function<String,ILicense> makeLicense = s -> ILicense.builder().setId("ID").setName(s).setFamily(family.getFamilyCategory())
                .setMatcher( matcher ).setLicenseFamilies(underTest.getLicenseFamilies(LicenseFilter.ALL))
                .build();
                
        underTest.addLicense(makeLicense.apply("license name"));
        
        // verify default second setting ignores change
        underTest.addLicense(makeLicense.apply("license name2"));
        assertThat(log.getCaptured()).contains("WARN");
        assertThat(underTest.getLicenses(LicenseFilter.ALL).stream().map(ILicense::getName).findFirst())
                .contains("license name");
        
        underTest.licenseDuplicateOption(Options.OVERWRITE);
        underTest.addLicense(makeLicense.apply("license name2"));
        assertThat(underTest.getLicenses(LicenseFilter.ALL).stream().map(ILicense::getName).findFirst())
                .contains("license name2");

        // verify fail throws exception
        underTest.licenseDuplicateOption(Options.FAIL);
        assertThatThrownBy(()-> underTest.addLicense(makeLicense.apply("another name")))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }


    /**
     * Validates that the configuration contains the default approved licenses.
     * @param config The configuration to test.
     */
    public static void validateDefaultApprovedLicenses(ReportConfiguration config, String... additionalIds) {
        validateLicenses(config, Arrays.asList(additionalIds), LicenseFilter.APPROVED, XMLConfigurationReaderTest.APPROVED_LICENSES);
    }

    /**
     * Validates that the configuration contains all the default licenses along with any addiitonal licenses
     * @param config the configuration to test.
     * @param additionalLicenses Additional licence IDs that are expected.
     */
    public static void validateDefaultLicenses(ReportConfiguration config, String...additionalLicenses) {
        validateLicenses(config, Arrays.asList(additionalLicenses), LicenseFilter.ALL, XMLConfigurationReaderTest.EXPECTED_LICENSES);
    }

    private static void validateLicenses(ReportConfiguration config, List<String> additionalIds, LicenseFilter filter, String[] approvedIds) {
        List<String> expected = new ArrayList<>(Arrays.asList(approvedIds));
        expected.addAll(additionalIds);
        assertThat(config.getLicenses(filter).stream().map(ILicense::getId).collect(Collectors.toSet())).containsExactlyInAnyOrderElementsOf(expected);
    }


    /**
     * Validates that the configuration contains the default license families.
     * @param config the configuration to test.
     */
    public static void validateDefaultLicenseFamilies(ReportConfiguration config, String...additionalIds) {
        validateLicenseFamilies(config, Arrays.asList(additionalIds), LicenseFilter.ALL, XMLConfigurationReaderTest.EXPECTED_IDS);
    }

    /**
     * Validates that the configuration contains the default license families.
     * @param config the configuration to test.
     */
    public static void validateDefaultApprovedLicenseFamilies(ReportConfiguration config, String...additionalIds) {
        validateLicenseFamilies(config, Arrays.asList(additionalIds), LicenseFilter.APPROVED, XMLConfigurationReaderTest.APPROVED_IDS);
    }

    private static void validateLicenseFamilies(ReportConfiguration config, List<String> additionalIds, LicenseFilter filter, String[] approvedIds) {
        List<String> expected = new ArrayList<>(Arrays.asList(approvedIds));
        expected.addAll(additionalIds);
        assertThat(config.getLicenseFamilies(filter).stream().map(lf -> lf.getFamilyCategory().trim())
                .collect(Collectors.toSet())).containsExactlyInAnyOrderElementsOf(expected);
    }

    
    /**
     * Validates that the configuration matches the default.
     * @param config The configuration to test.
     */
    public static void validateDefault(ReportConfiguration config) {
        assertThat(config.isAddingLicenses()).isFalse();
        assertThat(config.isAddingLicensesForced()).isFalse();
        assertThat(config.getCopyrightMessage()).isNull();
        assertThat(config.getStyleSheet()).withFailMessage("Stylesheet should not be null").isNotNull();

        validateDefaultLicenseFamilies(config);
        validateDefaultApprovedLicenseFamilies(config);
        validateDefaultLicenses(config);
        validateDefaultApprovedLicenses(config);
    }

    /**
     * A class to act as an output stream and count the number of close operations.
     */
    static class OutputStreamInterceptor extends OutputStream {
        int closeCount = 0;

        @Override
        public void write(int arg0) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void close() {
            ++closeCount;
        }
    }
}
