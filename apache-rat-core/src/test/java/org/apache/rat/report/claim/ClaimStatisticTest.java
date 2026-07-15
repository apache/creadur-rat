/*
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   https://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 */
package org.apache.rat.report.claim;

import org.apache.rat.api.Document;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class ClaimStatisticTest {

    @Test
    void counterTests() {
        ClaimStatistic underTest = new ClaimStatistic();
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            assertThat(underTest.getCounter(counter)).isZero();
            underTest.incCounter(counter, 1);
            assertThat(underTest.getCounter(counter)).isEqualTo(1);
            underTest.incCounter(counter, -2);
            assertThat(underTest.getCounter(counter)).isEqualTo(-1);
            underTest.setCounter(counter, Integer.MAX_VALUE);
            assertThat(underTest.getCounter(counter)).isEqualTo(Integer.MAX_VALUE);
        }
    }

    @Test
    void typeTests() {
        ClaimStatistic underTest = new ClaimStatistic();
        for (Document.Type docType : Document.Type.values()) {
            assertThat(underTest.getCounter(docType)).isZero();
            underTest.incCounter(docType, 1);
            assertThat(underTest.getCounter(docType)).isEqualTo(1);
            underTest.incCounter(docType, -2);
            assertThat(underTest.getCounter(docType)).isEqualTo(-1);
        }
    }

    @Test
    void documentTypeTest() {
        ClaimStatistic underTest = new ClaimStatistic();
        assertThat(underTest.getDocumentTypes()).isEmpty();
        underTest.incCounter(Document.Type.NOTICE, 1);
        underTest.incCounter(Document.Type.BINARY, -1);
        assertThat(underTest.getDocumentTypes()).containsExactly(Document.Type.BINARY, Document.Type.NOTICE);
    }

    @Test
    void licenseCategoryCountTest() {
        ClaimStatistic underTest = new ClaimStatistic();
        assertThat(underTest.getLicenseCategoryCount("fam")).isZero();
        underTest.incLicenseCategoryCount("fam", 1);
        assertThat(underTest.getLicenseCategoryCount("fam")).isEqualTo(1);
        underTest.incLicenseCategoryCount("fam", -2);
        assertThat(underTest.getLicenseCategoryCount("fam")).isEqualTo(-1);
        underTest.incLicenseCategoryCount("wayToLongAName", 5);
        assertThat(underTest.getLicenseCategoryCount("wayToLongAName")).isEqualTo(5);
    }

    @Test
    void licenseNameCountTest() {
        ClaimStatistic underTest = new ClaimStatistic();
        assertThat(underTest.getLicenseNameCount("fam")).isZero();
        underTest.incLicenseNameCount("fam", 1);
        assertThat(underTest.getLicenseNameCount("fam")).isEqualTo(1);
        underTest.incLicenseNameCount("fam", -2);
        assertThat(underTest.getLicenseNameCount("fam")).isEqualTo(-1);
    }

    @Test
    void licenseFamilyCategoriesTest() {
        ClaimStatistic underTest = new ClaimStatistic();
        assertThat(underTest.getLicenseFamilyCategories()).isEmpty();
        underTest.incLicenseCategoryCount("one", 1);
        underTest.incLicenseCategoryCount("neg", -1);
        assertThat(underTest.getLicenseFamilyCategories()).containsExactly("neg", "one");
        underTest.incLicenseCategoryCount("wayToLongAName", 5);
        assertThat(underTest.getLicenseFamilyCategories()).containsExactly("neg", "one", "wayToLongAName");
    }

    @Test
    void licenseFamilyNameTest() {
        ClaimStatistic underTest = new ClaimStatistic();
        assertThat(underTest.getLicenseNames()).isEmpty();
        underTest.incLicenseNameCount("one", 1);
        underTest.incLicenseNameCount("neg", -1);
        assertThat(underTest.getLicenseNames()).containsExactly("neg", "one");
        underTest.incLicenseNameCount("wayToLongAName", 5);
        assertThat(underTest.getLicenseNames()).containsExactly("neg", "one", "wayToLongAName");
    }

    /**
     * Compares two claim statistics for similarity after serialization/deserialization.
     * @param actual the deserialized version.
     * @param expected the original version.
     */
    public static void assertSame(final ClaimStatistic actual, final ClaimStatistic expected) {
        assertThat(actual.getLicenseFamilyCategories()).containsExactlyElementsOf(expected.getLicenseFamilyCategories());
        assertThat(actual.getLicenseNames()).containsExactlyElementsOf(expected.getLicenseNames());
        assertThat(actual.getDocumentTypes()).containsExactlyElementsOf(expected.getDocumentTypes());
        for (String cat : expected.getLicenseFamilyCategories()) {
            assertThat(actual.getLicenseCategoryCount(cat)).isEqualTo(expected.getLicenseCategoryCount(cat));
        }
        for (String name : expected.getLicenseNames()) {
            assertThat(actual.getLicenseNameCount(name)).isEqualTo(expected.getLicenseNameCount(name));
        }
        for (Document.Type type : expected.getDocumentTypes()) {
            assertThat(actual.getCounter(type)).isEqualTo(expected.getCounter(type));
        }
        for (ClaimStatistic.Counter counter : ClaimStatistic.Counter.values()) {
            assertThat(actual.getCounter(counter)).isEqualTo(expected.getCounter(counter));
        }
    }

    @Test
    void serdeRoundTrip() throws IOException {
        ClaimStatistic underTest = new ClaimStatistic();
        underTest.incLicenseCategoryCount("familyCategory", 1);
        underTest.incCounter(ClaimStatistic.Counter.APPROVED, 2);
        underTest.incCounter(Document.Type.IGNORED, 3);
        underTest.incLicenseNameCount("licenseName", 4);

        ClaimStatistic.SerDes serDes = underTest.serde();
        StringWriter stringWriter = new StringWriter();
        serDes.serialize(stringWriter);
        ClaimStatistic actual = new ClaimStatistic();
        ClaimStatistic.SerDes serDes2 = actual.serde();
        serDes2.deserialize(() -> new ByteArrayInputStream(stringWriter.toString().getBytes(StandardCharsets.UTF_8)));

        assertSame(actual, underTest);
    }
}
